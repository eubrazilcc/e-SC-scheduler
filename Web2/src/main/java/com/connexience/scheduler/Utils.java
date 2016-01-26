package com.connexience.scheduler;

import com.connexience.performance.model.WorkflowEngineInstance;
import com.connexience.scheduler.model.*;


/**
 * This is a utility class with some helper methods used throughout the project.
 *
 * Created by Jacek on 16/12/2015.
 */
public abstract class Utils
{
    /**
     * A simple helper method that converts an array of engine information into an array of generic ComputeNodes.
     *
     * @param engineInfo
     * @return
     */
    public static ComputeNode[] ToComputeNodes(WorkflowEngineInstance[] engineInfo)
    {
        ComputeNode[] nodes = new ComputeNode[engineInfo.length];

        int i = 0;
        for (WorkflowEngineInstance info : engineInfo) {
            nodes[i++] = ToComputeNode(info);
        }

        return nodes;
    }


    /**
     * This method converts e-SC specific information about a workflow engine into a generic ComputeNode information
     * used by the Scheduler.
     *
     * @param engineInfo
     * @return
     */
    public static ComputeNode ToComputeNode(WorkflowEngineInstance engineInfo)
    {
        ComputeNode node = new ComputeNode(engineInfo.getIpAddress());

        node.availableResources.add(new Resource("workflow invocation threads", Constants.ResourceType.INVOCATION_THREAD,
                new SatisfierProperty(Constants.Property.TOTAL, SatisfierPropertyKind.Attribute, false, engineInfo.getMaxConcurrentWorkflowInvocation()),
                new SatisfierProperty(Constants.Property.AVAILABLE, SatisfierPropertyKind.Quantity, true, engineInfo.getMaxConcurrentWorkflowInvocation() - engineInfo.getRunningWorkflowCount())));

        node.availableResources.add(new Resource("cpu", Constants.ResourceType.CPU,
                new SatisfierProperty(Constants.Property.CPU_ARCHITECTURE, SatisfierPropertyKind.Attribute, false, engineInfo.getArchitecture()),
                new SatisfierProperty(Constants.Property.CPU_CORES, SatisfierPropertyKind.Attribute, false, engineInfo.getCpuCount()),
                // TODO: Is this maximum CPU speed or current CPU speed: dynamic == true | false
                new SatisfierProperty(Constants.Property.CPU_SPEED, SatisfierPropertyKind.Maximum, false, engineInfo.getCpuSpeed()),
                new SatisfierProperty(Constants.Property.CPU_LOAD, SatisfierPropertyKind.Maximum, true, engineInfo.getCpuPercentUsed())));

        node.availableResources.add(new Resource("storage", Constants.ResourceType.DISK,
                new SatisfierProperty(Constants.Property.AVAILABLE, SatisfierPropertyKind.Capacity, true, engineInfo.getDiskFreeSpace())));

        node.availableResources.add(new Resource("RAM", Constants.ResourceType.MEMORY,
                new SatisfierProperty(Constants.Property.TOTAL, SatisfierPropertyKind.Attribute, false, engineInfo.getPhysicalRam()),
                new SatisfierProperty(Constants.Property.AVAILABLE, SatisfierPropertyKind.Capacity, true, engineInfo.getFreeRam())));

        return node;
    }


    public static ComputeNode GetDefaultComputeNode(String engineId, int maxInvocationThreads)
    {
        return new ComputeNode(
                engineId,
                new Resource("workflow execution threads", Constants.ResourceType.INVOCATION_THREAD,
                        new SatisfierProperty(Constants.Property.TOTAL, SatisfierPropertyKind.Attribute, false, maxInvocationThreads),
                        new SatisfierProperty(Constants.Property.AVAILABLE, SatisfierPropertyKind.Quantity, true, maxInvocationThreads)));
    }


    public static boolean IsNullOrEmpty(String text)
    {
        return text == null || "".equals(text);
    }


    public static boolean IsNullOrBlank(String text)
    {
        return text == null || "".equals(text.trim());
    }

    public static Long TryGetLong(Object propertyValue)
    {
        if (propertyValue == null) {
            return null;
        }

        if (propertyValue instanceof Long) {
            return (Long)propertyValue;
        } else if (propertyValue instanceof Integer) {
            return ((Integer) propertyValue).longValue();
        } else if (propertyValue instanceof Short) {
            return ((Short) propertyValue).longValue();
        } else if (propertyValue instanceof Byte) {
            return ((Byte) propertyValue).longValue();
        }

        throw new IllegalArgumentException("Cannot convert value type: " + propertyValue.getClass() + " to Long.");
    }

    public static Integer TryGetInteger(Object propertyValue)
    {
        if (propertyValue == null) {
            return null;
        }

        if (propertyValue instanceof Long) {
            Long v = (Long)propertyValue;
            if (v >= Integer.MIN_VALUE && v <= Integer.MAX_VALUE) {
                return v.intValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Integer");
            }
        } else if (propertyValue instanceof Integer) {
            return (Integer) propertyValue;
        } else if (propertyValue instanceof Short) {
            return ((Short) propertyValue).intValue();
        } else if (propertyValue instanceof Byte) {
            return ((Byte) propertyValue).intValue();
        }

        throw new IllegalArgumentException("Cannot convert value type: " + propertyValue.getClass() + " to Integer.");
    }

    public static Short TryGetShort(Object propertyValue)
    {
        if (propertyValue == null) {
            return null;
        }

        if (propertyValue instanceof Long) {
            Long v = (Long)propertyValue;
            if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
                return v.shortValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Short");
            }
        } else if (propertyValue instanceof Integer) {
            Integer v = (Integer) propertyValue;
            if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
                return v.shortValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Short");
            }
        } else if (propertyValue instanceof Short) {
            return (Short)propertyValue;
        } else if (propertyValue instanceof Byte) {
            return ((Byte) propertyValue).shortValue();
        }

        throw new IllegalArgumentException("Cannot convert value type: " + propertyValue.getClass() + " to Short.");
    }

    public static Byte TryGetByte(Object propertyValue)
    {
        if (propertyValue == null) {
            return null;
        }

        if (propertyValue instanceof Long) {
            Long v = (Long)propertyValue;
            if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
                return v.byteValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Byte");
            }
        } else if (propertyValue instanceof Integer) {
            Integer v = (Integer) propertyValue;
            if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
                return v.byteValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Byte");
            }
        } else if (propertyValue instanceof Short) {
            Short v = (Short) propertyValue;
            if  (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
                return v.byteValue();
            } else {
                throw new ArithmeticException("Property value: " + v + " cannot be cast to Byte");
            }
        } else if (propertyValue instanceof Byte) {
            return (Byte)propertyValue;
        }

        throw new IllegalArgumentException("Cannot convert value type: " + propertyValue.getClass() + " to Byte.");
    }
}
