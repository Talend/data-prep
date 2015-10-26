describe('Transform choice params controller', function () {
    'use strict';

    var createController, scope, extractedParams, parameter;

    beforeEach(module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        extractedParams = null;
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformChoiceParamCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should init choice default value', function() {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    {value: 'regex'},
                    {value: 'index'}
                ]
            },
            default: 'index'
        };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('index');
    });

    it('should init choice to first value if there is no default', function() {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    {value: 'regex'},
                    {value: 'index'}
                ]
            }
        };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('regex');
    });

    it('should not change current value if it is set', function() {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    {value: 'regex'},
                    {value: 'index'}
                ]
            },
            value: 'index'
        };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('index');
    });
});