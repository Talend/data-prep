describe('Add reset button to input directive', function() {
    'use strict';

    var scope, createElement, element;
    var body = angular.element('body');
    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.value = '';
        createElement = function() {
            element = angular.element('<div><input ng-model="value" add-reset-button-to-input></div>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render input', function() {
        //when
        createElement();

        //then
        expect(element.find('div').length).toBe(3);
        expect(element.find('img').length).toBe(2);

        expect(element.find('div').eq(1).css('display')).toEqual('none');
        expect(element.find('div').eq(2).css('display')).toEqual('block');

    });

    it('should switch icon when text field is not empty', function() {
        //when
        createElement();
        scope.value = 'test';
        scope.$digest();

        //then
        expect(element.find('div').eq(1).css('display')).toEqual('block');
        expect(element.find('div').eq(2).css('display')).toEqual('none');

    });

    it('should empty text field when clicking on clear icon', function() {
        //given
        createElement();
        scope.value = 'test';
        scope.$digest();

        //when
        element.find('div').eq(1).click();
        scope.$digest();

        //then
        expect(scope.value).toEqual('');
    });
});