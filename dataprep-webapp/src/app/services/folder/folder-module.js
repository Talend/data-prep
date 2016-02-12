/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import FolderService from './folder-service';
import FolderRestService from './rest/folder-rest-service';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.folder
     * @description This module contains the services to manipulate folders
     */
    angular.module('data-prep.services.folder',
        [
            'data-prep.services.dataset',
            'data-prep.services.state',
            'data-prep.services.utils'
        ])
        .service('FolderRestService', FolderRestService)
        .service('FolderService', FolderService);
})();