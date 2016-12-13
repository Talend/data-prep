/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const homeState = {
	sidePanelDocked: false,
	preparations: {
		creator: {
			isVisible: false,
		},
		copyMove: {
			isVisible: false,
			initialFolder: undefined,
			preparation: undefined,
		},
	},
	about: {
		isVisible: false,
		builds: [],
	},
};

export function HomeStateService() {
	return {
		toggleSidepanel,
		toggleCopyMovePreparation,
		togglePreparationCreator,
		toggleAbout,
		setBuilds,
	};

	function toggleSidepanel() {
		homeState.sidePanelDocked = !homeState.sidePanelDocked;
	}

	function toggleCopyMovePreparation(initialFolder, preparation) {
		homeState.preparations.copyMove.isVisible = !homeState.preparations.copyMove.isVisible;
		homeState.preparations.copyMove.initialFolder = initialFolder;
		homeState.preparations.copyMove.preparation = preparation;
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
