describe('Folder controller', function () {
    'use strict';

    var createController, scope;

    beforeEach(module('data-prep.folder'));

    beforeEach(inject(function($rootScope, $controller, $q, FolderService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('FolderCtrl', {
                $scope: scope
            });
        };
        spyOn(FolderService, 'goToFolder').and.returnValue($q.when(true));
        spyOn(FolderService, 'loadFolders').and.returnValue($q.when(true));
    }));

    it('should call goToFolder', inject(function (FolderService) {
        //when
        var ctrl = createController();
        ctrl.goToFolder();
        scope.$digest();

        //then
        expect(FolderService.goToFolder).toHaveBeenCalled();
    }));


});
