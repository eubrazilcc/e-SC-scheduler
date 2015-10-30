package com.connexience.model;

/**
 * Created by naa166 - Anirudh Agarwal on 17/09/2015.
 */
public class InvocationInformation {

    private String InvocationId;

    private String EngineIp;

    private long physicalRAM;

    private long diskSpace;

    public String getInvocationId() {
        return InvocationId;
    }

    public void setInvocationId(String invocationId) {
        InvocationId = invocationId;
    }

    public String getEngineIp() {
        return EngineIp;
    }

    public void setEngineIp(String engineIp) {
        EngineIp = engineIp;
    }

    public long getPhysicalRAM() {
        return physicalRAM;
    }

    public void setPhysicalRAM(long physicalRAM) {
        this.physicalRAM = physicalRAM;
    }

    public long getDiskSpace() {
        return diskSpace;
    }

    public void setDiskSpace(long diskSpace) {
        this.diskSpace = diskSpace;
    }
}
