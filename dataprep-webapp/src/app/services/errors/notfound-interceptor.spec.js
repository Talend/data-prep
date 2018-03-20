/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_404_ROUTE } from '../../index-route';

describe('Not Found interceptor', () => {
	let httpProvider;

	beforeEach(angular.mock.module('data-prep.services.errors', ($httpProvider) => {
		httpProvider = $httpProvider;
	}));

	beforeEach(inject(($state) => {
		spyOn($state, 'go').and.returnValue();
	}));

	it('should have the NotFoundInterceptor as an interceptor', () => {
		expect(httpProvider.interceptors).toContain('NotFoundInterceptor');
	});

	it('should redirect to 404', inject(($state, $http, $httpBackend) => {
		// given
		$httpBackend.when('GET', 'test').respond(404);
		expect($state.go).not.toHaveBeenCalled();

		// when
		$http.get('test');
		$httpBackend.flush();

		// then
		expect($state.go).toHaveBeenCalledWith(HOME_404_ROUTE);
	}));
});
