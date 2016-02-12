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
 * @name data-prep.services.transformation.service:TransformationRestService
 * @description Transformation REST service. This service provide the entry point to transformation REST api
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.transformation.service:TransformationService TransformationService} must be the only entry point for transformation</b>
 */
export default function TransformationRestService($http, RestURLs) {
    'ngInject';

    return {
        getDatasetTransformations: getDatasetTransformations,
        getDynamicParameters: getDynamicParameters,
        getColumnSuggestions: getColumnSuggestions,
        getColumnTransformations: getColumnTransformations,
        getLineTransformations: getLineTransformations
    };

    /**
     * @ngdoc method
     * @name getLookupActions
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @description Get the dataset actions
     * @param {string} datasetId The dataset id
     * @returns {Promise} The GET promise
     */
    function getDatasetTransformations(datasetId) {
        return $http.get(RestURLs.datasetUrl + '/' + datasetId + '/actions');
    }

    /**
     * @ngdoc method
     * @name getColumnTransformations
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @param {object} column The column metadata
     * @description Fetch the transformations on a column
     * @returns {Promise} The POST promise
     */
    function getColumnTransformations(column) {
        return $http.post(RestURLs.transformUrl + '/actions/column', column);
    }

    /**
     * @ngdoc method
     * @name getLineTransformations
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @description Fetch the transformations on a line
     * @returns {Promise} The GET promise
     */
    function getLineTransformations() {
        return $http.get(RestURLs.transformUrl + '/actions/line');
    }

    /**
     * @ngdoc method
     * @name getSuggestions
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @param {string} column The column metadata
     * @description Fetch the suggestions on a column
     * @returns {Promise} The POST promise
     */
    function getColumnSuggestions(column) {
        return $http.post(RestURLs.transformUrl + '/suggest/column', column);
    }


    /**
     * @ngdoc method
     * @name getDynamicParameters
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @param {string} action The action name
     * @param {string} columnId The column Id
     * @param {string} datasetId The datasetId
     * @param {string} preparationId The preparation Id
     * @param {string} stepId The step Id
     * @description Fetch the transformations dynamic params
     * @returns {Promise} The GET promise
     */
    function getDynamicParameters(action, columnId, datasetId, preparationId, stepId) {
        var queryParams = preparationId ? '?preparationId=' + encodeURIComponent(preparationId) : '?datasetId=' + encodeURIComponent(datasetId);
        queryParams += stepId ? '&stepId=' + encodeURIComponent(stepId) : '';
        queryParams += '&columnId=' + encodeURIComponent(columnId);

        return $http.get(RestURLs.transformUrl + '/suggest/' + action + '/params' + queryParams);
    }
}