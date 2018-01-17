/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

describe('Button Switch directive', function () {
    var scope;
    var element;
    var createElement;

    var values = [
        { id: 'name', name: 'Name' },
        { id: 'date', name: 'Date' },
        { id: 'owner', name: 'OWNER_SORT' },
    ];

    beforeEach(angular.mock.module('talend.widget'));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            scope.changeAction = jasmine.createSpy('changeAction');
            scope.values = values;

            var html =
            '<talend-button-switch' +
                '   values="values"' +
                '   display-key="name"' +
                '   current-value="currentValue"' +
                '   change-action="changeAction(selected)">' +
                '</talend-button-switch>';
            element = $compile(html)(scope);
            scope.$digest();
        };
    }));

    it('should render text from current value and display key', function () {
        //given
        scope.currentValue = values[1];

        //when
        createElement();

        //then
        expect(element.text().trim()).toBe(values[1].name);
    });

    it('should call action with next value on button click', function () {
        //given
        scope.currentValue = values[0];
        createElement();

        //when
        element.click();

        //then
        expect(scope.changeAction).toHaveBeenCalledWith(values[1]);
    });

    it('should call action with first value when there is no current value', function () {
        //given
        scope.currentValue = null;
        createElement();

        //when
        element.eq(0).click();

        //then
        expect(scope.changeAction).toHaveBeenCalledWith(values[0]);
    });

    it('should call action with first value when current value is the last value', function () {
        //given
        scope.currentValue = values[2];
        createElement();

        //when
        element.eq(0).click();

        //then
        expect(scope.changeAction).toHaveBeenCalledWith(values[0]);
    });
});
