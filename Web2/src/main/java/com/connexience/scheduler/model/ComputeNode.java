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

import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This is an abstract representation of a compute node which offers some allocatable/comparable resources.
 * The class offers two methods to allocate and release its resources.
 *
 * Created by Jacek on 14/12/2015.
 */
public class ComputeNode
{
    private static final Logger _Logger = LoggerFactory.getLogger(ComputeNode.class);

    public String id;

    public final ArrayList<Resource> availableResources = new ArrayList<>();


    public ComputeNode(String nodeId)
    {
        id = nodeId;
    }


    public ComputeNode(String nodeId, Collection<Resource> resources) {
        id = nodeId;
        availableResources.addAll(resources);
    }


    public ComputeNode(String nodeId, Resource... resources) {
        id = nodeId;
        availableResources.addAll(Arrays.asList(resources));
    }


    public ArrayList<Resource> getResourcesByType(String resourceType)
    {
        ArrayList<Resource> byType = new ArrayList<>();
        for (Resource res : availableResources) {
            if (res.type.equals(resourceType)) {
                byType.add(res);
            }
        }

        return byType;
    }


    public boolean tryAllocate(Collection<ResourceAllocation> allocations) throws ResourceNotAvailableException
    {
        return _allocate(allocations, false);
    }


    public boolean allocate(Collection<ResourceAllocation> allocations) throws ResourceNotAvailableException
    {
        return _allocate(allocations, true);
    }


    /**
     * <p>Allocates resources of this ComputeNode according to the given requirements.</p>
     *
     * <p>NOTE: this operation has a side effect such that for each allocation its resourceName will be set according
     * to the resource which was actually matched (if the matching was done by type).</p>
     *
     * @param allocations a collection of resource allocations requested from this compute node.
     * @param doAllocate if set, the operation makes actual resource allocation. Otherwise, only a test whether the
     *                   allocation fits this node is made.
     * @return <code>true</code> if the <code>allocations</code> fits the node, <code>false</code> otherwise.
     * @throws ResourceNotAvailableException
     */
    public synchronized boolean _allocate(Collection<ResourceAllocation> allocations, boolean doAllocate) throws ResourceNotAvailableException
    {
        // Note that the number of resources will be small so iterating over the resource array should be more
        // efficient than using a HashMap.

        int resNo = availableResources.size();
        // This array stores deep copy of resources that are altered by the allocation process.
        // If doAllocate is set, the resources from sandbox are copied into availableResources.
        Resource[] sandbox = availableResources.toArray(new Resource[resNo]);
        boolean[] changed = new boolean[resNo];
        String[] allocResourceName = new String[allocations.size()];
        int a = 0;

        // Loop through all required allocations
        for (ResourceAllocation alloc : allocations) {
            // Try to find a relevant resource which can match this allocation.
            if (Utils.IsNullOrEmpty(alloc.resourceName)) {
                // If resourceName is empty, use resourceType to search for a relevant resource.
                int r;
                for (r = 0; r < resNo; r++) {
                    Resource res = availableResources.get(r);
                    if (res.type.equals(alloc.resourceType)) {
                        // For resource not yet in the sandbox a deep copy must be made.
                        if (sandbox[r] == null) {
                            sandbox[r] = new Resource(res);
                        }
                        res = sandbox[r];

                        try {
                            changed[r] = _allocateProperties(res.properties, alloc.properties, res.name);

                            // Store the resource name because at the end we need to set the resource name in the
                            // allocation requirements.
                            allocResourceName[a] = res.name;

                            // If the allocation was successful, we can break the loop. Otherwise, we need to try
                            // another resources with the same type (if they exist).
                            break;
                        } catch (ResourceNotAvailableException x) {
                            // This resource cannot met the requirements;
                            _Logger.info("Resource {} cannot met given requirements", res.name, x);
                            // so continue the search.
                        }
                    }
                }

                if (r >= resNo) {
                    throw new ResourceNotAvailableException("Cannot find resource which would met requirements for allocation type: " + alloc.resourceType);
                }
            } else {
                // Otherwise, use resourceName to find exactly the requested resource.
                int r;
                for (r = 0; r < resNo; r++) {
                    if (availableResources.get(r).name.equals(alloc.resourceName)) {
                        break;
                    }
                }

                if (r >= resNo) {
                    throw new ResourceNotAvailableException("Cannot find resource: " + alloc.resourceName);
                }

                Resource resource = availableResources.get(r);

                // If the allocation resourceType is not null, it must match the actual resource type.
                if (!Utils.IsNullOrEmpty(alloc.resourceType) && !resource.type.equals(alloc.resourceType)) {
                    throw new ResourceNotAvailableException(
                            "Invalid resource type for resource: " + resource.name + ": required type: " + alloc.resourceType + ", actual type: " + resource.type);
                }

                if (sandbox[r] == null) {
                    sandbox[r] = new Resource(resource);
                }
                resource = sandbox[r];

                changed[r] = _allocateProperties(resource.properties, alloc.properties, resource.name);
            }

            a++;
        }

        // If doAllocate is set, change the available resources.
        if (doAllocate) {
            for (int i = 0; i < sandbox.length; i++) {
                if (changed[i]) {
                    availableResources.set(i, sandbox[i]);
                }
            }

            a = 0;
            for (ResourceAllocation alloc : allocations) {
                if (allocResourceName[a] != null) {
                    alloc.resourceName = allocResourceName[a];
                }

                // Sanity check
                if (Utils.IsNullOrEmpty(alloc.resourceName)) {
                    throw new RuntimeException("Internal error: empty resource name for allocation #" + a + ", type: " + alloc.resourceType);
                }

                a++;
            }
        }

        return true;
    }


    public synchronized void release(Collection<ResourceAllocation> allocations)
    {
        int resNo = availableResources.size();
        // This array stores deep copy of resources that are altered by the allocation process.
        // If doAllocate is set, the resources from sandbox are copied into availableResources.
        Resource[] sandbox = availableResources.toArray(new Resource[resNo]);
        boolean[] changed = new boolean[resNo];

        // Loop through all resource allocations...
        for (ResourceAllocation alloc : allocations) {
            if (Utils.IsNullOrEmpty(alloc.resourceName)) {
                throw new IllegalArgumentException("Error: name of an allocated resource cannot be null nor empty.");
            }

            // Otherwise, use resourceName to find exactly the requested resource.
            int r;
            for (r = 0; r < resNo; r++) {
                if (availableResources.get(r).name.equals(alloc.resourceName)) {
                    break;
                }
            }

            if (r >= resNo) {
                throw new IllegalArgumentException("Cannot find resource: " + alloc.resourceName);
            }

            Resource resource = availableResources.get(r);

            // If the allocation resourceType is not null, it must match the actual resource type.
            if (!Utils.IsNullOrEmpty(alloc.resourceType) && !resource.type.equals(alloc.resourceType)) {
                throw new IllegalArgumentException(
                        "Invalid resource type for resource: " + resource.name + ": required type: " + alloc.resourceType + ", actual type: " + resource.type);
            }

            if (sandbox[r] == null) {
                sandbox[r] = new Resource(resource);
            }
            resource = sandbox[r];

            changed[r] = _deallocateProperties(resource.properties, alloc.properties, resource.name);
        }

        // Apply changes
        for (int i = 0; i < sandbox.length; i++) {
            if (changed[i]) {
                availableResources.set(i, sandbox[i]);
            }
        }
    }


    /**
     *
     * @param resourceProperties
     * @param requirementProperties
     * @param resourceName
     * @return <code>true</code> if the operation was successful and the resourceProperties list has changed due to
     * release (e.g. changed capacity or quantity properties); <code>false</code> if the release was successful but
     * the resourceProperties list has not changed.
     */
    private boolean _deallocateProperties(ArrayList<SatisfierProperty> resourceProperties, Collection<Property> requirementProperties, String resourceName)
    {
        int resPropNo = resourceProperties.size();
        SatisfierProperty[] sandbox = new SatisfierProperty[resPropNo];

        // Now, for each property which was included in the allocation...
        for (Property allocProp : requirementProperties) {
            // Search for a relevant property in the ComputeNode resource...
            int p;
            for (p = 0; p < resPropNo; p++) {
                if (resourceProperties.get(p).getName().equals(allocProp.getName())) {
                    break;
                }
            }

            // If the property cannot be found, it's an error.
            if (p >= resPropNo) {
                throw new IllegalArgumentException("Invalid property: " + allocProp.getName() + " for resource: " + resourceName);
            }

            SatisfierProperty resProp = (sandbox[p] == null ? resourceProperties.get(p) : sandbox[p]);

            // We can ignore properties that are not dynamic, as there is nothing to release.
            if (!resProp.isDynamic()) {
                continue;
            }

            // Finally, release property according to its kind and then go for the next property.
            switch (resProp.getKind()) {
                case Attribute:
                case Maximum:
                case Minimum:
                case Selection:
                    // Nothing to be done really.
                    _Logger.debug("Property kind: {} does not involve any release action. Resource: {}, property: {}", resProp.getKind(), resourceName, resProp.getName());
                    break;
                case Capacity:
                    try {
                        if (resProp.getValue() instanceof Long) {
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetLong(resProp.getValue()) + Utils.TryGetLong(allocProp.getValue()));
                        } else if (resProp.getValue() instanceof Integer) {
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetInteger(resProp.getValue()) + Utils.TryGetInteger(allocProp.getValue()));
                        } else if (resProp.getValue() instanceof Short) {
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetShort(resProp.getValue()) + Utils.TryGetShort(allocProp.getValue()));
                        } else if (resProp.getValue() instanceof Byte) {
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetByte(resProp.getValue()) + Utils.TryGetByte(allocProp.getValue()));
                        } else {
                            throw new IllegalArgumentException("Illegal value type: " + resProp.getValue().getClass() + " for the Quantity property: " + resProp.getName() + " in resource: " + resourceName);
                        }
                    } catch (ArithmeticException x) {
                        throw new ArithmeticException("Illegal property value in resource: " + resourceName + ", property: " + resProp.getName() + ", value: " + allocProp.getValue());
                    }
                    break;
                case Quantity:
                    if (resProp.getValue() instanceof Long) {
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetLong(resProp.getValue()) + 1L);
                    } else if (resProp.getValue() instanceof Integer) {
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetInteger(resProp.getValue()) + 1);
                    } else if (resProp.getValue() instanceof Short) {
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetShort(resProp.getValue()) + (short)1);
                    } else if (resProp.getValue() instanceof Byte) {
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), Utils.TryGetByte(resProp.getValue()) + (byte)1);
                    } else {
                        throw new IllegalArgumentException("Illegal value type: " + resProp.getValue().getClass() + " for the Quantity property: " + resProp.getName() + " in resource: " + resourceName);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported resource property kind: " + resProp.getKind());
            }
        }

        // All ok, so substitute properties that has changed due to the allocation.
        boolean hasChanged = false;

        for (int p = 0; p < resPropNo; p++) {
            if (sandbox[p] != null) {
                resourceProperties.set(p, sandbox[p]);
                hasChanged = true;
            }
        }

        return hasChanged;
    }


    /**
     *
     * @param resourceProperties
     * @param requirementProperties
     * @param resourceName
     * @return <code>true</code> if the operation was successful and the resourceProperties list has changed due to
     * allocation (e.g. changed capacity or quantity properties); <code>false</code> if the allocation was successful but
     * the resourceProperties list has not changed (not capacity nor quantity properties has been requested).
     * @throws ResourceNotAvailableException if the required properties cannot be met by resource properties.
     */
    private boolean _allocateProperties(ArrayList<SatisfierProperty> resourceProperties, Collection<Property> requirementProperties, String resourceName)
    throws ResourceNotAvailableException
    {
        int resPropNo = resourceProperties.size();
        SatisfierProperty[] sandbox = new SatisfierProperty[resPropNo];

        // Loop through each allocation property and match it with a resource satisfier property.
        for (Property allocProp : requirementProperties) {
            // Search for a relevant property in the ComputeNode resource...
            int p;
            for (p = 0; p < resPropNo; p++) {
                if (resourceProperties.get(p).getName().equals(allocProp.getName())) {
                    break;
                }
            }

            // If the property cannot be found, it's an error.
            if (p >= resPropNo) {
                throw new ResourceNotAvailableException("Invalid property: " + allocProp.getName() + " for resource: " + resourceName);
            }

            SatisfierProperty resProp = (sandbox[p] == null ? resourceProperties.get(p) : sandbox[p]);
            switch (resProp.getKind()) {
                case Attribute:
                    if (!resProp.getValue().equals(allocProp.getValue())) {
                        throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                    }
                    break;
                case Maximum: {
                    Comparable<Object> resValue = (Comparable<Object>) resProp.getValue();
                    if (resValue.compareTo(allocProp.getValue()) < 0) {
                        throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                    }
                    break;
                }
                case Minimum: {
                    Comparable<Object> resValue = (Comparable<Object>) resProp.getValue();
                    if (resValue.compareTo(allocProp.getValue()) > 0) {
                        throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                    }
                    break;
                }
                case Selection: {
                    Collection<Object> resValue = (Collection<Object>) resProp.getValue();
                    boolean found = false;
                    for (Object v : resValue) {
                        if (v.equals(allocProp.getValue())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                    }
                    break;
                }
                case Capacity:
                    if (resProp.getValue() instanceof Long) {
                        Long resValue = Utils.TryGetLong(resProp.getValue());
                        try {
                            Long reqValue = Utils.TryGetLong(allocProp.getValue());

                            if (resValue < reqValue) {
                                throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                            }

                            // Make a note of this allocation.
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - reqValue);
                        } catch (ArithmeticException x) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()), x);
                        }
                    } else if (resProp.getValue() instanceof Integer) {
                        Integer resValue = Utils.TryGetInteger(resProp.getValue());
                        try {
                            Integer reqValue = Utils.TryGetInteger(allocProp.getValue());

                            if (resValue < reqValue) {
                                throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                            }

                            // Make a note of this allocation.
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - reqValue);
                        } catch (ArithmeticException x) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()), x);
                        }
                    } else if (resProp.getValue() instanceof Short) {
                        Short resValue = Utils.TryGetShort(resProp.getValue());
                        try {
                            Short reqValue = Utils.TryGetShort(allocProp.getValue());

                            if (resValue < reqValue) {
                                throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                            }

                            // Make a note of this allocation.
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - reqValue);
                        } catch (ArithmeticException x) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()), x);
                        }
                    } else if (resProp.getValue() instanceof Byte) {
                        Byte resValue = Utils.TryGetByte(resProp.getValue());
                        try {
                            Byte reqValue = Utils.TryGetByte(allocProp.getValue());

                            if (resValue < reqValue) {
                                throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                            }

                            // Make a note of this allocation.
                            sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - reqValue);
                        } catch (ArithmeticException x) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()), x);
                        }
                    } else {
                        throw new IllegalArgumentException("Illegal value type: " + resProp.getValue().getClass() + " for the Quantity property: " + resProp.getName() + " in resource: " + resourceName);
                    }
                    break;
                case Quantity:
                    if (resProp.getValue() instanceof Long) {
                        Long resValue = Utils.TryGetLong(resProp.getValue());

                        if (resValue < 1) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                        }

                        // Make a note of this allocation.
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - 1L);
                    } else if (resProp.getValue() instanceof Integer) {
                        Integer resValue = Utils.TryGetInteger(resProp.getValue());

                        if (resValue < 1) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                        }

                        // Make a note of this allocation.
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - 1);
                    } else if (resProp.getValue() instanceof Short) {
                        Short resValue = Utils.TryGetShort(resProp.getValue());

                        if (resValue < 1) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                        }

                        // Make a note of this allocation.
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - (short) 1);
                    } else if (resProp.getValue() instanceof Byte) {
                        Byte resValue = Utils.TryGetByte(resProp.getValue());

                        if (resValue < 1) {
                            throw new ResourceNotAvailableException(String.format("Cannot match %s property: %s of resource: %s: required value: %s, actual value: %s", resProp.getKind(), resProp.getName(), resourceName, resProp.getValue(), allocProp.getValue()));
                        }

                        // Make a note of this allocation.
                        sandbox[p] = new SatisfierProperty(resProp.getName(), resProp.getKind(), resProp.isDynamic(), resValue - (byte) 1);
                    } else {
                        throw new IllegalArgumentException("Illegal value type: " + resProp.getValue().getClass() + " for the Quantity property: " + resProp.getName() + " in resource: " + resourceName);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException("Unsupported resource property kind: " + resProp.getKind());
            }
        }

        // All ok, so substitute properties that has changed due to the allocation.
        boolean hasChanged = false;

        for (int p = 0; p < resPropNo; p++) {
            if (sandbox[p] != null) {
                resourceProperties.set(p, sandbox[p]);
                hasChanged = true;
            }
        }

        return hasChanged;
    }
}
