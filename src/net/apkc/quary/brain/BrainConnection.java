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
import java.net.InetSocketAddress;
import net.apkc.quary.util.QuaryConfiguration;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.net.NetUtils;
import org.apache.hadoop.security.UserGroupInformation;

public class BrainConnection
{

    private static final Configuration CONF = new QuaryConfiguration().create();

    /**
     * Establish a live connection to the *Brain*.
     * TODO: Find a way to add a custom RetryPolicy, like: <code>RetryPolicies.retryUpToMaximumCountWithFixedSleep(CONF.getInt("brain.connection.maxretries", 4),CONF.getInt("brain.connection.sleeptime", 5),TimeUnit.SECONDS)</code>
     *
     * @return A BrainInterface object. It can be used to interact with the *Brain*.
     *
     * @throws IOException If a connection wasn't possible.
     */
    public static BrainInterface getConnection() throws IOException
    {
        return RPC.getProtocolProxy(BrainInterface.class,
                                    BrainInterface.versionID,
                                    new InetSocketAddress(
                                            CONF.get("brain.host", "lucy.local"),
                                            CONF.getInt("brain.port", 14998)),
                                    UserGroupInformation.getCurrentUser(),
                                    CONF,
                                    NetUtils.getDefaultSocketFactory(CONF),
                                    2000,
                                    null).getProxy();
    }
}
