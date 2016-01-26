describe('Stats-details controller', function () {
    'use strict';

    var createController, scope, stateMock;

    beforeEach(module('data-prep.stats-details', function($provide) {
        stateMock = {
            playground: {
                grid: {}
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller, FilterService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('StatsDetailsCtrl', {
                $scope: scope
            });
        };
        spyOn(FilterService, 'addFilterAndDigest').and.returnValue();
    }));

    it('should add a new "pattern" filter', inject(function (FilterService) {
        //given
        var ctrl = createController();
        var obj = {'data': 'Ulysse', 'occurrences': 5, pattern:'Aa9'};

        stateMock.playground.grid.selectedColumn = {
            id: '0001',
            name: 'firstname'
        };

        //when
        ctrl.addPatternFilter (obj);

        //then
        expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('matches', '0001', 'firstname', {pattern: 'Aa9'});
    }));

    it('should add a new "empty" filter if pattern is empty', inject(function (FilterService) {
        //given
        var ctrl = createController();
        var obj = {'data': 'Ulysse', 'occurrences': 5};

        stateMock.playground.grid.selectedColumn = {
            id: '0001',
            name: 'firstname'
        };

        //when
        ctrl.addPatternFilter (obj);

        //then
        expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('empty_records', '0001', 'firstname');
    }));

});