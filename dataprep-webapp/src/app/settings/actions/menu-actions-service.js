/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class MenuActionsService {
	constructor($state, $window, state) {
		'ngInject';
		this.$state = $state;
		this.$window = $window;
		this.state = state;
	}

	executeRouterAction(actionEvent, method, args, param) {
		if (actionEvent && ((actionEvent.button === 0 && (actionEvent.ctrlKey || actionEvent.metaKey)) || actionEvent.button === 1)) {
			this.$window.open(this.$state.href(...args, param), '_blank');
		}
		else {
			this.$state[method](...args, param);
		}
	}

	dispatch(action) {
		switch (action.type) {
		case '@@router/GO': {
			const { method, args } = action.payload;
			this.executeRouterAction(action.event, method, args);
			break;
		}
		case '@@router/GO_FOLDER': {
			const { method, args, id } = action.payload;
			this.executeRouterAction(action.event, method, args, { folderId: id });
			break;
		}
		case '@@router/GO_CURRENT_FOLDER': {
			const { method, args } = action.payload;
			const folderId = this.state.inventory.folder.metadata.id;
			this.executeRouterAction(action.event, method, args, { folderId });
			break;
		}
		case '@@router/GO_PREPARATION': {
			const { method, args, id } = action.payload;
			this.executeRouterAction(action.event, method, args, { prepid: id });
			break;
		}
		}
	}
}
