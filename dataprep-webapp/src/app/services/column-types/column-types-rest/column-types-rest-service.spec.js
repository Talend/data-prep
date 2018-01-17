/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../mocks/Settings.mock';

describe('Column types rest service', () => {
	let $httpBackend;
	const types = [
		{ id: 'ANY', label: 'any', labelKey: 'ANY' },
		{ id: 'STRING', label: 'string', labelKey: 'STRING' },
		{ id: 'NUMERIC', label: 'numeric', labelKey: 'NUMERIC' },
		{ id: 'INTEGER', label: 'integer', labelKey: 'INTEGER' },
		{ id: 'DOUBLE', label: 'double', labelKey: 'DOUBLE' },
		{ id: 'FLOAT', label: 'float', labelKey: 'FLOAT' },
		{ id: 'BOOLEAN', label: 'boolean', labelKey: 'BOOLEAN' },
		{ id: 'DATE', label: 'date', labelKey: 'DATE' },
	];

	const semanticDomains = [
		{ id: 'AIRPORT', label: 'Airport', frequency: 3.03 },
		{ id: 'CITY', label: 'City', frequency: 99.24 },
	];

	beforeEach(angular.mock.module('data-prep.services.column-types'));

	beforeEach(inject(($rootScope, $injector, RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should fetch primitive types', inject(($rootScope, RestURLs, ColumnTypesRestService) => {
		//given
		let result = null;
		$httpBackend
			.expectGET(RestURLs.typesUrl)
			.respond(200, types);

		//when
		ColumnTypesRestService.fetchTypes()
			.then((response) => {
				result = response;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(types);
	}));

	it('should dataset semantic domains', inject(($rootScope, RestURLs, ColumnTypesRestService) => {
		//given
		let result = null;
		$httpBackend
			.expectGET(`${RestURLs.datasetUrl}/myDatasetId/columns/myColId/types`)
			.respond(200, semanticDomains);

		//when
		ColumnTypesRestService.fetchDomains('dataset', 'myDatasetId', 'myColId')
			.then((response) => {
				result = response;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(semanticDomains);
	}));

	it('should preparation semantic domains', inject(($rootScope, RestURLs, ColumnTypesRestService) => {
		//given
		let result = null;
		$httpBackend
			.expectGET(`${RestURLs.preparationUrl}/myPrepId/columns/myColId/types`)
			.respond(200, semanticDomains);

		//when
		ColumnTypesRestService.fetchDomains('preparation', 'myPrepId', 'myColId')
			.then((response) => {
				result = response;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(semanticDomains);
	}));
});
