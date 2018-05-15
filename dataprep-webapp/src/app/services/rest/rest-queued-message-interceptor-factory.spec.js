/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Rest message interceptor factory', () => {
	'use strict';

	let $httpBackend;
	let httpProvider;

	beforeEach(angular.mock.module('data-prep.services.rest', $httpProvider => {
		httpProvider = $httpProvider;
	}));

	beforeEach(inject(($injector, RestURLs, MessageService) => {
		RestURLs.register({});
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should have the RestQueuedMessageHandler as an interceptor', () => {
		expect(httpProvider.interceptors).toContain('RestQueuedMessageHandler');
	});

	it('should follow the location header on 202 ', inject(($rootScope, $http, $timeout, MessageService) => {
		$httpBackend.expectGET('slooooow').respond(202, '', { 'Location': 'status' });
		$httpBackend.expectGET('status').respond(200,
			{
				data: {
					status: 'DONE',
					result: {
						downloadUrl: 'result',
					}
				},
			},
		);

		$http.get('slooooow');
		$httpBackend.flush();
		$rootScope.$digest();
	}));

	it('should do nothing when response code is 200', inject(($rootScope, $http, $timeout, MessageService) => {
		let forbidden = false;
		$httpBackend.expectGET('test').respond(200, '', { 'Location': 'status' });
		$httpBackend.when('status').respond(() => {
			forbidden = true;
			return [400, ''];
		});


		$http.get('test');
		$httpBackend.flush();
		$rootScope.$digest();

		expect(forbidden).toBe(false);
	}));
});
