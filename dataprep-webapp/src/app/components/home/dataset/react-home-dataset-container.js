/*  ============================================================================
 Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE
 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France
 ============================================================================*/
import HomeDatasetCtrl from './react-home-dataset-controller';
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
				<label class="sr-only" translate-once="IMPORT_DATASET_FILE"></label>
				<input
					type="file"
					id="importDatasetFile"
					name="datasetFile"
					class="ng-hide"
					ng-file-select
					accept="*.csv"
					ng-model="$ctrl.importDatasetFile"
					ng-file-change="$ctrl.import($ctrl.ImportService.currentInputType)"/>
			</form>
			<inventory-list
				id="'datasets-list'"
				items="$ctrl.state.inventory.datasets"
				sort-by="$ctrl.state.inventory.datasetsSort.id"
				sort-desc="$ctrl.state.inventory.datasetsOrder.id === 'desc'"
				view-key="'listview:datasets'"
			/>
		</div>
		<talend-modal
				fullscreen="false"
				state="$ctrl.state.import.showImportModal"
				ng-if="$ctrl.state.import.showImportModal"
				close-button="true"
				disable-enter="true">
					<div class="modal-title">{{$ctrl.ImportService.currentInputType.title}}</div>
					<br/>
					<form name="$ctrl.ImportService.datasetForm"
						  ng-if="!$ctrl.ImportService.datastoreForm"
						  ng-submit="$ctrl.ImportService.datasetForm.$valid && $ctrl.import($ctrl.ImportService.currentInputType)">
						<transform-params parameters="$ctrl.ImportService.currentInputType.parameters"></transform-params>
						<div class="modal-buttons">
							<button class="btn talend-modal-close btn-default modal-secondary-button"
									type="button"
									translate-once="CANCEL"></button>
							<button class="btn btn-primary modal-primary-button"
									ng-disabled="$ctrl.ImportService.datasetForm.$invalid"
									translate-once="OK"></button>
						</div>
					</form>
					<div class="tcomp">
						<div class="datastore"
							 ng-if="$ctrl.ImportService.datastoreForm && $ctrl.ImportService.datastoreForm.jsonSchema">
							<talend-form data="$ctrl.ImportService.datastoreForm"
										 autocomplete="off"
										 actions="$ctrl.ImportService.datastoreFormActions"
										 on-change="$ctrl.onDatastoreFormChange"
										 on-submit="$ctrl.onDatastoreFormSubmit"/>
						</div>
						<p class="text-right text-success"
						   ng-if="$ctrl.ImportService.datasetForm"
						   translate-once="DATASTORE_CONNECTION_SUCCESSFUL">
						</p>
						<div class="dataset"
							 ng-if="$ctrl.ImportService.datasetForm && $ctrl.ImportService.datasetForm.jsonSchema">
							<talend-form data="$ctrl.ImportService.datasetForm"
										 autocomplete="off"
										 actions="$ctrl.ImportService.datasetFormActions"
										 on-change="$ctrl.onDatasetFormChange"
										 on-submit="$ctrl.onDatasetFormSubmit"/>
						</div>
					</div>
		</talend-modal>
		<talend-modal fullscreen="false"
              close-button="true"
              state="$ctrl.datasetNameModal"
              ng-if="$ctrl.datasetNameModal"
              disable-enter="true">
				<span translate-once="ENTER_DATASET_NAME"></span>
				<form name="$ctrl.datasetNameForm"
					  ng-submit="$ctrl.datasetNameForm.$valid && $ctrl.onImportNameValidation()">
					<input type="text"
						   ng-model="$ctrl.datasetName"
						   required
						   ng-model-options="{debounce: { 'default': 200, 'blur': 0 }}"/>

					<div class="modal-buttons">
						<button class="btn talend-modal-close btn-default modal-secondary-button"
								type="button"
								translate-once="CANCEL"></button>
						<button class="btn talend-modal-close btn-primary modal-primary-button"
								ng-disabled="$ctrl.datasetNameForm.$invalid"
								translate-once="OK"></button>
					</div>
				</form>
		</talend-modal>
	`,
	controller: HomeDatasetCtrl,
};

export default HomeDatasetContainer;
