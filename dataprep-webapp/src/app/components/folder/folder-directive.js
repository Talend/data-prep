/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.datagrid.directive:Folder
 * @description This directive create the folder
 * @restrict E
 * @usage <folder></folder>
 */
export default function Folder() {
    return {
        templateUrl: 'app/components/folder/folder.html',
        restrict: 'E',
        bindToController: true,
        controllerAs: 'folderCtrl',
        controller: 'FolderCtrl',
    };
}