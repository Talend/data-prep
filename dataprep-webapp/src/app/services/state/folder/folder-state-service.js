/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export const folderState = {
    currentFolder: {path: ''}, // currentFolder is initialized with root value
    currentFolderContent: {},
    foldersStack: [],
    menuChildren: []
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:FolderStateService
 * @description Folder state service. Manage the folder state
 */
export function FolderStateService() {
    return {
        setCurrentFolder: setCurrentFolder,
        setCurrentFolderContent: setCurrentFolderContent,
        setFoldersStack: setFoldersStack,
        setMenuChildren: setMenuChildren
    };

    /**
     * @ngdoc method
     * @name setCurrentFolder
     * @methodOf data-prep.services.state.service:FolderStateService
     * @param {object} folder The current folder
     * @description Update the current folder
     */
    function setCurrentFolder(folder) {
        folderState.currentFolder = folder;
    }

    /**
     * @ngdoc method
     * @name setCurrentFolderContent
     * @methodOf data-prep.services.state.service:FolderStateService
     * @param {object} children The content of the current folder
     */
    function setCurrentFolderContent(children) {
        folderState.currentFolderContent = children;
    }

    /**
     * @ngdoc method
     * @name setFoldersStack
     * @methodOf data-prep.services.state.service:FolderStateService
     * @param {object} stack The current folders stack
     */
    function setFoldersStack(stack) {
        folderState.foldersStack = stack;
    }

    /**
     * @ngdoc method
     * @name setMenuChildren
     * @methodOf data-prep.services.state.service:FolderStateService
     * @param {array} children The current children of the current menu entry
     */
    function setMenuChildren(children) {
        folderState.menuChildren = children;
    }
}