/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Stats-details controller', function () {
    'use strict';

    var createController, scope, stateMock;

    beforeEach(angular.mock.module('data-prep.stats-details', function($provide) {
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
        expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('matches', '0001', 'firstname', {
            patterns: [
                {
                    value: 'Aa9'
                }
            ]
        }, null, null);
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
        expect(FilterService.addFilterAndDigest).toHaveBeenCalledWith('empty_records', '0001', 'firstname', null, null, null);
    }));

});