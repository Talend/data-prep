/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Editable select directive', function () {
    'use strict';

    var scope;
    var createElement;
    var element;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function () {
            element = angular.element('<editable-select list="selectValues" ng-model="value"></editable-select>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function () {
        scope.$destroy();
        element.remove();
    });

    it('should render input and combobox', function () {
        //given
        scope.selectValues = ['val1', 'val2', 'val3'];
        scope.value = 'val2';

        //when
        createElement();

        //then
        expect(element.find('input[type="text"]').length).toBe(1);
        expect(element.find('select').length).toBe(1);

        var select = element.find('select').eq(0)[0];
        expect(select.options.length).toBe(3);
        expect(select.options[0].value).toBe('string:val1');
        expect(select.options[1].value).toBe('string:val2');
        expect(select.options[2].value).toBe('string:val3');
    });

    it('should add the selected value in the input and reset the select', function () {
        //given
        scope.selectValues = ['val1', 'val2', 'val3'];

        //when
        createElement();

        var select = element.find('select').eq(0);
        var input = element.find('input').eq(0);

        select.prop('selectedIndex', 2);
        select.triggerHandler('change');
        scope.$digest();

        //then
        expect(input.val()).toBe('val2');
        expect(select.val()).toBeNull();
    });
});
