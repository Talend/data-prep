/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const PREFIX = 'org.talend.dataprep.';
const LOOKUP_DATASETS_KEY = 'org.talend.dataprep.lookup_datasets';
const LOOKUP_DATASETS_SORT_KEY = 'org.talend.dataprep.lookup_datasets_sort';
const LOOKUP_DATASETS_ORDER_KEY = 'org.talend.dataprep.lookup_datasets_order';
const DATASETS_SORT_KEY = 'org.talend.dataprep.datasets.sort';
const DATASETS_ORDER_KEY = 'org.talend.dataprep.datasets.order';
const PREPARATIONS_SORT_KEY = 'org.talend.dataprep.preparations.sort';
const PREPARATIONS_ORDER_KEY = 'org.talend.dataprep.preparations.order';
const FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';

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

    //--------------------------------------------------------------------------------------------------------------
    //-----------------------------------------------------Common---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name setItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage. The value us stringified to get back the same type.
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
     * @description Get the value associated to the provided key. The result have the same type as the saved value.
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

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Feedback---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name saveFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage. The value us stringified to get back the same type.
     */
    saveFeedbackUserMail(value) {
        this.setItem(FEEDBACK_USER_MAIL_KEY, value);
    }

    /**
     * @ngdoc method
     * @name getFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the value associated to the provided key. The result have the same type as the saved value.
     * @returns {string} The value associated to the provided key.
     */
    getFeedbackUserMail() {
        return this.getItem(FEEDBACK_USER_MAIL_KEY, '');
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Aggregation------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
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
        key += (datasetId ? datasetId : '') + '.';
        key += (preparationId ? preparationId : '') + '.';
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

        aggregationsToRemove.forEach((key) => { this.removeItem(key) });
    }

    /**
     * @ngdoc method
     * @name savePreparationAggregationsFromDataset
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @description Get all the saved aggregations on the dataset and save them for the preparation.
     */
    savePreparationAggregationsFromDataset(datasetId, preparationId) {
        const datasetAggregationPrefix = this.getAggregationKey(datasetId, '', '');
        const aggregationsToAdd = [];

        for (let i = 0, len = this.$window.localStorage.length; i < len; i++) {
            const key = this.$window.localStorage.key(i);
            if (key.indexOf(datasetAggregationPrefix) === 0) {
                aggregationsToAdd.push({
                    columnId: key.substring(key.lastIndexOf('.') + 1),
                    aggregation: this.getItem(key)
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

    /**
     * @ngdoc method
     * @name moveAggregations
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} oldPreparationId The new preparation id
     * @param {string} newPreparationId The old preparation id
     * @description Move all preparation aggregation to another preparation id
     */
    moveAggregations(datasetId, oldPreparationId, newPreparationId) {
        const preparationAggregationPrefix = this.getAggregationKey(datasetId, oldPreparationId, '');
        const aggregationsToMove = [];

        for (let i = 0, len = this.$window.localStorage.length; i < len; i++) {
            const key = this.$window.localStorage.key(i);
            if (key.indexOf(preparationAggregationPrefix) === 0) {
                aggregationsToMove.push({
                    columnId: key.substring(key.lastIndexOf('.') + 1),
                    aggregation: this.getItem(key)
                });
            }
        }

        aggregationsToMove.forEach((aggregDef) => {
            this.setAggregation(datasetId, newPreparationId, aggregDef.columnId, aggregDef.aggregation);
            this.removeAggregation(datasetId, oldPreparationId, aggregDef.columnId);
        });
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Lookup-----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
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

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Sort/Order-------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
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
    setDatasetsSort(sort) {
        this.setItem(DATASETS_SORT_KEY, sort);
    }

    /**
     * @ngdoc method
     * @name getDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the datasets order from localStorage
     */
    getDatasetsOrder() {
        return this.getItem(DATASETS_ORDER_KEY);
    }

    /**
     * @ngdoc method
     * @name setDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the datasets order in localStorage
     */
    setDatasetsOrder(order) {
        this.setItem(DATASETS_ORDER_KEY, order);
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
    setPreparationsSort(sort) {
        this.setItem(PREPARATIONS_SORT_KEY, sort);
    }

    /**
     * @ngdoc method
     * @name getPreparationsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the preparations order from localStorage
     */
    getPreparationsOrder() {
        return this.getItem(PREPARATIONS_ORDER_KEY);
    }

    /**
     * @ngdoc method
     * @name setPreparationsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the preparations order in localStorage
     */
    setPreparationsOrder(order) {
        this.setItem(PREPARATIONS_ORDER_KEY, order);
    }
}