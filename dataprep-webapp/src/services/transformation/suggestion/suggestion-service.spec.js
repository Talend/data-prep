describe('Suggestion Service', function() {
    'use strict';

    beforeEach(module('data-prep.services.transformation'));

    beforeEach(inject(function(ColumnSuggestionService) {
        spyOn(ColumnSuggestionService, 'initTransformations').and.returnValue();
        spyOn(ColumnSuggestionService, 'reset').and.returnValue();
    }));

    it('should set new selected column', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.currentColumn).toBeFalsy();
        var column = {id: '0001'};

        //when
        SuggestionService.setColumn(column);

        //then
        expect(SuggestionService.currentColumn).toBe(column);
    }));

    it('should init column suggestions', inject(function(SuggestionService, ColumnSuggestionService) {
        //given
        expect(ColumnSuggestionService.initTransformations).not.toHaveBeenCalled();
        var column = {id: '0001'};
        SuggestionService.showAllAction = true;

        //when
        SuggestionService.setColumn(column, SuggestionService.showAllAction);

        //then
        expect(ColumnSuggestionService.initTransformations).toHaveBeenCalledWith(column, SuggestionService.showAllAction);
    }));


    it('should reset action search when showAllActionis false', inject(function(SuggestionService, ColumnSuggestionService) {
        //given
        var column = {id: '0001'};
        SuggestionService.showAllAction = false;
        ColumnSuggestionService.transformations =
            [
                {name: 'rename', category: 'column_metadata', label: 'z', labelHtml: '<span class="highlighted">z</span>'},
                {name: 'cluster', category: 'quickfix', label: 'f', labelHtml: 'f'}
            ];

        //when
        SuggestionService.setColumn(column, SuggestionService.showAllAction);

        //then
        expect(SuggestionService.searchActionString).toBeFalsy();
        expect(ColumnSuggestionService.transformations[0].labelHtml).toBe('z');

    }));


    it('should reset action search when showAllActionis true', inject(function(SuggestionService, ColumnSuggestionService) {
        //given
        var column = {id: '0001'};
        SuggestionService.showAllAction = true;
        ColumnSuggestionService.transformations =
        {'QUICKFI<span class="highlighted">X</span>': [{name: 'cluster', label: 'cluster', category: 'quickfix', categoryHtml: 'QUICKFI<span class="highlighted">X</span>'}]};

        //when
        SuggestionService.setColumn(column, SuggestionService.showAllAction);
        //then
        expect(SuggestionService.searchActionString).toBeFalsy();
        expect(ColumnSuggestionService.transformations.QUICKFIX[0].categoryHtml).toBe('QUICKFIX');

    }));

    it('should NOT init column suggestions when column is already selected', inject(function(SuggestionService, ColumnSuggestionService) {
        //given
        expect(ColumnSuggestionService.initTransformations).not.toHaveBeenCalled();
        var column = {id: '0001'};
        SuggestionService.currentColumn = column;

        //when
        SuggestionService.setColumn(column);

        //then
        expect(ColumnSuggestionService.initTransformations).not.toHaveBeenCalled();
    }));

    it('should select TEXT tab', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.tab).toBeFalsy();

        //when
        SuggestionService.selectTab('TEXT');

        //then
        expect(SuggestionService.tab).toBe(0);
    }));

    it('should select TEXT tab', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.tab).toBeFalsy();

        //when
        SuggestionService.selectTab('CELL');

        //then
        expect(SuggestionService.tab).toBe(1);
    }));

    it('should select TEXT tab', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.tab).toBeFalsy();

        //when
        SuggestionService.selectTab('LINE');

        //then
        expect(SuggestionService.tab).toBe(2);
    }));

    it('should select TEXT tab', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.tab).toBeFalsy();

        //when
        SuggestionService.selectTab('COLUMN');

        //then
        expect(SuggestionService.tab).toBe(3);
    }));

    it('should select TABLE tab', inject(function(SuggestionService) {
        //given
        expect(SuggestionService.tab).toBeFalsy();

        //when
        SuggestionService.selectTab('TABLE');

        //then
        expect(SuggestionService.tab).toBe(4);
    }));

    it('should reset column suggestions', inject(function(SuggestionService, ColumnSuggestionService) {
        //given
        expect(ColumnSuggestionService.reset).not.toHaveBeenCalled();

        //when
        SuggestionService.reset();

        //then
        expect(ColumnSuggestionService.reset).toHaveBeenCalled();
    }));

    it('should reset current column', inject(function(SuggestionService) {
        //given
        SuggestionService.currentColumn = {};

        //when
        SuggestionService.reset();

        //then
        expect(SuggestionService.currentColumn).toBeFalsy();
    }));

});