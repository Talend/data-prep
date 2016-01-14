describe('Playground state service', function () {
    'use strict';

    var recipeStateMock, gridStateMock, filterStateMock;

    beforeEach(module('data-prep.services.state', function ($provide) {
        recipeStateMock = {};
        gridStateMock = {};
        filterStateMock = {};
        $provide.constant('recipeState', recipeStateMock);
        $provide.constant('gridState', gridStateMock);
        $provide.constant('filterState', filterStateMock);
    }));

    beforeEach(inject(function (GridStateService, FilterStateService, LookupStateService, SuggestionsStateService) {
        spyOn(GridStateService, 'setData').and.returnValue();
        spyOn(GridStateService, 'setFilter').and.returnValue();
        spyOn(GridStateService, 'reset').and.returnValue();
        spyOn(FilterStateService, 'addGridFilter').and.returnValue();
        spyOn(FilterStateService, 'updateGridFilter').and.returnValue();
        spyOn(FilterStateService, 'removeGridFilter').and.returnValue();
        spyOn(FilterStateService, 'removeAllGridFilters').and.returnValue();
        spyOn(FilterStateService, 'reset').and.returnValue();
        spyOn(LookupStateService, 'reset').and.returnValue();
        spyOn(SuggestionsStateService, 'reset').and.returnValue();
    }));

    describe('playground state', function () {
        it('should set dataset metadata in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            var dataset = {
                id: '958cb63f235e4565'
            };
            expect(playgroundState.dataset).not.toBe(dataset);

            //when
            PlaygroundStateService.setDataset(dataset);

            //then
            expect(playgroundState.dataset).toBe(dataset);
        }));

        it('should set data in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            expect(playgroundState.data).toBeFalsy();
            var data = {
                records: []
            };

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(playgroundState.data).toBe(data);
        }));

        it('should set data in grid', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
            //given
            expect(GridStateService.setData).not.toHaveBeenCalled();
            var data = {
                records: []
            };

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(GridStateService.setData).toHaveBeenCalledWith(data);
        }));

        it('should set filters again on data change in grid to refresh statefull filters (ex: invalid filters)', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
            //given
            expect(GridStateService.setData).not.toHaveBeenCalled();
            var data = {
                records: []
            };
            var filters = [{}, {}];
            filterStateMock.gridFilters = filters;

            //when
            PlaygroundStateService.setData(data);

            //then
            expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
        }));

        it('should set preparation in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            expect(playgroundState.preparation).toBeFalsy();
            var preparation = {
                id: '3d245846bc46f51'
            };

            //when
            PlaygroundStateService.setPreparation(preparation);

            //then
            expect(playgroundState.preparation).toBe(preparation);
        }));

        it('should set name edition flag in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            expect(playgroundState.nameEditionMode).toBeFalsy();

            //when
            PlaygroundStateService.setNameEditionMode(true);

            //then
            expect(playgroundState.nameEditionMode).toBe(true);
        }));

        it('should set playground visibility to true in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            playgroundState.visible = false;

            //when
            PlaygroundStateService.show();

            //then
            expect(playgroundState.visible).toBe(true);
        }));

        it('should set playground visibility to false in state', inject(function (playgroundState, PlaygroundStateService) {
            //given
            playgroundState.visible = true;

            //when
            PlaygroundStateService.hide();

            //then
            expect(playgroundState.visible).toBe(false);
        }));

        it('should update columns statistics', inject(function (playgroundState, PlaygroundStateService) {
            //given
            playgroundState.data = {
                metadata: {
                    columns: [
                        {id: '0000', statistics: {}},
                        {id: '0001', statistics: {}},
                        {id: '0002', statistics: {}}
                    ]
                }
            };

            var newColumns = [
                {id: '0000', statistics: {frequencyTable: [{data: '5.0', occurrences: 98}]}},
                {id: '0001', statistics: {frequencyTable: [{data: 'Toto', occurrences: 5}]}},
                {id: '0002', statistics: {frequencyTable: [{data: '', occurrences: 66}]}}
            ];

            //when
            PlaygroundStateService.updateColumnsStatistics(newColumns);

            //then
            expect(playgroundState.data.metadata.columns[0].statistics).toBe(newColumns[0].statistics);
            expect(playgroundState.data.metadata.columns[1].statistics).toBe(newColumns[1].statistics);
            expect(playgroundState.data.metadata.columns[2].statistics).toBe(newColumns[2].statistics);
        }));
    });

    describe('filters', function () {
        describe('add', function () {
            it('should add filter in filter list', inject(function (PlaygroundStateService, FilterStateService) {
                //given
                var filter = {column: '0001'};

                //when
                PlaygroundStateService.addGridFilter(filter);

                //then
                expect(FilterStateService.addGridFilter).toHaveBeenCalledWith(filter);
            }));

            it('should apply filters in grid', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
                //given
                var filter = {column: '0001'};
                var filters = [{}, {}];
                var data = {records: []};
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.addGridFilter(filter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));
        });

        describe('update', function () {
            it('should update filter in filter list', inject(function (PlaygroundStateService, FilterStateService) {
                //given
                var oldFilter = {column: '0001'};
                var newFilter = {column: '0002'};

                //when
                PlaygroundStateService.updateGridFilter(oldFilter, newFilter);

                //then
                expect(FilterStateService.updateGridFilter).toHaveBeenCalledWith(oldFilter, newFilter);
            }));

            it('should apply filters in grid', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
                //given
                var oldFilter = {column: '0001'};
                var newFilter = {column: '0002'};
                var filters = [{}, {}];
                var data = {records: []};
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.updateGridFilter(oldFilter, newFilter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));
        });

        describe('remove', function () {
            it('should add filter in filter list', inject(function (PlaygroundStateService, FilterStateService) {
                //given
                var filter = {column: '0001'};

                //when
                PlaygroundStateService.removeGridFilter(filter);

                //then
                expect(FilterStateService.removeGridFilter).toHaveBeenCalledWith(filter);
            }));

            it('should apply filters in grid on single remove', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
                //given
                var filter = {column: '0001'};
                var filters = [{}, {}];
                var data = {records: []};
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.removeGridFilter(filter);

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));

            it('should add filter in filter list', inject(function (PlaygroundStateService, FilterStateService) {
                //when
                PlaygroundStateService.removeAllGridFilters();

                //then
                expect(FilterStateService.removeAllGridFilters).toHaveBeenCalled();
            }));

            it('should apply filters in grid on remove all', inject(function (playgroundState, PlaygroundStateService, GridStateService) {
                //given
                var filters = [{}, {}];
                var data = {records: []};
                filterStateMock.gridFilters = filters;
                playgroundState.data = data;

                //when
                PlaygroundStateService.removeAllGridFilters();

                //then
                expect(GridStateService.setFilter).toHaveBeenCalledWith(filters, data);
            }));
        });
    });

    describe('reset', function () {
        it('should reset playground and sub-states', inject(function (playgroundState, PlaygroundStateService, GridStateService, FilterStateService, LookupStateService, SuggestionsStateService) {
            //given
            playgroundState.data = {};
            playgroundState.dataset = {};
            playgroundState.lookupData = {};
            playgroundState.nameEditionMode = true;
            playgroundState.preparation = {};

            //when
            PlaygroundStateService.reset();

            //then
            expect(playgroundState.data).toBe(null);
            expect(playgroundState.dataset).toBe(null);
            expect(playgroundState.nameEditionMode).toBe(false);
            expect(playgroundState.lookupData).toBe(null);
            expect(playgroundState.preparation).toBe(null);

            expect(GridStateService.reset).toHaveBeenCalled();
            expect(FilterStateService.reset).toHaveBeenCalled();
            expect(LookupStateService.reset).toHaveBeenCalled();
            expect(SuggestionsStateService.reset).toHaveBeenCalled();
        }));
    });
});
