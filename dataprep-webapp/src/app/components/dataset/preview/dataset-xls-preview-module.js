/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import DatasetXlsPreviewCtrl from './dataset-xls-preview-controller';
import DatasetXlsPreview from './dataset-xls-preview-directive';

(function () {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.dataset-xls-preview
     * @description This module contains the controller and directives to manage the dataset xls preview
     * @requires talend.widget
     * @requires data-prep.services.dataset
     * @requires data-prep.services.playground
     * @requires data-prep.services.utils
     * @requires data-prep.services.state
     */
    angular.module('data-prep.dataset-xls-preview',
        [
            'ui.router',
            'pascalprecht.translate',
            'talend.widget',
            'data-prep.services.dataset',
            'data-prep.services.playground',
            'data-prep.services.utils',
            'data-prep.services.state'
        ])
        .controller('DatasetXlsPreviewCtrl', DatasetXlsPreviewCtrl)
        .directive('datasetXlsPreview', DatasetXlsPreview);
})();