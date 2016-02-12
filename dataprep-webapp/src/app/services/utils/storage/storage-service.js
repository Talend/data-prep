/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:StorageService
 * @description Local storage service
 */
export default function StorageService($window) {
    'ngInject';

    var PREFIX = 'org.talend.dataprep.';
    var LOOKUP_DATASETS_KEY = 'org.talend.dataprep.lookup_datasets';
    var LOOKUP_DATASETS_SORT_KEY = 'org.talend.dataprep.lookup_datasets_sort';
    var LOOKUP_DATASETS_ORDER_KEY = 'org.talend.dataprep.lookup_datasets_order';
    var DATASETS_SORT_KEY = 'org.talend.dataprep.datasets.sort';
    var DATASETS_ORDER_KEY = 'org.talend.dataprep.datasets.order';
    var FEEDBACK_USER_MAIL_KEY = 'org.talend.dataprep.feedback_user_mail';

    return {
        setAggregation: setAggregation,
        getAggregation: getAggregation,
        removeAggregation: removeAggregation,
        removeAllAggregations: removeAllAggregations,
        savePreparationAggregationsFromDataset: savePreparationAggregationsFromDataset,
        moveAggregations: moveAggregations,

        getFeedbackUserMail: getFeedbackUserMail,
        saveFeedbackUserMail: saveFeedbackUserMail,

        getLookupDatasets: getLookupDatasets,
        setLookupDatasets: setLookupDatasets,

        getDatasetsOrder: getDatasetsOrder,
        setDatasetsOrder: setDatasetsOrder,
        getDatasetsSort: getDatasetsSort,
        setDatasetsSort: setDatasetsSort,

        getLookupDatasetsSort: getLookupDatasetsSort,
        setLookupDatasetsSort: setLookupDatasetsSort,
        getLookupDatasetsOrder: getLookupDatasetsOrder,
        setLookupDatasetsOrder: setLookupDatasetsOrder
    };

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
    function setItem(key, value) {
        $window.localStorage.setItem(key, JSON.stringify(value));
    }

    /**
     * @ngdoc method
     * @name getItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @description Get the value associated to the provided key. The result have the same type as the saved value.
     * @returns The value associated to the provided key.
     */
    function getItem(key) {
        return JSON.parse($window.localStorage.getItem(key));
    }

    /**
     * @ngdoc method
     * @name removeItem
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @description Remove the entry associated to the provided key.
     */
    function removeItem(key) {
        $window.localStorage.removeItem(key);
    }


    /**
     * @ngdoc method
     * @name saveFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {any} value The value to save
     * @description Save the value with the provided key in localStorage. The value us stringified to get back the same type.
     */
    function saveFeedbackUserMail(value) {
        $window.localStorage.setItem(FEEDBACK_USER_MAIL_KEY, JSON.stringify(value));
    }

    /**
     * @ngdoc method
     * @name getFeedbackUserMail
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} key The localStorage key
     * @description Get the value associated to the provided key. The result have the same type as the saved value.
     * @returns The value associated to the provided key.
     */
    function getFeedbackUserMail() {
        var mail = $window.localStorage.getItem(FEEDBACK_USER_MAIL_KEY);
        return mail ? JSON.parse(mail) : '';
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
    function getAggregationKey(datasetId, preparationId, columnId) {
        var key = PREFIX + 'aggregation.';
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
    function setAggregation(datasetId, preparationId, columnId, aggregation) {
        var key = getAggregationKey(datasetId, preparationId, columnId);
        setItem(key, aggregation);
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
    function getAggregation(datasetId, preparationId, columnId) {
        var key = getAggregationKey(datasetId, preparationId, columnId);
        return getItem(key);
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
    function removeAggregation(datasetId, preparationId, columnId) {
        var key = getAggregationKey(datasetId, preparationId, columnId);
        removeItem(key);
    }

    /**
     * @ngdoc method
     * @name removeAllAggregations
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @description Remove all aggregations on the dataset/preparation.
     */
    function removeAllAggregations(datasetId, preparationId) {
        var keyAggregationPrefix = getAggregationKey(datasetId, preparationId, '');
        var aggregationsToRemove = [];

        for (var i = 0, len = $window.localStorage.length; i < len; i++) {
            var key = $window.localStorage.key(i);
            if (key.indexOf(keyAggregationPrefix) === 0) {
                aggregationsToRemove.push(key);
            }
        }

        _.forEach(aggregationsToRemove, function (key) {
            removeItem(key);
        });
    }

    /**
     * @ngdoc method
     * @name savePreparationAggregationsFromDataset
     * @methodOf data-prep.services.utils.service:StorageService
     * @param {string} datasetId The dataset id
     * @param {string} preparationId The preparation id
     * @description Get all the saved aggregations on the dataset and save them for the preparation.
     */
    function savePreparationAggregationsFromDataset(datasetId, preparationId) {
        var datasetAggregationPrefix = getAggregationKey(datasetId, '', '');
        var aggregationsToAdd = [];

        for (var i = 0, len = $window.localStorage.length; i < len; i++) {
            var key = $window.localStorage.key(i);
            if (key.indexOf(datasetAggregationPrefix) === 0) {
                aggregationsToAdd.push({
                    columnId: key.substring(key.lastIndexOf('.') + 1),
                    aggregation: getItem(key)
                });
            }
        }

        _.forEach(aggregationsToAdd, function (aggregDef) {
            setAggregation(datasetId, preparationId, aggregDef.columnId, aggregDef.aggregation);
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
    function moveAggregations(datasetId, oldPreparationId, newPreparationId) {
        var preparationAggregationPrefix = getAggregationKey(datasetId, oldPreparationId, '');
        var aggregationsToMove = [];

        for (var i = 0, len = $window.localStorage.length; i < len; i++) {
            var key = $window.localStorage.key(i);
            if (key.indexOf(preparationAggregationPrefix) === 0) {
                aggregationsToMove.push({
                    columnId: key.substring(key.lastIndexOf('.') + 1),
                    aggregation: getItem(key)
                });
            }
        }

        _.forEach(aggregationsToMove, function (aggregDef) {
            setAggregation(datasetId, newPreparationId, aggregDef.columnId, aggregDef.aggregation);
            removeAggregation(datasetId, oldPreparationId, aggregDef.columnId);
        });
    }

    /**
     * @ngdoc method
     * @name getLookupDatasets
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the lookup datasets from localStorage
     */
    function getLookupDatasets() {
        var params = $window.localStorage.getItem(LOOKUP_DATASETS_KEY);
        return params ? JSON.parse(params) : [];
    }

    /**
     * @ngdoc method
     * @name setLookupDatasets
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the lookup datasets in localStorage
     */
    function setLookupDatasets(datasets) {
        $window.localStorage.setItem(LOOKUP_DATASETS_KEY, JSON.stringify(datasets));
    }

    /**
     * @ngdoc method
     * @name getDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the datasets sort from localStorage
     */
    function getDatasetsSort() {
        return $window.localStorage.getItem(DATASETS_SORT_KEY);
    }

    /**
     * @ngdoc method
     * @name setDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the datasets sort in localStorage
     */
    function setDatasetsSort(sort) {
        $window.localStorage.setItem(DATASETS_SORT_KEY, sort);
    }

    /**
     * @ngdoc method
     * @name getDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the datasets order from localStorage
     */
    function getDatasetsOrder() {
        return $window.localStorage.getItem(DATASETS_ORDER_KEY);
    }

    /**
     * @ngdoc method
     * @name setDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the datasets order in localStorage
     */
    function setDatasetsOrder(order) {
        $window.localStorage.setItem(DATASETS_ORDER_KEY, order);
    }

    /**
     * @ngdoc method
     * @name getLookupDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the Lookup datasets sort from localStorage
     */
    function getLookupDatasetsSort() {
        return $window.localStorage.getItem(LOOKUP_DATASETS_SORT_KEY);
    }

    /**
     * @ngdoc method
     * @name setLookupDatasetsSort
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the Lookup datasets sort in localStorage
     */
    function setLookupDatasetsSort(sort) {
        $window.localStorage.setItem(LOOKUP_DATASETS_SORT_KEY, sort);
    }

    /**
     * @ngdoc method
     * @name getLookupDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Get the Lookup datasets order from localStorage
     */
    function getLookupDatasetsOrder() {
        return $window.localStorage.getItem(LOOKUP_DATASETS_ORDER_KEY);
    }

    /**
     * @ngdoc method
     * @name setLookupDatasetsOrder
     * @methodOf data-prep.services.utils.service:StorageService
     * @description Save the Lookup datasets order in localStorage
     */
    function setLookupDatasetsOrder(order) {
        $window.localStorage.setItem(LOOKUP_DATASETS_ORDER_KEY, order);
    }
}