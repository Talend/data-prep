/*jshint camelcase: false */

describe('Transform params controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.transformation-form'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('TransformParamsCtrl', {
                $scope: scope
            });
        };
    }));

    it('should get the correct parameter type', function() {
        // given / when
        var ctrl = createController();

        //then
        expect(ctrl.getParameterType({type: 'numeric'})).toEqual('simple');
        expect(ctrl.getParameterType({type: 'integer'})).toEqual('simple');
        expect(ctrl.getParameterType({type: 'double'})).toEqual('simple');
        expect(ctrl.getParameterType({type: 'float'})).toEqual('simple');
        expect(ctrl.getParameterType({type: 'string'})).toEqual('simple');
        expect(ctrl.getParameterType({type: 'select'})).toEqual('select');
        expect(ctrl.getParameterType({type: 'cluster'})).toEqual('cluster');
        expect(ctrl.getParameterType({type: 'date'})).toEqual('date');
        expect(ctrl.getParameterType({type: 'column'})).toEqual('column');
        expect(ctrl.getParameterType({type: 'regex'})).toEqual('regex');
        expect(ctrl.getParameterType({type: 'toto'})).toEqual('simple');
    });
});