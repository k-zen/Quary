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
import java.util.Set;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

/**
 * English language text analyzer.
 *
 * @author Andreas P. Koenzen
 * @version 1.0
 */
public class EnglishAnalyzer extends QuaryAnalyzer
{

    private Set<Object> stopWords;
    private boolean stemming = true;  // Always enabled unless explicitly disabled.
    private boolean filtering = true; // Always enabled unless explicitly disabled.

    public static EnglishAnalyzer newBuild()
    {
        return new EnglishAnalyzer();
    }

    private EnglishAnalyzer()
    {
        stopWords = StopFilter.makeStopSet(Version.LUCENE_46, StopWords.ENGLISH_STOP_WORDS);
    }

    @Override
    public EnglishAnalyzer enableStemming(boolean stemming)
    {
        this.stemming = stemming;
        return this;
    }

    @Override
    public EnglishAnalyzer enableFiltering(boolean filtering)
    {
        this.filtering = filtering;
        return this;
    }

    @Override
    public String toString()
    {
        return "English Language Analyzer";
    }

    @Override
    public TokenStreamComponents createComponents(String fieldName, Reader reader)
    {
        Tokenizer source = new StandardTokenizer(Version.LUCENE_46, reader);
        TokenStream filter = new StandardFilter(Version.LUCENE_46, source); // First filter with a very standard filter/analyzer.
        filter = new StopFilter(Version.LUCENE_46, filter, new CharArraySet(Version.LUCENE_46, stopWords, false)); // Remove stop words.
        filter = stemming ? new EnglishMinimalStemFilter(filter) : filter; // Stem the words.
        filter = filtering ? new ASCIIFoldingFilter(filter) : filter; // Filter the words.
        filter = new LowerCaseFilter(Version.LUCENE_46, filter); // Always to lowercase.

        return new TokenStreamComponents(source, filter);
    }
}
