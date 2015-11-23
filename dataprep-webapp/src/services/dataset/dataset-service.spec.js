describe('Dataset Service', function () {
    'use strict';

    var datasets = [{id: '11', name: 'my dataset'},
        {id: '22', name: 'my second dataset'},
        {id: '33', name: 'my second dataset (1)'},
        {id: '44', name: 'my second dataset (2)'}];

    var preparationConsolidation, datasetConsolidation;
    var promiseWithProgress;
    var preparations = [{id: '4385fa764bce39593a405d91bc23'}];

    beforeEach(module('data-prep.services.dataset'));

    beforeEach(inject(function ($q, DatasetListService, DatasetRestService, PreparationListService) {
        preparationConsolidation = $q.when(preparations);
        datasetConsolidation = $q.when(true);
        promiseWithProgress = $q.when(true);

        DatasetListService.datasets = datasets;

        spyOn(DatasetListService, 'refreshDefaultPreparation').and.returnValue(datasetConsolidation);
        spyOn(DatasetListService, 'delete').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'create').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'importRemoteDataset').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'update').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'clone').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'processCertification').and.returnValue($q.when(true));

        spyOn(DatasetRestService, 'getContent').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'getSheetPreview').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'toggleFavorite').and.returnValue($q.when({}));

        spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when(datasets));
        spyOn(PreparationListService, 'refreshMetadataInfos').and.returnValue(preparationConsolidation);
    }));

    afterEach(inject(function (DatasetListService) {
        DatasetListService.datasets = null;
    }));

    describe('lifecycle', function () {

        describe('import', function() {
            it('should import remote and return the http promise', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var importParameters = {
                    type: 'http',
                    name: 'great remote dataset',
                    url: 'http://talend.com'
                };

                //when
                var result = DatasetService.import(importParameters);
                $rootScope.$digest();

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.importRemoteDataset).toHaveBeenCalledWith(importParameters);
            }));
        });

        describe('create', function() {
            it('should create a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                var result = DatasetService.create(dataset);
                $rootScope.$digest();

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.create).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.create(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            }));
        });

        describe('update', function() {
            it('should update a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                var result = DatasetService.update(dataset);
                $rootScope.$digest();

                //then
                expect(result).toBe(promiseWithProgress);
                expect(DatasetListService.update).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.update(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            }));

        });

        describe('delete', function() {
            it('should delete a dataset', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetListService.delete).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService, StorageService) {
                //given
                var dataset = DatasetListService.datasets[0];
                spyOn(StorageService, 'removeAllAggregations').and.returnValue();

                //when
                DatasetService.delete(dataset);
                $rootScope.$digest();

                //then
                expect(StorageService.removeAllAggregations).toHaveBeenCalledWith(dataset.id);
            }));
        });

        describe('clone', function() {
            it('should clone a dataset and return the http promise (with progress function)', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = DatasetListService.datasets[0];
                var name = 'my clone';

                //when
                DatasetService.clone(dataset, name);

                //then
                expect(DatasetListService.clone).toHaveBeenCalledWith(dataset, name);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = DatasetListService.datasets[0];
                var name = 'my clone';

                //when
                DatasetService.clone(dataset, name);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            }));
        });
    });

    describe('metadata actions', function () {
        describe('certification', function () {
            it('should process certification on dataset', inject(function ($rootScope, DatasetService, DatasetListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.processCertification(dataset);
                $rootScope.$digest();

                //then
                expect(DatasetListService.processCertification).toHaveBeenCalledWith(dataset);
            }));

            it('should consolidate preparations and datasets', inject(function ($rootScope, DatasetService, DatasetListService, PreparationListService) {
                //given
                var dataset = DatasetListService.datasets[0];

                //when
                DatasetService.processCertification(dataset);
                $rootScope.$digest();

                //then
                expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
                expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
            }));
        });

        describe('favorite', function () {
            it('should toggle favorite in a dataset', inject(function ($rootScope, DatasetService, DatasetListService, DatasetRestService) {
                //given
                var dataset = DatasetListService.datasets[0];
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

            it('should set metadata sheet', inject(function (DatasetService, DatasetRestService) {
                //given
                var metadata = {id: '7c98ae64154bc', sheetName: 'my old sheet'};
                var sheetName = 'my sheet';

                //when
                DatasetService.setDatasetSheet(metadata, sheetName);

                //then
                expect(metadata.sheetName).toBe(sheetName);
                expect(DatasetRestService.updateMetadata).toHaveBeenCalledWith(metadata);
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
        it('should return dataset list from ListService', inject(function (DatasetService, DatasetListService) {
            //given
            DatasetListService.datasets = datasets;

            //when
            var result = DatasetService.datasetsList();

            //then
            expect(result).toBe(datasets);
        }));

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
            DatasetListService.datasets = null;

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
            DatasetListService.datasets = null;

            //when
            DatasetService.getDatasets();
            DatasetListService.datasets = datasets; // simulate dataset list initialisation
            $rootScope.$digest();

            //then
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
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
            DatasetListService.datasets = datasets; // simulate dataset list initialisation
            $rootScope.$digest();

            //then
            expect(PreparationListService.refreshMetadataInfos).toHaveBeenCalledWith(datasets);
            expect(DatasetListService.refreshDefaultPreparation).toHaveBeenCalledWith(preparations);
        }));

        it('should find dataset by name', inject(function (DatasetService, DatasetListService) {
            //given
            DatasetListService.datasets = datasets;

            //when
            var actual = DatasetService.getDatasetByName(datasets[1].name);

            //then
            expect(actual).toBe(datasets[1]);
        }));

        it('should not find dataset by name', inject(function (DatasetService, DatasetListService) {
            //given
            DatasetListService.datasets = datasets;

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

        it('should not find dataset by id', inject(function ($q, $rootScope, DatasetService, DatasetListService) {
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