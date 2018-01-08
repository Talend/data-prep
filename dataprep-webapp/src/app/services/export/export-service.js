/*
 * ============================================================================
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

/**
 * @ngdoc service
 * @name data-prep.services.export.service:ExportService
 * @description Export service. This service provide the entry point to the backend export REST api.
 * @requires data-prep.services.export.service:ExportRestService
 * @requires data-prep.services.parameters.service:ParametersService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class ExportService {
	constructor(state, StateService, ExportRestService, ParametersService, StorageService) {
		'ngInject';

		this.StorageService = StorageService;
		this.StateService = StateService;
		this.ExportRestService = ExportRestService;
		this.ParametersService = ParametersService;
		this.state = state;
	}

    /**
     * @ngdoc method
     * @name getType
     * @methodOf data-prep.services.export.service:ExportService
     * @param {String} id the export type id
     * @description Get the type by id
     */
	getType(id) {
		return _.find(this.state.export.exportTypes, { id });
	}

    /**
     * @ngdoc method
     * @name setExportParams
     * @methodOf data-prep.services.export.service:ExportService
     * @param {Object} params The export params
     * @description Set the export parameters in app state and storage
     */
	setExportParams(params) {
		this.StorageService.saveExportParams(params);
		this.StateService.setDefaultExportType(params);
	}

	/**
	 * @ngdoc method
	 * @name refreshTypes
	 * @methodOf data-prep.services.export.service:ExportService
	 * @description Refresh the export types available for the entity
	 * It saves default if no parameters has been saved yet
	 * @param {string} entityType preparations | datasets
	 * @param {string} entityId The entity id
	 */
	refreshTypes(entityType, entityId) {
		this.StateService.resetExportTypes();
		return this.ExportRestService.exportTypes(entityType, entityId)
			.then((exportTypes) => {
				let defaultExportParams = this.StorageService.getExportParams();

				if (!defaultExportParams) {
					const exportType = exportTypes.find(type => type.defaultExport === true);
					defaultExportParams = { exportType: exportType.id };
					this.StorageService.saveExportParams(defaultExportParams);
				}

				this.StateService.setExportTypes(exportTypes);
				this.StateService.setDefaultExportType(defaultExportParams);
			});
	}
}
