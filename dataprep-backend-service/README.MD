# Talend Data Preparation - Backend common classes & utilities
![alt text](http://www.talend.com/sites/all/themes/talend_responsive/images/logo.png "Talend")

This folder contains common libraries for all backend Data Preparation modules.

## Common objects
A set of common objects shared by multiple services:
* Data set metadata
* Preparation

## Common Spring configuration

All common Spring configurations are provided by package org.talend.dataprep.configuration. It provides:
* Common security configuration.
* Custom serialization of objects to JSON.
* A shared servlet filter to add CORS header to each response.

## Exception handling

The org.talend.dataprep.exception package contains a reusable way to convert a server side exception to a common exception (with error code).
This package provides an aspect that can wrap any method using @RequestMapping (any operation method in the REST services).

## i18n

i18n handling must be all the same across Data Preparation modules. This is possible thanks to package org.talend.dataprep.i18n.
To use i18n in a module you must:
* Have a file named `messages.properties` in the "resources/" folder of your module. Then declare a message.properties per locale (e.g. `messages_fr.properties`).
* To access messages use class org.talend.dataprep.i18n.MessageBundle

## Test

A extension for JSON file comparison is provided in package org.talend.dataprep.test

## License

Copyright (c) 2006-2015 Talend