/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

const defaultLang = 'en';
const specificLang = 'fr';

describe('HTML lang directive', () => {
	let element;
	let initLang;
	let createElement;

	beforeEach(angular.mock.module('data-prep.services.utils'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		initLang = (lang = defaultLang) => {
			$translateProvider.translations(lang, {});
			$translateProvider.preferredLanguage(defaultLang);
		};
	}));

	beforeEach(inject(($rootScope, $compile) => {
		createElement = () => {
			element = angular.element('<div html-lang></div>');
			$compile(element)($rootScope.$new());
		};
	}));

	it('should set default lang if no specific lang is provided', inject(($rootScope) => {
		//given
		initLang();
		createElement();

		//when
		$rootScope.$emit('$translateChangeSuccess');
		$rootScope.$digest();

		//then
		expect(element[0].hasAttribute('html-lang')).toBeFalsy();
		expect(element[0].hasAttribute('lang')).toBeTruthy();
		expect(element.attr('lang')).toEqual(defaultLang);
	}));

	it('should set lang if another than the default one is provided', inject(($rootScope) => {
		//given
		initLang(specificLang);
		createElement();

		//when
		$rootScope.$emit('$translateChangeSuccess', { language: specificLang });
		$rootScope.$digest();

		//then
		expect(element[0].hasAttribute('html-lang')).toBeFalsy();
		expect(element[0].hasAttribute('lang')).toBeTruthy();
		expect(element.attr('lang')).toEqual(specificLang);
	}));
});
