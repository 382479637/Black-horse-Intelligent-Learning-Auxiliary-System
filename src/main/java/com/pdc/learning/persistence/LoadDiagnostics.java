package com.pdc.learning.persistence;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 加载诊断：坏行、引用缺失、唯一性冲突与各文件加载数量。
 *
 * <p>日志只记录时间戳、文件名、行号、错误类别与安全摘要，不记录整行、密码、哈希、盐或联系方式。</p>
 */
public class LoadDiagnostics {

    public record LoadIssue(String file, int line, String category, String summary) {
    }

    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final List<LoadIssue> issues = new ArrayList<>();
    private final Map<String, int[]> fileStats = new LinkedHashMap<>();

    public void add(String file, int line, String category, String summary) {
        issues.add(new LoadIssue(file, line, category, summary));
    }

    public void recordStats(String file, int loaded, int skipped) {
        fileStats.put(file, new int[]{loaded, skipped});
    }

    /**
     * 首次运行写入种子数据后刷新已加载数量，保留原跳过数量。
     */
    public void updateLoaded(String file, int loaded) {
        int[] stats = fileStats.get(file);
        if (stats == null) {
            fileStats.put(file, new int[]{loaded, 0});
        } else {
            stats[0] = loaded;
        }
    }

    public boolean hasIssues() {
        return !issues.isEmpty();
    }

    public int issueCount() {
        return issues.size();
    }

    /**
     * 覆盖写入诊断日志。写入失败只向 stderr 警告，不阻断启动。
     */
    public void writeTo(Path path) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("# generated at ").append(TIMESTAMP.format(LocalDateTime.now())).append('\n');
            sb.append("# timestamp\tfile\tline\tcategory\tsummary\n");
            for (LoadIssue issue : issues) {
                sb.append(TIMESTAMP.format(LocalDateTime.now())).append('\t')
                        .append(issue.file()).append('\t')
                        .append(issue.line()).append('\t')
                        .append(issue.category()).append('\t')
                        .append(issue.summary()).append('\n');
            }
            sb.append("# file\tloaded\tskipped\n");
            for (Map.Entry<String, int[]> entry : fileStats.entrySet()) {
                sb.append("# ").append(entry.getKey()).append('\t')
                        .append(entry.getValue()[0]).append('\t')
                        .append(entry.getValue()[1]).append('\n');
            }
            Files.writeString(path, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("警告：无法写入加载诊断日志 " + path + "（" + e.getMessage() + "），加载继续。");
        }
    }

    public void printSummary(PrintStream out) {
        out.println("数据加载摘要：");
        for (Map.Entry<String, int[]> entry : fileStats.entrySet()) {
            out.println("  " + entry.getKey() + "：加载 " + entry.getValue()[0]
                    + " 条，跳过 " + entry.getValue()[1] + " 条");
        }
        if (issues.isEmpty()) {
            out.println("  未发现坏行或引用问题。");
        } else {
            out.println("  发现 " + issues.size() + " 条问题，详见 data/load-errors.log。");
        }
    }
}
