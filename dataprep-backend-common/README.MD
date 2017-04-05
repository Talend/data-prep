# Talend Data Preparation - Backend common classes & utilities
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains common libraries for all backend Data Preparation modules.

## Common objects
A set of common objects shared by multiple services:
* Data set metadata
* Preparation

## i18n

i18n handling must be all the same across Data Preparation modules. This is possible thanks to package org.talend.dataprep.i18n.
To use i18n in a module you must:
* Have a file named `messages.properties` in the "resources/" folder of your module. Then declare a message.properties per locale (e.g. `messages_fr.properties`).
* To access messages use class org.talend.dataprep.i18n.MessageBundle

## Test

A extension for JSON file comparison is provided in package org.talend.dataprep.test

## License

Copyright (c) 2006-2015 Talend
