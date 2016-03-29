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
 * @name EarlyPreviewService
 * @description Launches a preview before the transformation application
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.playground.service:PreviewService
 */
export default function EarlyPreviewService($timeout, state, RecipeService, PreviewService) {
    'ngInject';

    // early preview delay is 1 second
    const DELAY = 700;

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
        return function (params) {
            if (previewDisabled) {
                return;
            }

            cancelPendingPreview();

            previewTimeout = $timeout(function () {
                var line = state.playground.grid.selectedLine;
                var column = state.playground.grid.selectedColumn;
                var preparationId = state.playground.preparation ? state.playground.preparation.id : null;

                params.scope = scope;
                params.column_id = column && column.id;
                params.column_name = column && column.name;
                params.row_id = line && line.tdpId;

                RecipeService.earlyPreview(action, params);
                PreviewService.getPreviewAddRecords(preparationId, state.playground.dataset.id, action.name, params);
            }, DELAY);
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