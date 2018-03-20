/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './app-header-bar.html';
import AppHeaderBarCtrl from './app-header-bar-controller';

const AppHeaderBarContainer = {
	templateUrl: template,
	controller: AppHeaderBarCtrl,
	bindings: {
		viewKey: '<',
		searchToggle: '<',
		searchInput: '<',
		searchResults: '<',
		searching: '<',
		searchFocusedSectionIndex: '<',
		searchFocusedItemIndex: '<',
	},
};
export default AppHeaderBarContainer;
