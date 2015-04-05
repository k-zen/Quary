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
package net.apkc.quary.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Class for testing the NIO server.
 *
 * @author Andreas P. Koenzen <akc at apkc.net>
 */
public class ReactorTesting
{

    private Socket client;

    void makeConnection()
    {
        try {
            client = new Socket();
            client.connect(new InetSocketAddress("127.0.0.1", 14999));
        }
        catch (UnknownHostException e) {
            System.err.println("Error: " + e.toString());
        }
        catch (IOException e) {
            System.err.println("Error: " + e.toString());
        }

        try {
            OutputStream out = client.getOutputStream();
            for (int k = 0; k < 1000; k++) {
                sendMessage(k, out);
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e.toString());
        }

        try {
            client.close();
        }
        catch (IOException e) {
            System.err.println("Error: " + e.toString());
        }
    }

    void sendMessage(int k, OutputStream out) throws IOException
    {
        StringBuilder buffer = new StringBuilder();
        buffer
                .append("<root definitionID=\"000000000000\" id=\"").append(k + 1).append("\">")
                .append("<anchor>Test</anchor>")
                .append("<boost>1.0</boost>")
                .append("<boostwithgravity>1.0</boostwithgravity>")
                .append("<content>QW4gSW5wdXRTdHJlYW1SZWFkZXIgaXMgYSBicmlkZ2UgZnJvbSBieXRlIHN0cmVhbXMgdG8gY2hhcmFjdGVyIHN0cmVhbXM6IEl0IHJlYWRzIGJ5dGVzIGFuZCBkZWNvZGVzIHRoZW0gaW50byBjaGFyYWN0ZXJzIHVzaW5nIGEgc3BlY2lmaWVkIGNoYXJzZXQuIFRoZSBjaGFyc2V0IHRoYXQgaXQgdXNlcyBtYXkgYmUgc3BlY2lmaWVkIGJ5IG5hbWUgb3IgbWF5IGJlIGdpdmVuIGV4cGxpY2l0bHksIG9yIHRoZSBwbGF0Zm9ybSdzIGRlZmF1bHQgY2hhcnNldCBtYXkgYmUgYWNjZXB0ZWQuIEVhY2ggaW52b2NhdGlvbiBvZiBvbmUgb2YgYW4gSW5wdXRTdHJlYW1SZWFkZXIncyByZWFkKCkgbWV0aG9kcyBtYXkgY2F1c2Ugb25lIG9yIG1vcmUgYnl0ZXMgdG8gYmUgcmVhZCBmcm9tIHRoZSB1bmRlcmx5aW5nIGJ5dGUtaW5wdXQgc3RyZWFtLiBUbyBlbmFibGUgdGhlIGVmZmljaWVudCBjb252ZXJzaW9uIG9mIGJ5dGVzIHRvIGNoYXJhY3RlcnMsIG1vcmUgYnl0ZXMgbWF5IGJlIHJlYWQgYWhlYWQgZnJvbSB0aGUgdW5kZXJseWluZyBzdHJlYW0gdGhhbiBhcmUgbmVjZXNzYXJ5IHRvIHNhdGlzZnkgdGhlIGN1cnJlbnQgcmVhZCBvcGVyYXRpb24u</content>")
                .append("<contentraw>QW4gSW5wdXRTdHJlYW1SZWFkZXIgaXMgYSBicmlkZ2UgZnJvbSBieXRlIHN0cmVhbXMgdG8gY2hhcmFjdGVyIHN0cmVhbXM6IEl0IHJlYWRzIGJ5dGVzIGFuZCBkZWNvZGVzIHRoZW0gaW50byBjaGFyYWN0ZXJzIHVzaW5nIGEgc3BlY2lmaWVkIGNoYXJzZXQuIFRoZSBjaGFyc2V0IHRoYXQgaXQgdXNlcyBtYXkgYmUgc3BlY2lmaWVkIGJ5IG5hbWUgb3IgbWF5IGJlIGdpdmVuIGV4cGxpY2l0bHksIG9yIHRoZSBwbGF0Zm9ybSdzIGRlZmF1bHQgY2hhcnNldCBtYXkgYmUgYWNjZXB0ZWQuIEVhY2ggaW52b2NhdGlvbiBvZiBvbmUgb2YgYW4gSW5wdXRTdHJlYW1SZWFkZXIncyByZWFkKCkgbWV0aG9kcyBtYXkgY2F1c2Ugb25lIG9yIG1vcmUgYnl0ZXMgdG8gYmUgcmVhZCBmcm9tIHRoZSB1bmRlcmx5aW5nIGJ5dGUtaW5wdXQgc3RyZWFtLiBUbyBlbmFibGUgdGhlIGVmZmljaWVudCBjb252ZXJzaW9uIG9mIGJ5dGVzIHRvIGNoYXJhY3RlcnMsIG1vcmUgYnl0ZXMgbWF5IGJlIHJlYWQgYWhlYWQgZnJvbSB0aGUgdW5kZXJseWluZyBzdHJlYW0gdGhhbiBhcmUgbmVjZXNzYXJ5IHRvIHNhdGlzZnkgdGhlIGN1cnJlbnQgcmVhZCBvcGVyYXRpb24u</contentraw>")
                .append("<contentfilter>QW4gSW5wdXRTdHJlYW1SZWFkZXIgaXMgYSBicmlkZ2UgZnJvbSBieXRlIHN0cmVhbXMgdG8gY2hhcmFjdGVyIHN0cmVhbXM6IEl0IHJlYWRzIGJ5dGVzIGFuZCBkZWNvZGVzIHRoZW0gaW50byBjaGFyYWN0ZXJzIHVzaW5nIGEgc3BlY2lmaWVkIGNoYXJzZXQuIFRoZSBjaGFyc2V0IHRoYXQgaXQgdXNlcyBtYXkgYmUgc3BlY2lmaWVkIGJ5IG5hbWUgb3IgbWF5IGJlIGdpdmVuIGV4cGxpY2l0bHksIG9yIHRoZSBwbGF0Zm9ybSdzIGRlZmF1bHQgY2hhcnNldCBtYXkgYmUgYWNjZXB0ZWQuIEVhY2ggaW52b2NhdGlvbiBvZiBvbmUgb2YgYW4gSW5wdXRTdHJlYW1SZWFkZXIncyByZWFkKCkgbWV0aG9kcyBtYXkgY2F1c2Ugb25lIG9yIG1vcmUgYnl0ZXMgdG8gYmUgcmVhZCBmcm9tIHRoZSB1bmRlcmx5aW5nIGJ5dGUtaW5wdXQgc3RyZWFtLiBUbyBlbmFibGUgdGhlIGVmZmljaWVudCBjb252ZXJzaW9uIG9mIGJ5dGVzIHRvIGNoYXJhY3RlcnMsIG1vcmUgYnl0ZXMgbWF5IGJlIHJlYWQgYWhlYWQgZnJvbSB0aGUgdW5kZXJseWluZyBzdHJlYW0gdGhhbiBhcmUgbmVjZXNzYXJ5IHRvIHNhdGlzZnkgdGhlIGN1cnJlbnQgcmVhZCBvcGVyYXRpb24u</contentfilter>")
                .append("<contentlength>592</contentlength>")
                .append("<digest>1040840491643509340</digest>")
                .append("<domain>apkc.net</domain>")
                .append("<fetchtime>").append(System.currentTimeMillis()).append("</fetchtime>")
                .append("<filetype>text/plain</filetype>")
                .append("<gravity>10.0</gravity>")
                .append("<host>www.apkc.net</host>")
                .append("<indextime>").append(System.currentTimeMillis()).append("</indextime>")
                .append("<itsecond></itsecond>")
                .append("<itminute></itminute>")
                .append("<ithour></ithour>")
                .append("<itday></itday>")
                .append("<itmonth></itmonth>")
                .append("<ityear></ityear>")
                .append("<lang>en</lang>")
                .append("<lastmodified>").append(System.currentTimeMillis()).append("</lastmodified>")
                .append("<segment>123456</segment>")
                .append("<site>www.apkc.net</site>")
                .append("<title>Test Document</title>")
                .append("<url>www.apkc.net</url>")
                .append("</root>")
                .append('\t');

        System.out.println("Sending ==> " + buffer);

        out.write(buffer.toString().getBytes("UTF-8"));
        out.flush();
    }

    public static void main(String args[])
    {
        ReactorTesting t = new ReactorTesting();

        // Send in 10 bulks of 1000.
        for (int k = 0; k < 10; k++) {
            t.makeConnection();
        }

        System.exit(0);
    }
}
