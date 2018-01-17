/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.lookup-dataset-list.controller:LookupDatasetListCtrl
 * @description Lookup Dataset List controller.
 */

export default class LookupDatasetListCtrl {
	$onChanges() {
		this.filterDatasets();
	}

	/**
	 * @ngdoc method
	 * @name filterDatasets
	 * @methodOf data-prep.lookup-dataset-list.controller:LookupDatasetListCtrl
	 * @description filter datasets
	 */
	filterDatasets() {
		if (this.searchText) {
			this.filteredDatasets = this.datasets.filter(
				(dataset) => {
					return dataset.model.name.toLowerCase().indexOf(this.searchText.toLowerCase()) !== -1;
				});
		}
		else {
			this.filteredDatasets = this.datasets;
		}
	}

	/**
	 * @ngdoc method
	 * @name toogleSelect
	 * @methodOf data-prep.lookup-dataset-list.controller:LookupDatasetListCtrl
	 * @description Select/Deselect a dataset
	 */
	toogleSelect(dataset) {
		if (dataset.enableToAddToLookup) {
			dataset.addedToLookup = !dataset.addedToLookup;
		}
	}
}
