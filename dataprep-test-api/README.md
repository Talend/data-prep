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
By default cucumber test will call the backend api on localhost. You can set another value with the "backend.api.url" parameter:
```
$ mvn clean test -Dbackend.api.url=http://backend.api.server.url
```

## Report

The cucumber report will be available on the target/cucumber directory

## License

Copyright (c) 2006-2015 Talend
