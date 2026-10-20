package com.pdc.learning.service;

import com.pdc.learning.dto.ClassStatistics;
import com.pdc.learning.dto.ClassWarning;
import com.pdc.learning.dto.ClassWarningLevel;
import com.pdc.learning.dto.DataStatus;
import com.pdc.learning.dto.StudentMetrics;
import com.pdc.learning.dto.WarningLevel;
import com.pdc.learning.dto.WarningResult;
import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Student;
import com.pdc.learning.persistence.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 学习预警：评估 R1–R6，收集全部命中规则并取最高等级。
 */
public class WarningService {

    private final StatisticsService statisticsService;
    private final Session session;
    private final Repository<Student> studentRepository;
    private final Repository<SchoolClass> classRepository;
    private final Repository<ClassCourse> classCourseRepository;

    public WarningService(StatisticsService statisticsService,
                          Session session,
                          Repository<Student> studentRepository,
                          Repository<SchoolClass> classRepository,
                          Repository<ClassCourse> classCourseRepository) {
        this.statisticsService = statisticsService;
        this.session = session;
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
    }

    public List<WarningResult> studentWarnings(long classId) {
        findClassOrThrow(classId);
        requireClassAccess(classId);
        List<Student> students = studentRepository.findAll(
                student -> student.getClassId() == classId);
        students.sort(Comparator.comparing(Student::getStudentNo));
        List<WarningResult> results = new ArrayList<>();
        for (Student student : students) {
            results.add(evaluate(student));
        }
        return List.copyOf(results);
    }

    public ClassWarning classWarning(long classId) {
        ClassStatistics statistics = statisticsService.classStatistics(classId);
        if (statistics.dataStatus() == DataStatus.INSUFFICIENT
                || statistics.scoreCount() == 0) {
            return new ClassWarning(classId, statistics.className(),
                    ClassWarningLevel.INSUFFICIENT_DATA, null, null, 0);
        }
        boolean needsAttention = statistics.passRate() < 60
                || statistics.average() < 60;
        return new ClassWarning(classId, statistics.className(),
                needsAttention ? ClassWarningLevel.NEEDS_ATTENTION : ClassWarningLevel.NORMAL,
                statistics.passRate(), statistics.average(), statistics.scoreCount());
    }

    public WarningResult evaluateStudent(long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        requireStudentVisible(student);
        return evaluate(student);
    }

    private WarningResult evaluate(Student student) {
        StudentMetrics metrics = statisticsService.studentMetrics(student.getId());
        if (metrics.scoreCount() == 0) {
            return new WarningResult(student.getId(), student.getStudentNo(),
                    student.getName(), WarningLevel.INSUFFICIENT_DATA,
                    null, null, List.of());
        }
        double average = metrics.average();
        int failed = metrics.failedCount();
        List<String> hits = new ArrayList<>();
        WarningLevel level = WarningLevel.NORMAL;

        if (failed >= 3) {
            hits.add("R1：不及格课程数 ≥ 3（当前 " + failed + " 门）");
            level = max(level, WarningLevel.SEVERE);
        }
        if (average < 50) {
            hits.add("R2：平均分 < 50（当前 " + format(average) + "）");
            level = max(level, WarningLevel.SEVERE);
        }
        if (failed == 2) {
            hits.add("R3：不及格课程数 = 2");
            level = max(level, WarningLevel.MODERATE);
        }
        if (average < 60) {
            hits.add("R4：平均分 < 60（当前 " + format(average) + "）");
            level = max(level, WarningLevel.MODERATE);
        }
        if (failed == 1) {
            hits.add("R5：不及格课程数 = 1");
            level = max(level, WarningLevel.MILD);
        }
        if (average < 65) {
            hits.add("R6：平均分 < 65（当前 " + format(average) + "）");
            level = max(level, WarningLevel.MILD);
        }
        return new WarningResult(student.getId(), student.getStudentNo(),
                student.getName(), level, average, failed, List.copyOf(hits));
    }

    private WarningLevel max(WarningLevel current, WarningLevel candidate) {
        return severity(candidate) > severity(current) ? candidate : current;
    }

    private int severity(WarningLevel level) {
        return switch (level) {
            case SEVERE -> 3;
            case MODERATE -> 2;
            case MILD -> 1;
            case NORMAL -> 0;
            case INSUFFICIENT_DATA -> -1;
        };
    }

    private String format(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private SchoolClass findClassOrThrow(long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班级不存在"));
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
        throw new BusinessException("无权查看该班级的预警信息");
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
        throw new BusinessException("无权查看该学生的预警信息");
    }
}
