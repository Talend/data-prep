# Talend Data Preparation OS - Cucumber tests
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains the Data Preparation API cucumber tests.

## Prerequisites

You need Java *8* (or higher), Maven 3.x. and org.talend.dataprep:dataprep-api (test-jar) in your maven repository

## Launch integration tests
There are different ways to run Cucumber integration tests:
* Launch a maven test goal on `dataprep-test-api` project
* Launch `OSRunnerConfigurationIT` class as a JUnit test in your favorite IDE
* Use an IDE-dependent Cucumber plugin to launch a specific feature file.

_Note :
Data-prep backend service is configured in `dataprep-test-api/application.properties` file.
Be sure to adapt it to your own configuration before launching the integration tests._

### Maven launch
To launch all cucumber tests, you have to call the maven `verify` phase with the `skipITs` property set to `false`.
```
$ mvn verify -DskipITs=false
```

To launch cucumber tests *alone*, you can call directly the maven-failsafe-plugin dedicated goal, along with the `skipITs` property set to `false`:
```
mvn failsafe:integration-test -DskipITs=false
```

It is possible to launch a specific test by specifying it in the command line:
```
$ mvn verify -DskipITs=false -Dcucumber.options="classpath:features/ExportPreparationFromCSV.feature"
```

It is also possible to launch specific tests by specifying cucumber tags:
```
$ mvn verify -DskipITs=false -Dcucumber.options="--tags @LiveDataSet"
```

By default cucumber tests call the backend api on `http://dev.data-prep.talend.lan:8888`.
By default cucumber tests call the backend api on `http://localhost:8888`.
You can set another url value by using the following maven parameter:
```
$ mvn verify -DskipITs=false -Dbackend.api.url=http://dev.data-prep.talend.lan:8888
```
Available keys are:
* ``backend.api.url`` : to specify the global api base url
* ``restassured.debug`` : to switch on RestAssured library debug logs (default value : false)

## Report
The default cucumber report will be available in the `dataprep-test-api/target/cucumber` directory.
The full cucumber report will be available in `dataprep-test-api/target/site/cucumber-reports`, if you run the usual command line as follow:

```
$ mvn verify -DskipITs=false
```


## Adding new features

### Using and cleaning context
Some of data-prep items such as datasets, preparations, folders, etc.
are stored in a temporary local context specific to a feature (see `GlobalStep.java`).
This context is used for two reasons:
* Sharing references to items between scenarios and steps within a feature
* Cleaning the environment at the end of a feature to keep the tests re-entrant

For this last functionality, it is mandatory to add the following annotation on the last scenario of your new feature:
```
@CleanAfter
```
The ``@CleanAfter`` annotation calls a cleaning procedure in the test environment to delete all the created stuff in the right order.

### Writing new Java steps (classes inheriting from `DataPrepStep`)
* Don't forget to register any created item in the context in order to have it cleaned by ``@CleanAfter``
* As the values passed to the folder API differs depending on the environment, always use ``folderUtil`` functions first instead of direct ``OSDataPrepAPIHelper`` calls, in order to keep your features runnable in an on premise environment.

### How to support a new kind of sample export for Integration Tests
[Read the wiki !]('http://wiki.talend.com/display/rd/How+to+support+a+new+kind+of+sample+export+for+Integration+Tests')

## License
Copyright (c) 2006-2018 Talend
