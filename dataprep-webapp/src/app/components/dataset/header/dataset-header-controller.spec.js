/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Dataset Header controller', () => {

    let createController, scope, stateMock;

    const sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    const orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(angular.mock.module('data-prep.dataset-header', ($provide) => {
        stateMock = {
            inventory: {
                sortList: sortList,
                orderList: orderList,
                datasetsSort: sortList[0],
                datasetsOrder: orderList[0],
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($rootScope, $componentController, $q, StorageService, StateService) => {
        scope = $rootScope.$new();

        createController = () => $componentController('datasetHeader', {$scope: scope});

        spyOn(StorageService, 'setDatasetsSort').and.returnValue();
        spyOn(StorageService, 'setDatasetsOrder').and.returnValue();
        spyOn(StateService, 'setDatasetsSort').and.returnValue();
        spyOn(StateService, 'setDatasetsOrder').and.returnValue();
    }));

    describe('sort', () => {
        it('should set new sort in state', inject(($q, StateService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());
            expect(StateService.setDatasetsSort).not.toHaveBeenCalled();

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(StateService.setDatasetsSort).toHaveBeenCalledWith(sortType);
        }));

        it('should store new sort in storage', inject(($q, StorageService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());
            expect(StorageService.setDatasetsSort).not.toHaveBeenCalled();

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(StorageService.setDatasetsSort).toHaveBeenCalledWith(sortType.id);
        }));

        it('should refresh dataset list', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());

            //when
            ctrl.updateSortBy(sortType);

            //then
            expect(DatasetService.refreshDatasets).toHaveBeenCalled();
        }));

        it('should set back old sort in state when dataset list refresh fails', inject(($q, StateService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];
            const oldSortType = stateMock.inventory.datasetsSort;

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.reject());
            expect(StateService.setDatasetsSort).not.toHaveBeenCalledWith(oldSortType);

            //when
            ctrl.updateSortBy(sortType);
            expect(StateService.setDatasetsSort).not.toHaveBeenCalledWith(oldSortType);
            scope.$digest();

            //then
            expect(StateService.setDatasetsSort).toHaveBeenCalledWith(oldSortType);
        }));

        it('should set back old sort in storage when dataset list refresh fails', inject(($q, StorageService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortType = sortList[1];
            const oldSortType = stateMock.inventory.datasetsSort;

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.reject());
            expect(StorageService.setDatasetsSort).not.toHaveBeenCalledWith(oldSortType.id);

            //when
            ctrl.updateSortBy(sortType);
            expect(StorageService.setDatasetsSort).not.toHaveBeenCalledWith(oldSortType.id);
            scope.$digest();

            //then
            expect(StorageService.setDatasetsSort).toHaveBeenCalledWith(oldSortType.id);
        }));
    });

    describe('order', () => {
        it('should set new sort order in state', inject(($q, StateService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());
            expect(StateService.setDatasetsOrder).not.toHaveBeenCalled();

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(StateService.setDatasetsOrder).toHaveBeenCalledWith(sortOrder);
        }));

        it('should store new sort order in storage', inject(($q, StorageService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());
            expect(StorageService.setDatasetsOrder).not.toHaveBeenCalled();

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(StorageService.setDatasetsOrder).toHaveBeenCalledWith(sortOrder.id);
        }));

        it('should refresh dataset list', inject(($q, DatasetService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.when());

            //when
            ctrl.updateSortOrder(sortOrder);

            //then
            expect(DatasetService.refreshDatasets).toHaveBeenCalled();
        }));

        it('should set back old sort order in state when dataset list refresh fails', inject(($q, StateService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];
            const oldSortOrder = stateMock.inventory.datasetsOrder;

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.reject());
            expect(StateService.setDatasetsOrder).not.toHaveBeenCalledWith(oldSortOrder);

            //when
            ctrl.updateSortOrder(sortOrder);
            expect(StateService.setDatasetsOrder).not.toHaveBeenCalledWith(oldSortOrder);
            scope.$digest();

            //then
            expect(StateService.setDatasetsOrder).toHaveBeenCalledWith(oldSortOrder);
        }));

        it('should set back old sort order in storage when dataset list refresh fails', inject(($q, StorageService, DatasetService) => {
            //given
            const ctrl = createController();
            const sortOrder = orderList[1];
            const oldSortOrder = stateMock.inventory.datasetsOrder;

            spyOn(DatasetService, 'refreshDatasets').and.returnValue($q.reject());
            expect(StorageService.setDatasetsOrder).not.toHaveBeenCalledWith(oldSortOrder.id);

            //when
            ctrl.updateSortOrder(sortOrder);
            expect(StorageService.setDatasetsOrder).not.toHaveBeenCalledWith(oldSortOrder.id);
            scope.$digest();

            //then
            expect(StorageService.setDatasetsOrder).toHaveBeenCalledWith(oldSortOrder.id);
        }));
    });
});
