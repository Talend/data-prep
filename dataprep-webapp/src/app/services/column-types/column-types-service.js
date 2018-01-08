/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const IGNORED_TYPES = ['DOUBLE', 'NUMERIC', 'ANY'];

/**
 * @ngdoc service
 * @name data-prep.services.column-types.service:ColumnTypesService
 * @description Column types service
 */
export default class ColumnTypesService {
	constructor($q, $http, state, StateService, ColumnTypesRestService) {
		'ngInject';
		this.$q = $q;
		this.$http = $http;
		this.state = state;
		this.StateService = StateService;
		this.ColumnTypesRestService = ColumnTypesRestService;
	}

    /**
     * @ngdoc method
     * @name refreshTypes
     * @methodOf data-prep.services.column-types.service:ColumnTypesService
     * @description Get the primitive types and set them in app state
     * @returns {Promise} The GET promise
     */
	refreshTypes() {
		const types = this.state.playground.grid.primitiveTypes;
		if (types) {
			return this.$q.when(types);
		}

		return this.ColumnTypesRestService.fetchTypes()
			.then((primitiveTypes) => {
				const filteredTypes = primitiveTypes
					.filter(type => IGNORED_TYPES.indexOf(type.id) === -1);
				this.StateService.setPrimitiveTypes(filteredTypes);
				return filteredTypes;
			});
	}

	/**
	 * @ngdoc method
	 * @name refreshSemanticDomains
	 * @methodOf data-prep.services.column-types.service:ColumnTypesService
	 * @description Fetch the semantic domains of the column and set the in app state
	 * @param {string} colId the column id
	 * @returns {Promise} The GET promise
	 */
	refreshSemanticDomains(colId) {
		this.StateService.setSemanticDomains(null);
		const inventoryType = this.state.playground.preparation ?
			'preparation' :
			'dataset';
		const inventoryId = this.state.playground.preparation ?
			this.state.playground.preparation.id :
			this.state.playground.dataset.id;

		return this.ColumnTypesRestService.fetchDomains(inventoryType, inventoryId, colId)
			.then((semanticDomains) => {
				const domains = semanticDomains
					.filter(domain => domain.id)
					.sort((d1, d2) => d2.frequency - d1.frequency);
				this.StateService.setSemanticDomains(domains);
				return domains;
			});
	}
}
