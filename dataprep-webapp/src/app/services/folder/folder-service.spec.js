/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder services', () => {
    'use strict';

    let stateMock;

    const sortList = [
        { id: 'name', name: 'NAME_SORT', property: 'name' },
        { id: 'date', name: 'DATE_SORT', property: 'created' },
    ];

    const orderList = [
        { id: 'asc', name: 'ASC_ORDER' },
        { id: 'desc', name: 'DESC_ORDER' },
    ];

    beforeEach(angular.mock.module('data-prep.services.folder', ($provide) => {
        stateMock = {
            inventory: {
                sortList: sortList,
                orderList: orderList,
                preparationsSort: sortList[0],
                preparationsOrder: orderList[0],
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, FolderRestService) => {
        spyOn(FolderRestService, 'create').and.returnValue($q.when());
        spyOn(FolderRestService, 'children').and.returnValue($q.when());
        spyOn(FolderRestService, 'search').and.returnValue($q.when());
        spyOn(FolderRestService, 'rename').and.returnValue($q.when());
        spyOn(FolderRestService, 'remove').and.returnValue($q.when());
    }));

    describe('simple REST calls', () => {
        it('should call rest children', inject((FolderService, FolderRestService) => {
            //when
            FolderService.children('/foo');

            //then
            expect(FolderRestService.children).toHaveBeenCalledWith('/foo');
        }));

        it('should call rest create', inject((FolderService, FolderRestService) => {
            //when
            FolderService.create('/foo');

            //then
            expect(FolderRestService.create).toHaveBeenCalledWith('/foo');
        }));

        it('should call rest rename', inject((FolderService, FolderRestService) => {
            //when
            FolderService.rename('foo', 'beer');

            //then
            expect(FolderRestService.rename).toHaveBeenCalledWith('foo', 'beer');
        }));

        it('should call rest remove', inject((FolderService, FolderRestService) => {
            //when
            FolderService.remove('foo');

            //then
            expect(FolderRestService.remove).toHaveBeenCalledWith('foo');
        }));

        it('should call rest search', inject((FolderService, FolderRestService) => {
            //when
            FolderService.search('path');

            //then
            expect(FolderRestService.search).toHaveBeenCalledWith('path');
        }));
    });

    describe('refresh content', () => {
        const content = {
            folders: [{ path: 'toto', name: 'toto' }],
            preparations: [{ id: '5a8cf1763b58' }]
        };

        beforeEach(inject(($q, StateService, FolderRestService) => {
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when(content));
            spyOn(StateService, 'setFolder').and.returnValue();
        }));

        it('should call rest service with sort and order', inject((FolderService, FolderRestService) => {
            // given
            stateMock.inventory.preparationsSort = sortList[1];
            stateMock.inventory.preparationsOrder = orderList[1];

            // when
            FolderService.refreshContent('/my/path');

            // then
            expect(FolderRestService.getContent).toHaveBeenCalledWith('/my/path', 'date', 'desc');
        }));

        it('should set the folder content in app state', inject(($rootScope, FolderService, StateService) => {
            // given
            stateMock.inventory.preparationsSort = sortList[1];
            stateMock.inventory.preparationsOrder = orderList[1];

            // when
            FolderService.refreshContent('/my/path');
            $rootScope.$digest();

            // then
            expect(StateService.setFolder).toHaveBeenCalledWith('/my/path', content);
        }));
    });

    describe('init', () => {
        it('should set the preparation sort when there is a saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsSort').and.returnValue('date');
            spyOn(StateService, 'setPreparationsSort').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsSort).toHaveBeenCalledWith(sortList[1]);
        }));

        it('should NOT set the preparation sort when there is NO saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsSort').and.returnValue(null);
            spyOn(StateService, 'setPreparationsSort').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsSort).not.toHaveBeenCalled();
        }));

        it('should set the preparation order when there is a saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsOrder').and.returnValue('desc');
            spyOn(StateService, 'setPreparationsOrder').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsOrder).toHaveBeenCalledWith(orderList[1]);
        }));

        it('should NOT set the preparation order when there is NO saved one', inject((StateService, StorageService, FolderService) => {
            // given
            spyOn(StorageService, 'getPreparationsOrder').and.returnValue(null);
            spyOn(StateService, 'setPreparationsOrder').and.returnValue();

            // when
            FolderService.init('/my/path');

            // then
            expect(StateService.setPreparationsOrder).not.toHaveBeenCalled();
        }));

        it('should refresh folder content', inject(($q, FolderRestService, FolderService) => {
            // given
            spyOn(FolderRestService, 'getContent').and.returnValue($q.when());

            // when
            FolderService.init('/my/path');

            // then
            expect(FolderRestService.getContent).toHaveBeenCalled();
        }));
    });
});
