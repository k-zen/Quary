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

import org.apache.hadoop.conf.Configuration;

/**
 * This class handles all the scoring/ranking operations of Quary.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 */
public class Rank
{

    /**
     * This method calculates the gravity value for a certain timestamp.
     *
     * @param timestamp The timestamp value.
     *
     * @return The gravity value for a given timestamp.
     */
    public static double calculateGravity(String timestamp)
    {
        // Info: Convert timestamp from milliseconds to seconds for weaker gravity.
        // Info: Also, raise the value of the constant for weaker gravity.
        float tau = Long.parseLong(timestamp);
        final float C = 512;

        return (1 / (Math.exp(-C * Normalizer.normalize(tau) + C)));
    }

    /**
     * This method calculates the gravity value for a certain timestamp,
     * avoiding the conversion to seconds. This gives a lot lower granularity,
     * and its perfect for using when high differences between times is not
     * necessary.
     *
     * @param timestamp The timestamp value.
     *
     * @return The gravity value for a given timestamp.
     */
    public static double calculateGravityLowGranularity(long timestamp)
    {
        // Info: Convert timestamp from milliseconds to seconds for weaker gravity.
        // Info: Also, raise the value of the constant for weaker gravity.
        float tau = timestamp;
        final float C = 512;

        return (1 / (Math.exp(-C * Normalizer.normalize(tau) + C)));
    }

    /**
     * This method calculates the fetch interval with gravity for a given score.
     * This new system gives re-fetching preferences to more newer pages,
     * instead of re-fetching everything.
     *
     * <p>
     * For calculating the score with gravity this method works best with the
     * given method calculateGravityLowGranularity().</p>
     *
     * @param conf  The configuration object.
     * @param score The score with gravity of the page.
     *
     * @return The new fetching interval.
     */
    public static long calculateFetchInterval(Configuration conf, float score)
    {
        long tau = conf.getLong("db.fetch.interval.default", 86400);
        double omega = score;
        double delta = 10.00f;

        return Math.round(tau * (delta - omega) + tau);
    }
}
