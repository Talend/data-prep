/*jshint camelcase: false */

describe('Transformation Application Service', function () {
    'use strict';
    var stateMock;

    beforeEach(module('data-prep.services.transformation', function ($provide) {
        stateMock = {playground: {grid: {}}};
        stateMock.playground.filter = {
            applyTransformationOnFilters: true,
            gridFilters: [88]
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, PlaygroundService, FilterService) {
        spyOn(PlaygroundService, 'appendStep').and.returnValue();
        spyOn(FilterService, 'convertFiltersArrayToTreeFormat').and.returnValue({
            filter:{
                eq:{
                    field:'0001',
                    value:'john'
                }
            }
        });
    }));

    describe('Append Step', function () {
        it('should call appendStep', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            var params = {param: 'value'};
            stateMock.playground.grid.selectedColumn = {id: '0001', name: 'firstname'};
            stateMock.playground.filter.applyTransformationOnFilters = false;

            //when
            TransformationApplicationService.append(transformation, scope, params);

            //then
            var expectedParams = {
                param: 'value',
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname'
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should call appendStep without param', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            stateMock.playground.grid.selectedColumn = {id: '0001', name: 'firstname'};

            //when
            TransformationApplicationService.append(transformation, scope);

            //then
            var expectedParams = {
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                filter: {
                    eq:{
                        field:'0001',
                        value:'john'
                    }
                }
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should create an append closure', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            var params = {param: 'value'};
            stateMock.playground.grid.selectedColumn = {id: '0001', name: 'firstname'};

            //when
            var closure = TransformationApplicationService.appendClosure(transformation, scope);
            closure(params);

            //then
            var expectedParams = {
                param: 'value',
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                filter: {
                    eq:{
                        field:'0001',
                        value:'john'
                    }
                }
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));
    });
});