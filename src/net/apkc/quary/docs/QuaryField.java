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
import net.apkc.quary.util.StringTools;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Writable;
import org.apache.log4j.Logger;

/**
 * This class represents a multi-valued field with a weight.
 * <p>
 * Values are arbitrary objects. In most of the cases they are Strings.
 * </p>
 *
 * <p>
 * Weight is very important here, because this value is used in the
 * calculation of the score of the document during indexing. This weight value
 * is used to calculate the "Length Normalization" value for the document. This
 * value can't be 0.0f or it'll break the entire scoring system.
 * </p>
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 */
public class QuaryField implements Writable
{

    private static final Logger LOG = Logger.getLogger(QuaryField.class.getName());
    private static final int CHUNK_MAX = 16 * 1024;
    private float weight = 1.0F;
    private String value = "";

    public QuaryField()
    {
        weight = 1.0F;
    }

    public QuaryField(String value)
    {
        this(value, 1.0F);
    }

    public QuaryField(String value, float weight)
    {
        this.weight = weight;
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    public float getWeight()
    {
        return weight;
    }

    public void setWeight(float weight)
    {
        this.weight = weight;
    }

    public void reset()
    {
        weight = 1.0F;
        value = "";
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        // What info to receive as part of a document field.
        // 1. In how many chunks we are dividing the "value".
        // 2. Length in bytes of the total value.
        // 3. Value/s (Depends on the chunks) ... i.e. The URL of the document.
        //    With every chunk:
        //    3.1. Chunk
        //    3.2. MD5 hash of this chunk.
        //    3.3. Length in bytes of this chunk.
        String buffer = "";
        int chunks = in.readInt();
        int totalSize = in.readInt();
        for (int k = 0; k < chunks; k++) {
            String chunk = in.readUTF();
            String hash = in.readUTF();
            int chunkSize = in.readInt();
            // Check if data is correct.
            if (!MD5Hash.digest(chunk).toString().equals(hash)) {
                LOG.info("MD5 hash of chunk don't match!");
            }
            if (chunk.getBytes().length != chunkSize) {
                LOG.info("Chunk size don't match!");
            }
            // Add the chunk to the field value.
            buffer = buffer.concat(chunk);
        }

        // Check if data is correct.
        if (buffer.getBytes().length != totalSize) {
            LOG.info("Data size don't match!");
        }

        value = buffer; // Set the value.
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        // What info to send as part of a document field.
        // 1. In how many chunks we are dividing the "value".
        // 2. Length in bytes of the total value.
        // 3. Value/s (Depends on the chunks) ... i.e. The URL of the document.
        //    With every chunk:
        //    3.1. Chunk
        //    3.2. MD5 hash of this chunk.
        //    3.3. Length in bytes of this chunk.
        String[] chunks = StringTools.splitStringByBytes(value, CHUNK_MAX);
        if (chunks.length <= 1) { // If it's less than 16Kb, then send all together.
            out.writeInt(1);
            out.writeInt(value.getBytes().length);
            out.writeUTF(value);
            out.writeUTF(MD5Hash.digest(value).toString());
            out.writeInt(value.getBytes().length);
        }
        else { // Else send in chunks.
            out.writeInt(chunks.length);
            out.writeInt(value.getBytes().length);
            for (String chunk : chunks) {
                out.writeUTF(chunk);
                out.writeUTF(MD5Hash.digest(chunk).toString());
                out.writeInt(chunk.getBytes().length);
            }
        }
    }
}
