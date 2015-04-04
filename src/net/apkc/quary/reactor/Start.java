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

import net.apkc.emma.tasks.TasksHandler;
import net.apkc.quary.config.XMLBuilder;
import net.apkc.quary.definitions.index.IndexDefinitionDB;
import net.apkc.quary.tasks.init.ReactorStartTask;
import net.apkc.quary.util.Constants;
import org.apache.log4j.Logger;

public class Start
{

    private static final Logger LOG = Logger.getLogger(Start.class.getName());

    public static void main(String[] args)
    {
        // Add test definition to db and then save to file.
        IndexDefinitionDB.update(IndexDefinitionDB.read().addDefinition("000000000000", XMLBuilder.parseDefinitionFile(Start.class.getResourceAsStream(Constants.TEST_DEFINITION.getStringConstant()))));

        // Launch HTTP server proceses
        TasksHandler.getInstance().submitInfiniteTask(ReactorStartTask.newBuild());
    }
}
