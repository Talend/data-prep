/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import SlickGridMock from './../../../../../mocks/SlickGrid.mock';

describe('Lookup Datagrid column service', () => {
    'use strict';

    let gridMock;
    let columnsMetadata;

    beforeEach(angular.mock.module('data-prep.lookup'));

    beforeEach(inject(() => {
        columnsMetadata = [
            { id: '0000', name: 'col0', type: 'string' },
            { id: '0001', name: 'col1', type: 'integer' },
            { id: '0002', name: 'col2', type: 'string', domain: 'salary' },
        ];

        gridMock = new SlickGridMock();
    }));

    describe('on creation', () => {
        it('should add SlickGrid header creation handler', inject((LookupDatagridColumnService) => {
            //given
            spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();

            //when
            LookupDatagridColumnService.init(gridMock);

            //then
            expect(gridMock.onHeaderCellRendered.subscribe).toHaveBeenCalled();
        }));
    });

    describe('on column header rendered event', () => {
        beforeEach(inject((LookupDatagridColumnService) => {
            spyOn(gridMock.onHeaderCellRendered, 'subscribe').and.returnValue();
            LookupDatagridColumnService.init(gridMock);
        }));

        it('should create and attach a new header', inject(() => {
            //given
            const columnsArgs = {
                column:  {
                    id: '0002',
                    tdpColMetadata: {},
                },
                node: angular.element('<div></div>')[0],
            };

            //when
            const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(angular.element(columnsArgs.node).find('lookup-datagrid-header').length).toBe(1);
        }));

        it('should do nothing if column is index column', inject(() => {
            //given
            const columnsArgs = {
                column:  {
                    id: 'tdpId',
                },
                node: angular.element('<div></div>')[0],
            };

            //when
            const onHeaderCellRendered = gridMock.onHeaderCellRendered.subscribe.calls.argsFor(0)[0];
            onHeaderCellRendered(null, columnsArgs);

            //then
            expect(angular.element(columnsArgs.node).find('lookup-datagrid-header').length).toBe(0);
        }));
    });
});
