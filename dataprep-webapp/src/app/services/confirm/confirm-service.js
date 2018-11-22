/*
 * ============================================================================
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * https://github.com/Talend/data-prep/blob/master/LICENSE
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 *
 * ============================================================================
 */

/**
 * @ngdoc service
 * @name data-prep.services.export.service:ConfirmService
 * @description Confirm service. This service provide the entry point to the confirmation modals.
 */
export default class ConfirmService {
	constructor(state, StateService, $translate) {
		'ngInject';

		this.state = state;
		this.StateService = StateService;
		this.$translate = $translate;
		this.resolve = null;
		this.reject = null;
	}

	/**
	 * @ngdoc method
	 * @name confirm
	 * @methodOf data-prep.services.export.service:ConfirmService
	 * @param {string[]} textIds The text ids for translation
	 * @param {object} textArgs The text translation args
	 * @param {boolean} deletion True if it is a deletion modal
	 * @returns {promise} Promise that resolves (validate) or reject (refuse/cancel) the choice
	 * @description Show the confirm modal element and return a promise that will be resolve on button click or modal dismiss
	 * Example : ConfirmService.confirm(['First text', 'Second text'], {translateArg: 'value'})
	 */
	confirm(title, textIds, textArgs, deletion) {
		return this.$translate(title)
			.then((translation) => {
				this.$translate(textIds, textArgs)
					.then(translations => textIds.map(id => translations[id]))
					.then((texts) => {
						this.StateService.showConfirmationModal(translation, texts, deletion);
					});

				return new Promise((res, rej) => {
					this.resolve = res;
					this.reject = rej;
				}).finally(this.StateService.hideConfirmationModal);
			});
	}
}
