/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Service', () => {
    'use strict';

    let promiseWithProgress, stateMock;

    const datasets = [{ id: '11', name: 'my dataset' },
        { id: '22', name: 'my second dataset' },
        { id: '33', name: 'my second dataset (1)' },
        { id: '44', name: 'my second dataset (2)' }];

    const encodings = ['UTF-8', 'UTF-16'];

    const sortList = [
        { id: 'name', name: 'NAME_SORT', property: 'name' },
        { id: 'date', name: 'DATE_SORT', property: 'created' },
    ];

    const orderList = [
        { id: 'asc', name: 'ASC_ORDER' },
        { id: 'desc', name: 'DESC_ORDER' },
    ];

    beforeEach(angular.mock.module('data-prep.services.dataset', ($provide) => {
        stateMock = {
            inventory: {
                datasets: datasets,
                sortList: sortList,
                orderList: orderList,
            },
            playground: {},
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, DatasetListService, DatasetRestService, StateService) => {
        promiseWithProgress = $q.when(true);

        spyOn(DatasetListService, 'create').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'update').and.returnValue(promiseWithProgress);
        spyOn(DatasetListService, 'delete').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'clone').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'processCertification').and.returnValue($q.when(true));
        spyOn(DatasetListService, 'refreshDatasets').and.returnValue($q.when(datasets));

        spyOn(DatasetRestService, 'getContent').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'getSheetPreview').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'toggleFavorite').and.returnValue($q.when({}));
        spyOn(DatasetRestService, 'getEncodings').and.returnValue($q.when(encodings));

        spyOn(StateService, 'setDatasetEncodings').and.returnValue();
    }));

    describe('init', () => {
        it('should set the datasets sort when there is a saved one', inject((StateService, StorageService, DatasetService) => {
            // given
            spyOn(StorageService, 'getDatasetsSort').and.returnValue('date');
            spyOn(StateService, 'setDatasetsSort').and.returnValue();

            // when
            DatasetService.init('/my/path');

            // then
            expect(StateService.setDatasetsSort).toHaveBeenCalledWith(sortList[1]);
        }));

        it('should NOT set the datasets sort when there is NO saved one', inject((StateService, StorageService, DatasetService) => {
            // given
            spyOn(StorageService, 'getDatasetsSort').and.returnValue(null);
            spyOn(StateService, 'setDatasetsSort').and.returnValue();

            // when
            DatasetService.init('/my/path');

            // then
            expect(StateService.setDatasetsSort).not.toHaveBeenCalled();
        }));

        it('should set the datasets order when there is a saved one', inject((StateService, StorageService, DatasetService) => {
            // given
            spyOn(StorageService, 'getDatasetsOrder').and.returnValue('desc');
            spyOn(StateService, 'setDatasetsOrder').and.returnValue();

            // when
            DatasetService.init('/my/path');

            // then
            expect(StateService.setDatasetsOrder).toHaveBeenCalledWith(orderList[1]);
        }));

        it('should NOT set the datasets order when there is NO saved one', inject((StateService, StorageService, DatasetService) => {
            // given
            spyOn(StorageService, 'getDatasetsOrder').and.returnValue(null);
            spyOn(StateService, 'setDatasetsOrder').and.returnValue();

            // when
            DatasetService.init('/my/path');

            // then
            expect(StateService.setDatasetsOrder).not.toHaveBeenCalled();
        }));

        it('should refresh datasets list', inject(($q, DatasetListService, DatasetService) => {
            // given
            expect(DatasetListService.refreshDatasets).not.toHaveBeenCalled();

            // when
            DatasetService.init('/my/path');

            // then
            expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
        }));
    });

    describe('get all datasets', () => {
        it('should get a promise that resolve the existing datasets if already fetched', inject(($q, $rootScope, DatasetService, DatasetListService) => {
            // given
            spyOn(DatasetListService, 'hasDatasetsPromise').and.returnValue(true);
            spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(true));
            // when
            DatasetService.getDatasets();

            // then
            expect(DatasetListService.getDatasetsPromise).toHaveBeenCalled();
        }));

        it('should refresh datasets if datasets are not fetched', inject(($q, $rootScope, DatasetService, DatasetListService) => {
            // given
            spyOn(DatasetListService, 'hasDatasetsPromise').and.returnValue(false);
            let results = null;

            // when
            DatasetService.getDatasets()
                .then((response) => {
                    results = response;
                });

            $rootScope.$digest();

            // then
            expect(results).toBe(datasets);
        }));

        it('should get a promise that fetch datasets', inject(($rootScope, DatasetService, DatasetListService) => {
            // given
            let results = null;
            stateMock.inventory.datasets = null;

            // when
            DatasetService.getDatasets()
                .then((response) => {
                    results = response;
                });
            $rootScope.$digest();

            // then
            expect(results).toBe(datasets);
            expect(DatasetListService.refreshDatasets).toHaveBeenCalled();
        }));
    });

    describe('get', () => {
        describe('by name', () => {
            it('should find dataset', inject((DatasetService) => {
                // when
                const actual = DatasetService.getDatasetByName(datasets[1].name);

                // then
                expect(actual).toBe(datasets[1]);
            }));

            it('should find dataset with case insensitive', inject((DatasetService) => {
                // when
                const actual = DatasetService.getDatasetByName(datasets[1].name.toUpperCase());

                // then
                expect(actual).toBe(datasets[1]);
            }));

            it('should return undefined when name does not exist', inject((DatasetService) => {
                // when
                const actual = DatasetService.getDatasetByName('unknown');

                // then
                expect(actual).toBeUndefined();
            }));
        });

        describe('by id', () => {
            it('should find dataset', inject(($q, $rootScope, DatasetService, DatasetListService) => {
                // given
                spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(datasets));
                let actual = null;

                // when
                DatasetService.getDatasetById(datasets[2].id).then((dataset) => actual = dataset);
                $rootScope.$digest();

                // then
                expect(actual).toBe(datasets[2]);
            }));

            it('should return undefined when id does not exist', inject(($q, $rootScope, DatasetService, DatasetListService) => {
                // given
                spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when(datasets));
                let actual = null;

                // when
                DatasetService.getDatasetById('not to be found').then((dataset) => actual = dataset);
                $rootScope.$digest();

                // then
                expect(actual).toBeUndefined();
            }));
        });
    });

    describe('delete', () => {
        it('should delete a dataset', inject(($rootScope, DatasetService, DatasetListService) => {
            // given
            const dataset = stateMock.inventory.datasets[0];

            // when
            DatasetService.delete(dataset);
            $rootScope.$digest();

            // then
            expect(DatasetListService.delete).toHaveBeenCalledWith(dataset);
        }));

        it('should remove aggregations from local storage on the removed dataset', inject(($rootScope, DatasetService, StorageService) => {
            // given
            const dataset = stateMock.inventory.datasets[0];
            spyOn(StorageService, 'removeAllAggregations').and.returnValue();

            // when
            DatasetService.delete(dataset);
            $rootScope.$digest();

            // then
            expect(StorageService.removeAllAggregations).toHaveBeenCalledWith(dataset.id);
        }));
    });

    describe('sheet management', () => {
        it('should get sheet preview from rest service', inject((DatasetService, DatasetRestService) => {
            // given
            const metadata = { id: '7c98ae64154bc' };
            const sheetName = 'my sheet';

            // when
            DatasetService.getSheetPreview(metadata, sheetName);

            // then
            expect(DatasetRestService.getSheetPreview).toHaveBeenCalledWith(metadata.id, sheetName);
        }));

        it('should set metadata sheet', inject(($q, DatasetService, DatasetRestService) => {
            // given
            const metadata = { id: '7c98ae64154bc', sheetName: 'my old sheet' };
            const sheetName = 'my sheet';
            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when({}));

            // when
            DatasetService.setDatasetSheet(metadata, sheetName);

            // then
            expect(metadata.sheetName).toBe(sheetName);
            expect(DatasetRestService.updateMetadata).toHaveBeenCalledWith(metadata);
        }));
    });

    describe('parameters', () => {
        it('should get supported encodings and set them in state', inject(($rootScope, DatasetService, DatasetRestService, StateService) => {
            // given
            expect(DatasetRestService.getEncodings).not.toHaveBeenCalled();
            expect(StateService.setDatasetEncodings).not.toHaveBeenCalled();

            // when
            DatasetService.refreshSupportedEncodings();
            expect(DatasetRestService.getEncodings).toHaveBeenCalled();
            expect(StateService.setDatasetEncodings).not.toHaveBeenCalled();
            $rootScope.$digest();

            // then
            expect(StateService.setDatasetEncodings).toHaveBeenCalledWith(encodings);
        }));

        it('should update parameters (without its preparation to avoid cyclic ref: waiting for TDP-1348)', inject(($q, DatasetService, DatasetRestService) => {
            // given
            const metadata = {
                id: '543a216fc796e354',
                defaultPreparation: { id: '876a32bc545a846' },
                preparations: [{ id: '876a32bc545a846' }, { id: '799dc6b2562a186' }],
                encoding: 'UTF-8',
                parameters: { SEPARATOR: '|' }
            };
            const parameters = {
                separator: ';',
                encoding: 'UTF-16'
            };
            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());
            expect(DatasetRestService.updateMetadata).not.toHaveBeenCalled();

            // when
            DatasetService.updateParameters(metadata, parameters);

            //then
            expect(DatasetRestService.updateMetadata).toHaveBeenCalled();
            expect(metadata.defaultPreparation).toBeFalsy();
            expect(metadata.preparations).toBeFalsy();
        }));

        it('should set back preparations after parameters update (waiting for TDP-1348)', inject(($rootScope, $q, DatasetService, DatasetRestService) => {
            // given
            const metadata = {
                id: '543a216fc796e354',
                defaultPreparation: { id: '876a32bc545a846', parameters: { SEPARATOR: '|' } },
                preparations: [{ id: '876a32bc545a846' }, { id: '799dc6b2562a186' }],
                encoding: 'UTF-8',
                parameters: { SEPARATOR: '|' }
            };
            const parameters = {
                separator: ';',
                encoding: 'UTF-16'
            };
            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());

            // when
            DatasetService.updateParameters(metadata, parameters);
            expect(metadata.defaultPreparation).toBeFalsy();
            expect(metadata.preparations).toBeFalsy();
            $rootScope.$digest();

            // then
            expect(metadata.defaultPreparation).toEqual({ id: '876a32bc545a846', parameters: { SEPARATOR: '|' } });
            expect(metadata.preparations).toEqual([{ id: '876a32bc545a846' }, { id: '799dc6b2562a186' }]);
        }));

        it('should set back old parameters and preparations (waiting for TDP-1348) when update fails', inject(($rootScope, $q, DatasetService, DatasetRestService) => {
            // given
            const metadata = {
                id: '543a216fc796e354',
                defaultPreparation: { id: '876a32bc545a846', parameters: { SEPARATOR: '|' } },
                preparations: [{ id: '876a32bc545a846' }, { id: '799dc6b2562a186' }],
                encoding: 'UTF-8',
                parameters: { SEPARATOR: '|' }
            };
            const parameters = {
                separator: ';',
                encoding: 'UTF-16'
            };
            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.reject());

            // when
            DatasetService.updateParameters(metadata, parameters);
            expect(metadata.parameters.SEPARATOR).toBe(';');
            expect(metadata.encoding).toBe('UTF-16');
            expect(metadata.defaultPreparation).toBeFalsy();
            expect(metadata.preparations).toBeFalsy();
            $rootScope.$digest();

            // then
            expect(metadata.parameters.SEPARATOR).toBe('|');
            expect(metadata.encoding).toBe('UTF-8');
            expect(metadata.defaultPreparation).toEqual({ id: '876a32bc545a846', parameters: { SEPARATOR: '|' } });
            expect(metadata.preparations).toEqual([{ id: '876a32bc545a846' }, { id: '799dc6b2562a186' }]);
        }));
    });

    describe('rename', () => {
        it('should set new name via app state', inject(($q, DatasetService, DatasetRestService, StateService) => {
            //given
            const metadata = { id: '7a82d3002fc543e54', name: 'oldName' };
            const name = 'newName';

            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());
            spyOn(StateService, 'setDatasetName').and.returnValue(); // this update the metadata name too

            //when
            DatasetService.rename(metadata, name);

            //then
            expect(StateService.setDatasetName).toHaveBeenCalledWith(metadata.id, name);
        }));

        it('should call update metadata service (without its preparation to avoid cyclic ref: waiting for TDP-1348)', inject(($q, DatasetService, DatasetRestService, StateService) => {
            //given
            const metadata = {
                id: '7a82d3002fc543e54',
                name: 'oldName',
                defaultPreparation: { id: '893ad6695fe42d515' },
                preparations: [
                    { id: '893ad6695fe42d515' },
                    { id: '987efd121fa56898a' },
                ]
            };
            const name = 'newName';

            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());
            spyOn(StateService, 'setDatasetName').and.returnValue(); // this update the metadata name too

            //when
            DatasetService.rename(metadata, name);

            //then
            expect(DatasetRestService.updateMetadata).toHaveBeenCalledWith(metadata);
            expect(metadata.defaultPreparation).toBeFalsy();
            expect(metadata.preparations).toBeFalsy();
        }));

        it('should set back old name via app state on rename failure', inject(($rootScope, $q, DatasetService, DatasetRestService, StateService) => {
            //given
            const metadata = { id: '7a82d3002fc543e54', name: 'oldName' };
            const name = 'newName';

            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.reject());
            spyOn(StateService, 'setDatasetName').and.returnValue(); // this update the metadata name too

            //when
            DatasetService.rename(metadata, name);
            expect(StateService.setDatasetName).not.toHaveBeenCalledWith(metadata.id, 'oldName');
            $rootScope.$digest();

            //then
            expect(StateService.setDatasetName).toHaveBeenCalledWith(metadata.id, 'oldName');
        }));

        it('should set back preparations after rename (waiting for TDP-1348)', inject(($rootScope, $q, DatasetService, DatasetRestService, StateService) => {
            //given
            const metadata = {
                id: '7a82d3002fc543e54',
                name: 'oldName',
                defaultPreparation: { id: '893ad6695fe42d515' },
                preparations: [
                    { id: '893ad6695fe42d515' },
                    { id: '987efd121fa56898a' },
                ]
            };
            spyOn(DatasetRestService, 'updateMetadata').and.returnValue($q.when());
            spyOn(StateService, 'setDatasetName').and.returnValue(); // this update the metadata name too

            //when
            DatasetService.rename(metadata, 'newName');
            expect(metadata.defaultPreparation).toBeFalsy();
            expect(metadata.preparations).toBeFalsy();
            $rootScope.$digest();

            //then
            expect(metadata.defaultPreparation).toEqual({ id: '893ad6695fe42d515' });
            expect(metadata.preparations).toEqual([
                { id: '893ad6695fe42d515' },
                { id: '987efd121fa56898a' },
            ]);
        }));
    });

    describe('utils', () => {
        it('should adapt info to dataset object for upload', inject((DatasetService) => {
            // given
            const file = {
                path: '/path/to/file'
            };
            const name = 'myDataset';
            const id = 'e85afAa78556d5425bc2';

            // when
            const dataset = DatasetService.createDatasetInfo(file, name, id);

            // then
            expect(dataset.name).toBe(name);
            expect(dataset.progress).toBe(0);
            expect(dataset.file).toBe(file);
            expect(dataset.error).toBe(false);
            expect(dataset.id).toBe(id);
            expect(dataset.type).toBe('file');
        }));

        it('should adapt info to dataset object for remote dataset', inject((DatasetService) => {
            // given
            const importParameters = {
                type: 'http',
                name: 'remote dataset',
                url: 'http://www.lequipe.fr'
            };

            // when
            const dataset = DatasetService.createDatasetInfo(null, importParameters.name, null);

            // then
            expect(dataset.name).toBe(importParameters.name);
            expect(dataset.progress).toBe(0);
            expect(dataset.file).toBeNull();
            expect(dataset.error).toBe(false);
            expect(dataset.id).toBeNull();
            expect(dataset.type).toBe('remote');
        }));

        it('should get unique dataset name', inject((DatasetService) => {
            // given
            const name = 'my dataset';

            // when
            const uniqueName = DatasetService.getUniqueName(name);

            // then
            expect(uniqueName).toBe('my dataset (1)');
        }));

        it('should get unique dataset name with a number in it', inject((DatasetService) => {
            // given
            const name = 'my second dataset (2)';

            // when
            const uniqueName = DatasetService.getUniqueName(name);

            // then
            expect(uniqueName).toBe('my second dataset (3)');
        }));
    });

    describe('compatible preparations', () => {
        const preparations = [
            { id: '1', dataSetId: '22' }, //datasets[1]
            { id: '2', dataSetId: '33' }, //datasets[2]
            { id: '3', dataSetId: '22' }, //datasets[1]
            { id: '4', dataSetId: '11' }, //datasets[0]
        ];

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getCompatiblePreparations').and.returnValue($q.when(preparations));
        }));

        it('should get preparations from rest', inject((DatasetService, DatasetRestService) => {
            // given
            expect(DatasetRestService.getCompatiblePreparations).not.toHaveBeenCalled();

            // when
            DatasetService.getCompatiblePreparations('11');

            // then
            expect(DatasetRestService.getCompatiblePreparations).toHaveBeenCalledWith('11');
        }));

        it('should adapt compatible preparations with their datasets', inject(($rootScope, DatasetService) => {
            // given
            let result = null;

            // when
            DatasetService.getCompatiblePreparations('11')
                .then((preps) => { result = preps });
            $rootScope.$digest();

            // then
            expect(result.length).toBe(4);
            expect(result[0]).toEqual({ preparation: preparations[0], dataset: datasets[1] });
            expect(result[1]).toEqual({ preparation: preparations[1], dataset: datasets[2] });
            expect(result[2]).toEqual({ preparation: preparations[2], dataset: datasets[1] });
            expect(result[3]).toEqual({ preparation: preparations[3], dataset: datasets[0] });
        }));



        it('should exclude current preparation', inject(($rootScope, DatasetService) => {
            // given
            stateMock.playground.preparation = { id: '4' };
            let result = null;

            // when
            DatasetService.getCompatiblePreparations('11')
                .then((preps) => { result = preps });
            $rootScope.$digest();

            // then : it should have removed preparation[3]
            expect(result.length).toBe(3);
            expect(result[0]).toEqual({ preparation: preparations[0], dataset: datasets[1] });
            expect(result[1]).toEqual({ preparation: preparations[1], dataset: datasets[2] });
            expect(result[2]).toEqual({ preparation: preparations[2], dataset: datasets[1] });
        }));
    });
});