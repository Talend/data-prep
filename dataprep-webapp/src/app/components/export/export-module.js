/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';
import SERVICES_EXPORT_MODULE from '../../services/export/export-module';
import SERVICES_STATE_MODULE from '../../services/state/state-module';
import SERVICES_UTILS_MODULE from '../../services/utils/utils-module';

import ExportCtrl from './export-controller';
import Export from './export-directive';

const MODULE_NAME = 'data-prep.export';

angular.module(MODULE_NAME,
	[
		TALEND_WIDGET_MODULE,
		SERVICES_EXPORT_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_UTILS_MODULE,
	])
    .controller('ExportCtrl', ExportCtrl)
    .directive('export', Export);

export default MODULE_NAME;
