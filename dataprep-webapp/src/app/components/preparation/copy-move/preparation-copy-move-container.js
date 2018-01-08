/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import PreparationCopyMoveCtrl from './preparation-copy-move-controller';

/**
 * @ngdoc directive
 * @name data-prep.preparation-copy-move.component:PreparationCopyMoveContainer
 * @description This component display the inventory copy/move modal for preparations.
 * @restrict E
 */
const PreparationCopyMoveContainer = {
	template: `
		<talend-modal fullscreen="false"
              close-button="true"
              ng-if="$ctrl.state.home.preparations.copyMove.isVisible"
              state="$ctrl.state.home.preparations.copyMove.isVisible">
		    <inventory-copy-move
				initial-folder="$ctrl.state.inventory.homeFolder"
				item="$ctrl.state.home.preparations.copyMove.preparation"
				on-copy="$ctrl.copy(item, destination, name)"
				on-move="$ctrl.move(item, destination, name)"
				is-loading="$ctrl.state.home.preparations.copyMove.isTreeLoading"
				tree="$ctrl.state.home.preparations.copyMove.tree">
		    </inventory-copy-move>
		</talend-modal>
	`,
	controller: PreparationCopyMoveCtrl,
};

export default PreparationCopyMoveContainer;
