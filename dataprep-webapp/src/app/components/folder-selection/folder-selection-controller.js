/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
class FolderSelectionCtrl {
    constructor($translate, FolderService) {
        'ngInject';

        this.$translate = $translate;
        this.folderService = FolderService;
        this.folderItems = [];
        this.tree = [];
        this.searchFolderQuery = '';
    }

    /**
     * @ngdoc method
     * @name $onInit
     * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
     * @description initializes the folders tree on controller creation
     **/
    $onInit() {
        this._initTree();
    }

    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------- INITIALIZATION ----------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name $onInit
     * @description initializes the folders tree
     **/
    _initTree() {
        const rootFolder = {
            path: '',
            level: 0,
            childrenFetched: true,
            display: true,
            collapsed: false,
            name: this.$translate.instant('HOME_FOLDER'),
            originalFolder: {
                path: '',
                name: this.$translate.instant('HOME_FOLDER')
            }
        };
        this.tree = [rootFolder];
        if (this.selectedFolder) {
            let path = this.selectedFolder.path;
            let pathParts = path === '/' ? [''] : path.split('/').filter((part) => part);
            this._locateFolder(this.tree[0], pathParts, '');
        }
        else {
            this._locateFolder(this.tree[0], [''], '');
        }

        this.folderItems = this.tree;
    }

    /**
     * @ngdoc method
     * @name _locateFolder
     * @description locates a dataset into the folders tree
     * @param {object} currentFolder where the dataset exists
     * @param {Array} childPathParts different path parts
     * @param {string} currentFolderPath current path
     **/
    _locateFolder(currentFolder, childPathParts, currentFolderPath) {
        const childName = childPathParts.shift();
        const nextFolderPath = currentFolderPath + '/' + childName;
        this._getChildrenIntoTree(currentFolder)
            .then((children) => {
                const nextFolder = _.find(children, {name: childName});
                if (nextFolder) {
                    if (childPathParts.length) {
                        nextFolder.collapsed = false;
                        nextFolder.childrenFetched = true;
                        this._locateFolder(nextFolder, childPathParts, nextFolderPath);
                    }
                    else {
                        nextFolder.selected = true;
                        this.selectedFolder = nextFolder;
                    }
                }
                else {
                    currentFolder.selected = true;
                    this.selectedFolder = currentFolder;
                }
            });
    }


    //------------------------------------------------------------------------------------------------------------------
    //---------------------------------------------- TREE POPULATION ---------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name _getChildrenIntoTree
     * @description fetches the children of a given folder
     * @param {object} currentFolder the given folder
     **/
    _getChildrenIntoTree(currentFolder) {
        return this.folderService.children(currentFolder.path)
            .then((children) => this._adaptFolderToItem(children, currentFolder.level + 1))
            .then((children) => {
                this._insertFolderChildren(currentFolder, children);
                return children;
            });
    }

    /**
     * @ngdoc method
     * @name _adaptFolderToItem
     * @description adapts the children with the right labels
     * @param {Array} folders the children recently fetched from the server
     * @param {Number} level of the parent
     **/
    _adaptFolderToItem(folders, level) {
        return _.map(folders, (child) => {
            return {
                originalFolder: child,
                name: child.name,
                path: child.path,
                level: level,
                collapsed: true,
                childrenFetched: false,
                display: true
            };
        });
    }

    /**
     * @ngdoc method
     * @name _insertFolderChildren
     * @description inserts children at the right position of the tree
     * @param {Object} parentFolder the children parent
     * @param {Array} children to insert
     **/
    _insertFolderChildren(parentFolder, children) {
        if (!children.length) {
            parentFolder.hasNoChildren = true;
            return;
        }
        const currentIndex = _.findIndex(this.tree, parentFolder);
        this.tree.splice(currentIndex + 1, 0, ...children);
    }

    /**
     * @ngdoc method
     * @name _getChildren
     * @description extracts the children of a given folder from the tree
     * @param {Object} parent whose children will be extracted
     **/
    _getChildren(parent) {
        const parentIndex = _.findIndex(this.tree, parent);
        const followingFolders = this.tree.slice(parentIndex + 1);
        let siblingIndex = _.findIndex(followingFolders, (followingFolder) => followingFolder.level <= parent.level);
        if (siblingIndex === -1) {
            siblingIndex = this.tree.length - 1;
        }

        return this.tree.slice(parentIndex + 1, parentIndex + siblingIndex + 1)
    }


    //------------------------------------------------------------------------------------------------------------------
    //-------------------------------------------- ACTIONS ON FOLDERS --------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggle
     * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
     * @description show/hides the children of a given folder in the tree
     * @param {Object} folder whose children will be displayed/hidden
     **/
    toggle(folder) {
        if (folder.collapsed) {
            if (folder.childrenFetched) {
                this._displayChildren(folder);
            }
            else {
                this._getChildrenIntoTree(folder);
                folder.childrenFetched = true;
            }
        }
        else {
            this._hideChildren(folder);
        }
        folder.collapsed = !folder.collapsed;
    }

    /**
     * @ngdoc method
     * @name chooseFolder
     * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
     * @description selects a folder as destination of the move/copy
     * @param {Object} folder to be selected
     **/
    chooseFolder(folder) {
        this.selectedFolder.selected = false;
        this.selectedFolder = folder;
        this.selectedFolder.selected = true;
    }

    /**
     * @ngdoc method
     * @name _displayChildren
     * @description shows the children of a given folder in the tree
     * @param {Object} parent folders-to-display parent
     **/
    _displayChildren(parent) {
        _.chain(this._getChildren(parent))
            .filter((child) => child.level === parent.level + 1)
            .forEach((child) => {
                child.display = true;
                if (!child.collapsed) {
                    this._displayChildren(child);
                }
            })
            .value();
    }

    /**
     * @ngdoc method
     * @name _hideChildren
     * @description hides the children of a given folder in the tree
     * @param {Object} parent folders-to-hide parent
     **/
    _hideChildren(parent) {
        _.forEach(this._getChildren(parent), (child) => {
            child.display = false;
        });
    }


    //------------------------------------------------------------------------------------------------------------------
    //------------------------------------------------ SEARCH FOLDERS --------------------------------------------------
    //------------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name populateSearchResult
     * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
     * @description decides to show the folders tree or the search result
     **/
    populateSearchResult() {
        if (this.searchFolderQuery) {
            this._searchFolders()
                .then((result) => {
                    this.folderItems = result;
                    this.selectedFolder = _.find(this.folderItems, {selected: true});
                });
        }
        else {
            this.folderItems = this.tree;
            this.selectedFolder = _.find(this.folderItems, {selected: true});
        }
    }

    /**
     * @ngdoc method
     * @name _searchFolders
     * @description searches for folders corresponding to a given text query
     **/
    _searchFolders() {
        //Add the root folder if it matches the filter
        return this.folderService.search(this.searchFolderQuery)
            .then((response) => {
                let searchResult = [];
                const homePosition = this.$translate.instant('HOME_FOLDER').toLowerCase().indexOf(this.searchFolderQuery.toLowerCase());
                if (homePosition > -1) {
                    const rootFolder = {
                        path: '',
                        display: true,
                        hasNoChildren: true,
                        name: this.$translate.instant('HOME_FOLDER'),
                        originalFolder: {
                            name: this.$translate.instant('HOME_FOLDER'),
                            path: ''
                        }
                    };
                    searchResult.push(rootFolder);
                }
                const items = this._adaptFolderToItem(response, 0)
                    .map((folder) => {
                        folder.hasNoChildren = true;
                        folder.name = folder.originalFolder.path;
                        return folder;
                    });
                searchResult = searchResult.concat(items);

                if (searchResult.length > 0) {
                    searchResult[0].selected = true;
                }
                return searchResult;
            });
    }
}

export default FolderSelectionCtrl;
