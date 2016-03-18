/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.dataset-list.directive:DatasetList
 * @description This directive display the dataset list from {@link
 *     data-prep.services.dataset.service:DatasetService DatasetService}
 * @requires data-prep.dataset-list.controller:DatasetListCtrl
 * @restrict E
 */
export default function DatasetList () {
    return {
        restrict: 'E',
        templateUrl: 'app/components/dataset/list/dataset-list.html',
        bindToController: true,
        controllerAs: 'datasetListCtrl',
        controller: 'DatasetListCtrl'
    };
}