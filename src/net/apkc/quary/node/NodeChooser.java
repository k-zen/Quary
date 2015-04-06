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

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
    private static final Integer DICTIONARY_SIZE = 16;
    private final List<List<Node>> DICTIONARY = new ArrayList<>(DICTIONARY_SIZE);
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
     *
     * NOTE: Do not synchronize this method! It is already used inside synchronized methods.
     */
    private void resetDictionary()
    {
        DICTIONARY.clear();

        for (int k = 0; k < DICTIONARY_SIZE; k++) {
            DICTIONARY.add(k, new ArrayList<>());
        }
    }

    /**
     * This method calculates the index in the dictionary of a given Document Mapper.
     *
     * @param DOCUMENT_MAPPER The Document Mapper.
     *
     * @return The position in the dictionary.
     *
     * @throws InvalidDocumentMapperException If the Document Mapper is invalid.
     */
    public int calculateDictionaryIndex(final int DOCUMENT_MAPPER) throws InvalidDocumentMapperException
    {
        return Math.abs((((getIndexFromDocumentMapper(DOCUMENT_MAPPER) + 1) % lastNode) - DICTIONARY_SIZE) % DICTIONARY_SIZE);
    }

    /**
     * This method will map a character (Document Mapper) to a given index in the dictionary.
     *
     * @param DOCUMENT_MAPPER The Document Mapper (The Index).
     *
     * @return The dictionary index that corresponds to the Document Mapper.
     *
     * @throws InvalidDocumentMapperException If the Document Mapper is not valid.
     */
    public int getIndexFromDocumentMapper(final int DOCUMENT_MAPPER) throws InvalidDocumentMapperException
    {
        switch (DOCUMENT_MAPPER) {
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
                return Integer.parseInt(String.valueOf((char) DOCUMENT_MAPPER));
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
     * @param INDEX The index.
     *
     * @return The Document Mapper that corresponds to that index.
     *
     * @throws InvalidIndexException If the index is not valid.
     */
    public int getDocumentMapperFromIndex(final int INDEX) throws InvalidIndexException
    {
        switch (INDEX) {
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
                return String.valueOf(INDEX).charAt(0);
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
    public synchronized void addNode(Node node)
    {
        int currentNodeID = lastNode + 1;
        synchronized (NODES) {
            NODES.add(node.setNodeID(currentNodeID));

            lastNode = currentNodeID;

            // Assign the node to a letter in the dictionary.
            synchronized (DICTIONARY) {
                resetDictionary();
                NODES.stream().forEach((n) -> {
                    List<Node> l = DICTIONARY.get(Math.abs(((n.getNodeID() % lastNode) - DICTIONARY_SIZE) % DICTIONARY_SIZE));
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
                        LOG.info("\t" + ((char) getDocumentMapperFromIndex(k)) + (k == 0 ? " *CatchAll*" : "") + " ==> " + Arrays.toString(DICTIONARY.get(k).toArray(new Node[0])));
                    }
                    catch (Exception e) {
                        LOG.info("\tError printing this index.");
                    }
                }
            }
        }
    }

    /**
     * This method will remove a node from the hive.
     *
     * @param node The node to remove.
     */
    private synchronized void removeNode(Node node)
    {
        int currentNodeID = lastNode - 1;
        synchronized (NODES) {
            NODES.remove(node);

            lastNode = currentNodeID;

            // Assign the node to a letter in the dictionary.
            synchronized (DICTIONARY) {
                resetDictionary();
                NODES.stream().forEach((n) -> {
                    List<Node> l = DICTIONARY.get(Math.abs(((n.getNodeID() % lastNode) - DICTIONARY_SIZE) % DICTIONARY_SIZE));
                    l.add(n);
                });
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("****** NODE REMOVED ******");
                LOG.info("* Host ==> " + node.getIpAddress());
                LOG.info("* Port ==> " + node.getPort());
                LOG.info("* Nodes Hive ==>");
                for (int k = 0; k < DICTIONARY.size(); k++) {
                    try {
                        LOG.info("\t" + ((char) getDocumentMapperFromIndex(k)) + (k == 0 ? " *CatchAll*" : "") + " ==> " + Arrays.toString(DICTIONARY.get(k).toArray(new Node[0])));
                    }
                    catch (Exception e) {
                        LOG.info("\tError printing this index.");
                    }
                }
            }
        }
    }

    /**
     * Given a character, it will return the node that corresponds to that character.
     *
     * @param C A character.
     *
     * @return The node where that character belongs.
     *
     * @throws ZeroNodesException If no nodes was found.
     */
    Node getNode(final char C) throws ZeroNodesException, InvalidDocumentMapperException
    {
        if (lastNode == 0) {
            throw new ZeroNodesException("No nodes available!");
        }

        final int NODES_AVAILABLE = DICTIONARY.get(getIndexFromDocumentMapper(C)).size();
        if (NODES_AVAILABLE < 1) {
            // Document Mapper 0 is *CatchAll*.
            return DICTIONARY.get(getIndexFromDocumentMapper('0')).get(RandomUtils.nextInt(DICTIONARY.get(getIndexFromDocumentMapper('0')).size()));
        }
        else {
            return DICTIONARY.get(getIndexFromDocumentMapper(C)).get(RandomUtils.nextInt(NODES_AVAILABLE));
        }
    }

    public class Ping implements Runnable
    {

        final List<Node> NODES_FOR_REMOVAL = new LinkedList<>();

        @Override
        public void run()
        {
            while (true) {
                synchronized (NODES) {
                    NODES.stream().forEach((Node n) -> {
                        try {
                            try (Socket socket = new Socket()) {
                                socket.connect(new InetSocketAddress(n.getIpAddress(), n.getPort()), 200);
                                LOG.info("Node *" + n.toString() + "* is up.");
                            }
                        }
                        catch (Exception e) {
                            NODES_FOR_REMOVAL.add(n);
                            LOG.info("Node *" + n.toString() + "* is down. It will be removed.");
                        }
                    });
                }

                ListIterator<Node> i = NODES_FOR_REMOVAL.listIterator();
                while (i.hasNext()) {
                    Node n = i.next();
                    removeNode(n);
                    i.remove();
                }

                try {
                    Thread.currentThread().join(2000);
                }
                catch (Exception e) {
                    LOG.error("Generic error.", e);
                }

                if (LOG.isTraceEnabled()) {
                    LOG.trace("Checking nodes...");
                }
            }
        }
    }
}
