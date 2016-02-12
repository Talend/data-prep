/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Editable select directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            element = angular.element('<editable-select list="selectValues" ng-model="value"></editable-select>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    it('should render input and combobox', function() {
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
});