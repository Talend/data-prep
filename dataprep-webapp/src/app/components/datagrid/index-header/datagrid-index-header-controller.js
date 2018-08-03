/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import {
	QUALITY,
} from '../../../services/filter/adapter/tql-filter-adapter-service';

/**
 * @ngdoc controller
 * @name data-prep.datagrid-index-header.controller:DatagridIndexHeaderCtrl
 * @description Index Column Header controller.
 * @requires data-prep.services.filter-manager.service:FilterManagerService
 */
export default class DatagridIndexHeaderCtrl {
	constructor(FilterManagerService) {
		'ngInject';

		this.FilterManagerService = FilterManagerService;
		this.QUALITY = QUALITY;
	}
}
