/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from './../../../i18n/en';

describe('About component', () => {
	let scope;
	let element;
	let createElement;
	let stateMock;

	beforeEach(
		angular.mock.module('data-prep.about', $provide => {
			stateMock = {
				home: {
					about: {
						isVisible: false,
					},
				},
			};
			$provide.constant('state', stateMock);
		}),
	);

	beforeEach(
		angular.mock.module('pascalprecht.translate', $translateProvider => {
			$translateProvider.translations('en', i18n);
			$translateProvider.preferredLanguage('en');
		}),
	);

	beforeEach(
		inject(($q, $rootScope, $compile, AboutService) => {
			scope = $rootScope.$new(true);

			spyOn(AboutService, 'loadBuilds').and.returnValue($q.when([]));

			createElement = () => {
				element = angular.element(`<about></about>`);
				$compile(element)(scope);
				scope.$digest();
			};
		}),
	);

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('render', () => {
		it('should render about modal', () => {
			createElement();

			expect(element.find('pure-about-dialog').length).toBe(1);
		});
	});
});
