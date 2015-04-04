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
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TreeMap;
import net.apkc.quary.util.Constants;
import net.apkc.quary.util.SerializationUtils;

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
public final class IndexDefinitionDB implements Externalizable
{

    // Instance.
    private static volatile IndexDefinitionDB instance = new IndexDefinitionDB();
    private static volatile boolean isEmpty = true; // Mark if this instance is empty. If TRUE then we must load data from file.
    private static TreeMap<String, IndexDefinition> definitions = new TreeMap<>();

    public IndexDefinitionDB()
    {
    }

    public static IndexDefinitionDB getInstance()
    {
        return instance;
    }

    private void updateInstance(IndexDefinitionDB newInstance)
    {
        if (newInstance != null) {
            instance = newInstance;
        }
    }

    public IndexDefinitionDB addDefinition(String key, IndexDefinition def)
    {
        definitions.put(key, def);
        return this;
    }

    public IndexDefinition getDefinition(String key)
    {
        return definitions.get(key);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeObject(definitions);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        definitions = SerializationUtils.<TreeMap<String, IndexDefinition>>readObject(in);
    }

    public static IndexDefinitionDB read()
    {
        if (isEmpty) {
            File f = new File(Constants.DEFINITION_DB_FILE.getStringConstant());
            getInstance().updateInstance((IndexDefinitionDB) FileStoring.getInstance().readFromFile(
                    f,
                    true,
                    Constants.ENCRYPTION_KEY.getStringConstant(),
                    Constants.DEFAULT_CHAR_ENCODING.getStringConstant()));
            isEmpty = false;
        }

        return getInstance();
    }

    public static void update(IndexDefinitionDB data)
    {
        FileStoring.getInstance().writeToFile(
                new File(Constants.DEFINITION_DB_FILE.getStringConstant()),
                data,
                true,
                Constants.ENCRYPTION_KEY.getStringConstant(),
                Constants.DEFAULT_CHAR_ENCODING.getStringConstant());
    }
}
