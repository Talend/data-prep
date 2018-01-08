/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const LayoutContainer = {
	transclude: true,
	template: `
		<div class="app">
			<div class="header">
				<app-header-bar search-toggle="$ctrl.state.search.searchToggle"
		 						searching="$ctrl.state.search.searching"
		 						search-input="$ctrl.state.search.searchInput"
		 						search-results="$ctrl.state.search.searchResults"
		 						search-focused-section-index="$ctrl.state.search.focusedSectionIndex"
		 						search-focused-item-index="$ctrl.state.search.focusedItemIndex" />
			</div>
			<div class="content">
				<div class="sidemenu">
					<side-panel id="'side-panel'" active="$ctrl.$state.current.name"><side-panel/>
				</div>
				<ng-transclude class="main"></ng-transclude>
			</div>
		</div>
	`,
	controller($state, state) {
		'ngInject';
		this.$state = $state;
		this.state = state;
	},
};

export default LayoutContainer;
