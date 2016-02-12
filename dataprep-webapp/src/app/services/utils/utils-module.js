/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import copyright from './config/utils-copyrights-service';
import version from './config/utils-version-service';
import TDPMoment from './moment/moment-filter';
import RestURLs from './config/utils-rest-urls-service';
import ConverterService from './converter/converter-service';
import DateService from './date/date-service';
import MessageService from './message/message-service';
import StorageService from './storage/storage-service';
import TextFormatService from './text-format/text-format-service';
import WorkerService from './worker/worker-service';
import DisableRightClick from './click/disable-right-click-directive';

(() => {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.services.utils
     * @description This module contains all the utiles services
     */
    angular.module('data-prep.services.utils',
        [
            'pascalprecht.translate',
            'toaster'
        ])
        .value('copyRights', copyright)
        .value('version', version)
        .filter('TDPMoment', TDPMoment)
        .service('RestURLs', RestURLs)
        .service('ConverterService', ConverterService)
        .service('DateService', DateService)
        .service('MessageService', MessageService)
        .service('StorageService', StorageService)
        .service('TextFormatService', TextFormatService)
        .service('WorkerService', WorkerService)
        .directive('disableRightClick', DisableRightClick);
})();