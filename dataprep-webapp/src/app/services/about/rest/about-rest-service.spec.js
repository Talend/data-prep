/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('About REST Service', () => {
	'use strict';

	let $httpBackend;
	const buildDetails = [
		{
			"versionId": "2.0.0-SNAPSHOT",
			"buildId": "2adb70d",
			"serviceName": "API"
		},
		{
			"versionId": "2.0.0-SNAPSHOT",
			"buildId": "2adb70d",
			"serviceName": "DATASET"
		},
		{
			"versionId": "2.0.0-SNAPSHOT",
			"buildId": "2adb70d",
			"serviceName": "PREPARATION"
		},
		{
			"versionId": "2.0.0-SNAPSHOT",
			"buildId": "2adb70d",
			"serviceName": "TRANSFORMATION"
		}
	];

	beforeEach(angular.mock.module('data-prep.services.about'));

	beforeEach(inject(($injector, RestURLs) => {
		RestURLs.setServerUrl('');
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should get all build details', inject(($rootScope, RestURLs, AboutRestService) => {
		//given
		let details = null;

		$httpBackend
			.expectGET(RestURLs.buildsUrl)
			.respond(200, buildDetails);

		//when
		AboutRestService.buildDetails()
			.then((response) => {
				details = response;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(details).toEqual(buildDetails);
	}));
});
