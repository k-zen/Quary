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
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.Writable;

/**
 * This class handles the filters passed to search methods of the IndexServer
 * instance.
 *
 * @author K-Zen
 */
public class QuaryFilters implements Writable
{

    public static final byte DATE_RANGE_FILTER = 0x1;
    public static final byte TERM_FILTER = 0x2;
    private List<QuaryFilter> filters = new ArrayList<>(0);

    public void addFilter(QuaryFilter f)
    {
        filters.add(f);
    }

    public List<QuaryFilter> getFilters()
    {
        return filters;
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeInt(filters.size());
        for (QuaryFilter f : filters) {
            out.writeByte(f.getType());
            f.write(out);
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        int size = in.readInt();
        for (int k = 0; k < size; k++) {
            switch (in.readByte()) {
                case QuaryFilters.DATE_RANGE_FILTER:
                    QuaryFilter f1 = new DateRangeFilter();
                    f1.readFields(in);
                    filters.add(f1);
                    break;
                case QuaryFilters.TERM_FILTER:
                    QuaryFilter f2 = new FieldFilter();
                    f2.readFields(in);
                    filters.add(f2);
                    break;
            }
        }
    }

    public static QuaryFilters read(DataInput in) throws IOException
    {
        QuaryFilters filters = new QuaryFilters();
        filters.readFields(in);

        return filters;
    }
}
