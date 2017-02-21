/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const sortList = [
	{ id: 'name', name: 'NAME_SORT', property: 'name' },
	{ id: 'date', name: 'DATE_SORT', property: 'created' },
];

const orderList = [
	{ id: 'asc', name: 'ASC_ORDER' },
	{ id: 'desc', name: 'DESC_ORDER' },
];

export const lookupState = {
	actions: [],                                                // Actions list to add to the lookup  (1 action per dataset)
	addedActions: [],                                           // Actions already added to the lookup
	datasets: [],                                               // Datasets list to add to the lookup
	columnCheckboxes: [],                                       // column checkboxes model
	columnsToAdd: [],                                           // columns that are checked
	dataset: null,                                              // loaded lookup action (on a lookup dataset)
	data: null,                                                 // selected lookup action dataset data
	dataView: new Slick.Data.DataView({ inlineFilters: false }),  // grid view that hold the dataset data
	selectedColumn: null,                                       // selected column
	visibility: false,                                          // visibility flag
	step: null,                                                  // lookup step
	sort: sortList[1],
	order: orderList[1],
	sortList,
	orderList,
	searchDatasetString: '',
	showTooltip: false,
	tooltip: {},
	tooltipRuler: null,
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:LookupStateService
 * @description Lookup state service.
 */
export function LookupStateService() {
	return {
		reset,
		setVisibility,

		// lookup user selection update
		setSelectedColumn,
		updateColumnsToAdd,

		// init lookup
		setActions,
		setAddedActions,
		setDatasets,

		setAddMode,
		setUpdateMode,
		setSort,
		setOrder,
	};

	/**
	 * @ngdoc method
	 * @name sortDatasets
	 * @description Sort lookup datasets
	 */
	function sortDatasets() {
		lookupState.datasets = _.sortBy(lookupState.datasets,
			function (dataset) {
				const model = dataset.model;
				return _.isNumber(model[lookupState.sort.property]) ? model[lookupState.sort.property] : model[lookupState.sort.property].toLowerCase();
			});

		if (lookupState.order.id === 'desc') {
			lookupState.datasets = lookupState.datasets.reverse();
		}
	}

	/**
	 * @ngdoc method
	 * @name setSort
	 * @methodOf data-prep.services.state.service:LookupStateService
	 * @description Set the sort type of the lookup datasets
	 */
	function setSort(sort) {
		lookupState.sort = sort;
		sortDatasets();
	}

	/**
	 * @ngdoc method
	 * @name setOrder
	 * @methodOf data-prep.services.state.service:LookupStateService
	 * @description Set the order type of the lookup datasets
	 */
	function setOrder(order) {
		lookupState.order = order;
		sortDatasets();
	}

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
	 * @name setDatasets
	 * @methodOf data-prep.services.state.service:LookupStateService
	 * @param {object} datasets The datasets to add to the lookup
	 * @description Sets the current datasets added to the lookup
	 */
	function setDatasets(datasets) {
		lookupState.datasets = datasets;
		sortDatasets();
	}

	/**
	 * @ngdoc method
	 * @name setAddedActions
	 * @methodOf data-prep.services.state.service:LookupStateService
	 * @param {object} actions The actions to add to the lookup
	 * @description Sets the current actions added to the lookup
	 */
	function setAddedActions(actions) {
		lookupState.addedActions = actions;
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
		const addedColIds = lookupState.step ?
			_.map(lookupState.step.actionParameters.parameters.lookup_selected_cols, 'id') :
			[];
		lookupState.columnCheckboxes = _.map(data.metadata.columns, function (col) {
			return {
				id: col.id,
				name: col.name,
				isAdded: addedColIds.indexOf(col.id) > -1,
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
		setData(data); // this updates the checkboxes
		setSelectedColumn(data.metadata.columns[0]); // this update the columns to add
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
		const selectedColumn = _.find(data.metadata.columns, { id: step.actionParameters.parameters.lookup_join_on });
		lookupState.step = step;
		setDataset(lookupAction);
		setData(data); // this updates the checkboxes
		setSelectedColumn(selectedColumn); // this update the columns to add
	}

	/**
	 * @ngdoc method
	 * @name reset
	 * @methodOf data-prep.services.state.service:LookupStateService
	 * @description Reset the lookup internal state
	 */
	function reset() {
		lookupState.actions = [];
		lookupState.addedActions = [];
		lookupState.datasets = [];
		lookupState.columnsToAdd = [];
		lookupState.columnCheckboxes = [];
		lookupState.dataset = null;
		lookupState.data = null;
		lookupState.selectedColumn = null;
		lookupState.visibility = false;
		lookupState.step = null;
		lookupState.searchDatasetString = '';
		lookupState.showTooltip = false;
		lookupState.tooltip = {};
		lookupState.tooltipRuler = null;
	}
}
