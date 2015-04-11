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
import java.net.InetAddress;
import net.apkc.quary.brain.BrainConnection;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.log4j.Logger;

public class Start
{

    private static final Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args)
    {
        if (args.length < 1) {
            System.err.printf("Port parameter not passed! Try again.\n");
            System.exit(-1);
        }

        try {
            final Configuration CONF = new QuaryConfiguration().create();
            final String NODE_IP_ADDRESS = InetAddress.getLocalHost().getHostAddress();
            final String NODE_ADDRESS = "0.0.0.0";
            final Integer NODE_PORT = Integer.parseInt(args[0]);
            final Integer NODE_HANDLERS = CONF.getInt("node.handlers", 10);
            final Integer NODE_QUEUE_SIZE_PER_HANDLER = CONF.getInt("node.queuesizeperhandler", 10);
            final Integer NODE_READERS = CONF.getInt("node.readers", 10);

            System.out.printf("Starting node...\n");
            System.out.printf("\tBind Address: %s\n", NODE_ADDRESS);
            System.out.printf("\tPort: %d\n", NODE_PORT);
            System.out.printf("\tHandlers: %d\n", NODE_HANDLERS);
            System.out.printf("\tQueue Size Per Handler: %d\n", NODE_QUEUE_SIZE_PER_HANDLER);
            System.out.printf("\tReaders: %d\n", NODE_READERS);
            System.out.printf("\tRegistering node with *Brain* at address *%s:%d*.\n", CONF.get("brain.host", "lucy.local"), CONF.getInt("brain.port", 14998));
            BrainConnection
                    .getConnection()
                    .registerNode(Node
                            .newBuild()
                            .setIpAddress(NODE_IP_ADDRESS)
                            .setPort(String.valueOf(NODE_PORT)));

            RPC.Server srv = new RPC.Builder(CONF)
                    .setProtocol(NodeInterface.class)
                    .setInstance(new NodeImplementation())
                    .setBindAddress(NODE_ADDRESS)
                    .setPort(NODE_PORT)
                    .setNumHandlers(NODE_HANDLERS)
                    .setQueueSizePerHandler(NODE_QUEUE_SIZE_PER_HANDLER)
                    .setnumReaders(NODE_READERS)
                    .setVerbose(true)
                    .build();
            srv.start();
            srv.join();
        }
        catch (IOException e) {
            LOG.fatal("Error starting *Node*.", e);
        }
        catch (InterruptedException e) {
            LOG.fatal("*Node* was interrupted.", e);
        }
    }
}
