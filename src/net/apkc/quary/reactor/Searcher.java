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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.apkc.quary.exceptions.ObjectConfigurationException;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.apache.lucene.search.IndexSearcher;

/**
 * This class encapsulates a Lucene index search object alongside some
 * properties.
 *
 * @author K-Zen
 * @see Builder Pattern
 * @see Annotations
 */
public class Searcher
{

    private static final Logger LOG = Logger.getLogger(Searcher.class.getName());
    // Element positions in mark array.
    private static final String IS_OPEN = "setIsOpen";
    private static final String SHOULD_CLOSE = "setShouldClose";
    private static final String SEARCHER = "setSearcher";
    /** Array to mark if the variables had been initialized. */
    private Map<String, Mark> marks = new HashMap<>();
    private Configuration config = new QuaryConfiguration().create();
    /**
     * Mark if this searcher is open. If is open used, never use a close searcher, will result in a
     * NullPointerException.
     */
    private boolean isOpen;
    /** Mark if this searcher should be closed after using it. */
    private boolean shouldClose;
    /** The Lucene search object. */
    private IndexSearcher searcher;

    private Searcher()
    {
        marks.put(IS_OPEN, new Mark(Boolean.FALSE));
        marks.put(SHOULD_CLOSE, new Mark(Boolean.FALSE));
        marks.put(SEARCHER, new Mark(Boolean.FALSE));
    }

    /**
     * Make a new instance of this class.
     *
     * @return This instance.
     */
    public static Searcher newBuild()
    {
        return new Searcher();
    }

    /**
     * Sets the <b><i>isOpen</i></b> option. Order # is 1.
     *
     * @param isOpen TRUE if this searcher is open, FALSE otherwise.
     *
     * @return This instance.
     */
    @Use(isOptional = false)
    public Searcher setIsOpen(boolean isOpen)
    {
        this.isOpen = isOpen;
        marks.put(IS_OPEN, new Mark(Boolean.TRUE));
        return this;
    }

    /**
     * Sets the <b><i>searcher</i></b> option. Order # is 2.
     *
     * @param searcher The object which contains the search interface.
     *
     * @return This instance.
     */
    @Use(isOptional = false)
    public Searcher setSearcher(IndexSearcher searcher)
    {
        this.searcher = searcher;
        marks.put(SEARCHER, new Mark(Boolean.TRUE));
        return this;
    }

    /**
     * Sets the <b><i>shouldClose</i></b> option. Order # is 3.
     *
     * @param shouldClose TRUE if this searcher should be closed down after use, FALSE otherwise.
     *
     * @return This instance.
     */
    @Use(isOptional = false)
    public Searcher setShouldClose(boolean shouldClose)
    {
        this.shouldClose = shouldClose;
        marks.put(SHOULD_CLOSE, new Mark(Boolean.TRUE));
        return this;
    }

    /**
     * Checks if this object is properly configured.
     *
     * @return This instance.
     */
    public Searcher checkObject()
    {
        // Check annotations for this object.
        Method[] methods = Searcher.class.getMethods();
        for (Method m : methods) {
            String methodName = m.getName();
            Annotation[] annotations = m.getAnnotations();
            for (Annotation a : annotations) {
                if (a instanceof Use) {
                    Use u = (Use) a;
                    // Check if is optional.
                    if (marks.containsKey(methodName)) {
                        if ((!u.isOptional() && !marks.get(methodName).isOptional)) {
                            try {
                                throw new ObjectConfigurationException("This object hasn't been properly configured!");
                            }
                            catch (ObjectConfigurationException e) {
                                LOG.error("Object's configuration error. Error: " + e.toString(), e);
                            }
                        }
                    }
                }
            }
        }

        return this;
    }

    public boolean getIsOpen()
    {
        return isOpen;
    }

    public boolean getShouldClose()
    {
        return shouldClose;
    }

    public IndexSearcher getSearcher()
    {
        return searcher;
    }

    /**
     * States how this class should be used.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @interface Use
    {

        /**
         * If the method is optional or not.
         *
         * @return TRUE if its optional, FALSE otherwise.
         */
        boolean isOptional();
    }

    /**
     * Class that encapsulates the used and current properties of methods.
     *
     * <p>
     * This properties need to be checked against the annotations of each method
     * to see if they are equal to each other. If they are not then an exception must
     * be thrown.</p>
     */
    class Mark
    {

        boolean isOptional;

        Mark(boolean isOptional)
        {
            this.isOptional = isOptional;
        }
    }
}
