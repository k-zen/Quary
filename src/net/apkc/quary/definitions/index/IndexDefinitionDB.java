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
package net.apkc.quary.definitions.index;

import io.aime.aimemisc.io.FileStoring;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import net.apkc.quary.util.Constants;
import org.apache.log4j.Logger;

/**
 * Indexed file based database for storing definitions. This database should be able
 * to store serializable Java object into files, but at the same time should be
 * able to very fast access those objects.
 *
 * <p>
 * It should use something like "Hadoop's Sequence" files. One file for the index,
 * which should be an inverted index containing the definition ID and the offset
 * of contents, and another file having the bulk of data in a sequential format.
 * </p>
 *
 * <p>
 * Note: If some definition is removed, then the data file should remove the data
 * for that definition, but that implies that all other data after that record
 * should be rearranged along with the index. Another possibility is that after
 * some record has been removed the record in the index could just me marked as
 * removed or non-valid, and a separate job could rearrange the files, when the
 * system is idle.
 * </p>
 *
 * <p>
 * Concept of Quary IndexDefinition:<br/>
 * A definition is an object that defines the way a document should be stored in
 * Lucene. It specifies the Lucene fields and its properties.
 * </p>
 *
 * <p>
 * <b>Database example:</b><br/>
 * <pre>
 * Unique IndexDefinition ID => some_random_64_hash
 * DefinitionObj        => Complete definition object
 * </pre>
 * </p>
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 1.0
 */
public final class IndexDefinitionDB
{

    private static final Logger LOG = Logger.getLogger(IndexDefinitionDB.class.getName());
    private static final IndexDefinitionDB INSTANCE = new IndexDefinitionDB();
    private final TreeMap<String, IndexDefinition> DEFINITIONS;

    /**
     * Private default constructor.
     */
    private IndexDefinitionDB()
    {
        Object data = null;
        try {
            data = FileStoring.getInstance().readFromFile(
                    new File(Constants.DEFINITION_DB_FILE.getStringConstant()),
                    false,
                    null,
                    "UTF-8");
        }
        catch (IOException | ClassNotFoundException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            LOG.warn("Problem reading object from file.", e);
        }
        finally {
            if (data != null) {
                DEFINITIONS = (TreeMap<String, IndexDefinition>) data;
            }
            else {
                DEFINITIONS = new TreeMap<>();
            }
        }
    }

    /**
     * Returns the only instance of this class.
     *
     * @return The only instance of class.
     */
    public static IndexDefinitionDB getInstance()
    {
        return INSTANCE;
    }

    /**
     * Add a new definition to the DB.
     *
     * @param key The key of the definition.
     * @param def The definition object.
     *
     * @return This instance.
     */
    public IndexDefinitionDB addDefinition(String key, IndexDefinition def)
    {
        synchronized (DEFINITIONS) {
            DEFINITIONS.put(key, def);

            FileStoring.getInstance().writeToFile(
                    new File(Constants.DEFINITION_DB_FILE.getStringConstant()),
                    DEFINITIONS,
                    false,
                    null,
                    "UTF-8");
        }

        return this;
    }

    /**
     * Returns the definition that corresponds to the key.
     *
     * @param key The key to search.
     *
     * @return The definition object.
     */
    public IndexDefinition getDefinition(String key)
    {
        synchronized (DEFINITIONS) {
            return DEFINITIONS.get(key);
        }
    }
}
