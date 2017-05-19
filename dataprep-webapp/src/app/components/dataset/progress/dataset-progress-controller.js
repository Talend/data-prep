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
		const state = this.state.dataset;
		return !!(state && state.uploadingDataset);
	}

	get progression() {
		const state = this.state.dataset;

		if (!state.uploadingDataset || state.uploadingDataset.progress < 0 || isNaN(state.uploadingDataset.progress)) {
			return 0;
		}
		return state.uploadingDataset.progress > 100 ? 100 : state.uploadingDataset.progress;
	}

	get isUploadComplete() {
		const state = this.state.dataset;
		return !!(state.uploadingDataset && this.progression === 100);
	}
}
