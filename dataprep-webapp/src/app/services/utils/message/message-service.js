/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:MessageService
 * @description Display message toasts
 */
export default class MessageService {

	constructor($translate, $timeout, toaster) {
		'ngInject';
		this.$translate = $translate;
		this.$timeout = $timeout;
		this.toaster = toaster;

		this.pendingMessages = [];
		this.messagePromise = null;
	}

    /**
     * @ngdoc method
     * @name _pop
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {object} message The message definition
     * @description Translate and show the toast
     */
	_pop(message) {
		const { type, title, content, args, timeout } = message;
		return this.$translate([title, content], args)
            .then(translations => this.toaster.pop(type, translations[title], translations[content], timeout));
	}

    /**
     * @ngdoc method
     * @name _bufferPop
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {object} message The message definition
     * @description Add toast to buffer and schedule a display if not already scheduled
     */
	_bufferPop(message) {
		message.key = message.title + message.content;
		const messageAlreadyPending = _.find(this.pendingMessages, { key: message.key });

		if (messageAlreadyPending) {
			return;
		}

		this.pendingMessages.push(message);

		if (this.messagePromise) {
			return;
		}

		this.messagePromise = this.$timeout(() => {
			const messages = this.pendingMessages;
			this.pendingMessages = [];
			messages.forEach(next => this._pop(next));
			this.messagePromise = null;
		}, 300, false);
	}

    /**
     * @ngdoc method
     * @name error
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey The message title key (transformed by internationalization)
     * @param {string} contentKey The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display an error toast. Automatic dismiss is disabled
     */
	error(titleKey, contentKey, args) {
		const message = {
			type: 'error',
			title: titleKey,
			content: contentKey,
			args,
			timeout: 0,
		};
		this._bufferPop(message);
	}

    /**
     * @ngdoc method
     * @name success
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey The message title key (transformed by internationalization)
     * @param {string} contentKey The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display a success toast. The toast disappear after 5000ms
     */
	success(titleKey, contentKey, args) {
		const message = {
			type: 'success',
			title: titleKey,
			content: contentKey,
			args,
			timeout: 5000,
		};
		this._bufferPop(message);
	}

    /**
     * @ngdoc method
     * @name warning
     * @methodOf data-prep.services.utils.service:MessageService
     * @param {string} titleKey The message title key (transformed by internationalization)
     * @param {string} contentKey The message content key (transformed by internationalization)
     * @param {string} args The message (title and content) arguments used by internationalization to replace vars
     * @description Display a warning toast. Automatic dismiss is disabled
     */
	warning(titleKey, contentKey, args) {
		const message = {
			type: 'warning',
			title: titleKey,
			content: contentKey,
			args,
			timeout: 0,
		};
		this._bufferPop(message);
	}
}
