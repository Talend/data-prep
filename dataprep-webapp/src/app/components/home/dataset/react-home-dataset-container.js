/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const HomeDatasetContainer = {
	template: `
		<div class="home-content">
			<react-dataset-list
				display-mode="$ctrl.state.inventory.datasetsDisplayMode"
				items="$ctrl.state.inventory.datasets"
				sort-by="$ctrl.state.inventory.datasetsSort.id"
				sort-desc="$ctrl.state.inventory.datasetsOrder.id === 'desc'"
			/>
		</div>
	`,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};

export default HomeDatasetContainer;
