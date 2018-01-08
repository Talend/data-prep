/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { map } from 'lodash';

// actions scopes
const COLUMN = 'column';
const LINE = 'line';
const DATASET = 'dataset';

// early preview delay
const DELAY = 700;

/**
 * @ngdoc service
 * @name EarlyPreviewService
 * @description Launches a preview before the transformation application
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.playground.service:PreviewService
 */
export default function EarlyPreviewService($timeout, state, RecipeService, PreviewService) {
	'ngInject';
	let previewTimeout;
	let previewCancelerTimeout;

	return {
		cancelPendingPreview,
		earlyPreview,
		cancelEarlyPreview,
	};

    /**
     * @ngdoc method
     * @name cancelPendingPreview
     * @methodOf data-prep.services.early-preview.service:EarlyPreviewService
     * @description disables the pending previews
     */
	function cancelPendingPreview() {
		$timeout.cancel(previewTimeout);
		$timeout.cancel(previewCancelerTimeout);
	}

    /**
     * @ngdoc method
     * @name earlyPreview
     * @methodOf data-prep.services.early-preview.service:EarlyPreviewService
     * @param {object} action The transformation
     * @param {string} scope The transformation scope
     * @description Perform an early preview (preview before transformation application) after a 200ms delay
     */
	function earlyPreview(action, scope) {
		return (params) => {
			if (state.playground.previewDisabled) {
				return;
			}

			cancelPendingPreview();

			previewTimeout = $timeout(() => {
				const line = state.playground.grid.selectedLine;
				const columns = state.playground.grid.selectedColumns;
				const preparationId = state.playground.preparation ? state.playground.preparation.id : null;

				let parameters;
				switch (scope) {
				case DATASET :
					parameters = [
						{
							...params,
							scope,
						},
					];
					break;
				case COLUMN :
					parameters = map(columns, col => ({
						...params,
						scope,
						column_id: col.id,
						column_name: col.name,
					}));
					break;
				case LINE :
					parameters = [
						{
							...params,
							scope,
							row_id: line.tdpId,
						},
					];
					break;
				default:
					parameters = [];
					break;
				}

				PreviewService.getPreviewAddRecords(preparationId, state.playground.dataset.id, action.name, parameters)
                    .then(() => RecipeService.earlyPreview(action, parameters));
			}, DELAY);
		};
	}

    /**
     * @ngdoc method
     * @name cancelEarlyPreview
     * @methodOf data-prep.services.early-preview.service:EarlyPreviewService
     * @description Cancel any current or pending early preview
     */
	function cancelEarlyPreview() {
		if (state.playground.previewDisabled) {
			return;
		}

		cancelPendingPreview();

		previewCancelerTimeout = $timeout(() => {
			RecipeService.cancelEarlyPreview();
			PreviewService.cancelPreview();
		}, 100);
	}
}
