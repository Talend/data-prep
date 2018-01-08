/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform simple param controller', () => {
    'use strict';

    let createController;
    let scope;
    let parameter;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();

        createController = () => {
            var ctrlFn = $controller('TransformSimpleParamCtrl', {
                $scope: scope,
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should set numeric default value', () => {
        //given
        parameter = { name: 'param1', type: 'numeric', default: '5' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5);
    });

    it('should set integer default value', () => {
        //given
        parameter = { name: 'param1', type: 'integer', default: '5' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5);
    });

    it('should set double default value', () => {
        //given
        parameter = { name: 'param1', type: 'double', default: '5.1' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5.1);
    });

    it('should set float default value', () => {
        //given
        parameter = { name: 'param1', type: 'float', default: '5.1' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5.1);
    });

    it('should set boolean true default value', () => {
        //given
        parameter = { name: 'param1', type: 'boolean', default: 'true' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(true);
    });

    it('should set boolean true default value (with boolean default)', () => {
        //given
        parameter = { name: 'param1', type: 'boolean', default: true };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(true);
    });

    it('should set boolean false default value', () => {
        //given
        parameter = { name: 'param1', type: 'boolean', default: 'false' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(false);
    });

    it('should set 0 value if default value is not numeric with numeric type', () => {
        //given
        parameter = { name: 'param1', type: 'numeric', default: 'a' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(0);
    });

    it('should not set default value if no default value is null', () => {
        //given
        parameter = { name: 'param1', type: 'text', default: null };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBeUndefined();
    });

    it('should not set value if no default value is provided', () => {
        //given
        parameter = { name: 'param1', type: 'text' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBeUndefined();
    });

    it('should init params default values', () => {
        //given
        parameter = { name: 'param1', type: 'text', default: 'param1Value' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('param1Value');
    });

    it('should init params with values values instead of default when available', () => {
        //given
        parameter = { name: 'param1', type: 'text', value: 'my value', default: 'param1Value' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('my value');
    });

    it('should set initial value type', () => {
        //given
        parameter = { name: 'param2', type: 'integer', value: '12', initialValue: '3', default: '5' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.parameter.initialValue).toBe(3);
    });

    it('should check if it is boolean type', () => {
        //given
        parameter = { name: 'param1', type: 'boolean', default: 'false' };

        //when
        const ctrl = createController();

        //then
        expect(ctrl.isBooleanType()).toBe(true);
    });
});
