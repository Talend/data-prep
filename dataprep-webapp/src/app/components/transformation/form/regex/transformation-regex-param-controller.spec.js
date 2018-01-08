/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform Regex param controller', function () {
    'use strict';

    var createController;
    var scope;
    var parameter;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformRegexParamCtrl', {
                $scope: scope,
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should init value with default value when there is no defined value', function () {
        //given
        parameter = { name: 'param1', type: 'regex', default: 'azerty' };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('azerty');
    });

    it('should NOT init value when it is defined', function () {
        //given
        parameter = { name: 'param1', type: 'regex', value: 'qwerty', default: 'azerty' };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('qwerty');
    });
});
