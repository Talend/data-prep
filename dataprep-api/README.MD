# Talend Data Preparation - API
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains the REST API service that provides all services and operation the Data Preparation user may need (i.e. 
all operations invoked by the Web UI). Compared to other "low" level services, this API service provides additional features such as:

* Orchestrating invocation of multiple services.
* Caching
* A fault tolerant service (e.g. in case of low level services failures).

## Prerequisites

You need Java *8* (or higher) and Maven 3.x (tested with 3.2.2 on Fedora 21 with OpenJDK 1.8.0_25-b18).

## Usage
To build and start an instance of the data set service, you just have to run this command:
```
$ mvn -Dserver.port=8080 clean spring-boot:run
```
This will start a server listening on port 8080 (you may customize the server port with the property server.port).
If no "server.port" argument is specified, it defaults to 8888.

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

## Data sets
A Data set is the starting point for data prep operations: it represents the initial data the user is going to work on.
You can create a data set from a CSV file for example. To create a data set, use:
```
POST http://localhost:8888/api/datasets?name=Finance%20report
```
This returns a data set (e.g. "c88b6a58-b91b-49ff-8d62-82c2630b1792").

## Preparations
A preparation is a succession of steps. In each step, there are actions (upper case, lower case...).

### Create a new preparation
A preparation starts from a data set. To create a preparation:
```
POST http://localhost:8888/api/preparations
```
The body of the POST must contain the id of the data set. For example, if you supplied "1234" as data set id for the preparation the id of the preparation
will be "948bed0012a5f13cd1ab93d51992f8952cbbd03b". The preparation id is built on a hash function that combines data set id and author.
You may get details for the preparation using:
```
GET http://localhost:8888/api/preparations/948bed0012a5f13cd1ab93d51992f8952cbbd03b/details
```
This returns a JSON document:
```
{
  "id": "948bed0012a5f13cd1ab93d51992f8952cbbd03b",
  "dataSetId": "1234",
  "author": "anonymousUser",
  "creationDate": 1427107513121,
  "steps": [
    "599725f0e1331d5f8aae24f22cd1ec768b10348d"
  ],
  "actions": []
}
```
You may note there's already a step ("599725f0e1331d5f8aae24f22cd1ec768b10348d") in the preparation. All preparations 
are created with a first step that basically means "no op" (no action performed).
Step's id is computed based on actions content, this means 599725f0e1331d5f8aae24f22cd1ec768b10348d always represents
no op.

### Get preparation's content
If you immediately call this after preparation creation:
```
GET http://localhost:8888/api/preparations/948bed0012a5f13cd1ab93d51992f8952cbbd03b/content 
```
This gives the content of data set "1234" with no action (no action was performed in preparation).
### Add a step to the preparation
You may add actions to the preparation using:
```
POST http://localhost:8888/api/preparations/948bed0012a5f13cd1ab93d51992f8952cbbd03b/actions 
```
Adding actions to a preparation is a "step". A step is *at least* an action on the data. The POST body contains the action you want to add such as:
```
{
  "actions": [
    {
      "action": "uppercase",
      "parameters": {
        "column_name": "firstname"
      }
    }
  ]
}
```
(This means a step can contain several actions as "actions" is an array).
If you call "GET http://localhost:8888/api/preparations/948bed0012a5f13cd1ab93d51992f8952cbbd03b/content", you will notice the "firstname" column is now in upper case.
### Get content at version
You can access to a preparation version using:
```
GET http://localhost:8888/api/preparations/948bed0012a5f13cd1ab93d51992f8952cbbd03b/content?version=599725f0e1331d5f8aae24f22cd1ec768b10348d
```
Since "599725f0e1331d5f8aae24f22cd1ec768b10348d" is always the id of the origin step in all preparations, this gives the preparation content before an action was added.

## Low level services URLs

This API service needs access to low level services such as Data Set Service and Transformation Service. Both services URLs are
declared in the configuration with the following properties:

* dataset.service.url: URL to the Data Set Service.
* transformation.service.url: URL to the Transformation Service.

This means you may start the API service with the following command to customize the location of the low level services:
```
$ mvn -Ddataset.service.url=http://localhost:8080/datasets -Dtransformation.service.url=http://localhost:8180/transform clean spring-boot:run
```
In this example, API service will expect Data Set Service operations to be in "http://localhost:8080/datasets" and all
Transformation Service operation in "http://localhost:8180/transform".

Although these URLs must be reachable, Data Set and Transformation Services are only called when necessary (starting low 
level services isn't actually a prerequisite but strongly advised if you wish to serve requests).

## Documentation
REST service is self documented. Once started, go to http://localhost:8888 (modify 8888 if you choose a different port)
and then expand 'datasets' category. You can explore and even test the REST interface from this web page.

## License

Copyright (c) 2006-2015 Talend
