/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../../../mocks/Settings.mock';

describe('Search Inventory Rest Service', () => {

	let $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.search.inventory'));

	beforeEach(inject(($injector, RestURLs) => {
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should call inventory search rest service ', inject(($rootScope, $q, SearchInventoryRestService, RestURLs) => {
		//given
		let result = null;
		const expectedResult = {
			folders: [],
			preparations: [],
			datasets: [],
		};
		$httpBackend
			.expectGET(RestURLs.searchUrl + '?path=/&name=test')
			.respond(200, expectedResult);

		//when
		SearchInventoryRestService.search('test', $q.defer())
			.then((response) => {
				result = response.data;
			});

		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(result).toEqual(expectedResult);
	}));
});
