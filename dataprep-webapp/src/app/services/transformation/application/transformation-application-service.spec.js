describe('Transformation Application Service', function () {
    'use strict';
    var stateMock;

    beforeEach(angular.mock.module('data-prep.services.transformation', function ($provide) {
        stateMock = {playground: {grid: {}}};
        stateMock.playground.filter = {
            applyTransformationOnFilters: true,
            gridFilters: [88]
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, PlaygroundService, FilterAdapterService) {
        spyOn(PlaygroundService, 'appendStep').and.returnValue();
        spyOn(FilterAdapterService, 'toTree').and.returnValue({
            filter:{
                eq:{
                    field:'0001',
                    value:'john'
                }
            }
        });
    }));

    describe('Append Step', function () {
        it('should call appendStep with column', inject(function (TransformationApplicationService, PlaygroundService) {
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
                column_name: 'firstname',
                row_id: undefined
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should call appendStep with row', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'line';
            var params = {param: 'value'};
            stateMock.playground.grid.selectedLine = {tdpId: 125};
            stateMock.playground.filter.applyTransformationOnFilters = false;

            //when
            TransformationApplicationService.append(transformation, scope, params);

            //then
            var expectedParams = {
                param: 'value',
                scope: 'line',
                column_id: undefined,
                column_name: undefined,
                row_id: 125
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should call appendStep without param', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            stateMock.playground.grid.selectedColumn = {id: '0001', name: 'firstname'};
            stateMock.playground.filter.applyTransformationOnFilters = false;

            //when
            TransformationApplicationService.append(transformation, scope);

            //then
            var expectedParams = {
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                row_id: undefined
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should call appendStep with filter', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            stateMock.playground.grid.selectedColumn = {id: '0001', name: 'firstname'};
            stateMock.playground.filter.applyTransformationOnFilters = true;

            //when
            TransformationApplicationService.append(transformation, scope);

            //then
            var expectedParams = {
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                row_id: undefined,
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
            stateMock.playground.grid.selectedLine = {tdpId: 125};
            stateMock.playground.filter.applyTransformationOnFilters = false;

            //when
            var closure = TransformationApplicationService.appendClosure(transformation, scope);
            closure(params);

            //then
            var expectedParams = {
                param: 'value',
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname',
                row_id: 125
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));
    });
});