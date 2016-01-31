'use strict';

describe('Button Loader directive', function () {
    var scope, element, createElement;

    beforeEach(module('talend.widget'));
    beforeEach(module('htmlTemplates'));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            var html =
                '<talend-button-loader' +
                '   button-class="{{buttonClass}}" ' +
                '   disable-condition="disabled" ' +
                '   loading="loading" ' +
                '   loading-class="{{loadingClass}}">Submit</talend-button-loader>';
            element = $compile(html)(scope);
            scope.$digest();
        };
    }));

    it('should render button with text transclusion', function () {
        //when
        createElement();

        //then
        expect(element.find('button').eq(0).text().trim()).toBe('Submit');
        expect(element.find('.talend-button-loader-icon').length).toBe(0);
    });

    it('should render loading icon', function () {
        //given
        scope.loading = true;

        //when
        createElement();

        //then
        expect(element.find('button').eq(0).text().trim()).not.toBe('Submit');
        expect(element.find('.talend-button-loader-icon').length).toBe(1);
    });

    it('should set button class', function () {
        //given
        scope.buttonClass = 'my-button-class my-second-button-class';

        //when
        createElement();

        //then
        expect(element.find('button').eq(0).hasClass('my-button-class')).toBe(true);
        expect(element.find('button').eq(0).hasClass('my-second-button-class')).toBe(true);
    });

    it('should set loader class', function () {
        //given
        scope.loadingClass = 'my-loading-class my-second-loading-class';
        scope.loading = true;

        //when
        createElement();

        //then
        expect(element.find('i').eq(0).hasClass('my-loading-class')).toBe(true);
        expect(element.find('i').eq(0).hasClass('my-second-loading-class')).toBe(true);
    });

    it('should disable button', function () {
        //given
        scope.disabled = false;
        createElement();
        expect(element.find('button').eq(0).attr('disabled')).toBeFalsy();

        //when
        scope.disabled = true;
        scope.$digest();

        //then
        expect(element.find('button').eq(0).attr('disabled')).toBeTruthy();
    });
});
