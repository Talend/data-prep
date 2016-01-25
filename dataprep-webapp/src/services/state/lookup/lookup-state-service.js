(function () {
    'use strict';

    var lookupState = {
        actions: [],                                                // the lookup actions (1 action per dataset)
        columnCheckboxes: [],                                       // column checkboxes model
        columnsToAdd: [],                                           // columns that are checked
        dataset: null,                                              // loaded lookup action (on a lookup dataset)
        data: null,                                                 // selected lookup action dataset data
        dataView: new Slick.Data.DataView({inlineFilters: false}),  // grid view that hold the dataset data
        selectedColumn: null,                                       // selected column
        visibility: false,                                          // visibility flag
        step: null                                                  // lookup step
    };

    /**
     * @ngdoc service
     * @name data-prep.services.state.service:GridStateService
     * @description Lookup state service.
     */
    function LookupStateService() {
        return {
            reset: reset,
            setVisibility: setVisibility,

            //lookup user selection update
            setSelectedColumn: setSelectedColumn,
            updateColumnsToAdd: updateColumnsToAdd,

            //init lookup
            setActions: setActions,
            setAddMode: setAddMode,
            setUpdateMode: setUpdateMode

        };

        /**
         * @ngdoc method
         * @name setVisibility
         * @methodOf data-prep.services.state.service:LookupStateService
         * @description Set the lookup visibility
         */
        function setVisibility(visibility) {
            lookupState.visibility = visibility;
        }

        /**
         * @ngdoc method
         * @name setDataset
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} lookupAction The lookup action on a dataset
         * @description Sets the current lookup action
         */
        function setDataset(lookupAction) {
            lookupState.dataset = lookupAction;
        }

        /**
         * @ngdoc method
         * @name setData
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} data The data
         * @description Set data to display in the grid and reset the column checkboxes
         */
        function setData(data) {
            lookupState.dataView.beginUpdate();
            lookupState.dataView.setItems(data.records, 'tdpId');
            lookupState.dataView.endUpdate();

            lookupState.data = data;
            lookupState.columnsToAdd = [];
            createColumnsCheckboxes(data);
        }

        /**
         * @ngdoc method
         * @name setSelectedColumn
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} column The column metadata
         * @description Set the lookup ds selected column and update columns to add (omit the selected column)
         */
        function setSelectedColumn(column) {
            lookupState.selectedColumn = column;
            if (column) {
                updateColumnsToAdd();
            }
        }

        /**
         * @ngdoc method
         * @name createColumnsCheckboxes
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} data The data
         * @description Create the checkboxes definition for each column
         */
        function createColumnsCheckboxes(data) {
            var addedColIds = lookupState.step ?
                /*jshint camelcase: false */
                _.map(lookupState.step.actionParameters.parameters.lookup_selected_cols, 'id') :
                [];
            lookupState.columnCheckboxes = _.map(data.metadata.columns, function(col) {
                return {
                    id: col.id,
                    name: col.name,
                    isAdded: addedColIds.indexOf(col.id) > -1
                };
            });
        }

        /**
         * @ngdoc method
         * @name updateColumnsToAdd
         * @methodOf data-prep.services.state.service:LookupStateService
         * @description Update the columns to add in the lookup step
         */
        function updateColumnsToAdd() {
            lookupState.columnsToAdd = _.chain(lookupState.columnCheckboxes)
                .filter('isAdded')
                .filter(function (col) {
                    return col.id !== lookupState.selectedColumn.id;
                })
                .map(function (obj) {
                    return _.omit(obj, 'isAdded');
                })
                .value();
        }

        /**
         * @ngdoc method
         * @name setActions
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {Array} actions The lookup actions (1 per possible dataset)
         * @description Sets the actions
         */
        function setActions(actions) {
            lookupState.actions = actions;
        }

        /**
         * @ngdoc method
         * @name setAddMode
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} lookupAction the lookup action
         * @param {object} data The selected lookup dataset data
         * @description Set parameters for add mode
         */
        function setAddMode(lookupAction, data) {
            lookupState.step = null;
            setDataset(lookupAction);
            setData(data); //this updates the checkboxes
            setSelectedColumn(data.metadata.columns[0]); //this update the columns to add
        }

        /**
         * @ngdoc method
         * @name setUpdateMode
         * @methodOf data-prep.services.state.service:LookupStateService
         * @param {object} lookupAction the lookup action
         * @param {object} data The selected lookup dataset data
         * @param {object} step The step to update
         * @description Set parameters for update mode
         */
        function setUpdateMode(lookupAction, data, step) {
            /*jshint camelcase: false */
            var selectedColumn = _.find(data.metadata.columns, {id: step.actionParameters.parameters.lookup_join_on});
            lookupState.step = step;
            setDataset(lookupAction);
            setData(data); //this updates the checkboxes
            setSelectedColumn(selectedColumn); //this update the columns to add
        }

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.services.state.service:LookupStateService
         * @description Reset the lookup internal state
         */
        function reset() {
            lookupState.actions = [];
            lookupState.columnsToAdd = [];
            lookupState.columnCheckboxes = [];
            lookupState.dataset = null;
            lookupState.data = null;
            lookupState.selectedColumn = null;
            lookupState.visibility = false;
            lookupState.step = null;
        }
    }

    angular.module('data-prep.services.state')
        .service('LookupStateService', LookupStateService)
        .constant('lookupState', lookupState);
})();