/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import DatagridIndexHeaderCtrl from './datagrid-index-header-controller';
import template from './datagrid-index-header.html';

/**
 * @ngdoc component
 * @name data-prep.datagrid-index-header.component:DatagridIndexHeader
 * @description This component display the header of the index column
 * @restrict E
 */
const DatagridIndexHeaderComponent = {
	templateUrl: template,
	controller: DatagridIndexHeaderCtrl,
};

export default DatagridIndexHeaderComponent;
