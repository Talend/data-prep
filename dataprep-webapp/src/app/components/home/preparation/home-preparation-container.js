/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import './home-preparation.scss';

const HomePreparationContainer = {
	template: `
		<div class="home-preparation">
			<breadcrumbs id="preparations-breadcrumb" class="preparations-breadcrumb" items="$ctrl.state.inventory.breadcrumb"></breadcrumbs>
			<inventory-list
				id="'preparations-list'"
				class="preparations-list"
				folders="$ctrl.state.inventory.folder.content.folders"
				is-loading="$ctrl.state.inventory.isFetchingPreparations"
				items="$ctrl.state.inventory.folder.content.preparations"
				sort-by="$ctrl.state.inventory.folder.sort.field"
				sort-desc="$ctrl.state.inventory.folder.sort.isDescending"
				display-mode="$ctrl.state.inventory.preparationsDisplayMode"
				view-key="'listview:preparations'"
				folder-view-key="'listview:folders'"
			></inventory-list>
			<div insertion-home-preparation></div>
		</div>
	`,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};

export default HomePreparationContainer;
