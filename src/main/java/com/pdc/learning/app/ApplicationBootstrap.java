package com.pdc.learning.app;

import com.pdc.learning.exception.DataAccessException;
import com.pdc.learning.model.ClassCourse;
import com.pdc.learning.model.Course;
import com.pdc.learning.model.Department;
import com.pdc.learning.model.Employee;
import com.pdc.learning.model.SchoolClass;
import com.pdc.learning.model.Score;
import com.pdc.learning.model.Student;
import com.pdc.learning.persistence.DataInitializer;
import com.pdc.learning.persistence.DataLoader;
import com.pdc.learning.persistence.LoadDiagnostics;
import com.pdc.learning.persistence.csv.ClassCourseCsvCodec;
import com.pdc.learning.persistence.csv.CourseCsvCodec;
import com.pdc.learning.persistence.csv.CsvRepository;
import com.pdc.learning.persistence.csv.DepartmentCsvCodec;
import com.pdc.learning.persistence.csv.EmployeeCsvCodec;
import com.pdc.learning.persistence.csv.SchoolClassCsvCodec;
import com.pdc.learning.persistence.csv.ScoreCsvCodec;
import com.pdc.learning.persistence.csv.StudentCsvCodec;
import com.pdc.learning.service.AuthService;
import com.pdc.learning.service.ClassService;
import com.pdc.learning.service.CourseService;
import com.pdc.learning.service.DepartmentService;
import com.pdc.learning.service.EmployeeService;
import com.pdc.learning.service.ScoreService;
import com.pdc.learning.service.Session;
import com.pdc.learning.service.StatisticsService;
import com.pdc.learning.service.StudentService;
import com.pdc.learning.service.TeachingService;
import com.pdc.learning.service.WarningService;
import com.pdc.learning.ui.AdminMenuFactory;
import com.pdc.learning.ui.ConsoleIO;
import com.pdc.learning.ui.LoginUI;
import com.pdc.learning.ui.TablePrinter;
import com.pdc.learning.ui.TeacherMenuFactory;
import com.pdc.learning.ui.view.AccountUI;
import com.pdc.learning.ui.view.ClassUI;
import com.pdc.learning.ui.view.CourseUI;
import com.pdc.learning.ui.view.DepartmentUI;
import com.pdc.learning.ui.view.EmployeeUI;
import com.pdc.learning.ui.view.ScoreUI;
import com.pdc.learning.ui.view.StatisticsUI;
import com.pdc.learning.ui.view.StudentUI;
import com.pdc.learning.ui.view.TeachingUI;
import com.pdc.learning.ui.view.WarningUI;
import com.pdc.learning.util.PasswordHasher;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * 组合根：装配仓储、服务与界面，并负责启动/关闭流程（架构 3.1）。
 */
public class ApplicationBootstrap {

    private final Path dataDir = Path.of("data");
    private final PasswordHasher passwordHasher = new PasswordHasher();
    private final List<CsvRepository<?>> repositories = new ArrayList<>();

    private LoadDiagnostics diagnostics;
    private boolean started;

    public void start() {
        CsvRepository<Department> departmentRepository = new CsvRepository<>(
                dataDir.resolve("departments.csv"), new DepartmentCsvCodec());
        CsvRepository<Employee> employeeRepository = new CsvRepository<>(
                dataDir.resolve("employees.csv"), new EmployeeCsvCodec());
        CsvRepository<Course> courseRepository = new CsvRepository<>(
                dataDir.resolve("courses.csv"), new CourseCsvCodec());
        CsvRepository<SchoolClass> classRepository = new CsvRepository<>(
                dataDir.resolve("classes.csv"), new SchoolClassCsvCodec());
        CsvRepository<ClassCourse> classCourseRepository = new CsvRepository<>(
                dataDir.resolve("class_courses.csv"), new ClassCourseCsvCodec());
        CsvRepository<Student> studentRepository = new CsvRepository<>(
                dataDir.resolve("students.csv"), new StudentCsvCodec());
        CsvRepository<Score> scoreRepository = new CsvRepository<>(
                dataDir.resolve("scores.csv"), new ScoreCsvCodec());
        repositories.add(departmentRepository);
        repositories.add(employeeRepository);
        repositories.add(courseRepository);
        repositories.add(classRepository);
        repositories.add(classCourseRepository);
        repositories.add(studentRepository);
        repositories.add(scoreRepository);

        diagnostics = new LoadDiagnostics();
        DataInitializer initializer = new DataInitializer(departmentRepository,
                employeeRepository, courseRepository, classRepository,
                classCourseRepository, studentRepository, scoreRepository,
                passwordHasher, diagnostics);
        boolean fresh = initializer.isFresh();
        initializer.ensureLayout(dataDir);

        DataLoader loader = new DataLoader(departmentRepository, employeeRepository,
                courseRepository, classRepository, classCourseRepository,
                studentRepository, scoreRepository, diagnostics);
        loader.loadAll();
        if (fresh) {
            initializer.seedDemoData();
        }
        initializer.ensureAdmin();
        diagnostics.updateLoaded("departments.csv", departmentRepository.findAll().size());
        diagnostics.updateLoaded("employees.csv", employeeRepository.findAll().size());
        diagnostics.updateLoaded("courses.csv", courseRepository.findAll().size());
        diagnostics.updateLoaded("classes.csv", classRepository.findAll().size());
        diagnostics.updateLoaded("class_courses.csv", classCourseRepository.findAll().size());
        diagnostics.updateLoaded("students.csv", studentRepository.findAll().size());
        diagnostics.updateLoaded("scores.csv", scoreRepository.findAll().size());
        diagnostics.writeTo(dataDir.resolve("load-errors.log"));
        diagnostics.printSummary(System.out);
        started = true;

        Session session = new Session();
        AuthService authService = new AuthService(employeeRepository, passwordHasher, session);
        DepartmentService departmentService = new DepartmentService(
                departmentRepository, employeeRepository, session);
        EmployeeService employeeService = new EmployeeService(employeeRepository,
                departmentRepository, classRepository, classCourseRepository,
                passwordHasher, session);
        CourseService courseService = new CourseService(courseRepository,
                classCourseRepository, scoreRepository, session);
        ClassService classService = new ClassService(classRepository, employeeRepository,
                studentRepository, classCourseRepository, scoreRepository, session);
        TeachingService teachingService = new TeachingService(classCourseRepository,
                classRepository, courseRepository, employeeRepository,
                scoreRepository, session);
        StudentService studentService = new StudentService(studentRepository,
                classRepository, classCourseRepository, scoreRepository, session);
        ScoreService scoreService = new ScoreService(scoreRepository, studentRepository,
                classCourseRepository, classRepository, session);
        StatisticsService statisticsService = new StatisticsService(studentRepository,
                classRepository, classCourseRepository, scoreRepository,
                courseRepository, employeeRepository, session);
        WarningService warningService = new WarningService(statisticsService, session,
                studentRepository, classRepository, classCourseRepository);

        ConsoleIO io = new ConsoleIO();
        TablePrinter tablePrinter = new TablePrinter();

        DepartmentUI departmentUI = new DepartmentUI(departmentService, io, tablePrinter);
        EmployeeUI employeeUI = new EmployeeUI(employeeService, departmentService, io, tablePrinter);
        CourseUI courseUI = new CourseUI(courseService, io, tablePrinter, session);
        ClassUI classUI = new ClassUI(classService, studentService, teachingService,
                courseService, employeeService, io, tablePrinter, session);
        TeachingUI teachingUI = new TeachingUI(teachingService, classService, courseService,
                employeeService, scoreService, io, tablePrinter, session);
        StudentUI studentUI = new StudentUI(studentService, classService, io, tablePrinter, session);
        ScoreUI scoreUI = new ScoreUI(scoreService, classService, teachingService,
                studentService, courseService, employeeService, io, tablePrinter);
        StatisticsUI statisticsUI = new StatisticsUI(statisticsService, classService,
                studentService, io, tablePrinter);
        WarningUI warningUI = new WarningUI(warningService, classService,
                studentService, io, tablePrinter);
        AccountUI accountUI = new AccountUI(authService, io);

        AdminMenuFactory adminMenuFactory = new AdminMenuFactory(departmentUI, employeeUI,
                courseUI, classUI, teachingUI, studentUI, scoreUI,
                statisticsUI, warningUI, accountUI, io);
        TeacherMenuFactory teacherMenuFactory = new TeacherMenuFactory(classUI, studentUI,
                courseUI, scoreUI, statisticsUI, warningUI, accountUI, io);
        LoginUI loginUI = new LoginUI(authService, io, adminMenuFactory, teacherMenuFactory);

        try {
            loginUI.run();
        } catch (ConsoleIO.QuitRequestedException e) {
            io.println();
            io.println("正在退出系统……");
        } finally {
            shutdown();
        }
    }

    /**
     * 兜底保存全部仓储并重写诊断日志；保存失败只警告，不阻断退出。
     */
    public void shutdown() {
        if (!started) {
            return;
        }
        for (CsvRepository<?> repository : repositories) {
            try {
                repository.saveAll();
            } catch (DataAccessException e) {
                System.err.println("警告：保存 "
                        + repository.file().getFileName() + " 失败：" + e.getMessage());
            }
        }
        if (diagnostics != null) {
            diagnostics.writeTo(dataDir.resolve("load-errors.log"));
        }
    }
}
