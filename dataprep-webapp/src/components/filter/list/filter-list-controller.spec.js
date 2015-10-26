describe('filter list controller', function () {
    'use strict';

    var createController, scope;
    var onFilterChange, onFilterRemove;

    beforeEach(module('data-prep.filter-list'));

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
                value: 'toto'
            }
        };
        var value = 'tata';

        //when
        ctrl.changeFilter(filter, value);

        //then
        expect(onFilterChange).toHaveBeenCalledWith({
            filter: filter,
            value: value
        });
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