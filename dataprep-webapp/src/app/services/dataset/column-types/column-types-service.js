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
 * @name data-prep.services.dataset.service:ColumnTypesService
 * @description Column types service
 */
export default function ColumnTypesService($q, $http, RestURLs) {
	'ngInject';

	let types;

    /**
     * @ngdoc method
     * @name getTypes
     * @methodOf data-prep.services.dataset.service:ColumnTypesService
     * @description Return all primitive types
     * @returns {Promise} The GET promise
     */
	this.getTypes = function getTypes() {
		if (types) {
			return $q.when(types);
		}

		return $http.get(RestURLs.typesUrl).then((response) => {
			types = response.data;
			return types;
		});
	};
}
