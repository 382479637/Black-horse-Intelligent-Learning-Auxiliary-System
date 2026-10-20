package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Dates;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 教学任务（班级-课程-教师）管理。
 */
public class TeachingService {

    private final Repository<ClassCourse> classCourseRepository;
    private final Repository<SchoolClass> classRepository;
    private final Repository<Course> courseRepository;
    private final Repository<Employee> employeeRepository;
    private final Repository<Score> scoreRepository;
    private final Session session;

    public TeachingService(Repository<ClassCourse> classCourseRepository,
                           Repository<SchoolClass> classRepository,
                           Repository<Course> courseRepository,
                           Repository<Employee> employeeRepository,
                           Repository<Score> scoreRepository,
                           Session session) {
        this.classCourseRepository = classCourseRepository;
        this.classRepository = classRepository;
        this.courseRepository = courseRepository;
        this.employeeRepository = employeeRepository;
        this.scoreRepository = scoreRepository;
        this.session = session;
    }

    public List<ClassCourse> listByClass(long classId) {
        findClassOrThrow(classId);
        if (session.isTeacher() && !teachesClass(classId)) {
            throw new BusinessException("无权查看该班级的教学任务");
        }
        List<ClassCourse> result = classCourseRepository.findAll(
                classCourse -> classCourse.getClassId() == classId
                        && (!session.isTeacher()
                        || Objects.equals(classCourse.getTeacherId(), session.teacherId())));
        result.sort(Comparator.comparingLong(ClassCourse::getId));
        return result;
    }

    public ClassCourse addCourse(long classId, long courseId) {
        session.requireAdmin();
        SchoolClass schoolClass = findClassOrThrow(classId);
        if (schoolClass.getStatus() == ClassStatus.FINISHED) {
            throw new BusinessException("已结业班级不得新增教学任务");
        }
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException("课程不存在"));
        if (classCourseRepository.findFirst(classCourse ->
                classCourse.getClassId() == classId
                        && classCourse.getCourseId() == courseId).isPresent()) {
            throw new BusinessException("该班级已配置课程 " + course.getCode() + "，不能重复添加");
        }
        ClassCourse classCourse = new ClassCourse(classId, courseId, null, Dates.now());
        return classCourseRepository.save(classCourse);
    }

    public void assignTeacher(long classCourseId, Long teacherId) {
        session.requireAdmin();
        ClassCourse classCourse = findOrThrow(classCourseId);
        if (teacherId == null) {
            classCourse.unassignTeacher();
        } else {
            Employee teacher = employeeRepository.findById(teacherId)
                    .orElseThrow(() -> new BusinessException("所选教师不存在"));
            if (!teacher.canTeach()) {
                throw new BusinessException("任课教师必须是启用状态的教师");
            }
            classCourse.assignTeacher(teacherId);
        }
        classCourseRepository.save(classCourse);
    }

    public void removeCourse(long classCourseId) {
        session.requireAdmin();
        ClassCourse classCourse = findOrThrow(classCourseId);
        SchoolClass schoolClass = findClassOrThrow(classCourse.getClassId());
        if (schoolClass.getStatus() == ClassStatus.FINISHED) {
            throw new BusinessException("已结业班级不得移除教学任务");
        }
        if (scoreRepository.findFirst(
                score -> score.getClassCourseId() == classCourseId).isPresent()) {
            throw new BusinessException("该教学任务已有成绩，不能移除");
        }
        classCourseRepository.delete(classCourseId);
    }

    public List<ClassCourse> listByTeacher(long teacherId) {
        if (session.isTeacher() && !Objects.equals(session.teacherId(), teacherId)) {
            throw new BusinessException("只能查看本人的教学任务");
        }
        List<ClassCourse> result = classCourseRepository.findAll(
                classCourse -> Objects.equals(classCourse.getTeacherId(), teacherId));
        result.sort(Comparator.comparingLong(ClassCourse::getId));
        return result;
    }

    private ClassCourse findOrThrow(long id) {
        return classCourseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教学任务不存在"));
    }

    private SchoolClass findClassOrThrow(long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班级不存在"));
    }

    private boolean teachesClass(long classId) {
        Long teacherId = session.teacherId();
        return classCourseRepository.findFirst(classCourse ->
                classCourse.getClassId() == classId
                        && Objects.equals(classCourse.getTeacherId(), teacherId)).isPresent();
    }
}
