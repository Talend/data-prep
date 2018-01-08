/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.validation.directive:UniqueFolderValidation
 * @description This directive perform checks if a folder name is unique
 * @restrict E
 * @usage <input ...  unique-folder="folders" />
 * @param {string} unique-folder List of existing folders
 */
export default function UniqueFolderValidation() {
	return {
		restrict: 'A',
		require: 'ngModel',
		scope: {
			uniqueFolder: '=',
		},
		link(scope, elm, attrs, ctrl) {
			ctrl.$validators.uniqueFolderValidation = (name) => {
				if (name === '') {
					return false;
				}

				return !_.find(scope.uniqueFolder, (folder) => {
					return folder.name.toLowerCase() === name.toLowerCase();
				});
			};
		},
	};
}
