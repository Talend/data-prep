describe('Transform simple param controller', function () {
    'use strict';

    var createController, scope, parameter;

    beforeEach(module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformSimpleParamCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should set numeric default value', function () {
        //given
        parameter = {name: 'param1', type: 'numeric', default: '5'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5);
    });

    it('should set integer default value', function () {
        //given
        parameter = {name: 'param1', type: 'integer', default: '5'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5);
    });

    it('should set double default value', function () {
        //given
        parameter = {name: 'param1', type: 'double', default: '5.1'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5.1);
    });

    it('should set float default value', function () {
        //given
        parameter = {name: 'param1', type: 'float', default: '5.1'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(5.1);
    });

    it('should set boolean true default value', function () {
        //given
        parameter = {name: 'param1', type: 'boolean', default: 'true'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(true);
    });

    it('should set boolean true default value (with boolean default)', function () {
        //given
        parameter = {name: 'param1', type: 'boolean', default: true};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(true);
    });

    it('should set boolean false default value', function () {
        //given
        parameter = {name: 'param1', type: 'boolean', default: 'false'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(false);
    });

    it('should set 0 value if default value is not numeric with numeric type', function () {
        //given
        parameter = {name: 'param1', type: 'numeric', default: 'a'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe(0);
    });

    it('should not set default value if no default value is null', function () {
        //given
        parameter = {name: 'param1', type: 'text', default: null};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBeUndefined();
    });

    it('should not set value if no default value is provided', function () {
        //given
        parameter = {name: 'param1', type: 'text'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBeUndefined();
    });

    it('should init params default values', function () {
        //given
        parameter = {name: 'param1', type: 'text', default: 'param1Value'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('param1Value');
    });

    it('should init params with values values instead of default when available', function () {
        //given
        parameter = {name: 'param1', type: 'text', value: 'my value', default: 'param1Value'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('my value');
    });

    it('should set initial value type', function () {
        //given
        parameter = {name: 'param2', type: 'integer', value: '12', initialValue: '3', default: '5'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.initialValue).toBe(3);
    });
});