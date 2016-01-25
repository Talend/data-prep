describe('Editable regex widget directive', function() {
    'use strict';

    var scope, element, createElement;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'EQUALS': 'Equals',
            'CONTAINS': 'Contains',
            'STARTS_WITH': 'Starts With',
            'ENDS_WITH': 'Ends With',
            'REGEX': 'RegEx'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<form name="myTestForm">' +
                '   <talend-editable-regex ng-model="value"></talend-editable-regex>' +
                '</form>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    describe('init', function() {
        it('should render regex types', function() {
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

        it('should render regex input', function() {
            //when
            createElement();

            //then
            expect(element.find('input').length).toBe(1);
        });
    });

    describe('trim', function() {
        it('should not trim input content', function() {
            //then
            expect(element.find('input').attr('ng-trim')).toBe('false');
        });
    });
});