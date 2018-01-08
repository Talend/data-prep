/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import LookupDatasetListCtrl from './lookup-dataset-list-controller';

import template from './lookup-dataset-list.html';
/**
 * @ngdoc component
 * @name data-prep.lookup-dataset-list.component:LookupDatasetList
 * @description This component display the lookup dataset list
 * @usage <lookup-dataset-list datasets="datasets" searchText="searchText"></lookup-dataset-list>
 * @param {array} datasets datasets to add to the lookup
 * @param {string} searchText text is used to filter the datasets list
 * @restrict E
 */
const LookupDatasetListComponent = {
	templateUrl: template,
	controller: LookupDatasetListCtrl,
	bindings: {
		datasets: '<',
		searchText: '<',
	},
};

export default LookupDatasetListComponent;
