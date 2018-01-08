/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class PreparationPickerCtrl {
	constructor() {
		'ngInject';
		this.candidatePreparations = [];
	}

	/**
     * @ngdoc method
     * @name $onInit
     * @methodOf data-prep.preparation-picker.controller:PreparationPickerCtrl
     * @description initializes preparation picker form
     **/
	$onInit() {
		this.isFetchingPreparations = true;
		this.fetchPreparations({ datasetId: this.dataset.id })
			.then((compatiblePreparations) => {
				this.candidatePreparations = compatiblePreparations;
			})
			.finally(() => {
				this.isFetchingPreparations = false;
			});
	}
}

export default PreparationPickerCtrl;
