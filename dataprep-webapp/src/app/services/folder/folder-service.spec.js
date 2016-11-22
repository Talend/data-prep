/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

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
	author: 'anonymousUser',
	creationDate: new Date().getTime(),
	lastModificationDate: new Date().getTime(),
	steps: [
		'35890aabcf9115e4309d4ce93367bf5e4e77b82a',
		'4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
		'8a1c49d1b64270482e8db8232357c6815615b7cf',
		'599725f0e1331d5f8aae24f22cd1ec768b10348d',
	],
};
const adaptedPreparation = {
	id: '1',
	name: 'JSO prep 1',
	author: 'anonymousUser',
	creationDate: 'a few seconds ago',
	lastModificationDate: 'a few seconds ago',
	dataset: 'US states',
	nbLines: 400,
	nbSteps: 3,
	icon: 'talend-dataprep',
	actions: ['preparation:copy-move', 'preparation:remove'],
	model: preparation,
};

describe('Folder services', () => {

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
				homeFolderId: 'L215L2ZvbGRlcg==',

				sortList: sortList,
				orderList: orderList,
				preparationsSort: sortList[0],
				preparationsOrder: orderList[0],
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
			expect(FolderRestService.children).toHaveBeenCalledWith(stateMock.inventory.homeFolderId);
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
			expect(FolderRestService.create).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, name);
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
			expect(FolderRestService.rename).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, newName);
		}));

		it('should call rest remove', inject((FolderService, FolderRestService) => {
			$;
			//given
			const folderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

			//when
			FolderService.remove(folderId);

			//then
			expect(FolderRestService.remove).toHaveBeenCalledWith(folderId);
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
			expect(FolderRestService.getContent).toHaveBeenCalledWith(stateMock.inventory.homeFolderId, 'name', 'asc');
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
			folders: [{ path: 'toto', name: 'toto' }],
			preparations: [preparation],
		};
		const adaptedContent = {
			folders: content.folders,
			preparations: [adaptedPreparation],
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
			stateMock.inventory.preparationsSort = sortList[1];
			stateMock.inventory.preparationsOrder = orderList[1];

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
				stateMock.inventory.preparationsSort = sortList[1];
				stateMock.inventory.preparationsOrder = orderList[1];

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
			stateMock.inventory.homeFolderId = 'L215L3BlcnNvbmFsL2ZvbGRlcg==';

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
			expect(actualAdaption).toEqual([adaptedPreparation]);
		}));
	});

	describe('getPreparationActions', () => {
		it('should return fixed preparation actions', inject((FolderService) => {
			// when
			const actions = FolderService.getPreparationActions()

			// then
			expect(actions).toEqual(['preparation:copy-move', 'preparation:remove']);
		}));
	});
});
