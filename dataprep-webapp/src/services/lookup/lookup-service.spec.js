describe('lookup service', function () {
    'use strict';
    var stateMock;

    //lookup dataset content
    var firstDsLookupId = '9e739b88-5ec9-4b58-84b5-2127a7e2eac7';
    var dsLookupContent = {
        'metadata': {
            'id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
            'records': 3,
            'certificationStep': 'NONE',
            'location': {
                'type': 'local'
            },
            'name': 'lookup_2',
            'author': 'anonymous',
            'created': 1447689742940,
            'encoding': 'UTF-8',
            columns: [{id: '0000'}]
        }
    };

    //lookup actions
    var lookupActions = [
        {
            'category': 'data_blending',
            'name': 'lookup',
            'parameters': [
                {
                    'name': 'column_id',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'filter',
                    'type': 'filter',
                    'default': ''
                },
                {
                    'name': 'lookup_ds_name',
                    'type': 'string',
                    'default': 'lookup_2'
                },
                {
                    'name': 'lookup_ds_id',
                    'type': 'string',
                    'default': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7'
                },
                {
                    'name': 'lookup_join_on',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_join_on_name',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_selected_cols',
                    'type': 'list',
                    'default': ''
                }
            ]
        }
    ];
    var firstLookupAction = lookupActions[0];

    //recipe
    /*jshint camelcase: false */
    var lookupStep = {
        column: {
            id: '0000',
            name: 'id'
        },
        row: {
            id: '11'
        },
        transformation: {
            stepId: '72fe267d489b06890da69368f4760530b076ec59',
            name: 'lookup',
            label: 'Lookup',
            description: 'Blends columns from another dataset into this one',
            parameters: [],
            dynamic: false
        },
        actionParameters: {
            action: 'lookup',
            parameters: {
                column_id: '0000',
                column_name: 'id',
                filter: '',
                lookup_ds_id: '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
                lookup_ds_name: 'cluster_dataset',
                lookup_join_on: '0000',
                lookup_join_on_name: 'id',
                lookup_selected_cols: [
                    {
                        id: '0001',
                        name: 'uglystate'
                    }
                ],
                row_id: '11',
                scope: 'dataset'
            }
        },
        diff: {
            createdColumns: [
                '0009'
            ]
        },
        filters: null
    };

    beforeEach(module('data-prep.services.lookup', function ($provide) {
        stateMock = {
            playground: {
                data: null,
                dataset: {id: 'abcd'},
                lookup: {visibility: false},
                grid: {}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, TransformationRestService, DatasetRestService, StateService) {
        spyOn(DatasetRestService, 'getContent').and.returnValue($q.when(dsLookupContent));
        spyOn(StateService, 'setGridSelection').and.returnValue();
        spyOn(StateService, 'setLookupActions').and.returnValue();
        spyOn(StateService, 'setLookupAddMode').and.returnValue();
        spyOn(StateService, 'setLookupUpdateMode').and.returnValue();

        //spyOn(StateService, 'setLookupStep').and.returnValue();
        //spyOn(StateService, 'setLookupVisibility').and.returnValue();
        //spyOn(StateService, 'setLookupSelectedColumn').and.returnValue();
        //spyOn(StateService, 'updateLookupColumnsToAdd').and.returnValue();
    }));

    describe('init lookup', function () {
        describe('actions', function () {
            beforeEach(inject(function ($q, TransformationRestService) {
                spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({data: lookupActions}));
            }));

            it('should fetch lookup actions when they are not initialized yet', inject(function ($rootScope, $q, LookupService, StateService, TransformationRestService) {
                //given
                stateMock.playground.lookup.actions = [];

                //when
                LookupService.initLookups();
                $rootScope.$digest();

                //then
                expect(TransformationRestService.getDatasetTransformations).toHaveBeenCalled();
                expect(StateService.setLookupActions).toHaveBeenCalledWith(lookupActions);
            }));

            it('should NOT fetch lookup actions when they are already initialized', inject(function ($rootScope, $q, LookupService, StateService, TransformationRestService) {
                //given
                stateMock.playground.lookup.actions = lookupActions;

                //when
                LookupService.initLookups();
                $rootScope.$digest();

                //then
                expect(TransformationRestService.getDatasetTransformations).not.toHaveBeenCalled();
                expect(StateService.setLookupActions).not.toHaveBeenCalled();
            }));
        });

        describe('with no actions', function () {
            it('should NOT initialize lookup state', inject(function ($rootScope, $q, LookupService, StateService, TransformationRestService) {
                //given
                spyOn(TransformationRestService, 'getDatasetTransformations').and.returnValue($q.when({data: []}));
                stateMock.playground.lookup.actions = [];

                //when
                LookupService.initLookups();
                $rootScope.$digest();

                //then
                expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
                expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
            }));
        });

        describe('without lookup step on selected column', function () {
            it('should load the first action as new lookup', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
                //given
                stateMock.playground.lookup.actions = lookupActions;

                //when
                LookupService.initLookups();
                $rootScope.$digest();

                //then
                expect(DatasetRestService.getContent).toHaveBeenCalledWith(firstDsLookupId, true);
                expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
            }));
        });

        describe('with lookup step on selected column', function () {
            it('should load the step action as lookup update', inject(function ($rootScope, LookupService, DatasetRestService, RecipeService, StateService) {
                //given
                spyOn(RecipeService, 'getRecipe').and.returnValue([lookupStep]);
                stateMock.playground.data = {metadata: {columns: [{id: '0000'}]}};
                stateMock.playground.lookup.actions = lookupActions;
                stateMock.playground.grid.selectedColumn = {'id': '0000'};

                //when
                LookupService.initLookups();
                $rootScope.$digest();

                //then
                expect(DatasetRestService.getContent).toHaveBeenCalledWith(firstDsLookupId, true);
                expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
            }));
        });
    });

    describe('load from action', function () {
        it('should load action as new lookup', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;

            //when
            LookupService.loadFromAction(firstLookupAction);
            $rootScope.$digest();

            //then
            expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
        }));

        it('should load action as lookup update when selected column has this lookup action as step', inject(function ($rootScope, LookupService, RecipeService, StateService) {
            //given
            stateMock.playground.data = {metadata: {columns: [{id: '0000'}]}};
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.grid.selectedColumn = {id: '0000'};
            spyOn(RecipeService, 'getRecipe').and.returnValue([lookupStep]);

            //when
            LookupService.loadFromAction(firstLookupAction);
            $rootScope.$digest();

            //then
            expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
        }));

        it('should NOT change lookup state when the action is already loaded', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.lookup.dataset = firstLookupAction;
            stateMock.playground.lookup.step = null;

            //when
            LookupService.loadFromAction(firstLookupAction);
            $rootScope.$digest();

            //then
            expect(DatasetRestService.getContent).not.toHaveBeenCalled();
            expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
            expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
        }));
    });

    describe('load from step', function () {
        beforeEach(function () {
            stateMock.playground.data = {metadata: {columns: [{id: '0000'}]}};
        });

        it('should load action as lookup update', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;

            //when
            LookupService.loadFromStep(lookupStep);
            $rootScope.$digest();

            //then
            expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
        }));

        it('should set state grid selection to the step target', inject(function ($rootScope, LookupService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;

            //when
            LookupService.loadFromStep(lookupStep);
            $rootScope.$digest();

            //then
            expect(StateService.setGridSelection).toHaveBeenCalledWith({id: '0000'});
        }));

        it('should NOT change lookup state when the step lookup is already loaded', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.lookup.dataset = firstLookupAction;
            stateMock.playground.lookup.step = lookupStep;

            //when
            LookupService.loadFromStep(lookupStep);
            $rootScope.$digest();

            //then
            expect(DatasetRestService.getContent).not.toHaveBeenCalled();
            expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
            expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
        }));
    });

    describe('update target column', function () {
        it('should load action as new lookup on the current column when it was on an update mode in the previous column', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.lookup.dataset = firstLookupAction;
            stateMock.playground.lookup.step = lookupStep;
            stateMock.playground.lookup.visibility = true;

            //when
            LookupService.updateTargetColumn();
            $rootScope.$digest();

            //then
            expect(StateService.setLookupAddMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent);
        }));

        it('should load action as lookup update when the new selected column has this lookup action as step', inject(function ($rootScope, LookupService, RecipeService, StateService) {
            //given
            stateMock.playground.data = {metadata: {columns: [{id: '0000'}]}};
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.lookup.dataset = firstLookupAction;
            stateMock.playground.lookup.visibility = true;
            stateMock.playground.grid.selectedColumn = {id: '0000'};
            spyOn(RecipeService, 'getRecipe').and.returnValue([lookupStep]);

            //when
            LookupService.updateTargetColumn();
            $rootScope.$digest();

            //then
            expect(StateService.setLookupUpdateMode).toHaveBeenCalledWith(firstLookupAction, dsLookupContent, lookupStep);
        }));

        it('should NOT change state when the lookup is not visible', inject(function ($rootScope, LookupService, DatasetRestService, StateService) {
            //given
            stateMock.playground.lookup.actions = lookupActions;
            stateMock.playground.lookup.dataset = firstLookupAction;
            stateMock.playground.lookup.visibility = false;

            //when
            LookupService.updateTargetColumn();
            $rootScope.$digest();

            //then
            expect(StateService.setLookupAddMode).not.toHaveBeenCalled();
            expect(StateService.setLookupUpdateMode).not.toHaveBeenCalled();
        }));
    });
});