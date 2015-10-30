describe('Datagrid column service', function () {
    'use strict';

    var gridMock, columnsMetadata;

    beforeEach(module('data-prep.datagrid'));

    beforeEach(inject(function () {
        columnsMetadata = [
            {id: '0000', name: 'col0', type: 'string'},
            {id: '0001', name: 'col1', type: 'integer'},
            {id: '0002', name: 'col2', type: 'string', domain: 'salary'}
        ];

        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();
    }));

    describe('on creation', function () {
        it('should add SlickGrid header destroy handler', inject(function (DatagridColumnService) {
            //given
            spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

            //when
            DatagridColumnService.init(gridMock);

            //then
            expect(gridMock.onBeforeHeaderCellDestroy.subscribe).toHaveBeenCalled();
        }));

        it('should add SlickGrid header creation handler', inject(function (DatagridColumnService) {
            //given
            spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

            //when
            DatagridColumnService.init(gridMock);

            //then
            expect(gridMock.onHeaderCellRendered.subscribe).toHaveBeenCalled();
        }));
    });

    describe('create columns', function() {
        var headers = [
            {element: {detach: function() {}}},
            {element: {detach: function() {}}},
            {element: {detach: function() {}}}];
        var formatter = function() {};

        beforeEach(inject(function(DatagridColumnService, DatagridStyleService) {
            DatagridColumnService.init(gridMock);
            headers.forEach(function(header) {
                spyOn(header.element, 'detach').and.returnValue();
            });

            spyOn(DatagridStyleService, 'getColumnPreviewStyle').and.returnValue('');
            spyOn(DatagridStyleService, 'columnFormatter').and.returnValue(formatter);
        }));

        it('should create new preview grid columns', inject(function(DatagridColumnService) {
            //when
            var createdColumns = DatagridColumnService.createColumns(columnsMetadata, true, false);

            //then
            expect(createdColumns[0].id).toEqual('tdpId');
            expect(createdColumns[0].field).toEqual('tdpId');
            expect(createdColumns[0].name).toEqual('<div class="slick-header-column-index">#</div>');
            expect(createdColumns[0].resizable).toBeFalsy();
            expect(createdColumns[0].selectable).toBeFalsy();
            expect(createdColumns[0].formatter(1,2,3)).toEqual('<div class="index-cell">3</div>');
            expect(createdColumns[0].maxWidth).toEqual(45);

            expect(createdColumns[1].id).toEqual('0000');
            expect(createdColumns[1].field).toEqual('0000');
            expect(createdColumns[1].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col0</div>       <div class="grid-header-type ng-binding">text</div>   </div><div class="quality-bar"><div class="record-unknown"></div></div>');
            expect(createdColumns[1].formatter).toEqual(formatter);
            expect(createdColumns[1].minWidth).toEqual(120);
            expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' } );

            expect(createdColumns[2].id).toEqual('0001');
            expect(createdColumns[2].field).toEqual('0001');
            expect(createdColumns[2].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col1</div>       <div class="grid-header-type ng-binding">integer</div>   </div><div class="quality-bar"><div class="record-unknown"></div></div>');
            expect(createdColumns[2].formatter).toEqual(formatter);
            expect(createdColumns[2].minWidth).toEqual(120);
            expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' } );

            expect(createdColumns[3].id).toEqual('0002');
            expect(createdColumns[3].field).toEqual('0002');
            expect(createdColumns[3].name).toEqual('<div class="grid-header ">   <div class="grid-header-title dropdown-button ng-binding">col2</div>       <div class="grid-header-type ng-binding">salary</div>   </div><div class="quality-bar"><div class="record-unknown"></div></div>');
            expect(createdColumns[3].formatter).toEqual(formatter);
            expect(createdColumns[3].minWidth).toEqual(120);
            expect(createdColumns[3].tdpColMetadata).toEqual({ id: '0002', name: 'col2', type: 'string', domain: 'salary' } );
        }));

        it('should create new grid columns', inject(function(DatagridColumnService) {
            //when
            var createdColumns = DatagridColumnService.createColumns(columnsMetadata, false, false);

            //then
            expect(createdColumns[0].id).toEqual('tdpId');
            expect(createdColumns[0].field).toEqual('tdpId');
            expect(createdColumns[0].name).toEqual('<div class="slick-header-column-index">#</div>');
            expect(createdColumns[0].resizable).toBeFalsy();
            expect(createdColumns[0].selectable).toBeFalsy();
            expect(createdColumns[0].formatter(1,2,3)).toEqual('<div class="index-cell">3</div>');
            expect(createdColumns[0].maxWidth).toEqual(45);

            expect(createdColumns[1].id).toEqual('0000');
            expect(createdColumns[1].field).toEqual('0000');
            expect(createdColumns[1].name).toEqual('');
            expect(createdColumns[1].formatter).toEqual(formatter);
            expect(createdColumns[1].minWidth).toEqual(120);
            expect(createdColumns[1].tdpColMetadata).toEqual({ id: '0000', name: 'col0', type: 'string' } );

            expect(createdColumns[2].id).toEqual('0001');
            expect(createdColumns[2].field).toEqual('0001');
            expect(createdColumns[2].name).toEqual('');
            expect(createdColumns[2].formatter).toEqual(formatter);
            expect(createdColumns[2].minWidth).toEqual(120);
            expect(createdColumns[2].tdpColMetadata).toEqual({ id: '0001', name: 'col1', type: 'integer' } );

            expect(createdColumns[3].id).toEqual('0002');
            expect(createdColumns[3].field).toEqual('0002');
            expect(createdColumns[3].name).toEqual('');
            expect(createdColumns[3].formatter).toEqual(formatter);
            expect(createdColumns[3].minWidth).toEqual(120);
            expect(createdColumns[3].tdpColMetadata).toEqual({ id: '0002', name: 'col2', type: 'string', domain: 'salary' });
        }));
    });

    describe('on column header destroy event', function() {
        var columnDef;

        beforeEach(inject(function(DatagridColumnService) {
            spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();

            columnDef = {
                header: {remove: function() {}, detach: function() {}},
                scope: {$destroy: function() {}}
            };

            spyOn(columnDef.header, 'detach').and.returnValue();
            spyOn(columnDef.header, 'remove').and.returnValue();
            spyOn(columnDef.scope, '$destroy').and.returnValue();

            DatagridColumnService.init(gridMock);
        }));

        it('should do nothing when column is part of a preview', inject(function(DatagridColumnService) {
            //given
            columnDef.preview = true;
            var columnsArgs = {
                id: '0001',
                column: columnDef
            };
            DatagridColumnService.renewAllColumns(true);

            //when
            var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
            onBeforeHeaderCellDestroy(null, columnsArgs);

            //then
            expect(columnDef.header.detach).not.toHaveBeenCalled();
            expect(columnDef.header.remove).not.toHaveBeenCalled();
            expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
        }));

        it('should destroy header when renewAllFlag is set to true', inject(function(DatagridColumnService) {
            //given
            columnDef.preview = false;
            var columnsArgs = {
                id: '0001',
                column: columnDef
            };
            DatagridColumnService.renewAllColumns(true);

            //when
            var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
            onBeforeHeaderCellDestroy(null, columnsArgs);

            //then
            expect(columnDef.header.detach).not.toHaveBeenCalled();
            expect(columnDef.header.remove).toHaveBeenCalled();
            expect(columnDef.scope.$destroy).toHaveBeenCalled();
        }));

        it('should detach header when renewAllFlag is set to false', inject(function(DatagridColumnService) {
            //given
            columnDef.preview = false;
            var columnsArgs = {
                id: '0001',
                column: columnDef
            };
            DatagridColumnService.renewAllColumns(false);

            //when
            var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
            onBeforeHeaderCellDestroy(null, columnsArgs);

            //then
            expect(columnDef.header.detach).toHaveBeenCalled();
            expect(columnDef.header.remove).not.toHaveBeenCalled();
            expect(columnDef.scope.$destroy).not.toHaveBeenCalled();
        }));

        it('should NOT detach header for index column', inject(function(DatagridColumnService) {
            //given
            columnDef.preview = false;
            var columnsArgs = {
                id: 'tdpId',
                column: {
                    id: 'tdpId',
                    header: {remove: function() {}, detach: function() {}},
                    scope: {$destroy: function() {}}
                }
            };
            DatagridColumnService.renewAllColumns(false);

            //when
            var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
            onBeforeHeaderCellDestroy(null, columnsArgs);

            //then
            expect(columnDef.header.detach).not.toHaveBeenCalled();
        }));
    });

    describe('on column header rendered event', function() {
        var availableScope;
        var availableHeader;

        function saveHeader(id, scope, header) {
            var columnsToDestroy = {
                id: id,
                scope: scope,
                header: header,
                preview: false
            };
            var headerToDetach = {
                column: columnsToDestroy
            };

            //destroy to save header in the available headers
            var onBeforeHeaderCellDestroy = gridMock.onBeforeHeaderCellDestroy.subscribe.calls.argsFor(0)[0];
            onBeforeHeaderCellDestroy(null, headerToDetach);
        }

        beforeEach(inject(function(DatagridColumnService) {
            spyOn(gridMock.onBeforeHeaderCellDestroy, 'subscribe').and.returnValue();
            spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

            DatagridColumnService.init(gridMock);

            //save header in available headers list
            availableScope = {$destroy: function() {}, $digest: function() {}};
            availableHeader = angular.element('<div id="availableHeader"></div>');
            saveHeader('0001', availableScope, availableHeader);

            spyOn(availableScope, '$digest').and.returnValue();
        }));

        it('should attach and update available header that has the same id', inject(function() {
            //given
            var columnsArgs = {
                column:  {
                    id: '0001',
                    tdpColMetadata: {}
                },
                node: angular.element('<div></div>')[0]
            };

            //when
            var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(availableScope.column).toBe(columnsArgs.column.tdpColMetadata);
            expect(availableScope.$digest).toHaveBeenCalled();

            expect(columnsArgs.column.header).toBe(availableHeader);
            expect(columnsArgs.column.scope).toBe(availableScope);

            expect(angular.element(columnsArgs.node).find('#availableHeader').length).toBe(1);
        }));

        it('should create and attach a new header', inject(function() {
            //given
            var columnsArgs = {
                column:  {
                    id: '0002',
                    tdpColMetadata: {}
                },
                node: angular.element('<div></div>')[0]
            };

            //when
            var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(columnsArgs.column.scope).toBeDefined();
            expect(columnsArgs.column.header).toBeDefined();

            expect(columnsArgs.column.header).not.toBe(availableHeader);
            expect(columnsArgs.column.scope).not.toBe(availableScope);

            expect(angular.element(columnsArgs.node).find('datagrid-header').length).toBe(1);
        }));

        it('should do nothing if column is from preview', inject(function() {
            //given
            var columnsArgs = {
                column:  {
                    id: '0002',
                    tdpColMetadata: {},
                    preview: true
                },
                node: angular.element('<div></div>')[0]
            };

            //when
            var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(columnsArgs.column.scope).not.toBeDefined();
            expect(columnsArgs.column.header).not.toBeDefined();

            expect(angular.element(columnsArgs.node).find('datagrid-header').length).toBe(0);
        }));

        it('should do nothing if column is index column', inject(function() {
            //given
            var columnsArgs = {
                column:  {
                    id: 'tdpId'
                },
                node: angular.element('<div></div>')[0]
            };

            //when
            var onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(columnsArgs.column.scope).not.toBeDefined();
            expect(columnsArgs.column.header).not.toBeDefined();

            expect(angular.element(columnsArgs.node).find('datagrid-header').length).toBe(0);
        }));
    });
});
