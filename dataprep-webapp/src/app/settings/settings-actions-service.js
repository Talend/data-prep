/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export default class SettingsActionsService {
	constructor($timeout, SettingsActionsHandlers) {
		'ngInject';

		this.$timeout = $timeout;
		this.SettingsActionsHandlers = SettingsActionsHandlers;
	}

	createDispatcher(action) {
		return (event, model) => {
			if (action) {
				const adaptedAction = {
					...action,
					payload: {
						...action.payload,
						...model,
					},
					event,
				};
				this.$timeout(this.dispatch(adaptedAction));
			}
		};
	}

	dispatch(action) {
		this.SettingsActionsHandlers.forEach(handler => handler.dispatch(action));
	}
}
