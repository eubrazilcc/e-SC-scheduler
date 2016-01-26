/**
 * e-Science Central
 * Copyright (C) 2008-2015 School of Computing Science, Newcastle University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation at:
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, 5th Floor, Boston, MA 02110-1301, USA.
 */
package com.connexience.scheduler.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * This is a generic Scheduler's view on an incoming requests, whether these are workflow invocations or manual
 * reservations.
 *
 * Created by Jacek on 14/12/2015.
 */
public class ResourceAllocationRequest
{
    /**
     * This identifies the requesting party. It may, for example, be an invocation id from which this allocation
     * request has been generated.
     */
    public String id;

    /**
     * This identifies the type or the requesting party. It may refer to an invocation, user, or anything else the
     * Scheduler is going to process.
     */
    public String type;

    /**
     * Request specific content which scheduler needs to pass through. It depends on the request type and may be an
     * invocation message.
     */
    public Object content;

    /**
     * An estimated size of the request. The size may be something as simple as the number of instructions or
     * size of data to process or as complex as a vector of such values.
     *
     * Importantly, whatever the size is, it needs to be matched against relevant ComputeNode attributes such as
     * MIPS or GBPS. Then, scheduling algorithms may take the size into account when building task schedules.
     */
    public Object size;

    /**
     * This is a list of the allocations against all resources that are required by the requesting party.
     */
    public ArrayList<ResourceAllocation> resourceAllocations = new ArrayList<>(1);


    public ResourceAllocationRequest()
    { }


    public ResourceAllocationRequest(String requestId, String requestType, Object requestContent, Object requestSize, Collection<ResourceAllocation> allocations)
    {
        id = requestId;
        type = requestType;
        content = requestContent;
        size = requestSize;
        resourceAllocations.addAll(allocations);
    }


    public ResourceAllocationRequest(String requestId, String requestType, Object requestContent, Object requestSize, ResourceAllocation... allocations)
    {
        id = requestId;
        type = requestType;
        content = requestContent;
        size = requestSize;
        resourceAllocations.addAll(Arrays.asList(allocations));
    }
}
