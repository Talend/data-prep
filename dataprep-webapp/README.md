# Talend Data-prep web-application aka Talend Data Shaker
## How to run
### Prerequisites
1. nodejs : http://nodejs.org/
2. npm : it should be install with the above node
3. git clone git@github.com:Talend/data-prep.git in _checkouts_ (adapt others commands if clone in another folder)
4. go to _checkouts/data-prep/dataprep-webapp_ and type the following command

`npm install`

This will install all the dev package for dataprep as well as third party libraries required by this application.

### Root file structure

<pre>
├── build/                                  - untracked generated folder where you find minified build in prod mode and unminified in dev mode
├── config/                                 - untracked generated folder where you find build config
├── coverage/                               - untracked generated folder where karma istanbul plugin will put coverage files
├── docker/                                 - dockefile and scripts to generate the docker image 
├── node_modules/                           - untracked generated folder for gulp build node modules
├── src/                                    - sources
├── target/                                 - untracked generated folder for maven build
├── karma.conf.js                           - unit testing configuration
├── package.json                            - npm description for build dependencies
├── pom.xml                                 - maven description file
├── README.md                               - this file
└── spec.bundle.js                          - entry file to load unit tests from Karma
</pre>


### Sources file structure
The source file structure is based on the [Best Practice Recommendations for Angular App Structure](https://docs.google.com/document/d/1XXMvReO8-Awi1EZXAXS4PzDzdNvV6pGcuaF4Q9821Es/pub), except for css files that have their own structure

<pre>
├── assets                                      - assets source folder
│   ├── fonts                                   - fonts folder
│   └── images                                  - images folder
│
├── i18n                                        - translation folder
│   ├── en.json                                 - english translation files
│   └── ...                                     - other Enterprise edition translation files
│
├── mocks                                       - unit tests mocks
│
├── app                                         - app sources
│   ├── components                              - components folder
│       ├── my-comp                             - component folder
│           ├── my-comp.html                    - component template
│           ├── my-comp-controller.js           - component controller
│           ├── my-comp-controller.spec.js      - component controller unit tests
│           ├── my-comp-directive.js            - component directive
│           ├── my-comp-directive.spec.js       - component directive unit tests
│           └── my-comp-module.js               - component module definition
│
│       ├── widgets                             - Reusable components folder
│       ├── ...                                 - other components
│
│
│   ├── services                                - common services source folder
│       ├── my-feature                          - service folder
│           ├── my-feature-module.js            - service module definition
│           ├── my-feature-service.js           - service implementation
│           ├── my-feature-service.spec.js      - service unit tests
│           ├── ...                             - other my-feature services
│
│   ├── css                                     - style folder
│       ├── base                                - base styles (reset, typography, ...)
│           ├── _base.scss                      - file that only imports all base styles
│           └── ...                             - other base styles, imported in _base.scss
│       ├── components                          - components styles (Buttons, Carousel, Cover, Dropdown, ...)
│           ├── _components.scss                - file that only imports all common components styles
│           └── ...                             - other components styles, imported in _components.scss
│       ├── layout                              - layout styles (Navigation, Grid system, Header, Footer, Sidebar, Forms, ...)
│           ├── _layout.scss                    - file that only imports all layout styles
│           └── ...                             - other layout styles, imported in _layout.scss
│       ├── pages                               - pages specific styles (home page, ...)
│           ├── _pages.scss                     - file that only imports all pages styles
│           └── ...                             - other pages styles, imported in _pages.scss
│       ├── utils                               - utils styles (Mixins, Colors, ...)
│           ├── _utils.scss                     - file that only imports all utils styles
│           └── ...                             - other utils styles, imported in _utils.scss
│       └── vendors                             - vendors styles (third party frameworks)
│           ├── _vendors.scss                   - file that only imports all vendors styles
│           └── ...                             - other vendors styles, imported in _vendors.scss
│
│   ├── index.scss                              - main scss that only imports _base.scss, _components.scss, _layout.scss, _pages.scss, _utils.scss, _vendors.scss
│   ├── index-module.js                         - main module
│   └── index-module.spec.js                    - main module config unit tests
│
└── index.html                                  - main page
</pre>


### Code style
Interesting style guides to follow : 
* Todd Motto : https://github.com/toddmotto/angularjs-styleguide
* John Papa : https://github.com/johnpapa/angular-styleguide

### Run
when in folder _checkouts/data-prep/dataprep-webapp_ type the command

`npm run serve`

Then it will start a web server on port 3000 and watch any code change to refresh the browser. 

### Alternative way to launch, this does't require the prerequisites.

`docker run -v /home/smallet/Development/clones/data-prep/dataprep-webapp/:/data-prep/ -p 3000:3000 registry.talend.com:5000/talend/data-prep-webapp-tools:1.8 /bin/sh -c "npm run serve"`

Of course change */home/smallet/Development/clones/data-prep/dataprep-webapp* regarding the path of your git clone.

Then application is available at http://localhost:3000

### Run on your local machine
By default, the webapp is setup to access the api hosted on `10.42.10.99`. This setting can be changed in the following file :

* _src/assets/config/config.mine.json_.

This file is an untracked file that overwrites the configuration in config.json.

### Run tests
when in folder _checkouts/data-prep/dataprep-webapp_ type the command

`npm run test`

This will get all source files, include them in karma config and run all the unit tests only once.
To run it continuously with source watch, type the command

`npm run test:auto`

### Test coverage
During each test run, Karma will generate coverage files, using [karma-coverage plugin](https://github.com/karma-runner/karma-coverage).
Open the index.html in the coverage folder to display coverage details for each js file.

### Code documentation
On each entity (controller, directive, function, module, ...) creation and modification, the ngDoc must be updated.
For more information about how to write ngDoc :
* https://github.com/angular/angular.js/wiki/Writing-AngularJS-Documentation
* https://github.com/angular/dgeni-packages/blob/master/NOTES.md

### Build a standalone lib distrib (not minified)
run
`npm run build`

### Build a standalone prod distrib
run
`npm run build:dist`

##Maven profiles
The build and test can be executed using maven as well here are the different maven profile avaialble.

###-P dev (default)
The default maven profile called *dev* that launches all the necessary tasks for building and testing the app with the usuall maven phases : *test* *package*.
This profile assumes that all the tooling is installed on the current machine.

###-Duse.docker.tool=true
The profile name *ci* is triggered using this property. It is used for continuus integration build on our jenkins server. This build is using a docker image installed with all the required tooling because it was too much of a pain to install the tooling directly on the jenkins server. This allows for installing on other servers easilly too.
The docker image is build from the Dockerfile : [docker/Dockerfile-for-dev-tools](docker/Dockerfile-for-dev-tools)

###-P docker
The docker profile adds the build of a docker image to the packaging maven phase.
This image is launching the nginx web server with the webapp on the port 80. It is build using the Docker file [docker/Dockerfile](docker/Dockerfile).
You may launch the docker image manually once it is built using this command : `docker run -d -p 80:80 -e TDP_API_HOST="host_for_api_service" -e TDP_API_PORT="port_for_api_service" talend/dataprep-webapp:<pom_version>`
