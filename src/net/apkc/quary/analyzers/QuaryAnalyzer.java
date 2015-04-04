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
package net.apkc.quary.analyzers;

import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

public abstract class QuaryAnalyzer extends Analyzer
{

    public static Analyzer getAnalyzer(byte analyzerID)
    {
        switch (analyzerID) {
            case 0:
                return EnglishAnalyzer
                        .newBuild()
                        .enableFiltering(true)
                        .enableStemming(true);
            case 1:
                return EnglishAnalyzer
                        .newBuild()
                        .enableFiltering(true)
                        .enableStemming(false);
            case 2:
                return EnglishAnalyzer
                        .newBuild()
                        .enableFiltering(false)
                        .enableStemming(false);
            case 3:
                return SpanishAnalyzer
                        .newBuild()
                        .enableFiltering(true)
                        .enableStemming(true);
            case 4:
                return SpanishAnalyzer
                        .newBuild()
                        .enableFiltering(true)
                        .enableStemming(false);
            case 5:
                return SpanishAnalyzer
                        .newBuild()
                        .enableFiltering(false)
                        .enableStemming(false);
            case 6:
                return new StandardAnalyzer(Version.LUCENE_46);
            case 7:
                return new WhitespaceAnalyzer(Version.LUCENE_46);
            default:
                return EnglishAnalyzer
                        .newBuild()
                        .enableFiltering(true)
                        .enableStemming(true);
        }
    }

    /**
     * If the analyzer should stem the text.
     *
     * @param stemming If stemming is enabled.
     *
     * @return This instance.
     */
    abstract QuaryAnalyzer enableStemming(boolean stemming);

    /**
     * If the analyzer should filter the text.
     *
     * @param filtering If filtering is enabled.
     *
     * @return This instance.
     */
    abstract QuaryAnalyzer enableFiltering(boolean filtering);

    @Override
    protected abstract TokenStreamComponents createComponents(String fieldName, Reader reader);
}
