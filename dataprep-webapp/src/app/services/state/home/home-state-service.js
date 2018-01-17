/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { HOME_FOLDER } from '../inventory/inventory-state-service';

export const homeState = {
	sidePanelDocked: false,
	folders: {
		creator: {
			isVisible: false,
		},
	},
	preparations: {
		creator: {
			isVisible: false,
		},
		copyMove: {
			isVisible: false,
			initialFolder: undefined,
			preparation: undefined,
			isTreeLoading: false,
			tree: undefined,
		},
	},
	about: {
		isVisible: false,
		builds: [],
	},
};

export function HomeStateService($translate) {
	'ngInject';

	return {
		setBuilds,
		setCopyMoveTree,
		setCopyMoveTreeLoading,
		setSidePanelDock,
		toggleAbout,
		toggleCopyMovePreparation,
		toggleFolderCreator,
		togglePreparationCreator,
		toggleSidepanel,
	};

	function setSidePanelDock(docked) {
		homeState.sidePanelDocked = docked;
	}

	function toggleSidepanel() {
		homeState.sidePanelDocked = !homeState.sidePanelDocked;
	}

	function toggleCopyMovePreparation(initialFolder, preparation) {
		homeState.preparations.copyMove.isVisible = !homeState.preparations.copyMove.isVisible;
		homeState.preparations.copyMove.initialFolder = initialFolder;
		homeState.preparations.copyMove.preparation = preparation;
	}

	function setCopyMoveTree(tree) {
		if (tree.folder && tree.folder.path === HOME_FOLDER.path) {
			tree.folder.name = $translate.instant('HOME_FOLDER');
		}
		homeState.preparations.copyMove.tree = tree;
	}

	function setCopyMoveTreeLoading(bool) {
		homeState.preparations.copyMove.isTreeLoading = bool;
	}

	function toggleFolderCreator() {
		homeState.folders.creator.isVisible = !homeState.folders.creator.isVisible;
	}

	function togglePreparationCreator() {
		homeState.preparations.creator.isVisible = !homeState.preparations.creator.isVisible;
	}

	function toggleAbout() {
		homeState.about.isVisible = !homeState.about.isVisible;
	}

	function setBuilds(builds) {
		homeState.about.builds = builds;
	}
}
