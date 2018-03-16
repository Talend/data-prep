/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const FIRST_PARAMETER = '?';
const PARAMETERS_SEP = '&';

export default class ExternalActionsService {
	constructor($window) {
		'ngInject';
		this.$window = $window;
	}

	dispatch(action) {
		switch (action.type) {
		case '@@external/OPEN_PAGE': {
			const location = action.payload.args[0];
			const href = this.$window.location.href;
			let redirect = href;
			let separator = FIRST_PARAMETER;

			if (href.includes(FIRST_PARAMETER)) {
				redirect = href.substring(0, href.indexOf(FIRST_PARAMETER));
			}
			if (location.includes(FIRST_PARAMETER)) {
				separator = PARAMETERS_SEP;
			}

			this.$window.location.href = `${location}${separator}redirect=${redirect}`;
			break;
		}
		case '@@external/OPEN_WINDOW': {
			const { method, args } = action.payload;
			let externalActionArgs = args || [];
			if (!externalActionArgs.length) {
				externalActionArgs = [
					action.payload.url,
				];
			}

			this.$window[method](...externalActionArgs);
			break;
		}
		}
	}
}
