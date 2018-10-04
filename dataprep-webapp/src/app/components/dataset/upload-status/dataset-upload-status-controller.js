/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class PreparationCreatorFormCtrl {
	constructor($translate) {
		'ngInject';

		this.processingLabel = $translate.instant('UPLOAD_PROCESSING');
	}

	/**
	 * @ngdoc method
	 * @name getProgressionLabel
	 * @methodOf data-prep.dataset-upload-status:PreparationCreatorFormCtrl
	 * @returns {string} label to display as progressbar tooltip
	 */
	getProgressionLabel() {
		if (this.dataset.progress === 100) {
			return this.processingLabel;
		}

		return `${this.dataset.progress} %`;
	}
}
