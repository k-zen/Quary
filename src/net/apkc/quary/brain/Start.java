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
package net.apkc.quary.brain;

import java.io.IOException;
import net.apkc.emma.tasks.TasksHandler;
import net.apkc.quary.config.XMLBuilder;
import net.apkc.quary.definitions.index.IndexDefinitionDB;
import net.apkc.quary.exceptions.ServerNotConfiguredException;
import net.apkc.quary.node.NodeChooser;
import net.apkc.quary.reactor.Reactor;
import net.apkc.quary.util.Constants;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.log4j.Logger;

public class Start
{

    private static final Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args)
    {
        final Configuration CONF = new QuaryConfiguration().create();

        new Thread("QUARY:REACTOR:START")
        {
            @Override
            public void run()
            {
                try {
                    final Integer REACTOR_PORT = CONF.getInt("reactor.port", 14999);

                    IndexDefinitionDB.getInstance().addDefinition("000", XMLBuilder.parseDefinitionFile(Start.class.getResourceAsStream(Constants.TEST_DEFINITION.getStringConstant())));

                    System.out.printf("Starting Reactor...\n");
                    System.out.printf("\tPort: %d\n", REACTOR_PORT);

                    Reactor.newBuild().configure(REACTOR_PORT).startReactor();
                }
                catch (IOException | ServerNotConfiguredException e) {
                    LOG.fatal("Error starting *Reactor*.", e);
                }
            }
        }.start();

        new Thread("QUARY:BRAIN:START")
        {
            @Override
            public void run()
            {
                try {
                    final String BRAIN_ADDRESS = "0.0.0.0";
                    final Integer BRAIN_PORT = CONF.getInt("brain.port", 14998);
                    final Integer BRAIN_HANDLERS = CONF.getInt("brain.handlers", 10);
                    final Integer BRAIN_QUEUE_SIZE_PER_HANDLER = CONF.getInt("brain.queuesizeperhandler", 10);
                    final Integer BRAIN_READERS = CONF.getInt("brain.readers", 10);

                    System.out.printf("Starting brain...\n");
                    System.out.printf("\tBind Address: %s\n", BRAIN_ADDRESS);
                    System.out.printf("\tPort: %d\n", BRAIN_PORT);
                    System.out.printf("\tHandlers: %d\n", BRAIN_HANDLERS);
                    System.out.printf("\tQueue Size Per Handler: %d\n", BRAIN_QUEUE_SIZE_PER_HANDLER);
                    System.out.printf("\tReaders: %d\n", BRAIN_READERS);

                    RPC.Server srv = new RPC.Builder(CONF)
                            .setProtocol(BrainInterface.class)
                            .setInstance(new BrainImplementation())
                            .setBindAddress(BRAIN_ADDRESS)
                            .setPort(BRAIN_PORT)
                            .setNumHandlers(BRAIN_HANDLERS)
                            .setQueueSizePerHandler(BRAIN_QUEUE_SIZE_PER_HANDLER)
                            .setnumReaders(BRAIN_READERS)
                            .setVerbose(true)
                            .build();
                    srv.start();
                    srv.join();
                }
                catch (IOException e) {
                    LOG.fatal("Error starting *Brain*.", e);
                }
                catch (InterruptedException e) {
                    LOG.fatal("*Brain* was interrupted.", e);
                }
            }
        }.start();

        new Thread("QUARY:PING:START")
        {
            @Override
            public void run()
            {
                TasksHandler.getInstance().submitInfiniteTask(NodeChooser.getInstance().new Ping());
            }
        }.start();
    }
}
