describe('Preparation Service', () => {
    'use strict';

    let stateMock;
    const preparations = [
        { id: '4385fa764bce39593a405d91bc88', dataSetId: '3214a5454ef8642c13' },
        { id: '58444bce39593a405d9456' },
        { id: '2545764bce39593a405d91bc8673' }
    ];
    const newPreparationId = '6cd546546548a745';

    const updatedDatasetId = '99ac8561e62f34131';
    const updatedPreparationId = '5ea51464f515125e3';

    beforeEach(angular.mock.module('data-prep.services.preparation', ($provide) => {
        stateMock = {
            inventory: {
                preparations: null
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PreparationListService, PreparationRestService, StorageService) => {
        spyOn(PreparationListService, 'refreshPreparations').and.returnValue($q.when(preparations));
        spyOn(PreparationListService, 'create').and.returnValue($q.when({ id: newPreparationId }));
        spyOn(PreparationListService, 'update').and.returnValue($q.when({
            id: updatedPreparationId,
            dataSetId: updatedDatasetId
        }));
        spyOn(PreparationListService, 'delete').and.returnValue($q.when(true));
        spyOn(PreparationListService, 'copy').and.returnValue($q.when(true));

        spyOn(PreparationRestService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getContent').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getDetails').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewDiff').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewUpdate').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewAdd').and.returnValue($q.when(true));

        spyOn(StorageService, 'savePreparationAggregationsFromDataset').and.returnValue();
        spyOn(StorageService, 'removeAllAggregations').and.returnValue();
        spyOn(StorageService, 'moveAggregations').and.returnValue();
    }));

    describe('getter/refresher', () => {
        it('should return the pending refresh promise when it is defined', inject(($q, $rootScope, PreparationService, PreparationListService) => {
            //given
            const fetchPromise = $q.when(preparations);
            spyOn(PreparationListService, 'getPreparationsPromise').and.returnValue(fetchPromise);
            spyOn(PreparationListService, 'hasPreparationsPromise').and.returnValue(true);
            expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();

            let result = null;

            //when
            PreparationService.getPreparations().then((promiseResult) => result = promiseResult);
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
            expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();
        }));

        it('should return preparations from state when there are already fetched', inject((state, $rootScope, PreparationService, PreparationListService) => {
            //given
            stateMock.inventory.preparations = preparations;
            spyOn(PreparationListService, 'hasPreparationsPromise').and.returnValue(false);
            expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();

            let result = null;

            //when
            PreparationService.getPreparations().then((promiseResult) => result = promiseResult);
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
            expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();
        }));

        it('should fetch preparations when they are not already fetched', inject(($rootScope, PreparationService, PreparationListService) => {
            //given
            spyOn(PreparationListService, 'hasPreparationsPromise').and.returnValue(null);
            stateMock.inventory.preparations = null;
            expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();
            let result = null;

            //when
            PreparationService.getPreparations().then((promiseResult) => result = promiseResult);
            $rootScope.$digest();

            //then
            expect(result).toBe(preparations);
            expect(PreparationListService.refreshPreparations).toHaveBeenCalled();
        }));
    });

    describe('lifecycle', () => {
        describe('create', () => {
            it('should create a new preparation', inject(($rootScope, PreparationService, PreparationListService) => {
                //given
                const datasetId = '2430e5df845ab6034c85';
                const name = 'my preparation';
                expect(PreparationListService.create).not.toHaveBeenCalled();

                //when
                PreparationService.create(datasetId, name);
                $rootScope.$digest();

                //then
                expect(PreparationListService.create).toHaveBeenCalledWith(datasetId, name);
            }));

            it('should save aggregations for preparation from dataset aggregations', inject(($rootScope, PreparationService, StorageService) => {
                //given
                const datasetId = '2430e5df845ab6034c85';
                expect(StorageService.savePreparationAggregationsFromDataset).not.toHaveBeenCalled();

                //when
                PreparationService.create(datasetId, 'my preparation');
                $rootScope.$digest();

                //then
                expect(StorageService.savePreparationAggregationsFromDataset).toHaveBeenCalledWith(datasetId, newPreparationId);
            }));
        });

        describe('update', () => {
            it('should update current preparation name', inject(($rootScope, PreparationService, PreparationListService) => {
                //given
                const preparationId = '6cd546546548a745';
                const name = 'my preparation';
                expect(PreparationListService.update).not.toHaveBeenCalled();

                //when
                PreparationService.setName(preparationId, name);
                $rootScope.$digest();

                //then
                expect(PreparationListService.update).toHaveBeenCalledWith(preparationId, name);
            }));

            it('should move aggregations to the new preparation id key in localStorage', inject(($rootScope, PreparationService, StorageService) => {
                //given
                const preparationId = '6cd546546548a745';
                const name = 'my preparation';
                expect(StorageService.moveAggregations).not.toHaveBeenCalled();

                //when
                PreparationService.setName(preparationId, name);
                $rootScope.$digest();

                //then
                expect(StorageService.moveAggregations).toHaveBeenCalledWith(updatedDatasetId, preparationId, updatedPreparationId);
            }));
        });

        describe('delete', () => {
            it('should delete a preparation', inject(($rootScope, PreparationService, PreparationListService) => {
                //given
                expect(PreparationListService.delete).not.toHaveBeenCalled();

                //when
                PreparationService.delete(preparations[0]);
                $rootScope.$digest();

                //then
                expect(PreparationListService.delete).toHaveBeenCalledWith(preparations[0]);
            }));

            it('should remove aggregations from storage', inject(($rootScope, PreparationService, PreparationListService, StorageService) => {
                //given
                stateMock.inventory.preparations = preparations;
                expect(StorageService.removeAllAggregations).not.toHaveBeenCalled();

                //when
                PreparationService.delete(preparations[0]);
                $rootScope.$digest();

                //then
                expect(StorageService.removeAllAggregations).toHaveBeenCalledWith(preparations[0].dataSetId, preparations[0].id);
            }));
        });

        describe('open', () => {
            it('should set previous state to preparations', inject(($stateParams, $rootScope, $state, StateService, PreparationService) => {
                //given
                spyOn($state, 'go').and.returnValue();
                spyOn(StateService, 'setPreviousRoute').and.returnValue();

                const preparation = {
                    id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                    dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
                    author: 'anonymousUser'
                };
                $stateParams.folderPath = 'test/';

                //when
                PreparationService.open(preparation);
                $rootScope.$digest();

                //then
                expect(StateService.setPreviousRoute).toHaveBeenCalledWith('nav.index.preparations', { folderPath: 'test/' });
            }));

            it('should open a preparation', inject(($stateParams, $rootScope, $state, StateService, PreparationService) => {
                //given
                spyOn($state, 'go').and.returnValue();
                spyOn(StateService, 'setPreviousRoute').and.returnValue();

                const preparation = {
                    id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                    dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
                    author: 'anonymousUser'
                };

                //when
                PreparationService.open(preparation);
                $rootScope.$digest();

                //then
                expect($state.go).toHaveBeenCalledWith('playground.preparation', { prepid: preparation.id });
            }));
        });
    });

    describe('steps', () => {
        describe('copyImplicitParameters', () => {
            it('should copy implicit parameters when they are in original params', inject((PreparationService) => {
                //given
                const newParams = { value: 'tata' };
                const oldParams = {
                    value: 'toto',
                    scope: 'cell',
                    column_id: '0001',
                    row_id: '256',
                    column_name: 'state'
                };

                //when
                PreparationService.copyImplicitParameters(newParams, oldParams);

                //then
                expect(newParams).toEqual({
                    value: 'tata',
                    scope: 'cell',
                    column_id: '0001',
                    row_id: '256',
                    column_name: 'state'
                });
            }));

            it('should NOT copy implicit parameters when they are NOT in original params', inject((PreparationService) => {
                //given
                const newParams = { value: 'tata' };
                const oldParams = { value: 'toto', scope: 'cell' };

                //when
                PreparationService.copyImplicitParameters(newParams, oldParams);

                //then
                expect(newParams).toEqual({ value: 'tata', scope: 'cell' });
            }));
        });

        describe('paramsHasChanged', () => {
            it('should return true if the parameters are different', inject((PreparationService) => {
                //given
                const step = {
                    column: {
                        id: '1',
                        name: 'firstname'
                    },
                    actionParameters: {
                        parameters: { value: '--', column_name: 'firstname', column_id: '1' }
                    }
                };
                const newParams = { value: '.' };

                //when
                const result = PreparationService.paramsHasChanged(step, newParams);

                //then
                expect(result).toBe(true);
            }));

            it('should return false if the parameters are the same', inject((PreparationService) => {
                //given
                const step = {
                    column: {
                        id: '1',
                        name: 'firstname'
                    },
                    actionParameters: {
                        parameters: { value: '--', column_id: '1', column_name: 'firstname' }
                    }
                };
                const newParams = { value: '--', column_id: '1', column_name: 'firstname' };

                //when
                const result = PreparationService.paramsHasChanged(step, newParams);

                //then
                expect(result).toBe(false);
            }));
        });

        describe('update', () => {
            it('should update a preparation step with provided parameters', inject(($rootScope, PreparationService, PreparationRestService) => {
                //given
                const preparationId = '6cd546546548a745';
                const step = {
                    transformation: {
                        stepId: '867654ab15edf576844c4',
                        name: 'deletematch'
                    },
                    column: { id: '1', name: 'firstname' }
                };
                const parameters = { value: 'Toto', column_name: 'firstname', column_id: '1', scope: 'column' };

                //when
                PreparationService.updateStep(preparationId, step, parameters);
                $rootScope.$digest();

                //then
                expect(PreparationRestService.updateStep).toHaveBeenCalledWith(
                    '6cd546546548a745', //prep id
                    '867654ab15edf576844c4',  //step id
                    {
                        action: 'deletematch', //step name
                        parameters: { value: 'Toto', column_name: 'firstname', column_id: '1', scope: 'column' } //params
                    }
                );
            }));
        });
    });
});