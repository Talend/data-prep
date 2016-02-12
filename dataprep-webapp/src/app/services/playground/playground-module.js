/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import PlaygroundService from './playground-service';
import DatagridService from './grid/datagrid-service';
import EarlyPreviewService from './preview/early-preview-service';
import PreviewService from './preview/preview-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.playground
     * @description This module contains the services to load the playground
     * @requires data-prep.services.dataset
     * @requires data-prep.services.filter
     * @requires data-prep.services.history
     * @requires data-prep.services.onboarding
     * @requires data-prep.services.preparation
     * @requires data-prep.services.recipe
     * @requires data-prep.services.state
     * @requires data-prep.services.statistics
     * @requires data-prep.services.utils
     * @requires data-prep.services.export
     */
    angular.module('data-prep.services.playground',
        [
            'data-prep.services.dataset',
            'data-prep.services.filter',
            'data-prep.services.history',
            'data-prep.services.onboarding',
            'data-prep.services.preparation',
            'data-prep.services.recipe',
            'data-prep.services.statistics',
            'data-prep.services.state',
            'data-prep.services.utils',
            'data-prep.services.lookup',
            'data-prep.services.export'
        ])
        .service('PlaygroundService', PlaygroundService)
        .service('DatagridService', DatagridService)
        .service('EarlyPreviewService', EarlyPreviewService)
        .service('PreviewService', PreviewService);
})();