/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../mocks/Settings.mock';

describe('Feedback Rest Service', function () {
	'use strict';

	var $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.feedback'));

	beforeEach(inject(function ($rootScope, $injector, RestURLs) {
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should send feed back', inject(function ($rootScope, FeedbackRestService, RestURLs) {
		//given
		var feedback = { title: '' };

		$httpBackend
			.expectPUT(RestURLs.mailUrl, feedback)
			.respond(200);

		//when
		FeedbackRestService.sendFeedback(feedback);
		$httpBackend.flush();

		//then
		//Expect http call to be performed. If not an error is thrown by $httpBackend
	}));
});
