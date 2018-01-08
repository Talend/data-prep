/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import ACTIONS_HEADER_MODULE from '../suggestions-stats/actions-header/actions-header-module';
import ACTIONS_LIST_MODULE from '../suggestions-stats/actions-list/actions-list-module';
import ACTIONS_SUGGESTIONS_MODULE from '../suggestions-stats/actions-suggestions/actions-suggestions-module';
import STATS_COLUMN_PROFILE from '../suggestions-stats/column-profile/column-profile-module';
import STATS_DETAILS_MODULE from '../suggestions-stats/stats-details/stats-details-module';
import TALEND_WIDGET_MODULE from '../widgets/widget-module';

const MODULE_NAME = 'data-prep.suggestions-stats';

angular.module(MODULE_NAME,
	[
		ACTIONS_HEADER_MODULE,
		ACTIONS_LIST_MODULE,
		ACTIONS_SUGGESTIONS_MODULE,
		STATS_COLUMN_PROFILE,
		STATS_DETAILS_MODULE,
		TALEND_WIDGET_MODULE,
	]);

export default MODULE_NAME;
