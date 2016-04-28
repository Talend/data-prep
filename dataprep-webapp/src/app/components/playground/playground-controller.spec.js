/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Playground controller', () => {
    'use strict';

    let createController, scope, stateMock;
    const datasets = [
        {
            'id': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            'name': 'customers_jso_light',
            'author': 'anonymousUser',
            'records': 15,
            'nbLinesHeader': 1,
            'nbLinesFooter': 0,
            'created': '03-30-2015 08:06',
        },
        {
            'id': '3b21388c-f54a-4334-9bef-748912d0806f',
            'name': 'customers_jso',
            'author': 'anonymousUser',
            'records': 1000,
            'nbLinesHeader': 1,
            'nbLinesFooter': 0,
            'created': '03-30-2015 07:35',
        },
    ];

    const preparations = [
        {
            'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            'dataSetId': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            'author': 'anonymousUser',
            'creationDate': 1427447300300,
        },
        {
            'id': 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
            'dataSetId': '3b21388c-f54a-4334-9bef-748912d0806f',
            'author': 'anonymousUser',
            'creationDate': 1427447330693,
        },
    ];

    const createPreparation = { id: 'create-preparation-id'  };

    beforeEach(angular.mock.module('data-prep.playground', ($provide) => {
        stateMock = {
            route: {
              previous: 'nav.index.preparations',
            },
            playground: {
                dataset: {},
                lookup: { actions: [] },
                preparationName: '',
            },
            inventory: {
                currentFolder: { path: 'test' },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $q, $controller, $state, PlaygroundService, PreparationService) => {
        scope = $rootScope.$new();

        createController = () => {
            return $controller('PlaygroundCtrl', { $scope: scope });
        };

        spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(createPreparation));
        spyOn(PreparationService, 'setName').and.returnValue($q.when());
        spyOn(PreparationService, 'move').and.returnValue($q.when());
        spyOn($state, 'go').and.returnValue();
    }));

    afterEach(inject(($stateParams) => {
        $stateParams.prepid = null;
        $stateParams.datasetid = null;
    }));

    describe('bindings', () => {
        it('should bind hasActiveStep getter', inject((RecipeService) => {
            //given
            const ctrl = createController();
            expect(ctrl.hasActiveStep).toBeFalsy();

            //when
            RecipeService.getRecipe().push({inactive: false});

            //then
            expect(ctrl.hasActiveStep).toBe(true);
        }));
    });

    describe('initialization', () => {
        beforeEach(inject((MessageService, StateService) => {
            spyOn(StateService, 'setIsFetchingStats').and.returnValue();
            spyOn(MessageService, 'error').and.returnValue();
            stateMock.inventory = {preparations: preparations, datasets: datasets};
        }));

        describe('preparation', () => {
            it('should load playground', inject(($q, $stateParams, PlaygroundService) => {
                //given
                $stateParams.prepid = preparations[0].id;
                spyOn(PlaygroundService, 'load').and.returnValue($q.when());

                //when
                createController();

                //then
                expect(PlaygroundService.load).toHaveBeenCalledWith(preparations[0]);
            }));

            it('should go back to previous state when preparation does NOT exist', inject(($state, $stateParams, MessageService) => {
                //given
                $stateParams.prepid = 'non existing prep';
                $stateParams.datasetid = null;

                //when
                createController();

                //then
                expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'preparation'});
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));

            it('should go back to previous state when preparation content GET return an error', inject(($q, $state, $stateParams, MessageService, PlaygroundService) => {
                //given
                $stateParams.prepid = 'non existing prep';
                $stateParams.datasetid = null;
                spyOn(PlaygroundService, 'load').and.returnValue($q.reject());

                //when
                createController();

                //then
                expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'preparation'});
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));
        });

        describe('dataset', () => {
            it('should load playground', inject(($q, $stateParams, PlaygroundService) => {
                //given
                $stateParams.prepid = null;
                $stateParams.datasetid = 'de3cc32a-b624-484e-b8e7-dab9061a009c';
                spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when());

                //when
                createController();

                //then
                expect(PlaygroundService.initPlayground).toHaveBeenCalledWith(datasets[0]);
            }));

            it('should fetch statistics when they are not computed yet', inject(($q, $stateParams, PlaygroundService, StateService) => {
                //given
                $stateParams.prepid = null;
                $stateParams.datasetid = 'de3cc32a-b624-484e-b8e7-dab9061a009c';
                spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when());
                spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());

                //when
                createController();
                expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
                expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

                stateMock.playground.data = {metadata: {statistics: {frequencyTable: []}}}; // stats not computed
                scope.$digest();

                //then
                expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
                expect(PlaygroundService.updateStatistics).toHaveBeenCalled();
                expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
            }));

            it('should retry statistics fetch when the previous fetch has been rejected (stats not computed yet) with a delay of 1500ms', inject(($q, $timeout, $stateParams, PlaygroundService, StateService) => {
                //given
                let retry = 0;
                $stateParams.prepid = null;
                $stateParams.datasetid = 'de3cc32a-b624-484e-b8e7-dab9061a009c';
                spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when());
                spyOn(PlaygroundService, 'updateStatistics').and.callFake(() => {
                    if (retry === 0) {
                        retry++;
                        return $q.reject();
                    }
                    else {
                        return $q.when();
                    }
                });

                //when
                createController();
                expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
                expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

                stateMock.playground.data = {
                    metadata: {
                        columns: [{
                            statistics: {
                                frequencyTable: []       // stats not computed
                            }
                        }]
                    }
                };
                scope.$digest();

                expect(StateService.setIsFetchingStats.calls.count()).toBe(1);
                expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(true);
                expect(PlaygroundService.updateStatistics.calls.count()).toBe(1); //first call: rejected
                $timeout.flush(1500);

                //then
                expect(PlaygroundService.updateStatistics.calls.count()).toBe(2);
                expect(StateService.setIsFetchingStats).toHaveBeenCalledWith(false);
            }));

            it('should NOT fetch statistics when they are already computed', inject(($q, $stateParams, PlaygroundService, StateService) => {
                //given
                $stateParams.prepid = null;
                $stateParams.datasetid = 'de3cc32a-b624-484e-b8e7-dab9061a009c';
                spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.when());
                spyOn(PlaygroundService, 'updateStatistics').and.returnValue($q.when());

                //when
                expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
                createController();
                expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
                expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();

                stateMock.playground.data = {
                    metadata: {
                        columns: [{
                            statistics: {
                                frequencyTable: [{      // stats already computed
                                    value: 'toto',
                                    frequency: 10
                                }]
                            }
                        }]
                    }
                };
                scope.$digest();

                //then
                expect(StateService.setIsFetchingStats).not.toHaveBeenCalled();
                expect(PlaygroundService.updateStatistics).not.toHaveBeenCalled();
            }));

            it('should go back to previous state when dataset does NOT exist', inject(($state, $stateParams, MessageService) => {
                //given
                $stateParams.prepid = null;
                $stateParams.datasetid = 'non existing dataset';

                //when
                createController();

                //then
                expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'dataset'});
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));

            it('should go back to previous state when dataset content GET return an error', inject(($q, $state, $stateParams, MessageService, PlaygroundService) => {
                //given
                $stateParams.prepid = null;
                $stateParams.datasetid = 'non existing dataset';
                spyOn(PlaygroundService, 'initPlayground').and.returnValue($q.reject());

                //when
                createController();

                //then
                expect(MessageService.error).toHaveBeenCalledWith('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'dataset'});
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));
        });
    });

    describe('recipe header', () => {
        it('should create/update preparation with clean name on name edition confirmation', inject((PlaygroundService) => {
            //given
            const ctrl = createController();
            stateMock.playground.preparationName = '  my new name  ';

            //when
            ctrl.confirmPrepNameEdition(stateMock.playground.preparationName);

            //then
            expect(PlaygroundService.createOrUpdatePreparation).toHaveBeenCalledWith('my new name');
        }));

        it('should change route to preparation route on name edition confirmation', inject(($rootScope, $state) => {
            //given
            const ctrl = createController();
            stateMock.playground.preparationName = '  my new name  ';
            stateMock.playground.preparation = {id: 'fe6843da512545e'};

            //when
            ctrl.confirmPrepNameEdition(stateMock.playground.preparationName);
            $rootScope.$digest();

            //then
            expect($state.go).toHaveBeenCalledWith('playground.preparation', {prepid: createPreparation.id});
        }));

        it('should not call service create/updateName service if name is blank on name edition confirmation', inject((PlaygroundService) => {
            //given
            const ctrl = createController();

            //when
            ctrl.confirmPrepNameEdition(' ');

            //then
            expect(PlaygroundService.createOrUpdatePreparation).not.toHaveBeenCalled();
        }));
    });

    describe('lookup', () => {
        beforeEach(inject(($q, LookupService, StateService) => {
            spyOn(LookupService, 'initLookups').and.returnValue($q.when());
            spyOn(StateService, 'setLookupVisibility').and.returnValue();
        }));

        it('should load lookup panel when it is hidden', inject((LookupService) => {
            //given
            stateMock.playground.lookup.visibility = false;
            const ctrl = createController();

            //when
            ctrl.toggleLookup();

            //then
            expect(LookupService.initLookups).toHaveBeenCalled();
        }));

        it('should display lookup panel when it is hidden', inject((StateService) => {
            //given
            stateMock.playground.lookup.visibility = false;
            const ctrl = createController();

            //when
            ctrl.toggleLookup();
            scope.$digest();

            //then
            expect(StateService.setLookupVisibility).toHaveBeenCalledWith(true, undefined);
        }));

        it('should hide lookup panel when it is visible', inject((LookupService, StateService) => {
            //given
            stateMock.playground.lookup.visibility = true;
            const ctrl = createController();

            //when
            ctrl.toggleLookup();

            //then
            expect(LookupService.initLookups).not.toHaveBeenCalled();
            expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
        }));
    });

    describe('apply other preparation steps', () => {
        it('should display modal', () => {
            //given
            const ctrl = createController();
            ctrl.displayPreparationPicker = false;

            //when
            ctrl.showPreparationPicker();

            //then
            expect(ctrl.displayPreparationPicker).toBe(true);
        });

        it('should copy reference steps', inject(($q, PlaygroundService) => {
            //given
            const referenceId = '17614ef63541ba25c48';
            const ctrl = createController();
            ctrl.displayPreparationPicker = true;

            spyOn(PlaygroundService, 'copySteps').and.returnValue($q.when());

            //when
            ctrl.applySteps(referenceId);

            //then
            expect(PlaygroundService.copySteps).toHaveBeenCalledWith(referenceId);
        }));

        it('should hide picker after steps copy', inject(($q, PlaygroundService) => {
            //given
            const ctrl = createController();
            ctrl.displayPreparationPicker = true;

            spyOn(PlaygroundService, 'copySteps').and.returnValue($q.when());

            //when
            ctrl.applySteps();
            scope.$digest();

            //then
            expect(ctrl.displayPreparationPicker).toBe(false);
        }));
    });

    describe('close', () => {
        let ctrl;
        let preparation;

        beforeEach(inject(($q, PreparationService, StateService) => {
            preparation = {id: '9af874865e42b546', draft: true};
            stateMock.playground.preparation = preparation;
            stateMock.route.previous = 'nav.index.preparations';

            spyOn(PreparationService, 'delete').and.returnValue($q.when(true));
            spyOn(StateService, 'resetPlayground').and.returnValue();

            ctrl = createController();
        }));

        describe('before close', () => {
            it('should reset and redirect with NOT implicit preparation', inject(($timeout, $state, StateService) => {
                //given
                preparation.draft = false;
                expect(StateService.resetPlayground).not.toHaveBeenCalled();

                //when
                ctrl.beforeClose();
                $timeout.flush();
                //then
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));

            it('should reset and redirect with params', inject(($state) => {
                //given
                preparation.draft = false;
                stateMock.route.previous = 'nav.index.datasets';
                stateMock.route.previousOptions = {folderPath: 'test'};

                //when
                ctrl.beforeClose();

                //then
                expect($state.go).toHaveBeenCalledWith('nav.index.datasets', {folderPath: 'test'});
            }));

            it('should show preparation save/discard modal with implicit preparation', () => {
                //given
                expect(ctrl.showNameValidation).toBeFalsy();

                //when
                ctrl.beforeClose();

                //then
                expect(ctrl.showNameValidation).toBe(true);
            });
        });

        describe('discard preparation', () => {
            it('should delete current preparation', inject((PreparationService) => {
                //when
                ctrl.discardSaveOnClose();

                //then
                expect(PreparationService.delete).toHaveBeenCalledWith(preparation);
            }));

            it('should reset and redirect to previous route', inject(($timeout, $state, StateService) => {
                //when
                ctrl.discardSaveOnClose();
                scope.$digest();
                $timeout.flush();

                //then
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));
        });

        describe('save preparation', () => {
            it('should change preparation name when destination is home', inject((PreparationService) => {
                //given
                ctrl.destinationFolder = { path: '' };
                stateMock.playground.preparation = { id: '541da3f5c64' };
                stateMock.playground.preparationName = '  my preparation ';

                expect(PreparationService.setName).not.toHaveBeenCalled();

                //when
                ctrl.confirmSaveOnClose();

                //then
                expect(PreparationService.setName).toHaveBeenCalledWith('541da3f5c64', 'my preparation');
            }));

            it('should move preparation when destination has changed', inject((PreparationService) => {
                //given
                ctrl.destinationFolder = { path: '/my/folder' };
                stateMock.playground.preparation = { id: '541da3f5c64' };
                stateMock.playground.preparationName = '  my preparation ';

                expect(PreparationService.move).not.toHaveBeenCalled();

                //when
                ctrl.confirmSaveOnClose();

                //then
                expect(PreparationService.move).toHaveBeenCalledWith('541da3f5c64', '', '/my/folder', 'my preparation');
            }));

            it('should reset and redirect to previous route', inject(($timeout, $state, StateService) => {
                //given
                ctrl.destinationFolder = { path: '/my/folder' };

                //when
                ctrl.confirmSaveOnClose();
                scope.$digest();
                $timeout.flush();

                //then
                expect(StateService.resetPlayground).toHaveBeenCalled();
                expect($state.go).toHaveBeenCalledWith('nav.index.preparations', undefined);
            }));
        });
    });

    describe('feedback', () => {
        it('should open feedback modal', inject((StateService) => {
            //given
            spyOn(StateService, 'showFeedback').and.returnValue();
            const ctrl = createController();

            //when
            ctrl.openFeedbackForm();

            //then
            expect(StateService.showFeedback).toHaveBeenCalled();
        }));
    });

    describe('dataset parameters', () => {
        beforeEach(inject(($q, StateService, PlaygroundService) => {
            spyOn(StateService, 'hideDatasetParameters').and.returnValue();
            spyOn(StateService, 'toggleDatasetParameters').and.returnValue();
            spyOn(StateService, 'setIsSendingDatasetParameters').and.returnValue();
            spyOn(PlaygroundService, 'changeDatasetParameters').and.returnValue($q.when());
        }));

        it('should open dataset parameters', inject((StateService) => {
            //given
            const ctrl = createController();
            expect(StateService.toggleDatasetParameters).not.toHaveBeenCalled();

            //when
            ctrl.toggleParameters();

            //then
            expect(StateService.toggleDatasetParameters).toHaveBeenCalled();
        }));

        it('should manage progress flag', inject((StateService) => {
            //given
            const ctrl = createController();
            const parameters = {separator: ';', encoding: 'UTF-8'};

            expect(StateService.setIsSendingDatasetParameters).not.toHaveBeenCalled();

            //when
            ctrl.changeDatasetParameters(parameters);
            expect(StateService.setIsSendingDatasetParameters).toHaveBeenCalledWith(true);
            scope.$digest();

            //then
            expect(StateService.setIsSendingDatasetParameters).toHaveBeenCalledWith(false);
        }));

        it('should call parameter change function', inject((PlaygroundService) => {
            //given
            const ctrl = createController();
            const parameters = {separator: ';', encoding: 'UTF-8'};

            expect(PlaygroundService.changeDatasetParameters).not.toHaveBeenCalled();

            //when
            ctrl.changeDatasetParameters(parameters);

            //then
            expect(PlaygroundService.changeDatasetParameters).toHaveBeenCalled();
        }));

        it('should hide dataset parameters', inject((StateService) => {
            //given
            const ctrl = createController();
            const parameters = {separator: ';', encoding: 'UTF-8'};

            expect(StateService.hideDatasetParameters).not.toHaveBeenCalled();

            //when
            ctrl.changeDatasetParameters(parameters);
            scope.$digest();

            //then
            expect(StateService.hideDatasetParameters).toHaveBeenCalled();
        }));
    });
});
