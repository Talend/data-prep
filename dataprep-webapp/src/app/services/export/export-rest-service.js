/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.export.service:ExportRestService
 * @description Export service. This service provide the entry point to the backend export REST api.<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.export.service:ExportService ExportService} must be the only entry point for Export</b>
 */
export default class ExportRestService {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.url = RestURLs.exportUrl;
	}

    /**
     * @ngdoc method
     * @name exportTypes
     * @methodOf data-prep.services.export.service:ExportRestService
     * @description Fetch the available export types
     * @returns {Promise}  The GET call promise
     * @param {string} entityType preparations | datasets
     * @param {string} entityId The entity id
     */
	exportTypes(entityType, entityId) {
		return this.$http
			.get(`${this.url}/formats/${entityType}/${entityId}`)
			.then(resp => resp.data);
	}
}
