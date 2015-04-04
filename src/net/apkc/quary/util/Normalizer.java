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

/**
 * Handles the normalization of big numbers to a number between 0 and 1.
 * <pre>
 * Ej.
 * normalize(2037.968) -> 0.9211259
 * </pre>
 *
 * @author K-Zen
 */
public class Normalizer
{

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(long number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        long delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(float number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        float delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }

    /**
     * Normalizes any number between 0 and 1.
     * <pre>
     * µ = 0.85
     * Ω(∆) = 1 - 3√ ( ∆ / ( ∆^2 + µ ) )
     * </pre>
     *
     * @param number The number to normalize.
     *
     * @return A number between 0 and 1.
     */
    public static double normalize(double number)
    {
        double omega = 0.0d;
        double mu = 0.85d;
        double delta = number;

        if (number > 0) {
            omega = (1 - Math.cbrt(delta / (Math.pow(delta, 2) + mu)));
        }

        // Control momentaneo que no sea infinito.
        if (Double.isNaN(omega)) {
            return 0.0d;
        }
        else {
            return omega;
        }
    }
}
