/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset List Service', () => {
    'use strict';

    let datasets, stateMock;
    let restPromise;

    function initDatasets() {
        datasets = [
            {
                'id': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
                'name': 'customers_jso_light',
                'author': 'anonymousUser',
                'records': 15,
                'nbLinesHeader': 1,
                'nbLinesFooter': 0,
                'created': '03-30-2015 08:06'
            },
            {
                'id': '3b21388c-f54a-4334-9bef-748912d0806f',
                'name': 'customers_jso',
                'author': 'anonymousUser',
                'records': 1000,
                'nbLinesHeader': 1,
                'nbLinesFooter': 0,
                'created': '03-30-2015 07:35'
            },
            {
                'id': '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
                'name': 'first_interactions',
                'author': 'anonymousUser',
                'records': 29379,
                'nbLinesHeader': 1,
                'nbLinesFooter': 0,
                'created': '03-30-2015 08:05'
            },
            {
                'id': '5e95be9e-88cd-4765-9ecc-ee48cc28b6d5',
                'name': 'first_interactions_400',
                'author': 'anonymousUser',
                'records': 400,
                'nbLinesHeader': 1,
                'nbLinesFooter': 0,
                'created': '03-30-2015 08:06'
            }
        ];
    }

    const sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    const orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.services.dataset', ($provide) => {
        stateMock = {
            inventory: {
                datasets: [],
                sortList: sortList,
                orderList: orderList,
                sort: sortList[1],
                order: orderList[1],
                currentFolder: {
                    path: ''
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, DatasetRestService, StateService) => {
        initDatasets();
        restPromise = $q.when(true);

        spyOn(DatasetRestService, 'create').and.returnValue(restPromise);
        spyOn(DatasetRestService, 'import').and.returnValue(restPromise);
        spyOn(DatasetRestService, 'update').and.returnValue(restPromise);
        spyOn(DatasetRestService, 'clone').and.returnValue(restPromise);
        spyOn(DatasetRestService, 'move').and.returnValue(restPromise);
        spyOn(DatasetRestService, 'delete').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'processCertification').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'toggleFavorite').and.returnValue($q.when(true));

        spyOn(StateService, 'setDatasets').and.returnValue();
        spyOn(StateService, 'removeDataset').and.returnValue();
    }));

    describe('getter/refresher', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should refresh dataset list', inject(function ($rootScope, DatasetListService, StateService) {
            //given
            stateMock.inventory.datasets = [{name: 'my dataset'}, {name: 'my second dataset'}];

            //when
            DatasetListService.refreshDatasets();
            $rootScope.$apply();

            //then
            expect(StateService.setDatasets).toHaveBeenCalledWith(datasets);
        }));

        it('should trigger another refresh when one is already pending with different sort condition', inject(function ($rootScope, DatasetListService, DatasetRestService, StateService) {
            //given
            stateMock.inventory.datasets = [{name: 'my dataset'}, {name: 'my second dataset'}];
            DatasetListService.refreshDatasets();

            //when
            DatasetListService.refreshDatasets();
            $rootScope.$apply();

            //then
            expect(StateService.setDatasets).toHaveBeenCalledWith(datasets);
            expect(DatasetRestService.getDatasets.calls.count()).toBe(2);
        }));

        it('should call refreshDatasets when datasetsPromise is false', inject(function (DatasetRestService, DatasetListService) {
            //when
            DatasetListService.getDatasetsPromise();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return datasetsPromise when datasetsPromise is not false', inject(function (DatasetRestService, DatasetListService) {

            //given
            stateMock.inventory.datasets = null;

            //when
            DatasetListService.getDatasetsPromise();
            DatasetListService.getDatasetsPromise();

            //then
            expect(DatasetRestService.getDatasets.calls.count()).toBe(1);
        }));

        it('should return datasetsPromise', inject(function (DatasetRestService, DatasetListService) {
            //when
            const datasetsPromise = DatasetListService.hasDatasetsPromise();

            //then
            expect(datasetsPromise).toBeFalsy();
        }));
    });


    describe('getter/refresher errors ', () => {
        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.reject());
        }));

        it('should refresh dataset list when REST request is failed', inject(function ($rootScope, DatasetListService, StateService, DatasetRestService) {
            //when
            DatasetListService.refreshDatasets();
            $rootScope.$apply();

            //then
            expect(StateService.setDatasets).toHaveBeenCalledWith([]);
        }));
    });

    describe('import', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should import remote dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const importParameters = {
                type: 'http',
                name: 'great remote dataset',
                url: 'moc.dnelat//:ptth'
            };
            const folder = {id : '', path: '', name: 'Home'};

            //when
            DatasetListService.importRemoteDataset(importParameters, folder);

            //then
            expect(DatasetRestService.import).toHaveBeenCalledWith(importParameters, folder);
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const importParameters = {
                type: 'http',
                name: 'great remote dataset',
                url: 'moc.dnelat//:ptth'
            };
            const folder = {id : '', path: '', name: 'Home'};

            //when
            DatasetListService.importRemoteDataset(importParameters, folder);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
            //given
            const importParameters = {
                type: 'http',
                name: 'great remote dataset',
                url: 'moc.dnelat//:ptth'
            };
            const folder = {id : '', path: '', name: 'Home'};

            //when
            const promise = DatasetListService.importRemoteDataset(importParameters, folder);

            //then
            expect(promise).toBe(restPromise);
        }));
    });

    describe('create', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should create dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset'};
            const folder = {id : '', path: '', name: 'Home'};

            //when
            DatasetListService.create(dataset, folder);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.create).toHaveBeenCalledWith(dataset, folder);
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset'};

            //when
            DatasetListService.create(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
            //given
            const dataset = {name: 'my dataset'};

            //when
            const promise = DatasetListService.create(dataset);

            //then
            expect(promise).toBe(restPromise);
        }));
    });

    describe('clone', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should call rest service clone', inject((DatasetRestService, DatasetListService) => {
            const folder = {id: 'foo'};

            //when
            DatasetListService.clone(datasets[0], folder, 'beer');

            //then
            expect(DatasetRestService.clone).toHaveBeenCalledWith(datasets[0], folder, 'beer');
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetRestService, DatasetListService) => {
            const folder = {id: 'foo'};

            //when
            DatasetListService.clone(datasets[0], folder, 'beer');
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
            //given
            const folder = {id: 'foo'};

            //when
            const promise = DatasetListService.clone(datasets[0], folder, 'beer');

            //then
            expect(promise).toBe(restPromise);
        }));
    });

    describe('move', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should call rest service move', inject((DatasetRestService, DatasetListService) => {
            //given
            const dataset = {id: '8435618646684615'};
            const folder = {path: '/toto'};
            const newFolder = {path: '/tata'};
            const newName = 'my dataset';

            //when
            DatasetListService.move(dataset, newFolder, newName);

            //then
            expect(DatasetRestService.move).toHaveBeenCalledWith(dataset, stateMock.inventory.currentFolder, newFolder, newName);
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetRestService, DatasetListService) => {
            //given
            const dataset = {id: '8435618646684615'};
            const folder = {path: '/toto'};
            const newFolder = {path: '/tata'};
            const newName = 'my dataset';

            //when
            DatasetListService.move(dataset, folder, newFolder, newName);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
            //given
            const dataset = {id: '8435618646684615'};
            const folder = {path: '/toto'};
            const newFolder = {path: '/tata'};
            const newName = 'my dataset';

            //when
            const promise = DatasetListService.move(dataset, folder, newFolder, newName);

            //then
            expect(promise).toBe(restPromise);
        }));
    });

    describe('update', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should update dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset'};

            //when
            DatasetListService.update(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.update).toHaveBeenCalledWith(dataset);
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset'};

            //when
            DatasetListService.update(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));

        it('should return original REST promise (not the promise with dataset list refresh)', inject(($rootScope, DatasetListService) => {
            //given
            const dataset = {name: 'my dataset'};

            //when
            const promise = DatasetListService.update(dataset);

            //then
            expect(promise).toBe(restPromise);
        }));
    });

    describe('delete', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should delete dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            stateMock.inventory.datasets = datasets.slice(0);

            //when
            DatasetListService.delete(datasets[0]);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.delete).toHaveBeenCalledWith(datasets[0]);
        }));

        it('should remove dataset from its internal list', inject(($rootScope, DatasetListService, StateService) => {
            //given
            stateMock.inventory.datasets = datasets.slice(0);

            //when
            DatasetListService.delete(datasets[0]);
            $rootScope.$apply();

            //then
            expect(StateService.removeDataset).toHaveBeenCalledWith(datasets[0]);
        }));
    });

    describe('certification', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should process certification on dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {id: '6a543545de46512bf8651c'};

            //when
            DatasetListService.processCertification(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.processCertification).toHaveBeenCalledWith('6a543545de46512bf8651c');
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {id: '6a543545de46512bf8651c'};

            //when
            DatasetListService.processCertification(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));
    });

    describe('toggle', () => {

        beforeEach(inject(($q, DatasetRestService) => {
            spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        }));

        it('should toggle dataset', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset', favorite: true};

            //when
            DatasetListService.toggleFavorite(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.toggleFavorite).toHaveBeenCalledWith(dataset);
            expect(dataset.favorite).toBe(false);
        }));

        it('should refresh datasets list', inject(($rootScope, DatasetListService, DatasetRestService) => {
            //given
            const dataset = {name: 'my dataset', favorite: true};

            //when
            DatasetListService.toggleFavorite(dataset);
            $rootScope.$apply();

            //then
            expect(DatasetRestService.getDatasets).toHaveBeenCalled();
        }));
    });
});