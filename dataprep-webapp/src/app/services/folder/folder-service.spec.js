/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const preparation = {
	id: '1',
	dataset: {
		dataSetName: 'US states',
		dataSetNbRow: 400,
	},
	name: 'JSO prep 1',
	owner: { displayName: 'toto' },
	creationDate: new Date().getTime(),
	lastModificationDate: new Date().getTime(),
	steps: [
		'35890aabcf9115e4309d4ce93367bf5e4e77b82a',
		'4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
		'8a1c49d1b64270482e8db8232357c6815615b7cf',
		'599725f0e1331d5f8aae24f22cd1ec768b10348d',
	],
};
const getAdaptedPreparation = () => {
	return {
		id: '1',
		type: 'preparation',
		name: 'JSO prep 1',
		author: 'toto',
		creationDate: moment(preparation.creationDate).fromNow(),
		lastModificationDate: moment(preparation.lastModificationDate).fromNow(),
		datasetName: 'US states',
		nbSteps: 3,
		icon: 'talend-dataprep',
		displayMode: 'text',
		className: 'list-item-preparation',
		actions: ['inventory:edit', 'preparation:copy-move', 'preparation:remove'],
		model: preparation,
	};
};

const folder = {
	id: 'Lw==',
	path: 'toto',
	name: 'toto',
	owner: { displayName: 'toto' },
	creationDate: new Date().getTime(),
	lastModificationDate: new Date().getTime(),
};
const getAdaptedFolder = () => {
	return {
		id: 'Lw==',
		type: 'folder',
		name: 'toto',
		author: 'toto',
		creationDate: moment(folder.creationDate).fromNow(),
		lastModificationDate: moment(folder.lastModificationDate).fromNow(),
		icon: 'talend-folder',
		displayMode: 'text',
		className: 'list-item-folder',
		actions: ['inventory:edit', 'preparation:folder:remove'],
		model: folder,
	};
};

describe('Folder services', () => {
	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.folder', ($provide) => {
		stateMock = {
			inventory: {
				homeFolder: { id: 'L215L2ZvbGRlcg=='},
				folder: {
					sort: { field: 'name', isDescending: false },
					metadata: { id: '3a574ba62c69' },
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	describe('simple REST calls', () => {
		beforeEach(inject(($q, FolderRestService) => {
			spyOn(FolderRestService, 'create').and.returnValue($q.when());
			spyOn(FolderRestService, 'children').and.returnValue($q.when());
			spyOn(FolderRestService, 'rename').and.returnValue($q.when());
			spyOn(FolderRestService, 'remove').and.returnValue($q.when());
		}));

		it('should call rest children', inject((FolderService, FolderRestService) => {
			//given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			FolderService.children(folderId);

			//then
			expect(FolderRestService.children).toHaveBeenCalledWith(folderId);
		}));

		it('should call rest children with home folder by default', inject((FolderService, FolderRestService) => {
			//when
			FolderService.children();

			//then
			expect(FolderRestService.children).toHaveBeenCalledWith(stateMock.inventory.homeFolder.id);
		}));

		it('should call rest create', inject((FolderService, FolderRestService) => {
			//given
			const folderName = 'azerty';
			const parentId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			FolderService.create(parentId, folderName);

			//then
			expect(FolderRestService.create).toHaveBeenCalledWith(parentId, folderName);
		}));

		it('should call rest create with default folder', inject((FolderService, FolderRestService) => {
			//given
			const name = 'azerty';

			//when
			FolderService.create(undefined, name);

			//then
			expect(FolderRestService.create).toHaveBeenCalledWith(stateMock.inventory.homeFolder.id, name);
		}));

		it('should call rest rename', inject((FolderService, FolderRestService) => {
			//given
			const newName = 'azerty';
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			FolderService.rename(folderId, newName);

			//then
			expect(FolderRestService.rename).toHaveBeenCalledWith(folderId, newName);
		}));

		it('should call rest rename', inject((FolderService, FolderRestService) => {
			//given
			const newName = 'azerty';

			//when
			FolderService.rename(undefined, newName);

			//then
			expect(FolderRestService.rename).toHaveBeenCalledWith(stateMock.inventory.homeFolder.id, newName);
		}));
	});

	describe('init', () => {
		it('should start onboarding', inject(($rootScope, $q, $timeout, FolderService, OnboardingService) => {
			// given
			spyOn(OnboardingService, 'shouldStartTour').and.returnValue(true);
			spyOn(OnboardingService, 'startTour');
			spyOn(FolderService, 'refresh').and.returnValue($q.when());

			// when
			FolderService.init('/my/path');
			$rootScope.$digest();
			$timeout.flush(1000);

			// then
			expect(OnboardingService.startTour).toHaveBeenCalledWith('preparation');
		}));

		it('should set the preparation sort when there is a saved one',
			inject((StateService, StorageService, FolderService) => {
				// given
				const savedSort = {
					field: 'date',
					isDescending: true,
				};
				spyOn(StorageService, 'getPreparationsSort').and.returnValue(savedSort);
				spyOn(StateService, 'setPreparationsSort').and.returnValue();

				// when
				FolderService.init('/my/path');

				// then
				expect(StateService.setPreparationsSort).toHaveBeenCalledWith(
					savedSort.field,
					savedSort.isDescending
				);
			})
		);

		it('should NOT set the preparation sort when there is NO saved one', inject((StateService, StorageService, FolderService) => {
			// given
			spyOn(StorageService, 'getPreparationsSort').and.returnValue(null);
			spyOn(StateService, 'setPreparationsSort').and.returnValue();

			// when
			FolderService.init('/my/path');

			// then
			expect(StateService.setPreparationsSort).not.toHaveBeenCalled();
		}));

		it('should refresh folder content', inject(($q, FolderRestService, FolderService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
			spyOn(FolderRestService, 'getContent').and.returnValue($q.when());

			// when
			FolderService.init(folderId);

			// then
			expect(FolderRestService.getContent).toHaveBeenCalledWith(folderId, 'name', 'asc');
		}));

		it('should refresh folder content with home folder by default', inject(($q, FolderRestService, FolderService) => {
			// given
			spyOn(FolderRestService, 'getContent').and.returnValue($q.when());

			// when
			FolderService.init();

			// then
			expect(FolderRestService.getContent).toHaveBeenCalledWith(stateMock.inventory.homeFolder.id, 'name', 'asc');
		}));
	});

	describe('refresh', () => {
		const folderMetadata = {
			folder: { id: 'L215L3BlcnNvbmFsL2ZvbGRlcg==' },
			hierarchy: [
				{ id: 'L215' },
				{ id: 'L215L3BlcnNvbmFs' },
			],
		};
		const content = {
			folders: [folder],
			preparations: [preparation],
		};

		beforeEach(inject(($q, StateService, FolderRestService) => {
			spyOn(FolderRestService, 'getById').and.returnValue($q.when(folderMetadata));
			spyOn(FolderRestService, 'getContent').and.returnValue($q.when(content));
			spyOn(StateService, 'setFolder').and.returnValue();
			spyOn(StateService, 'setBreadcrumb').and.returnValue();
		}));

		it('should get content with sort and order', inject((FolderService, FolderRestService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
			stateMock.inventory.folder.sort = {
				field: 'date',
				isDescending: true,
			};

			// when
			FolderService.refresh(folderId);

			// then
			expect(FolderRestService.getContent).toHaveBeenCalledWith(folderId, 'date', 'desc');
		}));

		it('should get folder metadata', inject((FolderService, FolderRestService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			// when
			FolderService.refresh(folderId);

			// then
			expect(FolderRestService.getById).toHaveBeenCalledWith(folderId);
		}));

		it('should set the folder adapted content in app state',
			inject(($rootScope, FolderService, StateService) => {
				// given
				const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
				const adaptedContent = {
					folders: [getAdaptedFolder()],
					preparations: [getAdaptedPreparation()],
				};

				// when
				FolderService.refresh(folderId);
				$rootScope.$digest();

				// then
				expect(StateService.setFolder).toHaveBeenCalledWith(folderMetadata.folder, adaptedContent);
			})
		);

		it('should get folder metadata', inject(($rootScope, StateService, FolderService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			// when
			FolderService.refresh(folderId);
			$rootScope.$digest();

			// then
			expect(StateService.setBreadcrumb).toHaveBeenCalledWith(folderMetadata.hierarchy.concat(folderMetadata.folder));
		}));

		it('should get folder metadata with homeFolderId', inject((FolderService, FolderRestService) => {
			// given
			stateMock.inventory.homeFolder = { id: 'L215L3BlcnNvbmFsL2ZvbGRlcg=='};

			// when
			FolderService.refresh();

			// then
			expect(FolderRestService.getById).toHaveBeenCalledWith('L215L3BlcnNvbmFsL2ZvbGRlcg==');
		}));
	});

	describe('refreshBreadcrumbChildren', () => {
		const children = [{ id: '5' }];

		beforeEach(inject(($q, FolderRestService, StateService) => {
			spyOn(FolderRestService, 'children').and.returnValue($q.when(children));
			spyOn(StateService, 'setBreadcrumbChildren').and.returnValue();
		}));

		it('should fetch folder children', inject((FolderService, FolderRestService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
			expect(FolderRestService.children).not.toHaveBeenCalled();

			// when
			FolderService.refreshBreadcrumbChildren(folderId);

			// then
			expect(FolderRestService.children).toHaveBeenCalledWith(folderId);
		}));

		it('should set folder children as breadcrumb item children in app state', inject(($rootScope, StateService, FolderService) => {
			// given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';
			expect(StateService.setBreadcrumbChildren).not.toHaveBeenCalledWith();

			// when
			FolderService.refreshBreadcrumbChildren(folderId);
			$rootScope.$digest();

			// then
			expect(StateService.setBreadcrumbChildren).toHaveBeenCalledWith(
				folderId,
				children
			);
		}));
	});

	describe('adaptPreparations', () => {
		it('should wrap preparation', inject((FolderService) => {
			// when
			const actualAdaption = FolderService.adaptPreparations([preparation]);

			// then
			expect(actualAdaption).toEqual([getAdaptedPreparation()]);
		}));
	});

	describe('adaptFolders', () => {
		it('should wrap folder', inject((FolderService) => {
			// when
			const actualAdaptation = FolderService.adaptFolders([folder]);

			// then
			expect(actualAdaptation).toEqual([getAdaptedFolder()]);
		}));
	});

	describe('getPreparationActions', () => {
		it('should return fixed preparation actions', inject((FolderService) => {
			// when
			const actions = FolderService.getPreparationActions();

			// then
			expect(actions).toEqual(['inventory:edit', 'preparation:copy-move', 'preparation:remove']);
		}));
	});

	describe('getFolderActions', () => {
		it('should return fixed folder actions', inject((FolderService) => {
			// when
			const actions = FolderService.getFolderActions();

			// then
			expect(actions).toEqual(['inventory:edit', 'preparation:folder:remove']);
		}));
	});

	describe('changeSort', () => {
		let refreshMock;
		const sort = {
			field: 'date',
			isDescending: true,
		};

		beforeEach(inject(($q, FolderService, StateService, StorageService) => {
			refreshMock = spyOn(FolderService, 'refresh');
			spyOn(StorageService, 'setPreparationsSort').and.returnValue();
			spyOn(StateService, 'setPreparationsSort').and.returnValue();
		}));

		it('should set sort in app state',
			inject(($q, StateService, FolderService) => {
				// given
				refreshMock.and.returnValue($q.when());

				// when
				FolderService.changeSort(sort);

				// then
				expect(StateService.setPreparationsSort).toHaveBeenCalledWith('date', true);
			})
		);

		it('should refresh current folder',
			inject(($q, StateService, FolderService) => {
				// given
				refreshMock.and.returnValue($q.when());

				// when
				FolderService.changeSort(sort);

				// then
				expect(FolderService.refresh).toHaveBeenCalledWith(stateMock.inventory.folder.metadata.id);
			})
		);

		it('should save sort in local storage',
			inject(($rootScope, $q, StateService, StorageService, FolderService) => {
				// given
				refreshMock.and.returnValue($q.when());

				// when
				FolderService.changeSort(sort);
				$rootScope.$digest();

				// then
				expect(StorageService.setPreparationsSort).toHaveBeenCalledWith('date', true);
			})
		);

		it('should restore sort in app state in case of error',
			inject(($rootScope, $q, StateService, StorageService, FolderService) => {
				// given
				refreshMock.and.returnValue($q.reject());

				// when
				FolderService.changeSort(sort);
				expect(StateService.setPreparationsSort).not.toHaveBeenCalledWith('name', false); // old sort
				$rootScope.$digest();

				// then
				expect(StorageService.setPreparationsSort).not.toHaveBeenCalled();
				expect(StateService.setPreparationsSort).toHaveBeenCalledWith('name', false);
			})
		);
	});

	describe('remove', () => {
		const folder = { id: 'folder 1' };

		beforeEach(inject(($rootScope, $q, FolderRestService, FolderService) => {
			// given
			spyOn(FolderService, 'refresh').and.returnValue();
			spyOn(FolderRestService, 'remove').and.returnValue($q.when());

			// when
			FolderService.remove(folder);
			$rootScope.$digest();
		}));

		it('should remove folder', inject((FolderRestService) => {
			// then
			expect(FolderRestService.remove).toHaveBeenCalledWith(folder.id);
		}));

		it('should refresh current folder', inject((FolderService) => {
			// given
			const currentFolderId = stateMock.inventory.folder.metadata.id;

			// then
			expect(FolderService.refresh).toHaveBeenCalledWith(currentFolderId);
		}));
	});
});
