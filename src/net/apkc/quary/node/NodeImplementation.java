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
package net.apkc.quary.node;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import net.apkc.quary.analyzers.EnglishAnalyzer;
import net.apkc.quary.analyzers.QuaryAnalyzer;
import net.apkc.quary.definitions.index.IndexDefinition;
import net.apkc.quary.definitions.index.IndexDefinitionField;
import net.apkc.quary.docs.QuaryDocument;
import net.apkc.quary.filters.DateRangeFilter;
import net.apkc.quary.filters.QuaryFilter;
import net.apkc.quary.filters.QuaryFilters;
import net.apkc.quary.reactor.Parameters;
import net.apkc.quary.reactor.Result;
import net.apkc.quary.reactor.Searcher;
import net.apkc.quary.util.Constants;
import net.apkc.quary.util.GeneralUtilities;
import net.apkc.quary.util.Normalizer;
import net.apkc.quary.util.QuaryConfiguration;
import net.apkc.quary.util.Rank;
import net.apkc.quary.util.Timer;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.ProtocolSignature;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

class NodeImplementation implements NodeInterface
{

    private static final Logger LOG = Logger.getLogger(NodeImplementation.class.getName());
    private static final Configuration CONF = new QuaryConfiguration().create();
    private static final int MAX_URL_LENGTH = 80;
    private static final List<IndexSearcher> SEARCHERS = Collections.synchronizedList(new ArrayList<IndexSearcher>(0));
    private static volatile IndexWriter writer = null; // This object should be accesed from within the entire package.

    @Override
    public void shutdown()
    {
        if (LOG.isInfoEnabled()) {
            LOG.info("Shutting down the IndexServer in 10 seconds.");
        }

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try {
                    sleep(10000);
                }
                catch (InterruptedException ex) {
                }

                System.exit(0);
            }
        };
        t.start();
    }

    @Override
    public int close(String definitionID, Node node)
    {
        try {
            if (writer != null) {
                writer.close(); // Close the writer.
                writer = null; // Allow for GC to recall this object.
            }

            // The index has changed, so update the readers.
            if (areSearchersOpen(true, definitionID, node)) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Index changed: All readers where updated.");
                }
            }

            return 0;
        }
        catch (IOException e) {
            LOG.fatal("Error closing IndexServer indexer. Error: " + e.toString(), e);
            return -1;
        }
    }

    @Override
    public long version()
    {
        return versionID;
    }

    @Override
    public boolean isUp()
    {
        return true;
    }

    @Override
    public int openWriter(Configuration conf, String definitionID, Node node)
    {
        try {
            // Open an index writer to instance index.
            writer = new IndexWriter(
                    FSDirectory.open(new File(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID())),
                    new IndexWriterConfig(Version.LUCENE_46, EnglishAnalyzer.newBuild().enableFiltering(true).enableStemming(true))
                    .setUseCompoundFile(true)
                    .setSimilarity(new DefaultSimilarity()));

            return 0;
        }
        catch (IOException e) {
            LOG.fatal("Error opening IndexServer writer. Error: " + e.toString(), e);
            return -1;
        }
    }

    @Override
    public void write(Text key, QuaryDocument doc, IndexDefinition def, long elapsedTime)
    {
        DirectoryReader rdr = null;
        IndexSearcher scr;
        TreeMap<String, Field> fields = new TreeMap<>();
        try {
            Document newDoc = new Document();

            for (IndexDefinitionField f : def.getFields()) {
                FieldType type = new FieldType();
                type.setDocValueType(f.getDocValueType() == null ? null : FieldInfo.DocValuesType.valueOf(f.getDocValueType()));
                type.setIndexOptions(FieldInfo.IndexOptions.valueOf(f.getIndexOptions()));
                type.setIndexed(f.getFieldProperty(IndexDefinitionField.OptionID.INDEXED));
                type.setNumericPrecisionStep(f.getNumericPrecisionStep());
                type.setNumericType(f.getNumericType() == null ? null : FieldType.NumericType.valueOf(f.getNumericType()));
                type.setOmitNorms(f.getFieldProperty(IndexDefinitionField.OptionID.OMIT_NORMS));
                type.setStoreTermVectorOffsets(f.getFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_OFFSETS));
                type.setStoreTermVectorPayloads(f.getFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_PAYLOADS));
                type.setStoreTermVectorPositions(f.getFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_POSITIONS));
                type.setStoreTermVectors(f.getFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTORS));
                type.setStored(f.getFieldProperty(IndexDefinitionField.OptionID.STORED));
                type.setTokenized(f.getFieldProperty(IndexDefinitionField.OptionID.TOKENIZED));

                Field field = new Field(f.getFieldName(), doc.getFieldValue(f.getFieldName()), type);
                if (type.indexed()) {
                    field.setBoost(f.getFieldBoost());
                    field.tokenStream(QuaryAnalyzer.getAnalyzer(f.getAnalyzer()));
                }

                fields.put(f.getFieldName(), field);
            }

            // Add all fields to document.
            fields.entrySet().stream().forEach((e) -> {
                newDoc.add(e.getValue());
            });

            // Force the signature.
            FieldType type = new FieldType();
            type.setIndexed(true);
            type.setStored(true);
            type.setTokenized(false);
            newDoc.add(new Field("signature", doc.getSignature(), type));

            rdr = DirectoryReader.open(writer, true); // Always return an NRT reader. Dispose it at the end.
            scr = new IndexSearcher(rdr);

            // If it exists then return the document that matches this URL. Documents are always unique in their URLs.
            // No two exact URLs can exists in the index at the same time.
            Query uniqueID = new TermQuery(new Term("signature", doc.getSignature()));
            TopDocs uniqueIDDoc = scr.search(uniqueID, 1);

            // NEW DOCUMENT
            if (uniqueIDDoc.totalHits == 0) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Indexing: [" + GeneralUtilities.trimURL(newDoc.get("url"), MAX_URL_LENGTH) + "] " + "Hash: [" + MD5Hash.digest(newDoc.get("url")).toString() + "]");
                }

                // Add gravity to boost.
                float boostWithoutGravity = Float.parseFloat(newDoc.get("boost"));
                double gravity = Rank.calculateGravity(String.valueOf(System.currentTimeMillis() - Long.parseLong(newDoc.get("indextime"))));
                fields.get("boostwithgravity").setStringValue(String.valueOf(Normalizer.normalize(boostWithoutGravity / gravity) * 10));

                // Save gravity field, in the same format as the SocialCoefficient. [12 seconds ago] - 0.9999919691519064
                fields.get("gravity").setStringValue(
                        "[" + GeneralUtilities.computeOperationTime(Long.parseLong(newDoc.get("indextime")), System.currentTimeMillis(), "s", true) + " seconds ago]"
                        + "-"
                        + String.valueOf(gravity));

                // Save indexable time fields.
                // Note: DateTools saves dates in GMT time. We must substract -4 for PY.
                fields.get("itsecond").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.SECOND));
                fields.get("itminute").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.MINUTE));
                fields.get("ithour").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.HOUR));
                fields.get("itday").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.DAY));
                fields.get("itmonth").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.MONTH));
                fields.get("ityear").setStringValue(DateTools.timeToString(Long.valueOf(newDoc.get("indextime")), DateTools.Resolution.YEAR));

                // Re-Add all fields to document.
                fields.entrySet().stream().forEach((e) -> {
                    newDoc.add(e.getValue());
                });

                // Write the new document.
                writer.addDocument(newDoc);
                writer.commit();
            }
            // EXISTING DOCUMENT
            else {
                ScoreDoc[] hits = uniqueIDDoc.scoreDocs;
                for (ScoreDoc hit1 : hits) {
                    int docId = hit1.doc;
                    Document kernelIndexExistingDocument = scr.doc(docId);

                    // IMPORTANT!!! The index date should never be updated, since it reflects the exact moment that the doc was first indexed.
                    // Save original index date.
                    String originalIndexDate = kernelIndexExistingDocument.get("indextime");

                    // Update the doc with the original date.
                    fields.get("indextime").setStringValue(originalIndexDate);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Updating: [" + GeneralUtilities.trimURL(newDoc.get("url"), MAX_URL_LENGTH) + "] " + "Hash: [" + MD5Hash.digest(newDoc.get("url")).toString() + "]");
                    }
                    if (!kernelIndexExistingDocument.get("digest").equalsIgnoreCase(newDoc.get("digest")) && LOG.isDebugEnabled()) {
                        LOG.debug("Signatures don't match!");
                    }
                    if (!kernelIndexExistingDocument.get("segment").equalsIgnoreCase(newDoc.get("segment")) && LOG.isDebugEnabled()) {
                        LOG.debug("Segments don't match!");
                    }

                    // Check the old score against the new one. If new > old then all is correct and perform notification to console. Else if
                    // old > new then probably something wrong has occur and a notification via email is to be sent.
                    if (Float.parseFloat(kernelIndexExistingDocument.get("boost")) < Float.parseFloat(newDoc.get("boost")) && LOG.isDebugEnabled()) {
                        LOG.debug("Scores don't match!");
                        LOG.debug("Previous: " + kernelIndexExistingDocument.get("boost"));
                        LOG.debug("Current: " + newDoc.get("boost"));
                    }
                    else if (Float.parseFloat(kernelIndexExistingDocument.get("boost")) > Float.parseFloat(newDoc.get("boost"))) {
                        // Nothing to do here...
                    }

                    // Delete the old document.
                    writer.deleteDocuments(new Term("signature", doc.getSignature())); // Borrar el documento viejo.

                    // Add gravity to boost.
                    float boostWithoutGravity = Float.parseFloat(newDoc.get("boost"));
                    double gravity = Rank.calculateGravity(String.valueOf(System.currentTimeMillis() - Long.parseLong(originalIndexDate)));
                    fields.get("boostwithgravity").setStringValue(String.valueOf(Normalizer.normalize(boostWithoutGravity / gravity) * 10));

                    // Save gravity field, in the same format as the SocialCoefficient. [12 seconds ago] - 0.9999919691519064
                    fields.get("gravity").setStringValue(
                            "[" + GeneralUtilities.computeOperationTime(Long.parseLong(originalIndexDate), System.currentTimeMillis(), "s", true) + " seconds ago]"
                            + "-"
                            + String.valueOf(gravity));

                    // Save indexable time fields.
                    // Note: DateTools saves dates in GMT time. We must substract -4 for PY.
                    fields.get("itsecond").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.SECOND));
                    fields.get("itminute").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.MINUTE));
                    fields.get("ithour").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.HOUR));
                    fields.get("itday").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.DAY));
                    fields.get("itmonth").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.MONTH));
                    fields.get("ityear").setStringValue(DateTools.timeToString(Long.valueOf(originalIndexDate), DateTools.Resolution.YEAR));

                    // Re-Add all fields to document.
                    fields.entrySet().stream().forEach((e) -> {
                        newDoc.add(e.getValue());
                    });

                    // Write the new document.
                    writer.addDocument(newDoc);
                    writer.commit();
                }
            }
        }
        catch (IOException | NumberFormatException e) {
            LOG.error("Error adding new document to IndexServer's index. Error: " + e.toString(), e);
        }
        finally {
            try {
                // Always close the searcher, and reader.
                if (rdr != null) {
                    rdr.close();
                }
            }
            catch (IOException e) {
                LOG.error("Error closing down IndexServer's readers after adding new document. Error: " + e.toString(), e);
            }
        }
    }

    @Override
    public Text search(Configuration conf, IndexDefinition def, Node node, Parameters params)
    {
        StringBuilder xml = new StringBuilder();
        Query query;
        TopScoreDocCollector collector;
        Timer timer = new Timer();
        timer.starTimer();
        Searcher searcher;
        // Weights
        HashMap<String, Float> boosts = new HashMap<>();
        // Analyzer
        Map<String, Analyzer> analyzerPerField = new HashMap<>();
        // Searchable fields.
        List<String> sf = new ArrayList<>();

        try {
            searcher = getOpenSearcher(def.getDefinitionID(), node);
            if (!searcher.getIsOpen()) {
                xml.append(Result.dummyResult(timer.computeOperationTime(Timer.Time.SECOND)));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("No open searchers are available for searching.");
                }

                return new Text(xml.toString());
            }

            searcher.getSearcher().setSimilarity(new DefaultSimilarity());

            for (IndexDefinitionField f : def.getFields()) {
                boosts.put(f.getFieldName(), f.getFieldBoost());
                analyzerPerField.put(f.getFieldName(), QuaryAnalyzer.getAnalyzer(f.getAnalyzer()));
                if (f.getSearchable()) {
                    sf.add(f.getFieldName());
                }
            }

            PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(EnglishAnalyzer.newBuild(), analyzerPerField); // #TODO: Detect language.

            // Build the query.
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_46, sf.toArray(new String[0]), analyzer, boosts);
            query = queryParser.parse(params.getQueryString());
            if (LOG.isDebugEnabled()) {
                LOG.debug("Query: " + query.toString());
            }

            // Request N documents from the index, but offsets are available, so pagination is enabled.
            // Use the endOffset in this case as the results quantity, otherwise it doesn't work.
            collector = TopScoreDocCollector.create(params.getEndOffset(), true);

            // Set up the query filters.
            List<Filter> fltr = new ArrayList<>(0);
            for (QuaryFilter filter : params.getFilters().getFilters()) {
                switch (filter.getType()) {
                    case QuaryFilters.DATE_RANGE_FILTER:
                        // Create the number range filter for timestamps.
                        long now = System.currentTimeMillis();
                        long second = 1000L;
                        long minute = 60000L;
                        long hour = 3600000L;
                        long day = 86400000L;
                        long month = 2592000000L;
                        long year = 31104000000L;
                        BytesRef lowerTimestamp = null;
                        BytesRef upperTimestamp = null;
                        Filter rangeFilter = null;

                        switch (filter.getDateRange()) {
                            case DateRangeFilter.SECOND_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (second * filter.getTimeFactor()), DateTools.Resolution.SECOND));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.SECOND));
                                rangeFilter = new TermRangeFilter("itsecond", lowerTimestamp, upperTimestamp, true, true);
                                break;
                            case DateRangeFilter.MINUTE_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (minute * filter.getTimeFactor()), DateTools.Resolution.MINUTE));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.MINUTE));
                                rangeFilter = new TermRangeFilter("itminute", lowerTimestamp, upperTimestamp, true, true);
                                break;
                            case DateRangeFilter.HOUR_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (hour * filter.getTimeFactor()), DateTools.Resolution.HOUR));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.HOUR));
                                rangeFilter = new TermRangeFilter("ithour", lowerTimestamp, upperTimestamp, true, true);
                                break;
                            case DateRangeFilter.DAY_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (day * filter.getTimeFactor()), DateTools.Resolution.DAY));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.DAY));
                                rangeFilter = new TermRangeFilter("itday", lowerTimestamp, upperTimestamp, true, true);
                                break;
                            case DateRangeFilter.MONTH_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (month * filter.getTimeFactor()), DateTools.Resolution.MONTH));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.MONTH));
                                rangeFilter = new TermRangeFilter("itmonth", lowerTimestamp, upperTimestamp, true, true);
                                break;
                            case DateRangeFilter.YEAR_RANGE:
                                lowerTimestamp = new BytesRef(DateTools.timeToString(now - (year * filter.getTimeFactor()), DateTools.Resolution.YEAR));
                                upperTimestamp = new BytesRef(DateTools.timeToString(now, DateTools.Resolution.YEAR));
                                rangeFilter = new TermRangeFilter("ityear", lowerTimestamp, upperTimestamp, true, true);
                                break;
                        }

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Lower Timestamp: " + lowerTimestamp);
                            LOG.debug("Upper Timestamp: " + upperTimestamp);
                            LOG.debug("Now - 6 months: " + (now - (month * 6)));
                            LOG.debug(" Now: " + now);
                            LOG.debug(" 6 months back: " + (month * 6));
                        }

                        fltr.add(rangeFilter);
                        break;
                    case QuaryFilters.TERM_FILTER:
                        TermsFilter termFilter = new TermsFilter(new Term(filter.getField(), filter.getTerm()));
                        fltr.add(termFilter);
                        break;
                }
            }

            // Search the index.
            if (fltr.size() > 0) {
                searcher.getSearcher().search(query, new ChainedFilter(fltr.toArray(new Filter[fltr.size()]), ChainedFilter.AND), collector);
            }
            else {
                searcher.getSearcher().search(query, collector);
            }
            // If the collector is null, then the search has not been done well. Return a dummy result.
            if (collector == null) {
                xml.append(Result.dummyResult(timer.computeOperationTime(Timer.Time.SECOND)));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("There's been an error processing the query \"" + params.getQueryString() + "\". An empty result would be given.");
                }

                return new Text(xml.toString());
            }

            // Collect the results.
            ScoreDoc[] hits = collector.topDocs(params.getStartOffset(), params.getEndOffset() - params.getStartOffset()).scoreDocs;

            // Build the response.
            String res = Result.makeXMLResponse(hits, params, searcher, query, collector, timer);

            // Close the reader.
            if (searcher.getShouldClose()) {
                searcher.getSearcher().getIndexReader().close();
            }

            return new Text(res);
        }
        catch (IOException e) {
            LOG.fatal("Error querying IndexServer. Error: " + e.toString(), e);

            // Always return a dummy result if an exception as ocurred.
            xml.append(Result.dummyResult(timer.computeOperationTime(Timer.Time.SECOND)));

            return new Text(xml.toString());
        }
        catch (ParseException e) {
            // Fail silents this one.
            // Always return a dummy result if an exception as ocurred.
            xml.append(Result.dummyResult(timer.computeOperationTime(Timer.Time.SECOND)));

            if (LOG.isDebugEnabled()) {
                LOG.debug("There's been an error processing the query \"" + params.getQueryString() + "\". An empty result would be given.");
            }

            return new Text(xml.toString());
        }
    }

    @Override
    public boolean areSearchersOpen(boolean reOpenReaders, String definitionID, Node node)
    {
        // First try to open readers.
        openSearchers(reOpenReaders, definitionID, node);

        synchronized (SEARCHERS) {
            ListIterator<IndexSearcher> i = SEARCHERS.listIterator();
            while (i.hasNext()) {
                IndexSearcher e = i.next(); // Get the searcher.
                try {
                    if (e != null && e.getIndexReader().tryIncRef()) {
                        return true;
                    }
                }
                catch (Exception ex) {
                    LOG.error("Error checking if reader is open. Error: " + ex.toString());
                }
                finally {
                    try {
                        if (e != null) {
                            IndexReader ir = e.getIndexReader();
                            if (ir != null) {
                                ir.decRef();
                            }
                        }
                    }
                    catch (IOException ex) {
                        LOG.error("Error decrementing reader reference. Error: " + ex.toString());
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean cleanIndex(String definitionID, Node node)
    {
        if (writer != null) {
            return false;
        }

        if (GeneralUtilities.directoryExists(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID())) {
            if (!GeneralUtilities.deleteDirectoryContents(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID(), true)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public long getProtocolVersion(String protocol, long clientVersion) throws IOException
    {
        return versionID;
    }

    @Override
    public ProtocolSignature getProtocolSignature(String string, long l, int i) throws IOException
    {
        return new ProtocolSignature(versionID, null);
    }

    /**
     * Returns an IndexSearcher for this IndexServer instance.
     *
     * @return An IndexSearcher for the IndexServer.
     *
     * @throws IOException
     */
    protected static Searcher getOpenSearcher(String definitionID, Node node) throws IOException
    {
        if (writer == null) {
            openSearchers(false, definitionID, node);

            // Check if there is an index available.
            if (!DirectoryReader.indexExists(FSDirectory.open(new File(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID())))) {
                return Searcher
                        .newBuild()
                        .setIsOpen(false)
                        .setSearcher(null)
                        .setShouldClose(false)
                        .checkObject();
            }

            // If there is an index available.
            synchronized (SEARCHERS) {
                if (SEARCHERS.size() < 1) {
                    return null;
                }

                IndexSearcher e = SEARCHERS.get(RandomUtils.nextInt(SEARCHERS.size()));
                if (LOG.isInfoEnabled()) {
                    LOG.info("Reader use: Reader_" + RandomUtils.nextInt(SEARCHERS.size()) + " was asigned for dutty.");
                }

                return Searcher
                        .newBuild()
                        .setIsOpen(true)
                        .setSearcher(e)
                        .setShouldClose(false)
                        .checkObject();
            }
        }
        else {
            synchronized (writer) {
                // Check if there is an index available.
                if (!DirectoryReader.indexExists(FSDirectory.open(new File(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID())))) {
                    return Searcher
                            .newBuild()
                            .setIsOpen(false)
                            .setSearcher(null)
                            .setShouldClose(false)
                            .checkObject();
                }

                return Searcher
                        .newBuild()
                        .setIsOpen(true)
                        .setSearcher(new IndexSearcher(DirectoryReader.open(writer, true)))
                        .setShouldClose(true)
                        .checkObject();
            }
        }
    }

    /**
     * This method opens all the readers and searchers to the IndexServer.
     *
     * @param reOpenSearchers If TRUE we must re-open all readers and
     *                        searchers to the index, FALSE we leave them
     *                        alone.
     */
    static void openSearchers(boolean reOpenSearchers, String definitionID, Node node)
    {
        if (SEARCHERS.isEmpty() && !reOpenSearchers) {
            try {
                FSDirectory dir = FSDirectory.open(new File(Constants.INDEX_FILE.getStringConstant() + definitionID + "." + node.getNodeID()));

                // Only open the reader if there is an index.
                if (DirectoryReader.indexExists(dir)) {
                    int readersQt = CONF.getInt("node.readers", 10);
                    int counter = 1;
                    do {
                        SEARCHERS.add(new IndexSearcher(DirectoryReader.open(dir)));

                        if (LOG.isInfoEnabled()) {
                            LOG.info("Readers closed: New Reader_" + counter + " was opened.");
                        }

                        counter++;
                    } while (counter <= readersQt);
                }
            }
            catch (IOException e) {
                LOG.fatal("Impossible to open main reader to the kernel. Error: " + e.toString(), e);
            }
        }
        else if (!SEARCHERS.isEmpty() && reOpenSearchers) {
            try {
                // Iterate over all searchers and re-open.
                synchronized (SEARCHERS) {
                    ListIterator<IndexSearcher> i = SEARCHERS.listIterator();
                    while (i.hasNext()) {
                        IndexSearcher oldSearcher = i.next(); // Get the old searcher.
                        IndexSearcher newSearcher;
                        try (DirectoryReader oldReader = (DirectoryReader) oldSearcher.getIndexReader()) {
                            newSearcher = new IndexSearcher(DirectoryReader.openIfChanged(oldReader));
                        }

                        // Replace the reader.
                        i.set(newSearcher);

                        if (LOG.isInfoEnabled()) {
                            LOG.info("Readers re-opened: Reader was re-opened.");
                        }
                    }
                }
            }
            catch (IOException e) {
                LOG.fatal("Error trying to re-open reader. Error: " + e.toString(), e);
            }
        }
    }
}
