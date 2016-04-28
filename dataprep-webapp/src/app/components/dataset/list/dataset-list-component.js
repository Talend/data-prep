/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetListCtrl from './dataset-list-controller';

/**
 * @ngdoc component
 * @name data-prep.dataset-list.component:DatasetList
 * @description This component display the dataset list from {@link data-prep.services.dataset.service:DatasetService DatasetService}
 * @restrict E
 */
const DatasetListComponent = {
    templateUrl: 'app/components/dataset/list/dataset-list.html',
    controller: DatasetListCtrl,
};

export default DatasetListComponent;