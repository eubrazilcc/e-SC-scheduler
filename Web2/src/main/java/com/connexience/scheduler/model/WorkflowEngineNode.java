package com.connexience.scheduler.model;

/**
 * Created by naa166 - Anirudh Agarwal on 15/09/2015.
 * This class stores the information which will be relevant for scheduling and will be manager by the class EngineInformationManager
 */
public class WorkflowEngineNode extends ComputeNode
{
    private static int RES_THREAD_COUNT_INDEX = 0;
    private static int RES_CPU_INDEX = 1;
    private static int RES_MEMORY_INDEX = 2;
    private static int RES_DISK_INDEX = 3;

    public WorkflowEngineNode(String engineId) {
        super(engineId);
    }

    public int getThreadCount() {
        return (Integer)availableResources.get(RES_THREAD_COUNT_INDEX).getProperty(Constants.Property.AVAILABLE).getValue();
    }

    public void setThreadCount(int threadCount) {
        availableResources.get(RES_THREAD_COUNT_INDEX).setProperty(Constants.Property.AVAILABLE, threadCount);
    }

    /*
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
    */
}
