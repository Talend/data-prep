/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { chain } from 'lodash';

/**
 * @ngdoc controller
 * @name data-prep.export.controller:ExportCtrl
 * @description Export controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.utils.service:RestURLs
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.export.service:ExportService
 * @requires data-prep.services.utils.service:StorageService
 */
export default class ExportCtrl {
	constructor(
		$timeout,
		state,
		RestURLs,
		StepUtilsService,
		ExportService,
		StorageService
	) {
		'ngInject';

		this.$timeout = $timeout;
		this.state = state;
		this.RestURLs = RestURLs;
		this.ExportService = ExportService;
		this.StorageService = StorageService;
		this.StepUtilsService = StepUtilsService;

		this.exportParams = this.state.export.defaultExportType;
		this.selectedType = ExportService.getType(this.exportParams.exportType);
	}

	/**
     * @ngdoc method
     * @name selectType
     * @methodOf data-prep.export.controller:ExportCtrl
     * @param {object} type The selected type
     * @description Init parameters and display the params modal
     */
	selectType(type) {
		if (type.enabled) {
			this._initExportParameters(type);
			this.selectedType = type;
			this.showModal = true;
		}
	}

	/**
     * @ngdoc method
     * @name saveType
     * @methodOf data-prep.export.controller:ExportCtrl
     * @description Init parameters and display the params modal
     */
	saveAndExport() {
		const params = this._extractParameters(this.selectedType);
		this.ExportService.setExportParams(params);
		this.launchExport();
	}

	/**
     * @ngdoc method
     * @name launchDefaultExport
     * @methodOf data-prep.export.controller:ExportCtrl
     * @description launches default export without export type selection
     */
	launchDefaultExport() {
		this._initExportParameters(this.selectedType);
		this.launchExport();
	}

	/**
     * @ngdoc method
     * @name launchExport
     * @methodOf data-prep.export.controller:ExportCtrl
     * @description Save the current parameters and launch an export
     */
	launchExport() {
		this.exportParams = this._extractParameters(this.selectedType);

		this.$timeout(
			() => {
				this.form.action = this.RestURLs.exportUrl;
				this.form.submit();
			},
			0,
			false
		);
	}

	/**
     * @ngdoc method
     * @name _initExportParameters
     * @methodOf data-prep.export.controller:ExportCtrl
     * @param {object} exportType The type to init
     * @description Change the fileName of the type parameters to fit the current prep/dataset
     */
	_initExportParameters(exportType) {
		chain(exportType.parameters)
			.filter({ name: 'fileName' })
			.forEach((param) => {
				const state = this.state.playground;
				param.value = state.preparation
					? state.preparation.name
					: state.dataset.name;
			})
			.value();
	}

	/**
     * @ngdoc method
     * @name _extractParameters
     * @methodOf data-prep.export.controller:ExportCtrl
     * @param {object} exportType The type to extract
     * @description Extract the parameters of the selected type
     */
	_extractParameters(exportType) {
		const parameters = { exportType: exportType.id };
		exportType.parameters.forEach((param) => {
			parameters['exportParameters.' + param.name] =
				typeof param.value !== 'undefined'
					? param.value
					: param.default;
		});

		return parameters;
	}
}

/**
 * @ngdoc property
 * @name stepId
 * @propertyOf data-prep.export.controller:ExportCtrl
 * @description The current stepId
 */
Object.defineProperty(ExportCtrl.prototype, 'stepId', {
	enumerable: true,
	configurable: false,
	get() {
		const step = this.StepUtilsService.getLastActiveStep(
			this.state.playground.recipe
		);
		return step ? step.transformation.stepId : '';
	},
});
