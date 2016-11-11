/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class MenuActionsService {
	constructor($state) {
		'ngInject';
		this.$state = $state;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@router/GO': {
			const { method, args } = action.payload;
			this.$state[method](...args);
			break;
		}
		case '@@router/GO_FOLDER': {
			const { method, args, id } = action.payload;
			this.$state[method](...args, { folderId: id });
			break;
		}
		case '@@router/GO_PREPARATION': {
			const { method, args, id } = action.payload;
			this.$state[method](...args, { prepid: id });
			break;
		}
		}
	}
}
