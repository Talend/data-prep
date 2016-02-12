/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Folder controller', function () {
    'use strict';

    var createController, scope, stateMock;

    var sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    var orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.folder', function($provide){
        stateMock = {
            inventory: {
                datasets: [],
                sortList: sortList,
                orderList: orderList,
                sort: sortList[1],
                order: orderList[1]
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('data-prep.folder'));

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

    it('should refresh sort parameters', inject(function ($timeout, StorageService, StateService) {
        //given
        spyOn(StorageService, 'getDatasetsSort').and.returnValue('date');
        spyOn(StorageService, 'getDatasetsOrder').and.returnValue('desc');
        spyOn(StateService, 'setDatasetsOrder');
        spyOn(StateService, 'setDatasetsSort');


        //when
        createController();

        //then
        expect(StateService.setDatasetsSort).toHaveBeenCalledWith({id: 'date', name: 'DATE_SORT', property: 'created'});
        expect(StateService.setDatasetsOrder).toHaveBeenCalledWith({id: 'desc', name: 'DESC_ORDER'});
    }));

});
