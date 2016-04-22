/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Import controller', function () {
    'use strict';

    var ctrl, createController, scope, StateMock;
    var dataset = {id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false};

    beforeEach(angular.mock.module('data-prep.import', function ($provide) {
        StateMock = {
            inventory: {
                currentFolder: {id : '', path: '', name: 'Home'},
                currentFolderContent: {
                    folders: [],
                    datasets: []
                }
            }, import: {
                importTypes:[
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
                ]
            }
        };
        $provide.constant('state', StateMock);
    }));

    beforeEach(inject(function ($rootScope, $componentController) {
        scope = $rootScope.$new();

        createController = function () {
            return $componentController(
                'import',
                {$scope: scope}
            );
        };
    }));

    afterEach(inject(function() {
        dataset.error = false;
        dataset.progress = false;
    }));

    describe('start default import type', function () {

        it('should call the first import type if no defaultImportType', function () {
            //Given
            StateMock.import.importTypes[2].defaultImport = false;

            //when
            ctrl = createController();
            spyOn(ctrl, 'startImport');
            ctrl.startDefaultImport();

            ////then
            expect(ctrl.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[0]);
        });

        it('should call the default import type', inject(function () {

            //Given
            StateMock.import.importTypes[2].defaultImport = true;

            //when
            ctrl = createController();
            spyOn(ctrl, 'startImport');
            ctrl.startDefaultImport();

            ////then
            expect(ctrl.startImport).toHaveBeenCalledWith(StateMock.import.importTypes[2]);

        }));
    });

    describe('start import', function () {

        it('should start import from local file', function () {
            //when
            ctrl = createController();
            ctrl.startImport(StateMock.import.importTypes[2]);

            ////then
            expect(ctrl.showModal).toBe(false);
        });

        it('should start import from remote', inject(function () {
            //when
            ctrl = createController();
            ctrl.startImport(StateMock.import.importTypes[0]);

            ////then
            expect(ctrl.showModal).toBe(true);

        }));
    });


    describe('import', function () {
        var uploadDefer;
        beforeEach(inject(function (StateService, $q, DatasetService, UploadWorkflowService, FolderService) {

            ctrl = createController();
            ctrl.datasetFile = [{name: 'my dataset.csv'}];
            ctrl.datasetName = 'my cool dataset';

            uploadDefer = $q.defer();
            uploadDefer.promise.progress = function (callback) {
                uploadDefer.progressCb = callback;
                return uploadDefer.promise;
            };

            spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
            spyOn(UploadWorkflowService, 'openDataset').and.returnValue();
            spyOn(DatasetService, 'createDatasetInfo').and.callFake(function() {
                return dataset;
            });
            spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
            spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

            spyOn(StateService, 'startUploadingDataset').and.returnValue();
            spyOn(StateService, 'finishUploadingDataset').and.returnValue();

            spyOn(FolderService, 'getContent').and.returnValue();

            ctrl.currentInputType = StateMock.import.importTypes[0];
        }));

        it('should show dataset name popup when name already exists', inject(function (DatasetService) {
            //Given
            var dataset = {
                name: 'my dataset'
            };
            spyOn(DatasetService, 'getDatasetByName').and.returnValue(dataset);
            expect(ctrl.datasetNameModal).toBeFalsy();

            //when
            ctrl.import(StateMock.import.importTypes[0]);

            ////then
            expect(ctrl.datasetNameModal).toBe(true);
        }));

        it('should create dataset if unique', inject(function (DatasetService) {
            //Given
            spyOn(DatasetService, 'getDatasetByName').and.returnValue(null);
            expect(ctrl.datasetNameModal).toBeFalsy();


            //when
            ctrl.import(StateMock.import.importTypes[0]);

            ////then
            expect(ctrl.datasetNameModal).toBeFalsy();

            var paramsExpected = { name: 'my dataset', url: '', type: 'hdfs' };
            expect(DatasetService.create).toHaveBeenCalledWith(StateMock.inventory.currentFolder, paramsExpected, 'application/vnd.remote-ds.hdfs', {name: 'my dataset.csv'});

        }));
    });

    describe('upload dataset name', function () {
        var uploadDefer;

        beforeEach(inject(function (StateService, $q, DatasetService, UploadWorkflowService, FolderService) {

            ctrl = createController();
            ctrl.datasetFile = [{name: 'my dataset.csv'}];
            ctrl.datasetName = 'my cool dataset';

            uploadDefer = $q.defer();
            uploadDefer.promise.progress = function (callback) {
                uploadDefer.progressCb = callback;
                return uploadDefer.promise;
            };

            spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
            spyOn(UploadWorkflowService, 'openDataset').and.returnValue();
            spyOn(DatasetService, 'createDatasetInfo').and.callFake(function() {
                return dataset;
            });
            spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
            spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

            spyOn(StateService, 'startUploadingDataset').and.returnValue();
            spyOn(StateService, 'finishUploadingDataset').and.returnValue();

            spyOn(FolderService, 'getContent').and.returnValue();
        }));
        describe('with unique name', function () {

            beforeEach(inject(function ($q, $rootScope, DatasetService) {
                spyOn(DatasetService, 'getDatasetByName').and.returnValue(null);

                spyOn($rootScope, '$emit').and.returnValue();

                ctrl.currentInputType = StateMock.import.importTypes[0];
            }));

            it('should create dataset if name is unique', inject(function (StateService, $q, $rootScope, DatasetService, UploadWorkflowService, FolderService) {
                //given
                ctrl.uploadDatasetName();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();

                //when
                uploadDefer.resolve({data: dataset.id});
                scope.$digest();

                var paramsExpected = { name: 'my cool dataset', url: '', type: 'hdfs' };
                //then
                expect(DatasetService.create).toHaveBeenCalledWith(StateMock.inventory.currentFolder, paramsExpected, 'application/vnd.remote-ds.hdfs', {name: 'my dataset.csv'});
                expect(FolderService.getContent).toHaveBeenCalled();
                expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
                expect(StateService.finishUploadingDataset).toHaveBeenCalled();
            }));

            it('should update progress on create', inject(function (state, StateService, DatasetService) {
                //given
                ctrl.uploadDatasetName();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();
                expect(dataset.progress).toBeFalsy();

                var event = {
                    loaded: 140,
                    total: 200
                };

                //when
                uploadDefer.progressCb(event);
                scope.$digest();

                //then
                expect(DatasetService.create).toHaveBeenCalled();
                expect(dataset.progress).toBe(70);
            }));

            it('should set error flag and show error toast', inject(function (StateService, DatasetService) {
                //given
                ctrl.uploadDatasetName();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();
                expect(dataset.error).toBeFalsy();

                //when
                uploadDefer.reject();
                scope.$digest();

                //then
                expect(DatasetService.create).toHaveBeenCalled();
                expect(dataset.error).toBe(true);
            }));
        });

        describe('with existing name', function () {
            var dataset = {
                name: 'my cool dataset'
            };
            var confirmDefer;

            beforeEach(inject(function ($rootScope, $q, StateService, DatasetService, UpdateWorkflowService, TalendConfirmService) {
                confirmDefer = $q.defer();

                spyOn(StateService, 'resetPlayground').and.returnValue();
                spyOn(DatasetService, 'getDatasetByName').and.returnValue(dataset);
                spyOn(DatasetService, 'getUniqueName').and.returnValue('my cool dataset (1)');
                spyOn(TalendConfirmService, 'confirm').and.returnValue(confirmDefer.promise);
                spyOn($rootScope, '$emit').and.returnValue();
                spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue($q.when());

                ctrl.currentInputType = StateMock.import.importTypes[0];

            }));

            it('should do nothing on confirm modal dismiss', inject(function (TalendConfirmService, DatasetService) {
                //given
                ctrl.uploadDatasetName();

                //when
                confirmDefer.reject('dismiss');
                scope.$digest();

                //then
                expect(DatasetService.getDatasetByName).toHaveBeenCalledWith(ctrl.datasetName);
                expect(TalendConfirmService.confirm).toHaveBeenCalledWith(null, ['UPDATE_EXISTING_DATASET'], {dataset: 'my cool dataset'});
                expect(DatasetService.create).not.toHaveBeenCalled();
                expect(DatasetService.update).not.toHaveBeenCalled();
            }));

            it('should create dataset with modified name', inject(function ($rootScope, TalendConfirmService, DatasetService, FolderService) {
                //given
                ctrl.uploadDatasetName();

                //when
                confirmDefer.reject();
                scope.$digest();
                uploadDefer.resolve({data: 'dataset_id_XYZ'});
                scope.$digest();

                //then
                expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith({name: 'my dataset.csv'}, 'my cool dataset (1)');
                expect(FolderService.getContent).toHaveBeenCalled();

                expect(ctrl.datasetFile).toBe(null);
                expect(ctrl.datasetName).toBe('');
            }));

            it('should update existing dataset', inject(function (UpdateWorkflowService) {
                //given
                ctrl.uploadDatasetName();

                //when
                confirmDefer.resolve();
                scope.$digest();
                uploadDefer.resolve();
                scope.$digest();

                //then
                expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith({name: 'my dataset.csv'}, {name: 'my cool dataset'});
                expect(ctrl.datasetFile).toBe(null);
                expect(ctrl.datasetName).toBe('');
            }));
        });
    });
});
