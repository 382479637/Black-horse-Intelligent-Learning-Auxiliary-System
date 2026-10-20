package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Dates;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 成绩查询、录入、修改、删除与批量录入。
 *
 * <p>唯一键为 (studentId, courseId)；courseId 由 ClassCourse 解析。
 * 已有成绩不会被新班级重复建分，修改时保留原 classCourseId。</p>
 */
public class ScoreService {

    private final Repository<Score> scoreRepository;
    private final Repository<Student> studentRepository;
    private final Repository<ClassCourse> classCourseRepository;
    private final Repository<SchoolClass> classRepository;
    private final Session session;

    public ScoreService(Repository<Score> scoreRepository,
                        Repository<Student> studentRepository,
                        Repository<ClassCourse> classCourseRepository,
                        Repository<SchoolClass> classRepository,
                        Session session) {
        this.scoreRepository = scoreRepository;
        this.studentRepository = studentRepository;
        this.classCourseRepository = classCourseRepository;
        this.classRepository = classRepository;
        this.session = session;
    }

    public List<Score> listByClassCourse(long classCourseId) {
        ClassCourse classCourse = findClassCourseOrThrow(classCourseId);
        requireAccess(classCourse);
        List<Score> scores = scoreRepository.findAll(
                score -> score.getClassCourseId() == classCourseId);
        scores.sort(Comparator.comparing(score -> studentNoOf(score.getStudentId())));
        return List.copyOf(scores);
    }

    public Optional<Score> findByStudentAndCourse(long studentId, long courseId) {
        for (Score score : scoreRepository.findAll(
                item -> item.getStudentId() == studentId)) {
            ClassCourse classCourse = classCourseRepository
                    .findById(score.getClassCourseId()).orElse(null);
            if (classCourse != null && classCourse.getCourseId() == courseId
                    && session.canAccess(classCourse)) {
                return Optional.of(score);
            }
        }
        return Optional.empty();
    }

    public Score record(long studentId, long classCourseId, double score) {
        ClassCourse target = findClassCourseOrThrow(classCourseId);
        requireAccess(target);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("学生不存在"));
        SchoolClass schoolClass = classRepository.findById(target.getClassId())
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (schoolClass.getStatus() == ClassStatus.PLANNED) {
            throw new BusinessException("PLANNED 班级尚未开课，不能录入成绩");
        }
        if (!target.isAssigned()) {
            throw new BusinessException("该教学任务尚未分配任课教师，不能录入成绩");
        }
        validateScore(score);

        Score existing = findExisting(studentId, target.getCourseId());
        if (existing != null) {
            ClassCourse existingCourse = classCourseRepository
                    .findById(existing.getClassCourseId()).orElse(null);
            if (existingCourse == null || !session.canAccess(existingCourse)) {
                throw new BusinessException("该生此课程已有成绩，且由原班级任课教师维护，"
                        + "无法在当前教学任务下修改");
            }
            existing.updateScore(score, Dates.now());
            return scoreRepository.save(existing);
        }

        if (student.getClassId() != target.getClassId()) {
            throw new BusinessException("学生当前不属于该班级，不能新录入成绩");
        }
        Score created = new Score(studentId, classCourseId, score, Dates.now());
        return scoreRepository.save(created);
    }

    public void update(long id, double score) {
        Score existing = findScoreOrThrow(id);
        ClassCourse classCourse = classCourseRepository
                .findById(existing.getClassCourseId())
                .orElseThrow(() -> new BusinessException("成绩所属教学任务不存在"));
        requireAccess(classCourse);
        validateScore(score);
        existing.updateScore(score, Dates.now());
        scoreRepository.save(existing);
    }

    public void delete(long id) {
        Score existing = findScoreOrThrow(id);
        ClassCourse classCourse = classCourseRepository
                .findById(existing.getClassCourseId())
                .orElseThrow(() -> new BusinessException("成绩所属教学任务不存在"));
        requireAccess(classCourse);
        scoreRepository.delete(id);
    }

    /**
     * 批量录入。Map 的键为学号，值为分数；非法项记录消息后继续。
     */
    public List<String> batchRecord(long classCourseId, Map<String, Double> entries) {
        ClassCourse classCourse = findClassCourseOrThrow(classCourseId);
        requireAccess(classCourse);
        List<String> messages = new ArrayList<>();
        if (entries == null || entries.isEmpty()) {
            return messages;
        }
        for (Map.Entry<String, Double> entry : new LinkedHashMap<>(entries).entrySet()) {
            String studentNo = entry.getKey() == null ? "" : entry.getKey().trim();
            Double value = entry.getValue();
            if (value == null) {
                messages.add(studentNo + "：分数不能为空");
                continue;
            }
            Optional<Student> student = studentRepository.findFirst(
                    item -> item.getStudentNo().equalsIgnoreCase(studentNo));
            if (student.isEmpty()) {
                messages.add(studentNo + "：学生不存在");
                continue;
            }
            try {
                record(student.get().getId(), classCourseId, value);
                messages.add(studentNo + "：已保存 " + value + " 分");
            } catch (BusinessException e) {
                messages.add(studentNo + "：" + e.getMessage());
            }
        }
        return List.copyOf(messages);
    }

    private Score findExisting(long studentId, long courseId) {
        for (Score score : scoreRepository.findAll(
                item -> item.getStudentId() == studentId)) {
            ClassCourse classCourse = classCourseRepository
                    .findById(score.getClassCourseId()).orElse(null);
            if (classCourse != null && classCourse.getCourseId() == courseId) {
                return score;
            }
        }
        return null;
    }

    private void validateScore(double score) {
        if (Double.isNaN(score) || score < 0 || score > 100) {
            throw new BusinessException("分数必须在 0–100 之间");
        }
    }

    private void requireAccess(ClassCourse classCourse) {
        if (!session.canAccess(classCourse)) {
            throw new BusinessException("无权操作该教学任务的成绩");
        }
    }

    private Score findScoreOrThrow(long id) {
        return scoreRepository.findById(id)
                .orElseThrow(() -> new BusinessException("成绩不存在"));
    }

    private ClassCourse findClassCourseOrThrow(long id) {
        return classCourseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("教学任务不存在"));
    }

    private String studentNoOf(long studentId) {
        return studentRepository.findById(studentId)
                .map(Student::getStudentNo)
                .orElse("");
    }
}
