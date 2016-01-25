(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.export.controller:ExportCtrl
     * @description Export controller.
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.utils.service:RestURLs
     * @requires data-prep.services.export.service:ExportService
     */
    function ExportCtrl($timeout, state, PreparationService, RecipeService, RestURLs, ExportService) {
        var vm = this;
        vm.state = state;
        vm.preparationService = PreparationService;
        vm.recipeService = RecipeService;
        vm.exportService = ExportService;


        /**
         * @ngdoc method
         * @name cancelCurrentParameters
         * @methodOf data-prep.export.controller:ExportCtrl
         * @description cancel current export parameters with the saved one from localStorage
         */
        vm.cancelCurrentParameters = function cancelCurrentParameters() {
            vm.exportService.currentExportType = vm.exportService.getType(vm.exportService.getParameters().id);
            vm.exportService.currentExportParameters = _.extend({}, vm.state.playground.exportParameters);
        };

        /**
         * @ngdoc method
         * @name changeTypeAndExport
         * @methodOf data-prep.export.controller:ExportCtrl
         * @param {object} exportType The export type
         * @description Change the export type and launch the export.
         * If the export type has parameters, we init the form and display a modal
         */
        vm.changeTypeAndExport = function (exportType) {
            if(!exportType.enabled) {
                // Export type is not available, no need to go further.
                return;
            }
            vm.exportService.currentExportType = exportType;
            if (vm.exportService.currentExportType.parameters) {

                if(vm.state.playground.exportParameters){
                    vm.exportService.currentExportParameters = _.extend({}, vm.state.playground.exportParameters);
                    vm.exportService.currentExportParameters.exportType = vm.exportService.currentExportType.id;
                } else {
                    updateExportParameters();
                }
                vm.showModal = true;
            } else {
                vm.export();
            }
        };

        /**
         * @ngdoc method
         * @name saveEditionAndExport
         * @methodOf data-prep.export.controller:ExportCtrl
         * @description Save the currently edited export parameters and launch the export
         */
        vm.saveEditionAndExport = function saveEditionAndExport() {
            vm.exportService.setParameters(vm.exportService.currentExportType);
            vm.export();
        };

        /**
         * @ngdoc method
         * @name export
         * @methodOf data-prep.export.controller:ExportCtrl
         * @description Launch the export (with new name if rename)
         */
        vm.export = function () {
            if(!vm.exportService.currentExportParameters){
                updateExportParameters();
            }

            vm.state.playground.exportParameters = _.extend({}, vm.exportService.currentExportParameters);

            $timeout(function(){
                vm.form.action = RestURLs.exportUrl;
                vm.form.submit();
            });

        };

        /**
         * @ngdoc method
         * @name updateExportParameters
         * @methodOf data-prep.export.controller:ExportCtrl
         * @description Update export parameters
         */
        function updateExportParameters() {
            vm.exportService.currentExportParameters = {exportType: vm.exportService.currentExportType.id};
            _.forEach(vm.exportService.currentExportType.parameters, function(param) {
                vm.exportService.currentExportParameters['exportParameters.' + param.name] = param.defaultValue.value;
                //hack for fileName as we don't know it with exportTypes parameters
                //so if empty use the dataset.name
                if (param.name === 'fileName'){
                    if (vm.state.playground.preparation && vm.state.playground.preparation.name) {
                        vm.exportService.currentExportParameters['exportParameters.' + param.name] = vm.state.playground.preparation.name;
                    } else {
                        vm.exportService.currentExportParameters['exportParameters.' + param.name] = vm.state.playground.dataset.name;
                    }
                }
            });
        }


        // get types list
        vm.exportService.refreshTypes()
            .then(vm.exportService.resetCurrentParameters);
    }

    /**
     * @ngdoc property
     * @name stepId
     * @propertyOf data-prep.export.controller:ExportCtrl
     * @description The current stepId
     * It is bound to {@link data-prep.services.recipe.service:RecipeService RecipeService}.getLastActiveStep()
     */
    Object.defineProperty(ExportCtrl.prototype,
        'stepId', {
            enumerable: true,
            configurable: false,
            get: function () {
                var step = this.recipeService.getLastActiveStep();
                return step ? step.transformation.stepId : '';
            }
        });

    /**
     * @ngdoc property
     * @name datasetId
     * @propertyOf data-prep.export.controller:ExportCtrl
     * @description The current dataset id
     * It is bound to {@link data-prep.services.state.constant:state state} property
     */
    Object.defineProperty(ExportCtrl.prototype,
        'datasetId', {
            enumerable: true,
            configurable: false,
            get: function () {
                var dataset = this.state.playground.dataset;
                return dataset ? dataset.id : '';
            }
        });

    /**
     * @ngdoc property
     * @name exportTypes
     * @propertyOf data-prep.export.controller:ExportCtrl
     * @description The export types list
     * It is bound to {@link data-prep.services.export.service:ExportService ExportService} property
     */
    Object.defineProperty(ExportCtrl.prototype,
        'exportTypes', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.exportService.exportTypes;
            }
        });

    angular.module('data-prep.export')
        .controller('ExportCtrl', ExportCtrl);
})();