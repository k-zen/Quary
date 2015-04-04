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
package net.apkc.quary.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import net.apkc.emma.tasks.TasksHandler;
import net.apkc.quary.config.XMLBuilder;
import net.apkc.quary.definitions.index.IndexDefinitionDB;
import net.apkc.quary.docs.QuaryDocument;
import net.apkc.quary.exceptions.ServerNotConfiguredException;
import net.apkc.quary.node.NodeChooser;
import net.apkc.quary.node.NodeInterface;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.io.Text;
import org.apache.log4j.Logger;

/**
 * This class represents a custom NIO Web Server which is implemented using the
 * Reactor Pattern.
 *
 * <p>
 * The only job of this server is to receive an XML file for
 * indexing/searching and dispatch the request to an appropriate Handler which
 * will be responsible to service that request.
 * </p>
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 * @see <a href="http://en.wikipedia.org/wiki/Builder_pattern">Builder Pattern</a>
 * @see <a href="http://en.wikipedia.org/wiki/Reactor_pattern">Reactor Pattern</a>
 */
public class Reactor
{

    private static final Logger LOG = Logger.getLogger(Reactor.class.getName());
    private final int BUFFER_SIZE = 1024 * 1024 * 15;
    private Selector selector = null;
    private ServerSocketChannel server = null;
    private boolean isConfigured = false;

    public static Reactor newBuild()
    {
        return new Reactor();
    }

    public Reactor configure(int port) throws IOException, UnknownHostException
    {
        selector = Selector.open();
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(new InetSocketAddress("127.0.0.1", port));
        isConfigured = true;

        System.out.println("\tServer configured...");

        return this;
    }

    public void startReactor() throws IOException, ServerNotConfiguredException
    {
        if (!isConfigured) {
            throw new ServerNotConfiguredException("The server wasn't configured!");
        }

        SelectionKey acceptKey = server.register(selector, SelectionKey.OP_ACCEPT);
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        while (acceptKey.selector().select() > 0) // Here the selector will block for new incomming connections.
        {
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = (SelectionKey) it.next();
                it.remove();

                SocketChannel socket;

                if (key.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                    socket = (SocketChannel) ssc.accept();
                    socket.configureBlocking(false);
                    socket.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE); // Here we enable "Reading" and "Writing" in the selector.
                }

                if (key.isReadable()) {
                    try {
                        socket = (SocketChannel) key.channel();
                        int read;
                        while ((read = socket.read(buffer)) != -1) {
                            buffer.flip();
                            String data = Charset.forName("UTF-8").decode(buffer).toString();
                            String[] strArr = data.split("\\\t");

                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Documents Received ==> " + strArr.length);
                            }

                            for (int k = 0; k < strArr.length; k++) {
                                if (k + 1 == strArr.length) {
                                    buffer.clear();
                                    buffer.put(Charset.forName("UTF-8").encode(strArr[k]));

                                    if (data.length() < BUFFER_SIZE) {
                                        process(new Process(strArr[k]));
                                        buffer.clear();
                                    }
                                }
                                else {
                                    if (strArr[k].length() > 0) {
                                        process(new Process(strArr[k]));
                                    }
                                }
                            }
                        }
                    }
                    catch (IOException | InterruptedException | ExecutionException ex) {
                        LOG.fatal("Reactor Error.", ex);
                        System.out.println("\tA fatal error has occurred and the Reactor will be shutdown...");
                        System.exit(1);
                    }
                }

                if (key.isWritable()) {
                    // Do nothing for the moment.
                }
            }
        }
    }

    /**
     * Process a request.
     *
     * @param p The request to process.
     */
    private void process(Process p) throws InterruptedException, ExecutionException
    {
        TasksHandler.getInstance().submitFiniteTask(p).get(); // Start processing the request.
    }

    /**
     * This class will process an XML document and will derive it
     * to a Quary node for indexing.
     */
    class Process implements Runnable
    {

        String document;

        Process(String document)
        {
            this.document = document.trim();
        }

        @Override
        public void run()
        {
            if (document == null || document.isEmpty()) {
                return;
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("Received document ==> " + document);
            }

            try {
                // Steps:
                // 1. Process the XML document (Convert it to a QuaryDocument)
                QuaryDocument doc = XMLBuilder.parseExternalDocumentToQuaryDocument(document);

                // 2. Make the decision where to index the document based on its contents.
                try {
                    NodeInterface node = NodeChooser.getInstance().getConnection(doc.getSignature().charAt(0));

                    node.openWriter(new QuaryConfiguration().create(), doc.getDefinitionID());
                    node.write(new Text(doc.getSignature()), doc, IndexDefinitionDB.read().getDefinition(doc.getDefinitionID()), 0L);
                    node.close(doc.getDefinitionID());
                }
                catch (Exception e) {
                    LOG.error("No nodes available! Error: " + e.toString(), e);
                }
            }
            catch (Exception ex) {
                LOG.error("Error processing document.\nDocument: " + document, ex);
            }
        }
    }
}
