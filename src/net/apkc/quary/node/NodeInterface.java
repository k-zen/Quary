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

import net.apkc.quary.definitions.index.IndexDefinition;
import net.apkc.quary.docs.QuaryDocument;
import net.apkc.quary.reactor.Parameters;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.ipc.VersionedProtocol;

/**
 * Interface for DILI nodes. All DILI nodes must implement this interface.
 *
 * @author Andreas P. Koenzen
 * @version 1.0
 */
public interface NodeInterface extends VersionedProtocol
{

    public static long versionID = 1L;

    /**
     * Shutdown the node.
     */
    public void shutdown();

    /**
     * Close the indexer in the node. The node may still be up, but it'll be
     * out of service.
     *
     * @param definitionID The ID of the index.
     *
     * @return 0 if the operation was completed without errors, &gt;0 otherwise.
     */
    public int close(String definitionID);

    /**
     * Returns the version of the node.
     *
     * @return A long number with the version of the node.
     */
    public long version();

    /**
     * Utility method to check if a node is up and running.
     *
     * @return TRUE if the node is up, FALSE otherwise.
     */
    public boolean isUp();

    /**
     * This method initializes the kernel index writer.
     *
     * <p>
     * It must be called just before the write() method.</p>
     *
     * @param conf         The configuration file.
     * @param definitionID The ID of the index.
     *
     * @return 0 if the operation was completed without errors, >0 otherwise.
     */
    public int openWriter(Configuration conf, String definitionID);

    /**
     * Creates the kernel index. Via local or remote interface.
     *
     * @param key         Key that identifies the document.
     * @param doc         The AIME's document to be processed and indexed.
     * @param def         The definition object to use.
     * @param elapsedTime The elapsed time since the indexing process started.
     */
    public void write(Text key, QuaryDocument doc, IndexDefinition def, long elapsedTime);

    /**
     * Performs a search in a IndexServer instance.
     *
     * @param conf   Configuration file.
     * @param def    The definition object to use.
     * @param params The object containing the search parameters.
     *
     * @return The response in XML format, but encapsulated in an Hadoop Text
     *         object.
     */
    public Text search(Configuration conf, IndexDefinition def, Parameters params);

    /**
     * This method checks if the only reader to the kernel is open, and if it is
     * then return TRUE, FALSE otherwise.
     *
     * <p>
     * But first this method will try to open the reader to the kernel, by
     * calling the method openReader().</p>
     *
     * @param reOpenReaders If the reader/s should be re-open after an index
     *                      update operation. TRUE will re-open all readers, and
     *                      FALSE will leave everything as is.
     * @param definitionID  The ID of the index.
     *
     * @return TRUE if the reader is open, FALSE otherwise.
     */
    public boolean areSearchersOpen(boolean reOpenReaders, String definitionID);

    /**
     * This method cleans/deletes up all the files in the kernel index.
     *
     * @param definitionID The ID of the index.
     *
     * @return TRUE if the files had been deleted, FALSE otherwise.
     */
    public boolean cleanIndex(String definitionID);
}
