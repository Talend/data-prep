describe('Transform column param controller', function () {
    'use strict';

    var createController, scope;
    var parameter;
    var stateMock;

    beforeEach(module('data-prep.transformation-form', function ($provide) {
        var columns =  [
            {id: '0001', name: 'first name'},
            {id: '0002', name: 'last name'},
            {id: '0003', name: 'birth date'}
        ];
        stateMock = {
            playground: {
                // available dataset/preparation columns
                data: {
                    columns: columns
                },
                grid: {
                    // set the selected column to the first one
                    selectedColumn: columns[0]
                }
            }
        };

        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller) {
        parameter = {};
        scope = $rootScope.$new();

        createController = function () {
            var ctrlFn = $controller('TransformColumnParamCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should remove current column', function () {
        // when
        var ctrl = createController();

        // then
        expect(ctrl.columns.length).toBe(2);
        expect(ctrl.columns[0]).toBe(stateMock.playground.data.columns[1]);
        expect(ctrl.columns[1]).toBe(stateMock.playground.data.columns[2]);

    });

    it('should set selected value to first column', function () {
        // when
        var ctrl = createController();

        // then
        expect(ctrl.parameter.value).toBe(stateMock.playground.data.columns[1].id);
    });

    it('should NOT set selected value when there is no columns', function () {
        //given
        stateMock.playground.data.columns = [];

        // when
        var ctrl = createController();

        // then
        expect(ctrl.parameter.value).toBeFalsy();
    });
});