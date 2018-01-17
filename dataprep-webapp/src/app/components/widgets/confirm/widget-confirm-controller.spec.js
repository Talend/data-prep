/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Confirm directive', function () {
    'use strict';

    var ctrl;
    var createController;
    var scope;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(function ($rootScope, $controller, TalendConfirmService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('TalendConfirmCtrl', {
                $scope: scope,
            });
        };

        spyOn(TalendConfirmService, 'resolve').and.returnValue(null);
        spyOn(TalendConfirmService, 'reject').and.returnValue(null);
    }));

    it('should init modal state and button clicked flag', function () {
        //when
        ctrl = createController();

        //then
        expect(ctrl.modalState).toBeTruthy();
        expect(ctrl.buttonClicked).toBeFalsy();
    });

    it('should set clicked flag and call service resolve', inject(function (TalendConfirmService) {
        //given
        ctrl = createController();
        expect(ctrl.buttonClicked).toBeFalsy();

        //when
        ctrl.valid();

        //then
        expect(TalendConfirmService.resolve).toHaveBeenCalled();
        expect(ctrl.buttonClicked).toBeTruthy();
    }));

    it('should set clicked flag and call service reject', inject(function (TalendConfirmService) {
        //given
        ctrl = createController();
        expect(ctrl.buttonClicked).toBeFalsy();

        //when
        ctrl.cancel();

        //then
        expect(TalendConfirmService.reject).toHaveBeenCalled();
        expect(ctrl.buttonClicked).toBeTruthy();
    }));

    it('should set call service reject on modal dismiss', inject(function (TalendConfirmService) {
        //given
        ctrl = createController();
        expect(ctrl.buttonClicked).toBeFalsy();

        //when
        ctrl.modalState = false;
        scope.$digest();

        //then
        expect(TalendConfirmService.reject).toHaveBeenCalledWith('dismiss');
        expect(ctrl.buttonClicked).toBeFalsy();
    }));

    it('should do nothing on modal dismiss if one of the button has been clicked', inject(function (TalendConfirmService) {
        //given
        ctrl = createController();
        ctrl.buttonClicked = true;

        //when
        ctrl.modalState = false;
        scope.$digest();

        //then
        expect(TalendConfirmService.reject).not.toHaveBeenCalled();
    }));
});
