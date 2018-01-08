/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class SidePanelActionsService {
	constructor(state, StorageService, StateService) {
		'ngInject';
		this.state = state;
		this.StateService = StateService;
		this.StorageService = StorageService;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@sidepanel/TOGGLE':
			this.StateService[action.payload.method]();
			this.StorageService.setSidePanelDock(this.state.home.sidePanelDocked);
			break;
		}
	}
}
