/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search Inventory Service', () => {
	const results = {
		data: {
			folders: [{ name: 'folder test', lastModificationDate: 1 }],
			preparations: [{ name: 'prep test', lastModificationDate: 2 }],
			datasets: [{
				id: 'id',
				name: 'dataset test',
				lastModificationDate: 3,
				author: 'toto',
				created: 1,
				records: 100,
				path: 'home',
				type: 'csv',
				owner: {
					id: 'charles',
				},
			}],
		},
	};

	beforeEach(angular.mock.module('data-prep.services.search.inventory'));

	beforeEach(inject(($q, SearchInventoryRestService) => {
		spyOn(SearchInventoryRestService, 'search').and.returnValue($q.when(results));
	}));

	it('should call inventory search rest service and process data', inject(($rootScope, SearchInventoryService) => {
		// given
		let result = null;
		const expectedResult = [
			{
				id: 'id',
				inventoryType: 'dataset',
				author: 'toto',
				created: 1,
				records: 100,
				name: 'dataset test',
				path: 'home',
				type: 'csv',
				model: results.data.datasets[0],
				lastModificationDate: 3,
				tooltipName: 'dataset test',
				owner: {
					id: 'charles',
				},
			},
			{
				name: 'prep test',
				lastModificationDate: 2,
				inventoryType: 'preparation',
				tooltipName: 'prep test',
			},
			{
				name: 'folder test',
				lastModificationDate: 1,
				inventoryType: 'folder',
				tooltipName: 'folder test',
			},
		];

		// when
		SearchInventoryService.search('test').then((response) => {
			result = response;
		});

		$rootScope.$digest();

		// then
		expect(result).toEqual(expectedResult);
	}));
});
