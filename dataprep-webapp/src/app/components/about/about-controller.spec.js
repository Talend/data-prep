/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('about controller', () => {
	let scope;
	let createController;

	beforeEach(angular.mock.module('data-prep.about'));

	beforeEach(inject(($rootScope, $componentController, AboutService) => {
		scope = $rootScope.$new();

		createController = () => $componentController('about', { $scope: scope });
		spyOn(AboutService, 'loadBuilds').and.returnValue();
	}));

	it('should toggle build details display', () => {
		// given
		const ctrl = createController();

		// when
		ctrl.toggleDetailsDisplay();

		// then
		expect(ctrl.showBuildDetails).toBe(true);
	});

	it('should populate build details on controller instantiation', inject((AboutService) => {
		// when
		createController();

		// then
		expect(AboutService.loadBuilds).toHaveBeenCalled();
	}));
});
