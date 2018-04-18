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

### Writing new Java steps
* New Java steps must inherit from `DataPrepStep`
* They must follow the good practices described below
* Don't forget to register any created item in the context in order to have it cleaned by ``@CleanAfter``
* As the values passed to the folder API differs depending on the environment, always use ``folderUtil`` functions first instead of direct ``OSDataPrepAPIHelper`` calls, in order to keep your features runnable in an on premise environment.
* By default new features should be tagged with `@EnvOs @EnvOnPremise @EnvCloud`
* If a feature is known to be OS environment dependent it must be tagged only with `@EnvOs` 

### Good practices for writing new Java steps
#### Step Atomicity
When creating a new step always try to keep the step atomicity and avoiding hidden dependency.
This mean a step should not depend silently of the previous execution of another step.
It can be avoided by naming created items with aliases a creating an explicit dependency in further steps by asking the item name to work with (see examples below).

Bad example : 
```
Given I upload the dataset "/data/6L3C.csv"
And I create a preparation
And I add a "uppercase" step with parameters :
    | column_name      | lastname         |
    | column_id        | 0001             |
```
This example works, but isn't easily reusable :
* Just by reading the test we cannot tell without any doubt if it's possible to create a preparation without a dataset.
* If we create a preparation based on `6L3C.cvs` dataset and another based on `10L3C.csv` dataset, do we need to reimport the `6L3C.csv` dataset in order to create a third preparation based on it ?
* We may want to create two preparations and then add a preparation step on the first one, with the current steps it's not possible : we'll have to create the first preparation, then add its step, then create the second one, which is slightly different.    

Good example :
```
Given I upload the dataset "/data/6L3C.csv" with name "6L3C_dataset"
And I create a preparation with name "6L3C_preparation", based on "6L3C_dataset" dataset
And I add a "uppercase" step on the preparation "6L3C_preparation" with parameters :
    | column_name      | lastname         |
    | column_id        | 0001             |
```
While this example seems more complex, it makes the integration tests writing easier :
* Just by reading the test I know that I need a dataset to create a preparation (as it's a required parameter for the create preparation step) : the dependency isn't hidden.
* It's also obvious that a step apply on a preparation. 
* The dataset based on 6L3C.csv file can be used to create various preparation even after having uploaded other datasets (as we can refer to it through its alias "6L3C_dataset").     
* The use of aliases give the possibility to add more contextual information (for example the preparation could have been named `6L3C_preparation_to_export_in_xlsx`)

#### Using and cleaning context
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

### How to support a new kind of sample export for Integration Tests
[Read the wiki : How to support a new kind of sample export for Integration Tests]('https://in.talend.com/19139704')

## License
Copyright (c) 2006-2018 Talend
