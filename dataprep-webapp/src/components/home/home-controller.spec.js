describe('Home controller', function () {
    'use strict';

    var ctrl, createController, scope, $httpBackend, StateMock;
    var DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';
    var dataset = {id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false};

    beforeEach(module('data-prep.home', function ($provide) {
        StateMock = {
            dataset: {
                uploadingDatasets:[]
            },
            folder: {
                currentFolder: {}
            }
        };
        $provide.constant('state', StateMock);
    }));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.when('GET', 'i18n/en.json').respond({});
        $httpBackend.when('GET', 'i18n/fr.json').respond({});
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('HomeCtrl', {
                $scope: scope
            });
        };
    }));

    afterEach(inject(function($window) {
        $window.localStorage.removeItem(DATA_INVENTORY_PANEL_KEY);
        dataset.error = false;
        dataset.progress = false;
    }));

    it('should init upload list to empty array', function () {
        //when
        ctrl = createController();

        //then
        expect(ctrl.uploadingDatasets).toEqual([]);
    });

    it('should init right panel state with value from local storage', inject(function ($window) {
        //given
       $window.localStorage.setItem(DATA_INVENTORY_PANEL_KEY, 'true');

        //when
        ctrl = createController();

        //then
        expect(ctrl.showRightPanel).toBe(true);
    }));

    describe('with created controller', function () {
        var uploadDefer;

        beforeEach(inject(function (StateService, $q, DatasetService, UploadWorkflowService) {

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
            spyOn(DatasetService, 'import').and.returnValue(uploadDefer.promise);
            spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

            spyOn(StateService, 'startUploadingDataset').and.returnValue();
            spyOn(StateService, 'finishUploadingDataset').and.returnValue();
        }));

        describe('right panel management', function() {
            it('should toggle right panel flag', inject(function () {
                //given
                expect(ctrl.showRightPanel).toBe(false);

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanel).toBe(true);

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanel).toBe(false);
            }));

            it('should save toggled state in local storage', inject(function ($window) {
                //given
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeFalsy();

                //when
                ctrl.toggleRightPanel();

                //then
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeTruthy();
                //when
                ctrl.toggleRightPanel();

                //then
                expect(JSON.parse($window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY))).toBeFalsy();
            }));

            it('should update right panel icon', inject(function () {
                //given
                expect(ctrl.showRightPanelIcon).toBe('u');

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanelIcon).toBe('t');

                //when
                ctrl.toggleRightPanel();

                //then
                expect(ctrl.showRightPanelIcon).toBe('u');
            }));
        });

        describe('remote file import', function() {
            it('should display http import form', function () {
                //given
                expect(ctrl.datasetHttpModal).toBeFalsy();

                //when
                ctrl.startImport({id: 'http', name: 'from HTTP'});

                //then
                expect(ctrl.datasetHttpModal).toBeTruthy();
            });

            it('should create remote http dataset', inject(function (StateService, DatasetService, UploadWorkflowService) {
                //given
                expect(ctrl.uploadingDatasets.length).toBe(0);
                ctrl.importHttpDataSet();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();

                //when
                uploadDefer.resolve({data: dataset.id});
                scope.$digest();

                //then
                expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith(null, 'my cool dataset');
                expect(DatasetService.import).toHaveBeenCalled();
                expect(ctrl.uploadingDatasets.length).toBe(0);
                expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
                expect(StateService.finishUploadingDataset).toHaveBeenCalled();
            }));

            it('should display hdfs import form', function () {
                //given
                expect(ctrl.datasetHdfsModal).toBeFalsy();

                //when
                ctrl.startImport({id: 'hdfs', name: 'from HDFS'});

                //then
                expect(ctrl.datasetHdfsModal).toBeTruthy();
            });

            it('should create remote hdfs dataset', inject(function (StateService, DatasetService, UploadWorkflowService) {
                //given
                expect(ctrl.uploadingDatasets.length).toBe(0);
                ctrl.importHdfsDataSet();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();

                //when
                uploadDefer.resolve({data: dataset.id});
                scope.$digest();

                //then
                expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith(null, 'my cool dataset');
                expect(DatasetService.import).toHaveBeenCalled();
                expect(ctrl.uploadingDatasets.length).toBe(0);
                expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
                expect(StateService.finishUploadingDataset).toHaveBeenCalled();
            }));
        });

        describe('local file import', function() {
            it('should remove file extension for name init on step 1', function () {
                //given
                ctrl.datasetName = null;

                //when
                ctrl.uploadDatasetFile();

                //then
                expect(ctrl.datasetName).toBe('my dataset');
            });

            it('should display name modal on step 1 when name already exists', inject(function (DatasetService) {
                //given
                expect(ctrl.datasetNameModal).toBeFalsy();
                spyOn(DatasetService, 'getDatasetByName').and.returnValue({});

                //when
                ctrl.uploadDatasetFile();

                //then
                expect(ctrl.datasetNameModal).toBeTruthy();
            }));

            it('should display name modal on step 1 when name is unique', inject(function (StateService, DatasetService) {
                //given
                expect(ctrl.datasetNameModal).toBeFalsy();
                spyOn(DatasetService, 'getDatasetByName').and.returnValue(null);

                //when
                ctrl.uploadDatasetFile();

                //then
                expect(ctrl.datasetNameModal).toBeFalsy();
                expect(DatasetService.create).toHaveBeenCalled();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();
            }));

            describe('step 2 with unique name', function () {

                beforeEach(inject(function ($q, $rootScope, DatasetService, FolderService) {
                    spyOn(DatasetService, 'getDatasetByName').and.returnValue(null);
                    spyOn(FolderService,'createFolderEntry').and.returnValue($q.when(true));
                    spyOn(FolderService,'listFolderEntries').and.returnValue($q.when(true));
                    spyOn(FolderService,'getFolderContent').and.returnValue($q.when(true));

                    spyOn($rootScope, '$emit').and.returnValue();
                }));

                it('should create dataset if name is unique', inject(function (StateService, $q, $rootScope, DatasetService, UploadWorkflowService, FolderService) {
                    //given
                    expect(ctrl.uploadingDatasets.length).toBe(0);
                    ctrl.uploadDatasetName();
                    expect(StateService.startUploadingDataset).toHaveBeenCalled();

                    //when
                    uploadDefer.resolve({data: dataset.id});
                    scope.$digest();

                    //then
                    expect(DatasetService.create).toHaveBeenCalled();
                    expect(FolderService.createFolderEntry).toHaveBeenCalled();
                    //expect(FolderService.listFolderEntries).toHaveBeenCalled();
                    expect(FolderService.getFolderContent).toHaveBeenCalled();
                    expect(ctrl.uploadingDatasets.length).toBe(0);
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

            describe('step 2 with existing name', function () {
                var dataset = {
                    name: 'my cool dataset'
                };
                var confirmDefer;

                beforeEach(inject(function ($rootScope, $q, StateService, DatasetService, UpdateWorkflowService, TalendConfirmService, FolderService) {
                    confirmDefer = $q.defer();

                    spyOn(StateService, 'resetPlayground').and.returnValue();
                    spyOn(DatasetService, 'getDatasetByName').and.returnValue(dataset);
                    spyOn(DatasetService, 'getUniqueName').and.returnValue('my cool dataset (1)');
                    spyOn(TalendConfirmService, 'confirm').and.returnValue(confirmDefer.promise);
                    spyOn($rootScope, '$emit').and.returnValue();
                    spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue();
                    spyOn(FolderService,'createFolderEntry').and.returnValue($q.when(true));
                    spyOn(FolderService,'listFolderEntries').and.returnValue($q.when(true));
                    spyOn(FolderService,'getFolderContent').and.returnValue($q.when(true));

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
                    expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith(ctrl.datasetFile[0], 'my cool dataset (1)');
                    expect(FolderService.getFolderContent).toHaveBeenCalled();
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
                    expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith(ctrl.datasetFile[0], {name: 'my cool dataset'});
                }));
            });
        });

    });
});
