package com.pdc.learning.service;

import com.pdc.learning.dto.ClassStatistics;
import com.pdc.learning.dto.CourseStatistics;
import com.pdc.learning.dto.DataStatus;
import com.pdc.learning.dto.RankEntry;
import com.pdc.learning.dto.ScoreDetail;
import com.pdc.learning.dto.StudentMetrics;
import com.pdc.learning.dto.StudentStatistics;
import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.persistence.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * 班级、课程、学生统计与排名。聚合口径严格按 Session 的可见范围。
 */
public class StatisticsService {

    private final Repository<Student> studentRepository;
    private final Repository<SchoolClass> classRepository;
    private final Repository<ClassCourse> classCourseRepository;
    private final Repository<Score> scoreRepository;
    private final Repository<Course> courseRepository;
    private final Repository<Employee> employeeRepository;
    private final Session session;

    public StatisticsService(Repository<Student> studentRepository,
                             Repository<SchoolClass> classRepository,
                             Repository<ClassCourse> classCourseRepository,
                             Repository<Score> scoreRepository,
                             Repository<Course> courseRepository,
                             Repository<Employee> employeeRepository,
                             Session session) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
        this.scoreRepository = scoreRepository;
        this.courseRepository = courseRepository;
        this.employeeRepository = employeeRepository;
        this.session = session;
    }

    public ClassStatistics classStatistics(long classId) {
        SchoolClass schoolClass = findClassOrThrow(classId);
        requireClassAccess(classId);
        List<ClassCourse> visibleCourses = visibleCoursesOfClass(classId);
        Set<Long> courseIds = idsOf(visibleCourses);
        List<Score> scores = scoresOf(courseIds);
        int studentCount = studentRepository.findAll(
                student -> student.getClassId() == classId).size();

        List<CourseStatistics> courseStatistics = new ArrayList<>();
        for (ClassCourse classCourse : visibleCourses) {
            courseStatistics.add(buildCourseStatistics(classCourse));
        }
        courseStatistics.sort(Comparator.comparing(CourseStatistics::courseCode));

        if (scores.isEmpty()) {
            return new ClassStatistics(classId, schoolClass.getName(), studentCount, 0,
                    DataStatus.INSUFFICIENT, null, null, null, null,
                    List.copyOf(courseStatistics));
        }
        Aggregate aggregate = aggregate(scores);
        return new ClassStatistics(classId, schoolClass.getName(), studentCount,
                scores.size(), DataStatus.AVAILABLE, aggregate.average(),
                aggregate.passRate(), aggregate.highest(), aggregate.lowest(),
                List.copyOf(courseStatistics));
    }

    public CourseStatistics courseStatistics(long classCourseId) {
        ClassCourse classCourse = findClassCourseOrThrow(classCourseId);
        requireAccess(classCourse);
        return buildCourseStatistics(classCourse);
    }

    public StudentStatistics studentStatistics(long studentId) {
        Student student = findStudentOrThrow(studentId);
        requireStudentVisible(student);
        List<Score> visibleScores = visibleScoresOfStudent(studentId);
        if (visibleScores.isEmpty()) {
            return new StudentStatistics(studentId, student.getStudentNo(), student.getName(),
                    0, DataStatus.INSUFFICIENT, null, null, null, "数据不足", List.of());
        }
        Aggregate aggregate = aggregate(visibleScores);
        List<ScoreDetail> details = buildDetails(visibleScores);

        Integer rank = null;
        String rankNote = null;
        for (RankEntry entry : classRanking(student.getClassId())) {
            if (entry.studentId() == studentId) {
                rank = entry.rank();
                break;
            }
        }
        if (rank == null) {
            rankNote = "无排名（本班暂无成绩）";
        }
        return new StudentStatistics(studentId, student.getStudentNo(), student.getName(),
                visibleScores.size(), DataStatus.AVAILABLE, aggregate.average(),
                aggregate.failedCount(), rank, rankNote, details);
    }

    public List<RankEntry> classRanking(long classId) {
        findClassOrThrow(classId);
        requireClassAccess(classId);
        List<ClassCourse> visibleCourses = visibleCoursesOfClass(classId);
        Set<Long> courseIds = idsOf(visibleCourses);
        List<Student> students = studentRepository.findAll(
                student -> student.getClassId() == classId);
        students.sort(Comparator.comparing(Student::getStudentNo));

        List<RankEntry> provisional = new ArrayList<>();
        for (Student student : students) {
            List<Score> scores = scoreRepository.findAll(score ->
                    score.getStudentId() == student.getId()
                            && courseIds.contains(score.getClassCourseId()));
            if (scores.isEmpty()) {
                continue;
            }
            Aggregate aggregate = aggregate(scores);
            provisional.add(new RankEntry(0, student.getId(), student.getStudentNo(),
                    student.getName(), aggregate.average(), aggregate.failedCount()));
        }
        provisional.sort(Comparator.comparingDouble(RankEntry::average).reversed()
                .thenComparing(RankEntry::studentNo));

        List<RankEntry> ranked = new ArrayList<>();
        int position = 1;
        for (RankEntry entry : provisional) {
            ranked.add(new RankEntry(position++, entry.studentId(), entry.studentNo(),
                    entry.name(), entry.average(), entry.failedCount()));
        }
        return List.copyOf(ranked);
    }

    public StudentMetrics studentMetrics(long studentId) {
        Student student = findStudentOrThrow(studentId);
        requireStudentVisible(student);
        List<Score> visibleScores = visibleScoresOfStudent(studentId);
        if (visibleScores.isEmpty()) {
            return new StudentMetrics(studentId, 0, null, null);
        }
        Aggregate aggregate = aggregate(visibleScores);
        return new StudentMetrics(studentId, visibleScores.size(), aggregate.average(),
                aggregate.failedCount());
    }

    private CourseStatistics buildCourseStatistics(ClassCourse classCourse) {
        Course course = courseRepository.findById(classCourse.getCourseId())
                .orElseThrow(() -> new BusinessException("课程不存在"));
        String teacherName = classCourse.getTeacherId() == null ? null
                : employeeRepository.findById(classCourse.getTeacherId())
                .map(Employee::getName).orElse(null);
        List<Score> scores = scoreRepository.findAll(
                score -> score.getClassCourseId() == classCourse.getId());
        if (scores.isEmpty()) {
            return new CourseStatistics(classCourse.getId(), course.getCode(),
                    course.getName(), teacherName, 0, DataStatus.INSUFFICIENT,
                    null, null, null, null);
        }
        Aggregate aggregate = aggregate(scores);
        return new CourseStatistics(classCourse.getId(), course.getCode(), course.getName(),
                teacherName, scores.size(), DataStatus.AVAILABLE, aggregate.average(),
                aggregate.passRate(), aggregate.highest(), aggregate.lowest());
    }

    private List<ScoreDetail> buildDetails(List<Score> scores) {
        List<ScoreDetail> details = new ArrayList<>();
        for (Score score : scores) {
            ClassCourse classCourse = classCourseRepository
                    .findById(score.getClassCourseId()).orElse(null);
            if (classCourse == null) {
                continue;
            }
            SchoolClass schoolClass = classRepository
                    .findById(classCourse.getClassId()).orElse(null);
            Course course = courseRepository.findById(classCourse.getCourseId()).orElse(null);
            if (schoolClass == null || course == null) {
                continue;
            }
            details.add(new ScoreDetail(schoolClass.getId(), schoolClass.getName(),
                    course.getId(), course.getCode(), course.getName(),
                    score.getScore(), score.getUpdatedAt()));
        }
        details.sort(Comparator.comparingLong(ScoreDetail::classId)
                .thenComparing(ScoreDetail::courseCode));
        return List.copyOf(details);
    }

    private List<ClassCourse> visibleCoursesOfClass(long classId) {
        List<ClassCourse> courses = classCourseRepository.findAll(
                classCourse -> classCourse.getClassId() == classId);
        courses.sort(Comparator.comparingLong(ClassCourse::getId));
        return session.filterClassCourses(courses);
    }

    private List<Score> visibleScoresOfStudent(long studentId) {
        List<Score> scores = scoreRepository.findAll(
                score -> score.getStudentId() == studentId);
        List<Score> visible = new ArrayList<>();
        for (Score score : scores) {
            Optional<ClassCourse> classCourse = classCourseRepository
                    .findById(score.getClassCourseId());
            if (classCourse.isPresent() && session.canAccess(classCourse.get())) {
                visible.add(score);
            }
        }
        return visible;
    }

    private List<Score> scoresOf(Set<Long> classCourseIds) {
        return scoreRepository.findAll(
                score -> classCourseIds.contains(score.getClassCourseId()));
    }

    private Set<Long> idsOf(List<ClassCourse> classCourses) {
        Set<Long> ids = new HashSet<>();
        for (ClassCourse classCourse : classCourses) {
            ids.add(classCourse.getId());
        }
        return ids;
    }

    private Aggregate aggregate(List<Score> scores) {
        int count = scores.size();
        double sum = 0;
        int passed = 0;
        int failed = 0;
        double highest = Double.NEGATIVE_INFINITY;
        double lowest = Double.POSITIVE_INFINITY;
        for (Score score : scores) {
            double value = score.getScore();
            sum += value;
            if (value >= 60) {
                passed++;
            } else {
                failed++;
            }
            highest = Math.max(highest, value);
            lowest = Math.min(lowest, value);
        }
        double average = sum / count;
        double passRate = passed * 100.0 / count;
        return new Aggregate(average, passRate, highest, lowest, failed);
    }

    private void requireClassAccess(long classId) {
        if (session.isAdmin()) {
            return;
        }
        if (session.isTeacher() && classCourseRepository.findFirst(classCourse ->
                classCourse.getClassId() == classId
                        && Objects.equals(classCourse.getTeacherId(),
                        session.teacherId())).isPresent()) {
            return;
        }
        throw new BusinessException("无权查看该班级的统计信息");
    }

    private void requireStudentVisible(Student student) {
        if (session.isAdmin()) {
            return;
        }
        if (session.isTeacher() && classCourseRepository.findFirst(classCourse ->
                classCourse.getClassId() == student.getClassId()
                        && Objects.equals(classCourse.getTeacherId(),
                        session.teacherId())).isPresent()) {
            return;
        }
        throw new BusinessException("无权查看该学生的统计信息");
    }

    private void requireAccess(ClassCourse classCourse) {
        if (!session.canAccess(classCourse)) {
            throw new BusinessException("无权查看该教学任务的统计信息");
        }
    }

    private SchoolClass findClassOrThrow(long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班级不存在"));
    }

    private Student findStudentOrThrow(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在"));
    }

    private ClassCourse findClassCourseOrThrow(long id) {
        return classCourseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教学任务不存在"));
    }

    private record Aggregate(double average, double passRate, double highest,
                             double lowest, int failedCount) {
    }
}
