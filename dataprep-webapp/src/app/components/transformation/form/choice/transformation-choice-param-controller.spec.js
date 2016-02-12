/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform choice params controller', function () {
    'use strict';

    var createController, scope, parameter;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
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