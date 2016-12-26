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
			<form>
				<label class="sr-only" translate-once="UPDATE_DATASET_INPUT"></label>
				<input
					id="inputUpdateDataset"
					type="file"
					class="ng-hide"
					ng-file-select
					accept="*.csv"
					ng-model="$ctrl.updateDatasetFile"
					ng-file-change="$ctrl.onFileChange()"/>
			</form>
			<inventory-list
				id="'datasets-list'"
				is-loading="$ctrl.state.inventory.isFetchingDatasets"
				items="$ctrl.state.inventory.datasets"
				sort-by="$ctrl.state.inventory.datasetsSort.id"
				sort-desc="$ctrl.state.inventory.datasetsOrder.id === 'desc'"
				view-key="'listview:datasets'"
			/>
		</div>
	`,
	controller(state, UpdateWorkflowService) {
		'ngInject';
		this.state = state;
		this.UpdateWorkflowService = UpdateWorkflowService;

		this.updateDatasetFile = null;

		this.onFileChange = () => {
			this.UpdateWorkflowService.updateDataset(this.updateDatasetFile[0], this.state.inventory.datasetToUpdate);
		};
	},
};

export default HomeDatasetContainer;
