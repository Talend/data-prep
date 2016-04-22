/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import service', function() {
    'use strict';

    var importTypes = [
        {
            "locationType":"hdfs",
            "contentType":"application/vnd.remote-ds.hdfs",
            "parameters":[
                {
                    "name":"name",
                    "type":"string",
                    "implicit":false,
                    "canBeBlank":false,
                    "format":"",
                    "default":"",
                    "description":"Name",
                    "label":"Enter the dataset name:"
                },
                {
                    "name":"url",
                    "type":"string",
                    "implicit":false,
                    "canBeBlank":false,
                    "format":"hdfs://host:port/file",
                    "default":"",
                    "description":"URL",
                    "label":"Enter the dataset URL:"
                }
            ],
            "defaultImport":false,
            "label":"From HDFS",
            "title":"Add HDFS dataset"
        },
        {
            "locationType":"http",
            "contentType":"application/vnd.remote-ds.http",
            "parameters":[
                {
                    "name":"name",
                    "type":"string",
                    "implicit":false,
                    "canBeBlank":false,
                    "format":"",
                    "default":"",
                    "description":"Name",
                    "label":"Enter the dataset name:"
                },
                {
                    "name":"url",
                    "type":"string",
                    "implicit":false,
                    "canBeBlank":false,
                    "format":"http://",
                    "default":"",
                    "description":"URL",
                    "label":"Enter the dataset URL:"
                }
            ],
            "defaultImport":false,
            "label":"From HTTP",
            "title":"Add HTTP dataset"
        },
        {
            "locationType":"local",
            "contentType":"text/plain",
            "parameters":[
                {
                    "name":"datasetFile",
                    "type":"file",
                    "implicit":false,
                    "canBeBlank":false,
                    "format":"*.csv",
                    "default":"",
                    "description":"File",
                    "label":"File"
                }
            ],
            "defaultImport":true,
            "label":"Local File",
            "title":"Add local file dataset"
        },{
            "locationType": "job",
            "contentType": "application/vnd.remote-ds.job",
            "parameters": [
                {
                    "name": "name",
                    "type": "string",
                    "implicit": false,
                    "canBeBlank": false,
                    "format": "",
                    "description": "Name",
                    "label": "Enter the dataset name:",
                    "default": ""
                },
                {
                    "name": "jobId",
                    "type": "select",
                    "implicit": false,
                    "canBeBlank": false,
                    "format": "",
                    "configuration": {
                        "values": [
                            {
                                "value": "1",
                                "label": "TestInput"
                            }
                        ],
                        "multiple": false
                    },
                    "description": "Talend Job",
                    "label": "Select the Talend Job:",
                    "default": ""
                }
            ],
            "defaultImport": false,
            "label": "From Talend Job",
            "title": "Add Talend Job dataset"
        }
    ];

    beforeEach(angular.mock.module('data-prep.services.import'));

    beforeEach(inject(function($q, ImportRestService) {
        spyOn(ImportRestService, 'importTypes').and.returnValue($q.when({data: importTypes}));
    }));

    it('should fetch import types list from REST call', inject(function($rootScope, ImportService, StateService) {
        //given
        spyOn(StateService, 'setImportTypes');

        //when
        ImportService.initImport();
        $rootScope.$digest();

        //then
        expect(StateService.setImportTypes).toHaveBeenCalledWith(importTypes);
    }));

});