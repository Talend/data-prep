/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Tooltip widget controller', function () {
    'use strict';

    var createController;
    var scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $controller) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('TalendTooltipCtrl', {
                $scope: scope,
            });
            return ctrl;
        };
    }));

    it('should update position', function () {
        //given
        var ctrl = createController();
        var horizontalPosition = { left: 1, right: 2 };
        var verticalPosition = { top: 3, bottom: 4 };

        //when
        ctrl.updatePosition(horizontalPosition, verticalPosition);

        //then
        expect(ctrl.style.left).toBe(1);
        expect(ctrl.style.right).toBe(2);
        expect(ctrl.style.top).toBe(3);
        expect(ctrl.style.bottom).toBe(4);
    });

    it('should update visibility state when it is not blocked', function () {
        //given
        var ctrl = createController();
        expect(ctrl.requestedState).toBeFalsy();

        //when
        ctrl.requestedState = true;
        scope.$digest();

        //then
        expect(ctrl.requestedState).toBe(true);
        expect(ctrl.innerState).toBe(true);
    });

    it('should not update visibility state when it is blocked', function () {
        //given
        var ctrl = createController();
        ctrl.blockState();
        expect(ctrl.requestedState).toBeFalsy();

        //when
        ctrl.requestedState = true;
        scope.$digest();

        //then
        expect(ctrl.requestedState).toBe(true);
        expect(ctrl.innerState).toBeFalsy();
    });

    it('should unblock and update visibility state', function () {
        //given
        var ctrl = createController();
        ctrl.blockState();
        expect(ctrl.requestedState).toBeFalsy();

        ctrl.requestedState = true;
        scope.$digest();
        expect(ctrl.innerState).toBeFalsy();

        //when
        ctrl.unblockState();

        //then
        expect(ctrl.innerState).toBe(true);
    });
});
