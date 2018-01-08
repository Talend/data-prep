/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform column param controller', function () {
    'use strict';

    var createController;
    var scope;
    var parameter;
    var stateMock;

    beforeEach(angular.mock.module('data-prep.transformation-form', function ($provide) {
        var columns =  [
            { id: '0001', name: 'first name' },
            { id: '0002', name: 'last name' },
            { id: '0003', name: 'birth date' },
        ];
        stateMock = {
            playground: {
                // available dataset/preparation columns
                data: {
                    metadata: {
                        columns: columns,
                    },
                },
                grid: {
                    // set the selected column to the first one
                    selectedColumn: columns[0],
                },
            },
        };

        $provide.constant('state', stateMock);
    }));

    describe('with undefined parameter value', function () {
        beforeEach(inject(function ($rootScope, $controller) {
            parameter = {};
            scope = $rootScope.$new();

            createController = function () {
                var ctrlFn = $controller('TransformColumnParamCtrl', {
                    $scope: scope,
                }, true);
                ctrlFn.instance.parameter = parameter;
                return ctrlFn();
            };
        }));

        it('should set selected value to first column when value is undefined', function () {
            // when
            var ctrl = createController();

            // then
            expect(ctrl.parameter.value).toBe(stateMock.playground.data.metadata.columns[0].id);
        });

        it('should NOT set selected value when there is no columns and value does NOT exist', function () {
            //given
            stateMock.playground.data.metadata.columns = [];

            // when
            var ctrl = createController();

            // then
            expect(ctrl.parameter.value).toBeFalsy();
        });
    });

    describe('with defined parameter value', function () {
        beforeEach(inject(function ($rootScope, $controller) {
            parameter = {
                value: '0002',
            };
            scope = $rootScope.$new();

            createController = function () {
                var ctrlFn = $controller('TransformColumnParamCtrl', {
                    $scope: scope,
                }, true);
                ctrlFn.instance.parameter = parameter;
                return ctrlFn();
            };
        }));

        it('should NOT set selected value when value already exists and columns does NOT exist', function () {
            //given
            stateMock.playground.data.metadata.columns = [{ id: '0001', name: 'first name' }];

            // when
            var ctrl = createController();

            // then
            expect(ctrl.parameter.value).toBe('0002');
        });
    });
});
