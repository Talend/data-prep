
#![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend") Data Preparation Project  

## Folders description
| _Project_                                          | _Description_                                                        |
|:---------------------------------------------------|----------------------------------------------------------------------|
| [dataprep-api](dataprep-api)                       | *rest service on top of transformation & dataset, used by webapp*    |
| [dataprep-backend-common](dataprep-backend-common) | *set of common objects shared by multiple services*                  |
| [dataprep-backend](dataprep-backend)               | *parent project for all backend projects*                            |
| [dataprep-dataset](dataprep-dataset)               | *rest service to manage datasets (import, export, sample, stats)*    |
| [dataprep-preparation](dataprep-preparation)       | *rest service to manage preparations (create, list steps, get data)* |
| [dataprep-transformation](dataprep-transformation) | *rest service to apply transformations (actions are here)*           |
| [dataprep-webapp](dataprep-webapp)                 | *web app (AngularJS)*                                                |
| [tooling](tooling)                                 | *IDE specific config files + some other stuff*                       |

### UI

The following projects provides UI for Data Prep.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-webapp](dataprep-webapp)         | *Builds all the AngularJS UI of Data Prep* |

### Back-end

#### Services
The following projects provides back-end services exposed as REST API.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-api](dataprep-api)         | *Back-end API service for Data Preparation Web UI.* |
| [dataprep-transformation](dataprep-transformation)         | *Back-end service for data set transformation.* |
| [dataprep-dataset](dataprep-dataset)         | *Back-end service for data set operations (create, update, content index, get data set metadata...)* |
| [dataprep-preparation](dataprep-preparation)         | *Back-end service for preparation operations (create, update, get content, add or edit steps in preparations...)* |


#### Common and module management
The following projects don't build any back-end artifacts but provide build utilities.

| _Project_                  | _Description_ |
|:-------------------------|-------------|
| [dataprep-backend](dataprep-backend)          | *Common POM for all back-end modules (defines all libraries & versions, as well as common build behaviors)* |
| [dataprep-backend-common](dataprep-backend-common)         | *Common resources and classes for back-end modules (CORS Filter, Branded Swagger UI...)* |


## Build
- All project are maven based.
- A parent in pom build the web-app and its dependencies.
- Specific Maven settings are required. See instructions in [tooling](/tooling/).
- See each module (e.g. [dataprep-dataset](/dataprep-dataset/)) for specific build instructions.

## IDE setup
See the [tooling](/tooling/) folder.
