/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Tooltip widget directive', function () {
    'use strict';
    var scope;
    var element;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile, $window) {
        scope = $rootScope.$new();
        element = angular.element('<talend-tooltip position="position" requested-state="showTooltip">Toto aux toilettes <textarea></textarea></talend-tooltip>');
        $compile(element)(scope);
        scope.$digest();

        angular.element('body').append(element);
        $window.innerWidth = 1920;
        $window.innerHeight = 1080;
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should display tooltip with the right content', function () {
        //given
        scope.showTooltip = true;

        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);
        expect(element.find('.talend-tooltip').text().trim()).toBe('Toto aux toilettes');
    });

    it('should set the position when requested position is on the left', function () {
        //given
        scope.position = { x: 120, y: 300 };

        expect(element.find('.talend-tooltip')[0].style.left).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.right).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.left).toBe('120px');
        expect(element.find('.talend-tooltip')[0].style.right).toBe('auto');
    });

    it('should set the position when requested position is on the right', function () {
        //given
        scope.position = { x: 1900, y: 300 };

        expect(element.find('.talend-tooltip')[0].style.left).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.right).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.left).toBe('auto');
        expect(element.find('.talend-tooltip')[0].style.right).toBe('20px');
    });

    it('should set the position when requested position is on the top', function () {
        //given
        scope.position = { x: 120, y: 300 };

        expect(element.find('.talend-tooltip')[0].style.top).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.bottom).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.top).toBe('300px');
        expect(element.find('.talend-tooltip')[0].style.bottom).toBe('auto');
    });

    it('should set the position when requested position is on the bottom', function () {
        //given
        scope.position = { x: 120, y: 1000 };

        expect(element.find('.talend-tooltip')[0].style.top).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.bottom).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.top).toBe('auto');
        expect(element.find('.talend-tooltip')[0].style.bottom).toBe('80px');
    });

    it('should get documentElement client size if window inner size is not available', inject(function ($window, $document) {
        //given
        $window.innerWidth = undefined;
        $window.innerHeight = undefined;

        $document.documentElement = { clientWidth: 1920, clientHeight: 1080 };

        scope.position = { x: 120, y: 300 };

        expect(element.find('.talend-tooltip')[0].style.left).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.right).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.left).toBe('120px');
        expect(element.find('.talend-tooltip')[0].style.right).toBe('auto');
    }));

    it('should get body client size if window inner size and document body size are not available', inject(function ($window, $document) {
        //given
        $window.innerWidth = undefined;
        $window.innerHeight = undefined;
        $document.documentElement = { clientWidth: undefined, clientHeight: undefined };

        $document.body = { clientWidth: 1920, clientHeight: 1080 };

        scope.position = { x: 120, y: 300 };

        expect(element.find('.talend-tooltip')[0].style.left).toBeFalsy();
        expect(element.find('.talend-tooltip')[0].style.right).toBeFalsy();

        //when
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip')[0].style.left).toBe('120px');
        expect(element.find('.talend-tooltip')[0].style.right).toBe('auto');
    }));

    it('should block visibility change when mouse is over tooltip', function () {
        //given
        var event = angular.element.Event('mouseenter');
        element.trigger(event);

        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        //when
        scope.showTooltip = true;
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);
    });

    it('should unblock and update visibility change when mouse is not over anymore', inject(function ($timeout) {
        //given
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        var eventEnter = angular.element.Event('mouseenter');
        element.trigger(eventEnter);

        scope.showTooltip = true;
        scope.$digest();
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        //when
        var eventLeave = angular.element.Event('mouseleave');
        element.trigger(eventLeave);
        $timeout.flush();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);
    }));

    it('should block visibility change when content is focused', function () {
        //given
        scope.showTooltip = true;
        scope.$digest();

        element.find('textarea').eq(0).focus();
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);

        //when
        scope.showTooltip = false;
        scope.$digest();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);
    });

    it('should unblock and update visibility change when content is unfocused', inject(function ($timeout) {
        //given
        scope.showTooltip = true;
        scope.$digest();

        element.find('textarea').eq(0).focus();

        scope.showTooltip = false;
        scope.$digest();
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);

        //when
        element.find('textarea').eq(0).focusout();
        $timeout.flush();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);
    }));

    it('should unblock and update visibility change when mouse is not over anymore', inject(function ($timeout) {
        //given
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        var eventEnter = angular.element.Event('mouseenter');
        element.trigger(eventEnter);

        scope.showTooltip = true;
        scope.$digest();
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(true);

        //when
        var eventLeave = angular.element.Event('mouseleave');
        element.trigger(eventLeave);
        $timeout.flush();

        //then
        expect(element.find('.talend-tooltip').hasClass('ng-hide')).toBe(false);
    }));
});
