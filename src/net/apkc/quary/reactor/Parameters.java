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
package net.apkc.quary.reactor;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.apkc.quary.exceptions.IncompleteSearchParametersException;
import net.apkc.quary.filters.DateRangeFilter;
import net.apkc.quary.filters.FieldFilter;
import net.apkc.quary.filters.QuaryFilter;
import net.apkc.quary.filters.QuaryFilters;
import org.apache.hadoop.io.Writable;

/**
 * An instance of this class represents the parameters sent by the user for
 * performing a search.
 *
 * @author K-Zen
 */
public final class Parameters implements Writable
{

    private String queryString = ""; // Don't allow null, breaks serialization.
    private int startOffset = 0;
    private int endOffset = 10;
    private boolean useFormattedDate = false;
    private boolean useTimeRange = false;
    private int dateRangeUnit = 1;
    private int dateRangeTimeFactor = 60;
    private String fileRangeUnit = ""; // Don't allow null, breaks serialization.
    private String languageRangeUnit = ""; // Don't allow null, breaks serialization.
    private boolean useDeduplication = false;
    private int maxHammingDistance = 32;
    private QuaryFilters filters = new QuaryFilters();

    /**
     * Always make the constructor private. To create a new instance of the
     * class we must use the method newBuild().
     */
    private Parameters()
    {
        // Always empty.
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeUTF(this.queryString);
        out.writeInt(this.startOffset);
        out.writeInt(this.endOffset);
        out.writeBoolean(this.useFormattedDate);
        out.writeBoolean(this.useTimeRange);
        out.writeInt(this.dateRangeUnit);
        out.writeInt(this.dateRangeTimeFactor);
        out.writeUTF(this.fileRangeUnit);
        out.writeUTF(this.languageRangeUnit);
        out.writeBoolean(this.useDeduplication);
        out.writeInt(this.maxHammingDistance);
        this.filters.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        this.queryString = in.readUTF();
        this.startOffset = in.readInt();
        this.endOffset = in.readInt();
        this.useFormattedDate = in.readBoolean();
        this.useTimeRange = in.readBoolean();
        this.dateRangeUnit = in.readInt();
        this.dateRangeTimeFactor = in.readInt();
        this.fileRangeUnit = in.readUTF();
        this.languageRangeUnit = in.readUTF();
        this.useDeduplication = in.readBoolean();
        this.maxHammingDistance = in.readInt();
        this.filters = QuaryFilters.read(in);
    }

    public static Parameters newBuild()
    {
        return new Parameters();
    }

    public String getQueryString()
    {
        return this.queryString;
    }

    public int getStartOffset()
    {
        return this.startOffset;
    }

    public int getEndOffset()
    {
        return this.endOffset;
    }

    public boolean getUseFormattedDate()
    {
        return this.useFormattedDate;
    }

    public boolean getUseDeduplication()
    {
        return this.useDeduplication;
    }

    public int getMaxHammingDistance()
    {
        return this.maxHammingDistance;
    }

    public QuaryFilters getFilters()
    {
        return this.filters;
    }

    /**
     * The query string of the search. (Mandatory)
     *
     * @param queryString The query string to be searched.
     *
     * @return This object.
     */
    public Parameters setQueryString(String queryString) throws IncompleteSearchParametersException
    {
        if (queryString == null || queryString.isEmpty()) {
            throw new IncompleteSearchParametersException("The query string is necessary.");
        }

        this.queryString = queryString;
        return this;
    }

    /**
     * The start offset. (Optional)
     *
     * @param startOffset The start offset.
     *
     * @return This object.
     */
    public Parameters setStartOffset(String startOffset)
    {
        try {
            this.startOffset = (startOffset != null && !startOffset.isEmpty()) ? Integer.parseInt(startOffset) : 0;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * The end offset. (Optional)
     *
     * @param endOffset The end offset.
     *
     * @return This object.
     */
    public Parameters setEndOffset(String endOffset)
    {
        try {
            this.endOffset = (endOffset != null && !endOffset.isEmpty()) ? Integer.parseInt(endOffset) : 10;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * If the date should be formatted. (Optional)
     *
     * @param useFormattedDate 1 if the date should be formatted, 0 otherwise.
     *
     * @return This object.
     */
    public Parameters setUseFormattedDate(String useFormattedDate)
    {
        try {
            this.useFormattedDate = (useFormattedDate != null && !useFormattedDate.isEmpty()) ? ((Integer.parseInt(useFormattedDate) != 0)) : false;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * If the search should be a range query by time. (Optional)
     *
     * @param useTimeRange 1 if its a time range query, 0 otherwise.
     *
     * @return This object.
     */
    public Parameters setUseTimeRange(String useTimeRange)
    {
        try {
            this.useTimeRange = (useTimeRange != null && !useTimeRange.isEmpty()) ? ((Integer.parseInt(useTimeRange) != 0)) : false;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * The type of range for the date: 1(seconds), 2(minutes), 3(hours),
     * 4(days), 5(months), 6(years). (Optional)
     *
     * @param dateRangeUnit The unit of the range.
     *
     * @return This object.
     */
    public Parameters setDateRangeUnit(String dateRangeUnit)
    {
        try {
            this.dateRangeUnit = (dateRangeUnit != null && !dateRangeUnit.isEmpty()) ? Integer.parseInt(dateRangeUnit) : 1;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * The factor of time. i.e. dr=1&tm=60 => Means 60 seconds.
     *
     * @param dateRangeTimeFactor The time factor.
     *
     * @return This object.
     */
    public Parameters setDateRangeTimeFactor(String dateRangeTimeFactor)
    {
        try {
            this.dateRangeTimeFactor = (dateRangeTimeFactor != null && !dateRangeTimeFactor.isEmpty()) ? Integer.parseInt(dateRangeTimeFactor) : 60;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * The file type to be used in a filter. If this param is set, then search
     * only files which are of this filetype. i.e. application/vnd.ms-excel
     *
     * @param fileRangeUnit The mime type to use in the range.
     *
     * @return This object.
     */
    public Parameters setFileRangeUnit(String fileRangeUnit)
    {
        try {
            this.fileRangeUnit = (fileRangeUnit != null && !fileRangeUnit.isEmpty()) ? fileRangeUnit : "";
        }
        catch (Exception e) {
            // Do something
        }
        return this;
    }

    /**
     * Filter results by language. 2 letter language code. i.e. en, es
     *
     * @param languageRangeUnit The 2 letter code of the language.
     *
     * @return This object.
     */
    public Parameters setLanguageRangeUnit(String languageRangeUnit)
    {
        try {
            this.languageRangeUnit = (languageRangeUnit != null && !languageRangeUnit.isEmpty()) ? languageRangeUnit : "";
        }
        catch (Exception e) {
            // Do something
        }
        return this;
    }

    /**
     * De-duplicate results in real-time. (Optional)
     *
     * @param useDeduplication 1 if de-duplication is to be used, 0 otherwise.
     *
     * @return This object.
     */
    public Parameters setUseDeduplication(String useDeduplication)
    {
        try {
            this.useDeduplication = (useDeduplication != null && !useDeduplication.isEmpty()) ? ((Integer.parseInt(useDeduplication) != 0)) : false;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * Max Hamming distance for the de-duplication. i.e. 32, 24, 16, 8.
     * Representing the max. limit in bits.
     *
     * @param maxHammingDistance The maximum hamming distance.
     *
     * @return This object.
     */
    public Parameters setMaxHammingDistance(String maxHammingDistance)
    {
        try {
            this.maxHammingDistance = (maxHammingDistance != null && !maxHammingDistance.isEmpty()) ? Integer.parseInt(maxHammingDistance) : 32;
        }
        catch (NumberFormatException e) {
            // Do something
        }
        return this;
    }

    /**
     * Creates the filter based on the previous options.
     *
     * @return This object.
     */
    public Parameters setFilters()
    {
        try {
            if (useTimeRange) { // Use DateRangeFilter.
                // Create the DateRangeFilter object.
                QuaryFilter filter = new DateRangeFilter();
                filter.setDateRange(dateRangeUnit);
                filter.setTimeFactor(dateRangeTimeFactor);
                // Add the filter.
                filters.addFilter(filter);
            }
            if (!fileRangeUnit.isEmpty()) {
                // Create the FieldFilter object.
                QuaryFilter filter = new FieldFilter();
                filter.setField("filetype");
                filter.setTerm(fileRangeUnit);
                // Add the filter.
                filters.addFilter(filter);
            }
            if (!languageRangeUnit.isEmpty()) {
                // Create the FieldFilter object.
                QuaryFilter filter = new FieldFilter();
                filter.setField("lang");
                filter.setTerm(languageRangeUnit);
                // Add the filter.
                filters.addFilter(filter);
            }
        }
        catch (Exception e) {
            // Do something
        }
        return this;
    }
}
