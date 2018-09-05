/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from './../../../i18n/en';

describe('Breadcrumb component', () => {
	let scope;
	let element;
	let createElement;
	let stateMock;
	let controller;

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
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile, AboutService) => {
		scope = $rootScope.$new(true);
		spyOn(AboutService, 'loadBuilds').and.returnValue();

		createElement = () => {
			const html = `<pure-about-dialog></pure-about-dialog>`;
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
			stateMock.home.about.isVisible = false;
			createElement();
			expect(element.find('talend-modal').length).toBe(0);

			stateMock.home.about.isVisible = true;
			scope.$digest();

			expect(element.find('pure-about-dialog').length).toBe(1);
		});
	});
});
