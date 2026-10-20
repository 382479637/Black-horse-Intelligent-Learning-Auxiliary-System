package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.Course;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Validators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 课程目录 CRUD。读取对教师开放，写操作仅管理员。
 */
public class CourseService {

    private final Repository<Course> courseRepository;
    private final Repository<com.pdc.learning.model.ClassCourse> classCourseRepository;
    private final Repository<com.pdc.learning.model.Score> scoreRepository;
    private final Session session;

    public CourseService(Repository<Course> courseRepository,
                         Repository<com.pdc.learning.model.ClassCourse> classCourseRepository,
                         Repository<com.pdc.learning.model.Score> scoreRepository,
                         Session session) {
        this.courseRepository = courseRepository;
        this.classCourseRepository = classCourseRepository;
        this.scoreRepository = scoreRepository;
        this.session = session;
    }

    public List<Course> list() {
        List<Course> courses = new ArrayList<>(courseRepository.findAll());
        courses.sort(Comparator.comparing(Course::getCode));
        return List.copyOf(courses);
    }

    public Course create(String code, String name, String description) {
        session.requireAdmin();
        String validCode = Validators.requireCode(code, "课程编号");
        String validName = Validators.requireName(name);
        String validDescription = Validators.optionalDescription(description);
        ensureCodeUnique(validCode, -1);
        return courseRepository.save(new Course(validCode, validName, validDescription));
    }

    public void update(long id, String code, String name, String description) {
        session.requireAdmin();
        Course course = findOrThrow(id);
        String validCode = Validators.requireCode(code, "课程编号");
        String validName = Validators.requireName(name);
        String validDescription = Validators.optionalDescription(description);
        ensureCodeUnique(validCode, id);
        course.update(validCode, validName, validDescription);
        courseRepository.save(course);
    }

    public void delete(long id) {
        session.requireAdmin();
        Course course = findOrThrow(id);
        if (classCourseRepository.findFirst(
                classCourse -> classCourse.getCourseId() == id).isPresent()) {
            throw new BusinessException("该课程已被班级选用，不能删除");
        }
        if (scoreRepository.findAll(score -> classCourseRepository
                .findById(score.getClassCourseId())
                .map(classCourse -> classCourse.getCourseId() == id)
                .orElse(false)).size() > 0) {
            throw new BusinessException("该课程仍有成绩记录，不能删除");
        }
        courseRepository.delete(course.getId());
    }

    private Course findOrThrow(long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("课程不存在"));
    }

    private void ensureCodeUnique(String code, long excludeId) {
        boolean duplicate = courseRepository.findAll(course ->
                        course.getId() != excludeId
                                && course.getCode().equalsIgnoreCase(code))
                .size() > 0;
        if (duplicate) {
            throw new BusinessException("课程编号已存在，请使用其他编号");
        }
    }
}
