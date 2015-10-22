describe('Column suggestion service', function () {
    'use strict';

    var firstSelectedColumn = {id: '0001', name: 'col1'};

    beforeEach(module('data-prep.services.transformation'));
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
                {name: 'removetrailingspaces', category: 'quickfix', label: 'm'}
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

    it('should filter "column" category, sort and group the transformations by category', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn, true);
        expect(ColumnSuggestionService.transformations).toBeFalsy();
        $rootScope.$digest();

        //then : transformations initialized
        expect(TransformationCacheService.getTransformations).toHaveBeenCalledWith(firstSelectedColumn, true);

        //then : column category filtered
        var suggestedTransformations = ColumnSuggestionService.transformations;
        expect(suggestedTransformations).toBeDefined();
        var columnCategoryTransformation = _.find(suggestedTransformations, {category: 'column_metadata'});
        expect(columnCategoryTransformation).toBeFalsy();

        //then : result grouped
        expect(suggestedTransformations.CASE.length).toBe(3);
        expect(suggestedTransformations.CLEAR.length).toBe(1);
        expect(suggestedTransformations.QUICKFIX.length).toBe(2);

    }));


    it('should filter "column" category', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService) {
        //given
        ColumnSuggestionService.transformations = {};

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn, false);
        expect(ColumnSuggestionService.transformations).toBeFalsy();
        $rootScope.$digest();

        //then : transformations initialized
        expect(TransformationCacheService.getTransformations).toHaveBeenCalledWith(firstSelectedColumn, false);

        //then : column category filtered
        var suggestedTransformations = ColumnSuggestionService.transformations;
        expect(suggestedTransformations).toBeDefined();
        var columnCategoryTransformation = _.find(suggestedTransformations, {category: 'column_metadata'});
        expect(columnCategoryTransformation).toBeFalsy();

        //then : result alphabetically sorted
        expect(suggestedTransformations[0].label).toEqual('f');
        expect(suggestedTransformations[0].labelHtml).toEqual('f');
        expect(suggestedTransformations[suggestedTransformations.length - 1].label).toEqual('m');
    }));
});