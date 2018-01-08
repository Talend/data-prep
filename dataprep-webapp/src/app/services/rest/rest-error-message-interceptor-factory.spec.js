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

	beforeEach(inject(($injector, MessageService) => {
		$httpBackend = $injector.get('$httpBackend');
		$httpBackend.when('GET', 'i18n/en.json').respond({});
		$httpBackend.when('GET', 'i18n/fr.json').respond({});

		spyOn(MessageService, 'error').and.returnValue();
	}));

	it('should have the RestErrorMessageHandler as an interceptor', () => {
		expect(httpProvider.interceptors).toContain('RestErrorMessageHandler');
	});

	it('should show alert when service is unavailable', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(0);

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).toHaveBeenCalledWith('SERVER_ERROR_TITLE', 'SERVICE_UNAVAILABLE');
	}));

	it('should show toast on status 500', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(500);

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).toHaveBeenCalledWith('SERVER_ERROR_TITLE', 'GENERIC_ERROR');
	}));

	it('should not show toast when fileSilently flag is set', inject(($rootScope, $http, MessageService) => {
		//given
		const request = {
			method: 'POST',
			url: 'testService',
			failSilently: true,
		};
		$httpBackend.expectPOST('testService').respond(500);

		//when
		$http(request);
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));

	it('should not show message on user cancel', inject(($rootScope, $q, $http, MessageService) => {
		//given
		const canceler = $q.defer();
		const request = {
			method: 'POST',
			url: 'testService',
			timeout: canceler.promise,
		};
		$httpBackend.expectPOST('testService').respond(500);

		//when
		$http(request);
		canceler.resolve('user cancel');
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));

	it('should show expected error message if exist', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(400, {
			messageTitle: 'TDP_API_DATASET_STILL_IN_USE_TITLE',
			message: 'TDP_API_DATASET_STILL_IN_USE'
		});

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).toHaveBeenCalledWith('TDP_API_DATASET_STILL_IN_USE_TITLE', 'TDP_API_DATASET_STILL_IN_USE');
	}));

	it('should not show error message if not exist', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(400, '');

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));

	it('should not show error message if unauthorized', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(401, {
			messageTitle: 'TDP_API_DATASET_FORBIDDEN_TITLE',
			message: 'TDP_API_DATASET_FORBIDDEN'
		});

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));

	it('should not show error message if forbidden', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(403, {
			messageTitle: 'TDP_API_DATASET_FORBIDDEN_TITLE',
			message: 'TDP_API_DATASET_FORBIDDEN'
		});

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));

	it('should not show error message if not found', inject(($rootScope, $http, MessageService) => {
		//given
		$httpBackend.expectGET('testService').respond(404, {
			messageTitle: 'TDP_API_DATASET_NOT_FOUND_TITLE',
			message: 'TDP_API_DATASET_NOT_FOUND'
		});

		//when
		$http.get('testService');
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(MessageService.error).not.toHaveBeenCalled();
	}));
});
