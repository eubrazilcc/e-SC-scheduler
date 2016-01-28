package com.connexience.scheduler.model;

import java.util.*;


/**
 * Created by Jacek on 14/12/2015.
 */
public class Resource
{
    /**
     * A unique name of the resource within a node.
     */
    public String name;

    /**
     * A type of the resource taken from Constants.ResourceType.
     */
    public String type;

    public ArrayList<SatisfierProperty> properties = new ArrayList<>(1);


    public Resource() {}


    public Resource(String name, String type, Collection<SatisfierProperty> properties)
    {
        this.name = name;
        this.type = type;
        this.properties.addAll(properties);
    }


    public Resource(String name, String type, SatisfierProperty... properties)
    {
        this.name = name;
        this.type = type;
        this.properties.addAll(Arrays.asList(properties));
    }


    public Resource(Resource r)
    {
        this.name = r.name;
        this.type = r.type;
        this.properties.addAll(r.properties);
    }


    public SatisfierProperty getProperty(String name)
    {
        for (SatisfierProperty p : properties) {
            if (p.getName().equals(name)) {
                return p;
            }
        }

        return null;
    }


    public void setProperty(String name, Number value)
    {
        ListIterator<SatisfierProperty> iter = properties.listIterator();

        while (iter.hasNext()) {
            SatisfierProperty p = iter.next();
            if (p.getName().equals(name)) {
                iter.set(new SatisfierProperty(p.getName(), p.getKind(), p.isDynamic(), value));
                return;
            }
        }

        throw new IllegalArgumentException("Invalid property name: " + name);
    }
}
