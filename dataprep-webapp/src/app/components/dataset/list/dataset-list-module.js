/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import DatasetListCtrl from './dataset-list-controller';
import DatasetList from './dataset-list-directive';

(function () {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.dataset-list
     * @description This module contains the controller and directives to manage the dataset list
     * @requires talend.widget
     * @requires ddata-prep.dataset-xls-preview
     * @requires data-prep.inventory-item
     * @requires data-prep.services.dataset
     * @requires data-prep.services.playground
     * @requires data-prep.services.datasetWorkflowService
     * @requires data-prep.services.utils
     * @requires data-prep.services.state
     * @requires data-prep.services.folder
     */
    angular.module('data-prep.dataset-list',
        [
            'ui.router',
            'pascalprecht.translate',
            'talend.widget',
            'data-prep.dataset-xls-preview',
            'data-prep.inventory-item',
            'data-prep.services.dataset',
            'data-prep.services.playground',
            'data-prep.services.datasetWorkflowService',
            'data-prep.services.utils',
            'data-prep.services.state',
            'data-prep.services.folder'
        ])
        .controller('DatasetListCtrl', DatasetListCtrl)
        .directive('datasetList', DatasetList);
})();