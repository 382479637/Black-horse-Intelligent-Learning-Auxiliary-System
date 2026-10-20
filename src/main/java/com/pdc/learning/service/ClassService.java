package com.pdc.learning.service;

import com.pdc.learning.exception.BusinessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.persistence.Repository;
import com.pdc.learning.util.Validators;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 班级 CRUD、班主任、状态转换与删除检查。
 */
public class ClassService {

    private final Repository<SchoolClass> classRepository;
    private final Repository<Employee> employeeRepository;
    private final Repository<Student> studentRepository;
    private final Repository<ClassCourse> classCourseRepository;
    private final Repository<Score> scoreRepository;
    private final Session session;

    public ClassService(Repository<SchoolClass> classRepository,
                        Repository<Employee> employeeRepository,
                        Repository<Student> studentRepository,
                        Repository<ClassCourse> classCourseRepository,
                        Repository<Score> scoreRepository,
                        Session session) {
        this.classRepository = classRepository;
        this.employeeRepository = employeeRepository;
        this.studentRepository = studentRepository;
        this.classCourseRepository = classCourseRepository;
        this.scoreRepository = scoreRepository;
        this.session = session;
    }

    public List<SchoolClass> list() {
        List<SchoolClass> classes = visibleClasses();
        classes.sort(Comparator.comparing(SchoolClass::getCode));
        return List.copyOf(classes);
    }

    public List<SchoolClass> filter(String keyword, ClassStatus status, Long headTeacherId) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        List<SchoolClass> result = visibleClasses();
        result.removeIf(schoolClass ->
                (!needle.isEmpty()
                        && !schoolClass.getCode().toLowerCase().contains(needle)
                        && !schoolClass.getName().toLowerCase().contains(needle))
                        || (status != null && schoolClass.getStatus() != status)
                        || (headTeacherId != null
                        && !Objects.equals(schoolClass.getHeadTeacherId(), headTeacherId)));
        result.sort(Comparator.comparing(SchoolClass::getCode));
        return List.copyOf(result);
    }

    public SchoolClass create(String code,
                              String name,
                              Long headTeacherId,
                              LocalDate startDate,
                              LocalDate endDate,
                              int capacity) {
        session.requireAdmin();
        String validCode = Validators.requireCode(code, "班级编号");
        String validName = Validators.requireName(name);
        validateDates(startDate, endDate);
        validateCapacity(capacity, 0);
        validateHeadTeacher(headTeacherId);
        ensureCodeUnique(validCode, -1);
        SchoolClass schoolClass = new SchoolClass(validCode, validName, headTeacherId,
                startDate, endDate, capacity, ClassStatus.PLANNED);
        return classRepository.save(schoolClass);
    }

    public void update(long id,
                       String code,
                       String name,
                       Long headTeacherId,
                       LocalDate startDate,
                       LocalDate endDate,
                       int capacity) {
        session.requireAdmin();
        SchoolClass schoolClass = findOrThrow(id);
        String validCode = Validators.requireCode(code, "班级编号");
        String validName = Validators.requireName(name);
        validateDates(startDate, endDate);
        validateCapacity(capacity, currentStudentCount(id));
        validateHeadTeacher(headTeacherId);
        ensureCodeUnique(validCode, id);
        schoolClass.updateDetails(validCode, validName, startDate, endDate, capacity);
        schoolClass.assignHeadTeacher(headTeacherId);
        classRepository.save(schoolClass);
    }

    public void delete(long id) {
        session.requireAdmin();
        SchoolClass schoolClass = findOrThrow(id);
        if (schoolClass.isFinished()) {
            throw new BusinessException("已结业（FINISHED）的班级不得删除");
        }
        long students = currentStudentCount(id);
        if (students > 0) {
            throw new BusinessException("班级仍有 " + students
                    + " 名学生，请先转出学生后再删除");
        }
        if (hasScores(id)) {
            throw new BusinessException("班级的教学任务已有成绩，不能删除");
        }
        List<ClassCourse> classCourses = classCourseRepository.findAll(
                classCourse -> classCourse.getClassId() == id);
        for (ClassCourse classCourse : classCourses) {
            classCourseRepository.delete(classCourse.getId());
        }
        classRepository.delete(id);
    }

    public void changeStatus(long id, ClassStatus target) {
        session.requireAdmin();
        if (target == null) {
            throw new BusinessException("请选择目标状态");
        }
        SchoolClass schoolClass = findOrThrow(id);
        ClassStatus current = schoolClass.getStatus();
        if (current == target) {
            return;
        }
        boolean allowed = (current == ClassStatus.PLANNED && target == ClassStatus.ACTIVE)
                || (current == ClassStatus.ACTIVE && target == ClassStatus.FINISHED);
        if (!allowed) {
            throw new BusinessException("班级状态只能按 PLANNED → ACTIVE → FINISHED 转换，"
                    + "不允许从 " + current + " 转为 " + target);
        }
        schoolClass.changeStatus(target);
        classRepository.save(schoolClass);
    }

    public void assignHeadTeacher(long classId, Long teacherId) {
        session.requireAdmin();
        SchoolClass schoolClass = findOrThrow(classId);
        validateHeadTeacher(teacherId);
        schoolClass.assignHeadTeacher(teacherId);
        classRepository.save(schoolClass);
    }

    private List<SchoolClass> visibleClasses() {
        if (session.isAdmin()) {
            return new ArrayList<>(classRepository.findAll());
        }
        if (!session.isTeacher()) {
            return new ArrayList<>();
        }
        long teacherId = session.teacherId();
        List<SchoolClass> result = new ArrayList<>();
        for (SchoolClass schoolClass : classRepository.findAll()) {
            boolean teaches = classCourseRepository.findFirst(classCourse ->
                    classCourse.getClassId() == schoolClass.getId()
                            && Objects.equals(classCourse.getTeacherId(), teacherId)).isPresent();
            if (teaches) {
                result.add(schoolClass);
            }
        }
        return result;
    }

    private SchoolClass findOrThrow(long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new BusinessException("班级不存在"));
    }

    private void ensureCodeUnique(String code, long excludeId) {
        boolean duplicate = classRepository.findAll(schoolClass ->
                        schoolClass.getId() != excludeId
                                && schoolClass.getCode().equalsIgnoreCase(code))
                .size() > 0;
        if (duplicate) {
            throw new BusinessException("班级编号已存在，请使用其他编号");
        }
    }

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BusinessException("班级开始日期和结束日期不能为空");
        }
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("班级开始日期不能晚于结束日期");
        }
    }

    private void validateCapacity(int capacity, long currentCount) {
        if (capacity <= 0) {
            throw new BusinessException("容量必须是正整数");
        }
        if (capacity < currentCount) {
            throw new BusinessException("容量不能低于当前在班人数（" + currentCount + " 人）");
        }
    }

    private void validateHeadTeacher(Long teacherId) {
        if (teacherId == null) {
            return;
        }
        Employee teacher = employeeRepository.findById(teacherId)
                .orElseThrow(() -> new BusinessException("所选班主任不存在"));
        if (!teacher.canTeach()) {
            throw new BusinessException("班主任必须是启用状态的教师");
        }
    }

    private long currentStudentCount(long classId) {
        return studentRepository.findAll(student -> student.getClassId() == classId).size();
    }

    private boolean hasScores(long classId) {
        for (ClassCourse classCourse : classCourseRepository.findAll(
                item -> item.getClassId() == classId)) {
            if (scoreRepository.findFirst(
                    score -> score.getClassCourseId() == classCourse.getId()).isPresent()) {
                return true;
            }
        }
        return false;
    }
}
