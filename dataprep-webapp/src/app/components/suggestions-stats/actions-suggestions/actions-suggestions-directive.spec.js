/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const i18n = {
	COLON: ': ',
};

describe('Actions suggestions-stats directive', () => {
	'use strict';

	let scope;
	let element;
	let createElement;
	let stateMock;
	const body = angular.element('body');
	beforeEach(angular.mock.module('data-prep.actions-suggestions', ($provide) => {
		stateMock = {
			playground: {
				suggestions: {},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();
		createElement = () => {
			element = angular.element('<actions-suggestions></actions-suggestions>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render ghost when actions are being fetched', inject(() => {
		//given
		stateMock.playground.suggestions.isLoading = true;

		//when
		createElement();

		//then
		const elm = element.find('#actions-ghost');
		expect(elm.length).toBe(1);
	}));
});
