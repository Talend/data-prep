/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const PREPARATION = 'preparation';

export default class SearchActionsService {

	constructor(MenuActionsService) {
		'ngInject';
		this.MenuActionsService = MenuActionsService;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@search/OPEN': {
			switch (action.payload.category) {
			case PREPARATION: {
				const menuAction = {
					type: '@@router/GO_PREPARATION',
					payload: {
						method: 'go',
						args: ['playground.preparation'],
						id: action.payload.id,
					},
				};
				this.MenuActionsService.dispatch(menuAction);
				break;
			}
			}
			break;
		}
		}
	}
}
