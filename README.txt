****** Quary (Distributed Wrapper for Apache Lucene) ******

INTRODUCTION
Quary is a distributed and interconnected wrapper for Apache Lucene, it works by
using indexing nodes connected to a central named *Brain*. The way documents
are added to the nodes is via a *Reactor*, which is just a NIO server using the
Reactor Pattern. This server receives a stream of XML (Documents for indexing 
must be XML) documents for indexing and de-multiplexes each document and sends
it to an appropriate node.
Each node is connected or registered with the *Brain*, which is responsible
of managing all nodes; it means it should detect if a node is down and remove
it from the list, while holding the indexing job until the node is restored.
This is basically the job of the *Brain*.

FAQ
How indexes are defined?
You use an XML document to define the structure of your index. An example can be
found in the file */resources/definitions/TestDefinition.xml*.

Can I have more than one index?
Yes, once the *Reactor* is ready (still not supporting parallel index nor 
searching), parallel indexing and searching will be available.

CURRENT WORK
    The source code for Quary can be found here: https://github.com/k-zen/Quary

FILES
    lib/*
        3rd party libraries needed by the software.
    src/*
        Source code.
    test/*
        Test files and classes.

DOCUMENTATION
    Work in progress.

ADDITIONAL LIBRARIES
    There are a number of additional libraries that various parts of Quary may depend. These are  
    included in the source distribution.
    Libraries:
        - Apache Commons Codec 1.6
        - Apache Commons Collections 3.2.1
        - Apache Commons Configuration 1.10
        - Apache Commons Lang 2.4
        - Apache Commons Logging 1.2
        - Guava 14.0.1
        - Apache Hadoop Annotations 2.6.0
        - Apache Hadoop Auth 2.6.0
        - Apache Hadoop Common 2.6.0
        - Apache Htrace Core 3.0.4
        - Apache Log4j 1.2.17
        - Apache Lucene Analyzers Common 4.6.0
        - Apache Lucene Core 4.6.0
        - Apache Lucene Misc 4.6.0
        - Apache Lucene Queries 4.6.0
        - Apache Lucene QueryParser 4.6.0
        - PrettyTime 1.0.8
        - ProtoBuf 2.6.1
        - SLF4 API 1.6.1
        - SLF4 Log4j 1.6.1