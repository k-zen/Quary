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
package net.apkc.quary.util;

import java.io.IOException;
import java.io.ObjectInput;

/**
 * Serialization utilities.
 *
 * @author Andreas P. Koenzen <akc@apkc.net>
 * @version 1.0
 */
public final class SerializationUtils
{

    /**
     * Reads an object from an input and converts it to the appropriate type.
     *
     * @param <T> The type of object to return.
     * @param in  The input object.
     *
     * @return The casted object.
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public static <T> T readObject(ObjectInput in) throws IOException, ClassNotFoundException
    {
        return (T) in.readObject();
    }

    /**
     * Reads an object from an input and converts it to the appropriate type.
     *
     * @param <T> The type of object to return.
     * @param ob  The object to cast.
     *
     * @return The casted object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T castObject(Object ob)
    {
        return (T) ob;
    }
}
