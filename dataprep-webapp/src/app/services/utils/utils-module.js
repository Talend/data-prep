/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ngTranslate from 'angular-translate';
import toaster from 'angularjs-toaster';

import copyright from './config/utils-copyrights-service';
import version from './config/utils-version-service';
import TDPMoment from './moment/moment-filter';
import RestURLs from './config/utils-rest-urls-service';
import ConverterService from './converter/converter-service';
import DateService from './date/date-service';
import MessageService from './message/message-service';
import StepUtilsService from './step/step-utils-service';
import StorageService from './storage/storage-service';
import TextFormatService from './text-format/text-format-service';
import DisableRightClick from './click/disable-right-click-directive';
import documentationSearchURL from './config/utils-documentation-search-url-service';

const MODULE_NAME = 'data-prep.services.utils';

/**
 * @ngdoc object
 * @name data-prep.services.utils
 * @description This module contains all the utiles services
 */
angular.module(MODULE_NAME,
	[
		ngTranslate,
		toaster,
	])
    .value('copyRights', copyright)
    .value('version', version)
    .value('documentationSearchURL', documentationSearchURL)
    .filter('TDPMoment', TDPMoment)
    .service('RestURLs', RestURLs)
    .service('ConverterService', ConverterService)
    .service('DateService', DateService)
    .service('MessageService', MessageService)
    .service('StepUtilsService', StepUtilsService)
    .service('StorageService', StorageService)
    .service('TextFormatService', TextFormatService)
    .directive('disableRightClick', DisableRightClick);

export default MODULE_NAME;
