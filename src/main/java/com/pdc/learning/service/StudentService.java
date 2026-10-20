package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.StudentStatus;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 学生 CRUD、转班与状态管理。
 */
public class StudentService {

    private final Repository<Student> studentRepository;
    private final Repository<SchoolClass> classRepository;
    private final Repository<ClassCourse> classCourseRepository;
    private final Repository<Score> scoreRepository;
    private final Session session;

    public StudentService(Repository<Student> studentRepository,
                          Repository<SchoolClass> classRepository,
                          Repository<ClassCourse> classCourseRepository,
                          Repository<Score> scoreRepository,
                          Session session) {
        this.studentRepository = studentRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
        this.scoreRepository = scoreRepository;
        this.session = session;
    }

    public List<Student> list() {
        List<Student> students = visibleStudents();
        students.sort(Comparator.comparing(Student::getStudentNo));
        return List.copyOf(students);
    }

    public List<Student> search(String keyword, Long classId, StudentStatus status) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<Student> result = visibleStudents();
        result.removeIf(student ->
                (!needle.isEmpty()
                        && !student.getStudentNo().toLowerCase().contains(needle)
                        && !student.getName().toLowerCase().contains(needle))
                        || (classId != null && student.getClassId() != classId)
                        || (status != null && student.getStatus() != status));
        result.sort(Comparator.comparing(Student::getStudentNo));
        return List.copyOf(result);
    }

    public List<Student> listByClass(long classId) {
        SchoolClass schoolClass = findClassOrThrow(classId);
        if (session.isTeacher() && !teachesClass(schoolClass.getId())) {
            throw new BusinessException("无权查看该班级的学生");
        }
        List<Student> result = studentRepository.findAll(
                student -> student.getClassId() == classId);
        result.sort(Comparator.comparing(Student::getStudentNo));
        return List.copyOf(result);
    }

    public Student create(String studentNo,
                          String name,
                          Gender gender,
                          String phone,
                          long classId,
                          LocalDate enrollmentDate) {
        session.requireAdmin();
        String validNo = Validators.requireCode(studentNo, "学号");
        String validName = Validators.requireName(name);
        String validPhone = Validators.optionalPhone(phone);
        if (enrollmentDate == null) {
            throw new BusinessException("入学日期不能为空");
        }
        SchoolClass schoolClass = findClassOrThrow(classId);
        if (!schoolClass.canAcceptStudent()) {
            throw new BusinessException("已结业班级不能新增学生");
        }
        long currentCount = currentStudentCount(classId);
        if (schoolClass.isFull((int) currentCount)) {
            throw new BusinessException("班级人数已满（容量 " + schoolClass.getCapacity() + "）");
        }
        ensureStudentNoUnique(validNo, -1);
        Student student = new Student(validNo, validName, gender, validPhone,
                classId, enrollmentDate);
        return studentRepository.save(student);
    }

    public void update(long id, String name, Gender gender, String phone) {
        session.requireAdmin();
        Student student = findOrThrow(id);
        String validName = Validators.requireName(name);
        String validPhone = Validators.optionalPhone(phone);
        student.updateProfile(validName, gender, validPhone);
        studentRepository.save(student);
    }

    public void delete(long id) {
        session.requireAdmin();
        Student student = findOrThrow(id);
        SchoolClass currentClass = findClassOrThrow(student.getClassId());
        if (currentClass.isFinished()) {
            throw new BusinessException("已结业班级的学生不得删除");
        }
        if (hasScoreInFinishedClass(id)) {
            throw new BusinessException("该生在已结业班级的教学任务下有成绩，不得删除");
        }
        for (Score score : scoreRepository.findAll(
                item -> item.getStudentId() == id)) {
            scoreRepository.delete(score.getId());
        }
        studentRepository.delete(id);
    }

    public void transfer(long studentId, long targetClassId) {
        session.requireAdmin();
        Student student = findOrThrow(studentId);
        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new BusinessException("只有 ACTIVE 状态的学生可以转班，请先恢复状态");
        }
        SchoolClass currentClass = findClassOrThrow(student.getClassId());
        if (currentClass.isFinished()) {
            throw new BusinessException("已结业班级的学生不能转出");
        }
        if (student.getClassId() == targetClassId) {
            throw new BusinessException("学生已在该班级中");
        }
        SchoolClass targetClass = findClassOrThrow(targetClassId);
        if (!targetClass.canAcceptStudent()) {
            throw new BusinessException("目标班级已结业，不能接收转入学生");
        }
        long targetCount = currentStudentCount(targetClassId);
        if (targetClass.isFull((int) targetCount)) {
            throw new BusinessException("目标班级人数已满（容量 "
                    + targetClass.getCapacity() + "）");
        }
        student.transferTo(targetClassId);
        studentRepository.save(student);
    }

    public void changeStatus(long id, StudentStatus status) {
        session.requireAdmin();
        if (status == null) {
            throw new BusinessException("请选择学生状态");
        }
        Student student = findOrThrow(id);
        SchoolClass schoolClass = findClassOrThrow(student.getClassId());
        if (schoolClass.getStatus() == ClassStatus.FINISHED
                && status == StudentStatus.ACTIVE) {
            throw new BusinessException("已结业班级的学生不能改为 ACTIVE，"
                    + "只能保持原状态或设为 GRADUATED");
        }
        student.changeStatus(status);
        studentRepository.save(student);
    }

    private List<Student> visibleStudents() {
        if (session.isAdmin()) {
            return new ArrayList<>(studentRepository.findAll());
        }
        if (!session.isTeacher()) {
            return new ArrayList<>();
        }
        List<Student> result = new ArrayList<>();
        for (Student student : studentRepository.findAll()) {
            if (teachesClass(student.getClassId())) {
                result.add(student);
            }
        }
        return result;
    }

    private boolean teachesClass(long classId) {
        Long teacherId = session.teacherId();
        return classCourseRepository.findFirst(classCourse ->
                classCourse.getClassId() == classId
                        && Objects.equals(classCourse.getTeacherId(), teacherId)).isPresent();
    }

    private boolean hasScoreInFinishedClass(long studentId) {
        for (Score score : scoreRepository.findAll(
                item -> item.getStudentId() == studentId)) {
            ClassCourse classCourse = classCourseRepository
                    .findById(score.getClassCourseId()).orElse(null);
            if (classCourse == null) {
                continue;
            }
            SchoolClass schoolClass = classRepository
                    .findById(classCourse.getClassId()).orElse(null);
            if (schoolClass != null && schoolClass.isFinished()) {
                return true;
            }
        }
        return false;
    }

    private Student findOrThrow(long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在"));
    }

    private SchoolClass findClassOrThrow(long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班级不存在"));
    }

    private long currentStudentCount(long classId) {
        return studentRepository.findAll(student -> student.getClassId() == classId).size();
    }

    private void ensureStudentNoUnique(String studentNo, long excludeId) {
        boolean duplicate = studentRepository.findAll(student ->
                        student.getId() != excludeId
                                && student.getStudentNo().equalsIgnoreCase(studentNo))
                .size() > 0;
        if (duplicate) {
            throw new BusinessException("学号已存在，请使用其他学号");
        }
    }
}
