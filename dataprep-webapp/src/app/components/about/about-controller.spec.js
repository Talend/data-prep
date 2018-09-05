/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

fdescribe('about controller', () => {
	let scope;
	let createController;

	beforeEach(angular.mock.module('data-prep.about'));
	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			ABOUT_COPYRIGHTS: 'fake',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q, $rootScope, $componentController, AboutService) => {
		scope = $rootScope.$new();

		createController = () => $componentController('about', { $scope: scope });
		spyOn(AboutService, 'loadBuilds').and.returnValue($q.when());
	}));

	it('should toggle build details display', () => {
		const ctrl = createController();
		ctrl.toggle();
		expect(ctrl.expanded).toBe(true);
	});

	it('should populate build details on controller instantiation', inject((AboutService) => {
		const ctrl = createController();

		// when
		ctrl.$onInit();

		// then
		expect(AboutService.loadBuilds).toHaveBeenCalled();
	}));

	it('should return copyrights', inject(() => {
		const ctrl = createController();
		expect(ctrl.getCopyrights()).toBe('fake');
	}));
});
