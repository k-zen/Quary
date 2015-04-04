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

/**
 * Enumerations that are used inside other classes and methods in DILI.
 *
 * @author Andreas P. Koenzen
 * @version 1.0
 */
public enum Constants
{

    // Resources
    TEST_DEFINITION("/resources/definitions/TestDefinition.xml"),
    XSD_SCHEMA_FILE("/resources/definitions/Schema.xsd"),
    // Paths
    DEFINITION_DB_FILE("/Quary/data/definition_db"),
    INDEX_FILE("/Quary/data/index_"),
    LOGS_PATH("/Quary/logs"),
    TMP_PATH("/Quary/tmp"),
    // Encryption
    ENCRYPTION_KEY("tictactoe1234567"),
    // Character Encoding
    DEFAULT_CHAR_ENCODING("UTF-8");
    // Valor de la constante.
    private String c1 = "";
    private int c2 = 0;
    private boolean c3 = false;

    private Constants(Object constant)
    {
        if (constant instanceof String) {
            c1 = (String) constant;
        }
        else if (constant instanceof Integer) {
            c2 = (Integer) constant;
        }
        else if (constant instanceof Boolean) {
            c3 = (Boolean) constant;
        }
    }

    public String getStringConstant()
    {
        return c1;
    }

    public int getIntegerConstant()
    {
        return c2;
    }

    public boolean getBooleanConstant()
    {
        return c3;
    }
}
