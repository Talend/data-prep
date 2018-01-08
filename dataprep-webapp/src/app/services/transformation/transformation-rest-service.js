/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const LINE = 'line';
const COLUMN = 'column';
const DATASET = 'dataset';

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationRestService
 * @description Transformation REST service. This service provide the entry point to transformation REST api
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.transformation.service:TransformationService TransformationService} must be the only entry point for transformation</b>
 */
export default class TransformationRestService {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.RestURLs = RestURLs;
	}

    /**
     * @ngdoc method
     * @name getLookupActions
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @description Get the dataset actions
     * @param {string} datasetId The dataset id
     * @returns {Promise} The GET promise
     */
	getDatasetTransformations(datasetId) {
		return this.$http.get(`${this.RestURLs.datasetUrl}/${datasetId}/actions`);
	}

    /**
     * @ngdoc method
     * @name getTransformations
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @param {string} scope The transformations scope
     * @param {object} entity The transformations target entity
     * @description Fetch the transformations
     * @returns {Promise} The promise
     */
	getTransformations(scope, entity) {
		switch (scope) {
		case LINE:
		case DATASET:
			return this.$http.get(`${this.RestURLs.transformUrl}/actions/${scope}`)
                .then(response => response.data);
		case COLUMN:
			return this.$http.post(`${this.RestURLs.transformUrl}/actions/${scope}`, entity)
                .then(response => response.data);
		}
	}

    /**
     * @ngdoc method
     * @name getSuggestions
     * @methodOf data-prep.services.transformation.service:TransformationRestService
     * @param {string} scope The suggestions scope
     * @param {object} entity The suggestions target entity
     * @description Fetch the suggestions on a column
     * @returns {Promise} The promise
     */
	getSuggestions(scope, entity) {
		return this.$http.post(`${this.RestURLs.transformUrl}/suggest/${scope}`, entity)
            .then(response => response.data);
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
	getDynamicParameters(action, columnId, datasetId, preparationId, stepId) {
		let queryParams = preparationId ? 'preparationId=' + encodeURIComponent(preparationId) : 'datasetId=' + encodeURIComponent(datasetId);
		queryParams += stepId ? '&stepId=' + encodeURIComponent(stepId) : '';
		queryParams += '&columnId=' + encodeURIComponent(columnId);

		return this.$http
            .get(`${this.RestURLs.transformUrl}/suggest/${action}/params?${queryParams}`)
            .then(response => response.data);
	}
}
