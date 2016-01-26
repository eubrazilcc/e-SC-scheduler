package com.connexience.scheduler.api;

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.model.ResourceAllocationRequest;

/**
 * Created by Jacek on 14/12/2015.
 */
public interface IResourceMapper<T>
{
    ResourceAllocationRequest generateAllocationRequest(T mappedObject) throws ResourceNotAvailableException;
}
