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
 * This class represents an allocation of a single specific resource. It is intended to be combined into
 * a list of allocations (for CPU, Memory, Disk, etc.) required by an invocation.
 *
 * Created by Jacek on 14/12/2015.
 */
public class ResourceAllocation
{
    /**
     * This is the primary mean by which resource are allocated. The type may be one from Constants.ResourceType
     */
    public String resourceType;

    /**
     * This a secondary mean by which resources may be allocated, e.g. a request to allocate a specific resource.
     * However, the name is mainly used during deallocation.
     */
    public String resourceName;

    /**
     * This list usually includes just a single property defining allocation against a particular resource.
     * But in some cases may include more than one, e.g. CPU -- speed, number of cores, architecture.
     *
     */
    public final ArrayList<Property> properties = new ArrayList<>(); // This is a list of properties specific to the particular resource, e.g.


    public ResourceAllocation(String resourceType, Collection<Property> properties)
    {
        this.resourceType = resourceType;
        this.properties.addAll(properties);
    }


    public ResourceAllocation(String resourceType, Property... properties)
    {
        this.resourceType = resourceType;
        this.properties.addAll(Arrays.asList(properties));
    }

    public ResourceAllocation(String resourceName, String resourceType, Collection<Property> properties)
    {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.properties.addAll(properties);
    }


    public ResourceAllocation(String resourceName, String resourceType, Property... properties)
    {
        this.resourceName = resourceName;
        this.resourceType = resourceType;
        this.properties.addAll(Arrays.asList(properties));
    }
}
