/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation date param directive', () => {
    'use strict';
    let scope;
    let createElement;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            let element = angular.element('<transform-date-param parameter="parameter" is-readonly="isReadonly"></transform-date-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with parameter', () => {
        //given
        scope.parameter = {
            name: 'param1',
            label: 'Param 1',
            type: 'date',
            default: '02/01/2012 10:00:12',
        };

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.datetimepicker').val()).toBe('02/01/2012 10:00:12');
    });

    it('should render an action with parameter in readonly mode', () => {
        //given
        scope.parameter = {
            name: 'param1',
            label: 'Param 1',
            type: 'date',
            default: '02/01/2012 10:00:12',
            value: '02/01/2012 10:00:12',
        };
        scope.isReadonly = true;

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.param-input-label').text().trim()).toBe('02/01/2012 10:00:12');
    });
});
