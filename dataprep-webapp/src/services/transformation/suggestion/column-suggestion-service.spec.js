describe('Column suggestion service', function () {
    'use strict';

    var firstSelectedColumn = {id: '0001', name: 'col1'};

    beforeEach(module('data-prep.services.transformation'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'ACTION_SUGGESTION': 'Suggestion'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($q, TransformationCacheService) {
        spyOn(TransformationCacheService, 'getTransformations').and.returnValue($q.when(
            [
                {name: 'rename', category: 'column_metadata', label: 'z'},
                {name: 'cluster', category: 'quickfix', label: 'f'},
                {name: 'split', category: 'column_metadata', label: 'c'},
                {name: 'tolowercase', category: 'case', label: 'v'},
                {name: 'touppercase', category: 'case', label: 'u'},
                {name: 'removeempty', category: 'clear', label: 'a'},
                {name: 'totitlecase', category: 'case', label: 't'},
                {name: 'removetrailingspaces', category: 'quickfix', label: 'm'},
                {name: 'split', category: 'split', label: 'l', dynamic: true}
            ]
        ));

        spyOn(TransformationCacheService, 'getSuggestions').and.returnValue($q.when(
            [
                {name: 'tolowercase', category: 'case', label: 'v'},
                {name: 'touppercase', category: 'case', label: 'u'}
            ]
        ));
    }));

    it('should reset the suggested transformations', inject(function (ColumnSuggestionService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.reset();

        //then
        expect(ColumnSuggestionService.transformations).toBeFalsy();
    }));

    it('should group the transformations by category', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        expect(ColumnSuggestionService.transformations).toBeFalsy();
        $rootScope.$digest();

        //then
        expect(TransformationCacheService.getTransformations).toHaveBeenCalledWith(firstSelectedColumn);
        expect(TransformationCacheService.getSuggestions).toHaveBeenCalledWith(firstSelectedColumn);

        var suggestedTransformations = ColumnSuggestionService.transformations;
        expect(suggestedTransformations.SUGGESTION.length).toBe(2);
        expect(suggestedTransformations.CASE.length).toBe(3);
        expect(suggestedTransformations.CLEAR.length).toBe(1);
        expect(suggestedTransformations.QUICKFIX.length).toBe(2);
        expect(suggestedTransformations.SPLIT.length).toBe(1);
    }));

    it('should insert html label (with "..." with parameters) in each transformation/suggestions', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        var suggestedTransformations = ColumnSuggestionService.transformations;

        expect(suggestedTransformations.SUGGESTION.length).toBe(2);
        expect(suggestedTransformations.CASE[0].labelHtml).toBe('t');
        expect(suggestedTransformations.CASE[1].labelHtml).toBe('u');
        expect(suggestedTransformations.CASE[2].labelHtml).toBe('v');
        expect(suggestedTransformations.CLEAR[0].labelHtml).toBe('a');
        expect(suggestedTransformations.QUICKFIX[0].labelHtml).toBe('f');
        expect(suggestedTransformations.QUICKFIX[1].labelHtml).toBe('m');
        expect(suggestedTransformations.SPLIT[0].labelHtml).toBe('l...');
    }));

    it('should filter "column metadata" category', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        var suggestedTransformations = ColumnSuggestionService.transformations;
        var columnCategoryTransformation = _.find(suggestedTransformations, {category: 'column_metadata'});
        expect(columnCategoryTransformation).toBeFalsy();
    }));

    it('should update transformations list after searching', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.transformations = {
            '<span class="highlighted">SUGGESTION</span>': [{name: 'cluster', categoryHtml: 'SUGGESTION', category: 'quickfix',label: 'f', labelHtml: 'f'}, {name: 'removetrailingspaces', categoryHtml: 'SUGGESTION',category: 'quickfix', label: 'm', labelHtml: 'm'}],
            QUICKFIX: [{name: 'cluster', categoryHtml: 'QUICKFIX', category: 'quickfix', label: 'f', labelHtml: 'f'}, {name: 'removetrailingspaces', categoryHtml: 'QUICKFIX', category: 'quickfix',label: 'm', labelHtml: 'm'}]
        };
        //when
        ColumnSuggestionService.updateTransformations();
        $rootScope.$digest();

        //then
        expect(ColumnSuggestionService.transformations.SUGGESTION.length).toBe(2);

        expect(ColumnSuggestionService.transformations.QUICKFIX.length).toBe(2);
        expect(ColumnSuggestionService.transformations.QUICKFIX[0].label).toBe('f');
        expect(ColumnSuggestionService.transformations.QUICKFIX[1].label).toBe('m');
    }));
});