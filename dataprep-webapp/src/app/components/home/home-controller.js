/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class HomeCtrl {
	constructor(state, StateService, StorageService, ProgressService) {
		'ngInject';
		this.state = state;
		this.StateService = StateService;
		this.StorageService = StorageService;
		this.ProgressService = ProgressService;
	}

	$onInit() {
		this.configureSidePanel();
	}

	configureSidePanel() {
		const docked = this.StorageService.getSidePanelDock();
		this.StateService.setHomeSidePanelDock(docked);
	}
}
