/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset Service', function () {
    'use strict';

    var datasets = [{id: '11', name: 'my dataset'},
        {id: '22', name: 'my second dataset'},
        {id: '33', name: 'my second dataset (1)'},
        {id: '44', name: 'my second dataset (2)'}];
    var encodings = ['UTF-8', 'UTF-16'];
    var preparationConsolidation, datasetConsolidation;
    var promiseWithProgress, stateMock;
    var preparations = [{id: '4385fa764bce39593a405d91bc23'}];

    beforeEach(angular.mock.module('data-prep.services.dataset', function ($provide) {
        stateMock = {
            folder: {
                currentFolderContent: {
                    datasets: datasets
                }
            }, inventory: {
                datasets: []
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, DatasetListService, DatasetRestService, PreparationListService, StateService) {
        preparationConsolidation = $q.when(preparations);
        datasetConsolidation = $q.when(true);
        promiseWithProgress = $q.when(true);

        stateMock.inventory.datasets = datasets;

        spyOn(DatasetListService, 'refreshPreparations').and.returnValue(datasetConsolidation);
        spyOn(DatasetListService, 'delete').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'create').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'importRemoteDataset').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'update').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'clone').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'processCertification').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'move').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when(datasets));

        spyOn(DatasetRestService, 'getContent').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'getSheetPreview').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'toggleFavorite').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'getEncodings').and.returnValue($q.when(encodings));

        spyOn(PreparationListService, 'refreshMetadataInfos').and.returnValue(preparationConsolidation);
        spyOn(StateService, 'setDatasetEncodings').and.returnValue();
    }));

    afterEach(inject(function () {
        stateMock.inventory.datasets = [];
    }));

    describe('lifecycle', function () {

        describe('import', function () {
            it('should import remote and return the http promise', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var importParameters = {
                    type: 'http',
                    name: 'great remote dataset',
                    url: 'http://talend.com'
                };

                var folder = {id: '', path: '', name: 'Home'};

                //when
                var result = DatasetService.import(importParameters, folder);
                $rootScope.$digest();

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.importRemoteDataset).toHaveBeenCalledWith(importParameters, folder);
            }));
        });

        describe('create', function () {
            it('should create a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                var folder = {id: '', path: '', name: 'Home'};

                //when
                var result = DatasetService.create(dataset, folder);
                $rootScope.$digest();

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.create).toHaveBeenCalledWith(dataset, folder);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.create(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));
        });

        describe('update', function () {
            it('should update a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                var result = DatasetService.update(dataset);

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.update).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, $q, DatasetService, DatasetListService, PreparationListService) {
                //given
                spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(datasets));
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.update(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));

        });

        describe('delete', function () {
            it('should delete a dataset', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetListService.delete).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService, StorageService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                spyOn(StorageService, 'removeAllAggregations').and.returnValue();

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(StorageService.removeAllAggregations).toHaveBeenCalledWith(dataset.id);
            }));
        });

        describe('clone', function () {
            it('should clone a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                var newFolder = {id: '/wine/beer'};
                var name = 'my clone';
                var mockPromise = {};

                //when
                DatasetService.clone(dataset, newFolder, name, mockPromise);

                //then
                expect(DatasetListService.clone).toHaveBeenCalledWith(dataset, newFolder, name, mockPromise);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                var name = 'my clone';

                //when
                DatasetService.clone(dataset, name);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));
        });

        describe('move', function () {
            it('should love a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                var folder = {id: '/wine/foo'};
                var newFolder = {id: '/wine/beer'};
                var name = 'my clone';
                var mockPromise = {};

                //when
                DatasetService.move(dataset, folder, newFolder, name, mockPromise);

                //then
                expect(DatasetListService.move).toHaveBeenCalledWith(dataset, folder, newFolder, name, mockPromise);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                var name = 'my clone';

                //when
                DatasetService.move(dataset, name);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));
        });

    });

    describe('metadata actions', function () {
        describe('certification', function () {
            it('should process certification on dataset', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.processCertification(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetListService.processCertification).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = stateMock.inventory.datasets[0];

                //when
                DatasetService.processCertification(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
            }));
        });

        describe('favorite', function () {
            it('should toggle favorite in a dataset', inject(function ($rootScope, DatasetService, DatasetListService, DatasetRestService) {
                //given
                var dataset = stateMock.inventory.datasets[0];
                dataset.favorite = false;
                //when
                DatasetService.toggleFavorite(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetRestService.toggleFavorite).toHaveBeenCalledWith(dataset);
                expect(dataset.favorite).toBeTruthy();

                //check the unset too
                //when
                DatasetService.toggleFavorite(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetRestService.toggleFavorite).toHaveBeenCalledWith(dataset);
                expect(dataset.favorite).toBeFalsy();

            }));
        });

        describe('sheet management', function () {
            it('should get sheet preview from rest service', inject(function (DatasetService, DatasetRestService) {
                //given
                var metadata = {id: '7c98ae64154bc'};
                var sheetName = 'my sheet';

                //when
                DatasetService.getSheetPreview(metadata, sheetName);

                //then
                expect(DatasetRestService.getSheetPreview).toHaveBeenCalledWith(metadata.id, sheetName);
            }));

            it('should set metadata sheet', inject(function ($q, DatasetService, DatasetRestService) {
                //given
                var metadata = {id: '7c98ae64154bc', sheetName: 'my old sheet'};
                var sheetName = 'my sheet';
                spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when({}));

                //when
                DatasetService.setDatasetSheet(metadata, sheetName);

                //then
                expect(metadata.sheetName).toBe(sheetName);
                expect(DatasetRestService.updateMetadata).toHaveBeenCalledWith(metadata);
            }));
        });

        describe('dataset parameters', function () {
            it('should get supported encodings and set them in state', inject(function ($rootScope, DatasetService, DatasetRestService, StateService){
                //given
                expect(DatasetRestService.getEncodings).not.toHaveBeenCalled();
                expect(StateService.setDatasetEncodings).not.toHaveBeenCalled();

                //when
                DatasetService.refreshSupportedEncodings();
                expect(DatasetRestService.getEncodings).toHaveBeenCalled();
                expect(StateService.setDatasetEncodings).not.toHaveBeenCalled();
                $rootScope.$digest();

                //then
                expect(StateService.setDatasetEncodings).toHaveBeenCalledWith(encodings);
            }));

            it('should update parameters (without its preparation to avoid cyclic ref: waiting for TDP-1348)', inject(function ($q, DatasetService, DatasetRestService){
                //given
                var metadata = {
                    id: '543a216fc796e354',
                    defaultPreparation: {id: '876a32bc545a846'},
                    preparations: [{id: '876a32bc545a846'}, {id: '799dc6b2562a186'}],
                    encoding: 'UTF-8',
                    parameters: {SEPARATOR: '|'}
                };
                var parameters = {
                    separator: ';',
                    encoding: 'UTF-16'
                };
                spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());
                expect(DatasetRestService.updateMetadata).not.toHaveBeenCalled();

                //when
                DatasetService.updateParameters(metadata, parameters);

                //then
                expect(DatasetRestService.updateMetadata).toHaveBeenCalled();
                expect(metadata.defaultPreparation).toBeFalsy();
                expect(metadata.preparations).toBeFalsy();
            }));

            it('should set back preparations after parameters update (waiting for TDP-1348)', inject(function ($rootScope, $q, DatasetService, DatasetRestService){
                //given
                var metadata = {
                    id: '543a216fc796e354',
                    defaultPreparation: {id: '876a32bc545a846', parameters: {SEPARATOR: '|'}},
                    preparations: [{id: '876a32bc545a846'}, {id: '799dc6b2562a186'}],
                    encoding: 'UTF-8',
                    parameters: {SEPARATOR: '|'}
                };
                var parameters = {
                    separator: ';',
                    encoding: 'UTF-16'
                };
                spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());

                //when
                DatasetService.updateParameters(metadata, parameters);
                expect(metadata.defaultPreparation).toBeFalsy();
                expect(metadata.preparations).toBeFalsy();
                $rootScope.$digest();

                //then
                expect(metadata.defaultPreparation).toEqual({id: '876a32bc545a846', parameters: {SEPARATOR: '|'}});
                expect(metadata.preparations).toEqual([{id: '876a32bc545a846'}, {id: '799dc6b2562a186'}]);
            }));

            it('should set back old parameters and preparations (waiting for TDP-1348) when update fails', inject(function ($rootScope, $q, DatasetService, DatasetRestService){
                //given
                var metadata = {
                    id: '543a216fc796e354',
                    defaultPreparation: {id: '876a32bc545a846', parameters: {SEPARATOR: '|'}},
                    preparations: [{id: '876a32bc545a846'}, {id: '799dc6b2562a186'}],
                    encoding: 'UTF-8',
                    parameters: {SEPARATOR: '|'}
                };
                var parameters = {
                    separator: ';',
                    encoding: 'UTF-16'
                };
                spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.reject());

                //when
                DatasetService.updateParameters(metadata, parameters);
                expect(metadata.parameters.SEPARATOR).toBe(';');
                expect(metadata.encoding).toBe('UTF-16');
                expect(metadata.defaultPreparation).toBeFalsy();
                expect(metadata.preparations).toBeFalsy();
                $rootScope.$digest();

                //then
                expect(metadata.parameters.SEPARATOR).toBe('|');
                expect(metadata.encoding).toBe('UTF-8');
                expect(metadata.defaultPreparation).toEqual({id: '876a32bc545a846', parameters: {SEPARATOR: '|'}});
                expect(metadata.preparations).toEqual([{id: '876a32bc545a846'}, {id: '799dc6b2562a186'}]);
            }));
        });
    });

    describe('content', function () {
        it('should get content from rest service', inject(function ($rootScope, DatasetService, DatasetRestService) {
            //given
            var datasetId = '34a5dc948967b5';
            var withMetadata = true;

            //when
            DatasetService.getContent(datasetId, withMetadata);
            $rootScope.$digest();

            //then
            expect(DatasetRestService.getContent).toHaveBeenCalledWith(datasetId, withMetadata);
        }));
    });

    describe('getter', function () {
        it('should get a promise that resolve the existing datasets if already fetched', inject(function ($q, $rootScope, DatasetService, DatasetListService) {
            //given
            spyOn(DatasetListService, 'hasDatasetsPromise').and.returnValue(true);
            spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(true));
            //when
            DatasetService.getDatasets();

            //then
            expect(DatasetListService.getDatasetsPromise).toHaveBeenCalled();
        }));

        it('should refresh datasets if datasets are not fetched', inject(function ($q, $rootScope, DatasetService, DatasetListService) {
            //given
            spyOn(DatasetListService, 'hasDatasetsPromise').and.returnValue(false);
            var results = null;

            //when
            DatasetService.getDatasets()
                .then(function (response) {
                    results = response;
                });

            $rootScope.$digest();

            //then
            expect(results).toBe(datasets);
        }));

        it('should get a promise that fetch datasets', inject(function ($rootScope, DatasetService, DatasetListService) {
            //given
            var results = null;
            stateMock.inventory.datasets = null;

            //when
            DatasetService.getDatasets()
                .then(function (response) {
                    results = response;
                });
            $rootScope.$digest();

            //then
            expect(results).toBe(datasets);
            expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
        }));

        it('should consolidate preparations and datasets on new dataset fetch', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
            //given
            stateMock.inventory.datasets = null;

            //when
            DatasetService.getDatasets();
            stateMock.inventory.datasets = datasets; // simulate dataset list initialisation
            $rootScope.$digest();

            //then
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
            expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
        }));

        it('should refresh dataset list', inject(function (DatasetService, DatasetListService) {
            //when
            DatasetService.refreshDatasets();

            //then
            expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
        }));

        it('should consolidate preparations and datasets on datasets refresh', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
            //when
            DatasetService.refreshDatasets();
            stateMock.inventory.datasets = datasets; // simulate dataset list initialisation
            $rootScope.$digest();

            //then
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
            expect(DatasetListService.refreshPreparations).toHaveBeenCalledWith(preparations);
        }));

        it('should find dataset by name', inject(function (DatasetService) {
            //when
            var actual = DatasetService.getDatasetByName(datasets[1].name);

            //then
            expect(actual).toBe(datasets[1]);
        }));

        it('should find dataset by name with case insensitive', inject(function (DatasetService) {
            //when
            var actual = DatasetService.getDatasetByName(datasets[1].name.toUpperCase());

            //then
            expect(actual).toBe(datasets[1]);
        }));

        it('should NOT find dataset by name', inject(function (DatasetService) {
            //when
            var actual = DatasetService.getDatasetByName('unknown');

            //then
            expect(actual).toBeUndefined();
        }));

        it('should find dataset by id', inject(function ($q, $rootScope, DatasetService, DatasetListService) {
            //given
            spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(datasets));

            var actual;

            //when
            DatasetService.getDatasetById(datasets[2].id)
                .then(function (dataset) {
                    actual = dataset;
                });

            $rootScope.$digest();

            //then
            expect(actual).toBe(datasets[2]);
        }));

        it('should NOT find dataset by id', inject(function ($q, $rootScope, DatasetService, DatasetListService) {
            //given
            spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(datasets));

            var actual;

            //when
            DatasetService.getDatasetById('not to be found')
                .then(function (dataset) {
                    actual = dataset;
                });

            $rootScope.$digest();

            //then
            expect(actual).toBeUndefined();
        }));
    });

    describe('utils', function () {
        it('should adapt info to dataset object for upload', inject(function (DatasetService) {
            //given
            var file = {
                path: '/path/to/file'
            };
            var name = 'myDataset';
            var id = 'e85afAa78556d5425bc2';

            //when
            var dataset = DatasetService.createDatasetInfo(file, name, id);

            //then
            expect(dataset.name).toBe(name);
            expect(dataset.progress).toBe(0);
            expect(dataset.file).toBe(file);
            expect(dataset.error).toBe(false);
            expect(dataset.id).toBe(id);
            expect(dataset.type).toBe('file');
        }));

        it('should adapt info to dataset object for remote dataset', inject(function (DatasetService) {
            //given
            var importParameters = {
                type: 'http',
                name: 'remote dataset',
                url: 'http://www.lequipe.fr'
            };

            //when
            var dataset = DatasetService.createDatasetInfo(null, importParameters.name, null);

            //then
            expect(dataset.name).toBe(importParameters.name);
            expect(dataset.progress).toBe(0);
            expect(dataset.file).toBeNull();
            expect(dataset.error).toBe(false);
            expect(dataset.id).toBeNull();
            expect(dataset.type).toBe('remote');
        }));

        it('should get unique dataset name', inject(function (DatasetService) {
            //given
            var name = 'my dataset';

            //when
            var uniqueName = DatasetService.getUniqueName(name);

            //then
            expect(uniqueName).toBe('my dataset (1)');
        }));

        it('should get unique dataset name with a number in it', inject(function (DatasetService) {
            //given
            var name = 'my second dataset (2)';

            //when
            var uniqueName = DatasetService.getUniqueName(name);

            //then
            expect(uniqueName).toBe('my second dataset (3)');
        }));
    });
});