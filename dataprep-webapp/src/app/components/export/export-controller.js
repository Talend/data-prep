/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

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
    constructor($timeout, state, RestURLs, StepUtilsService, ExportService, StorageService) {
        'ngInject';

        this.$timeout = $timeout;
        this.state = state;
        this.RestURLs = RestURLs;
        this.ExportService = ExportService;
        this.StorageService = StorageService;
        this.StepUtilsService = StepUtilsService;

        this.exportParams = StorageService.getExportParams();
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
        this._initExportParameters(type);
        this.nextSelectedType = type;
        this.showModal = true;
    }

    /**
     * @ngdoc method
     * @name saveType
     * @methodOf data-prep.export.controller:ExportCtrl
     * @description Init parameters and display the params modal
     */
    saveAndExport() {
        this.selectedType = this.nextSelectedType;
        const params = this._extractParameters(this.selectedType);
        this.StorageService.saveExportParams(params);
        this.launchExport();
    }

    /**
     * @ngdoc method
     * @name launchExport
     * @methodOf data-prep.export.controller:ExportCtrl
     * @param {boolean} shouldResetName If reset name is needed
     * @description Save the current parameters and launch an export
     */
    launchExport(shouldResetName) {
        if (shouldResetName) {
            this._initExportParameters(this.selectedType);
        }
        this.exportParams = this._extractParameters(this.selectedType);

        this.$timeout(() => {
            this.form.action = this.RestURLs.exportUrl;
            this.form.submit();
        }, 0, false);
    }

    /**
     * @ngdoc method
     * @name _initExportParameters
     * @methodOf data-prep.export.controller:ExportCtrl
     * @param {object} exportType The type to init
     * @description Change the fileName of the type parameters to fit the current prep/dataset
     */
    _initExportParameters(exportType) {
        if (!exportType) {
            return;
        }
        _.chain(exportType.parameters)
            .filter({ name: 'fileName' })
            .forEach((param) => {
                param.value = this.state.playground.preparation ?
                    this.state.playground.preparation.name :
                    this.state.playground.dataset.name;
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
        _.forEach(exportType.parameters, (param) => {
            parameters['exportParameters.' + param.name] = param.value ? param.value : param.default;
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
Object.defineProperty(ExportCtrl.prototype,
    'stepId', {
        enumerable: true,
        configurable: false,
        get: function () {
            const step = this.StepUtilsService.getLastActiveStep(this.state.playground.recipe);
            return step ? step.transformation.stepId : '';
        }
    });
