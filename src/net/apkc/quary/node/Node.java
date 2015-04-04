/*
 * Copyright (c) 2015, Andreas P. Koenzen <akc at apkc.net>
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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.apache.log4j.Logger;

/**
 * This class represents a node and all its settings.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public class Node implements Externalizable
{

    private static final Logger LOG = Logger.getLogger(Node.class.getName());
    private int nodeID = -1;
    private String ipAddress = "";
    private int port = 15000;

    public Node()
    {
    }

    public static Node newBuild()
    {
        return new Node();
    }

    Node setNodeID(int nodeID)
    {
        this.nodeID = nodeID;
        return this;
    }

    public Node setIpAddress(String newString)
    {
        if (newString == null || newString.isEmpty()) {
            return this;
        }

        ipAddress = newString;
        return this;
    }

    public Node setPort(String newString)
    {
        if (newString == null || newString.isEmpty()) {
            return this;
        }

        try {
            port = Integer.parseInt(newString);
        }
        catch (Exception e) {
            LOG.warn("Impossible to parse node port. Using default.", e);
        }

        return this;
    }

    int getNodeID()
    {
        return nodeID;
    }

    public String getIpAddress()
    {
        return ipAddress;
    }

    public int getPort()
    {
        return port;
    }

    @Override
    public String toString()
    {
        StringBuilder b = new StringBuilder();
        b.append(nodeID).append(":").append(ipAddress).append(":").append(port);

        return b.toString();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException
    {
        out.writeInt(nodeID);
        out.writeUTF(ipAddress);
        out.writeInt(port);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
    {
        nodeID = in.readInt();
        ipAddress = in.readUTF();
        port = in.readInt();
    }
}
