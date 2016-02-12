/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import UploadWorkflowService from './upload-workflow-service';
import UpdateWorkflowService from './update-workflow-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.dataset-workflow
     * @description This module contains the services to manage the upload & update of datasets
     * @requires ui.router
     * @requires data-prep.services.dataset
     * @requires data-prep.services.state
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.datasetWorkflowService',
        [
        'ui.router',
        'data-prep.services.dataset',
        'data-prep.services.state',
        'data-prep.services.utils'
    ])
        .service('UploadWorkflowService', UploadWorkflowService)
        .service('UpdateWorkflowService', UpdateWorkflowService);
})();