/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder selection controller', () => {
	let createController;
	let scope;
	const tree = {
		folder: { // HOME
			id: '1',
			name: 'HOME',
			path: '/',
		},
		children: [
			{
				folder: { // /folder1
					id: '2',
					name: 'folder1',
					path: '/folder1',
				},
				children: [
					{
						folder: { // /folder1/subfolder1
							id: '4',
							name: 'subfolder1',
							path: '/folder1/subfolder1',
						},
						children: [],
					},
					{
						folder: { // /folder1/subfolder2
							id: '5',
							name: 'subfolder2',
							path: '/folder1/subfolder2',
						},
						children: [],
					},
				],
			},
			{
				folder: { // /folder2
					id: '3',
					name: 'folder2',
					path: '/folder2',
				},
				children: [
					{
						folder: { // /folder2/subfolder3
							id: '6',
							name: 'subfolder3',
							path: '/folder2/subfolder3',
						},
						children: [],
					},
				],
			},
		],
	};

	beforeEach(angular.mock.module('data-prep.folder-selection'));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = (selectedFolder, tree) => {
			return $componentController('folderSelection',
				{ $scope: scope },
				{ selectedFolder: selectedFolder, isLoading: false, tree: tree })
		}
	}));

	describe('initialization', () => {
		it('should open path to selected folder', () => {
			// given
			const currentFolder = { id: '5' };
			const ctrl = createController(currentFolder, tree);

			// when
			ctrl.$onChanges();
			scope.$digest();

			// then
			expect(ctrl.tree.folder.opened).toBe(true);
			expect(ctrl.tree.children[0].folder.opened).toBe(true);
			expect(ctrl.tree.children[1].folder.opened).toBeFalsy();
		});

		it('should update selected folder', () => {
			// given
			const currentFolder = { id: '5' };
			const ctrl = createController(currentFolder, tree);

			// when
			ctrl.$onChanges();
			scope.$digest();

			// then
			expect(ctrl.tree.children[0].children[1].folder.selected).toBe(true);
			expect(ctrl.selectedFolder).toBe(tree.children[0].children[1].folder);
		});

		it('should select HOME when ngModel is not set', () => {
			// given
			const ctrl = createController(null, tree);

			// when
			ctrl.$onChanges();
			scope.$digest();

			// then
			expect(ctrl.tree.folder.selected).toBe(true);
			expect(ctrl.selectedFolder).toBe(tree.folder);
		});

		it('should select HOME when ngModel folder id not in tree', () => {
			// given
			const currentFolder = { id: 'unknown' };
			const ctrl = createController(currentFolder, tree);

			// when
			ctrl.$onChanges();
			scope.$digest();

			// then
			expect(ctrl.tree.folder.selected).toBe(true);
			expect(ctrl.selectedFolder).toBe(tree.folder);
		});
	});

	describe('toggle', () => {
		it('should toggle the folder opened flag', () => {
			// given
			const ctrl = createController(null, tree);
			const node = { folder: { opened: false } };

			// when
			ctrl.toggle(node);

			// then
			expect(node.folder.opened).toBe(true);
		});
	});

	describe('choose folder', () => {
		it('should unselect previous folder', () => {
			// given
			const ctrl = createController(null, tree);
			const previousFolder = { selected: true };
			ctrl.selectedFolder = previousFolder;
			const nextFolder = { selected: false };

			// when
			ctrl.chooseFolder(nextFolder);

			// then
			expect(previousFolder.selected).toBe(false);
		});

		it('should update selectedFolder', () => {
			// given
			const ctrl = createController(null, tree);
			ctrl.selectedFolder = { selected: true };
			const nextFolder = { selected: false };

			// when
			ctrl.chooseFolder(nextFolder);

			// then
			expect(ctrl.selectedFolder).toBe(nextFolder);
		});

		it('should update new selected folder flag', () => {
			// given
			const ctrl = createController(null, tree);
			ctrl.selectedFolder = { selected: true };
			const nextFolder = { selected: false };

			// when
			ctrl.chooseFolder(nextFolder);

			// then
			expect(nextFolder.selected).toBe(true);
		});
	});

	describe('search', () => {
		let ctrl;

		beforeEach(() => {
			ctrl = createController(null, tree);
			ctrl.$onChanges();
			scope.$digest();
		});

		it('should init search items with search result', () => {
			// given
			ctrl.searchFolderQuery = 'folder1';

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.searchItems).toEqual([tree.children[0], tree.children[0].children[0]]);
		});

		it('should init perform search with empty array when search result is empty', () => {
			// given
			ctrl.searchFolderQuery = 'toto';

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.searchItems).toEqual([]);
		});

		it('should save previously selected folder', () => {
			// given
			ctrl.searchFolderQuery = 'folder1';
			expect(ctrl.selectedFolder).toBe(tree.folder);
			expect(ctrl.lastTreeSelection).toBeUndefined();

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.lastTreeSelection).toBe(tree.folder);
		});

		it('should select first folder in result', () => {
			// given
			ctrl.searchFolderQuery = 'folder1';
			expect(ctrl.selectedFolder).toBe(tree.folder);

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.selectedFolder).toBe(tree.children[0].folder);
		});

		it('should reset search items when search query is empty', () => {
			// given
			ctrl.searchFolderQuery = '';
			ctrl.searchItems = [];
			ctrl.lastTreeSelection = { id: '5', selected: true };

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.searchItems).toBe(null);
		});

		it('should set back saved folder as selected when search query is empty', () => {
			// given
			ctrl.searchFolderQuery = '';
			const savedFolder = { id: '5', selected: true };
			ctrl.lastTreeSelection = savedFolder;

			// when
			ctrl.performSearch();

			// then
			expect(ctrl.selectedFolder).toBe(savedFolder);
			expect(ctrl.lastTreeSelection).toBe(null);
		});
	});
});
