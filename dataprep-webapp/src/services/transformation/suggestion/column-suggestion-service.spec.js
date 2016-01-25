describe('Column suggestion service', function () {
    'use strict';

    var firstSelectedColumn = {id: '0001', name: 'col1'};
    var stateMock, columnTransformations, columnSuggestions, allCategories;

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'ACTION_SUGGESTION': 'Suggestion'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(module('data-prep.services.transformation', function ($provide) {
        stateMock = {
            playground: {
                suggestions: {
                    column: {
                        allSuggestions: [],     // all selected column suggestions
                        allTransformations: [], // all selected column transformations
                        filteredTransformations: [], // categories with their transformations to display, result of filter
                        searchActionString: '',
                        allCategories: null
                    },
                    transformationsForEmptyCells: [], // all column transformations applied to empty cells
                    transformationsForInvalidCells: []// all column transformations applied to invalid cells
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, TransformationCacheService, StateService) {

        columnTransformations = {
            allTransformations: [
                {name: 'rename', category: 'column_metadata', label: 'z', labelHtml: 'z', description: 'test'},
                {name: 'cluster', category: 'quickfix', label: 'f', labelHtml: 'f', description: 'test'},
                {name: 'split', category: 'column_metadata', label: 'c', labelHtml: 'c', description: 'test'},
                {name: 'tolowercase', category: 'case', label: 'v', labelHtml: 'v', description: 'test'},
                {
                    name: 'touppercase',
                    category: 'case',
                    label: 'u',
                    labelHtml: 'u',
                    description: 'test',
                    actionScope: ['unknown']
                },
                {
                    name: 'removeempty',
                    category: 'clear',
                    label: 'a',
                    labelHtml: 'a',
                    description: 'test',
                    actionScope: ['empty', 'invalid']
                },
                {
                    name: 'totitlecase',
                    category: 'case',
                    label: 't',
                    labelHtml: 't',
                    description: 'test',
                    actionScope: ['invalid']
                },
                {
                    name: 'removetrailingspaces',
                    category: 'quickfix',
                    label: 'm',
                    labelHtml: 'm',
                    description: 'test',
                    actionScope: ['empty', 'unknown']
                },
                {name: 'split', category: 'split', label: 'l', labelHtml: 'l...', dynamic: true, description: 'test'}
            ],
            allCategories: [
                {
                    category: 'case',
                    transformations: [
                        {
                            name: 'totitlecase',
                            category: 'case',
                            label: 't',
                            labelHtml: 't',
                            description: 'test',
                            actionScope: ['invalid']
                        },
                        {
                            name: 'touppercase',
                            category: 'case',
                            label: 'u',
                            labelHtml: 'u',
                            description: 'test',
                            actionScope: ['unknown']
                        },
                        {name: 'tolowercase', category: 'case', label: 'v', labelHtml: 'v', description: 'test'}
                    ]
                },
                {
                    category: 'clear',
                    transformations: [{
                        name: 'removeempty',
                        category: 'clear',
                        label: 'a',
                        labelHtml: 'a',
                        description: 'test',
                        actionScope: ['empty', 'invalid']
                    }]
                },
                {
                    category: 'quickfix',
                    transformations: [
                        {name: 'cluster', category: 'quickfix', label: 'f', labelHtml: 'f', description: 'test'},
                        {
                            name: 'removetrailingspaces',
                            category: 'quickfix',
                            label: 'm',
                            labelHtml: 'm',
                            description: 'test',
                            actionScope: ['empty', 'unknown']
                        }
                    ]
                },
                {
                    category: 'split',
                    transformations: [{
                        name: 'split',
                        category: 'split',
                        label: 'l',
                        labelHtml: 'l...',
                        dynamic: true,
                        description: 'test'
                    }]
                }
            ]
        };
        columnSuggestions = [
            {name: 'touppercase', category: 'case', label: 'u', labelHtml: 'u', description: 'test'},
            {name: 'tolowercase', category: 'case', label: 'v', labelHtml: 'v', description: 'test'}
        ];
        allCategories = [
            {
                category: 'suggestion',
                categoryHtml: 'SUGGESTION',
                transformations: [
                    {
                        name: 'touppercase',
                        category: 'case',
                        label: 'u',
                        labelHtml: 'u',
                        description: 'test'
                    },
                    {
                        name: 'tolowercase',
                        category: 'case',
                        label: 'v',
                        labelHtml: 'v',
                        description: 'test'
                    }
                ]
            },
            {
                category: 'case',
                transformations: [
                    {
                        name: 'totitlecase',
                        category: 'case',
                        label: 't',
                        labelHtml: 't',
                        description: 'test',
                        actionScope: [
                            'invalid'
                        ]
                    },
                    {
                        name: 'touppercase',
                        category: 'case',
                        label: 'u',
                        labelHtml: 'u',
                        description: 'test',
                        actionScope: [
                            'unknown'
                        ]
                    },
                    {
                        name: 'tolowercase',
                        category: 'case',
                        label: 'v',
                        labelHtml: 'v',
                        description: 'test'
                    }
                ]
            },
            {
                category: 'clear',
                transformations: [
                    {
                        name: 'removeempty',
                        category: 'clear',
                        label: 'a',
                        labelHtml: 'a',
                        description: 'test',
                        actionScope: [
                            'empty',
                            'invalid'
                        ]
                    }
                ]
            },
            {
                category: 'quickfix',
                transformations: [
                    {
                        name: 'cluster',
                        category: 'quickfix',
                        label: 'f',
                        labelHtml: 'f',
                        description: 'test'
                    },
                    {
                        name: 'removetrailingspaces',
                        category: 'quickfix',
                        label: 'm',
                        labelHtml: 'm',
                        description: 'test',
                        actionScope: [
                            'empty',
                            'unknown'
                        ]
                    }
                ]
            },
            {
                category: 'split',
                transformations: [
                    {
                        name: 'split',
                        category: 'split',
                        label: 'l',
                        labelHtml: 'l...',
                        dynamic: true,
                        description: 'test'
                    }
                ]
            }
        ];

        spyOn(TransformationCacheService, 'getColumnTransformations').and.returnValue($q.when(columnTransformations));
        spyOn(TransformationCacheService, 'getColumnSuggestions').and.returnValue($q.when(columnSuggestions));
        spyOn(StateService, 'setSuggestionsLoading').and.returnValue();
        spyOn(StateService, 'setTransformationsForEmptyCells').and.returnValue();
        spyOn(StateService, 'setTransformationsForInvalidCells').and.returnValue();
    }));

    describe('loading suggestions', function () {
        it('should hide spinner', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            expect(StateService.setSuggestionsLoading).not.toHaveBeenCalled();

            //when
            ColumnSuggestionService.initTransformations(firstSelectedColumn);
            $rootScope.$digest();

            //then
            expect(StateService.setSuggestionsLoading.calls.count()).toBe(2);
            expect(StateService.setSuggestionsLoading).toHaveBeenCalledWith(false);

        }));
    });

    describe('search filter', function () {
        it('should not filter transformations when searchActionString is empty', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            spyOn(StateService, 'setColumnTransformations');

            //when
            ColumnSuggestionService.initTransformations(firstSelectedColumn);
            $rootScope.$digest();

            ColumnSuggestionService.filterTransformations();
            $rootScope.$digest();

            //then
            var filteredTransformations = StateService.setColumnTransformations.calls.argsFor(1)[0].filteredTransformations;

            expect(filteredTransformations[0].transformations[0].labelHtml).toBe('u');
            expect(filteredTransformations[0].transformations[1].labelHtml).toBe('v');
            expect(filteredTransformations[1].transformations[0].labelHtml).toBe('t');
            expect(filteredTransformations[1].transformations[1].labelHtml).toBe('u');
            expect(filteredTransformations[1].transformations[2].labelHtml).toBe('v');
            expect(filteredTransformations[2].transformations[0].labelHtml).toBe('a');
            expect(filteredTransformations[3].transformations[0].labelHtml).toBe('f');
            expect(filteredTransformations[3].transformations[1].labelHtml).toBe('m');
            expect(filteredTransformations[4].transformations[0].labelHtml).toBe('l...');
        }));

        it('should filter transformations', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            stateMock.playground.suggestions.column.allCategories = allCategories;
            stateMock.playground.suggestions.column.allTransformations = columnTransformations.allTransformations;
            stateMock.playground.suggestions.column.allSuggestions = columnSuggestions;

            spyOn(StateService, 'updateFilteredTransformations');

            //when
            stateMock.playground.suggestions.column.searchActionString = 'l';

            ColumnSuggestionService.filterTransformations();
            $rootScope.$digest();

            //then
            var filteredTransformations = StateService.updateFilteredTransformations.calls.argsFor(0)[0];

            expect(filteredTransformations[0].category).toBe('clear');
            expect(filteredTransformations[0].transformations.length).toBe(1);
            expect(filteredTransformations[1].category).toBe('split');
            expect(filteredTransformations[1].transformations.length).toBe(1);
        }));

        it('should highlight categories and transformations labels', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            stateMock.playground.suggestions.column.allCategories = allCategories;
            stateMock.playground.suggestions.column.allTransformations = columnTransformations.allTransformations;
            stateMock.playground.suggestions.column.allSuggestions = columnSuggestions;

            spyOn(StateService, 'updateFilteredTransformations');

            //when
            stateMock.playground.suggestions.column.searchActionString = 'l';

            ColumnSuggestionService.filterTransformations();
            $rootScope.$digest();

            //then
            var filteredTransformations = StateService.updateFilteredTransformations.calls.argsFor(0)[0];

            expect(filteredTransformations.length).toBe(2);
            expect(filteredTransformations[0].categoryHtml).toBe('C<span class="highlighted">L</span>EAR');
            expect(filteredTransformations[0].transformations[0].labelHtml).toBe('a');
            expect(filteredTransformations[1].categoryHtml).toBe('SP<span class="highlighted">L</span>IT');
            expect(filteredTransformations[1].transformations[0].labelHtml).toBe('<span class="highlighted">l</span>...');
        }));

        it('should filter transformations with case insensitive', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            stateMock.playground.suggestions.column.allCategories = allCategories;
            stateMock.playground.suggestions.column.allTransformations = columnTransformations.allTransformations;
            stateMock.playground.suggestions.column.allSuggestions = columnSuggestions;

            spyOn(StateService, 'updateFilteredTransformations');

            //when
            stateMock.playground.suggestions.column.searchActionString = 'L';

            ColumnSuggestionService.filterTransformations();
            $rootScope.$digest();

            //then
            var filteredTransformations = StateService.updateFilteredTransformations.calls.argsFor(0)[0];

            expect(filteredTransformations.length).toBe(2);
            expect(filteredTransformations[0].categoryHtml).toBe('C<span class="highlighted">L</span>EAR');
            expect(filteredTransformations[0].transformations[0].labelHtml).toBe('a');
            expect(filteredTransformations[1].categoryHtml).toBe('SP<span class="highlighted">L</span>IT');
            expect(filteredTransformations[1].transformations[0].labelHtml).toBe('<span class="highlighted">l</span>...');
        }));

        it('should filter transformations by escaping regex', inject(function ($rootScope, ColumnSuggestionService, StateService) {
            //given
            stateMock.playground.suggestions.column.allCategories = allCategories;
            stateMock.playground.suggestions.column.allTransformations = columnTransformations.allTransformations;
            stateMock.playground.suggestions.column.allSuggestions = columnSuggestions;

            spyOn(StateService, 'updateFilteredTransformations');

            //when
            stateMock.playground.suggestions.column.searchActionString = '...';

            ColumnSuggestionService.filterTransformations();
            $rootScope.$digest();

            //then
            var filteredTransformations = StateService.updateFilteredTransformations.calls.argsFor(0)[0];

            expect(filteredTransformations.length).toBe(1);
            expect(filteredTransformations[0].categoryHtml).toBe('SPLIT');
            expect(filteredTransformations[0].transformations[0].labelHtml).toBe('l<span class="highlighted">...</span>');
        }));

    });

    it('should initialize column transformations with new transfos for empty and invalid cells', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService, StateService) {
        //when
        spyOn(StateService, 'setColumnTransformations').and.returnValue();
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        expect(TransformationCacheService.getColumnTransformations).toHaveBeenCalledWith(firstSelectedColumn);
        expect(StateService.setTransformationsForEmptyCells).toHaveBeenCalledWith([
            {
                name: 'removeempty',
                category: 'clear',
                label: 'a',
                labelHtml: 'a',
                description: 'test',
                actionScope: ['empty', 'invalid']
            },
            {
                name: 'removetrailingspaces',
                category: 'quickfix',
                label: 'm',
                labelHtml: 'm',
                description: 'test',
                actionScope: ['empty', 'unknown']
            }
        ]);
        expect(StateService.setTransformationsForInvalidCells).toHaveBeenCalledWith([
            {
                name: 'removeempty',
                category: 'clear',
                label: 'a',
                labelHtml: 'a',
                description: 'test',
                actionScope: ['empty', 'invalid']
            },
            {
                name: 'totitlecase',
                category: 'case',
                label: 't',
                labelHtml: 't',
                description: 'test',
                actionScope: ['invalid']
            }
        ]);
        expect(StateService.setColumnTransformations).toHaveBeenCalledWith({
            allSuggestions: columnSuggestions,
            allTransformations: columnTransformations.allTransformations,
            filteredTransformations: allCategories,
            allCategories: allCategories,
            searchActionString: ''
        });

    }));

    it('should NOT change transfos for empty and invalid cells when they are already initialized', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService, StateService) {
        //given
        stateMock.playground.suggestions.transformationsForEmptyCells = [{name: 'deleteEmpty'}];
        stateMock.playground.suggestions.transformationsForInvalidCells = [{name: 'deleteInvalid'}];

        //when
        spyOn(StateService, 'setColumnTransformations').and.returnValue();
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        expect(TransformationCacheService.getColumnTransformations).toHaveBeenCalledWith(firstSelectedColumn);
        expect(StateService.setTransformationsForEmptyCells).not.toHaveBeenCalled();
        expect(StateService.setTransformationsForInvalidCells).not.toHaveBeenCalled();

    }));
});