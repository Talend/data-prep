# Talend Data Preparation - Data Set Service
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains the REST service to all Data Set related operations (creation, removal...).

## Prerequisites

You need Java *8* (or higher) and Maven 3.x (tested with 3.2.2 on Fedora 21 with OpenJDK 1.8.0_25-b18).

## Usage
To build and start an instance of the data set service, you just have to run this command:
```
$ mvn -Dserver.port=8080 clean spring-boot:run
```
This will start a server listening on port 8080 (you may customize the server port with the property server.port).
If no "server.port" argument is specified, it defaults to 8080.

It is also possible to start on a random port:
```
$ mvn -Dserver.port=0 clean spring-boot:run
```
You should look in the console the line that indicates the port:
```
$ mvn -Dserver.port=0 clean spring-boot:run
... (many lines omitted) ...
2014-12-31 10:27:04.499  INFO 8426 --- [lication.main()] s.b.c.e.t.TomcatEmbeddedServletContainer : Tomcat started on port(s): 58996/http
2014-12-31 10:27:04.501  INFO 8426 --- [lication.main()] org.talend.dataprep.dataset.Application  : Started Application in 7.297 seconds (JVM running for 10.494)
```
(in this example, server started on port 58996).

## Data set information storage

By default (for ease of deployment), all data set related information is stored in memory (information such as id, column metadata...).
You may switch to a local Mongo DB instance with the property "dataset.metadata.store".
```
$ mvn -Ddataset.metadata.store=mongodb clean spring-boot:run
```
Look for this line in the log:
```
2015-01-06 12:03:57.459  INFO 13798 --- [lication.main()] o.t.d.d.c.DataSetStoreConfiguration      : Data Set metadata store: mongodb
```
(it defaults to "in-memory" value when nothing is specified).

By default, service looks for a Mongo DB instance running on localhost and default port. You may override these settings using properties
during startup. For example, the following command will connect to a Mongo DB instance running on "mongoserver" and port "27017":
```
$ mvn -Ddataset.metadata.store=mongodb -Dspring.data.mongodb.host=mongoserver -Dspring.data.mongodb.port=27017 clean spring-boot:run
```

## Data set content storage

You can choose between 2 content storage:
* local: stores content to <i>${java.io.tmpdir}/talend/tdp/datasets/</i>. You may customize this path using the property "dataset.content.store.file.location" as follows:
```
$ mvn -Ddataset.content.store.file.location=/home/user/permanent_store/
```
* hdfs (experimental):
```
$ mvn -Ddataset.content.store=hdfs -Ddataset.content.store.hdfs.location=file:/
```
In this configuration, dataset.content.store.hdfs.location takes a URI to initialize a HDFS FileSystem instance (currently no credential support).

## Documentation
REST service is self documented. Once started, go to http://localhost:8080 (modify 8080 if you choose a different port)
and then expand 'datasets' category. You can explore and even test the REST interface from this web page.

## License

Copyright (c) 2006-2015 Talend
