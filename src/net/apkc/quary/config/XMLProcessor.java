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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import net.apkc.esxp.exceptions.AttributeNotFoundException;
import net.apkc.esxp.exceptions.ParserNotInitializedException;
import net.apkc.esxp.exceptions.TagNotFoundException;
import net.apkc.esxp.processor.Processor;
import net.apkc.esxp.walker.DOMWalker;
import net.apkc.esxp.walker.DOMWalkerFactory;
import net.apkc.quary.definitions.index.IndexDefinition;
import net.apkc.quary.definitions.index.IndexDefinitionField;
import net.apkc.quary.docs.QuaryDocument;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Custom XML processor for the application.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @see Singleton Pattern
 */
class XMLProcessor
{

    private static final Logger LOG = Logger.getLogger(XMLProcessor.class.getName());
    private static final XMLProcessor _INSTANCE = new XMLProcessor();
    private final byte WALKER = DOMWalkerFactory.STACK_DOM_WALKER;
    private final boolean STRICT_MODE = false;
    private Processor processor = Processor.newBuild();
    private Document doc;
    private NodeList nodes;

    static XMLProcessor getInstance()
    {
        return _INSTANCE;
    }

    private XMLProcessor()
    {
    }

    /**
     * Configure this XML processor.
     *
     * @param xml          The XML document to parse.
     * @param schemaString The schema used to validate the XML, if null skip.
     * @param rootNode     The root node of the XML.
     *
     * @return This instance.
     */
    XMLProcessor configure(InputStream xmlStream, InputStream schemaStream, String rootNode)
    {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

            if (schemaStream != null) {
                // Validate the XML file againts our default schema.
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = factory.newSchema(new StreamSource(schemaStream));
                dbFactory.setSchema(schema);
            }

            // Configure to Focus on Content.
            dbFactory.setValidating(false);
            dbFactory.setNamespaceAware(true);
            dbFactory.setCoalescing(true);
            dbFactory.setExpandEntityReferences(true);
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);

            // Create a DOM document.
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler()
            {
                @Override
                public void warning(SAXParseException e) throws SAXException
                {
                    LOG.warn("DOM Warning: " + e.toString(), e);
                }

                @Override
                public void error(SAXParseException e) throws SAXException
                {
                    LOG.error("DOM Error: " + e.toString(), e);
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException
                {
                    LOG.fatal("DOM Fatal: " + e.toString(), e);
                    throw e;
                }
            });

            doc = builder.parse(new InputSource(xmlStream)); // Create document
            doc.getDocumentElement().normalize(); // Configure
            nodes = doc.getElementsByTagName(rootNode);
        }
        catch (ParserConfigurationException | SAXException | IOException ex) {
            System.err.println("Error configuring processor. Error: " + ex.toString());
            System.exit(1);
        }

        return this;
    }

    /**
     * Method for extracting all declared fields inside a definition document.
     *
     * @return A list containing definition objects.
     *
     * @throws ParserNotInitializedException If the processor could not be started.
     */
    List<IndexDefinitionField> getFields() throws ParserNotInitializedException
    {
        if (nodes == null) {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        List<IndexDefinitionField> list = new ArrayList<>();
        IndexDefinitionField df = IndexDefinitionField.newBuild();

        try {
            DOMWalker mainParser = DOMWalkerFactory.getWalker(WALKER).configure(nodes.item(0), DOMWalker.ELEMENT_NODES);
            while (mainParser.hasNext()) {
                Node n1 = mainParser.nextNode();
                switch (n1.getNodeName()) {
                    // If its a node with sub-nodes.
                    case "field": // Node with sub-nodes.
                        DOMWalker subParser1 = DOMWalkerFactory.getWalker(WALKER).configure(n1, DOMWalker.ELEMENT_NODES);
                        while (subParser1.hasNext()) {
                            Node n2 = subParser1.nextNode();
                            switch (n2.getNodeName()) {
                                case "analyzer":
                                    df.setAnalyzer(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "boost":
                                    df.setFieldBoost(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "docvaluetype":
                                    df.setDocValueType(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "indexed":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.INDEXED, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "indexoptions":
                                    df.setIndexOptions(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "numericprecisionstep":
                                    df.setNumericPrecisionStep(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "numerictype":
                                    df.setNumericType(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "omitnorms":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.OMIT_NORMS, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "stored":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.STORED, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "storetermvectoroffset":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_OFFSETS, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "storetermvectorpayloads":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_PAYLOADS, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "storetermvectorpositions":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTOR_POSITIONS, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "storetermvectors":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.STORE_TERM_VECTORS, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "tokenized":
                                    df.addFieldProperty(IndexDefinitionField.OptionID.TOKENIZED, processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                                case "value":
                                    df.setFieldName(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "contentencoding":
                                    df.setContentEncoding(processor.getNodeValue(n2, STRICT_MODE));
                                    break;
                                case "searchable":
                                    df.setSearchable(processor.getNodeValue(n2, STRICT_MODE).equals("1"));
                                    break;
                            }
                        }
                        break;
                }

                if (!df.isEmpty()) {
                    list.add(df);
                    df = IndexDefinitionField.newBuild();
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error parsing DOM tree. Error: " + e.toString(), e);
        }

        return list;
    }

    QuaryDocument buildQuaryDocument(IndexDefinition definition) throws ParserNotInitializedException
    {
        if (nodes == null) {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        QuaryDocument quaryDoc = QuaryDocument.newBuild();

        try {
            DOMWalker mainParser = DOMWalkerFactory.getWalker(WALKER).configure(nodes.item(0), DOMWalker.ELEMENT_NODES);
            while (mainParser.hasNext()) {
                Node n1 = mainParser.nextNode();
                for (IndexDefinitionField field : definition.getFields()) {
                    if (field.getFieldName().equalsIgnoreCase(n1.getNodeName())) { // We found a match. Load the field into the document!
                        quaryDoc.add(field.getFieldName(), processor.getNodeValue(n1, STRICT_MODE));
                    }
                }
            }
        }
        catch (Exception e) {
            LOG.error("Error parsing DOM tree. Error: " + e.toString(), e);
        }

        return quaryDoc;
    }

    /**
     * Shortcut method to extract the value of a tag.
     *
     * @param rootNode The root node.
     * @param tag      The name of the tag to extract.
     *
     * @return The value of the tag.
     *
     * @throws ParserNotInitializedException If the processor could not be started.
     * @throws TagNotFoundException          If the tag does not exists.
     */
    String getTagValue(String rootNode, String tag) throws ParserNotInitializedException, TagNotFoundException
    {
        if (nodes == null) {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        return processor.searchTagValue(doc, rootNode, tag, STRICT_MODE);
    }

    /**
     * Shortcut method to extract the attribute of a tag.
     *
     * @param rootNode      The root node.
     * @param tag           The name of the tag.
     * @param attributeName The name of the attribute.
     *
     * @return The value of the attribute.
     *
     * @throws ParserNotInitializedException If the processor could not be started.
     * @throws TagNotFoundException          If the tag does not exists.
     * @throws AttributeNotFoundException    If the attribute does not exists.
     */
    String getTagAttribute(String rootNode, String tag, String attributeName) throws ParserNotInitializedException, TagNotFoundException, AttributeNotFoundException
    {
        if (nodes == null) {
            throw new ParserNotInitializedException("Parser was not started!");
        }

        return processor.searchTagAttributeValue(doc, rootNode, tag, attributeName, STRICT_MODE);
    }
}
