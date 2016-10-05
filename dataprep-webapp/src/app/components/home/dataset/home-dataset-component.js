/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const HomeDatasetComponent = {
	template: `
        <div class="home-main">
            <dataset-upload-list class="upload-list-container" ng-if="$ctrl.state.dataset.uploadingDatasets.length" datasets="$ctrl.state.dataset.uploadingDatasets"></dataset-upload-list>
            <dataset-header ng-if="!$ctrl.state.inventory.isFetchingDatasets" class="inventory-list-header"></dataset-header>
            <dataset-list class="inventory-list-container"></dataset-list>
        </div>
    `,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};
export default HomeDatasetComponent;
