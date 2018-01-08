/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
import docSearchResults from '../../../../../mocks/Documentation.mock';

import settings from '../../../../../mocks/Settings.mock';

const { help } = settings;

describe('Search Documentation Rest Service', () => {
	let $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.search.documentation'));

	beforeEach(inject(($rootScope, $injector) => {
		$httpBackend = $injector.get('$httpBackend');
	}));

	beforeEach(inject((HelpService) => {
		HelpService.register(help);
	}));

	it('should call external documentation rest service ',
		inject(($rootScope, SearchDocumentationRestService, HelpService) => {
		// given
		const keyword = 'chart';
		let result = null;
		$httpBackend
			.expectPOST(HelpService.searchUrl)
			.respond(200, docSearchResults);

		// when
		SearchDocumentationRestService.search(keyword).then((response) => {
			result = response.data;
		});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(result).toEqual(docSearchResults);
	}));
});
