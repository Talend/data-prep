/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import settings from '../../../mocks/Settings.mock';

describe('About Service', () => {
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
		RestURLs.register({ serverUrl: '' }, settings.uris);
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should get all build details', inject(($rootScope, RestURLs, AboutService) => {
		//given
		let details = null;

		$httpBackend
			.expectGET(RestURLs.versionUrl)
			.respond(200, buildDetails);

		//when
		AboutService.fetchBuildDetails()
			.then((response) => {
				details = response;
			});
		$httpBackend.flush();
		$rootScope.$digest();

		//then
		expect(details).toEqual(buildDetails);
	}));

	it('should populate state with builds details', inject(($q, $rootScope, RestURLs, AboutService, StateService) => {
		//given
		spyOn(AboutService, 'fetchBuildDetails').and.returnValue($q.when(buildDetails));
		spyOn(StateService, 'setBuilds').and.returnValue();

		//when
		AboutService.loadBuilds();
		$rootScope.$digest();

		//then
		expect(StateService.setBuilds).toHaveBeenCalledWith(buildDetails);
	}));
});
