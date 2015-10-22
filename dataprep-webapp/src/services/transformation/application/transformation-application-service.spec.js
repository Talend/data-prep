/*jshint camelcase: false */

describe('Transformation Application Service', function () {
    'use strict';
    var stateMock;

    beforeEach(module('data-prep.services.transformation', function ($provide) {
        stateMock = {playground: {}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($q, PlaygroundService) {
        spyOn(PlaygroundService, 'appendStep').and.returnValue();
    }));

    describe('Append Step', function () {
        it('should call appendStep', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            var params = {param: 'value'};
            stateMock.playground.column = {id: '0001', name: 'firstname'};

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
            stateMock.playground.column = {id: '0001', name: 'firstname'};

            //when
            TransformationApplicationService.append(transformation, scope);

            //then
            var expectedParams = {
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname'
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));

        it('should create an append closure', inject(function (TransformationApplicationService, PlaygroundService) {
            //given
            var transformation = {name: 'tolowercase'};
            var scope = 'column';
            var params = {param: 'value'};
            stateMock.playground.column = {id: '0001', name: 'firstname'};

            //when
            var closure = TransformationApplicationService.appendClosure(transformation, scope);
            closure(params);

            //then
            var expectedParams = {
                param: 'value',
                scope: 'column',
                column_id: '0001',
                column_name: 'firstname'
            };
            expect(PlaygroundService.appendStep).toHaveBeenCalledWith('tolowercase', expectedParams);
        }));
    });
});