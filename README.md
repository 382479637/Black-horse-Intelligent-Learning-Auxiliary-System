# 黑马智能学习辅助系统（Java CUI）

COMP603/ENSE600 Project 1，类别 A：学生信息管理。本仓库实现**第 1 轮 CUI 版本**，
使用纯 JDK（无第三方运行时依赖）与 CSV 文件持久化。第 2 轮 GUI/Derby/JUnit 内容不在本轮范围内。

## 运行环境

- JDK 21
- Maven 3.9+
- Windows / macOS / Linux 终端（UTF-8 或系统默认编码）

## 构建与运行

```bash
mvn clean package
java -jar target/learning-assistant.jar
```

程序必须从项目根目录启动（或至少让当前目录可写），数据文件位于相对路径 `data/`。

> Windows 控制台若出现中文乱码，请先执行 `chcp 65001` 并确认终端字体支持中文；
> 程序会优先使用控制台自身字符集读取输入，输出使用终端编码。

## 一键启动（Windows）

项目根目录提供两个文件，双击即可运行（自动定位 JDK 21、jar 不存在时自动构建、自动切到 UTF-8 控制台）：

| 文件 | 用途 |
|---|---|
| `启动项目.lnk` | 快捷方式，双击启动（工作目录已指向项目根，图标为 Java） |
| `启动项目.cmd` | 实际脚本，可直接编辑；也可在命令行执行 `启动项目.cmd` |

脚本默认假定 JDK 21 在 `D:\dev\jdk21-current`、Maven 在 `D:\dev\apache-maven-3.9.16`；
换机器时只改脚本开头的 `JAVA_HOME` 与 `mvn.cmd` 路径即可，其余逻辑不用动。

> 注意：`target/` 与 `data/load-errors.log` 已在 `.gitignore` 中忽略，构建与运行不会污染提交内容。

## 首次运行与演示账号

首次启动（`data/` 下 7 个 CSV 均无数据行）会写入演示数据：

| 账号 | 密码 | 角色 |
|---|---|---|
| `admin` | `Admin@123` | 管理员 |
| `t01` | `Teach@123` | 教师（张伟） |
| `t02` | `Teach@123` | 教师（李娜） |
| `t03` | `Teach@123` | 教师（王强） |
| `t04` | `Teach@123` | 教师（刘洋） |

演示数据包含 3 个班级、21 名学生、5 门课程与若干成绩，覆盖正常、轻度、中度、重度预警场景。
若已有数据但没有任何可用管理员，程序会自动创建 `admin / Admin@123`。

密码使用 PBKDF2WithHmacSHA256（210,000 次迭代、16 字节盐、256 位密钥）加盐哈希，
CSV 中不保存明文密码。

## 数据与诊断

```text
data/
├── departments.csv
├── employees.csv
├── courses.csv
├── classes.csv
├── class_courses.csv
├── students.csv
├── scores.csv
└── load-errors.log      # 每次启动覆盖写入的加载诊断
```

- 所有文件为 UTF-8 CSV，首行表头，字段按双引号转义。
- 写入采用“临时文件 + 原子替换”，避免中途崩溃损坏数据。
- 坏行、重复 ID、缺失外键会被跳过，并在控制台与 `load-errors.log` 中说明；
  诊断日志不记录整行、密码、哈希、盐或联系方式。
- 重新体验首次运行：退出程序后删除 `data/` 目录再启动即可。

## 主要功能

- **管理员**：部门、员工（教师）、课程、班级、教学任务、学生、成绩、统计与预警、修改密码。
- **教师**：我的教学（我的班级 / 我的学生 / 课程目录）、成绩管理（仅本人任课任务）、
  统计与预警（仅本人可见范围）、修改密码。
- 权限同时由菜单隐藏与 Service 层校验控制。
- 统计与预警的可见范围：管理员看全部，教师只看本人任课 `ClassCourse`；
  班级学生排名只统计当前在班且在本班有可见成绩的学生。
- 预警规则 R1–R6 累积命中并取最高等级，界面展示命中规则与“数据不足”口径。

## 项目结构

```text
src/main/java/com/pdc/learning/
├── app/          Main、ApplicationBootstrap（组合根）
├── model/        实体与枚举
├── dto/          统计/预警只读数据载体
├── persistence/  CSV 仓储、编解码器、加载与初始化
├── service/      业务规则、权限与可见范围
├── ui/           登录、菜单、表格与各业务界面
└── util/         密码哈希、CSV、日期、字段校验
```

依赖方向：`app → ui → service → model/persistence`。所有对象在 `ApplicationBootstrap`
中装配，不使用单例。
