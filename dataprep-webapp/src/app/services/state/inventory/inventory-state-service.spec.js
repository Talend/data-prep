/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory state service', () => {
	let datasets;
	let preparations;

	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(() => {
		datasets = [
			{
				id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
				name: 'customers_jso_light',
				author: 'anonymousUser',
				records: 15,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 08:06',
			},
			{
				id: '3b21388c-f54a-4334-9bef-748912d0806f',
				name: 'customers_jso',
				author: 'anonymousUser',
				records: 1000,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 07:35',
			},
			{
				id: '124568124-8da46-6635-6b5e-7845748fc54',
				name: 'dataset_without_preparations',
				author: 'anonymousUser',
				records: 1000,
				nbLinesHeader: 1,
				nbLinesFooter: 0,
				created: '03-30-2015 07:35',
			},
		];
		preparations = [
			{
				id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
				dataSetId: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
				author: 'anonymousUser',
				creationDate: 1427447300300,
			},
			{
				id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
				dataSetId: '3b21388c-f54a-4334-9bef-748912d0806f',
				author: 'anonymousUser',
				creationDate: 1427447330693,
			},
		];
	});

	describe('datasets', () => {
		it('should set datasets', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.preparations = null;
			inventoryState.datasets = null;

			//when
			InventoryStateService.setDatasets(datasets);

			//then
			expect(inventoryState.datasets).toBe(datasets);
		}));

		it('should remove a dataset from datasets list', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.datasets = datasets;
			expect(inventoryState.datasets.length).toBe(3);

			//when
			InventoryStateService.removeDataset(datasets[0]);

			//then
			expect(inventoryState.datasets.length).toBe(2);
			expect(inventoryState.datasets[0].id).toBe('3b21388c-f54a-4334-9bef-748912d0806f');
			expect(inventoryState.datasets[1].id).toBe('124568124-8da46-6635-6b5e-7845748fc54');
		}));

		it('should update dataset name', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.datasets = [
				{ id: '1', name: 'toto' },
				{ id: '2', name: 'tata' },
				{ id: '3', name: 'titi' },
				{ id: '4', name: 'tutu' },
			];

			// when
			InventoryStateService.setDatasetName('2', 'tonton');

			// then
			expect(inventoryState.datasets[1].name).toBe('tonton');
		}));
	});

	describe('preparation', () => {
		it('should set display mode', inject((inventoryState, InventoryStateService) => {
			// given
			expect(inventoryState.preparationsDisplayMode).toBe('table');
			const displayMode = 'tile';

			// when
			InventoryStateService.setPreparationsDisplayMode(displayMode);

			// then
			expect(inventoryState.preparationsDisplayMode).toBe('tile');
		}));
	});

	describe('folder', () => {
		it('should set folder metadata', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.folder = {};
			const folderMetadata = {
				id: 'L215L3BlcnNvbmFsL2ZvbGRlcg==',
			};

			//when
			InventoryStateService.setFolder(folderMetadata);

			//then
			expect(inventoryState.folder.metadata).toBe(folderMetadata);
		}));

		it('should set content', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.folder = {};
			const content = {
				folders: [],
				preparations: [],
			};

			//when
			InventoryStateService.setFolder(undefined, content);

			//then
			expect(inventoryState.folder.content).toBe(content);
		}));

		it('should set user\'s home folder id', inject((inventoryState, InventoryStateService) => {
			//given
			expect(inventoryState.homeFolderId).toBe('Lw==');
			const homeId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			InventoryStateService.setHomeFolderId(homeId);

			//then
			expect(inventoryState.homeFolderId).toBe(homeId);
		}));
	});

	describe('sort/order', () => {
		it('should set datasets sort', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.datasetsSort = '';

			//when
			InventoryStateService.setDatasetsSort('name');

			//then
			expect(inventoryState.datasetsSort).toBe('name');
		}));

		it('should set datasets order', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.datasetsOrder = '';

			//when
			InventoryStateService.setDatasetsOrder('desc');

			//then
			expect(inventoryState.datasetsOrder).toBe('desc');
		}));

		it('should set preparations sort', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.preparationsSort = '';

			//when
			InventoryStateService.setPreparationsSort('name');

			//then
			expect(inventoryState.preparationsSort).toBe('name');
		}));

		it('should set preparations order', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.preparationsOrder = '';

			//when
			InventoryStateService.setPreparationsOrder('desc');

			//then
			expect(inventoryState.preparationsOrder).toBe('desc');
		}));

		it('should set preparations sort/order from ids',
			inject((inventoryState, InventoryStateService) => {
				//given
				inventoryState.preparationsSort = '';
				inventoryState.preparationsOrder = '';

				//when
				InventoryStateService.setPreparationsSortFromIds('name', 'desc');

				//then
				expect(inventoryState.preparationsSort.id).toBe('name');
				expect(inventoryState.preparationsOrder.id).toBe('desc');
			})
		);
	});

	describe('loading', () => {
		it('should set FetchingDatasets', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.isFetchingDatasets = false;

			//when
			InventoryStateService.setFetchingDatasets(true);

			//then
			expect(inventoryState.isFetchingDatasets).toBe(true);
		}));

		it('should set FetchingPreparations', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.isFetchingPreparations = false;

			//when
			InventoryStateService.setFetchingPreparations(true);

			//then
			expect(inventoryState.isFetchingPreparations).toBe(true);
		}));
	});
});
