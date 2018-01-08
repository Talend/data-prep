/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('navigationList directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    var list = [
        { label: 'us-customers-500' },
        { label: 'dates' },
        { label: 'exponential' },
    ];

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile, $timeout) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<navigation-list ' +
                'list="list"' +
                'on-click="trigger(item)"' +
                'selected-item="item"' +
                'get-label="getLabelCb(item)"' +
                '></navigation-list>'
            );
            scope.list = list;
            scope.item = list[0];
            scope.getLabelCb = function (item) {
                return item.label;
            };

            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render items list', function () {
        //when
        createElement();

        //then
        expect(element.find('.back-forth-btn div').length).toBe(2);
        expect(element.find('.item-label').length).toBe(3);
        expect(element.find('.item-label').eq(0).text().trim()).toBe('us-customers-500');
        expect(element.find('.item-label').eq(1).text().trim()).toBe('dates');
        expect(element.find('.item-label').eq(2).text().trim()).toBe('exponential');
        expect(element.find('.selected-item-label').length).toBe(1);
        expect(element.find('.selected-item-label').eq(0).text().trim()).toBe('us-customers-500');
    });

    it('should trigger item selection callback', function () {
        //given
        scope.trigger = jasmine.createSpy('clickCb');
        createElement();

        //when
        element.find('.item-label').eq(1).click();

        //then
        expect(scope.trigger).toHaveBeenCalledWith(list[1]);
    });
});
