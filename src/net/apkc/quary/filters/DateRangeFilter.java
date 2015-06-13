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
 * Filter that implements a RangeTerm query.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 */
public class DateRangeFilter implements QuaryFilter
{

    public static final int SECOND_RANGE = 1;
    public static final int MINUTE_RANGE = 2;
    public static final int HOUR_RANGE = 3;
    public static final int DAY_RANGE = 4;
    public static final int MONTH_RANGE = 5;
    public static final int YEAR_RANGE = 6;
    private byte type = QuaryFilters.DATE_RANGE_FILTER;
    private int dateRange = 1;
    private int timeFactor = 60;

    @Override
    public void setDateRange(int dateRange)
    {
        this.dateRange = dateRange;
    }

    @Override
    public int getDateRange()
    {
        return dateRange;
    }

    @Override
    public void setTimeFactor(int timeFactor)
    {
        this.timeFactor = timeFactor;
    }

    @Override
    public int getTimeFactor()
    {
        return timeFactor;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeInt(dateRange);
        out.writeInt(timeFactor);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        dateRange = in.readInt();
        timeFactor = in.readInt();
    }

    @Override
    public byte getType()
    {
        return type;
    }

    @Override
    public String getField()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getTerm()
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setField(String field)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTerm(String term)
    {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
