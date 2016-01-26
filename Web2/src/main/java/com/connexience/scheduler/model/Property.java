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

import java.io.*;

/**
 * Created by Jacek on 14/12/2015.
 */
public class Property
{
    private final String name;
    private final Object value;


    public Property(String name, String value)
    {
        this.name = name;
        this.value = value;
    }


    public Property(String name, Number value)
    {
        this.name = name;
        // The assumption is that Number is immutable, which is true at least for basic types such as Byte, Short, etc.
        this.value = value;
    }


    public Property(String name, Serializable value) throws IOException
    {
        this.name = name;

        // Since value is of a very generic type, to make the property immutable we need to make a copy.
        // The easiest way is to serialize and deserialize it.

        // Serialize the value
        ByteArrayOutputStream outData = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(outData)) {
            out.writeObject(value);
        } catch (IOException x) {
            throw new IOException("Cannot make a copy of the given property value", x);
        }

        // And deserialize it.
        ByteArrayInputStream inData = new ByteArrayInputStream(outData.toByteArray());
        try (ObjectInputStream in = new ObjectInputStream(inData)) {
            this.value = in.readObject();
        } catch (IOException | ClassNotFoundException x) {
            throw new IOException("Cannot make a copy of the given property value", x);
        }
    }

    public String getName() { return name; }
    public Object getValue() { return value; }
}
