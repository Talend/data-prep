describe('Preparation list controller', function () {
    'use strict';

    var createController, scope;
    var allPreparations = [
        {
            'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            'dataSetId': 'ddb74c89-6d23-4528-9f37-7a9860bb468e',
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
        }
    ];

    beforeEach(module('data-prep.preparation-list'));

    beforeEach(inject(function ($q, $rootScope, $controller, PreparationService, PlaygroundService, MessageService, StateService) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('PreparationListCtrl', {
                $scope: scope
            });
            return ctrl;
        };

        spyOn($rootScope, '$emit').and.returnValue();

        spyOn(PreparationService, 'clone').and.returnValue($q.when(true));
        spyOn(PreparationService, 'delete').and.returnValue($q.when(true));
        spyOn(PreparationService, 'getPreparations').and.callFake(function () {
            return $q.when(allPreparations);
        });
        spyOn(PreparationService, 'preparationsList').and.returnValue(allPreparations);
        spyOn(PreparationService, 'setName').and.returnValue($q.when(true));
        spyOn(PlaygroundService, 'load').and.returnValue($q.when(true));
        spyOn(StateService, 'showPlayground').and.returnValue();
        spyOn(MessageService, 'success').and.returnValue(null);
        spyOn(MessageService, 'error').and.returnValue(null);
    }));

    afterEach(inject(function ($stateParams) {
        $stateParams.prepid = null;
    }));

    it('should init preparations', inject(function (PreparationService) {
        //given

        //when
        createController();
        scope.$digest();

        //then
        expect(PreparationService.getPreparations).toHaveBeenCalled();
    }));

    it('should load preparation if requested in url', inject(function ($stateParams, PlaygroundService, StateService) {
        //given
        $stateParams.prepid = 'fbaa18e82e913e97e5f0e9d40f04413412be1126';

        //when
        createController();
        scope.$digest();

        //then
        expect(PlaygroundService.load).toHaveBeenCalledWith(allPreparations[1]);
        expect(StateService.showPlayground).toHaveBeenCalled();
    }));

    it('should show error message if requested preparation is not in preparation list', inject(function ($stateParams, PlaygroundService, MessageService, StateService) {
        //given
        $stateParams.prepid = 'azerty';

        //when
        createController();
        scope.$digest();

        //then
        expect(PlaygroundService.load).not.toHaveBeenCalled();
        expect(StateService.showPlayground).not.toHaveBeenCalled();
        expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'preparation'});
    }));

    it('should load preparation and show playground', inject(function (PlaygroundService, StateService) {
        //given
        var ctrl = createController();
        var preparation = {
            id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
            author: 'anonymousUser',
            creationDate: 1427460984585,
            steps: [
                '228c16230de53de5992eb44c7aba362ac714ab1c'
            ],
            actions: []
        };
        expect(PlaygroundService.load).not.toHaveBeenCalled();

        //when
        ctrl.load(preparation);
        scope.$digest();

        //then
        expect(PlaygroundService.load).toHaveBeenCalledWith(preparation);
        expect(StateService.showPlayground).toHaveBeenCalled();
    }));

    it('should delete preparation, show success message on confirm', inject(function ($q, TalendConfirmService, PreparationService, MessageService) {
        //given
        spyOn(TalendConfirmService, 'confirm').and.returnValue($q.when(true));

        var ctrl = createController();
        var preparation = {
            id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            name: 'my preparation'
        };

        //when
        ctrl.delete(preparation);
        scope.$digest();

        //then
        expect(TalendConfirmService.confirm).toHaveBeenCalledWith({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
            type: 'preparation',
            name: preparation.name
        });
        expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
        expect(MessageService.success).toHaveBeenCalledWith('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
            type: 'preparation',
            name: preparation.name
        });
    }));

    it('should do nothing on delete dismiss', inject(function ($q, TalendConfirmService, PreparationService, MessageService) {
        //given
        spyOn(TalendConfirmService, 'confirm').and.returnValue($q.reject(null));

        var ctrl = createController();
        var preparation = {
            id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            name: 'my preparation'
        };

        //when
        ctrl.delete(preparation);
        scope.$digest();

        //then
        expect(TalendConfirmService.confirm).toHaveBeenCalledWith({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
            type: 'preparation',
            name: preparation.name
        });
        expect(PreparationService.delete).not.toHaveBeenCalled();
        expect(MessageService.success).not.toHaveBeenCalled();
    }));

    describe('rename', function () {

        it('should call preparation service to rename the preparation', inject(function (PreparationService) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer', name: 'my old name'};
            var name = 'new preparation name';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(PreparationService.setName).toHaveBeenCalledWith(preparation.id, name);
        }));

        it('should show success message on success', inject(function (MessageService) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer', name: 'my old name'};
            var name = 'new preparation name';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('PREPARATION_RENAME_SUCCESS_TITLE', 'PREPARATION_RENAME_SUCCESS');
        }));

        it('should manage loader screen', inject(function ($rootScope) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer', name: 'my old name'};

            expect($rootScope.$emit).not.toHaveBeenCalled();

            //when
            ctrl.rename(preparation, 'new preparation name');
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            scope.$digest();

            //then
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));

        it('should not call preparation service to rename the preparation with empty name', inject(function (PreparationService, MessageService) {
            //given

            var ctrl = createController();
            var preparation = {id: 'foo_beer', name: 'my old name'};
            var name = '';

            //when
            ctrl.rename(preparation, name);
            scope.$digest();

            //then
            expect(PreparationService.setName).not.toHaveBeenCalled();
            expect(MessageService.success).not.toHaveBeenCalled();
        }));

        it('should not call preparation service to rename the preparation with null name', inject(function (PreparationService, MessageService) {
            //given

            var ctrl = createController();
            var preparation = {id: 'foo_beer', name: 'my old name'};

            //when
            ctrl.rename(preparation);
            scope.$digest();

            //then
            expect(PreparationService.setName).not.toHaveBeenCalled();
            expect(MessageService.success).not.toHaveBeenCalled();

        }));
    });

    describe('clone', function () {

        it('should call preparation service to clone the preparation', inject(function (PreparationService) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer'};

            //when
            ctrl.clone(preparation);
            scope.$digest();

            //then
            expect(PreparationService.clone).toHaveBeenCalledWith(preparation.id);
        }));

        it('should show message on success', inject(function (MessageService) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer'};

            expect(MessageService.success).not.toHaveBeenCalled();

            //when
            ctrl.clone(preparation);
            scope.$digest();

            //then
            expect(MessageService.success).toHaveBeenCalledWith('PREPARATION_CLONING_SUCCESS_TITLE', 'PREPARATION_CLONING_SUCCESS');
        }));

        it('should manage loader screen', inject(function ($rootScope) {
            //given
            var ctrl = createController();
            var preparation = {id: 'foo_beer'};

            expect($rootScope.$emit).not.toHaveBeenCalled();

            //when
            ctrl.clone(preparation);
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.start');
            scope.$digest();

            //then
            expect($rootScope.$emit).toHaveBeenCalledWith('talend.loading.stop');
        }));

    });

});
