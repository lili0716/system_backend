package com.artdesign.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.VirtualMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.util.*;

@RestController
@RequestMapping("/ops")
public class ServerMonitorController {

    private final SystemInfo systemInfo = new SystemInfo();

    @GetMapping("/server-info")
    public Map<String, Object> getServerInfo() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("msg", "success");

        Map<String, Object> data = new HashMap<>();

        // 服务器基本信息
        try {
            InetAddress addr = InetAddress.getLocalHost();
            data.put("hostName", addr.getHostName());
            data.put("ip", addr.getHostAddress());
        } catch (Exception e) {
            data.put("hostName", "Unknown");
            data.put("ip", "Unknown");
        }

        HardwareAbstractionLayer hal = systemInfo.getHardware();
        OperatingSystem os = systemInfo.getOperatingSystem();

        // 操作系统
        data.put("osName", os.toString());

        // CPU 信息
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        try {
            Thread.sleep(500); // 等500ms采样
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("name", processor.getProcessorIdentifier().getName());
        cpu.put("physicalCount", processor.getPhysicalProcessorCount());
        cpu.put("logicalCount", processor.getLogicalProcessorCount());
        cpu.put("usage", Math.round(cpuUsage * 10.0) / 10.0);
        data.put("cpu", cpu);

        // 内存信息
        GlobalMemory memory = hal.getMemory();
        Map<String, Object> ram = new HashMap<>();
        long totalMem = memory.getTotal();
        long usedMem = totalMem - memory.getAvailable();
        ram.put("total", formatBytes(totalMem));
        ram.put("used", formatBytes(usedMem));
        ram.put("free", formatBytes(memory.getAvailable()));
        ram.put("usage", Math.round((double) usedMem / totalMem * 1000.0) / 10.0);
        data.put("ram", ram);

        // SWAP 信息
        VirtualMemory vm = memory.getVirtualMemory();
        Map<String, Object> swap = new HashMap<>();
        long swapTotal = vm.getSwapTotal();
        long swapUsed = vm.getSwapUsed();
        swap.put("total", formatBytes(swapTotal));
        swap.put("used", formatBytes(swapUsed));
        swap.put("free", formatBytes(swapTotal - swapUsed));
        swap.put("usage", swapTotal > 0 ? Math.round((double) swapUsed / swapTotal * 1000.0) / 10.0 : 0);
        data.put("swap", swap);

        // 磁盘信息
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fileStores = fileSystem.getFileStores();
        long diskTotal = 0, diskFree = 0;
        List<Map<String, Object>> disks = new ArrayList<>();
        for (OSFileStore fs : fileStores) {
            long total = fs.getTotalSpace();
            long free = fs.getUsableSpace();
            if (total > 0) {
                diskTotal += total;
                diskFree += free;
                Map<String, Object> diskInfo = new HashMap<>();
                diskInfo.put("mount", fs.getMount());
                diskInfo.put("type", fs.getType());
                diskInfo.put("total", formatBytes(total));
                diskInfo.put("free", formatBytes(free));
                diskInfo.put("used", formatBytes(total - free));
                diskInfo.put("usage", Math.round((double) (total - free) / total * 1000.0) / 10.0);
                disks.add(diskInfo);
            }
        }
        Map<String, Object> disk = new HashMap<>();
        disk.put("total", formatBytes(diskTotal));
        disk.put("used", formatBytes(diskTotal - diskFree));
        disk.put("free", formatBytes(diskFree));
        disk.put("usage", diskTotal > 0 ? Math.round((double) (diskTotal - diskFree) / diskTotal * 1000.0) / 10.0 : 0);
        disk.put("details", disks);
        data.put("disk", disk);

        // JVM 信息
        RuntimeMXBean runtimeMx = ManagementFactory.getRuntimeMXBean();
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("vendor", System.getProperty("java.vendor"));
        jvm.put("maxMemory", formatBytes(runtime.maxMemory()));
        jvm.put("totalMemory", formatBytes(runtime.totalMemory()));
        jvm.put("freeMemory", formatBytes(runtime.freeMemory()));
        jvm.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        // JVM 运行时长
        long uptimeMs = runtimeMx.getUptime();
        jvm.put("uptime", formatUptime(uptimeMs));
        data.put("jvm", jvm);

        result.put("data", data);
        return result;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        if (bytes < 1024 * 1024)
            return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024)
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatUptime(long millis) {
        long seconds = millis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        if (days > 0)
            return days + "天" + hours + "小时" + minutes + "分钟";
        if (hours > 0)
            return hours + "小时" + minutes + "分钟";
        return minutes + "分钟";
    }
}
