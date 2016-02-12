/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import ExportRestService from './export-rest-service';
import ExportService from './export-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.export
     * @description This module contains the services for export
     */
    angular.module('data-prep.services.export', ['data-prep.services.utils'])
        .service('ExportRestService', ExportRestService)
        .service('ExportService', ExportService);
})();