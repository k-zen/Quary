/*
 * Copyright (c) 2015, Andreas P. Koenzen <akc at apkc.net>
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
import net.apkc.quary.exceptions.InvalidDocumentMapperException;
import net.apkc.quary.exceptions.ZeroNodesException;
import net.apkc.quary.util.QuaryConfiguration;
import net.apkc.quary.util.Timer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.log4j.Logger;

public class NodeConnection
{

    private static final Logger LOG = Logger.getLogger(NodeConnection.class.getName());
    private static final Configuration CONF = new QuaryConfiguration().create();

    /**
     * Establish a live connection a given node.
     *
     * @param c The first character of the document to handle. Based on this we know where to index.
     *
     * @return A NodeInterface object. It can be used to interact with the node.
     *
     * @throws ZeroNodesException             If no nodes was found.
     * @throws InvalidDocumentMapperException If the Document Mapper is invalid.
     */
    public static NodeInterface getConnection(char c) throws ZeroNodesException, InvalidDocumentMapperException
    {
        Timer t = new Timer();
        t.starTimer();
        NodeInterface bean = null;
        int maxtries = 30;
        int tryCounter = 0;

        while (tryCounter < maxtries) {
            try {
                bean = (NodeInterface) RPC.getProxy(NodeInterface.class,
                                                    NodeInterface.versionID,
                                                    new InetSocketAddress(NodeChooser.getInstance().getNode(c).getIpAddress(), NodeChooser.getInstance().getNode(c).getPort()),
                                                    CONF);

                if (bean != null && bean.isUp()) {
                    t.endTimer();
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
