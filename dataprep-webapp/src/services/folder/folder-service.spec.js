describe('Folder services', function () {
    'use strict';

    var stateMock, datasets, preparations;

    var datasetResult = {
        'id': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
        'name': 'customers_jso_light',
        'author': 'anonymousUser',
        'records': 15,
        'nbLinesHeader': 1,
        'nbLinesFooter': 0,
        'created': '03-30-2015 08:06',
        'defaultPreparation': {
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
        }
    };

    beforeEach(module('data-prep.services.folder', function ($provide) {
        stateMock = {folder: {currentFolderContent: {}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'HOME_FOLDER': 'Home'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($q, FolderRestService) {
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
                'id': 'jghjjghjgh',
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

        spyOn(FolderRestService, 'create').and.returnValue($q.when());
        spyOn(FolderRestService, 'children').and.returnValue($q.when());
        spyOn(FolderRestService, 'search').and.returnValue($q.when());
        spyOn(FolderRestService, 'rename').and.returnValue($q.when());
        spyOn(FolderRestService, 'remove').and.returnValue($q.when());
    }));

    describe('simple REST calls', function () {
        it('should call rest children', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.children('/foo');

            //then
            expect(FolderRestService.children).toHaveBeenCalledWith('/foo');
        }));

        it('should call rest create', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.create('/foo');

            //then
            expect(FolderRestService.create).toHaveBeenCalledWith('/foo');
        }));

        it('should call rest rename', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.rename('foo', 'beer');

            //then
            expect(FolderRestService.rename).toHaveBeenCalledWith('foo', 'beer');
        }));

        it('should call rest remove', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.remove('foo');

            //then
            expect(FolderRestService.remove).toHaveBeenCalledWith('foo');
        }));

        it('should call rest search', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.search('path');

            //then
            expect(FolderRestService.search).toHaveBeenCalledWith('path');
        }));
    });

    describe('content', function () {
        var content;
        beforeEach(inject(function ($q, StateService, FolderRestService, DatasetListSortService, PreparationListService) {
            content = {
                data: {
                    folders: [{id: 'toto', path: 'toto', name: 'toto'}],
                    datasets: [datasets[0]]
                }
            };
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when(content));

            spyOn(DatasetListSortService, 'getSort').and.returnValue('name');
            spyOn(DatasetListSortService, 'getOrder').and.returnValue('asc');
            spyOn(StateService, 'setFoldersStack').and.returnValue();
            spyOn(StateService, 'setCurrentFolder').and.returnValue();
            spyOn(StateService, 'setCurrentFolderContent').and.returnValue();
            spyOn(PreparationListService, 'getPreparationsPromise').and.returnValue($q.when(preparations));
        }));

        it('should get folder content', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.getContent({id: '1/2', path: 'toto', name: 'toto'});
            $rootScope.$digest();

            //then
            expect(FolderRestService.getContent).toHaveBeenCalledWith('1/2', 'name', 'asc');
        }));

        it('should get content for root folder when there is no provided folder', inject(function ($rootScope, FolderService, FolderRestService) {
            //when
            FolderService.getContent();
            $rootScope.$digest();

            //then
            expect(FolderRestService.getContent).toHaveBeenCalledWith(undefined, 'name', 'asc');
        }));

        it('should populate state with folder content infos', inject(function ($rootScope, FolderService, StateService) {
            //when
            FolderService.getContent({id: '1/2', path: 'toto', name: 'toto'});
            $rootScope.$digest();

            //then
            expect(StateService.setCurrentFolder).toHaveBeenCalledWith({id: '1/2', path: 'toto', name: 'toto'});
            expect(StateService.setCurrentFolderContent).toHaveBeenCalledWith(content.data);
            expect(StateService.setFoldersStack).toHaveBeenCalledWith([
                {id: '/', path: '/', name: 'Home'},
                {id: '1', path: '1', name: '1'},
                {id: '1/2', path: '1/2', name: '2'}
            ]);
        }));
    });

    describe('populate menu children', function () {
        beforeEach(inject(function ($q, StateService, FolderRestService) {
            var content = {
                data: {
                    folders: [{id: 'toto', path: 'toto', name: 'toto'}]
                }
            };
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when(content));
            spyOn(StateService, 'setMenuChildren').and.returnValue();
        }));

        it('should call populateMenuChildren REST service', inject(function (FolderService, FolderRestService) {
            //when
            FolderService.populateMenuChildren({id: 'toto', path: 'toto', name: 'toto'});

            //then
            expect(FolderRestService.getContent).toHaveBeenCalledWith('toto');
        }));

        it('should call populateMenuChildren REST service without provided folder', inject(function (FolderService, FolderRestService) {
            //when
            FolderService.populateMenuChildren();

            //then
            expect(FolderRestService.getContent).toHaveBeenCalledWith(undefined);
        }));

        it('should set menu children state', inject(function ($rootScope, FolderService, StateService) {
            //when
            FolderService.populateMenuChildren({id: 'toto', path: 'toto', name: 'toto'});
            $rootScope.$digest();

            //then
            expect(StateService.setMenuChildren).toHaveBeenCalledWith([{id: 'toto', path: 'toto', name: 'toto'}]);
        }));
    });

    describe('default preparation', function () {
        it('should set default preparation in current folder datasets', inject(function (FolderService) {
            //given
            stateMock.folder.currentFolderContent.datasets = [datasets[0]];
            expect(stateMock.folder.currentFolderContent.datasets[0].defaultPreparation).toBeFalsy();

            //when
            FolderService.refreshDefaultPreparation(preparations);

            //then
            expect(stateMock.folder.currentFolderContent.datasets[0].defaultPreparation.id).toBe(datasetResult.defaultPreparation.id);
        }));

        it('should not set default preparation on dataset that has no preparation', inject(function ($q, $rootScope, FolderService) {
            //given
            var datasetWithoutPreparation = {
                'id': 'de3cc32a-b624-484e-xxxx-dab9061a009c',
                'name': 'customers_jso_light',
                'author': 'anonymousUser',
                'records': 15,
                'nbLinesHeader': 1,
                'nbLinesFooter': 0,
                'created': '03-30-2015 08:06'
            };
            stateMock.folder.currentFolderContent.datasets = [datasetWithoutPreparation];
            expect(stateMock.folder.currentFolderContent.datasets[0].defaultPreparation).toBeFalsy();

            //when
            FolderService.refreshDefaultPreparation(preparations);

            //then
            expect(stateMock.folder.currentFolderContent.datasets[0].defaultPreparation).toBeFalsy();
        }));
    });
});
