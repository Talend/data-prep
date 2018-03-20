/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DataViewMock from '../../../../../mocks/DataView.mock';
import SlickGridMock from '../../../../../mocks/SlickGrid.mock';

describe('Lookup Datagrid grid service', () => {
	const realSlickGrid = Slick;
	let dataViewMock;
	let stateMock;

	beforeEach(angular.mock.module('data-prep.lookup'));

	beforeEach(() => {
		dataViewMock = new DataViewMock();
	});

	beforeEach(angular.mock.module('data-prep.datagrid', ($provide) => {
		stateMock = {
			playground: {
				lookup: {
					dataView: dataViewMock,
				},
			},
		};
		$provide.constant('state', stateMock);

		spyOn(dataViewMock.onRowCountChanged, 'subscribe').and.returnValue();
		spyOn(dataViewMock.onRowsChanged, 'subscribe').and.returnValue();
	}));

	beforeEach(inject((LookupDatagridColumnService, LookupDatagridStyleService) => {
		spyOn(LookupDatagridColumnService, 'init').and.returnValue();
		spyOn(LookupDatagridStyleService, 'init').and.returnValue();
	}));

	beforeEach(inject(($window) => {
		$window.Slick = {
			Grid: SlickGridMock,
			AutoColumnSize() {
			},
		};
	}));

	afterEach(inject(($window) => {
		$window.Slick = realSlickGrid;
	}));

	describe('on creation', () => {
		it('should init the other datagrid services', inject((LookupDatagridGridService, LookupDatagridColumnService, LookupDatagridStyleService) => {
			// when
			LookupDatagridGridService.initGrid();

			// then
			expect(LookupDatagridColumnService.init).toHaveBeenCalled();
			expect(LookupDatagridStyleService.init).toHaveBeenCalled();
		}));

		it('should add grid listeners', inject((LookupDatagridGridService) => {
			// when
			LookupDatagridGridService.initGrid();

			// then
			expect(stateMock.playground.lookup.dataView.onRowCountChanged.subscribe).toHaveBeenCalled();
			expect(stateMock.playground.lookup.dataView.onRowsChanged.subscribe).toHaveBeenCalled();
		}));
	});
});
