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

/**
 * This class includes a set of constants related to resources and resource allocations.
 *
 * Created by Jacek on 14/12/2015.
 */
public abstract class Constants
{
    public static class ResourceType
    {
        public static final String CPU = "Cpu";
        public static final String MEMORY = "Memory";  // Volatile RAM
        public static final String DISK = "Disk";  // Non-volatile disk space
        public static final String INVOCATION_THREAD = "InvocationThread";
    }

    public static class Property
    {
        public static final String CPU_ARCHITECTURE = "CpuArchitecture";
        public static final String CPU_CORES = "CpuCoreNumber";
        public static final String CPU_SPEED = "CpuClockFrequency";
        public static final String CPU_LOAD = "CpuLoad";

        public static final String TOTAL = "Total";
        public static final String AVAILABLE = "Available";
    }

    public static class Request
    {
        public static final String REQUEST_TYPE_INVOCATION = "InvocationRequest";
        public static final String REQUEST_TYPE_USER = "UserRequest";
    }
}
