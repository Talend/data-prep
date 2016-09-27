/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


describe('Auto-scroll directive', function () {
    'use strict';

    let scope, element, createElement, body;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($timeout, $rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.eventClientY = 0;
        scope.isDragStart = false;
        createElement = (scope) => {
            element = angular.element(`<div style="height: 200px; overflow-y:auto;"
                auto-scroll
                bottom-delta="0"
                dnd-position="eventClientY"
                scroll-step="10"
                top-delta="0"
                while-dragging="isDragStart">
                <ul>
                    <li style="height: 50px;">1</li>
                    <li style="height: 50px;">2</li>
                    <li style="height: 50px;">3</li>
                    <li style="height: 50px;">4</li>
                    <li style="height: 50px;">5</li>
                    <li style="height: 50px;">6</li>
                    <li style="height: 50px;">7</li>
                    <li style="height: 50px;">8</li>
                    <li style="height: 50px;">9</li>
                </ul>
            </div>`);
            angular.element('body').append(element);
            $compile(element)(scope);
            scope.$digest();
            return element;
        }
    }));

    beforeEach(function () {
        jasmine.clock().install();
    });

    afterEach(function () {
        jasmine.clock().uninstall();
        scope.$destroy();
        element.remove();
    });

    it('should scroll down of 1 scroll step', () => {
        //given
        scope.isDragStart = true;
        scope.eventClientY = 500;

        //when
        createElement(scope);
        scope.$digest();
        expect(element.scrollTop()).toBe(0);
        jasmine.clock().tick(100);

        scope.isDragStart = false;// to clear the current interval
        scope.$digest();

        //then
        expect(element.scrollTop()).toBe(10);
    });

    it('should scroll down of 2 scroll steps', () => {
        //given
        scope.isDragStart = true;
        scope.eventClientY = 500;

        //when
        createElement(scope);
        scope.$digest();
        expect(element.scrollTop()).toBe(0);
        jasmine.clock().tick(200); // 100 * 2

        scope.isDragStart = false;// to clear the current interval
        scope.$digest();

        //then
        expect(element.scrollTop()).toBe(20); //10 * 2
    });

    it('should scroll top after a scroll down', () => {
        //given
        createElement(scope);
        scope.$digest();
        element.scrollTop(50);
        expect(element.scrollTop()).toBe(50);

        //when
        scope.isDragStart = true;
        scope.eventClientY = -20;
        scope.$digest();
        jasmine.clock().tick(200); // 100 * 2

        scope.isDragStart = false;// to clear the current interval
        scope.$digest();

        //then
        expect(element.scrollTop()).toBe(30);// 50 - (10 * 2)
    });
});
