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

import io.aime.aimemisc.datamining.Block;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.apkc.quary.util.GeneralUtilities;
import net.apkc.quary.util.Timer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.ocpsoft.pretty.time.PrettyTime;

/**
 * Class for building a result for a given query performed by the user.
 *
 * @author K-Zen
 */
public class Result
{

    private static final Logger LOG = Logger.getLogger(Result.class.getName());
    private static PrettyTime pTime = new PrettyTime(new Locale("en"));

    /**
     * Builds an entire XML response to a search. It also has support for document de-duplication.
     *
     * @param hits      The hits founded
     * @param params    The search parameters
     * @param searcher  The searcher object used to make the search
     * @param query     The query object entered by the user
     * @param collector The collector of results
     * @param timer     The search timer
     *
     * @return A XML formatted string with the response
     *
     * @throws IOException
     */
    public static String makeXMLResponse(ScoreDoc[] hits, Parameters params, Searcher searcher, Query query, TopScoreDocCollector collector, Timer timer) throws IOException
    {
        StringBuilder res = new StringBuilder();
        Set<Long> digestDB = new HashSet<>(); // Contains all documents digest signature for this search.

        res.append("<?xml version=\"1.0\"?>");
        res.append("<results>");

        if (params.getUseDeduplication()) {
            for (ScoreDoc hit : hits) {
                String digest = searcher.getSearcher().doc(hit.doc).get("digest");
                digestDB.add(Long.parseLong(digest));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Load " + digest + " into DigestDB.");
                }
            }
        }

        outerLoop:
        for (ScoreDoc hit : hits) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Explanation:");
                LOG.debug(searcher.getSearcher().explain(query, hit.doc).toString());
            }
            // Real-time digest de-duplication:
            if (params.getUseDeduplication()) {
                long documentDigest = Long.parseLong(searcher.getSearcher().doc(hit.doc).get("digest"));
                long maxHammingDistance = (long) Math.pow(2.0d, (double) params.getMaxHammingDistance());
                for (long dgs : digestDB) {
                    if (dgs == documentDigest) {
                        break;
                    }

                    if (Math.abs(dgs - documentDigest) < maxHammingDistance) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Duplicate detected between " + dgs + " AND " + documentDigest + ".");
                        }

                        continue outerLoop;
                    }
                }
            }
            res.append(addXMLEntry(searcher.getSearcher().doc(hit.doc), params.getQueryString(), params.getUseFormattedDate()));
        }

        // Count the distincts filetypes.
        // i.e. application/pdf => 201
        //      text/html => 406
        res.append("<filetypes>");
        Map<String, Integer> filetypes = new HashMap<>();
        for (ScoreDoc hit : hits) {
            Document doc = searcher.getSearcher().doc(hit.doc);
            String t = doc.get("filetype");
            if (!filetypes.containsKey(t)) {
                filetypes.put(t, 1);
            }
            else {
                filetypes.put(t, filetypes.get(t) + 1);
            }
        }

        Iterator<Map.Entry<String, Integer>> i = filetypes.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<String, Integer> e = i.next();
            String ext = e.getKey().replaceAll("\\/", "-");
            res.append("<").append(ext).append(">").append(e.getValue()).append("</").append(ext).append(">");
        }
        res.append("</filetypes>");
        res.append("<totalhits>").append(collector.getTotalHits()).append("</totalhits>");
        // Mark end of processing.
        timer.endTimer();
        res.append("  <searchtime>").append(timer.computeOperationTime(Timer.Time.SECOND)).append("</searchtime>");
        res.append("</results>");

        return res.toString();
    }

    /**
     * This method construct a new entry in the XML response. An XML entry
     * corresponds to an individual result.
     *
     * @param doc         The Lucene document from which to construct the XML entry.
     * @param queryPhrase The query phrase.
     * @param formatDate  TRUE if the dates should be formatted using the
     *                    PrettyTime library, FALSE we should return the raw
     *                    timestamp in milliseconds.
     *
     * @return A String containing the XML entry.
     */
    public static String addXMLEntry(Document doc, String queryPhrase, boolean formatDate)
    {
        // Make the entry
        ResultEntry entry = ResultEntry
                .newBuild()
                .setType(ResultEntry.XML)
                .setBoost(doc.get("boost"))
                .setBoostGravity(doc.get("boostwithgravity"))
                .setContentBlocks(getContentBlocks(doc.get("segment"), doc.get("url")))
                .setContentLength(doc.get("contentlength"))
                .setDigest(doc.get("digest"))
                .setExtension(doc.get("filetype"))
                .setFetchTime(processDate(formatDate, doc.get("fetchtime")))
                .setFileType(doc.get("filetype"))
                .setGravity(doc.get("gravity"))
                .setIndexTime(processDate(formatDate, doc.get("indextime")))
                .setLanguage(doc.get("lang"))
                .setLastModified(doc.get("lastmodified"))
                .setSegment(doc.get("segment"))
                .setSummary(getSummary(doc.get("segment"), doc.get("url"), new Text(queryPhrase), new Text(doc.get("lang"))).toString())
                .setTitle(doc.get("title"))
                .setURL(doc.get("url"));

        // Prepare XML
        StringBuilder xmlEntry = new StringBuilder();
        xmlEntry.append("<result>");
        xmlEntry.append(entry.getBoost());
        xmlEntry.append(entry.getBoostGravity());
        xmlEntry.append(entry.getContentBlocks());
        xmlEntry.append(entry.getContentLength());
        xmlEntry.append(entry.getDigest());
        xmlEntry.append(entry.getExtension());
        xmlEntry.append(entry.getFetchTime());
        xmlEntry.append(entry.getFileType());
        xmlEntry.append(entry.getGravity());
        xmlEntry.append(entry.getIndexTime());
        xmlEntry.append(entry.getLanguage());
        xmlEntry.append(entry.getLastModified());
        xmlEntry.append(entry.getSegment());
        xmlEntry.append(entry.getSummary());
        xmlEntry.append(entry.getTitle());
        xmlEntry.append(entry.getURL());
        xmlEntry.append("</result>");

        return xmlEntry.toString();
    }

    /**
     * Returns an empty/dummy result, which can be used in the calling class to
     * not throw a NullPointerException.
     *
     * @param time The computation time.
     *
     * @return A string containing the XML result.
     */
    public static String dummyResult(double time)
    {
        StringBuilder res = new StringBuilder();
        res.append("<?xml version=\"1.0\"?>\n");
        res.append("<results>\n");
        res.append("  <totalhits>0</totalhits>\n");
        res.append("  <searchtime>").append(time).append("</searchtime>\n");
        res.append("</results>");

        return res.toString();
    }

    /**
     * Returns an empty/dummy result, which can be used in the calling class to
     * not throw a NullPointerException.
     *
     * @param message The error message.
     *
     * @return A string containing the XML result.
     */
    public static String errorResult(String message)
    {
        StringBuilder res = new StringBuilder();
        res.append("<?xml version=\"1.0\"?>\n");
        res.append("<results>\n");
        res.append("  <totalhits>0</totalhits>\n");
        res.append("  <searchtime>0.00</searchtime>\n");
        res.append("  <errormessage>").append(message).append("</errormessage>\n");
        res.append("</results>");

        return res.toString();
    }

    /**
     * This method prepares a given text/string for putting into an XML
     * response.
     *
     * @param text The text to prepare.
     *
     * @return The prepared text.
     */
    public static String forXML(String text)
    {
        return StringEscapeUtils.escapeXml(GeneralUtilities.stripNonValidXMLCharacters(text));
    }

    /**
     * This method contacts a SegmentServer instance and returns the summary
     * for a specific document.
     *
     * @param segment     The name of the segment containing the entry (URL).
     * @param url         The URL of the document (entry).
     * @param queryPhrase The query entered by the user.
     * @param language    The language of the document.
     *
     * @return The summary of the document.
     */
    public static String getSummary(String segment, String url, Text queryPhrase, Text language)
    {
        Timer t = new Timer();
        t.starTimer();
        // TODO: Connect to Redis.
        t.endTimer();

        if (LOG.isDebugEnabled()) {
            LOG.debug("IndexServer getParseText() retrieve time: " + url + " [" + t.computeOperationTime(Timer.Time.MILLISECOND) + "ms]");
        }

        return "Dummy summary! Do more work and finish this function!";
    }

    /**
     * This method contacts a SegmentServer instance and returns the content blocks
     * for a specific document.
     *
     * @param segment The name of the segment containing the entry (URL).
     * @param url     The URL of the document (entry).
     *
     * @return An array of content blocks.
     */
    public static Block[] getContentBlocks(String segment, String url)
    {
        Timer t = new Timer();
        t.starTimer();
        // TODO: Connect to Redis.
        t.endTimer();

        if (LOG.isDebugEnabled()) {
            LOG.debug("IndexServer getParseText() retrieve time: " + url + " [" + t.computeOperationTime(Timer.Time.MILLISECOND) + "ms]");
        }

        return new Block[0];
    }

    /**
     * Transforms a date string into a more friendly form.
     *
     * @param formatDate TRUE if the date should be formatted, FALSE otherwise.
     * @param date       The date string to be formatted.
     *
     * @return The formatted date.
     */
    public static String processDate(boolean formatDate, String date)
    {
        try {
            if (formatDate) {
                date = pTime.format(DateTools.stringToDate(DateTools.timeToString(Long.valueOf(date), DateTools.Resolution.MILLISECOND)));
            }
        }
        catch (ParseException ex) {
            LOG.error("Impossible to parse the \"timestamp\" field of the kernel's index. Default value will be used. Error: " + ex.toString(), ex);
        }

        return date;
    }
}
