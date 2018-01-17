/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Editable regex widget directive', () => {
    'use strict';

    let scope;
    let element;
    let createElement;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            EQUALS: 'Equals',
            CONTAINS: 'Contains',
            STARTS_WITH: 'Starts With',
            ENDS_WITH: 'Ends With',
            REGEX: 'RegEx',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<form name="myTestForm">' +
                '<talend-editable-regex ng-model="value" is-readonly="isReadonly"></talend-editable-regex>' +
                '</form>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('init', () => {
        it('should render regex types', () => {
            //when
            createElement();

            //then
            expect(element.find('.dropdown-menu > li').length).toBe(5);
            expect(element.find('.dropdown-menu > li > .regex-type-item-key').eq(0).text()).toBe('=');
            expect(element.find('.dropdown-menu > li > .regex-type-item-label').eq(0).text()).toBe('Equals');
            expect(element.find('.dropdown-menu > li > .regex-type-item-key').eq(1).text()).toBe('≅');
            expect(element.find('.dropdown-menu > li > .regex-type-item-label').eq(1).text()).toBe('Contains');
            expect(element.find('.dropdown-menu > li > .regex-type-item-key').eq(2).text()).toBe('>');
            expect(element.find('.dropdown-menu > li > .regex-type-item-label').eq(2).text()).toBe('Starts With');
            expect(element.find('.dropdown-menu > li > .regex-type-item-key').eq(3).text()).toBe('<');
            expect(element.find('.dropdown-menu > li > .regex-type-item-label').eq(3).text()).toBe('Ends With');
            expect(element.find('.dropdown-menu > li > .regex-type-item-key').eq(4).text()).toBe('^\\');
            expect(element.find('.dropdown-menu > li > .regex-type-item-label').eq(4).text()).toBe('RegEx');
        });

        it('should render regex types in readonly mode', () => {
            //given
            scope.isReadonly = true;
            scope.value = {
                token: 't',
                operator: 'starts_with',
            };

            //when
            createElement();

            //then
            expect(element.find('.editable-regex > div > span').length).toBe(2);
            expect(element.find('.editable-regex > div > span').eq(0).text().trim()).toBe('>');
            expect(element.find('.editable-regex > div > span').eq(1).text().trim()).toBe('t');
        });

        it('should render regex input', () => {
            //when
            createElement();

            //then
            expect(element.find('input').length).toBe(1);
        });
    });

    describe('trim', () => {
        it('should not trim input content', () => {
            //then
            expect(element.find('input').attr('ng-trim')).toBe('false');
        });
    });
});
