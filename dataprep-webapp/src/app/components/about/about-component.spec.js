/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Breadcrumb component', () => {
	let scope;
	let element;
	let createElement;
	let stateMock;
	let controller;

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

	beforeEach(angular.mock.module('data-prep.about', ($provide) => {
		stateMock = {
			home: {
				about: {
					isVisible: true,
					builds: allBuildDetails
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			"ABOUT_HEADER": "ABOUT TALEND DATA PREPARATION",
			"MORE": "more",
			"LESS": "less",
			"SERVICE_NAME": "SERVICE",
			"BUILD_ID": "BUILD ID",
			"VERSION_ID": "VERSION ID",
			"VERSION": "VERSION",
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile, AboutService) => {
		scope = $rootScope.$new(true);
		spyOn(AboutService, 'loadBuilds').and.returnValue();

		createElement = () => {
			const html = `<about></about>`;
			element = $compile(html)(scope);
			scope.$digest();
			controller = element.controller('about');
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render about modal', () => {
			// given
			stateMock.home.about.isVisible = false;
			createElement();
			expect(element.find('talend-modal').length).toBe(0);

			// when
			stateMock.home.about.isVisible = true;
			scope.$digest();

			// then
			expect(element.find('talend-modal').length).toBe(1);
		});

		it('should render about modal title', () => {
			// when
			createElement();

			// then
			expect(element.find('.modal-header').text().trim()).toBe('ABOUT TALEND DATA PREPARATION');
		});

		it('should render data-prep icon', () => {
			// when
			createElement();

			// then
			expect(element.find('#about-dataprep-logo').attr('data-icon')).toBe('c');
		});

		it('should render data-prep current version', () => {
			// given
			createElement();

			// when
			controller.version = 'current version';
			scope.$digest();

			// then
			expect(element.find('#version').text().trim()).toBe('VERSION: current version');
		});

		it('should render data-prep copyrights', () => {
			// given
			createElement();

			// when
			controller.copyRights = 'current copyRights';
			scope.$digest();

			// then
			expect(element.find('#copyrights').text().trim()).toBe('current copyRights');
		});

		describe('no builds list', () => {
			it('should not render builds list', () => {
				// when
				createElement();

				// then
				expect(element.find('table').length).toBe(0);
			});

			it('should render more button', () => {
				// when
				createElement();

				// then
				expect(element.find('.modal-body button').length).toBe(1);
				expect(element.find('.modal-body button').text().trim()).toBe('more');
			});
		});

		describe('with builds list', () => {
			it('should render builds list', () => {
				// when
				createElement();
				controller.showBuildDetails = true;
				scope.$digest();

				// then
				expect(element.find('table').length).toBe(1);
				expect(element.find('table thead th').eq(0).text().trim()).toBe('SERVICE');
				expect(element.find('table thead th').eq(1).text().trim()).toBe('BUILD ID');
				expect(element.find('table thead th').eq(2).text().trim()).toBe('VERSION ID');

				expect(element.find('table tbody tr').length).toBe(allBuildDetails.length);
				expect(element.find('table tbody tr td').eq(0).text().trim()).toBe(allBuildDetails[0].serviceName);
				expect(element.find('table tbody tr td').eq(1).text().trim()).toBe(allBuildDetails[0].buildId);
				expect(element.find('table tbody tr td').eq(2).text().trim()).toBe(allBuildDetails[0].versionId);
			});

			it('should render less button', () => {
				// when
				createElement();
				controller.showBuildDetails = true;
				controller.buildDetails = allBuildDetails;
				scope.$digest();

				// then
				expect(element.find('.modal-body button').length).toBe(1);
				expect(element.find('.modal-body button').text().trim()).toBe('less');
			});
		});

		describe('clicks', () => {
			it('should display table on more button click', () => {
				// given
				createElement();

				// when
				element.find('.modal-body button').click();
				scope.$digest();

				// then
				expect(element.find('table').length).toBe(1);
			});

			it('should hide table on less button click', () => {
				// given
				createElement();
				controller.showBuildDetails = true;
				scope.$digest();
				expect(element.find('table').length).toBe(1);

				// when
				element.find('.modal-body button').click();
				scope.$digest();

				// then
				expect(element.find('table').length).toBe(0);
			});
		});
	});
});
