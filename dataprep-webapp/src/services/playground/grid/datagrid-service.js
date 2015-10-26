(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.playground.service:DatagridService
     * @description Datagrid service. This service holds the datagrid (SlickGrid) view and the (SlickGrid) filters<br/>
     * <b style="color: red;">WARNING : do NOT use this service directly for FILTERS.
     * {@link data-prep.services.filter.service:FilterService FilterService} must be the only entry point for datagrid filters</b>
     * @requires data-prep.services.utils.service:ConverterService
     * @requires data-prep.state.service:StateService
     */
    function DatagridService(ConverterService, StateService, state) {
        var DELETE = 'DELETE';
        var REPLACE = 'REPLACE';
        var INSERT = 'INSERT';

        var service = {
            focusedColumn: null, //TODO JSO : put this in state

            //grid data
            updateData: updateData, //updata data in the current dataset
            getColumns: getColumns,
            getColumnsContaining: getColumnsContaining,
            getSameContentConfig: getSameContentConfig,
            getNumericColumns: getNumericColumns,

            //preview
            execute: execute,
            previewDataExecutor: previewDataExecutor
        };
        return service;

        //------------------------------------------------------------------------------------------------------
        //---------------------------------------------------DATA-----------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getLastNewColumnId
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {object} columns The new columns
         * @description Get the last new created column
         */
        function getLastNewColumnId(columns) {
            var ancientColumnsIds = _.map(state.playground.data.columns, 'id');
            var newColumnsIds = _.map(columns, 'id');
            var diffIds = _.difference(newColumnsIds, ancientColumnsIds);

            return diffIds[diffIds.length - 1];
        }

        /**
         * @ngdoc method
         * @name updateData
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object} data - the new data (columns and records)
         * @description Update the data in the datagrid
         */
        function updateData(data) {
            if (state.playground.data.columns.length < data.columns.length) {
                service.focusedColumn = getLastNewColumnId(data.columns);
            }
            StateService.setCurrentData(data);
        }

        //------------------------------------------------------------------------------------------------------
        //--------------------------------------------------PREVIEW---------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name execute
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object} executor The infos to apply on the dataset
         * @description Update the data in the datagrid with a set of instructions and the column list to apply.
         * This allows to update the dataset, with limited SlickGrid computation, for more performant operations than
         * setItems which compute everything on the whole dataset.
         */
        function execute(executor) {
            if (!executor) {
                return;
            }

            var revertInstructions = [];

            state.playground.grid.dataView.beginUpdate();
            _.forEach(executor.instructions, function (step) {
                switch (step.type) {
                    case INSERT:
                        state.playground.grid.dataView.insertItem(step.index, step.row);
                        revertInstructions.push({
                            type: DELETE,
                            row: step.row
                        });
                        break;
                    case DELETE:
                        var index = state.playground.grid.dataView.getIdxById(step.row.tdpId);
                        state.playground.grid.dataView.deleteItem(step.row.tdpId);
                        revertInstructions.push({
                            type: INSERT,
                            row: step.row,
                            index: index
                        });
                        break;
                    case REPLACE:
                        var originalRow = state.playground.grid.dataView.getItemById(step.row.tdpId);
                        state.playground.grid.dataView.updateItem(step.row.tdpId, step.row);
                        revertInstructions.push({
                            type: REPLACE,
                            row: originalRow
                        });
                        break;
                }
            });
            state.playground.grid.dataView.endUpdate();

            var reverter = {
                instructions: revertInstructions,
                preview: state.playground.data.preview,
                columns: state.playground.data.columns
            };

            if (state.playground.data.columns.length < executor.columns.length) {
                service.focusedColumn = getLastNewColumnId(executor.columns);
            }

            StateService.setCurrentData({
                columns: executor.columns,
                records: state.playground.data.records,
                preview: executor.preview
            });

            return reverter;
        }

        /**
         * @ngdoc method
         * @name previewDataExecutor
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {Object} data The new preview data to insert
         * @description Create an executor that reflect the provided preview data, in order to update the current dataset
         */
        function previewDataExecutor(data) {
            var executor = {
                columns: data.columns,
                instructions: [],
                preview: true
            };

            var nextInsertionIndex = state.playground.grid.dataView.getIdxById(data.records[0].tdpId);
            _.forEach(data.records, function (row) {
                if (row.__tdpRowDiff || row.__tdpDiff) {
                    if (row.__tdpRowDiff === 'new') {
                        executor.instructions.push({
                            type: INSERT,
                            row: row,
                            index: nextInsertionIndex
                        });
                    }
                    else {
                        executor.instructions.push({
                            type: REPLACE,
                            row: row
                        });
                    }
                }
                nextInsertionIndex++;
            });

            return executor;
        }

        //------------------------------------------------------------------------------------------------------
        //------------------------------------------------DATA UTILS--------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name getColumns
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {boolean} excludeNumeric - filter the numeric columns
         * @param {boolean} excludeBoolean - filter the boolean columns
         * @description Filter the column ids
         * @returns {Object[]} - the column list that match the desired filters (id & name)
         */
        function getColumns(excludeNumeric, excludeBoolean) {
            var cols = state.playground.data.columns;

            if (excludeNumeric) {
                cols = _.filter(cols, function (col) {
                    var simplifiedType = ConverterService.simplifyType(col.type);
                    return simplifiedType !== 'integer' && simplifiedType !== 'decimal';
                });
            }
            if (excludeBoolean) {
                cols = _.filter(cols, function (col) {
                    return col.type !== 'boolean';
                });
            }

            return _.map(cols, function (col) {
                return {'id': col.id, 'name': col.name};
            });
        }

        /**
         * @ngdoc method
         * @name getColumnsContaining
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} regexp - the regexp
         * @param {boolean} canBeNumeric - filter the numeric columns
         * @param {boolean} canBeBoolean - filter the boolean columns
         * @description Return the column id list that has a value that match the regexp
         * @returns {Object[]} - the column list that contains a value that match the regexp (col.id & col.name)
         */
        function getColumnsContaining(regexp, canBeNumeric, canBeBoolean) {
            var results = [];

            var data = state.playground.data.records;
            var potentialColumns = getColumns(!canBeNumeric, !canBeBoolean);

            //we loop over data while there is data and potential columns that can contains the searched term
            //if a col value for a row contains the term, we add it to result
            var dataIndex = 0;
            while (dataIndex < data.length && potentialColumns.length) {
                var record = data[dataIndex];
                for (var colIndex in potentialColumns) {
                    var col = potentialColumns[colIndex];
                    if (record[col.id].toLowerCase().match(regexp)) {
                        potentialColumns.splice(colIndex, 1);
                        results.push(col);
                    }
                }

                potentialColumns = _.difference(potentialColumns, results);
                dataIndex++;
            }

            return results;
        }

        /**
         * @ngdoc method
         * @name getSameContentConfig
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {string} colId The column index
         * @param {string} term The cell content to search
         * @param {string} cssClass The css class to apply
         * @description Return displayed rows index where data[rowId][colId] contains the searched term
         * @returns {Object} The SlickGrid css config for each column with the provided content
         */
        function getSameContentConfig(colId, term, cssClass) {
            var config = {};
            for (var i = 0; i < state.playground.grid.dataView.getLength(); ++i) {
                var item = state.playground.grid.dataView.getItem(i);
                if (term === item[colId]) {
                    config[i] = {};
                    config[i][colId] = cssClass;
                }
            }

            return config;
        }

        /**
         * @ngdoc method
         * @name getNumericColumns
         * @methodOf data-prep.services.playground.service:DatagridService
         * @param {object} columnToSkip The column to skip
         * @description Filter numeric columns
         * @returns {array} The numeric columns
         */
        function getNumericColumns(columnToSkip) {
            return _.chain(state.playground.data.columns)
                .filter(function (column) {
                    return !columnToSkip || column.id !== columnToSkip.id;
                })
                .filter(function (column) {
                    var simpleType = ConverterService.simplifyType(column.type);
                    return simpleType === 'integer' || simpleType === 'decimal';
                })
                .value();
        }
    }

    angular.module('data-prep.services.playground')
        .service('DatagridService', DatagridService);

})();