package com.connexience.scheduler.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Jacek on 16/12/2015.
 */
public class CommittedResourceAllocation
{
    /**
     * The original request that was accepted.
     */
    public ResourceAllocationRequest request;

    /**
     * A timestamp when the resource allocation has been committed.
     */
    public long timestamp;

    /**
     * The node which resources where allocated.
     */
    public ComputeNode node;


    public CommittedResourceAllocation()
    { }

    public CommittedResourceAllocation(ResourceAllocationRequest request, long timestamp, ComputeNode node)
    {
        this.request = request;
        this.timestamp = timestamp;
        this.node = node;
    }
}
