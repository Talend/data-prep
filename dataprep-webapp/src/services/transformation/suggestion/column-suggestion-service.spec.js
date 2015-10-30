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
                {name: 'rename', category: 'column_metadata', label: 'z', description: 'test'},
                {name: 'cluster', category: 'quickfix', label: 'f', description: 'test'},
                {name: 'split', category: 'column_metadata', label: 'c', description: 'test'},
                {name: 'tolowercase', category: 'case', label: 'v', description: 'test'},
                {name: 'touppercase', category: 'case', label: 'u', description: 'test'},
                {name: 'removeempty', category: 'clear', label: 'a', description: 'test'},
                {name: 'totitlecase', category: 'case', label: 't', description: 'test'},
                {name: 'removetrailingspaces', category: 'quickfix', label: 'm', description: 'test'},
                {name: 'split', category: 'split', label: 'l', dynamic: true, description: 'test'}
            ]
        ));

        spyOn(TransformationCacheService, 'getSuggestions').and.returnValue($q.when(
            [
                {name: 'tolowercase', category: 'case', label: 'v', description: 'test'},
                {name: 'touppercase', category: 'case', label: 'u', description: 'test'}
            ]
        ));
    }));

    it('should reset the transformations', inject(function (ColumnSuggestionService) {
        //given
        ColumnSuggestionService.allTransformations = [{}];
        ColumnSuggestionService.allSuggestions = [{}];
        ColumnSuggestionService.searchActionString = 'azeaz';
        ColumnSuggestionService.filteredTransformations = [{}];

        //when
        ColumnSuggestionService.reset();

        //then
        expect(ColumnSuggestionService.allTransformations).toEqual([]);
        expect(ColumnSuggestionService.allSuggestions).toEqual([]);
        expect(ColumnSuggestionService.searchActionString).toEqual('');
        expect(ColumnSuggestionService.filteredTransformations).toBeFalsy();
    }));

    it('should group the transformations by category', inject(function ($rootScope, ColumnSuggestionService, TransformationCacheService) {
        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        expect(TransformationCacheService.getTransformations).toHaveBeenCalledWith(firstSelectedColumn);
        expect(TransformationCacheService.getSuggestions).toHaveBeenCalledWith(firstSelectedColumn);

        var suggestedTransformations = ColumnSuggestionService.filteredTransformations;
        expect(suggestedTransformations[0].category).toBe('suggestion');
        expect(suggestedTransformations[0].categoryHtml).toBe('SUGGESTION');
        expect(suggestedTransformations[0].transformations.length).toBe(2);
        expect(suggestedTransformations[1].category).toBe('case');
        expect(suggestedTransformations[1].categoryHtml).toBe('CASE');
        expect(suggestedTransformations[1].transformations.length).toBe(3);
        expect(suggestedTransformations[2].category).toBe('clear');
        expect(suggestedTransformations[2].categoryHtml).toBe('CLEAR');
        expect(suggestedTransformations[2].transformations.length).toBe(1);
        expect(suggestedTransformations[3].category).toBe('quickfix');
        expect(suggestedTransformations[3].categoryHtml).toBe('QUICKFIX');
        expect(suggestedTransformations[3].transformations.length).toBe(2);
        expect(suggestedTransformations[4].category).toBe('split');
        expect(suggestedTransformations[4].categoryHtml).toBe('SPLIT');
        expect(suggestedTransformations[4].transformations.length).toBe(1);
    }));

    it('should insert html label (with "..." with parameters) in each transformation/suggestions', inject(function ($rootScope, ColumnSuggestionService) {
        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        var suggestedTransformations = ColumnSuggestionService.filteredTransformations;

        expect(suggestedTransformations[0].transformations[0].labelHtml).toBe('v');
        expect(suggestedTransformations[0].transformations[1].labelHtml).toBe('u');
        expect(suggestedTransformations[1].transformations[0].labelHtml).toBe('t');
        expect(suggestedTransformations[1].transformations[1].labelHtml).toBe('u');
        expect(suggestedTransformations[1].transformations[2].labelHtml).toBe('v');
        expect(suggestedTransformations[2].transformations[0].labelHtml).toBe('a');
        expect(suggestedTransformations[3].transformations[0].labelHtml).toBe('f');
        expect(suggestedTransformations[3].transformations[1].labelHtml).toBe('m');
        expect(suggestedTransformations[4].transformations[0].labelHtml).toBe('l...');
    }));

    it('should filter "column metadata" category', inject(function ($rootScope, ColumnSuggestionService) {
        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //then
        ColumnSuggestionService.filteredTransformations.forEach(function(categoryTransformations) {
            expect(categoryTransformations.category).not.toBe('column_metadata');
        });
    }));

    it('should not filter transformations when searchActionString is empty', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.searchActionString = '';

        //when
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();
        ColumnSuggestionService.filterTransformations();
        $rootScope.$digest();

        //then
        var filteredTransformations = ColumnSuggestionService.filteredTransformations;

        expect(filteredTransformations[0].transformations[0].labelHtml).toBe('v');
        expect(filteredTransformations[0].transformations[1].labelHtml).toBe('u');
        expect(filteredTransformations[1].transformations[0].labelHtml).toBe('t');
        expect(filteredTransformations[1].transformations[1].labelHtml).toBe('u');
        expect(filteredTransformations[1].transformations[2].labelHtml).toBe('v');
        expect(filteredTransformations[2].transformations[0].labelHtml).toBe('a');
        expect(filteredTransformations[3].transformations[0].labelHtml).toBe('f');
        expect(filteredTransformations[3].transformations[1].labelHtml).toBe('m');
        expect(filteredTransformations[4].transformations[0].labelHtml).toBe('l...');
    }));

    it('should filter transformations when searchActionString is not empty', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //when
        ColumnSuggestionService.searchActionString = 'l';

        ColumnSuggestionService.filterTransformations();
        $rootScope.$digest();

        //then
        var filteredTransformations = ColumnSuggestionService.filteredTransformations;
        expect(filteredTransformations[0].category).toBe('clear');
        expect(filteredTransformations[0].transformations.length).toBe(1);
        expect(filteredTransformations[1].category).toBe('split');
        expect(filteredTransformations[1].transformations.length).toBe(1);
    }));

    it('should highlight categories and transformations labels when searchActionString is not empty', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //when
        ColumnSuggestionService.searchActionString = 'l';

        ColumnSuggestionService.filterTransformations();
        $rootScope.$digest();

        //then
        var filteredTransformations = ColumnSuggestionService.filteredTransformations;
        expect(filteredTransformations.length).toBe(2);
        expect(filteredTransformations[0].categoryHtml).toBe('C<span class="highlighted">L</span>EAR');
        expect(filteredTransformations[0].transformations[0].labelHtml).toBe('a');
        expect(filteredTransformations[1].categoryHtml).toBe('SP<span class="highlighted">L</span>IT');
        expect(filteredTransformations[1].transformations[0].labelHtml).toBe('<span class="highlighted">l</span>...');
    }));

    it('should filter transformations with case insensitive', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //when
        ColumnSuggestionService.searchActionString = 'L';

        ColumnSuggestionService.filterTransformations();
        $rootScope.$digest();

        //then
        var filteredTransformations = ColumnSuggestionService.filteredTransformations;
        expect(filteredTransformations.length).toBe(2);
        expect(filteredTransformations[0].categoryHtml).toBe('C<span class="highlighted">L</span>EAR');
        expect(filteredTransformations[0].transformations[0].labelHtml).toBe('a');
        expect(filteredTransformations[1].categoryHtml).toBe('SP<span class="highlighted">L</span>IT');
        expect(filteredTransformations[1].transformations[0].labelHtml).toBe('<span class="highlighted">l</span>...');
    }));

    it('should filter transformations by escaping regex', inject(function ($rootScope, ColumnSuggestionService) {
        //given
        ColumnSuggestionService.initTransformations(firstSelectedColumn);
        $rootScope.$digest();

        //when
        ColumnSuggestionService.searchActionString = '...';

        ColumnSuggestionService.filterTransformations();
        $rootScope.$digest();

        //then
        var filteredTransformations = ColumnSuggestionService.filteredTransformations;
        expect(filteredTransformations.length).toBe(1);
        expect(filteredTransformations[0].categoryHtml).toBe('SPLIT');
        expect(filteredTransformations[0].transformations[0].labelHtml).toBe('l<span class="highlighted">...</span>');
    }));
});