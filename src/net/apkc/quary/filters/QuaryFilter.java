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

import org.apache.hadoop.io.Writable;

public interface QuaryFilter extends Writable
{

    /**
     * This method will return the type of object this filter is.
     *
     * @return A byte denoting the type of object this implementation of filter it is.
     */
    public byte getType();

    /**
     * This method sets the range of date only to be used in the DateRangeFilter.
     *
     * @param dateRange The date range.
     */
    public void setDateRange(int dateRange);

    /**
     * Returns the value of the date range.
     *
     * @return The value of the date range.
     */
    public int getDateRange();

    /**
     * This method sets the time factor only to be used in the DateRangeFilter.
     *
     * @param timeFactor The time factor.
     */
    public void setTimeFactor(int timeFactor);

    /**
     * Returns the value of the time factor.
     *
     * @return The value of the time factor.
     */
    public int getTimeFactor();

    /**
     * Returns the value of the selected field for search.
     *
     * @return A string containing the field to search.
     */
    public String getField();

    /**
     * This method sets the field to be used for search.
     *
     * @param field The name of the field.
     */
    public void setField(String field);

    /**
     * Returns the value of the term to search.
     *
     * @return A string containing the term to search.
     */
    public String getTerm();

    /**
     * This method sets the term to be used for search.
     *
     * @param term The value of the term.
     */
    public void setTerm(String term);
}
