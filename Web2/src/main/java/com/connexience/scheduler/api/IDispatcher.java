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
package com.connexience.scheduler.api;

import com.connexience.scheduler.model.ComputeNode;
import com.connexience.scheduler.model.ResourceAllocationRequest;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A simple interface for a task scheduler. It assumes that tasks are independent
 *
 * Created by Jacek on 14/12/2015.
 */
public interface IDispatcher
{
    /**
     * Dispatches given requests among available resources and returns a map { request -> node } about what should run
     * where. Note that the output map size maybe shorter than the length of the requests collection, which means
     * that certain requests cannot be fulfilled at the moment.
     *
     * @param requests
     * @param availableNodes
     * @return a list of pairs with allocations { request -> node }. The returned list may be shorter than <code>requests</code> (or even empty)
     * if available nodes cannot meet requirements for some requests.
     */
    List<Map.Entry<ResourceAllocationRequest, ComputeNode>> dispatch(Collection<ResourceAllocationRequest> requests, Collection<ComputeNode> availableNodes);
}
