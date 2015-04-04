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
import java.util.EnumMap;
import java.util.Map.Entry;
import org.apache.hadoop.io.Writable;

public final class IndexDefinitionField extends Object implements Comparable<IndexDefinitionField>, Externalizable, Writable
{

    public enum OptionID
    {

        /** If this field should be indexable. */
        INDEXED,
        /** If this field should omit normalization. */
        OMIT_NORMS,
        /** If this field should be stored. */
        STORED,
        /** If this field stores vector's offsets. */
        STORE_TERM_VECTOR_OFFSETS,
        /** If this field stores vector's payloads. */
        STORE_TERM_VECTOR_PAYLOADS,
        /** If this field stores vector's positions. */
        STORE_TERM_VECTOR_POSITIONS,
        /** If this field should store vectors. */
        STORE_TERM_VECTORS,
        /** If this field should be tokenized/analyzed. Using the designated Analyzer. */
        TOKENIZED;
    }
    /** The name of this field. */
    private String fieldName = "";
    /** The analyzer for this field. Default = 0 */
    private byte analyzer = 0;
    /** The boost of this field. */
    private float fieldBoost = .0F;
    /** The Document Value Type of this field. Default = "" */
    private String docValueType = "";
    /** The indexing options of this field. Default = DOCS_AND_FREQS_AND_POSITIONS */
    private String indexOptions = "DOCS_AND_FREQS_AND_POSITIONS";
    /** The Numeric Precision Step of this field. Default = 4 */
    private int numericPrecisionStep = 4;
    /** The Numeric Type of this field. Default = "" */
    private String numericType = "";
    /** The value of this field. */
    private byte[] fieldValue = new byte[0];
    /** Lucene's properties for this given field. */
    private EnumMap<OptionID, Boolean> properties = new EnumMap<>(OptionID.class);
    /** If this field should be searched. */
    private boolean searchable = false;
    /** The encoding of the field when its sent to for indexing. Default = NONE */
    private String contentEncoding = "NONE";

    public IndexDefinitionField()
    {
    }

    public static IndexDefinitionField newBuild()
    {
        return new IndexDefinitionField();
    }

    public IndexDefinitionField setFieldName(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        fieldName = p;
        return this;
    }

    public IndexDefinitionField setAnalyzer(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        try {
            analyzer = Byte.parseByte(p);
        }
        catch (NumberFormatException e) {
            // TODO: Do something
        }

        return this;
    }

    public IndexDefinitionField setFieldBoost(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        try {
            fieldBoost = Float.parseFloat(p);
        }
        catch (NumberFormatException e) {
            // TODO: Do something
        }

        return this;
    }

    public IndexDefinitionField setDocValueType(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        docValueType = p;
        return this;
    }

    public IndexDefinitionField setIndexOptions(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        indexOptions = p;
        return this;
    }

    public IndexDefinitionField setNumericPrecisionStep(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        try {
            numericPrecisionStep = Integer.parseInt(p);
        }
        catch (NumberFormatException e) {
            // TODO: Do something
        }

        return this;
    }

    public IndexDefinitionField setNumericType(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        numericType = p;
        return this;
    }

    public IndexDefinitionField setFieldValue(byte[] p)
    {
        // Don't allow null values!
        if (p == null || p.length == 0) {
            return this;
        }

        fieldValue = p;
        return this;
    }

    public IndexDefinitionField addFieldProperty(OptionID key, boolean value)
    {
        properties.put(key, value);
        return this;
    }

    public IndexDefinitionField setSearchable(boolean value)
    {
        searchable = value;
        return this;
    }

    public IndexDefinitionField setContentEncoding(String p)
    {
        // Don't allow null values!
        if (p == null || p.isEmpty()) {
            return this;
        }

        contentEncoding = p;
        return this;
    }

    public String getFieldName()
    {
        return fieldName;
    }

    public byte getAnalyzer()
    {
        return analyzer;
    }

    public float getFieldBoost()
    {
        return fieldBoost;
    }

    public String getDocValueType()
    {
        return !docValueType.isEmpty() ? docValueType : null;
    }

    public String getIndexOptions()
    {
        return indexOptions;
    }

    public int getNumericPrecisionStep()
    {
        return numericPrecisionStep;
    }

    public String getNumericType()
    {
        return !numericType.isEmpty() ? numericType : null;
    }

    public byte[] getFieldValue()
    {
        return fieldValue;
    }

    public boolean getFieldProperty(OptionID key)
    {
        return properties.get(key) != null ? properties.get(key) : false;
    }

    public boolean getSearchable()
    {
        return searchable;
    }

    public String getContentEncoding()
    {
        return contentEncoding;
    }

    @Override
    public int compareTo(IndexDefinitionField o)
    {
        return fieldName.compareTo(o.getFieldName());
    }

    /**
     * Checks if this object is empty.
     *
     * @return TRUE if this object is empty, FALSE otherwise.
     */
    public boolean isEmpty()
    {
        return fieldName.equals("");
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
        out.writeUTF(fieldName);
        out.writeByte(analyzer);
        out.writeFloat(fieldBoost);
        out.writeUTF(docValueType);
        out.writeUTF(indexOptions);
        out.writeInt(numericPrecisionStep);
        out.writeUTF(numericType);
        out.writeInt(fieldValue.length);
        for (byte b : fieldValue) {
            out.writeByte(b);
        }
        out.writeInt(properties.size());
        for (Entry<OptionID, Boolean> e : properties.entrySet()) {
            out.writeUTF(e.getKey().name());
            out.writeBoolean(e.getValue());
        }
        out.writeBoolean(searchable);
        out.writeUTF(contentEncoding);
    }

    private void internalRead(DataInput in) throws IOException
    {
        fieldName = in.readUTF();
        analyzer = in.readByte();
        fieldBoost = in.readFloat();
        docValueType = in.readUTF();
        indexOptions = in.readUTF();
        numericPrecisionStep = in.readInt();
        numericType = in.readUTF();
        fieldValue = new byte[in.readInt()];
        for (int k = 0; k < fieldValue.length; k++) {
            fieldValue[k] = in.readByte();
        }
        properties = new EnumMap<>(OptionID.class);
        int propertiesLength = in.readInt();
        for (int k = 0; k < propertiesLength; k++) {
            String key = in.readUTF();
            Boolean value = in.readBoolean();
            properties.put(Enum.valueOf(OptionID.class, key), value);
        }
        searchable = in.readBoolean();
        contentEncoding = in.readUTF();
    }
}
