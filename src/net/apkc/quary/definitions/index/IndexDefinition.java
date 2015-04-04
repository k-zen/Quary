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
package net.apkc.quary.definitions.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.Writable;

public final class IndexDefinition extends Object implements Comparable<IndexDefinition>, Externalizable, Writable
{

    private String definitionID = "";
    private float scoreCoeficient = .0f;
    private List<IndexDefinitionField> fields = new ArrayList<>();

    public IndexDefinition()
    {
    }

    public static IndexDefinition newBuild()
    {
        return new IndexDefinition();
    }

    public IndexDefinition setDefinitionID(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        definitionID = p;
        return this;
    }

    public IndexDefinition setScoreCoeficient(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        try {
            scoreCoeficient = Float.parseFloat(p);
        }
        catch (NumberFormatException e) {
            // TODO: Do something
        }

        return this;
    }

    public IndexDefinition setFields(List<IndexDefinitionField> fields)
    {
        this.fields = fields;
        return this;
    }

    //////////
    // MISC //
    //////////
    public IndexDefinition addField(IndexDefinitionField field)
    {
        // Don't allow null values!
        if (field == null || field.isEmpty()) {
            return this;
        }

        fields.add(field);
        return this;
    }

    public String getDefinitionID()
    {
        return definitionID;
    }

    public float getScoreCoeficient()
    {
        return scoreCoeficient;
    }

    public IndexDefinitionField[] getFields()
    {
        return fields.toArray(new IndexDefinitionField[0]);
    }

    @Override
    public int compareTo(IndexDefinition o)
    {
        return definitionID.compareTo(o.getDefinitionID());
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        internalRead(in);
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        internalWrite(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        internalRead(in);
    }

    private void internalWrite(DataOutput out) throws IOException
    {
        out.writeUTF(definitionID);
        out.writeFloat(scoreCoeficient);
        out.writeInt(fields.size());
        for (IndexDefinitionField f : fields) {
            f.write(out);
        }
    }

    private void internalRead(DataInput in) throws IOException
    {
        definitionID = in.readUTF();
        scoreCoeficient = in.readFloat();
        int fieldsLength = in.readInt();
        for (int k = 0; k < fieldsLength; k++) {
            IndexDefinitionField f = IndexDefinitionField.newBuild();
            f.readFields(in);
            fields.add(f);
        }
    }
}
