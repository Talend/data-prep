describe('Inventory Item controller', function () {
    'use strict';

    var createController, scope, ctrl;

    beforeEach(module('data-prep.inventory-item'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = function () {
            return $controller('InventoryItemCtrl', {
                $scope: scope
            });
        };
    }));

    describe('call action when actions are enabled', function() {
        beforeEach(inject(function() {
            ctrl = createController();
            ctrl.open = jasmine.createSpy('open');
        }));

        it('should call open', inject(function () {
            //given
            ctrl.actionsEnabled = true;

            //when
            ctrl.openOnClick({});
            scope.$digest();

            //then
            expect(ctrl.open).toHaveBeenCalledWith({});
        }));

        it('should NOT call open', inject(function () {
            //given
            ctrl.actionsEnabled = false;

            //when
            ctrl.openOnClick({});
            scope.$digest();

            //then
            expect(ctrl.open).not.toHaveBeenCalled();
        }));
    });
});
