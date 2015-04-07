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
import net.apkc.quary.exceptions.ZeroNodesException;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;

public class NodeConnection
{

    private static final Configuration CONF = new QuaryConfiguration().create();

    /**
     * Establish a live connection a given node.
     * TODO: Find a way to add a custom RetryPolicy, like: <code>RetryPolicies.retryUpToMaximumCountWithFixedSleep(CONF.getInt("node.connection.maxretries", 4),CONF.getInt("node.connection.sleeptime", 5),TimeUnit.SECONDS)</code>
     *
     * @param node The node where to open the connection to.
     *
     * @return A NodeInterface object. It can be used to interact with the node.
     *
     * @throws ZeroNodesException If no nodes was found.
     * @throws IOException        If a connection wasn't possible.
     */
    public static NodeInterface getConnection(Node node) throws IOException, ZeroNodesException
    {
        return RPC.getProtocolProxy(NodeInterface.class,
                                    NodeInterface.versionID,
                                    new InetSocketAddress(
                                            node.getIpAddress(),
                                            node.getPort()),
                                    UserGroupInformation.getCurrentUser(),
                                    CONF,
                                    NetUtils.getDefaultSocketFactory(CONF),
                                    2000,
                                    null).getProxy();
    }
}
