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
package net.apkc.quary.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

/**
 * Utility class for counting time.
 *
 * @author K-Zen
 */
public class Timer implements Writable
{

    public static enum Time
    {

        MILLISECOND, SECOND, MINUTE, HOUR
    }
    private long start = 0L;
    private long end = 0L;

    public void starTimer()
    {
        this.start = System.currentTimeMillis();
    }

    public void endTimer()
    {
        this.end = System.currentTimeMillis();
    }

    /**
     * Returns the running time in milliseconds.
     *
     * @return The running time.
     */
    public long getExecutionTime()
    {
        if (this.start > 0L) {
            return (System.currentTimeMillis() - this.start);
        }
        else {
            return 0L;
        }
    }

    /**
     * This method will compute the difference between two times in
     * milliseconds.
     *
     * @param timeUnit The time unit for the response.
     *
     * @return The time difference.
     */
    public double computeOperationTime(Timer.Time timeUnit)
    {
        switch (timeUnit) {
            case MILLISECOND:
                return (this.end - this.start);
            case SECOND:
                return ((this.end - this.start) / 1000.0);
            case MINUTE:
                return ((this.end - this.start) / 1000.0) / 60;
            case HOUR:
                return ((this.end - this.start) / 1000.0) / 3600;
            default:
                return (this.end - this.start);
        }
    }

    /**
     * This method will compute the difference between two times, but the time
     * is passed as a parameter.
     *
     * @param timeUnit    The time unit for the response.
     * @param elapsedTime The time in milliseconds.
     *
     * @return The time difference.
     */
    public double customComputeOperationTime(Timer.Time timeUnit, long elapsedTime)
    {
        switch (timeUnit) {
            case MILLISECOND:
                return (elapsedTime);
            case SECOND:
                return (elapsedTime / 1000.0);
            case MINUTE:
                return (elapsedTime / 1000.0) / 60;
            case HOUR:
                return (elapsedTime / 1000.0) / 3600;
            default:
                return (elapsedTime);
        }
    }

    @Override
    public void write(DataOutput out) throws IOException
    {
        out.writeLong(this.start);
        out.writeLong(this.end);
    }

    @Override
    public void readFields(DataInput in) throws IOException
    {
        this.start = in.readLong();
        this.end = in.readLong();
    }
}
