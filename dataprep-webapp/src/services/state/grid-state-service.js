(function() {
    'use strict';

    var gridState = {dataView: new Slick.Data.DataView({inlineFilters: false})};

    function GridStateService() {
        return {
            reset: reset,

            setFilter: setFilter,

            setColumnFocus: setColumnFocus,
            setData: setData,
            setGridSelection: setGridSelection
        };

        function updateLinesCount(data) {
            gridState.nbLines = gridState.dataView.getLength();
            gridState.nbTotalLines = data.records.length;
            gridState.displayLinesPercentage = (gridState.nbLines * 100 / gridState.nbTotalLines).toFixed(0);
        }

        function setFilter(filters, data) {
            /**
             * @ngdoc method
             * @name filterFn
             * @methodOf data-prep.services.playground.service:DatagridService
             * @param {object} item The item to test
             * @param {object} args Object containing the filters predicates
             * @description Filter function. It iterates over all filters and return if the provided item fit the predicates
             * @returns {boolean} True if the item pass all the filters
             */
            var allFilterFn = function allFilterFn(item, args) {
                //init filters with actual data
                var initializedFilters = _.map(args.filters, function (filter) {
                    return filter(data);
                });
                //execute each filter on the value
                for (var i = 0; i < initializedFilters.length; i++) {
                    var filter = initializedFilters[i];
                    if (!filter(item)) {
                        return false;
                    }
                }
                return true;
            };

            gridState.dataView.beginUpdate();
            gridState.dataView.setFilterArgs({filters: _.map(filters, 'filterFn')});
            gridState.dataView.setFilter(allFilterFn);
            gridState.dataView.endUpdate();

            updateLinesCount(data);
        }

        function setColumnFocus(columnId) {
            gridState.columnFocus = columnId;
        }

        function setData(data) {
            gridState.dataView.beginUpdate();
            gridState.dataView.setItems(data.records, 'tdpId');
            gridState.dataView.endUpdate();

            updateLinesCount(data);
        }

        function setGridSelection(column, line) {
            gridState.selectedColumn = column;
            gridState.selectedLine = line;
        }

        function reset() {
            gridState.columnFocus = null;
            gridState.selectedColumn = null;
            gridState.selectedLine = null;
        }
    }

    angular.module('data-prep.services.state')
        .service('GridStateService', GridStateService)
        .constant('gridState', gridState);
})();