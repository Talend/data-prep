/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const INVALID_RECORDS = 'invalid_records';
const EMPTY_RECORDS = 'empty_records';
const INVALID_EMPTY_RECORDS = 'invalid_empty_records';

/**
 * @ngdoc controller
 * @name data-prep.datagrid-index-header.controller:DatagridIndexHeaderCtrl
 * @description Index Column Header controller.
 * @requires data-prep.services.filter.service:FilterService
 */
export default class DatagridIndexHeaderCtrl {
	constructor(FilterService) {
		'ngInject';

		this.FilterService = FilterService;
		this.INVALID_RECORDS = INVALID_RECORDS;
		this.EMPTY_RECORDS = EMPTY_RECORDS;
		this.INVALID_EMPTY_RECORDS = INVALID_EMPTY_RECORDS;
	}

	createFilter(type) {
		switch (type) {
		case INVALID_RECORDS:
			this.FilterService.addFilterAndDigest('invalid_records');
			break;
		case EMPTY_RECORDS:
			this.FilterService.addFilterAndDigest('empty_records');
			break;
		case INVALID_EMPTY_RECORDS:
			this.FilterService.addFilterAndDigest('quality', undefined, undefined, { invalid: true, empty: true });
			break;
		}
	}
}
