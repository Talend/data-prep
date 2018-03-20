/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './dataset-xls-preview.html';

/**
 * @ngdoc directive
 * @name data-prep.dataset-preview.directive:Preview
 * @description This directive display the sheet choice and previews on sheet select
 * @requires data-prep.dataset-preview.controller:DatasetXlsPreviewCtrl
 * @restrict E
 */
export default function DatasetXlsPreview($timeout) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'previewCtrl',
		controller: 'DatasetXlsPreviewCtrl',
		link: (scope, iElement, iAttrs, ctrl) => {
			$timeout(() => {
				ctrl.initGrid();
			}, 100);
		},
	};
}
