/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const HomePreparationContainer = {
	template: `
		<div class="home-content">
			<breadcrumbs items="$ctrl.state.inventory.breadcrumb"></breadcrumbs>
			<react-preparation-list 
				display-mode="$ctrl.state.inventory.preparationsDisplayMode"
				items="$ctrl.state.inventory.folder.content.preparations"
				sort-by="$ctrl.state.inventory.preparationsSort.id"
				sort-desc="$ctrl.state.inventory.preparationsOrder.id === 'desc'"
			/>
		</div>
	`,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};

export default HomePreparationContainer;
