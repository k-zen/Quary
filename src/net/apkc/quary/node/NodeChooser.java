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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.apkc.quary.exceptions.ZeroNodesException;
import net.apkc.quary.util.QuaryConfiguration;
import net.apkc.quary.util.Timer;
import org.apache.hadoop.ipc.RPC;
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
    private static final Integer CHAR_0 = 0;
    private static final Integer CHAR_1 = 1;
    private static final Integer CHAR_2 = 2;
    private static final Integer CHAR_3 = 3;
    private static final Integer CHAR_4 = 4;
    private static final Integer CHAR_5 = 5;
    private static final Integer CHAR_6 = 6;
    private static final Integer CHAR_7 = 7;
    private static final Integer CHAR_8 = 8;
    private static final Integer CHAR_9 = 9;
    private static final Integer CHAR_A = 10;
    private static final Integer CHAR_B = 11;
    private static final Integer CHAR_C = 12;
    private static final Integer CHAR_D = 13;
    private static final Integer CHAR_E = 14;
    private static final Integer CHAR_F = 15;
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

        DICTIONARY.add(CHAR_0, new ArrayList<>());
        DICTIONARY.add(CHAR_1, new ArrayList<>());
        DICTIONARY.add(CHAR_2, new ArrayList<>());
        DICTIONARY.add(CHAR_3, new ArrayList<>());
        DICTIONARY.add(CHAR_4, new ArrayList<>());
        DICTIONARY.add(CHAR_5, new ArrayList<>());
        DICTIONARY.add(CHAR_6, new ArrayList<>());
        DICTIONARY.add(CHAR_7, new ArrayList<>());
        DICTIONARY.add(CHAR_8, new ArrayList<>());
        DICTIONARY.add(CHAR_9, new ArrayList<>());
        DICTIONARY.add(CHAR_A, new ArrayList<>());
        DICTIONARY.add(CHAR_B, new ArrayList<>());
        DICTIONARY.add(CHAR_C, new ArrayList<>());
        DICTIONARY.add(CHAR_D, new ArrayList<>());
        DICTIONARY.add(CHAR_E, new ArrayList<>());
        DICTIONARY.add(CHAR_F, new ArrayList<>());
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

        System.out.println("\t\tAdded new node at:");
        System.out.println("\t\t\tHost ==> " + node.getIpAddress());
        System.out.println("\t\t\tPort ==> " + node.getPort());
    }

    private Node getNode(char c) throws ZeroNodesException
    {
        if (lastNode == 0) {
            throw new ZeroNodesException("No nodes available!");
        }

        switch (c) {
            case '0':
                return DICTIONARY.get(CHAR_0).get(0); // Default node is 0. In this case it may be null.
            case '1':
                return (DICTIONARY.get(CHAR_1).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_1).get(0);
            case '2':
                return (DICTIONARY.get(CHAR_2).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_2).get(0);
            case '3':
                return (DICTIONARY.get(CHAR_3).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_3).get(0);
            case '4':
                return (DICTIONARY.get(CHAR_4).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_4).get(0);
            case '5':
                return (DICTIONARY.get(CHAR_5).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_5).get(0);
            case '6':
                return (DICTIONARY.get(CHAR_6).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_6).get(0);
            case '7':
                return (DICTIONARY.get(CHAR_7).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_7).get(0);
            case '8':
                return (DICTIONARY.get(CHAR_8).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_8).get(0);
            case '9':
                return (DICTIONARY.get(CHAR_9).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_9).get(0);
            case 'A':
            case 'a':
                return (DICTIONARY.get(CHAR_A).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_A).get(0);
            case 'B':
            case 'b':
                return (DICTIONARY.get(CHAR_B).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_B).get(0);
            case 'C':
            case 'c':
                return (DICTIONARY.get(CHAR_C).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_C).get(0);
            case 'D':
            case 'd':
                return (DICTIONARY.get(CHAR_D).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_D).get(0);
            case 'E':
            case 'e':
                return (DICTIONARY.get(CHAR_E).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_E).get(0);
            case 'F':
            case 'f':
                return (DICTIONARY.get(CHAR_F).size() < 1) ? DICTIONARY.get(CHAR_0).get(0) : DICTIONARY.get(CHAR_F).get(0);
            default:
                return DICTIONARY.get(CHAR_0).get(0); // Default node is 0. In this case it may be null.
        }
    }

    public void printDictionary()
    {
        for (int k = 0; k < DICTIONARY.size(); k++) {
            System.out.println("Assigned nodes for character ==> " + k);
            System.out.println("\tNodes ==> " + Arrays.toString(DICTIONARY.get(k).toArray(new Node[0])));
        }
    }

    public NodeInterface getConnection(char c) throws ZeroNodesException
    {
        Timer t = new Timer();
        t.starTimer(); // Start timer.
        NodeInterface bean = null;
        int maxtries = 30;
        int tryCounter = 0;

        while (tryCounter < maxtries) {
            try {
                bean = (NodeInterface) RPC.getProxy(NodeInterface.class,
                                                    NodeInterface.versionID,
                                                    new InetSocketAddress(getNode(c).getIpAddress(), getNode(c).getPort()),
                                                    new QuaryConfiguration().create());

                if (bean != null && bean.isUp()) {
                    t.endTimer(); // Stop the timer.
                    return bean;
                }
            }
            catch (IOException e) {
                tryCounter++;
                LOG.info("Can't connect to node. Try (" + tryCounter + "). Trying again...");
            }
        }

        LOG.fatal("Impossible to connect to node. Giving up.");

        return bean;
    }
}
