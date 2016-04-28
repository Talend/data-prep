/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Home controller', () => {
    'use strict';

    let ctrl, createController, scope, $httpBackend, StateMock;
    const DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';
    const dataset = { id: 'ec4834d9bc2af8', name: 'Customers (50 lines)', draft: false };

    beforeEach(angular.mock.module('data-prep.home', ($provide) => {
        StateMock = {
            dataset: { uploadingDatasets: [] },
            inventory: {}
        };
        $provide.constant('state', StateMock);
    }));

    beforeEach(inject(($injector) => {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend.when('GET', 'i18n/en.json').respond({});
        $httpBackend.when('GET', 'i18n/fr.json').respond({});
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = () => $componentController('home', { $scope: scope });
    }));

    afterEach(inject(($window) => {
        $window.localStorage.removeItem(DATA_INVENTORY_PANEL_KEY);
        dataset.error = false;
        dataset.progress = false;
    }));

    it('should init upload list to empty array', () => {
        //when
        ctrl = createController();

        //then
        expect(ctrl.uploadingDatasets).toEqual([]);
    });

    it('should init right panel state with value from local storage', inject(($window) => {
        //given
        $window.localStorage.setItem(DATA_INVENTORY_PANEL_KEY, 'true');

        //when
        ctrl = createController();

        //then
        expect(ctrl.showRightPanel).toBe(true);
    }));

    describe('with created controller', () => {
        let uploadDefer;

        beforeEach(inject((StateService, $q, DatasetService, UploadWorkflowService) => {

            ctrl = createController();
            ctrl.datasetFile = [{ name: 'my dataset.csv' }];
            ctrl.datasetName = 'my cool dataset';

            uploadDefer = $q.defer();
            uploadDefer.promise.progress = (callback) => {
                uploadDefer.progressCb = callback;
                return uploadDefer.promise;
            };

            spyOn(DatasetService, 'getDatasetById').and.returnValue($q.when(dataset));
            spyOn(UploadWorkflowService, 'openDataset').and.returnValue();
            spyOn(DatasetService, 'createDatasetInfo').and.callFake(() => {
                return dataset;
            });
            spyOn(DatasetService, 'create').and.returnValue(uploadDefer.promise);
            spyOn(DatasetService, 'import').and.returnValue(uploadDefer.promise);
            spyOn(DatasetService, 'update').and.returnValue(uploadDefer.promise);

            spyOn(StateService, 'startUploadingDataset').and.returnValue();
            spyOn(StateService, 'finishUploadingDataset').and.returnValue();
        }));

        describe('right panel management', () => {
            it('should toggle right panel flag', inject(() => {
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

            it('should save toggled state in local storage', inject(($window) => {
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

            it('should update right panel icon', inject(() => {
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

        describe('remote file import', () => {
            it('should display http import form', () => {
                //given
                expect(ctrl.datasetHttpModal).toBeFalsy();

                //when
                ctrl.startImport({ id: 'http', name: 'from HTTP' });

                //then
                expect(ctrl.datasetHttpModal).toBeTruthy();
            });

            it('should create remote http dataset', inject((StateService, DatasetService, UploadWorkflowService) => {
                //given
                expect(ctrl.uploadingDatasets.length).toBe(0);
                ctrl.importHttpDataSet();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();

                //when
                uploadDefer.resolve({ data: dataset.id });
                scope.$digest();

                //then
                expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith(null, 'my cool dataset');
                expect(DatasetService.import).toHaveBeenCalled();
                expect(ctrl.uploadingDatasets.length).toBe(0);
                expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
                expect(StateService.finishUploadingDataset).toHaveBeenCalled();
            }));

            it('should display hdfs import form', () => {
                //given
                expect(ctrl.datasetHdfsModal).toBeFalsy();

                //when
                ctrl.startImport({ id: 'hdfs', name: 'from HDFS' });

                //then
                expect(ctrl.datasetHdfsModal).toBeTruthy();
            });

            it('should create remote hdfs dataset', inject((StateService, DatasetService, UploadWorkflowService) => {
                //given
                expect(ctrl.uploadingDatasets.length).toBe(0);
                ctrl.importHdfsDataSet();
                expect(StateService.startUploadingDataset).toHaveBeenCalled();

                //when
                uploadDefer.resolve({ data: dataset.id });
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

        describe('local file import', () => {
            it('should remove file extension for name init on step 1', () => {
                //given
                ctrl.datasetName = null;

                //when
                ctrl.uploadDatasetFile();

                //then
                expect(ctrl.datasetName).toBe('my dataset');
            });

            it('should display name modal on step 1 when name already exists', inject(($q, DatasetService) => {
                //given
                expect(ctrl.datasetNameModal).toBeFalsy();
                spyOn(DatasetService, 'getDatasetByName').and.returnValue(dataset);

                //when
                ctrl.uploadDatasetFile();

                //then
                expect(ctrl.datasetNameModal).toBeTruthy();
            }));

            it('should display name modal on step 1 when name is unique', inject((StateService, DatasetService) => {
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

            describe('step 2 with unique name', () => {

                beforeEach(inject(($q, $rootScope, DatasetService) => {
                    spyOn(DatasetService, 'getDatasetByName').and.returnValue(null);

                    spyOn($rootScope, '$emit').and.returnValue();
                }));

                it('should create dataset if name is unique', inject((StateService, $q, $rootScope, DatasetService, UploadWorkflowService) => {
                    //given
                    expect(ctrl.uploadingDatasets.length).toBe(0);
                    ctrl.uploadDatasetName();
                    expect(StateService.startUploadingDataset).toHaveBeenCalled();

                    //when
                    uploadDefer.resolve({ data: dataset.id });
                    scope.$digest();

                    //then
                    expect(DatasetService.create).toHaveBeenCalled();
                    expect(ctrl.uploadingDatasets.length).toBe(0);
                    expect(DatasetService.getDatasetById).toHaveBeenCalledWith(dataset.id);
                    expect(UploadWorkflowService.openDataset).toHaveBeenCalled();
                    expect(StateService.finishUploadingDataset).toHaveBeenCalled();
                }));

                it('should update progress on create', inject((state, StateService, DatasetService) => {
                    //given
                    ctrl.uploadDatasetName();
                    expect(StateService.startUploadingDataset).toHaveBeenCalled();
                    expect(dataset.progress).toBeFalsy();

                    const event = {
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

                it('should set error flag and show error toast', inject((StateService, DatasetService) => {
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

            describe('step 2 with existing name', () => {
                const dataset = {
                    name: 'my cool dataset'
                };
                let confirmDefer;

                beforeEach(inject(($rootScope, $q, StateService, DatasetService, UpdateWorkflowService, TalendConfirmService) => {
                    confirmDefer = $q.defer();

                    spyOn(StateService, 'resetPlayground').and.returnValue();
                    spyOn(DatasetService, 'getDatasetByName').and.returnValue(dataset);
                    spyOn(DatasetService, 'getUniqueName').and.returnValue('my cool dataset (1)');
                    spyOn(TalendConfirmService, 'confirm').and.returnValue(confirmDefer.promise);
                    spyOn($rootScope, '$emit').and.returnValue();
                    spyOn(UpdateWorkflowService, 'updateDataset').and.returnValue();

                }));

                it('should do nothing on confirm modal dismiss', inject((TalendConfirmService, DatasetService) => {
                    //given
                    ctrl.uploadDatasetName();

                    //when
                    confirmDefer.reject('dismiss');
                    scope.$digest();

                    //then
                    expect(DatasetService.getDatasetByName).toHaveBeenCalledWith(ctrl.datasetName);
                    expect(TalendConfirmService.confirm).toHaveBeenCalledWith(null, ['UPDATE_EXISTING_DATASET'], { dataset: 'my cool dataset' });
                    expect(DatasetService.create).not.toHaveBeenCalled();
                    expect(DatasetService.update).not.toHaveBeenCalled();
                }));

                it('should create dataset with modified name', inject(($rootScope, TalendConfirmService, DatasetService) => {
                    //given
                    ctrl.uploadDatasetName();

                    //when
                    confirmDefer.reject();
                    scope.$digest();
                    uploadDefer.resolve({ data: 'dataset_id_XYZ' });
                    scope.$digest();

                    //then
                    expect(DatasetService.createDatasetInfo).toHaveBeenCalledWith(ctrl.datasetFile[0], 'my cool dataset (1)');
                }));

                it('should update existing dataset', inject((UpdateWorkflowService) => {
                    //given
                    ctrl.uploadDatasetName();

                    //when
                    confirmDefer.resolve();
                    scope.$digest();
                    uploadDefer.resolve();
                    scope.$digest();

                    //then
                    expect(UpdateWorkflowService.updateDataset).toHaveBeenCalledWith(ctrl.datasetFile[0], { name: 'my cool dataset' });
                }));
            });
        });
    });
});
