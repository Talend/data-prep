/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './dataset-upload-list.html';

/**
 * @ngdoc directive
 * @name data-prep.dataset-upload-list.directive:DatasetUploadList
 * @description This dataset display the upload list that had an upload error. Unlike the dataset list which is
 * based on a service property for the dataset list, this one is based on a given attribute value.
 * @restrict E
 * @usage <dataset-upload-list datasets="datasets"></dataset-upload-list>
 * @param {object[]} datasets The upload error datasets
 */
export default function DatasetUploadList() {
	return {
		templateUrl: template,
		restrict: 'E',
		scope: {
			datasets: '=',
		},
		bindToController: true,
		controllerAs: 'uploadListCtrl',
		controller: () => {
		},
	};
}
