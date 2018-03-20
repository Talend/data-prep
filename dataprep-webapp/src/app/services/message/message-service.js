/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const MESSAGE_TYPES = {
	ERROR: 'error',
	WARNING: 'warning',
	SUCCESS: 'info',
};

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:MessageService
 * @description Display message toasts
 */
export default class MessageService {
	constructor($translate, StateService) {
		'ngInject';
		this.$translate = $translate;
		this.StateService = StateService;
	}

    /**
     * @ngdoc method
     * @name _pop
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {object} message The message definition
     * @description Translate and show the toast
     */
	_pop(type, message) {
		const { title, content, args } = message;

		return this.$translate([title, content], args)
			.then((translations) => {
				this.StateService.pushMessage({
					type,
					title: translations[title],
					message: translations[content],
				});
			});
	}

    /**
     * @ngdoc method
     * @name error
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} title The message title key (transformed by internationalization)
     * @param {string} content The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display an error toast. Automatic dismiss is disabled
     */
	error(title, content, args) {
		this._pop(MESSAGE_TYPES.ERROR, {
			title,
			content,
			args,
		});
	}

    /**
     * @ngdoc method
     * @name success
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} title The message title key (transformed by internationalization)
     * @param {string} content The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display a success toast. The toast disappear after 5000ms
     */
	success(title, content, args) {
		this._pop(MESSAGE_TYPES.SUCCESS, {
			title,
			content,
			args,
		});
	}

    /**
     * @ngdoc method
     * @name warning
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} title The message title key (transformed by internationalization)
     * @param {string} content The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display a warning toast. Automatic dismiss is disabled
     */
	warning(title, content, args) {
		this._pop(MESSAGE_TYPES.WARNING, {
			title,
			content,
			args,
		});
	}
}
