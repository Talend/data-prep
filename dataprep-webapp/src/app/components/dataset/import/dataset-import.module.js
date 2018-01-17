/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

import DatasetImportComponent from './dataset-import.component';

import SERVICES_IMPORT_MODULE from '../../../services/import/import-module';

const MODULE_NAME = 'data-prep.dataset-import';

angular.module(MODULE_NAME, [SERVICES_IMPORT_MODULE])
	.component('datasetImport', DatasetImportComponent);

export default MODULE_NAME;
