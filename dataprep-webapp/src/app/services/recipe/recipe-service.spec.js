describe('Recipe service', function () {
    'use strict';

    var preparationDetails = function () {
        return {
            id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4',
            dataSetId: 'db6c4ad8-77da-4a30-b29f-ca552706b058',
            author: 'anonymousUser',
            name: 'JSO prep',
            creationDate: 1427980028390,
            lastModificationDate: 1427980216038,
            steps: [
                'f6e172c33bdacbc69bca9d32b2bd78174712a171',
                '329ccf0cce42db4dc0ffa9f389c05ff7d75c1748',
                'ec87e2acda2b181fc7eb7c22d91e128c6d0434fc',
                '0c58ee3034114eb620b8e598e02c74172a43e96a',
                '1e1f41dd6d4554705abebd8d1896022acdbad217',
                'add60ff0f6de4c703fa75725ada38fb37af065e6',
                '2aba0e60054728f046d35315830bce9abc3c5249',
                '3543514689a35456884d54584fe5463c14dd6846',
            ],
            actions: [
                {
                    action: 'uppercase',
                    parameters: {
                        column_name: 'country',
                        filter: {
                            valid: {
                                field: '0000',
                            },
                        },
                    },
                },
                {
                    action: 'fillemptywithdefault',
                    parameters: {
                        default_value: 'M',
                        column_name: 'gender',
                    },
                },
                {
                    action: 'negate',
                    parameters: {
                        column_name: 'campain',
                    },
                },
                {
                    action: 'cut',
                    parameters: {
                        pattern: '.',
                        column_name: 'first_item',
                    },
                },
                {
                    action: 'textclustering',
                    parameters: {
                        Texa: 'Texas',
                        Tixass: 'Texas',
                        'Tex@s': 'Texas',
                        Massachusetts: 'Massachussets',
                        Masachusetts: 'Massachussets',
                        Massachussetts: 'Massachussets',
                        Massachusets: 'Massachussets',
                        Masachussets: 'Massachussets',
                        column_name: 'uglystate',
                        column_id: '1',
                    },
                },
                {
                    action: 'fillemptywithdefaultboolean',
                    parameters: {
                        default_value: 'True',
                        column_name: 'campain',
                    },
                },
                {
                    action: 'delete',
                    parameters: {
                        row_id: '125',
                    },
                },
            ],
            metadata: [
                {
                    compatibleColumnTypes: [
                        'STRING',
                    ],
                    category: 'case',
                    name: 'uppercase',
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'parameter.column_name.desc',
                            label: 'parameter.column_name.label',
                            default: '',
                            implicit: true,
                        },
                    ],
                    description: 'action.uppercase.desc',
                    label: 'action.uppercase.label',
                },
                {
                    compatibleColumnTypes: [
                        'STRING',
                    ],
                    name: 'fillemptywithdefault',
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'parameter.column_name.desc',
                            label: 'parameter.column_name.label',
                            default: '',
                            implicit: true,
                        },
                        {
                            name: 'default_value',
                            type: 'string',
                            description: 'parameter.default_value.desc',
                            label: 'parameter.default_value.label',
                            default: '',
                        },
                    ],
                    category: 'repair',
                    description: 'action.fillemptywithdefault.desc',
                    label: 'action.fillemptywithdefault.label',
                },
                {
                    compatibleColumnTypes: [
                        'BOOLEAN',
                    ],
                    category: 'boolean',
                    name: 'negate',
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'parameter.column_name.desc',
                            label: 'parameter.column_name.label',
                            default: '',
                            implicit: true,
                        },
                    ],
                    description: 'action.negate.desc',
                    label: 'action.negate.label',
                },
                {
                    compatibleColumnTypes: [
                        'STRING',
                    ],
                    category: 'repair',
                    name: 'cut',
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'parameter.column_name.desc',
                            label: 'parameter.column_name.label',
                            default: '',
                            implicit: true,
                        },
                        {
                            name: 'pattern',
                            type: 'string',
                            description: 'parameter.pattern.desc',
                            label: 'parameter.pattern.label',
                            default: '',
                        },
                    ],
                    description: 'action.cut.desc',
                    label: 'action.cut.label',
                },
                {
                    compatibleColumnTypes: [
                        'STRING',
                    ],
                    category: 'quickfix',
                    name: 'textclustering',
                    dynamic: true,
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'The column on which apply this action to',
                            label: 'Column',
                            default: '',
                            implicit: true,
                        },
                    ],
                    description: 'Replace all similar values with the right one',
                    label: 'Cluster',
                },
                {
                    compatibleColumnTypes: [
                        'BOOLEAN',
                    ],
                    name: 'fillemptywithdefaultboolean',
                    parameters: [
                        {
                            name: 'column_name',
                            type: 'string',
                            description: 'parameter.column_name.desc',
                            label: 'parameter.column_name.label',
                            default: '',
                            implicit: true,
                        },
                        {
                            name: 'default_value',
                            type: 'select',
                            description: 'parameter.default_value.desc',
                            label: 'parameter.default_value.label',
                            configuration: {
                                values: [
                                    { name: 'True', value: 'True' },
                                    { name: 'False', value: 'False' },
                                ],
                            },
                            default: 'True',
                        },
                    ],
                    category: 'repair',
                    description: 'action.fillemptywithdefaultboolean.desc',
                    label: 'action.fillemptywithdefaultboolean.label',
                },
                {
                    name: 'delete line',
                    parameters: [
                        {
                            name: 'row_id',
                            type: 'string',
                            description: 'parameter.row_id.desc',
                            label: 'parameter.row_id.label',
                            implicit: true,
                        },
                    ],
                    category: 'clean',
                    description: 'action.delete_single_line.desc',
                    label: 'action.delete_single_line.label',
                },
            ],
        };
    };

    var initialCluster = function () {
        return {
            titles: [
                'We found these values',
                'And we\'ll keep this value',
            ],
            clusters: [
                {
                    parameters: [
                        {
                            name: 'Texa',
                            type: 'boolean',
                            description: 'parameter.Texa.desc',
                            label: 'parameter.Texa.label',
                            default: null,
                        },
                        {
                            name: 'Tixass',
                            type: 'boolean',
                            description: 'parameter.Tixass.desc',
                            label: 'parameter.Tixass.label',
                            default: null,
                        },
                        {
                            name: 'Tex@s',
                            type: 'boolean',
                            description: 'parameter.Tex@s.desc',
                            label: 'parameter.Tex@s.label',
                            default: null,
                        },
                    ],
                    replace: {
                        name: 'replaceValue',
                        type: 'string',
                        description: 'parameter.replaceValue.desc',
                        label: 'parameter.replaceValue.label',
                        default: 'Texas',
                    },
                },
                {
                    parameters: [
                        {
                            name: 'Massachusetts',
                            type: 'boolean',
                            description: 'parameter.Massachusetts.desc',
                            label: 'parameter.Massachusetts.label',
                            default: null,
                        },
                        {
                            name: 'Masachusetts',
                            type: 'boolean',
                            description: 'parameter.Masachusetts.desc',
                            label: 'parameter.Masachusetts.label',
                            default: null,
                        },
                        {
                            name: 'Massachussetts',
                            type: 'boolean',
                            description: 'parameter.Massachussetts.desc',
                            label: 'parameter.Massachussetts.label',
                            default: null,
                        },
                        {
                            name: 'Massachusets',
                            type: 'boolean',
                            description: 'parameter.Massachusets.desc',
                            label: 'parameter.Massachusets.label',
                            default: null,
                        },
                        {
                            name: 'Masachussets',
                            type: 'boolean',
                            description: 'parameter.Masachussets.desc',
                            label: 'parameter.Masachussets.label',
                            default: null,
                        },
                    ],
                    replace: {
                        name: 'replaceValue',
                        type: 'string',
                        description: 'parameter.replaceValue.desc',
                        label: 'parameter.replaceValue.label',
                        default: 'Massachussets',
                    },
                },
            ],
        };
    };

    var expectedInitializedCluster = {
        titles: [
            'We found these values',
            'And we\'ll keep this value',
        ],
        clusters: [
            {
                parameters: [
                    {
                        name: 'Texa',
                        type: 'boolean',
                        description: 'parameter.Texa.desc',
                        label: 'parameter.Texa.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Tixass',
                        type: 'boolean',
                        description: 'parameter.Tixass.desc',
                        label: 'parameter.Tixass.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Tex@s',
                        type: 'boolean',
                        description: 'parameter.Tex@s.desc',
                        label: 'parameter.Tex@s.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                ],
                replace: {
                    name: 'replaceValue',
                    type: 'string',
                    description: 'parameter.replaceValue.desc',
                    label: 'parameter.replaceValue.label',
                    default: 'Texas',
                    value: 'Texas',
                    initialValue: 'Texas',
                    inputType: 'text',
                },
                initialActive: true,
            },
            {
                parameters: [
                    {
                        name: 'Massachusetts',
                        type: 'boolean',
                        description: 'parameter.Massachusetts.desc',
                        label: 'parameter.Massachusetts.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Masachusetts',
                        type: 'boolean',
                        description: 'parameter.Masachusetts.desc',
                        label: 'parameter.Masachusetts.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Massachussetts',
                        type: 'boolean',
                        description: 'parameter.Massachussetts.desc',
                        label: 'parameter.Massachussetts.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Massachusets',
                        type: 'boolean',
                        description: 'parameter.Massachusets.desc',
                        label: 'parameter.Massachusets.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                    {
                        name: 'Masachussets',
                        type: 'boolean',
                        description: 'parameter.Masachussets.desc',
                        label: 'parameter.Masachussets.label',
                        default: null,
                        value: true,
                        initialValue: true,
                    },
                ],
                replace: {
                    name: 'replaceValue',
                    type: 'string',
                    description: 'parameter.replaceValue.desc',
                    label: 'parameter.replaceValue.label',
                    default: 'Massachussets',
                    value: 'Massachussets',
                    initialValue: 'Massachussets',
                    inputType: 'text',
                },
                initialActive: true,
            },
        ],
    };

    var filtersFromTree = [];

    var stateMock;

    beforeEach(angular.mock.module('data-prep.services.recipe', function ($provide) {
        stateMock = {
            playground: {
                recipe: {
                    current: {
                        steps: [],
                    },
                },
                data: {
                    metadata: {
                        columns: [
                            { id: '1', name: 'firstname' },
                            { id: '2', name: 'lastname' },
                            { id: '3', name: 'age' },
                        ],
                    },
                },
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, StateService, ParametersService, TransformationService, FilterAdapterService) => {
        spyOn(ParametersService, 'resetParamValue').and.returnValue();
        spyOn(ParametersService, 'initParamsValues').and.callThrough();
        spyOn(TransformationService, 'initDynamicParameters').and.callFake((transformation) => {
            transformation.cluster = initialCluster();
            return $q.when(transformation);
        });
        spyOn(FilterAdapterService, 'fromTree').and.returnValue(filtersFromTree);
        spyOn(StateService, 'setRecipeSteps').and.callFake((initialStep, steps) => {
            stateMock.playground.recipe.current.steps = steps;
        });
	    spyOn(StateService, 'setRecipeAllowDistributedRun').and.returnValue();
        spyOn(StateService, 'setRecipePreviewSteps').and.returnValue();
        spyOn(StateService, 'restoreRecipeBeforePreview').and.returnValue();
    }));

    describe('refresh', () => {
        it('should set recipe steps with row infos when a preparation is loaded', inject(($rootScope, StateService,  RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const initialStep = args[0];
            const steps = args[1];

            expect(initialStep).toEqual({
                transformation: { stepId: 'f6e172c33bdacbc69bca9d32b2bd78174712a171' },
            });
            expect(steps.length).toBe(7);
            expect(steps[6].row.id).toBe('125');
        }));

        it('should set recipe steps with no params when a preparation is loaded', inject(($rootScope, StateService, RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
	        expect(StateService.setRecipeAllowDistributedRun).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const steps = args[1];

            expect(steps.length).toBe(7);
            expect(steps[0].column.name).toBe('country');
            expect(steps[0].transformation.stepId).toBe('329ccf0cce42db4dc0ffa9f389c05ff7d75c1748');
            expect(steps[0].transformation.name).toBe('uppercase');
            expect(steps[0].transformation.parameters).toEqual([]);

            expect(steps[2].column.name).toBe('campain');
            expect(steps[2].transformation.stepId).toBe('0c58ee3034114eb620b8e598e02c74172a43e96a');
            expect(steps[2].transformation.name).toBe('negate');
            expect(steps[2].transformation.parameters).toEqual([]);
        }));

        it('should get recipe from preparation and init recipe simple params', inject(($rootScope, StateService, RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const steps = args[1];
            expect(steps.length).toBe(7);

            expect(steps[1].column.name).toBe('gender');
            expect(steps[1].transformation.stepId).toBe('ec87e2acda2b181fc7eb7c22d91e128c6d0434fc');
            expect(steps[1].transformation.name).toBe('fillemptywithdefault');
            expect(steps[1].transformation.parameters).toEqual([
                {
                    name: 'default_value',
                    type: 'string',
                    description: 'parameter.default_value.desc',
                    label: 'parameter.default_value.label',
                    default: '',
                    initialValue: 'M',
                    value: 'M',
                    inputType: 'text',
                },]);

            expect(steps[3].column.name).toBe('first_item');
            expect(steps[3].transformation.stepId).toBe('1e1f41dd6d4554705abebd8d1896022acdbad217');
            expect(steps[3].transformation.name).toBe('cut');
            expect(steps[3].transformation.parameters).toEqual([
                {
                    name: 'pattern',
                    type: 'string',
                    description: 'parameter.pattern.desc',
                    label: 'parameter.pattern.label',
                    default: '',
                    initialValue: '.',
                    value: '.',
                    inputType: 'text',
                },]);
        }));

        it('should get recipe from preparation and init recipe choices', inject(($rootScope, StateService, RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const steps = args[1];
            expect(steps.length).toBe(7);

            expect(steps[5].column.name).toBe('campain');
            expect(steps[5].transformation.stepId).toBe('2aba0e60054728f046d35315830bce9abc3c5249');
            expect(steps[5].transformation.name).toBe('fillemptywithdefaultboolean');
            expect(steps[5].transformation.parameters).toEqual([
                    {
                        name: 'default_value',
                        type: 'select',
                        description: 'parameter.default_value.desc',
                        label: 'parameter.default_value.label',
                        configuration: {
                            values: [
                                {
                                    name: 'True',
                                    value: 'True',
                                },
                                {
                                    name: 'False',
                                    value: 'False',
                                },
                            ],
                        },
                        default: 'True',
                        value: 'True',
                        initialValue: 'True',
                        inputType: 'text',
                    },
                ]
            );
        }));

        it('should get recipe from preparation and init dynamic params', inject(($rootScope, StateService, FilterService, RecipeService, ParametersService, TransformationService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const steps = args[1];
            expect(steps.length).toBe(7);

            expect(steps[4].column.name).toBe('uglystate');
            expect(steps[4].transformation.stepId).toBe('add60ff0f6de4c703fa75725ada38fb37af065e6');
            expect(steps[4].transformation.name).toBe('textclustering');
            expect(steps[4].transformation.parameters).toEqual([]);
            expect(steps[4].transformation.cluster).toEqual(expectedInitializedCluster);

            expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(steps[4].transformation, {
                columnId: '1',
                preparationId: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4',
                stepId: '1e1f41dd6d4554705abebd8d1896022acdbad217',
            });
            expect(ParametersService.initParamsValues).toHaveBeenCalledWith(steps[4].transformation, steps[4].actionParameters.parameters);
        }));

        it('should init step filters from backend tree', inject(($rootScope, StateService, FilterAdapterService, RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const args = StateService.setRecipeSteps.calls.argsFor(0);
            const steps = args[1];
            expect(FilterAdapterService.fromTree).toHaveBeenCalledWith(
                steps[0].actionParameters.parameters.filter,
                stateMock.playground.data.metadata.columns
            );
            expect(steps[0].filters).toBe(filtersFromTree);
        }));

        it('should reuse dynamic params from previous recipe if ids are the same, on refresh', inject(($rootScope, StateService, RecipeService, TransformationService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();
            expect(StateService.setRecipeSteps.calls.count()).toBe(1);
            const oldSteps = StateService.setRecipeSteps.calls.argsFor(0)[1];
            expect(TransformationService.initDynamicParameters.calls.count()).toBe(1);

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps.calls.count()).toBe(2);
            const steps = StateService.setRecipeSteps.calls.argsFor(1)[1];
            expect(steps[4].transformation.parameters).toBe(oldSteps[4].transformation.parameters);
            expect(steps[4].transformation.items).toBe(oldSteps[4].transformation.items);
            expect(steps[4].transformation.cluster).toBe(oldSteps[4].transformation.cluster);

            expect(TransformationService.initDynamicParameters.calls.count()).toBe(1);
        }));

        it('should save steps actions parameters', inject(($rootScope, StateService, RecipeService) => {
            //given
            stateMock.playground.preparation = { id: '627766216e4b3c99ee5c8621f32ac42f4f87f1b4' };

            //when
            RecipeService.refresh(preparationDetails());
            $rootScope.$digest();

            //then
            expect(StateService.setRecipeSteps).toHaveBeenCalled();
            const steps = StateService.setRecipeSteps.calls.argsFor(0)[1];
            expect(steps[0].actionParameters).toEqual({
                action: 'uppercase',
                parameters: { column_name: 'country', filter: { valid: { field: '0000' } } },
            });
            expect(steps[1].actionParameters).toEqual({
                action: 'fillemptywithdefault',
                parameters: { default_value: 'M', column_name: 'gender' },
            });
            expect(steps[2].actionParameters).toEqual({ action: 'negate', parameters: { column_name: 'campain' } });
            expect(steps[3].actionParameters).toEqual({
                action: 'cut',
                parameters: { pattern: '.', column_name: 'first_item' },
            });
            expect(steps[4].actionParameters).toEqual({
                action: 'textclustering',
                parameters: {
                    Texa: 'Texas',
                    Tixass: 'Texas',
                    'Tex@s': 'Texas',
                    Massachusetts: 'Massachussets',
                    Masachusetts: 'Massachussets',
                    Massachussetts: 'Massachussets',
                    Massachusets: 'Massachussets',
                    Masachussets: 'Massachussets',
                    column_id: '1',
                    column_name: 'uglystate',
                },
            });
            expect(steps[5].actionParameters).toEqual({
                action: 'fillemptywithdefaultboolean',
                parameters: { default_value: 'True', column_name: 'campain' },
            });
        }));
    });

    describe('early preview', () => {
        var originalRecipe;
        var column;
        var transformation;
        var params;

        beforeEach(() => {
            //init recipe
            const steps = [
                { transformation: { id: '1' } },
                { transformation: { id: '2' }, inactive: true },
                { transformation: { id: '3' }, inactive: true },
            ];
            stateMock.playground.recipe.current = {
                steps: steps,
                lastActiveStep: steps[0],
            };
            originalRecipe = steps;

            //params
            transformation = {
                name: 'replace_on_value',
                label: 'Replace value that match...',
                description: 'Replace cells that match the value',
                parameters: [
                    { name: 'value', type: 'string' },
                    { name: 'replace', type: 'string' },
                    { name: 'dummy param', type: 'select' },
                ],
                dynamic: false,
            };
            params = [{
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                value: 'James',
                replace: 'Jimmy',
            }, {
                scope: 'column',
                column_id: '0002',
                column_name: 'lastname',
                value: 'James',
                replace: 'Jimmy',
            }];

            stateMock.playground.filter = {
                applyTransformationOnFilters: true,
                gridFilters: [88],
            };
        });

        it('should create a new recipe with preview step appended', inject((StateService, RecipeService) => {
            //when
            RecipeService.earlyPreview(transformation, params);

            //then
            expect(StateService.setRecipePreviewSteps).toHaveBeenCalled();
            const steps = StateService.setRecipePreviewSteps.calls.argsFor(0)[0];
            expect(steps).not.toBe(originalRecipe);
            expect(steps.length).toBe(5);
            expect(steps[0]).toBe(originalRecipe[0]);
            expect(steps[1]).toBe(originalRecipe[1]);
            expect(steps[2]).toBe(originalRecipe[2]);
            expect(steps[3]).toEqual({
                column: {
                    id: params[0].column_id,
                    name: params[0].column_name,
                },
                row: { id: undefined },
                transformation: {
                    stepId: 'early_preview_0',
                    name: transformation.name,
                    label: transformation.label,
                    description: transformation.description,
                    parameters: [
                        { name: 'value', type: 'string', value: 'James', initialValue: 'James', inputType: 'text' },
                        { name: 'replace', type: 'string', value: 'Jimmy', initialValue: 'Jimmy', inputType: 'text' },
                        {
                            name: 'dummy param',
                            type: 'select',
                            value: undefined,
                            initialValue: undefined,
                            inputType: 'text',
                        },
                    ],
                    dynamic: transformation.dynamic,
                },
                actionParameters: {
                    action: transformation.name,
                    parameters: params[0],
                },
                preview: true,
                filters: [88],
            });
            expect(steps[3].transformation.parameters).not.toBe(transformation.parameters);
            expect(steps[4]).toEqual({
                column: {
                    id: params[1].column_id,
                    name: params[1].column_name,
                },
                row: { id: undefined },
                transformation: {
                    stepId: 'early_preview_1',
                    name: transformation.name,
                    label: transformation.label,
                    description: transformation.description,
                    parameters: [
                        { name: 'value', type: 'string', value: 'James', initialValue: 'James', inputType: 'text' },
                        { name: 'replace', type: 'string', value: 'Jimmy', initialValue: 'Jimmy', inputType: 'text' },
                        {
                            name: 'dummy param',
                            type: 'select',
                            value: undefined,
                            initialValue: undefined,
                            inputType: 'text',
                        },
                    ],
                    dynamic: transformation.dynamic,
                },
                actionParameters: {
                    action: transformation.name,
                    parameters: params[1],
                },
                preview: true,
                filters: [88],
            });
        }));

        it('should cancel preview and set back previous state', inject((StateService, RecipeService) => {
            // given
            expect(StateService.restoreRecipeBeforePreview).not.toHaveBeenCalled();

            // when
            RecipeService.cancelEarlyPreview();

            // then
            expect(StateService.restoreRecipeBeforePreview).toHaveBeenCalled();
        }));
    });
});
