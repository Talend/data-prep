# Talend Data Preparation - Transformation Service
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains the REST service to all Transformation operations.

## Prerequisites

You need Java *8* (or higher) and Maven 3.x (tested with 3.2.2 on Fedora 21 with OpenJDK 1.8.0_25-b18).

## Usage
To build and start an instance of the data set service, you just have to run this command:
```
$ mvn -Dserver.port=8080 clean spring-boot:run
```
This will start a server listening on port 8080 (you may customize the server port with the property server.port).
If no "server.port" argument is specified, it defaults to 8180.

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

## Documentation
REST service is self documented. Once started, go to http://localhost:8080 (modify 8080 if you choose a different port)
and then expand 'datasets' category. You can explore and even test the REST interface from this web page.

## License

Copyright (c) 2006-2015 Talend
