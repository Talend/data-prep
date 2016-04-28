/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation list service', () => {
    'use strict';

    let preparations, stateMock;
    const createdPreparationId = '54d85af494e1518bec54546';

    function initPreparations() {
        preparations = [
            {
                'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
                'dataSetId': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
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
                'dataSetId': '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
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
                'dataSetId': '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
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
                'dataSetId': '3b21388c-f54a-4334-9bef-748912d0806f',
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

    beforeEach(angular.mock.module('data-prep.services.preparation', ($provide) => {
        stateMock = {
            inventory: {
                preparations: null
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PreparationRestService, StateService) => {
        initPreparations();

        spyOn(PreparationRestService, 'create').and.returnValue($q.when({data: createdPreparationId}));
        spyOn(PreparationRestService, 'update').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'delete').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'copy').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'move').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'appendStep').and.returnValue($q.when(true));

        spyOn(StateService, 'setPreparations').and.returnValue();
        spyOn(StateService, 'removePreparation').and.returnValue();
    }));

    describe('getter/refresher', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should refresh preparations', inject(($rootScope, PreparationListService, StateService) => {
            //given
            expect(StateService.setPreparations).not.toHaveBeenCalled();

            //when
            PreparationListService.refreshPreparations();
            $rootScope.$digest();

            //then
            expect(StateService.setPreparations).toHaveBeenCalledWith(preparations);
        }));

        it('should return a promise resolving the preparation list if it is already fetched', inject(($q, $rootScope, PreparationListService, PreparationRestService) => {
            //given
            PreparationListService.refreshPreparations();

            //when
            PreparationListService.getPreparationsPromise();

            //then
            expect(PreparationRestService.getPreparations.calls.count()).toBe(1);
        }));

        it('should refresh preparation list when it is not already fetched', inject(($rootScope, PreparationListService, PreparationRestService) => {
            //given
            expect(PreparationRestService.getPreparations).not.toHaveBeenCalled();

            //when
            PreparationListService.getPreparationsPromise();

            //then
            expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        }));

        it('should save fetch promise and remove it on promise resolution', inject(($rootScope, PreparationListService) => {
            //given
            expect(PreparationListService.hasPreparationsPromise()).toBeFalsy();

            //when
            PreparationListService.refreshPreparations();
            expect(PreparationListService.hasPreparationsPromise()).toBeTruthy();
            $rootScope.$digest();

            //then
            expect(PreparationListService.hasPreparationsPromise()).toBeFalsy();
        }));
    });

    describe('getter/refresher errors', () => {
        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.reject());
        }));

        it('should refresh preparations when REST request is failed', inject(($rootScope, PreparationListService, StateService) => {

            //when
            PreparationListService.refreshPreparations();
            $rootScope.$digest();

            //then
            expect(StateService.setPreparations).toHaveBeenCalledWith([]);
        }));
    });

    describe('create', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should create a new preparation', inject((PreparationListService, PreparationRestService) => {
            //given
            stateMock.inventory.preparations = preparations;

            //when
            PreparationListService.create('84ab54cd867f4645a', 'my preparation');

            //then
            expect(PreparationRestService.create).toHaveBeenCalledWith('84ab54cd867f4645a', 'my preparation');
        }));

        it('should return created preparation id', inject(($rootScope, PreparationListService) => {
            //given
            let result = null;
            const datasetId = '84ab54cd867f4645a';
            const createdPreparation = {id: createdPreparationId};

            stateMock.inventory.preparations = preparations;

            //when
            PreparationListService.create(datasetId, 'my preparation')
                .then((prep) => result = prep);
            stateMock.inventory.preparations.push(createdPreparation); //simulate the preparation refresh after creation
            $rootScope.$digest();

            //then
            expect(result).toBe(createdPreparation);
        }));

        it('should refresh preparations list on creation', inject(($rootScope, PreparationListService, PreparationRestService) => {
            //given
            stateMock.inventory.preparations = preparations;

            //when
            PreparationListService.create('84ab54cd867f4645a', 'my preparation');
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        }));
    });

    describe('copy', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should copy a preparation', inject((PreparationListService, PreparationRestService) => {
            //given
            const folderPath = '/toto/tata/jso';
            const name = 'my_prep';

            //when
            PreparationListService.copy(preparations[0].id, folderPath, name);

            //then
            expect(PreparationRestService.copy).toHaveBeenCalledWith(preparations[0].id, folderPath, name);
        }));

        it('should refresh preparations list', inject(($rootScope, PreparationListService, PreparationRestService) => {
            //when
            PreparationListService.copy(preparations[0].id);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        }));
    });

    describe('move', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should move a preparation', inject((PreparationListService, PreparationRestService) => {
            //given
            const preparationId = '16de4f39e787932a1';
            const originPath = '/my/folder';
            const destinationPath = '/toto/tata/jso';
            const name = 'my_prep';

            //when
            PreparationListService.move(preparationId, originPath, destinationPath, name);

            //then
            expect(PreparationRestService.move).toHaveBeenCalledWith(preparationId, originPath, destinationPath, name);
        }));

        it('should refresh preparations list', inject(($rootScope, PreparationListService, PreparationRestService) => {
            //given
            const preparationId = '16de4f39e787932a1';
            const originPath = '/my/folder';
            const destinationPath = '/toto/tata/jso';
            const name = 'my_prep';

            //when
            PreparationListService.move(preparationId, originPath, destinationPath, name);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        }));
    });

    describe('update', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should update a preparation name', inject((PreparationListService, PreparationRestService) => {
            //given
            stateMock.inventory.preparations = preparations;

            //when
            PreparationListService.update('84ab54cd867f4645a', 'my preparation');

            //then
            expect(PreparationRestService.update).toHaveBeenCalledWith('84ab54cd867f4645a', {name: 'my preparation'});
        }));

        it('should refresh preparations list on creation', inject(($rootScope, PreparationListService, PreparationRestService) => {
            //given
            stateMock.inventory.preparations = preparations;

            //when
            PreparationListService.update('84ab54cd867f4645a', 'my preparation');
            $rootScope.$digest();

            //then
            expect(PreparationRestService.getPreparations).toHaveBeenCalled();
        }));
    });

    describe('delete', () => {

        beforeEach(inject(($q, PreparationRestService) => {
            spyOn(PreparationRestService, 'getPreparations').and.returnValue($q.when({data: preparations}));
        }));

        it('should delete a preparation', inject(($rootScope, PreparationListService, PreparationRestService, StateService) => {
            //given
            stateMock.inventory.preparations = preparations.slice(0);

            //when
            PreparationListService.delete(preparations[0]);
            $rootScope.$digest();

            //then
            expect(PreparationRestService.delete).toHaveBeenCalledWith(preparations[0].id);
            expect(StateService.removePreparation).toHaveBeenCalledWith(preparations[0]);
        }));
    });
});
