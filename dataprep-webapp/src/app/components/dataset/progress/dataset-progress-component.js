/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const DatasetProgressComponent = {
	template: `<pure-progress
					id="dataset-progress-bar"
					percent="$ctrl.state.dataset.uploadingDatasets.length ? $ctrl.state.dataset.uploadingDatasets[0].progress : 0"
					tooltip="$ctrl.state.dataset.uploadingDatasets.length ? ('COLLECTING_DATA' | translate: {datasetName : $ctrl.state.dataset.uploadingDatasets[0].name}) : ''"/>
				</pure-progress>`,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};
export default DatasetProgressComponent;
