describe('Suggestion Service', function() {
    'use strict';

    beforeEach(module('data-prep.services.transformation'));

    beforeEach(inject(function(ColumnSuggestionService, LineSuggestionService) {
        spyOn(LineSuggestionService, 'initTransformations').and.returnValue();
        spyOn(ColumnSuggestionService, 'initTransformations').and.returnValue();
    }));

    describe('transformations/suggestions', function() {
        it('should init column suggestions', inject(function(SuggestionService, ColumnSuggestionService) {
            //given
            expect(ColumnSuggestionService.initTransformations).not.toHaveBeenCalled();
            var column = {id: '0001'};

            //when
            SuggestionService.setColumn(column);

            //then
            expect(ColumnSuggestionService.initTransformations).toHaveBeenCalledWith(column);
        }));

        it('should init line suggestions', inject(function(SuggestionService, LineSuggestionService) {
            //given
            expect(LineSuggestionService.initTransformations).not.toHaveBeenCalled();
            var line = {tdpId: 125};

            //when
            SuggestionService.setLine(line);

            //then
            expect(LineSuggestionService.initTransformations).toHaveBeenCalled();
        }));

        it('should NOT init line suggestions when selected line is falsy', inject(function(SuggestionService, LineSuggestionService) {
            //given
            expect(LineSuggestionService.initTransformations).not.toHaveBeenCalled();

            //when
            SuggestionService.setLine();

            //then
            expect(LineSuggestionService.initTransformations).not.toHaveBeenCalled();
        }));
    });

    describe('tab', function() {
        it('should select TEXT tab', inject(function(SuggestionService) {
            //given
            expect(SuggestionService.tab).toBeFalsy();

            //when
            SuggestionService.selectTab('TEXT');

            //then
            expect(SuggestionService.tab).toBe(0);
        }));

        it('should select CELL tab', inject(function(SuggestionService) {
            //given
            expect(SuggestionService.tab).toBeFalsy();

            //when
            SuggestionService.selectTab('CELL');

            //then
            expect(SuggestionService.tab).toBe(1);
        }));

        it('should select LINE tab', inject(function(SuggestionService) {
            //given
            expect(SuggestionService.tab).toBeFalsy();

            //when
            SuggestionService.selectTab('LINE');

            //then
            expect(SuggestionService.tab).toBe(2);
        }));

        it('should select COLUMN tab', inject(function(SuggestionService) {
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
    });

});