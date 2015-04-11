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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import net.apkc.quary.exceptions.ZeroNodesException;
import org.apache.commons.codec.digest.DigestUtils;
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
    private final List<Node> NODES = new ArrayList<>();
    private static final NodeChooser INSTANCE = new NodeChooser();

    /**
     * Private default constructor.
     */
    private NodeChooser()
    {
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
     * This method will add a new node to the hive.
     * TODO: Implement controls to avoid inserting a bad node.
     *
     * @param node The new node to add.
     */
    public synchronized void addNode(Node node)
    {
        final String NODE_ID = DigestUtils.md5Hex(node.getIpAddress() + node.getPort()).substring(0, 24);

        synchronized (NODES) {
            NODES.add(node.setNodeID(NODE_ID));

            if (LOG.isInfoEnabled()) {
                LOG.info("****** NEW NODE ******");
                LOG.info("* Host ==> " + node.getIpAddress());
                LOG.info("* Port ==> " + node.getPort());
                LOG.info("* Nodes Hive ==>");
                NODES.stream().forEach((Node n) -> {
                    LOG.info("\t" + n.toString());
                });
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
        synchronized (NODES) {
            NODES.remove(node);

            if (LOG.isInfoEnabled()) {
                LOG.info("****** NODE REMOVED ******");
                LOG.info("* Host ==> " + node.getIpAddress());
                LOG.info("* Port ==> " + node.getPort());
                LOG.info("* Nodes Hive ==>");
                NODES.stream().forEach((Node n) -> {
                    LOG.info("\t" + n.toString());
                });
            }
        }
    }

    /**
     * Return a node from the pool.
     *
     * @return A random node.
     *
     * @throws ZeroNodesException If no nodes was found.
     */
    public Node getNode() throws ZeroNodesException
    {
        if (NODES.isEmpty()) {
            throw new ZeroNodesException("No nodes available!");
        }

        return NODES.get(RandomUtils.nextInt(NODES.size()));
    }

    /**
     * Return all nodes from the pool.
     *
     * @return All nodes from the pool.
     *
     * @throws ZeroNodesException If no nodes was found.
     */
    public Node[] getNodes() throws ZeroNodesException
    {
        if (NODES.isEmpty()) {
            throw new ZeroNodesException("No nodes available!");
        }

        return NODES.toArray(new Node[0]);
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
