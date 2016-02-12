/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataset List Service', function () {
    'use strict';

    var preparations, datasets, stateMock;

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

    function initPreparations() {
        preparations = [
            {
                'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
                'dataSetId': datasets[0].id,
                'author': 'anonymousUser',
                'creationDate': 1427447300300,
                'steps': [
                    '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                    '4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
                    '8a1c49d1b64270482e8db8232357c6815615b7cf',
                    '599725f0e1331d5f8aae24f22cd1ec768b10348d'
                ],
                'actions': [
                    {
                        'action': 'lowercase',
                        'parameters': {
                            'column_name': 'birth'
                        }
                    },
                    {
                        'action': 'uppercase',
                        'parameters': {
                            'column_name': 'country'
                        }
                    },
                    {
                        'action': 'cut',
                        'parameters': {
                            'pattern': '.',
                            'column_name': 'first_item'
                        }
                    }
                ]
            },
            {
                'id': 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
                'dataSetId': datasets[2].id,
                'author': 'anonymousUser',
                'lastModificationDate': 1427447330693,
                'steps': [
                    '47e2444dd1301120b539804507fd307072294048',
                    'ae1aebf4b3fa9b983c895486612c02c766305410',
                    '24dcd68f2117b9f93662cb58cc31bf36d6e2867a',
                    '599725f0e1331d5f8aae24f22cd1ec768b10348d'
                ],
                'actions': [
                    {
                        'action': 'cut',
                        'parameters': {
                            'pattern': '-',
                            'column_name': 'birth'
                        }
                    },
                    {
                        'action': 'fillemptywithdefault',
                        'parameters': {
                            'default_value': 'N/A',
                            'column_name': 'state'
                        }
                    },
                    {
                        'action': 'uppercase',
                        'parameters': {
                            'column_name': 'lastname'
                        }
                    }
                ]
            },
            {
                'id': 'ds3f51sf3q1df35qsf412qdsf15ds3ff454qg8r4qr',
                'dataSetId': datasets[2].id,
                'author': 'anonymousUser',
                'lastModificationDate': 1437487330692,
                'steps': [
                    '87e38cv438dth4yd6k84x3dr84htryj84xc3k21u'
                ],
                'actions': [
                    {
                        'action': 'cut',
                        'parameters': {
                            'pattern': '-',
                            'column_name': 'birth'
                        }
                    }
                ]
            },
            {
                'id': '8v4z38u4n3ioç43f815c3w5v4by3h8u4w3fv4bgyds',
                'dataSetId': datasets[1].id,
                'author': 'anonymousUser',
                'creationDate': 1437497330694,
                'steps': [
                    '3w8xt4hxt3fh125ydx8y6j4i8l4ds358g4zfbe3e'
                ],
                'actions': [
                    {
                        'action': 'cut',
                        'parameters': {
                            'pattern': '-',
                            'column_name': 'birth'
                        }
                    }
                ]
            }
        ];
    }

    var sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    var orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.services.dataset', function ($provide) {
        stateMock = {
            inventory: {
                datasets: [],
                sortList: sortList,
                orderList: orderList,
                sort: sortList[1],
                order: orderList[1]
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, DatasetRestService, StateService) {
        initDatasets();
        initPreparations();

        spyOn(DatasetRestService, 'getDatasets').and.returnValue($q.when({data: datasets.slice(0)}));
        spyOn(DatasetRestService, 'create').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'import').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'update').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'delete').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'clone').and.returnValue($q.when(true));
        spyOn(DatasetRestService, 'processCertification').and.returnValue($q.when(true));

        spyOn(StateService, 'setDatasets').and.returnValue();
        spyOn(StateService, 'removeDataset').and.returnValue();
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

    it('should create dataset', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {name: 'my dataset'};
        var folder = {id : '', path: '', name: 'Home'};

        //when
        DatasetListService.create(dataset, folder);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.create).toHaveBeenCalledWith(dataset, folder);
    }));

    it('should import remote dataset', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var importParameters = {
            type: 'http',
            name: 'great remote dataset',
            url: 'moc.dnelat//:ptth'
        };
        var folder = {id : '', path: '', name: 'Home'};

        //when
        DatasetListService.importRemoteDataset(importParameters, folder);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.import).toHaveBeenCalledWith(importParameters, folder);
    }));

    it('should refresh datasets list on dataset creation', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {name: 'my dataset'};

        //when
        DatasetListService.create(dataset);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.getDatasets).toHaveBeenCalled();
    }));

    it('should update dataset', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {name: 'my dataset'};

        //when
        DatasetListService.update(dataset);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.update).toHaveBeenCalledWith(dataset);
    }));

    it('should refresh datasets list on dataset update', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {name: 'my dataset'};

        //when
        DatasetListService.update(dataset);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.getDatasets).toHaveBeenCalled();
    }));

    it('should process certification on dataset', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {id: '6a543545de46512bf8651c'};

        //when
        DatasetListService.processCertification(dataset);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.processCertification).toHaveBeenCalledWith('6a543545de46512bf8651c');
    }));

    it('should refresh datasets list on dataset certification', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        var dataset = {id: '6a543545de46512bf8651c'};

        //when
        DatasetListService.processCertification(dataset);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.getDatasets).toHaveBeenCalled();
    }));

    it('should delete dataset', inject(function ($rootScope, DatasetListService, DatasetRestService) {
        //given
        stateMock.inventory.datasets = datasets.slice(0);

        //when
        DatasetListService.delete(datasets[0]);
        $rootScope.$apply();

        //then
        expect(DatasetRestService.delete).toHaveBeenCalledWith(datasets[0]);
    }));

    it('should remove dataset from its internal list', inject(function ($rootScope, DatasetListService, StateService) {
        //given
        stateMock.inventory.datasets = datasets.slice(0);

        //when
        DatasetListService.delete(datasets[0]);
        $rootScope.$apply();

        //then
        expect(StateService.removeDataset).toHaveBeenCalledWith(datasets[0]);
    }));

    it('should init default preparations in datasets', inject(function ($rootScope, DatasetListService) {
        //given
        stateMock.inventory.datasets = datasets.slice(0);

        //when
        DatasetListService.refreshPreparations(preparations);
        $rootScope.$apply();

        //then
        expect(datasets[0].preparations[0].id).toBe(preparations[0].id);
        expect(datasets[1].preparations[0].id).toBe(preparations[3].id);
        expect(datasets[2].preparations[0].id).toBe(preparations[2].id);// due to sort process
        expect(datasets[2].preparations[1].id).toBe(preparations[1].id);
        expect(datasets[3].preparations).toEqual([]);
    }));

    it('should fetch datasets when not already initialized and init default preparations in datasets', inject(function ($rootScope, DatasetListService) {
        //given
        stateMock.inventory.datasets = null;

        //when
        DatasetListService.refreshPreparations(preparations);
        $rootScope.$apply();

        //then
        expect(datasets[0].preparations[0].id).toBe(preparations[0].id);
        expect(datasets[1].preparations[0].id).toBe(preparations[3].id);
        expect(datasets[2].preparations[0].id).toBe(preparations[2].id);
        expect(datasets[2].preparations[1].id).toBe(preparations[1].id);
        expect(datasets[3].preparations).toEqual([]);
    }));

    it('should return datasets after init default preparations in datasets', inject(function ($rootScope, DatasetListService) {
        //given
        stateMock.inventory.datasets = null;
        var result = [];

        //when
        DatasetListService.refreshPreparations(preparations)
            .then(function (promiseResult) {
                result = promiseResult;
            });
        $rootScope.$apply();

        //then
        expect(result).toEqual(datasets);
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
        var datasetsPromise = DatasetListService.hasDatasetsPromise();

        //then
        expect(datasetsPromise).toBeFalsy();
    }));

    it('should call rest service clone', inject(function (DatasetRestService, DatasetListService) {
        var folder = {id: 'foo'};

        //when
        DatasetListService.clone(datasets[0], folder, 'beer');

        //then
        expect(DatasetRestService.clone).toHaveBeenCalledWith(datasets[0], folder, 'beer');
    }));
});