/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform date param controller', function () {
    'use strict';

    var createController;
    var scope;
    var parameter;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformDateParamCtrl', {
                $scope: scope,
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should set default if value is not already set', function () {
        //given
        parameter = { name: 'param2', type: 'date', default: '02/01/2012 09:42:22' };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('02/01/2012 09:42:22');
    });

    it('should not set default', function () {
        //given
        parameter = { name: 'param2', type: 'date', default: '02/01/2012 09:42:22', value: '01/01/2015 00:00:00' };

        //when
        var ctrl = createController();

        //then
        expect(ctrl.parameter.value).toBe('01/01/2015 00:00:00');
    });
});
