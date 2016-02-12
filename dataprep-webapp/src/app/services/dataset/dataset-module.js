/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import ColumnTypesService from './column-types/column-types-service';
import DatasetSheetPreviewService from './preview/dataset-sheet-preview-service';
import DatasetListService from './list/dataset-list-service';
import DatasetRestService from './rest/dataset-rest-service';
import DatasetService from './dataset-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.dataset
     * @description This module contains the services to manipulate datasets
     * @requires data-prep.services.folder
     * @requires data-prep.services.preparation
     * @requires data-prep.services.state
     * @requires data-prep.services.utils
     */
    angular.module('data-prep.services.dataset',
        [
            'angularFileUpload', //file upload with progress support
            'data-prep.services.folder',
            'data-prep.services.preparation',
            'data-prep.services.state',
            'data-prep.services.utils'
        ])
        .service('ColumnTypesService', ColumnTypesService)
        .service('DatasetSheetPreviewService', DatasetSheetPreviewService)
        .service('DatasetRestService', DatasetRestService)
        .service('DatasetListService', DatasetListService)
        .service('DatasetService', DatasetService);
})();