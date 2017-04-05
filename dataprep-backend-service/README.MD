# Talend Data Preparation - Backend common classes & utilities
![alt text](https://www.talend.com/wp-content/uploads/2016/07/talend-logo.png "Talend")

This folder contains common libraries for all backend Data Preparation modules.

## Common objects
A set of common objects shared by multiple services:
* Data set metadata
* Preparation

## Exception handling

The org.talend.dataprep.exception package contains a reusable way to convert a server side exception to a common exception (with error code).
This package provides an aspect that can wrap any method using @RequestMapping (any operation method in the REST services).

## License

Copyright (c) 2006-2015 Talend
