/*
 * Copyright (c) 2014, Andreas P. Koenzen <akc at apkc.net>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.apkc.quary.docs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.hadoop.io.VersionMismatchException;
import org.apache.hadoop.io.Writable;

public class QuaryDocument implements Writable, Iterable<Entry<String, QuaryField>>
{

    public static final byte VERSION = 1;
    private Map<String, QuaryField> fields = new HashMap<>();
    private float weight = 1.0F;
    private String signature = "";
    private String definitionID = "";

    public QuaryDocument()
    {
    }

    public static QuaryDocument newBuild()
    {
        return new QuaryDocument();
    }

    public QuaryDocument add(String name, String value)
    {
        fields.put(name, new QuaryField(value));
        return this;
    }

    public String getFieldValue(String name)
    {
        QuaryField field = fields.get(name);
        if (field == null) {
            return "";
        }

        return field.getValue();
    }

    public QuaryField getField(String name)
    {
        return fields.get(name);
    }

    public QuaryField removeField(String name)
    {
        return fields.remove(name);
    }

    public Collection<String> getFieldNames()
    {
        return fields.keySet();
    }

    @Override
    public Iterator<Entry<String, QuaryField>> iterator()
    {
        return fields.entrySet().iterator();
    }

    public float getWeight()
    {
        return weight;
    }

    public QuaryDocument setWeight(float weight)
    {
        this.weight = weight;
        return this;
    }

    public QuaryDocument setSignature(String signature)
    {
        this.signature = signature;
        return this;
    }

    public String getSignature()
    {
        return signature;
    }

    public QuaryDocument setDefintionID(String definitionID)
    {
        this.definitionID = definitionID;
        return this;
    }

    public String getDefinitionID()
    {
        return definitionID;
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        fields.clear(); // Clear all fields.

        // 1. Version (Byte)
        // 2. Weight (Float)
        // 3. DocumentMeta (Object)
        // 4. Field Size (Int) 
        // 5. Fields
        //    5.1. Name of Field (String)
        //    5.2. AIMEField (Object)
        byte version = in.readByte();
        if (version != VERSION) {
            throw new VersionMismatchException(VERSION, version);
        }
        weight = in.readFloat();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String name = in.readUTF();
            QuaryField field = new QuaryField();
            field.readFields(in);
            fields.put(name, field);
        }
        signature = in.readUTF();
        definitionID = in.readUTF();
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        // 1. Version (Byte)
        // 2. Weight (Float)
        // 3. DocumentMeta (Object)
        // 4. Field Size (Int)
        // 5. Fields
        //    5.1. Name of Field (String)
        //    5.2. AIMEField (Object)
        out.writeByte(VERSION);
        out.writeFloat(weight);
        out.writeInt(fields.size());
        for (Map.Entry<String, QuaryField> entry : fields.entrySet()) {
            out.writeUTF(entry.getKey());
            entry.getValue().write(out);
        }
        out.writeUTF(signature);
        out.writeUTF(definitionID);
    }
}
