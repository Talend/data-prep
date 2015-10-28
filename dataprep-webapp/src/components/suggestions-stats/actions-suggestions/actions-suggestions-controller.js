(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
     * @description Actions suggestion controller
     * @requires data-prep.services.transformation.service:SuggestionService
     * @requires data-prep.services.transformation.service:ColumnSuggestionService
     * @requires data-prep.services.transformation.service:TransformationService
     * @requires data-prep.services.transformation.service:TransformationApplicationService
     * @requires data-prep.services.playground.service:EarlyPreviewService
     */
    function ActionsSuggestionsCtrl(state, SuggestionService, ColumnSuggestionService, TransformationService,
                                    TransformationApplicationService, EarlyPreviewService) {

        var vm = this;
        vm.columnSuggestionService = ColumnSuggestionService;
        vm.suggestionService = SuggestionService;

        vm.earlyPreview = EarlyPreviewService.earlyPreview;
        vm.cancelEarlyPreview = EarlyPreviewService.cancelEarlyPreview;

        /**
         * @ngdoc property
         * @name dynamicTransformation
         * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description The dynamic param transformation to display
         */
        vm.dynamicTransformation = null;

        /**
         * @ngdoc property
         * @name dynamicFetchInProgress
         * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Flag that indicates if a fetch of dynamic parameters is in progress
         */
        vm.dynamicFetchInProgress = false;

        /**
         * @ngdoc property
         * @name showDynamicModal
         * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Flag that change the dynamic parameters modal display
         */
        vm.showDynamicModal = false;

        /**
         * @ngdoc property
         * @name showModalContent
         * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description show/hides the dynamic transformation or the alert message
         */
        vm.showModalContent = null;


        /**
         * @ngdoc property
         * @name searchActionString
         * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Actions to search
         */
        vm.searchActionString = '';


        /**
         * @ngdoc method
         * @name initDynamicParams
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description [PRIVATE] Fetch the transformation dynamic parameters and inject them into transformation menu params
         * @returns {promise} The GET request promise
         */
        var initDynamicParams = function(transfo) {
            var infos = {
                columnId: vm.column.id,
                datasetId:  state.playground.dataset.id,
                preparationId:  state.playground.preparation ? state.playground.preparation.id : null
            };
            return TransformationService.initDynamicParameters(transfo, infos);
        };

        /**
         * @ngdoc method
         * @name checkDynamicResponse
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description [PRIVATE] sets the showHideModalContent and the emptyParamsMsg properties
         */
        vm.checkDynamicResponse = function showModalContent (){
            //transformation type :cluster
            if(vm.dynamicTransformation.cluster){
                if(vm.dynamicTransformation.cluster.clusters.length){
                    vm.showModalContent = true;
                }
                else{
                    vm.showModalContent = false;
                    vm.emptyParamsMsg = 'NO_CLUSTERS_ACTION_MSG';
                }
            }

            //transformation type :simpleParams
            else if(vm.dynamicTransformation.parameters){
                if(vm.dynamicTransformation.parameters.length){
                    vm.showModalContent = true;
                }
                else{
                    vm.showModalContent = false;
                    vm.emptyParamsMsg = 'NO_CHOICES_ACTION_MSG';
                }
            }

            //transformation type :choice
            else if(vm.dynamicTransformation.items){
                if(vm.dynamicTransformation.items.length){
                    vm.showModalContent = true;
                }
                else{
                    vm.showModalContent = false;
                    vm.emptyParamsMsg = 'NO_PARAMS_ACTION_MSG';
                }
            }
        };

        /**
         * @ngdoc method
         * @name select
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Transformation selection.
         <ul>
            <li>Dynamic transformation : fetch the dynamic parameters and show the modal</li>
            <li>Static transformation : append the new step in the current preparation</li>
         </ul>
         */
        vm.select = function select(transfo, transfoScope) {
            if(transfo.dynamic) {
                vm.dynamicTransformation = transfo;
                vm.dynamicScope = transfoScope;
                vm.dynamicFetchInProgress = true;
                vm.showDynamicModal = true;

                //get new parameters
                initDynamicParams(transfo).finally(function() {
                    vm.checkDynamicResponse();
                    vm.dynamicFetchInProgress = false;
                });
            }
            else {
                vm.transform(transfo, transfoScope)();
            }
        };

        /**
         * @ngdoc method
         * @name transform
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Apply a transformation
         */
        vm.transform = function transform(action, scope) {
            return function(params) {
                EarlyPreviewService.deactivatePreview();
                EarlyPreviewService.cancelPendingPreview();

                TransformationApplicationService.append(action, scope, params)
                    .then(function() {
                        vm.showDynamicModal = false;
                    })
                    .finally(function() {
                        setTimeout(EarlyPreviewService.activatePreview, 500);
                    });
            };
        };
    }

    /**
     * @ngdoc property
     * @name suggestions
     * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
     * @description The suggested transformations list.
     * This is bound to {@link data-prep.services.transformation:ColumnSuggestionService ColumnSuggestionService}.transformations
     */
    Object.defineProperty(ActionsSuggestionsCtrl.prototype,
        'columnSuggestions', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.columnSuggestionService.transformations;
            }
        });

    /**
     * @ngdoc property
     * @name column
     * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
     * @description The transformations column.
     * This is bound to {@link data-prep.services.transformation:SuggestionService SuggestionService}.currentColumn
     */
    Object.defineProperty(ActionsSuggestionsCtrl.prototype,
        'column', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.suggestionService.currentColumn;
            }
        });

    /**
     * @ngdoc property
     * @name tab
     * @propertyOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
     * @description The new selected action tab
     * This is bound to {@link data-prep.services.transformation:SuggestionService SuggestionService}.tab
     */
    Object.defineProperty(ActionsSuggestionsCtrl.prototype,
        'tab', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.suggestionService.tab;
            }
        });

    angular.module('data-prep.actions-suggestions')
        .controller('ActionsSuggestionsCtrl', ActionsSuggestionsCtrl);
})();