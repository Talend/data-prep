/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import Home from './home-directive';
import HomeCtrl from './home-controller';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.home
     * @description This module contains the home page of the app.
     * @requires talend.widget
     * @requires data-prep.dataset-upload-list
     * @requires data-prep.dataset-list
     * @requires data-prep.playground
     * @requires data-prep.preparation-list
     * @requires data-prep.services.dataset
     * @requires data-prep.services.utils
     * @requires data-prep.services.datasetWorkflowService
     * @requires data-prep.services.state
     * @requires data-prep.folder
     * @requires data-prep.services.folder
     */
    angular.module('data-prep.home',
        [
            'talend.widget',
            'data-prep.dataset-list',
            'data-prep.dataset-upload-list',
            'data-prep.folder',
            'data-prep.playground',
            'data-prep.preparation-list',
            'data-prep.services.dataset',
            'data-prep.services.datasetWorkflowService',
            'data-prep.services.folder',
            'data-prep.services.state',
            'data-prep.services.utils'
        ])
        .controller('HomeCtrl', HomeCtrl)
        .directive('home', Home);
})();