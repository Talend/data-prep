/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class ColumnTypesRest {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.RestURLs = RestURLs;
	}

	/**
	 * @ngdoc method
	 * @name fetchTypes
	 * @methodOf data-prep.services.column-types.service:ColumnTypesRestService
	 * @description Fetch the columns static types
	 * @returns {Promise} The GET promise
	 */
	fetchTypes() {
		return this.$http.get(this.RestURLs.typesUrl).then(resp => resp.data);
	}

	/**
	 * @ngdoc method
	 * @name fetchDomains
	 * @methodOf data-prep.services.column-types.service:ColumnTypesRestService
	 * @description Fetch the columns static types
	 * @param {string} inventoryType the item type (dataset | preparation)
	 * @param {string} inventoryId the item id
	 * @param {string} colId the column id
	 * @returns {Promise} The GET promise
	 */
	fetchDomains(inventoryType, inventoryId, colId) {
		const baseUrl = inventoryType === 'dataset' ?
			this.RestURLs.datasetUrl :
			this.RestURLs.preparationUrl;
		const url = `${baseUrl}/${inventoryId}/columns/${colId}/types`;
		return this.$http.get(url).then(resp => resp.data);
	}
}
