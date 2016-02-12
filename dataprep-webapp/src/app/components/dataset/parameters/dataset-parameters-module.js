/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import DatasetParametersCtrl from './dataset-parameters-controller';
import DatasetParameters from './dataset-parameters-directive';

(function () {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.dataset-parameters
     * @description This module contains the controller and directives to manage the dataset parameters
     * @requires talend.widget
     * @requires data-prep.services.dataset
     * @requires data-prep.services.playground
     */
    angular.module('data-prep.dataset-parameters',
        [
            'pascalprecht.translate',
            'talend.widget',
            'data-prep.services.dataset',
            'data-prep.services.playground'
        ])
        .controller('DatasetParametersCtrl', DatasetParametersCtrl)
        .directive('datasetParameters', DatasetParameters);
})();