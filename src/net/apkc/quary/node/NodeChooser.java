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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.apkc.quary.exceptions.InvalidDocumentMapperException;
import net.apkc.quary.exceptions.InvalidIndexException;
import net.apkc.quary.exceptions.ZeroNodesException;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.log4j.Logger;

/**
 * The *Node Chooser* contains algorithms for choosing in what node a document belongs.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @see Singleton Pattern
 */
public final class NodeChooser
{

    private static final Logger LOG = Logger.getLogger(NodeChooser.class.getName());
    private final List<List<Node>> DICTIONARY = new ArrayList<>(16);
    private final List<Node> NODES = new ArrayList<>();
    private Integer lastNode = 0;
    private static final NodeChooser INSTANCE = new NodeChooser();

    /**
     * Private default constructor.
     */
    private NodeChooser()
    {
        resetDictionary();
    }

    /**
     * Returns the only instance of this class.
     *
     * @return The only instance of class.
     */
    public static final NodeChooser getInstance()
    {
        return INSTANCE;
    }

    /**
     * Reset the dictionary to its original settings. Each time a new node has been
     * added, the dictionary must be recomputed.
     */
    private void resetDictionary()
    {
        DICTIONARY.clear();

        for (int k = 0; k < 16; k++) {
            DICTIONARY.add(k, new ArrayList<>());
        }
    }

    /**
     * This method calculates the index in the dictionary of a given Document Mapper.
     *
     * @param documentMapper The Document Mapper.
     *
     * @return The position in the dictionary.
     *
     * @throws InvalidDocumentMapperException If the Document Mapper is invalid.
     */
    public int calculateDictionaryIndex(int documentMapper) throws InvalidDocumentMapperException
    {
        return Math.abs((((getIndexFromDocumentMapper(documentMapper) + 1) % lastNode) - 16) % 16);
    }

    /**
     * This method will map a character (Document Mapper) to a given index in the dictionary.
     *
     * @param documentMapper The Document Mapper (The Index).
     *
     * @return The dictionary index that corresponds to the Document Mapper.
     *
     * @throws InvalidDocumentMapperException If the Document Mapper is not valid.
     */
    public int getIndexFromDocumentMapper(int documentMapper) throws InvalidDocumentMapperException
    {
        switch (documentMapper) {
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
                return Integer.parseInt(String.valueOf((char) documentMapper));
            case 65:
            case 97:
                return 10;
            case 66:
            case 98:
                return 11;
            case 67:
            case 99:
                return 12;
            case 68:
            case 100:
                return 13;
            case 69:
            case 101:
                return 14;
            case 70:
            case 102:
                return 15;
            default:
                throw new InvalidDocumentMapperException("Document Mapper not valid!");
        }
    }

    /**
     * This method will map an index in dictionary to a given Document Mapper.
     *
     * @param index The index.
     *
     * @return The Document Mapper that corresponds to that index.
     *
     * @throws InvalidIndexException If the index is not valid.
     */
    public int getDocumentMapperFromIndex(int index) throws InvalidIndexException
    {
        switch (index) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return String.valueOf(index).charAt(0);
            case 10:
                return 97;
            case 11:
                return 98;
            case 12:
                return 99;
            case 13:
                return 100;
            case 14:
                return 101;
            case 15:
                return 102;
            default:
                throw new InvalidIndexException("Index not valid!");
        }
    }

    /**
     * This method will add a new node to the hive.
     * TODO: Implement controls to avoid inserting a bad node.
     *
     * @param node The new node to add.
     */
    public void addNode(Node node)
    {
        int currentNodeID = lastNode + 1;
        synchronized (NODES) {
            NODES.add(node.setNodeID(currentNodeID));
        }

        lastNode = currentNodeID;

        // Assign the node to a letter in the dictionary.
        synchronized (DICTIONARY) {
            resetDictionary();
            NODES.stream().forEach((n) -> {
                List<Node> l = DICTIONARY.get(Math.abs(((n.getNodeID() % lastNode) - 16) % 16));
                l.add(n);
            });
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("****** NEW NODE ******");
            LOG.info("* Host ==> " + node.getIpAddress());
            LOG.info("* Port ==> " + node.getPort());
            LOG.info("* Nodes Hive ==>");
            for (int k = 0; k < DICTIONARY.size(); k++) {
                try {
                    LOG.info("\t" + ((char) getDocumentMapperFromIndex(k)) + " ==> " + Arrays.toString(DICTIONARY.get(k).toArray(new Node[0])));
                }
                catch (Exception e) {
                    LOG.info("\tError printing this index.");
                }
            }
        }
    }

    /**
     * Given a character, it will return the node that corresponds to that character.
     *
     * @param c A character.
     *
     * @return The node where that character belongs.
     *
     * @throws ZeroNodesException If no nodes was found.
     */
    Node getNode(char c) throws ZeroNodesException, InvalidDocumentMapperException
    {
        if (lastNode == 0) {
            throw new ZeroNodesException("No nodes available!");
        }

        final int NODES_AVAILABLE = DICTIONARY.get(getIndexFromDocumentMapper(c)).size();
        if (NODES_AVAILABLE < 1) {
            throw new ZeroNodesException("No nodes available for that Document Mapper!");
        }
        else {
            // Select a random node.
            return DICTIONARY.get(getIndexFromDocumentMapper(c)).get(RandomUtils.nextInt(NODES_AVAILABLE));
        }
    }
}
