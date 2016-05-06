/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('filter list controller', function () {
    'use strict';

    var createController, scope;
    var onFilterChange, onFilterRemove;

    beforeEach(angular.mock.module('data-prep.filter-list'));

    beforeEach(inject(function ($rootScope, $controller) {
        onFilterChange = jasmine.createSpy('onFilterChange');
        onFilterRemove = jasmine.createSpy('onFilterRemove');
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('FilterListCtrl', {
                $scope: scope
            });
            ctrl.onFilterChange = onFilterChange;
            ctrl.onFilterRemove = onFilterRemove;
            return ctrl;
        };
    }));

    it('should call filter change callback', function () {
        //given
        var ctrl = createController();
        var filter = {
            column: '0001',
            type: 'contains',
            args: {
                phrase: [
                    {
                        value: 'toto'
                    }
                ]

            }
        };

        //when
        ctrl.changeFilter(filter);

        //then
        expect(onFilterChange).toHaveBeenCalledWith({filter, value: undefined});
    });

    it('should call filter change callback', function () {
        //given
        var ctrl = createController();
        var filter = {
            column: '0001',
            type: 'contains',
            args: {
                value: 'toto'
            }
        };

        //when
        ctrl.removeFilter(filter);

        //then
        expect(onFilterRemove).toHaveBeenCalledWith({
            filter: filter
        });
    });
});