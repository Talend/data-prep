/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation Header controller', () => {

    let createController, scope, stateMock;

    const sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    const orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.preparation-header', ($provide) => {
        stateMock = {
            inventory: {
                sortList: sortList,
                orderList: orderList,
                preparationsSort: sortList[0],
                preparationsOrder: orderList[0],
                folder: {
                    metadata: { path: '/my/folder' }
                },
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $componentController, $q, StorageService, StateService) => {
        scope = $rootScope.$new();

        createController = () => $componentController('preparationHeader', {$scope: scope});

        spyOn(StorageService, 'setPreparationsSort').and.returnValue();
        spyOn(StorageService, 'setPreparationsOrder').and.returnValue();
        spyOn(StateService, 'setPreparationsSort').and.returnValue();
        spyOn(StateService, 'setPreparationsOrder').and.returnValue();
    }));

    describe('sort', () => {
        it('should set new sort in state', inject(($q, StateService, FolderService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());
            expect(StateService.setPreparationsSort).not.toHaveBeenCalled();

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(StateService.setPreparationsSort).toHaveBeenCalledWith(sortType);
        }));

        it('should store new sort in storage', inject(($q, StorageService, FolderService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());
            expect(StorageService.setPreparationsSort).not.toHaveBeenCalled();

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(StorageService.setPreparationsSort).toHaveBeenCalledWith(sortType.id);
        }));

        it('should refresh folder content', inject(($q, FolderService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/folder');
        }));

        it('should set back old sort in state when folder refresh fails', inject(($q, StateService, FolderService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];
            const oldSortType = stateMock.inventory.preparationsSort;

            spyOn(FolderService, 'refreshContent').and.returnValue($q.reject());
            expect(StateService.setPreparationsSort).not.toHaveBeenCalledWith(oldSortType);

            //when
            ctrl.updateSortBy(sortType);
            expect(StateService.setPreparationsSort).not.toHaveBeenCalledWith(oldSortType);
            scope.$digest();

            //then
            expect(StateService.setPreparationsSort).toHaveBeenCalledWith(oldSortType);
        }));

        it('should set back old sort in storage when folder refresh fails', inject(($q, StorageService, FolderService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];
            const oldSortType = stateMock.inventory.preparationsSort;

            spyOn(FolderService, 'refreshContent').and.returnValue($q.reject());
            expect(StorageService.setPreparationsSort).not.toHaveBeenCalledWith(oldSortType.id);

            //when
            ctrl.updateSortBy(sortType);
            expect(StorageService.setPreparationsSort).not.toHaveBeenCalledWith(oldSortType.id);
            scope.$digest();

            //then
            expect(StorageService.setPreparationsSort).toHaveBeenCalledWith(oldSortType.id);
        }));
    });

    describe('order', () => {
        it('should set new sort order in state', inject(($q, StateService, FolderService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());
            expect(StateService.setPreparationsOrder).not.toHaveBeenCalled();

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(StateService.setPreparationsOrder).toHaveBeenCalledWith(sortOrder);
        }));

        it('should store new sort order in storage', inject(($q, StorageService, FolderService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());
            expect(StorageService.setPreparationsOrder).not.toHaveBeenCalled();

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(StorageService.setPreparationsOrder).toHaveBeenCalledWith(sortOrder.id);
        }));

        it('should refresh folder content', inject(($q, FolderService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/folder');
        }));

        it('should set back old sort order in state when folder refresh fails', inject(($q, StateService, FolderService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];
            const oldSortOrder = stateMock.inventory.preparationsOrder;

            spyOn(FolderService, 'refreshContent').and.returnValue($q.reject());
            expect(StateService.setPreparationsOrder).not.toHaveBeenCalledWith(oldSortOrder);

            //when
            ctrl.updateSortOrder(sortOrder);
            expect(StateService.setPreparationsOrder).not.toHaveBeenCalledWith(oldSortOrder);
            scope.$digest();

            //then
            expect(StateService.setPreparationsOrder).toHaveBeenCalledWith(oldSortOrder);
        }));

        it('should set back old sort order in storage when folder refresh fails', inject(($q, StorageService, FolderService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];
            const oldSortOrder = stateMock.inventory.preparationsOrder;

            spyOn(FolderService, 'refreshContent').and.returnValue($q.reject());
            expect(StorageService.setPreparationsOrder).not.toHaveBeenCalledWith(oldSortOrder.id);

            //when
            ctrl.updateSortOrder(sortOrder);
            expect(StorageService.setPreparationsOrder).not.toHaveBeenCalledWith(oldSortOrder.id);
            scope.$digest();

            //then
            expect(StorageService.setPreparationsOrder).toHaveBeenCalledWith(oldSortOrder.id);
        }));
    });

    describe('create folder', () => {
        it('should call create service with absolute folder path', inject(($q, FolderService) => {
            //given
            const ctrl = createController();
            spyOn(FolderService, 'create').and.returnValue($q.when());

            //when
            ctrl.createFolder('toto');

            //then
            expect(FolderService.create).toHaveBeenCalledWith('/my/folder/toto');
        }));

        it('should refresh folder content', inject(($q, FolderService) => {
            //given
            const ctrl = createController();
            spyOn(FolderService, 'create').and.returnValue($q.when());
            spyOn(FolderService, 'refreshContent').and.returnValue($q.when());

            //when
            ctrl.createFolder('toto');
            scope.$digest();

            //then
            expect(FolderService.refreshContent).toHaveBeenCalledWith('/my/folder');
        }));
    });
});
