/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder selection controller', () => {

    let createController, scope, ctrl;

    beforeEach(angular.mock.module('data-prep.folder-selection'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            'HOME_FOLDER': 'Home'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $componentController) => {
        scope = $rootScope.$new();

        createController = (selectedFolder) => {
            return $componentController('folderSelection',
                {$scope: scope},
                {selectedFolder: selectedFolder});
        };
    }));

    describe('initialization', () => {
        it('should init tree with home folder', inject(($q, FolderService) => {
            //given
            spyOn(FolderService, 'children').and.returnValue($q.when([]));

            //when
            ctrl = createController();
            ctrl.$onInit();

            //then
            expect(ctrl.folderItems[0]).toEqual({
                path: '',
                level: 0,
                childrenFetched: true,
                display: true,
                collapsed: false,
                name: 'Home',
                originalFolder: {
                    path: '',
                    name: 'Home'
                }
            });
        }));

        it('should fetch Home children when selected folder is Home', inject(function ($q, FolderService) {
            //given
            let currentFolder = {name: 'Home', path: '/'};
            spyOn(FolderService, 'children').and.returnValue($q.when([]));

            //when
            ctrl = createController(currentFolder);
            ctrl.$onInit();
            scope.$digest();

            //then
            expect(FolderService.children.calls.count()).toBe(1);
            expect(FolderService.children).toHaveBeenCalledWith('');
            expect(ctrl.selectedFolder).toBe(ctrl.folderItems[0]);
            expect(ctrl.selectedFolder.selected).toBe(true);
            expect(ctrl.selectedFolder.childrenFetched).toBe(true);
        }));

        it('should fetch folders from Home to current folder (level 1)', inject(($q, FolderService) => {
            //given
            const currentFolder = {name: 'lookup', path: 'lookup'};
            spyOn(FolderService, 'children').and.returnValue($q.when([currentFolder]));

            //when
            ctrl = createController(currentFolder);
            ctrl.$onInit();
            scope.$digest();

            //then
            expect(FolderService.children.calls.count()).toBe(1);
            expect(FolderService.children).toHaveBeenCalledWith('');
            expect(ctrl.selectedFolder.selected).toBe(true);
        }));

        it('should fetch folders from Home to current folder (level 3)', inject(($q, FolderService) => {
            //given
            const currentFolder = {name: 'subSubFolder', path: 'folder/subFolder/subSubFolder'};
            let counter = 0;
            spyOn(FolderService, 'children').and.callFake(() => {
                if (counter === 0) {
                    counter++;
                    return $q.when([{name: 'folder', path: 'folder'}]);
                }
                else if (counter === 1) {
                    counter++;
                    return $q.when([{name: 'subFolder', path: 'folder/subFolder'}]);
                }
                else if (counter === 2) {
                    return $q.when([currentFolder])
                }
            });

            //when
            ctrl = createController(currentFolder);
            ctrl.$onInit();
            scope.$digest();

            //then
            expect(FolderService.children.calls.count()).toBe(3);
            expect(FolderService.children).toHaveBeenCalledWith('');
            expect(FolderService.children).toHaveBeenCalledWith('folder');
            expect(FolderService.children).toHaveBeenCalledWith('folder/subFolder');
            expect(ctrl.selectedFolder.selected).toBe(true);
        }));
    });

    describe('toggle', () => {

        let homeFolder, folderToToggle, anotherFolder, folderChildren;

        beforeEach(() => {
            homeFolder = {
                path: '',
                level: 0,
                childrenFetched: true,
                display: true,
                collapsed: false,
                name: 'Home'
            };
            folderToToggle = {
                name: '1-folder11',
                path: '1-folder11',
                level: 1,
                display: true
            };
            anotherFolder = {
                name: '1-folder12',
                path: '1-folder12',
                level: 1,
                display: true
            };
            folderChildren = [
                {
                    path: '1-folder11/2-folder111',
                    name: '2-folder111',
                    level: 2
                },
                {
                    path: '1-folder11/2-folder112',
                    name: '2-folder112',
                    level: 2
                },
                {
                    path: '1-folder11/2-folder113',
                    name: '2-folder113',
                    level: 2
                }
            ];

            const currentFolder = {name: 'Home', path: '/'};
            ctrl = createController(currentFolder);
        });

        describe('with children', () => {
            beforeEach(inject(($q, FolderService) => {
                spyOn(FolderService, 'children').and.returnValue($q.when(folderChildren));
            }));

            it('should fetch and show folder\'s children', () => {
                //given
                ctrl.folderItems = [
                    homeFolder,
                    folderToToggle,
                    anotherFolder
                ];
                ctrl.tree = ctrl.folderItems;

                folderToToggle.collapsed = true;
                folderToToggle.childrenFetched = false;

                //when
                ctrl.toggle(folderToToggle);
                scope.$digest();

                //then
                expect(ctrl.folderItems.length).toBe(6);

                expect(ctrl.folderItems[0]).toBe(homeFolder);
                expect(ctrl.folderItems[1]).toBe(folderToToggle);
                expect(ctrl.folderItems[2].originalFolder).toBe(folderChildren[0]);
                expect(ctrl.folderItems[3].originalFolder).toBe(folderChildren[1]);
                expect(ctrl.folderItems[4].originalFolder).toBe(folderChildren[2]);
                expect(ctrl.folderItems[5]).toBe(anotherFolder);

                expect(ctrl.folderItems[2].level).toBe(2);
                expect(ctrl.folderItems[2].collapsed).toBe(true);
                expect(ctrl.folderItems[2].display).toBe(true);

                expect(ctrl.folderItems[3].level).toBe(2);
                expect(ctrl.folderItems[3].collapsed).toBe(true);
                expect(ctrl.folderItems[3].display).toBe(true);

                expect(ctrl.folderItems[4].level).toBe(2);
                expect(ctrl.folderItems[4].collapsed).toBe(true);
                expect(ctrl.folderItems[4].display).toBe(true);

                expect(folderToToggle.collapsed).toBe(false);
                expect(anotherFolder.collapsed).toBeFalsy();
            });

            it('should hide folder\'s children, case when parent has no siblings', () => {
                //given
                folderToToggle.collapsed = false;
                ctrl.folderItems = [
                    homeFolder,
                    folderToToggle,
                    ...folderChildren
                ];
                ctrl.folderItems.forEach((folder) => {
                    folder.display = true;
                });
                ctrl.tree = ctrl.folderItems;

                //when
                ctrl.toggle(folderToToggle);

                //then
                expect(ctrl.folderItems[0].display).toBe(true); //home
                expect(ctrl.folderItems[1].display).toBe(true); //folder to toggle
                expect(ctrl.folderItems[2].display).toBe(false); //children
                expect(ctrl.folderItems[3].display).toBe(false); //children
                expect(ctrl.folderItems[4].display).toBe(false); //children
                expect(folderToToggle.collapsed).toBe(true);
            });

            it('should hide folder\'s children, case when parent has siblings', () => {
                //given
                folderToToggle.collapsed = false;
                ctrl.folderItems = [
                    homeFolder,
                    folderToToggle,
                    ...folderChildren,
                    anotherFolder
                ];
                ctrl.folderItems.forEach((folder) => {
                    folder.display = true;
                });
                ctrl.tree = ctrl.folderItems;

                //when
                ctrl.toggle(folderToToggle);

                //then
                expect(ctrl.folderItems[0].display).toBe(true); //home
                expect(ctrl.folderItems[1].display).toBe(true); //folder to toggle
                expect(ctrl.folderItems[2].display).toBe(false); //children
                expect(ctrl.folderItems[3].display).toBe(false); //children
                expect(ctrl.folderItems[4].display).toBe(false); //children
                expect(ctrl.folderItems[5].display).toBe(true); //another folder
                expect(folderToToggle.collapsed).toBe(true);
            });

            it('should show the same subfolders before their parent has been collapsed', () => {
                //given
                folderToToggle.collapsed = true;        // level 1 folder is collapsed
                folderToToggle.childrenFetched = true;
                folderChildren.forEach((child) => {     // all level 2 folders are hidden
                    child.display = false;
                });
                folderChildren[2].collapsed = false;    // last level 2 folder is opened
                const level3Child = {                   // last level 2 folder's child
                    name: '2-folder1131',
                    path: '1-folder11/2-folder113/3-folder1131',
                    level: 3,
                    display: false
                };

                ctrl.folderItems = [
                    homeFolder,
                    folderToToggle,
                    ...folderChildren,
                    level3Child
                ];
                ctrl.tree = ctrl.folderItems;

                //when
                ctrl.toggle(folderToToggle);

                //then
                expect(ctrl.folderItems[2].display).toBe(true);
                expect(ctrl.folderItems[3].display).toBe(true);
                expect(ctrl.folderItems[4].display).toBe(true);
                expect(ctrl.folderItems[5].display).toBe(true); // last level 2 folder's child is displayed because its parent is opened
            });
        });

        describe('with no children', () => {
            it('should still set folder\'s childrenFetched flag', inject(($q, FolderService) => {
                //given
                spyOn(FolderService, 'children').and.returnValue($q.when([]));
                ctrl.folderItems = [
                    homeFolder,
                    folderToToggle
                ];
                ctrl.tree = ctrl.folderItems;

                folderToToggle.collapsed = true;
                folderToToggle.childrenFetched = false;

                //when
                ctrl.toggle(folderToToggle);
                scope.$digest();

                //then
                expect(folderToToggle.childrenFetched).toBe(true);
                expect(folderToToggle.collapsed).toBe(false);
                expect(ctrl.folderItems.length).toBe(2);
            }));
        });
    });

    describe('chooseFolder', () => {
        it('should set the destination folder as the selected one', () => {
            //given
            var previousSelectedFolder = {selected: true};
            ctrl = createController(previousSelectedFolder);
            var folder = {};

            //when
            ctrl.chooseFolder(folder);

            //then
            expect(previousSelectedFolder.selected).toBe(false);
            expect(ctrl.selectedFolder).toBe(folder);
            expect(ctrl.selectedFolder.selected).toBe(true);
        })
    });

    describe('search', () => {
        describe('with matching results', () => {
            var foldersSearchResult = [
                {path: 'folder-1', name: 'folder-1'},
                {path: 'folder-1/sub-1', name: 'sub-1'},
                {path: 'folder-1/sub-2/folder-1-beer', name: 'folder-1-beer'}
            ];

            beforeEach(inject(($q, FolderService) => {
                ctrl = createController();
                spyOn(FolderService, 'search').and.returnValue($q.when(foldersSearchResult));
            }));

            it('should call search folders service', inject((FolderService) => {
                //given
                ctrl.searchFolderQuery = 'beer';

                //when
                ctrl.populateSearchResult();

                //then
                expect(FolderService.search).toHaveBeenCalledWith('beer');
            }));

            it('should add home folder in result if it match the search query', () => {
                //given
                ctrl.searchFolderQuery = 'e';

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.folderItems.length).toBe(foldersSearchResult.length + 1);
                expect(ctrl.folderItems[0].name).toBe('Home');
            });

            it('should adapt search result and populate folder items', () => {
                //given
                ctrl.searchFolderQuery = 'f';

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.folderItems.length).toBe(foldersSearchResult.length);
                expect(ctrl.folderItems[0].originalFolder).toBe(foldersSearchResult[0]);
                expect(ctrl.folderItems[1].originalFolder).toBe(foldersSearchResult[1]);
                expect(ctrl.folderItems[2].originalFolder).toBe(foldersSearchResult[2]);
            });

            it('should select the 1st result by default', () => {
                //given
                ctrl.searchFolderQuery = 'f';

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.selectedFolder.originalFolder).toBe(foldersSearchResult[0]);
                expect(ctrl.selectedFolder.selected).toBe(true);
            });

            it('should set back the tree when search query is empty', () => {
                //given
                ctrl.searchFolderQuery = '';
                ctrl.tree = [{
                    path: '',
                    display: true,
                    hasNoChildren: true,
                    name: 'Home',
                    originalFolder: {
                        name: 'Home',
                        path: ''
                    }
                }];

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.folderItems).toBe(ctrl.tree);
            });
        });

        describe('without matching results', () => {
            beforeEach(inject(($q, FolderService) => {
                ctrl = createController();
                spyOn(FolderService, 'search').and.returnValue($q.when([]));
            }));

            it('should reset selected folder', () => {
                //given
                ctrl.searchFolderQuery = 'f';

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.selectedFolder).toBe(undefined);
            });

            it('should not show any folder', () => {
                //given
                ctrl.searchFolderQuery = 'f';

                //when
                ctrl.populateSearchResult();
                scope.$digest();

                //then
                expect(ctrl.folderItems.length).toBe(0);
            });
        });
    });
});