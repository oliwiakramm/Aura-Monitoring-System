package com.aura.agent.collector;


import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.logging.Logger;

public class JvmMetricCollector {
    private static final Logger LOG = Logger.getLogger(JvmMetricCollector.class.getName());
    private static final long BYTES_IN_MB = 1024L * 1024L;

    private final OperatingSystemMXBean osMxBean;
    private final MemoryMXBean memoryMXBean;

    public JvmMetricCollector(){
        this.osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
    }

    public MetricSnapshot collect(){
        double cpuLoad = osMxBean.getProcessCpuLoad();

        double normalizedCpu = Math.max(0.0,cpuLoad);

        long heapUsedBytes = memoryMXBean.getHeapMemoryUsage().getUsed();
        long heapMaxBytes = memoryMXBean.getHeapMemoryUsage().getMax();

        long usedMb = heapUsedBytes / BYTES_IN_MB;
        long maxMb = heapMaxBytes / BYTES_IN_MB;

        double roundedCpu = Math.round(normalizedCpu * 10_000.0) / 10_000.0;

        MetricSnapshot snapshot = new MetricSnapshot(normalizedCpu,usedMb,maxMb);

        LOG.fine(() -> String.format(
                "Collected metrics: CPU=%.1f%%, RAM=%dMB/%dMB (%.1f%%)",
                normalizedCpu * 100, usedMb,maxMb, snapshot.ramUsagePercent()
        ));

        return snapshot;
    }
}
