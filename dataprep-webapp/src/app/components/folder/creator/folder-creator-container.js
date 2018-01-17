/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc component
 * @name data-prep.folder-creator:folderCreator
 * @description This component renders add folder modal content
 * @usage <folder-creator></folder-creator>
 * */

export default {
	template: `
		<talend-modal id="create-folder-modal"
			fullscreen="false"
			close-button="true"
			state="$ctrl.state.home.folders.creator.isVisible"
			ng-if="$ctrl.state.home.folders.creator.isVisible">
			<folder-creator-form />
		</talend-modal>
	`,
	controller(state) {
		'ngInject';
		this.state = state;
	},
};
