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
package net.apkc.quary.filters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Filter that implements a Term query.
 *
 * @author K-Zen
 */
public class FieldFilter implements QuaryFilter
{

    private byte type = QuaryFilters.TERM_FILTER;
    private String field = "filetype";
    private String term = "text/plain";

    @Override
    public String getField()
    {
        return field;
    }

    @Override
    public String getTerm()
    {
        return term;
    }

    @Override
    public void setField(String field)
    {
        this.field = field;
    }

    @Override
    public void setTerm(String term)
    {
        this.term = term;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeUTF(field);
        out.writeUTF(term);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        field = in.readUTF();
        term = in.readUTF();
    }

    @Override
    public byte getType()
    {
        return type;
    }

    @Override
    public void setDateRange(int dateRange)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getDateRange()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTimeFactor(int timeFactor)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTimeFactor()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
