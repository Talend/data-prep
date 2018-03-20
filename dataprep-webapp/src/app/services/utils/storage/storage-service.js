/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const PREFIX = 'org.talend.dataprep.';
const DATASETS_SORT_KEY = 'org.talend.dataprep.datasets.sort';
const EXPORT_PARAMS_KEY = 'org.talend.dataprep.export.params';
const FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';
const LOOKUP_DATASETS_KEY = 'org.talend.dataprep.lookup_datasets';
const LOOKUP_DATASETS_SORT_KEY = 'org.talend.dataprep.lookup_datasets_sort';
const LOOKUP_DATASETS_ORDER_KEY = 'org.talend.dataprep.lookup_datasets_order';
const PREFIX_FILTER = 'org.talend.dataprep.filter_';
const PREFIX_SELECTED_COLUMNS_KEY = 'org.talend.dataprep.selected_columns_';
const PREPARATIONS_SORT_KEY = 'org.talend.dataprep.preparations.sort';
const TOUR_OPTIONS_KEY = 'org.talend.dataprep.tour_options';
const SIDE_PANEL_DOCK_KEY = 'org.talend.dataprep.sidePanel.docked';
const LISTS_DISPLAY_MODE_KEY = 'org.talend.dataprep.lists_display_mode';

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:StorageService
 * @description Local storage service
 */
export default class StorageService {
	constructor($window) {
		'ngInject';
		this.$window = $window;
	}

    // --------------------------------------------------------------------------------------------
    // ---------------------------------------Common-----------------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name setItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage.
     * The value us stringified to get back the same type.
     */
	setItem(key, value) {
		this.$window.localStorage.setItem(key, JSON.stringify(value));
	}

    /**
     * @ngdoc method
     * @name getItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @param {any} defaultValue The default value to return when key is not in localStorage
     * @description Get the value associated to the provided key.
     * The result have the same type as the saved value.
     * @returns {any} The value associated to the provided key.
     */
	getItem(key, defaultValue) {
		const value = this.$window.localStorage.getItem(key);
		return value ? JSON.parse(value) : defaultValue;
	}

    /**
     * @ngdoc method
     * @name removeItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @description Remove the entry associated to the provided key.
     */
	removeItem(key) {
		this.$window.localStorage.removeItem(key);
	}


    // --------------------------------------------------------------------------------------------
    // -------------------------------------------FILTER-----------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name saveFilter
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} entityId The dataset/preparation Id
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage.
     */
	saveFilter(entityId, value) {
		this.setItem(PREFIX_FILTER + entityId, value);
	}

    /**
     * @ngdoc method
     * @name getFilter
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} entityId The dataset/preparation Id
     * @description Get the saved filters for the entity.
     * @returns {array} The filters or an empty array is no saved filter found.
     */
	getFilter(entityId) {
		return this.getItem(PREFIX_FILTER + entityId, []);
	}

    /**
     * @ngdoc method
     * @name getFilter
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} entityId The dataset/preparation Id
     * @description Remove the saved filters associated to the entity.
     */
	removeFilter(entityId) {
		return this.removeItem(PREFIX_FILTER + entityId);
	}

    // --------------------------------------------------------------------------------------------
    // -------------------------------------------Feedback-----------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name saveFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage.
     * The value us stringified to get back the same type.
     */
	saveFeedbackUserMail(value) {
		this.setItem(FEEDBACK_USER_MAIL_KEY, value);
	}

    /**
     * @ngdoc method
     * @name getFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the value associated to the provided key.
     * The result have the same type as the saved value.
     * @returns {string} The value associated to the provided key.
     */
	getFeedbackUserMail() {
		return this.getItem(FEEDBACK_USER_MAIL_KEY, '');
	}

    // --------------------------------------------------------------------------------------------
    // ------------------------------------------Export--------------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name saveExportParams
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage. The value us stringified to get back the same type.
     */
	saveExportParams(value) {
		this.setItem(EXPORT_PARAMS_KEY, value);
	}

    /**
     * @ngdoc method
     * @name getExportParams
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the value associated to the provided key. The result have the same type as the saved value.
     * @returns {object} The value associated to the provided key.
     */
	getExportParams() {
		return this.getItem(EXPORT_PARAMS_KEY);
	}

    // --------------------------------------------------------------------------------------------
    // -------------------------------------------Aggregation--------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name removeItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @param {string} columnId The column id
     * @description Create a localStorage key for aggregation
     */
	getAggregationKey(datasetId, preparationId, columnId) {
		let key = PREFIX + 'aggregation.';
		key += (datasetId || '') + '.';
		key += (preparationId || '') + '.';
		key += columnId;

		return key;
	}

    /**
     * @ngdoc method
     * @name setAggregation
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @param {string} columnId The column id
     * @param {object} aggregation The aggregation to save
     * @description Save the aggregation with a generated key from the other parameters.
     */
	setAggregation(datasetId, preparationId, columnId, aggregation) {
		const key = this.getAggregationKey(datasetId, preparationId, columnId);
		this.setItem(key, aggregation);
	}

    /**
     * @ngdoc method
     * @name getAggregation
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @param {string} columnId The column id
     * @description Get the aggregation with a generated key.
     */
	getAggregation(datasetId, preparationId, columnId) {
		const key = this.getAggregationKey(datasetId, preparationId, columnId);
		return this.getItem(key);
	}

    /**
     * @ngdoc method
     * @name removeAggregation
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @param {string} columnId The column id
     * @description Remove the aggregation on a generated key.
     */
	removeAggregation(datasetId, preparationId, columnId) {
		const key = this.getAggregationKey(datasetId, preparationId, columnId);
		this.removeItem(key);
	}

    /**
     * @ngdoc method
     * @name removeAllAggregations
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @description Remove all aggregations on the dataset/preparation.
     */
	removeAllAggregations(datasetId, preparationId) {
		const keyAggregationPrefix = this.getAggregationKey(datasetId, preparationId, '');
		const aggregationsToRemove = [];

		for (let i = 0, len = this.$window.localStorage.length; i < len; i++) {
			const key = this.$window.localStorage.key(i);
			if (key.indexOf(keyAggregationPrefix) === 0) {
				aggregationsToRemove.push(key);
			}
		}

		aggregationsToRemove.forEach((key) => {
			this.removeItem(key);
		});
	}

    /**
     * @ngdoc method
     * @name savePreparationAggregationsFromDataset
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @description Get all the saved aggregations on the dataset
     * and save them for the preparation.
     */
	savePreparationAggregationsFromDataset(datasetId, preparationId) {
		const datasetAggregationPrefix = this.getAggregationKey(datasetId, '', '');
		const aggregationsToAdd = [];

		for (let i = 0, len = this.$window.localStorage.length; i < len; i++) {
			const key = this.$window.localStorage.key(i);
			if (key.indexOf(datasetAggregationPrefix) === 0) {
				aggregationsToAdd.push({
					columnId: key.substring(key.lastIndexOf('.') + 1),
					aggregation: this.getItem(key),
				});
			}
		}

		aggregationsToAdd.forEach((aggregDef) => {
			this.setAggregation(
                datasetId,
                preparationId,
                aggregDef.columnId,
                aggregDef.aggregation
            );
		});
	}

    // --------------------------------------------------------------------------------------------
    // -------------------------------------------Lookup-------------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getLookupDatasets
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the lookup datasets from localStorage
     */
	getLookupDatasets() {
		return this.getItem(LOOKUP_DATASETS_KEY, []);
	}

    /**
     * @ngdoc method
     * @name setLookupDatasets
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the lookup datasets in localStorage
     */
	setLookupDatasets(datasets) {
		this.setItem(LOOKUP_DATASETS_KEY, datasets);
	}

    /**
     * @ngdoc method
     * @name getLookupDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the Lookup datasets sort from localStorage
     */
	getLookupDatasetsSort() {
		return this.getItem(LOOKUP_DATASETS_SORT_KEY);
	}

    /**
     * @ngdoc method
     * @name setLookupDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the Lookup datasets sort in localStorage
     */
	setLookupDatasetsSort(sort) {
		this.setItem(LOOKUP_DATASETS_SORT_KEY, sort);
	}

    /**
     * @ngdoc method
     * @name getLookupDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the Lookup datasets order from localStorage
     */
	getLookupDatasetsOrder() {
		return this.getItem(LOOKUP_DATASETS_ORDER_KEY);
	}

    /**
     * @ngdoc method
     * @name setLookupDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the Lookup datasets order in localStorage
     */
	setLookupDatasetsOrder(order) {
		this.setItem(LOOKUP_DATASETS_ORDER_KEY, order);
	}

    // --------------------------------------------------------------------------------------------
    // ---------------------------------------------Sort/Order-------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the datasets sort from localStorage
     */
	getDatasetsSort() {
		return this.getItem(DATASETS_SORT_KEY);
	}

    /**
     * @ngdoc method
     * @name setDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the datasets sort in localStorage
     */
	setDatasetsSort(field, isDescending) {
		this.setItem(DATASETS_SORT_KEY, { field, isDescending });
	}

    /**
     * @ngdoc method
     * @name getPreparationsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the preparations sort from localStorage
     */
	getPreparationsSort() {
		return this.getItem(PREPARATIONS_SORT_KEY);
	}

    /**
     * @ngdoc method
     * @name setPreparationsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the preparations sort in localStorage
     */
	setPreparationsSort(field, isDescending) {
		this.setItem(PREPARATIONS_SORT_KEY, { field, isDescending });
	}

    // --------------------------------------------------------------------------------------------
    // ---------------------------------------------Display modes-------------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getListsDisplayModes
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the lists display modes from localStorage
     */
	getListsDisplayModes() {
		return this.getItem(LISTS_DISPLAY_MODE_KEY) || {};
	}

	/**
     * @ngdoc method
     * @name setListsDisplayModes
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the lists display modes in localStorage
     */
	setListsDisplayModes(modes) {
		this.setItem(LISTS_DISPLAY_MODE_KEY, modes);
	}


    // --------------------------------------------------------------------------------------------
    // ------------------------------------------Selected columns----------------------------------
    // --------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getSelectedColumns
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} id The id of the preparation or dataset
     * @description Get the least selected columns of a preparation/detaset from localStorage
     */
	getSelectedColumns(id) {
		return this.getItem(PREFIX_SELECTED_COLUMNS_KEY + id, []);
	}

    /**
     * @ngdoc method
     * @name setSelectedColumns
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} id The id of the preparation or dataset
     * @param {array} selectedColumns The selected columns of the preparation or dataset
     * @description  Set the least selected columns of a preparation/detaset to localStorage
     */
	setSelectedColumns(id, selectedColumns) {
		this.setItem(PREFIX_SELECTED_COLUMNS_KEY + id, selectedColumns);
	}

	// --------------------------------------------------------------------------------------------
	// ------------------------------------------SIDE PANEL----------------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name setSidePanelDock
	 * @methodOf data-prep.services.utils.service:StorageService
	 * @param {boolean} docked The side panel dock value
	 * @description Save the value with the provided key in localStorage.
	 */
	setSidePanelDock(docked) {
		this.setItem(SIDE_PANEL_DOCK_KEY, docked);
	}

	/**
	 * @ngdoc method
	 * @name getSidePanelDock
	 * @methodOf data-prep.services.utils.service:StorageService
	 * @description Get the saved side panel dock value
	 * @returns {array} The side panel dock value
	 */
	getSidePanelDock() {
		return this.getItem(SIDE_PANEL_DOCK_KEY, false);
	}


	// --------------------------------------------------------------------------------------------
	// ------------------------------------------OnBoarding----------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name getTourOptions
	 * @methodOf data-prep.services.utils.service:StorageService
	 * @description Get options from localStorage
	 * @returns {object} The saved tour config
	 */
	getTourOptions() {
		return this.getItem(TOUR_OPTIONS_KEY, {});
	}

	/**
	 * @ngdoc method
	 * @name setTourOptions
	 * @methodOf data-prep.services.utils.service:StorageService
	 * @param {object} options The options to save
	 * @description Set options in localStorage
	 */
	setTourOptions(options) {
		this.setItem(TOUR_OPTIONS_KEY, options);
	}
}
