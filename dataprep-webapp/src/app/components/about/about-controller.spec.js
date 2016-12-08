/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('about controller', () => {
	let scope;
	let createController;

	const allBuildDetails = [
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


	beforeEach(angular.mock.module('data-prep.about'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => $componentController('about', { $scope: scope });
	}));

	it('should toggle build details display', () => {
		// given
		const ctrl = createController();

		// when
		ctrl.toggleDetailsDisplay();

		// then
		expect(ctrl.showBuildDetails).toBe(true);
	});

	it('should call rest api', inject(($q, AboutService) => {
		// given
		const ctrl = createController();
		spyOn(AboutService, 'buildDetails').and.returnValue($q.when());

		// when
		ctrl.getBuildDetails();
		scope.$digest();

		// then
		expect(ctrl.aboutService.buildDetails).toHaveBeenCalled();
	}));

	it('should toggle build details display after api call', inject(($q, AboutService) => {
		// given
		spyOn(AboutService, 'buildDetails').and.returnValue($q.when(true));
		const ctrl = createController();
		expect(ctrl.showBuildDetails).toBe(false);

		// when
		ctrl.getBuildDetails();
		scope.$digest();

		// then
		expect(ctrl.showBuildDetails).toBe(true);
	}));

	it('should populate build details after api call', inject(($q, AboutService) => {
		// given
		spyOn(AboutService, 'buildDetails').and.returnValue($q.when(allBuildDetails));
		const ctrl = createController();
		expect(ctrl.buildDetails).toEqual([]);

		// when
		ctrl.getBuildDetails();
		scope.$digest();

		// then
		expect(ctrl.buildDetails).toBe(allBuildDetails);
	}));
});
