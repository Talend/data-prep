describe('Transform Regex param controller', function () {
    'use strict';

    var createController, scope, parameter;

    beforeEach(module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformRegexParamCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should init value with default value when there is no defined value', function () {
        //given
        parameter = {name: 'param1', type: 'regex', default: 'azerty'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('azerty');
    });

    it('should NOT init value when it is defined', function () {
        //given
        parameter = {name: 'param1', type: 'regex', value: 'qwerty', default: 'azerty'};

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('qwerty');
    });
});