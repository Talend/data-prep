describe('Preparation list service', function() {
    'use strict';

    var preparations, datasets;
    var createdPreparationId = '54d85af494e1518bec54546';

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
                'creationDate': 1427447330693,
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
                'creationDate': 1437487330692,
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

    beforeEach(module('data-prep.services.preparation'));

    beforeEach(inject(function($q, PreparationRestService) {
        initDatasets();
        initPreparations();

        spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        spyOn(PreparationRestService, 'create').and.returnValue($q.when({data: createdPreparationId}));
        spyOn(PreparationRestService, 'update').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'delete').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'clone').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'appendStep').and.returnValue($q.when(true));
    }));

    it('should refresh preparations', inject(function($rootScope, PreparationListService) {
        //given
        expect(PreparationListService.preparations).toBeNull();

        //when
        PreparationListService.refreshPreparations();
        $rootScope.$digest();

        //then
        expect(PreparationListService.preparations).toBe(preparations);
    }));

    it('should not call refresh when a refresh is in progress', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.refreshPreparations();
        expect(PreparationRestService.getPreparations.calls.count()).toBe(1);

        //when
        PreparationListService.refreshPreparations();
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations.calls.count()).toBe(1);
    }));

    it('should return a promise resolving its internal preparation list if it is already fetched', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations;
        var result = null;

        //when
        PreparationListService.getPreparationsPromise()
            .then(function(promiseResult) {
                result = promiseResult;
            });
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations).not.toHaveBeenCalled();
        expect(result).toBe(preparations);
    }));

    it('should refresh preparation list when it is not already fetched', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = null;
        var result = null;

        //when
        PreparationListService.getPreparationsPromise()
            .then(function(promiseResult) {
                result = promiseResult;
            });
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        expect(result).toBe(preparations);
    }));

    it('should create a new preparation', inject(function(PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations;

        //when
        PreparationListService.create('84ab54cd867f4645a', 'my preparation');

        //then
        expect(PreparationRestService.create).toHaveBeenCalledWith('84ab54cd867f4645a', 'my preparation');
    }));

    it('should return created preparation id', inject(function($rootScope, PreparationListService) {
        //given
        var result = null;
        var datasetId = '84ab54cd867f4645a';
        var createdPreparation = {id: createdPreparationId};

        PreparationListService.preparations = preparations;

        //when
        PreparationListService.create(datasetId, 'my preparation')
            .then(function(prep) {
                result = prep;
            });
        PreparationListService.preparations.push(createdPreparation); //simulate the preparation refresh after creation
        $rootScope.$digest();

        //then
        expect(result).toBe(createdPreparation);
    }));

    it('should refresh preparations list on creation', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations;

        //when
        PreparationListService.create('84ab54cd867f4645a', 'my preparation');
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations).toHaveBeenCalled();
    }));

    it('should update a preparation name', inject(function(PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations;

        //when
        PreparationListService.update('84ab54cd867f4645a', 'my preparation');

        //then
        expect(PreparationRestService.update).toHaveBeenCalledWith('84ab54cd867f4645a', 'my preparation');
    }));

    it('should refresh preparations list on creation', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations;

        //when
        PreparationListService.update('84ab54cd867f4645a', 'my preparation');
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations).toHaveBeenCalled();
    }));

    it('should delete a preparation', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations.slice(0);
        var expectedListWithoutFirstPreparation = preparations.slice(1);

        //when
        PreparationListService.delete(preparations[0]);
        $rootScope.$digest();

        //then
        expect(PreparationRestService.delete).toHaveBeenCalledWith(preparations[0].id);
        expect(PreparationListService.preparations).toEqual(expectedListWithoutFirstPreparation);
    }));

    it('should init preparations dataset', inject(function ($rootScope, PreparationListService) {
        //given
        PreparationListService.preparations = preparations;

        //when
        PreparationListService.refreshMetadataInfos(datasets);
        $rootScope.$apply();

        //then
        expect(PreparationListService.preparations[0].dataset).toBe(datasets[0]);
        expect(PreparationListService.preparations[1].dataset).toBe(datasets[2]);
    }));

    it('should fetch preparations when not already initialized and init preparations dataset', inject(function ($rootScope, PreparationListService) {
        //given
        PreparationListService.preparations = null;

        //when
        PreparationListService.refreshMetadataInfos(datasets);
        $rootScope.$apply();

        //then
        expect(PreparationListService.preparations[0].dataset).toBe(datasets[0]);
        expect(PreparationListService.preparations[1].dataset).toBe(datasets[2]);
    }));

    it('should return preparations after init preparations dataset', inject(function ($rootScope, PreparationListService) {
        //given
        PreparationListService.preparations = null;
        var result = [];

        //when
        PreparationListService.refreshMetadataInfos(datasets)
            .then(function(promiseResult) {
                result = promiseResult;
            });
        $rootScope.$apply();

        //then
        expect(result).toEqual(preparations);
    }));

    it('should clone a preparation', inject(function($q, $rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations.slice(0);

        //when
        PreparationListService.clone(preparations[0].id);
        $rootScope.$digest();

        //then
        expect(PreparationRestService.clone).toHaveBeenCalledWith(preparations[0].id);
    }));

    it('should refresh preparations list on clone', inject(function($rootScope, PreparationListService, PreparationRestService) {
        //given
        PreparationListService.preparations = preparations.slice(0);

        //when
        PreparationListService.clone(preparations[0].id);
        $rootScope.$digest();

        //then
        expect(PreparationRestService.getPreparations).toHaveBeenCalled();
    }));
});
