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

import com.connexience.scheduler.AlreadyRegisteredException;
import com.connexience.scheduler.ResourceNotAvailableException;
import com.connexience.scheduler.model.ComputeNode;
import com.connexience.scheduler.model.ResourceAllocationRequest;
import com.connexience.server.ConnexienceException;

import javax.ejb.Local;
import java.rmi.RemoteException;


/**
 * Created by Jacek on 14/12/2015.
 */
@Local
public interface InvocationSchedulerLocal
{
    void requestResources(ResourceAllocationRequest request) throws ResourceNotAvailableException;

    void releaseResources(String requestId) throws ResourceNotAvailableException;

    ResourceAllocationRequest[] listResourceRequests();

    String registerNode(ComputeNode node) throws AlreadyRegisteredException;

    void unregisterNode(String nodeId, String regId) throws ResourceNotAvailableException;

    ComputeNode[] listRegisteredNodes();
}
