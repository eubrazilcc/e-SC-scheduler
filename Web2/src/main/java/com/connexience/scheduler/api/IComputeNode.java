package com.connexience.scheduler.api;

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.model.Resource;
import com.connexience.scheduler.model.ResourceAllocation;

import java.util.Collection;

/**
 * A simple interface to represent a compute node that offers some resources for tasks.
 *
 * Created by Jacek on 17/12/2015.
 */
public interface IComputeNode
{
    String getName();

    Resource getResourceByName(String name) throws ResourceNotAvailableException;

    Resource[] getResourcesByType(String type) throws ResourceNotAvailableException;

    boolean tryAllocate(Collection<ResourceAllocation> allocations) throws ResourceNotAvailableException;

    boolean allocate(Collection<ResourceAllocation> allocations) throws ResourceNotAvailableException;

    void release(Collection<ResourceAllocation> allocations);
}
