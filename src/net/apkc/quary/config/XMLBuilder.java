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
package net.apkc.quary.config;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import net.apkc.esxp.exceptions.AttributeNotFoundException;
import net.apkc.esxp.exceptions.ParserNotInitializedException;
import net.apkc.esxp.exceptions.TagNotFoundException;
import net.apkc.quary.definitions.index.IndexDefinition;
import net.apkc.quary.definitions.index.IndexDefinitionDB;
import net.apkc.quary.docs.QuaryDocument;
import net.apkc.quary.util.Constants;
import net.apkc.quary.util.Timer;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

public final class XMLBuilder
{

    private static final Logger LOG = Logger.getLogger(XMLBuilder.class.getName());
    private static XMLProcessor processor = XMLProcessor.getInstance();

    private XMLBuilder()
    {
    }

    /**
     * Creates a new Quary document based on information passed as an XML document.
     * To index a document into Quary, it must be passed on as an XML file, and it
     * must match a previously defined definition. If it's matched then it will be
     * indexed where the definition says so, if not an exception is thrown and an
     * error response is returned to the user.
     *
     * @param xml The XML file containing the data to be indexed.
     *
     * @return The XML message
     */
    public static QuaryDocument parseExternalDocumentToQuaryDocument(String xml)
    {
        Timer timer = new Timer();
        timer.starTimer();

        // Re-start the parser and point to root node.
        processor = processor.configure(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), null, "root");

        try {
            // Look in the IndexDefinitionDB for a match. For now use the *definitionID* to look for a match.
            IndexDefinition definition = IndexDefinitionDB.getInstance().getDefinition(processor.getTagAttribute("root", "root", "definitionID"));

            // Match the XML file to the definition.
            QuaryDocument doc = processor.buildQuaryDocument(definition).setSignature(DigestUtils.sha512Hex(xml)).setDefintionID(definition.getDefinitionID());

            timer.endTimer();

            if (LOG.isInfoEnabled()) {
                LOG.info("Tiempo Unmarshall: " + timer.computeOperationTime(Timer.Time.MILLISECOND) + "ms");
            }

            return doc;
        }
        catch (ParserNotInitializedException | TagNotFoundException | AttributeNotFoundException e) {
            LOG.fatal("Error procesando XML. Error: " + e.toString(), e);
            return QuaryDocument.newBuild();
        }
    }

    /**
     * Creates a new definition object using data from an XML document.
     *
     * @param xml The XML document.
     *
     * @return A definition object.
     */
    public static IndexDefinition parseDefinitionFile(InputStream xml)
    {
        Timer timer = new Timer();
        timer.starTimer();

        // Re-start the parser and point to root node.
        processor = processor.configure(xml, XMLBuilder.class.getResourceAsStream(Constants.XSD_SCHEMA_FILE.getStringConstant()), "fields");

        try {
            IndexDefinition definition = IndexDefinition
                    .newBuild()
                    .setDefinitionID(processor.getTagAttribute("fields", "fields", "definitionID"))
                    .setScoreCoeficient(processor.getTagAttribute("fields", "fields", "scoreCoeficient"))
                    .setFields(processor.getFields());

            timer.endTimer();

            if (LOG.isInfoEnabled()) {
                LOG.info("Tiempo Unmarshall: " + timer.computeOperationTime(Timer.Time.MILLISECOND) + "ms");
            }

            return definition;
        }
        catch (ParserNotInitializedException | TagNotFoundException | AttributeNotFoundException e) {
            LOG.fatal("Error procesando XML. Error: " + e.toString(), e);
            return IndexDefinition.newBuild();
        }
    }
}
