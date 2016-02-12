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
 * @name data-prep.dataset-preview.directive:Preview
 * @description This directive display the sheet choice and previews on sheet select
 * @requires data-prep.dataset-preview.controller:DatasetXlsPreviewCtrl
 * @restrict E
 */
export default function DatasetXlsPreview() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/dataset/preview/dataset-xls-preview.html',
        bindToController: true,
        controllerAs: 'previewCtrl',
        controller: 'DatasetXlsPreviewCtrl'
    };
}