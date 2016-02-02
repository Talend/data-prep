# Talend Data Preparation Free Desktop
http://www.talend.com


![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend")

This repository contains the source files for Talend Data Preparation Free Desktop.

These files must be used together with the common code contained in [daikon](https://github.com/Talend/daikon) and [data quality](https://github.com/Talend/data-quality).

### Overview

[Folders description](#1-folders-description) | [Download](#2-download) | [Documentation](#3-usage-and-documentation) | [Support](#4-support) | [Contributing](#5-contributing) | [License](#6-license)

## 1. Folders description
| _Project_                                          | _Description_                                                        |
|:---------------------------------------------------|----------------------------------------------------------------------|
| [dataprep-api](dataprep-api)                       | *rest service on top of transformation, preparation & dataset, used by webapp*    |
| [dataprep-backend-common](dataprep-backend-common) | *set of common objects shared by multiple services*                  |
| [dataprep-backend](dataprep-backend)               | *parent project for all backend projects*                            |
| [dataprep-dataset](dataprep-dataset)               | *rest service to manage datasets (import, export, sample, stats)*    |
| [dataprep-preparation](dataprep-preparation)       | *rest service to manage preparations (create, list steps, get data)* |
| [dataprep-transformation](dataprep-transformation) | *rest service to apply transformations (actions are here)*           |
| [dataprep-webapp](dataprep-webapp)                 | *web app (AngularJS)*                                                |
| [tooling](tooling)                                 | *IDE specific config files + some other stuff*                       |

### UI

The following project provides UI for Data Prep.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-webapp](dataprep-webapp)         | *Builds all the AngularJS UI of Data Prep* |

### Back-end

#### Services
The following projects provide back-end services exposed as REST API.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-api](dataprep-api)         | *Back-end API service for Data Preparation Web UI.* |
| [dataprep-transformation](dataprep-transformation)         | *Back-end service for dataset transformation.* |
| [dataprep-dataset](dataprep-dataset)         | *Back-end service for dataset operations (create, update, content index, get dataset metadata...)* |
| [dataprep-preparation](dataprep-preparation)         | *Back-end service for preparation operations (create, update, get content, add or edit steps in preparations...)* |


#### Common and module management
The following projects don't build any back-end artifacts but provide build utilities.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-backend](dataprep-backend)          | *Common POM for all back-end modules (defines all libraries & versions, as well as common build behaviors)* |
| [dataprep-backend-common](dataprep-backend-common)         | *Common resources and classes for back-end modules (CORS Filter, Branded Swagger UI...)* |



## 2. Download

You can download this product from the [Talend website](http://www.talend.com/download/talend-open-studio?qt-product_tos_download_new=5&utm_source=github&utm_campaign=dataprep).


## 3. Usage and Documentation

Documentation is available on [Talend Help Center](http://help.talend.com/).



## 4. Support 

You can ask for help on our [Forum](http://www.talend.com/services/global-technical-support).



## 5. Contributing

We welcome contributions of all kinds from anyone.

Using the bug tracker [Talend bugtracker](http://jira.talendforge.org/) is the best channel for bug reports, feature requests and submitting pull requests.


### Build
- All project are maven based.
- A parent pom builds the web-app and its dependencies.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).
- See each module (e.g. [dataprep-dataset](/dataprep-dataset/)) for specific build instructions.


### IDE setup
See the [tooling](/tooling/) folder.



## 6. License

Copyright (c) 2006-2016 Talend

Licensed under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt)
