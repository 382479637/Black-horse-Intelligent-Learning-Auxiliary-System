package com.pdc.learning.persistence;

import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.model.enums.ClassStatus;
import com.pdc.learning.model.enums.EmployeeStatus;
import com.pdc.learning.model.enums.Gender;
import com.pdc.learning.model.enums.Role;
import com.pdc.learning.model.enums.StudentStatus;
import com.pdc.learning.persistence.csv.CsvRepository;
import com.pdc.learning.util.CsvUtil;
import com.pdc.learning.util.PasswordHasher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 首次运行的目录/文件初始化、演示数据与管理员兜底。
 */
public class DataInitializer {

    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    private static final String DEMO_PASSWORD = "Teach@123";

    private final CsvRepository<Department> departmentRepository;
    private final CsvRepository<Employee> employeeRepository;
    private final CsvRepository<Course> courseRepository;
    private final CsvRepository<SchoolClass> classRepository;
    private final CsvRepository<ClassCourse> classCourseRepository;
    private final CsvRepository<Student> studentRepository;
    private final CsvRepository<Score> scoreRepository;
    private final PasswordHasher passwordHasher;
    private final LoadDiagnostics diagnostics;

    public DataInitializer(CsvRepository<Department> departmentRepository,
                           CsvRepository<Employee> employeeRepository,
                           CsvRepository<Course> courseRepository,
                           CsvRepository<SchoolClass> classRepository,
                           CsvRepository<ClassCourse> classCourseRepository,
                           CsvRepository<Student> studentRepository,
                           CsvRepository<Score> scoreRepository,
                           PasswordHasher passwordHasher,
                           LoadDiagnostics diagnostics) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.courseRepository = courseRepository;
        this.classRepository = classRepository;
        this.classCourseRepository = classCourseRepository;
        this.studentRepository = studentRepository;
        this.scoreRepository = scoreRepository;
        this.passwordHasher = passwordHasher;
        this.diagnostics = diagnostics;
    }

    /**
     * 建立 data 目录与 7 个 CSV；空文件写入表头，已有内容不覆盖。
     */
    public void ensureLayout(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            ensureFile(departmentRepository, dataDir);
            ensureFile(employeeRepository, dataDir);
            ensureFile(courseRepository, dataDir);
            ensureFile(classRepository, dataDir);
            ensureFile(classCourseRepository, dataDir);
            ensureFile(studentRepository, dataDir);
            ensureFile(scoreRepository, dataDir);
        } catch (IOException e) {
            throw new com.pdc.learning.exception.DataAccessException("初始化数据目录失败：" + dataDir, e);
        }
    }

    private void ensureFile(CsvRepository<?> repository, Path dataDir) throws IOException {
        Path file = dataDir.resolve(repository.file().getFileName());
        if (!Files.exists(file) || Files.size(file) == 0) {
            Files.writeString(file, CsvUtil.join(repository.header()) + System.lineSeparator(),
                    StandardCharsets.UTF_8);
        }
    }

    /**
     * 所有 CSV 均无数据行时为 true。
     */
    public boolean isFresh() {
        return isFileEmpty(departmentRepository.file())
                && isFileEmpty(employeeRepository.file())
                && isFileEmpty(courseRepository.file())
                && isFileEmpty(classRepository.file())
                && isFileEmpty(classCourseRepository.file())
                && isFileEmpty(studentRepository.file())
                && isFileEmpty(scoreRepository.file());
    }

    private boolean isFileEmpty(Path file) {
        if (!Files.exists(file)) {
            return true;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            boolean first = true;
            for (String line : lines) {
                if (first) {
                    first = false;
                    continue;
                }
                if (!line.isBlank()) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            // 无法判断时按“已有数据”处理，避免覆盖用户文件
            return false;
        }
    }

    /**
     * 写入 FR-SYS-11 规定的演示数据。
     */
    public void seedDemoData() {
        Department academic = departmentRepository.save(
                new Department("教务部", "负责教学运行与学籍管理"));
        Department computer = departmentRepository.save(
                new Department("计算机学院", "计算机类专业教学单位"));
        Department math = departmentRepository.save(
                new Department("数学学院", "公共数学课程教学单位"));

        createEmployee("admin", DEFAULT_ADMIN_PASSWORD, "系统管理员", Gender.UNKNOWN,
                null, null, academic, Role.ADMIN, LocalDate.of(2024, 1, 1));
        Employee zhang = createEmployee("t01", DEMO_PASSWORD, "张伟", Gender.MALE,
                "13800000001", "zhangwei@example.com", computer, Role.TEACHER,
                LocalDate.of(2024, 2, 20));
        Employee li = createEmployee("t02", DEMO_PASSWORD, "李娜", Gender.FEMALE,
                "13800000002", "lina@example.com", computer, Role.TEACHER,
                LocalDate.of(2024, 3, 5));
        Employee wang = createEmployee("t03", DEMO_PASSWORD, "王强", Gender.MALE,
                "13800000003", "wangqiang@example.com", math, Role.TEACHER,
                LocalDate.of(2024, 3, 10));
        Employee liu = createEmployee("t04", DEMO_PASSWORD, "刘洋", Gender.MALE,
                "13800000004", "liuyang@example.com", computer, Role.TEACHER,
                LocalDate.of(2024, 8, 25));

        Course mathCourse = courseRepository.save(
                new Course("C001", "高等数学", "面向大一学生的公共基础数学课程"));
        Course programming = courseRepository.save(
                new Course("C002", "程序设计基础", "Java 语言与结构化程序设计入门"));
        Course dataStructure = courseRepository.save(
                new Course("C003", "数据结构", "线性表、树、图与基本算法"));
        Course english = courseRepository.save(
                new Course("C004", "大学英语", "综合英语能力训练"));
        courseRepository.save(
                new Course("C005", "线性代数", "矩阵、行列式与向量空间基础"));

        SchoolClass class1 = classRepository.save(new SchoolClass(
                "CLS-2026-01", "计科2601", zhang.getId(),
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15),
                40, ClassStatus.ACTIVE));
        SchoolClass class2 = classRepository.save(new SchoolClass(
                "CLS-2026-02", "计科2602", li.getId(),
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15),
                35, ClassStatus.ACTIVE));
        classRepository.save(new SchoolClass(
                "CLS-2027-01", "计科2701", null,
                LocalDate.of(2027, 9, 1), LocalDate.of(2028, 1, 15),
                40, ClassStatus.PLANNED));

        LocalDateTime createdAt = LocalDateTime.of(2026, 9, 1, 8, 0, 0);
        ClassCourse c1Math = classCourseRepository.save(
                new ClassCourse(class1.getId(), mathCourse.getId(), zhang.getId(), createdAt));
        ClassCourse c1Programming = classCourseRepository.save(
                new ClassCourse(class1.getId(), programming.getId(), li.getId(), createdAt));
        ClassCourse c1DataStructure = classCourseRepository.save(
                new ClassCourse(class1.getId(), dataStructure.getId(), wang.getId(), createdAt));
        classCourseRepository.save(
                new ClassCourse(class1.getId(), english.getId(), null, createdAt));
        ClassCourse c2Math = classCourseRepository.save(
                new ClassCourse(class2.getId(), mathCourse.getId(), zhang.getId(), createdAt));
        ClassCourse c2Programming = classCourseRepository.save(
                new ClassCourse(class2.getId(), programming.getId(), li.getId(), createdAt));
        ClassCourse c2DataStructure = classCourseRepository.save(
                new ClassCourse(class2.getId(), dataStructure.getId(), wang.getId(), createdAt));
        ClassCourse c2English = classCourseRepository.save(
                new ClassCourse(class2.getId(), english.getId(), liu.getId(), createdAt));

        LocalDate enroll = LocalDate.of(2026, 9, 1);
        LocalDateTime scoredAt = LocalDateTime.of(2026, 10, 15, 10, 0, 0);

        Student s1 = createStudent("2026001", "陈晨", Gender.FEMALE, "13900000001",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s2 = createStudent("2026002", "李雷", Gender.MALE, "13900000002",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s3 = createStudent("2026003", "韩梅梅", Gender.FEMALE, "13900000003",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s4 = createStudent("2026004", "王芳", Gender.FEMALE, "13900000004",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s5 = createStudent("2026005", "赵磊", Gender.MALE, "13900000005",
                class1.getId(), enroll, StudentStatus.SUSPENDED);
        Student s6 = createStudent("2026006", "刘敏", Gender.FEMALE, "13900000006",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s7 = createStudent("2026007", "周杰", Gender.MALE, "13900000007",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s8 = createStudent("2026008", "吴静", Gender.FEMALE, "13900000008",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s9 = createStudent("2026009", "郑浩", Gender.MALE, "13900000009",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s10 = createStudent("2026010", "孙丽", Gender.FEMALE, "13900000010",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s11 = createStudent("2026011", "马超", Gender.MALE, "13900000011",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s12 = createStudent("2026012", "朱琳", Gender.FEMALE, "13900000012",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s13 = createStudent("2026013", "胡军", Gender.MALE, "13900000013",
                class1.getId(), enroll, StudentStatus.ACTIVE);
        Student s14 = createStudent("2026014", "郭雪", Gender.FEMALE, "13900000014",
                class1.getId(), enroll, StudentStatus.GRADUATED);
        Student s15 = createStudent("2026015", "林峰", Gender.MALE, "13900000015",
                class1.getId(), enroll, StudentStatus.ACTIVE);

        addScore(s1, c1Math, 92, scoredAt);
        addScore(s1, c1Programming, 88, scoredAt);
        addScore(s1, c1DataStructure, 85, scoredAt);

        addScore(s2, c1Math, 59, scoredAt);
        addScore(s2, c1Programming, 60, scoredAt);
        addScore(s2, c1DataStructure, 64, scoredAt);

        addScore(s3, c1Math, 52, scoredAt);
        addScore(s3, c1Programming, 48, scoredAt);
        addScore(s3, c1DataStructure, 41, scoredAt);

        addScore(s4, c1Math, 65, scoredAt);
        addScore(s4, c1Programming, 64, scoredAt);
        addScore(s4, c1DataStructure, 59, scoredAt);

        addScore(s5, c1Math, 0, scoredAt);

        addScore(s6, c1Math, 78, scoredAt);
        addScore(s6, c1Programming, 81, scoredAt);

        addScore(s7, c1Math, 60, scoredAt);
        addScore(s7, c1Programming, 72, scoredAt);
        addScore(s7, c1DataStructure, 75, scoredAt);

        addScore(s8, c1Math, 45, scoredAt);
        addScore(s8, c1Programming, 58, scoredAt);
        addScore(s8, c1DataStructure, 50, scoredAt);

        addScore(s9, c1Math, 86, scoredAt);
        addScore(s9, c1Programming, 65, scoredAt);

        addScore(s10, c1Math, 70, scoredAt);
        addScore(s10, c1Programming, 68, scoredAt);
        addScore(s10, c1DataStructure, 64, scoredAt);

        addScore(s11, c1Math, 55, scoredAt);
        addScore(s11, c1Programming, 58, scoredAt);
        addScore(s11, c1DataStructure, 52, scoredAt);

        addScore(s12, c1Math, 95, scoredAt);
        addScore(s12, c1Programming, 92, scoredAt);
        addScore(s12, c1DataStructure, 90, scoredAt);

        addScore(s13, c1Math, 63, scoredAt);
        addScore(s13, c1Programming, 60, scoredAt);
        addScore(s13, c1DataStructure, 41, scoredAt);

        addScore(s14, c1Math, 49, scoredAt);
        addScore(s14, c1Programming, 45, scoredAt);
        addScore(s14, c1DataStructure, 47, scoredAt);

        addScore(s15, c1Math, 75, scoredAt);
        addScore(s15, c1Programming, 80, scoredAt);
        s15.transferTo(class2.getId());
        studentRepository.save(s15);

        Student s21 = createStudent("2026021", "何平", Gender.MALE, "13900000021",
                class2.getId(), enroll, StudentStatus.ACTIVE);
        Student s22 = createStudent("2026022", "高雅", Gender.FEMALE, "13900000022",
                class2.getId(), enroll, StudentStatus.ACTIVE);
        Student s23 = createStudent("2026023", "罗成", Gender.MALE, "13900000023",
                class2.getId(), enroll, StudentStatus.ACTIVE);
        Student s24 = createStudent("2026024", "梁晓", Gender.FEMALE, "13900000024",
                class2.getId(), enroll, StudentStatus.ACTIVE);
        Student s25 = createStudent("2026025", "宋涛", Gender.MALE, "13900000025",
                class2.getId(), enroll, StudentStatus.ACTIVE);
        Student s26 = createStudent("2026026", "谢婷", Gender.FEMALE, "13900000026",
                class2.getId(), enroll, StudentStatus.ACTIVE);

        addScore(s21, c2Math, 88, scoredAt);
        addScore(s21, c2Programming, 90, scoredAt);
        addScore(s21, c2DataStructure, 86, scoredAt);
        addScore(s21, c2English, 85, scoredAt);

        addScore(s22, c2Math, 55, scoredAt);
        addScore(s22, c2Programming, 60, scoredAt);
        addScore(s22, c2DataStructure, 58, scoredAt);
        addScore(s22, c2English, 72, scoredAt);

        addScore(s23, c2Math, 72, scoredAt);
        addScore(s23, c2Programming, 70, scoredAt);
        addScore(s23, c2DataStructure, 68, scoredAt);
        addScore(s23, c2English, 78, scoredAt);

        addScore(s24, c2Math, 40, scoredAt);
        addScore(s24, c2Programming, 45, scoredAt);
        addScore(s24, c2DataStructure, 35, scoredAt);

        addScore(s25, c2Math, 66, scoredAt);
        addScore(s25, c2Programming, 64, scoredAt);
        addScore(s25, c2DataStructure, 61, scoredAt);

        addScore(s26, c2Math, 80, scoredAt);
        addScore(s26, c2Programming, 77, scoredAt);
        addScore(s26, c2DataStructure, 74, scoredAt);

        System.out.println("首次运行：已写入演示数据（班级 3 个、学生 21 名、成绩若干）。");
        System.out.println("演示教师账号：t01 / t02 / t03 / t04，初始密码均为 " + DEMO_PASSWORD);
    }

    /**
     * 保证至少存在一个可用管理员；否则创建 admin（重名时追加序号）并输出提示。
     */
    public void ensureAdmin() {
        boolean hasActiveAdmin = employeeRepository
                .findFirst(employee -> employee.isAdmin() && employee.isActive())
                .isPresent();
        if (hasActiveAdmin) {
            return;
        }
        Department department = departmentRepository
                .findFirst(item -> "教务部".equals(item.getName()))
                .orElseGet(() -> departmentRepository.save(
                        new Department("教务部", "系统自动创建，用于管理员兜底")));
        String username = "admin";
        int suffix = 2;
        while (usernameTaken(username)) {
            username = "admin" + suffix++;
        }
        Employee admin = new Employee(username, "系统管理员", Gender.UNKNOWN, null, null,
                department.getId(), Role.ADMIN, LocalDate.now());
        String salt = passwordHasher.generateSalt();
        admin.updatePassword(salt, passwordHasher.hash(DEFAULT_ADMIN_PASSWORD, salt));
        admin.changeStatus(EmployeeStatus.ACTIVE);
        employeeRepository.save(admin);
        diagnostics.add("employees.csv", 0, "admin-fallback",
                "未找到可用管理员，已创建默认管理员 " + username);
        System.out.println("未找到可用管理员，已自动创建：" + username
                + " / " + DEFAULT_ADMIN_PASSWORD + "（请登录后尽快修改密码）");
    }

    private boolean usernameTaken(String username) {
        return employeeRepository
                .findFirst(employee -> employee.getUsername().equalsIgnoreCase(username))
                .isPresent();
    }

    private Employee createEmployee(String username,
                                    String rawPassword,
                                    String name,
                                    Gender gender,
                                    String phone,
                                    String email,
                                    Department department,
                                    Role role,
                                    LocalDate hireDate) {
        Employee employee = new Employee(username, name, gender, phone, email,
                department.getId(), role, hireDate);
        String salt = passwordHasher.generateSalt();
        employee.updatePassword(salt, passwordHasher.hash(rawPassword, salt));
        return employeeRepository.save(employee);
    }

    private Student createStudent(String studentNo,
                                  String name,
                                  Gender gender,
                                  String phone,
                                  long classId,
                                  LocalDate enrollmentDate,
                                  StudentStatus status) {
        Student student = new Student(studentNo, name, gender, phone, classId, enrollmentDate);
        student.changeStatus(status);
        return studentRepository.save(student);
    }

    private Score addScore(Student student, ClassCourse classCourse, double value,
                           LocalDateTime updatedAt) {
        return scoreRepository.save(
                new Score(student.getId(), classCourse.getId(), value, updatedAt));
    }
}
