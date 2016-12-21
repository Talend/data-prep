/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import AppHeaderBarCtrl from './app-header-bar-controller';

const AppHeaderBarContainer = {
	template: `<pure-app-header-bar
		 	app="$ctrl.app"
		 	brand-link="$ctrl.brandLink"
		 	content="$ctrl.content"
		 	watch-depth="reference"
		/>`,
	controller: AppHeaderBarCtrl,
	bindings: {
		searchToggle: '<',
		searchInput: '<',
		searchResults: '<',
		searching: '<',
		searchFocusedSectionIndex: '<',
		searchFocusedItemIndex: '<',
	},
};
export default AppHeaderBarContainer;
