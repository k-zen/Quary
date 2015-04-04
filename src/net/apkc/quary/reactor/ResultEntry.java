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

import io.aime.aimemisc.datamining.Block;

/**
 * A result entry corresponds to an individual result/document matched inside
 * the overall XML result provided to the user.
 *
 * @see Builder Pattern
 *
 * @author K-Zen
 */
class ResultEntry
{

    static final byte XML = 1;
    // Config
    private byte type = XML;
    // Document
    private String boost = "";
    private String boostGravity = "";
    private String contentBlocks = "";
    private String contentLength = "";
    private String digest = "";
    private String extension = "";
    private String fetchTime = "";
    private String fileType = "";
    private String gravity = "";
    private String indexTime = "";
    private String language = "";
    private String lastModified = "";
    private String segment = "";
    private String summary = "";
    private String title = "";
    private String url = "";

    private ResultEntry()
    {
    }

    static ResultEntry newBuild()
    {
        return new ResultEntry();
    }

    byte getType()
    {
        return type;
    }

    String getBoost()
    {
        return boost;
    }

    String getBoostGravity()
    {
        return boostGravity;
    }

    String getContentBlocks()
    {
        return contentBlocks;
    }

    String getContentLength()
    {
        return contentLength;
    }

    String getDigest()
    {
        return digest;
    }

    String getExtension()
    {
        return extension;
    }

    String getFetchTime()
    {
        return fetchTime;
    }

    String getFileType()
    {
        return fileType;
    }

    String getGravity()
    {
        return gravity;
    }

    String getIndexTime()
    {
        return indexTime;
    }

    String getLanguage()
    {
        return language;
    }

    String getLastModified()
    {
        return lastModified;
    }

    String getSegment()
    {
        return segment;
    }

    String getSummary()
    {
        return summary;
    }

    String getTitle()
    {
        return title;
    }

    String getURL()
    {
        return url;
    }

    ResultEntry setType(byte type)
    {
        this.type = type;
        return this;
    }

    ResultEntry setBoost(String boost)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<boost>").append((boost != null && !boost.isEmpty()) ? boost : "0.00").append("</boost>");
                break;
        }
        this.boost = s.toString();
        return this;
    }

    ResultEntry setBoostGravity(String boostGravity)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<boostwithgravity>").append((boostGravity != null && !boostGravity.isEmpty()) ? boostGravity : "0.00").append("</boostwithgravity>");
                break;
        }
        this.boostGravity = s.toString();
        return this;
    }

    ResultEntry setContentBlocks(Block[] contentBlocks)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<contentblocks>");
                for (Block block : contentBlocks) {
                    s.append("<block>").append(Result.forXML(block.getBlockContent())).append("</block>");
                }
                s.append("</contentblocks>");
                break;
        }
        this.contentBlocks = s.toString();
        return this;
    }

    ResultEntry setContentLength(String contentLength)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<contentlength>").append((contentLength != null && !contentLength.isEmpty()) ? contentLength : "0.00").append("</contentlength>");
                break;
        }
        this.contentLength = s.toString();
        return this;
    }

    ResultEntry setDigest(String digest)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<digest>").append((digest != null && !digest.isEmpty()) ? digest : "").append("</digest>");
                break;
        }
        this.digest = s.toString();
        return this;
    }

    ResultEntry setExtension(String extension)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<extension>").append((extension != null && !extension.isEmpty()) ? extension : "").append("</extension>");
                break;
        }
        this.extension = s.toString();
        return this;
    }

    ResultEntry setFetchTime(String fetchTime)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<fetchtime>").append((fetchTime != null && !fetchTime.isEmpty()) ? fetchTime : "0").append("</fetchtime>");
                break;
        }
        this.fetchTime = s.toString();
        return this;
    }

    ResultEntry setFileType(String fileType)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<filetype>").append((fileType != null && !fileType.isEmpty()) ? fileType : "").append("</filetype>");
                break;
        }
        this.fileType = s.toString();
        return this;
    }

    ResultEntry setGravity(String gravity)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<gravity>").append((gravity != null && !gravity.isEmpty()) ? gravity : "").append("</gravity>");
                break;
        }
        this.gravity = s.toString();
        return this;
    }

    ResultEntry setIndexTime(String indexTime)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<indextime>").append((indexTime != null && !indexTime.isEmpty()) ? indexTime : "0").append("</indextime>");
                break;
        }
        this.indexTime = s.toString();
        return this;
    }

    ResultEntry setLanguage(String language)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<language>").append((language != null && !language.isEmpty()) ? language : "").append("</language>");
                break;
        }
        this.language = s.toString();
        return this;
    }

    ResultEntry setLastModified(String lastModified)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<lastmodified>").append((lastModified != null && !lastModified.isEmpty()) ? lastModified : "0").append("</lastmodified>");
                break;
        }
        this.lastModified = s.toString();
        return this;
    }

    ResultEntry setSegment(String segment)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<segment>").append((segment != null && !segment.isEmpty()) ? segment : "").append("</segment>");
                break;
        }
        this.segment = s.toString();
        return this;
    }

    ResultEntry setSummary(String summary)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<summary>").append((summary != null && !summary.isEmpty()) ? Result.forXML(summary) : "").append("</summary>");
                break;
        }
        this.summary = s.toString();
        return this;
    }

    ResultEntry setTitle(String title)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<title>").append((title != null && !title.isEmpty()) ? Result.forXML(title) : "").append("</title>");
                break;
        }
        this.title = s.toString();
        return this;
    }

    ResultEntry setURL(String url)
    {
        StringBuilder s = new StringBuilder();
        switch (type) {
            case XML:
                s.append("<url>").append((url != null && !url.isEmpty()) ? Result.forXML(url) : "").append("</url>");
                break;
        }
        this.url = s.toString();
        return this;
    }
}
