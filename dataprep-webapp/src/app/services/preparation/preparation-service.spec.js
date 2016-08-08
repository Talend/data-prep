describe('Preparation Service', () => {
    'use strict';

    let stateMock;
    const preparations = [
        { id: '4385fa764bce39593a405d91bc88', dataSetId: '3214a5454ef8642c13' },
        { id: '58444bce39593a405d9456' },
        { id: '2545764bce39593a405d91bc8673' },
    ];
    const newPreparationId = '6cd546546548a745';

    const updatedDatasetId = '99ac8561e62f34131';
    const updatedPreparationId = '5ea51464f515125e3';

    beforeEach(angular.mock.module('data-prep.services.preparation', ($provide) => {
        stateMock = {
            inventory: {
                homeFolderId: 'LW==',
                preparations: null,
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, PreparationRestService, StorageService) => {
        spyOn(PreparationRestService, 'create').and.returnValue($q.when({ id: newPreparationId }));
        spyOn(PreparationRestService, 'update').and.returnValue($q.when({
            id: updatedPreparationId,
            dataSetId: updatedDatasetId,
        }));
        spyOn(PreparationRestService, 'delete').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'copy').and.returnValue($q.when(true));

        spyOn(PreparationRestService, 'updateStep').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getContent').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getDetails').and.returnValue($q.when({
            id: newPreparationId,
            dataSetId: updatedDatasetId,
        }));
        spyOn(PreparationRestService, 'getPreviewDiff').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewUpdate').and.returnValue($q.when(true));
        spyOn(PreparationRestService, 'getPreviewAdd').and.returnValue($q.when(true));

        spyOn(StorageService, 'savePreparationAggregationsFromDataset').and.returnValue();
        spyOn(StorageService, 'removeAllAggregations').and.returnValue();
        spyOn(StorageService, 'moveAggregations').and.returnValue();
    }));

    afterEach(inject((StateService) => {
        StateService.resetRoute();
    }));

    describe('lifecycle', () => {
        describe('create', () => {
            beforeEach(inject((StateService) => {
                spyOn(StateService, 'setPreviousRoute').and.returnValue();
            }));

            it('should set previous route', inject(($stateParams, $rootScope, PreparationService, StateService) => {
                //given
                const datasetId = '2430e5df845ab6034c85';
                const name = 'my preparation';
                $stateParams.folderId = '';

                //when
                PreparationService.create(datasetId, name, 'destinationFolder');

                //then
                expect(StateService.setPreviousRoute).toHaveBeenCalledWith('nav.index.preparations', { folderId: $stateParams.folderId });
            }));

            it('should create a new preparation', inject(($rootScope, PreparationService, PreparationRestService) => {
                //given
                const datasetId = '2430e5df845ab6034c85';
                const name = 'my preparation';
                expect(PreparationRestService.create).not.toHaveBeenCalled();

                //when
                PreparationService.create(datasetId, name, 'destinationFolder');
                $rootScope.$digest();

                //then
                expect(PreparationRestService.create).toHaveBeenCalledWith(datasetId, name, 'destinationFolder');
            }));

            it('should save aggregations for preparation from dataset aggregations', inject(($rootScope, PreparationService, StorageService) => {
                //given
                const datasetId = '2430e5df845ab6034c85';
                expect(StorageService.savePreparationAggregationsFromDataset).not.toHaveBeenCalled();

                //when
                PreparationService.create(datasetId, 'my preparation', 'destinationFolder');
                $rootScope.$digest();

                //then
                expect(StorageService.savePreparationAggregationsFromDataset).toHaveBeenCalledWith(datasetId, newPreparationId);
            }));
        });

        describe('update', () => {
            it('should update current preparation name', inject(($rootScope, PreparationService, PreparationRestService) => {
                //given
                const preparationId = '6cd546546548a745';
                const name = 'my preparation';
                expect(PreparationRestService.update).not.toHaveBeenCalled();

                //when
                PreparationService.setName(preparationId, name);
                $rootScope.$digest();

                //then
                expect(PreparationRestService.update).toHaveBeenCalledWith(preparationId, {name});
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
                expect(StorageService.moveAggregations).toHaveBeenCalledWith(updatedDatasetId, preparationId, newPreparationId);
            }));
        });

        describe('delete', () => {
            it('should delete a preparation', inject(($rootScope, PreparationService, PreparationRestService) => {
                //given
                expect(PreparationRestService.delete).not.toHaveBeenCalled();

                //when
                PreparationService.delete(preparations[0]);
                $rootScope.$digest();

                //then
                expect(PreparationRestService.delete).toHaveBeenCalledWith(preparations[0].id);
            }));

            it('should remove aggregations from storage', inject(($rootScope, PreparationService, StorageService) => {
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
            beforeEach(inject(($state) => {
                spyOn($state, 'href').and.returnValue('absoluetUrl');
            }));

            it('should open a preparation in the current tab', inject(($stateParams, $rootScope, $state, StateService, PreparationService) => {
                //given
                spyOn($state, 'go').and.returnValue();

                const preparation = {
                    id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
                    dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
                    author: 'anonymousUser',
                };
                $stateParams.folderId = 'test/';

                //when
                PreparationService.open(preparation);

                //then
                expect($state.go).toHaveBeenCalledWith('playground.preparation', {prepid: preparation.id});
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
                    column_name: 'state',
                };

                //when
                PreparationService.copyImplicitParameters(newParams, oldParams);

                //then
                expect(newParams).toEqual({
                    value: 'tata',
                    scope: 'cell',
                    column_id: '0001',
                    row_id: '256',
                    column_name: 'state',
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
                        name: 'firstname',
                    },
                    actionParameters: {
                        parameters: { value: '--', column_name: 'firstname', column_id: '1' },
                    },
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
                        name: 'firstname',
                    },
                    actionParameters: {
                        parameters: { value: '--', column_id: '1', column_name: 'firstname' },
                    },
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
                        name: 'deletematch',
                    },
                    column: { id: '1', name: 'firstname' },
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
                        parameters: { value: 'Toto', column_name: 'firstname', column_id: '1', scope: 'column' }, //params
                    }
                );
            }));
        });
    });
});
