/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class DatasetProgressCtrl {
	constructor(state) {
		'ngInject';
		this.state = state;
	}

	get isUploading() {
		return !!this.state.dataset.uploadingDataset;
	}

	get progression() {
		return this.state.dataset.uploadingDataset && this.state.dataset.uploadingDataset.progress;
	}

	get isUploadComplete() {
		return this.state.dataset.uploadingDataset && this.state.dataset.uploadingDataset.progress === 100;
	}
}
