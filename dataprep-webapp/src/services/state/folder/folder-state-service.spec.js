describe('Folder state service', function () {
	'use strict';

	var currentFolder = {
		'id': 'lookups',
		'path': 'lookups',
		'name': 'lookups',
		'creationDate': 1448880133000,
		'modificationDate': 1448880133000
	};

	var currentFolderContent = {
		'folders': [
			{
				'id': 'lookups/simple_lookup'
			}
		],
		'datasets': [
			{
				'id': '601f9785-c5b1-403f-bba9-952d8f674a37',
				'favorite': false,
				'lifecycle': {
					'inProgress': false
				},
				'records': 50
			}
		]
	};

	var foldersStack = [
		{'id':'','path':'','name':'Home'},
		{'id':'lookups','path':'lookups','name':'lookups'}
	];

	var menuChildren = [
		{
			'id':'lookups/simple_lookup',
			'path':'lookups/simple_lookup',
			'name':'simple_lookup',
			'creationDate':1448880158000,
			'modificationDate':1448880158000
		}
	];

	beforeEach(module('data-prep.services.state'));

	describe('setters', function () {
		it('should set CurrentFolder', inject(function (folderState, FolderStateService) {
			//given
			folderState.currentFolder = null;

			//when
			FolderStateService.setCurrentFolder(currentFolder);

			//then
			expect(folderState.currentFolder).toBe(currentFolder);
		}));

		it('should set currentFolderContent', inject(function (folderState, FolderStateService) {
			//given
			folderState.currentFolderContent = null;

			//when
			FolderStateService.setCurrentFolderContent(currentFolderContent);

			//then
			expect(folderState.currentFolderContent).toBe(currentFolderContent);
		}));

		it('should set foldersStack', inject(function (folderState, FolderStateService) {
			//given
			folderState.foldersStack = [];

			//when
			FolderStateService.setFoldersStack(foldersStack);

			//then
			expect(folderState.foldersStack).toBe(foldersStack);
		}));

		it('should set menuChildren', inject(function (folderState, FolderStateService) {
			//given
			folderState.menuChildren = [];

			//when
			FolderStateService.setMenuChildren(menuChildren);

			//then
			expect(folderState.menuChildren).toBe(menuChildren);
		}));
	});
});
