/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Slidable directive', function () {
    'use strict';

    var scope;
    var element;
    var createElement;
    var createResizableElement;
    var resizableId = 'resizableId';
    var widthStorageKey = 'org.talend.dataprep.' + resizableId + '.width';

    beforeEach(angular.mock.module('talend.widget'));

    afterEach(inject(function ($window) {
        scope.$destroy();
        element.remove();

        $window.localStorage.removeItem(widthStorageKey);
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function (side) {
            var template = '<talend-slidable visible="visible" side="' + side + '">' +
                '   Content' +
                '</talend-slidable>';
            element = $compile(template)(scope);
            scope.$digest();
        };

        createResizableElement = function (side) {
            var template = '<talend-slidable visible="visible" side="' + side + '" resizable="' + resizableId + '">' +
                '   Content' +
                '</talend-slidable>';
            element = $compile(template)(scope);
            scope.$digest();
        };
    }));

    it('should hide slidable on creation', function () {
        //given
        scope.visible = false;

        //when
        createElement();

        //then
        expect(element.hasClass('slide-hide')).toBe(true);
    });

    it('should show slidable on creation', function () {
        //given
        scope.visible = true;

        //when
        createElement();

        //then
        expect(element.hasClass('slide-hide')).toBe(false);
    });

    it('should hide slidable on action click', function () {
        //given
        scope.visible = true;
        createElement();
        expect(element.hasClass('slide-hide')).toBe(false);

        //when
        element.find('.action').eq(0).click();

        //then
        expect(element.hasClass('slide-hide')).toBe(true);
    });

    it('should show slidable on action click', function () {
        //given
        scope.visible = false;
        createElement();
        expect(element.hasClass('slide-hide')).toBe(true);

        //when
        element.find('.action').eq(0).click();

        //then
        expect(element.hasClass('slide-hide')).toBe(false);
    });

    it('should show ' > ' when left slidable is hidden', function () {
        //given
        scope.visible = false;

        //when
        createElement('left');

        //then
        var actionOnlySpan = element.find('.action').eq(0).find('span').eq(0);
        var displayedActionText = actionOnlySpan.find('span').not('.ng-hide').eq(0).text();
        var hiddenActionText = actionOnlySpan.find('span.ng-hide').eq(0).text();
        expect(displayedActionText).toBe('›');
        expect(hiddenActionText).toBe('‹');
    });

    it('should show ' < ' when left slidable is displayed', function () {
        //given
        scope.visible = true;

        //when
        createElement('left');

        //then
        var actionOnlySpan = element.find('.action').eq(0).find('span').eq(0);
        var displayedActionText = actionOnlySpan.find('span').not('.ng-hide').eq(0).text();
        var hiddenActionText = actionOnlySpan.find('span.ng-hide').eq(0).text();
        expect(displayedActionText).toBe('‹');
        expect(hiddenActionText).toBe('›');
    });

    it('should show ' < ' when right slidable is hidden', function () {
        //given
        scope.visible = false;

        //when
        createElement('right');

        //then
        var actionOnlySpan = element.find('.action').eq(0).find('span').eq(0);
        var displayedActionText = actionOnlySpan.find('span').not('.ng-hide').eq(0).text();
        var hiddenActionText = actionOnlySpan.find('span.ng-hide').eq(0).text();
        expect(displayedActionText).toBe('‹');
        expect(hiddenActionText).toBe('›');
    });

    it('should show ' > ' when right slidable is displayed', function () {
        //given
        scope.visible = true;

        //when
        createElement('right');

        //then
        var actionOnlySpan = element.find('.action').eq(0).find('span').eq(0);
        var displayedActionText = actionOnlySpan.find('span').not('.ng-hide').eq(0).text();
        var hiddenActionText = actionOnlySpan.find('span.ng-hide').eq(0).text();
        expect(displayedActionText).toBe('›');
        expect(hiddenActionText).toBe('‹');
    });

    it('should set and configure resize feature on left slidable', function () {
        //when
        createResizableElement('left');

        //then
        expect(element.find('.ui-resizable-handle').length).toBe(1);
        expect(element.find('.ui-resizable-handle').eq(0).hasClass('ui-resizable-e')).toBe(true);
    });

    it('should set and configure resize feature on right slidable', function () {
        //when
        createResizableElement('right');

        //then
        expect(element.find('.ui-resizable-handle').length).toBe(1);
        expect(element.find('.ui-resizable-handle').eq(0).hasClass('ui-resizable-w')).toBe(true);
    });

    it('should set flex constant size on slidable creation if there is a size in localStorage', inject(function ($window) {
        //given
        $window.localStorage.setItem(widthStorageKey, '500px');
        scope.visible = false;

        //when
        createResizableElement('right');

        //then
        const elementStyle = element[0].style;
        const flexStyle = elementStyle['flex'] || elementStyle['-webkit-flex'];
        expect(flexStyle).toBe('0 0 500px');
    }));

    it('should remove transition when resize start', function () {
        //given
        createResizableElement('right');
        var start = element.resizable('option', 'start');
        expect(element.hasClass('no-transition')).toBe(false);

        //when
        start();

        //then
        expect(element.hasClass('no-transition')).toBe(true);
    });

    it('should set transition again and save width to localstorage, when resize stop', inject(function ($window) {
        //given
        createResizableElement('right');
        var stop = element.resizable('option', 'stop');

        element.addClass('no-transition');
        expect($window.localStorage.getItem(widthStorageKey)).toBeFalsy();

        //when
        stop(null, { size: { width: 250 } });
        scope.$digest();

        //then
        expect(element.hasClass('no-transition')).toBe(false);
        expect($window.localStorage.getItem(widthStorageKey)).toBe('250px');
    }));

    it('should set element css to fit wanted size on resize action', function () {
        //given
        createResizableElement('right');
        var resize = element.resizable('option', 'resize');

        expect(element[0].style.flex).toBeFalsy();

        //when
        resize(null, { size: { width: 250 } });
        scope.$digest();

        //then
        const elementStyle = element[0].style;
        const flexStyle = elementStyle['flex'] || elementStyle['-webkit-flex'];
        expect(flexStyle).toBe('0 0 250px');
    });
});
