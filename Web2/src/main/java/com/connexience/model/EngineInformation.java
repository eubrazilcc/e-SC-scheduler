package com.connexience.model;

/**
 * Created by naa166 - Anirudh Agarwal on 15/09/2015.
 * This class stores the information which will be relevant for scheduling and will be manager by the class EngineInformationManager
 */
public class EngineInformation {

    private int threadCount;

    private long diskFreeSpace;

    private long freeRam;

    private long physicalRam;

    private double cpuPercentUsed;

    private int maxConcurrentWorkflowInvocation;


    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public long getDiskFreeSpace() {
        return diskFreeSpace;
    }

    public void setDiskFreeSpace(long diskFreeSpace) {
        this.diskFreeSpace = diskFreeSpace;
    }

    public long getFreeRam() {
        return freeRam;
    }

    public void setFreeRam(long freeRam) {
        this.freeRam = freeRam;
    }

    public long getPhysicalRam() {
        return physicalRam;
    }

    public void setPhysicalRam(long physicalRam) {
        this.physicalRam = physicalRam;
    }

    public double getCpuPercentUsed() {
        return cpuPercentUsed;
    }

    public void setCpuPercentUsed(double cpuPercentUsed) {
        this.cpuPercentUsed = cpuPercentUsed;
    }

    public int getMaxConcurrentWorkflowInvocation() {
        return maxConcurrentWorkflowInvocation;
    }

    public void setMaxConcurrentWorkflowInvocation(int maxConcurrentWorkflowInvocation) {
        this.maxConcurrentWorkflowInvocation = maxConcurrentWorkflowInvocation;
    }


}
