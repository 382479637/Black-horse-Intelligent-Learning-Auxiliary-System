package com.pdc.learning.persistence;

import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.persistence.csv.CsvRepository;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 按固定顺序加载全部 CSV，并校验引用完整性与成绩唯一性。
 *
 * <p>引用缺失的实体从内存中移除（不写回文件），保证后续业务逻辑只面对有效数据。</p>
 */
public class DataLoader {

    private final CsvRepository<Department> departmentRepository;
    private final CsvRepository<Employee> employeeRepository;
    private final CsvRepository<Course> courseRepository;
    private final CsvRepository<SchoolClass> classRepository;
    private final CsvRepository<ClassCourse> classCourseRepository;
    private final CsvRepository<Student> studentRepository;
    private final CsvRepository<Score> scoreRepository;
    private final LoadDiagnostics diagnostics;

    public DataLoader(CsvRepository<Department> departmentRepository,
                      CsvRepository<Employee> employeeRepository,
                      CsvRepository<Course> courseRepository,
                      CsvRepository<SchoolClass> classRepository,
                      CsvRepository<ClassCourse> classCourseRepository,
                      CsvRepository<Student> studentRepository,
                      CsvRepository<Score> scoreRepository,
                      LoadDiagnostics diagnostics) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.diagnostics = diagnostics;
    }

    public void loadAll() {
        departmentRepository.load(diagnostics);
        employeeRepository.load(diagnostics);
        courseRepository.load(diagnostics);
        classRepository.load(diagnostics);
        classCourseRepository.load(diagnostics);
        studentRepository.load(diagnostics);
        scoreRepository.load(diagnostics);
        validateEmployees();
        validateClasses();
        validateClassCourses();
        validateStudents();
        validateScores();
    }

    private void validateEmployees() {
        for (Employee employee : employeeRepository.findAll()) {
            if (departmentRepository.findById(employee.getDepartmentId()).isEmpty()) {
                employeeRepository.removeInMemory(employee.getId());
                diagnostics.add("employees.csv", employeeRepository.lineOf(employee.getId()),
                        "reference", "departmentId=" + employee.getDepartmentId()
                                + " 不存在，员工记录已移除");
            }
        }
    }

    private void validateClasses() {
        for (SchoolClass schoolClass : classRepository.findAll()) {
            Long headTeacherId = schoolClass.getHeadTeacherId();
            if (headTeacherId == null) {
                continue;
            }
            Optional<Employee> teacher = employeeRepository.findById(headTeacherId);
            if (teacher.isEmpty() || !teacher.get().canTeach()) {
                schoolClass.assignHeadTeacher(null);
                diagnostics.add("classes.csv", classRepository.lineOf(schoolClass.getId()),
                        "optional-ref", "headTeacherId=" + headTeacherId
                                + " 不可用，班主任已置空");
            }
        }
    }

    private void validateClassCourses() {
        for (ClassCourse classCourse : classCourseRepository.findAll()) {
            if (!classRepository.findById(classCourse.getClassId()).isPresent()
                    || !courseRepository.findById(classCourse.getCourseId()).isPresent()) {
                classCourseRepository.removeInMemory(classCourse.getId());
                diagnostics.add("class_courses.csv", classCourseRepository.lineOf(classCourse.getId()),
                        "reference", "班级或课程引用不存在，开课记录已移除");
                continue;
            }
            Long teacherId = classCourse.getTeacherId();
            if (teacherId != null) {
                Optional<Employee> teacher = employeeRepository.findById(teacherId);
                if (teacher.isEmpty() || !teacher.get().canTeach()) {
                    classCourse.unassignTeacher();
                    diagnostics.add("class_courses.csv",
                            classCourseRepository.lineOf(classCourse.getId()),
                            "optional-ref", "teacherId=" + teacherId + " 不可用，任课教师已置空");
                }
            }
        }
    }

    private void validateStudents() {
        for (Student student : studentRepository.findAll()) {
            if (!classRepository.findById(student.getClassId()).isPresent()) {
                studentRepository.removeInMemory(student.getId());
                diagnostics.add("students.csv", studentRepository.lineOf(student.getId()),
                        "reference", "classId=" + student.getClassId() + " 不存在，学生记录已移除");
            }
        }
    }

    private void validateScores() {
        Map<Long, ClassCourse> classCourses = new HashMap<>();
        for (ClassCourse classCourse : classCourseRepository.findAll()) {
            classCourses.put(classCourse.getId(), classCourse);
        }
        Set<String> seen = new HashSet<>();
        for (Score score : scoreRepository.findAll()) {
            if (!studentRepository.findById(score.getStudentId()).isPresent()) {
                scoreRepository.removeInMemory(score.getId());
                diagnostics.add("scores.csv", scoreRepository.lineOf(score.getId()),
                        "reference", "studentId=" + score.getStudentId() + " 不存在，成绩已移除");
                continue;
            }
            ClassCourse classCourse = classCourses.get(score.getClassCourseId());
            if (classCourse == null) {
                scoreRepository.removeInMemory(score.getId());
                diagnostics.add("scores.csv", scoreRepository.lineOf(score.getId()),
                        "reference", "classCourseId=" + score.getClassCourseId()
                                + " 不存在，成绩已移除");
                continue;
            }
            String key = score.getStudentId() + ":" + classCourse.getCourseId();
            if (!seen.add(key)) {
                scoreRepository.removeInMemory(score.getId());
                diagnostics.add("scores.csv", scoreRepository.lineOf(score.getId()),
                        "unique", "同一学生同一课程存在重复成绩，保留首条");
            }
        }
    }
}
