# Talend Data Preparation - Cucumber test
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains the cucumber test of the Data Preparation API

## Prerequisites

You need Java *8* (or higher), Maven 3.x. and dataprep-api jar in your maven repository

## Usage
To launch all cucumber test, you have to call the test phase of maven. It will launch both OS ans EE cucumber test
```
$ mvn test
```
You can launch only one test by specify it in the command line:
```
$ mvn test -Dcucumber.options="classpath:features/os/ExportPreparation.feature"
```
By default cucumber test will call the backend api on localhost:8888. You can set another url value with the maven parameter:
```
$ mvn clean test -DmyKey=http://backend.api.server.url
```

Available key are:

* backend.global.api.url : to specify the global api base url
* backend.upload.api.url : to specify the upload api base url
* backend.export.api.url : to specify the export api base url

## Report

The default cucumber report will be available on the target/cucumber directory. If you want a more lisible cucumber report just launch the command line:

```
$ mvn test verify
```

The cucumber report will be available on /site/cucumber-reports

## Adding new features
Don't forget to add the following annotation on the last scenario of your new feature : 
```
@CleanAfter
```
All created datasets, preparations, steps, folders are stored in a temporary context in order to be accessible to all scenarios within the same feature.
The @CleanAfter annotation just call a cleaning procedure in the test environment to delete all the created stuff in the right order.  

## License

Copyright (c) 2006-2017 Talend
