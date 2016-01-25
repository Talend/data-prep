describe('Folder controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.folder'));

    beforeEach(inject(function($rootScope, $controller, $q, FolderService, StateService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('FolderCtrl', {
                $scope: scope
            });
        };
        spyOn(FolderService, 'getContent').and.returnValue($q.when(true));
        spyOn(FolderService, 'populateMenuChildren').and.returnValue($q.when(true));
        spyOn(StateService, 'setMenuChildren').and.returnValue();
    }));

    it('should call goToFolder service', inject(function (FolderService) {
        //when
        var ctrl = createController();
        ctrl.goToFolder();
        scope.$digest();

        //then
        expect(FolderService.getContent).toHaveBeenCalled();
    }));

    it('should call populateMenuChildren service', inject(function (FolderService, StateService) {

        //when
        var ctrl = createController();
        ctrl.initMenuChildren();
        scope.$digest();

        //then
        expect(FolderService.populateMenuChildren).toHaveBeenCalled();
        expect(StateService.setMenuChildren).toHaveBeenCalledWith([]);
    }));

});
