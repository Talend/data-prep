/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import COLUMN_PROFILE_OPTIONS_MODULE from '../column-profile-options/column-profile-options-module';
import TALEND_WIDGET_MODULE from '../../widgets/widget-module';
import SERVICES_DATASET_MODULE from '../../../services/dataset/dataset-module';
import SERVICES_FILTER_MANAGER_MODULE from '../../../services/filter/manager/filter-manager-module';
import SERVICES_RECIPE_MODULE from '../../../services/recipe/recipe-module';
import SERVICES_STATE_MODULE from '../../../services/state/state-module';
import SERVICES_STATISTICS_MODULE from '../../../services/statistics/statistics-module';

import ColumnProfileCtrl from './column-profile-controller';
import ColumnProfile from './column-profile-directive';

const MODULE_NAME = 'data-prep.column-profile';

angular.module(MODULE_NAME,
	[
		COLUMN_PROFILE_OPTIONS_MODULE,
		TALEND_WIDGET_MODULE,
		SERVICES_DATASET_MODULE,
		SERVICES_FILTER_MANAGER_MODULE,
		SERVICES_RECIPE_MODULE,
		SERVICES_STATE_MODULE,
		SERVICES_STATISTICS_MODULE,
	])
    .controller('ColumnProfileCtrl', ColumnProfileCtrl)
    .directive('columnProfile', ColumnProfile);

export default MODULE_NAME;
