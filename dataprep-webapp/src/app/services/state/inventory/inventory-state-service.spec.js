/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import i18n from '../../../../i18n/en.json';

describe('Inventory state service', () => {
	let datasets;
	let preparations;
	let folders;
	let storageSpy;

	beforeEach(angular.mock.module('data-prep.services.state'));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q,  StorageService) => {
		storageSpy = spyOn(StorageService, 'setListsDisplayModes').and.returnValue();
	}));

	beforeEach(inject(inventoryState => {
		inventoryState.preparationsDisplayMode = 'table';
		inventoryState.datasetsDisplayMode = 'table';
	}));


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
				type: 'preparation',
			},
			{
				id: 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
				dataSetId: '3b21388c-f54a-4334-9bef-748912d0806f',
				author: 'anonymousUser',
				creationDate: 1427447330693,
				type: 'preparation',
			},
		];
		folders = [
			{ id: 'folder1', type: 'folder' },
			{ id: 'folder2', type: 'folder' },
		];
	});

	describe('datasets', () => {
		it('should set display mode', inject((inventoryState, InventoryStateService) => {
			// given
			expect(inventoryState.datasetsDisplayMode).toBe('table');
			const displayMode = { mode: 'tile' };

			// when
			InventoryStateService.setDatasetsDisplayMode(displayMode);

			// then
			expect(inventoryState.datasetsDisplayMode).toBe('tile');
		}));

		it('should set datasets', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.preparations = null;
			inventoryState.datasets.content = null;

			//when
			InventoryStateService.setDatasets(datasets);

			//then
			expect(inventoryState.datasets.content).toBe(datasets);
		}));

		it('should remove a dataset from datasets list', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.datasets.content = datasets;
			expect(inventoryState.datasets.content.length).toBe(3);

			//when
			InventoryStateService.removeDataset(datasets[0]);

			//then
			expect(inventoryState.datasets.content.length).toBe(2);
			expect(inventoryState.datasets.content[0].id).toBe('3b21388c-f54a-4334-9bef-748912d0806f');
			expect(inventoryState.datasets.content[1].id).toBe('124568124-8da46-6635-6b5e-7845748fc54');
		}));

		it('should update dataset name', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.datasets.content = [
				{ id: '1', name: 'toto' },
				{ id: '2', name: 'tata' },
				{ id: '3', name: 'titi' },
				{ id: '4', name: 'tutu' },
			];

			// when
			InventoryStateService.setDatasetName('2', 'tonton');

			// then
			expect(inventoryState.datasets.content[1].name).toBe('tonton');
		}));

		it('should set dataset to update', inject((inventoryState, InventoryStateService) => {
			// given
			const datasetToUpdate = { id: '1', name: 'toto' };

			// when
			InventoryStateService.setDatasetToUpdate(datasetToUpdate);

			// then
			expect(inventoryState.datasetToUpdate).toBe(datasetToUpdate);
		}));

		it('should set datasets list display mode and persist it if different from the actual', inject((inventoryState, InventoryStateService, StorageService) => {
			InventoryStateService.setDatasetsDisplayMode({ mode: 'large' });

			expect(inventoryState.datasetsDisplayMode).toBe('large');
			expect(StorageService.setListsDisplayModes).toHaveBeenCalledWith({
				datasets: 'large',
				preparations: 'table',
			});

			storageSpy.calls.reset();

			InventoryStateService.setDatasetsDisplayMode({ mode: 'large' });
			expect(StorageService.setListsDisplayModes).not.toHaveBeenCalled();
		}));
	});

	describe('preparation', () => {
		it('should set display mode', inject((inventoryState, InventoryStateService) => {
			// given
			expect(inventoryState.preparationsDisplayMode).toBe('table');
			const displayMode = { mode: 'tile' };

			// when
			InventoryStateService.setPreparationsDisplayMode(displayMode);

			// then
			expect(inventoryState.preparationsDisplayMode).toBe('tile');
		}));

		it('should enable edit mode', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.folder.content.preparations = preparations;

			// when
			InventoryStateService.enableEdit(preparations[1]);

			// then
			expect(inventoryState.folder.content.preparations[0].displayMode).toBe(undefined);
			expect(inventoryState.folder.content.preparations[1].displayMode).toBe('input');
		}));

		it('should disable edit mode', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.folder.content.preparations = preparations;
			inventoryState.folder.content.preparations[1].displayMode = 'input';

			// when
			InventoryStateService.disableEdit(preparations[1]);

			// then
			expect(inventoryState.folder.content.preparations[1].displayMode).toBe(undefined);
		}));

		it('should set preparations list display mode and persist it if different from actual', inject((inventoryState, InventoryStateService, StorageService) => {
			InventoryStateService.setPreparationsDisplayMode({ mode: 'large' });

			expect(inventoryState.preparationsDisplayMode).toBe('large');
			expect(StorageService.setListsDisplayModes).toHaveBeenCalledWith({
				preparations: 'large',
				datasets: 'table',
			});

			storageSpy.calls.reset();

			InventoryStateService.setPreparationsDisplayMode({ mode: 'large' });
			expect(StorageService.setListsDisplayModes).not.toHaveBeenCalled();
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
			inventoryState.homeFolder = { id: 'Lw=='};
			inventoryState.folder = {
				metadata: { id: 'myFolderId' },
				content: {
					folders: [],
					preparations: [],
				},
			};
			const homeId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			InventoryStateService.setHomeFolderId(homeId);

			//then
			expect(inventoryState.homeFolder.id).toBe(homeId);
		}));

		it('should set current home folder to user\'s home folder id', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.homeFolder = { id: 'Lw=='};
			inventoryState.folder = {
				metadata: { id: 'Lw==' },
				content: {
					folders: [],
					preparations: [],
				},
			};
			const homeId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			InventoryStateService.setHomeFolderId(homeId);

			//then
			expect(inventoryState.folder.metadata.id).toBe(homeId);
		}));

		it('should enable edit mode', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.folder.content.folders = folders;

			// when
			InventoryStateService.enableEdit(folders[1]);

			// then
			expect(inventoryState.folder.content.folders[0].displayMode).toBe(undefined);
			expect(inventoryState.folder.content.folders[1].displayMode).toBe('input');
		}));

		it('should disable edit mode', inject((inventoryState, InventoryStateService) => {
			// given
			inventoryState.folder.content.folders = folders;
			inventoryState.folder.content.folders[1].displayMode = 'input';

			// when
			InventoryStateService.disableEdit(folders[1]);

			// then
			expect(inventoryState.folder.content.folders[1].displayMode).toBe(undefined);
		}));
	});

	describe('sort/order', () => {
		it('should set datasets sort', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.datasets.sort = {
				field: 'date',
				isDescending: false,
			};

			//when
			InventoryStateService.setDatasetsSort('name', true);

			//then
			expect(inventoryState.datasets.sort).toEqual({
				field: 'name',
				isDescending: true,
			});
		}));

		it('should set preparations sort', inject((inventoryState, InventoryStateService) => {
			//given
			inventoryState.folder.sort = {
				field: 'date',
				isDescending: false,
			};

			//when
			InventoryStateService.setPreparationsSort('name', true);

			//then
			expect(inventoryState.folder.sort).toEqual({
				field: 'name',
				isDescending: true,
			});
		}));
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
