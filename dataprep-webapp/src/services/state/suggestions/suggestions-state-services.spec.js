describe('Suggestions state service', function () {
    'use strict';

    beforeEach(module('data-prep.services.state'));

    describe('loading flag', function() {
        it('should set loading flag', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.isLoading = false;

            //when
            SuggestionsStateService.setLoading(true);

            //then
            expect(suggestionsState.isLoading).toBe(true);
        }));
    });

    describe('transformations', function() {
        it('should init line transformations', inject(function (suggestionsState) {
            //then
            expect(suggestionsState.line.allTransformations).toEqual([]);
            expect(suggestionsState.line.filteredTransformations).toEqual([]);
            expect(suggestionsState.line.allCategories).toBe(null);
        }));

        it('should set line transformations', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            var transformations = {
                allTransformations: [{name: 'delete'}, {name: 'uppercase'}],
                filteredTransformations: [{name: 'delete'}],
                allCategories: {
                    'clean': [{name: 'delete'}],
                    'case': [{name: 'uppercase'}]
                }
            };
            expect(suggestionsState.line).not.toBe(transformations);

            //when
            SuggestionsStateService.setLineTransformations(transformations);

            //then
            expect(suggestionsState.line).toBe(transformations);
        }));

        it('should init column transformations', inject(function (suggestionsState) {
            //then
            expect(suggestionsState.column).toEqual({
                allSuggestions: [],
                allTransformations: [],
                filteredTransformations: [],
                allCategories: null,
                searchActionString: ''
            });
        }));

        it('should set column transformations', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            var transformations = {
                allTransformations: [{name: 'delete'}, {name: 'uppercase'}],
                filteredTransformations: [{name: 'delete'}],
                allSuggestions: [{name: 'delete'}, {name: 'uppercase'}],
                allCategories: [{}],
                searchActionString: ''
            };
            expect(suggestionsState.column).not.toBe(transformations);

            //when
            SuggestionsStateService.setColumnTransformations(transformations);

            //then
            expect(suggestionsState.column).toBe(transformations);
        }));

        it('should reset column transformations when the new transformations are undefined', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.column = {
                allTransformations: [{name: 'delete'}, {name: 'uppercase'}],
                filteredTransformations: [{name: 'delete'}],
                allSuggestions: [{name: 'delete'}, {name: 'uppercase'}],
                allCategories: [{}],
                searchActionString: ''
            };

            //when
            SuggestionsStateService.setColumnTransformations();

            //then
            expect(suggestionsState.column).toEqual({
                allSuggestions: [],
                allTransformations: [],
                filteredTransformations: [],
                allCategories: null,
                searchActionString: ''
            });
        }));

        it('should update filtered Transformations', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.column.filteredTransformations = [{name: 'delete'}];
            var filteredTransformations = [{name: 'delete'}, {name: 'split'}];

            //when
            SuggestionsStateService.updateFilteredTransformations(filteredTransformations);

            //then
            expect(suggestionsState.column.filteredTransformations).toBe(filteredTransformations);
        }));

        it('should set transformations for empty cells', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.column.transformationsForEmptyCells = [{name: 'delete'}];
            var transformations = [{name: 'delete'}, {name: 'split'}];

            //when
            SuggestionsStateService.setTransformationsForEmptyCells(transformations);

            //then
            expect(suggestionsState.transformationsForEmptyCells).toBe(transformations);
        }));

        it('should set transformations for invalid cells', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.column.transformationsForInvalidCells = [{name: 'delete'}];
            var transformations = [{name: 'delete'}, {name: 'split'}];

            //when
            SuggestionsStateService.setTransformationsForInvalidCells(transformations);

            //then
            expect(suggestionsState.transformationsForInvalidCells).toBe(transformations);
        }));
    });

    describe('reset', function() {
        it('should reset loading flag', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.isLoading = true;

            //when
            SuggestionsStateService.reset();

            //then
            expect(suggestionsState.isLoading).toBe(false);
        }));

        it('should reset line transformations', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.line = {
                allTransformations: [{name: 'delete'}, {name: 'uppercase'}],
                filteredTransformations: [{name: 'delete'}],
                allCategories: {
                    'clean': [{name: 'delete'}],
                    'case': [{name: 'uppercase'}]
                }
            };

            //when
            SuggestionsStateService.reset();

            //then
            expect(suggestionsState.line.allTransformations).toEqual([]);
            expect(suggestionsState.line.filteredTransformations).toEqual([]);
            expect(suggestionsState.line.allCategories).toBe(null);
        }));

        it('should reset column transformations', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.column = {
                allSuggestions: [{}],
                allTransformations: [{}],
                filteredTransformations: [{}],
                allCategories: [{}],
                searchActionString: 'ssdsdsd'
            };

            //when
            SuggestionsStateService.reset();

            //then
            expect(suggestionsState.column).toEqual({
                allSuggestions: [],
                allTransformations: [],
                filteredTransformations: [],
                allCategories: null,
                searchActionString: ''
            });
        }));

        it('should NOT reset transformations for invalid cells', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.transformationsForInvalidCells = [{}];

            //when
            SuggestionsStateService.reset();

            //then
            expect(suggestionsState.transformationsForInvalidCells).toEqual([{}]);
        }));

        it('should NOT reset transformations for empty cells', inject(function (suggestionsState, SuggestionsStateService) {
            //given
            suggestionsState.transformationsForEmptyCells = [{}];

            //when
            SuggestionsStateService.reset();

            //then
            expect(suggestionsState.transformationsForEmptyCells).toEqual([{}]);
        }));
    });
});
