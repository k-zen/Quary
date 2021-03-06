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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import net.apkc.quary.util.QuaryWritable;
import org.apache.log4j.Logger;

/**
 * This class represents a *Node* component and all its settings.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 * @version 0.1
 */
public final class Node extends QuaryWritable
{

    private static final Logger LOG = Logger.getLogger(Node.class.getName());
    private String nodeID = "";
    private String ipAddress = "";
    private int port = 15000;

    public static Node newBuild()
    {
        return new Node();
    }

    Node setNodeID(String newString)
    {
        if (newString == null || newString.isEmpty()) {
            return this;
        }

        this.nodeID = newString;
        return this;
    }

    String getNodeID()
    {
        return nodeID;
    }

    public Node setIpAddress(String newString)
    {
        if (newString == null || newString.isEmpty()) {
            return this;
        }

        ipAddress = newString;
        return this;
    }

    public String getIpAddress()
    {
        return ipAddress;
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
    public void internalWrite(DataOutput out) throws IOException
    {
        out.writeUTF(nodeID);
        out.writeUTF(ipAddress);
        out.writeInt(port);
    }

    @Override
    public void internalRead(DataInput in) throws IOException
    {
        nodeID = in.readUTF();
        ipAddress = in.readUTF();
        port = in.readInt();
    }

    @Override
    public int compareTo(QuaryWritable o)
    {
        Node otherNode = (Node) o;
        return this.nodeID.compareTo(otherNode.nodeID);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Node)) {
            return false;
        }

        Node n = (Node) obj;
        return hashCode() == n.hashCode();
    }

    @Override
    public int hashCode()
    {
        int hash = 0x54;
        hash ^= nodeID != null ? nodeID.hashCode() : 0x0;
        hash ^= ipAddress != null ? ipAddress.hashCode() : 0x0;
        hash ^= port;

        return hash;
    }

    @Override
    protected Object clone()
    {
        try {
            return ((Node) super.clone())
                    .setNodeID(nodeID)
                    .setIpAddress(ipAddress)
                    .setPort(String.valueOf(port));
        }
        catch (CloneNotSupportedException e) {
            LOG.warn("Error cloning *Node* object.", e);
        }

        return null;
    }
}
