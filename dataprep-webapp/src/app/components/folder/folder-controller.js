/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.folder.controller:FolderCtrl
 * @description Export controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.folder.service:FolderService
 */
export default function FolderCtrl($state, state, StateService, FolderService) {
    'ngInject';

    var vm = this;
    vm.folderService = FolderService;
    vm.state = state;

    /**
     * @ngdoc method
     * @name initMenuChildren
     * @methodOf data-prep.folder.controller:FolderCtrl
     * @param {object} folder - the folder
     * @description build the children of the folder menu entry as parameter
     */
    vm.initMenuChildren = function initMenuChildren(folder) {
        StateService.setMenuChildren([]);
        FolderService.children(folder.path)
            .then((children) => { StateService.setMenuChildren(children) });
    };
}