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
            element = angular.element('<div><input ng-model="value" talend-search-input></div>');
            body.append(element);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render icons', function() {
        //when
        createElement();

        //then
        var icons = element.find('div.search-input-icon');
        expect(icons.length).toBe(2);
        expect(icons.eq(0).find('span[data-icon="d"]').length).toBe(1);
        expect(icons.eq(1).find('span[data-icon="D"]').length).toBe(1);

        expect(icons.eq(0).css('display')).toEqual('none');
        expect(icons.eq(1).css('display')).toEqual('block');

    });

    it('should switch icon when text field is not empty', function() {
        //when
        createElement();
        scope.value = 'test';
        scope.$digest();

        //then
        var icons = element.find('div.search-input-icon');
        expect(icons.eq(0).css('display')).toEqual('block');
        expect(icons.eq(1).css('display')).toEqual('none');
    });

    it('should empty text field when clicking on clear icon', function() {
        //given
        createElement();
        scope.value = 'test';
        scope.$digest();

        //when
        var clearIcons = element.find('div.clear-icon').eq(0);
        clearIcons.click();
        scope.$digest();

        //then
        expect(scope.value).toEqual('');
    });
});