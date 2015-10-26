(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name EarlyPreviewService
     * @description Launches a preview before the transformation application
     * @requires data-prep.services.recipe.service:RecipeService
     * @requires data-prep.services.playground.service:PreviewService
     */
    function EarlyPreviewService($timeout, state, RecipeService, PreviewService) {
        var previewDisabled = false;
        var previewTimeout;
        var previewCancelerTimeout;

        return {
            activatePreview: activatePreview,
            deactivatePreview: deactivatePreview,

            cancelPendingPreview: cancelPendingPreview,
            earlyPreview: earlyPreview,
            cancelEarlyPreview: cancelEarlyPreview
        };

        /**
         * @ngdoc method
         * @name deactivatePreview
         * @methodOf data-prep.services.playground.service:EarlyPreviewService
         * @description deactivates the preview
         */
        function deactivatePreview() {
            previewDisabled = true;
        }

        /**
         * @ngdoc method
         * @name activatePreview
         * @methodOf data-prep.services.playground.service:EarlyPreviewService
         * @description activates the preview
         */
        function activatePreview() {
            previewDisabled = false;
        }

        /**
         * @ngdoc method
         * @name cancelPendingPreview
         * @methodOf data-prep.services.playground.service:EarlyPreviewService
         * @description disables the pending previews
         */
        function cancelPendingPreview() {
            $timeout.cancel(previewTimeout);
            $timeout.cancel(previewCancelerTimeout);
        }

        /**
         * @ngdoc method
         * @name earlyPreview
         * @methodOf data-prep.services.playground.service:EarlyPreviewService
         * @param {object} action The transformation
         * @param {string} scope The transformation scope
         * @description Perform an early preview (preview before transformation application) after a 200ms delay
         */
        function earlyPreview(action, scope) {
            /*jshint camelcase: false */
            return function (params) {
                if (previewDisabled) {
                    return;
                }

                cancelPendingPreview();

                previewTimeout = $timeout(function () {
                    var column = state.playground.grid.selectedColumn;
                    var preparationId = state.playground.preparation ? state.playground.preparation.id : null;

                    params.scope = scope;
                    params.column_id = column.id;
                    params.column_name = column.name;

                    RecipeService.earlyPreview(column, action, params);
                    PreviewService.getPreviewAddRecords(preparationId, state.playground.dataset.id, action.name, params);
                }, 300);
            };
        }

        /**
         * @ngdoc method
         * @name cancelEarlyPreview
         * @methodOf data-prep.services.playground.service:EarlyPreviewService
         * @description Cancel any current or pending early preview
         */
        function cancelEarlyPreview() {
            if (previewDisabled) {
                return;
            }

            cancelPendingPreview();

            previewCancelerTimeout = $timeout(function () {
                RecipeService.cancelEarlyPreview();
                PreviewService.cancelPreview();
            }, 100);
        }
    }

    angular.module('data-prep.services.playground')
        .service('EarlyPreviewService', EarlyPreviewService);
})();