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
 * @name talend.widget.service:TalendConfirmService
 * @description Talend confirm service
 */
export default function TalendConfirmService($rootScope, $compile, $document, $q, $timeout, $translate) {
	'ngInject';

	const body = $document.find('body').eq(0);
	const self = this;

    /**
     * @ngdoc method
     * @name translateTexts
     * @methodOf talend.widget.service:TalendConfirmService
     * @param {string[]} textIds The text ids for translation
     * @param {object} textArgs The text translation args
     * @description [PRIVATE] Translate the texts to display
     * @returns {promise} The promise that resolves the translated texts
     */
	const translateTexts = function (textIds, textArgs) {
		return $translate(textIds, textArgs)
            .then(function (translations) {
	return _.map(textIds, function (id) {
		return translations[id];
	});
});
	};

    /**
     * @ngdoc method
     * @name createScope
     * @methodOf talend.widget.service:TalendConfirmService
     * @param {object} options The modal options
     * @param {string[]} textIds The text ids for translation
     * @param {object} textArgs The translation args
     * @description [PRIVATE] Create confirm modal isolated scope
     */
	const createScope = function (options, textIds, textArgs) {
		if (self.modalScope) {
			throw new Error('A confirm popup is already created');
		}

		self.modalScope = $rootScope.$new(true);
		translateTexts(textIds, textArgs)
            .then(function (translatedTexts) {
	self.modalScope.texts = translatedTexts;
});

		if (options) {
			self.modalScope.disableEnter = options.disableEnter;
		}
	};

    /**
     * @ngdoc method
     * @name removeScope
     * @methodOf talend.widget.service:TalendConfirmService
     * @description [PRIVATE] Destroy the modal scope
     */
	const removeScope = function () {
		self.modalScope.$destroy();
		self.modalScope = null;
	};

    /**
     * @ngdoc method
     * @name createElement
     * @methodOf talend.widget.service:TalendConfirmService
     * @description [PRIVATE] Create the confirm modal element and attach it to the body
     */
	const createElement = function () {
		self.element = angular.element('<talend-confirm disable-enter="disableEnter" texts="texts"></talend-confirm>');
		$compile(self.element)(self.modalScope);
		body.append(self.element);
	};

    /**
     * @ngdoc method
     * @name removeElement
     * @methodOf talend.widget.service:TalendConfirmService
     * @description [PRIVATE] Remove the the element
     */
	const removeElement = function () {
		self.element.remove();
		self.element = null;
	};

    /**
     * @ngdoc method
     * @name close
     * @methodOf talend.widget.service:TalendConfirmService
     * @description [PRIVATE] Remove the modal and reset everything
     */
	const close = function () {
		removeScope();
		removeElement();

		self.confirmResolve = null;
		self.confirmReject = null;
	};

    /**
     * @ngdoc method
     * @name resolve
     * @methodOf talend.widget.service:TalendConfirmService
     * @description Resolve the modal promise and destroy the modal
     */
	this.resolve = function () {
		self.confirmResolve();
		$timeout(close);
	};

    /**
     * @ngdoc method
     * @name reject
     * @methodOf talend.widget.service:TalendConfirmService
     * @param {string} cause 'dismiss' if the modal is closed without clicking on a button
     * @description Reject the modal promise and destroy the modal
     */
	this.reject = function (cause) {
		self.confirmReject(cause);
		$timeout(close);
	};

    /**
     * @ngdoc method
     * @name confirm
     * @methodOf talend.widget.service:TalendConfirmService
     * @param {object} options Confirm modal options {disableEnter: boolean}
     * @param {string[]} textIds Array containing the texts ids to display
     * @param {object} textArgs Text translation args
     * @returns {promise} Promise that resolves (validate) or reject (refuse/cancel) the choice
     * @description Create the confirm modal element and return a promise that will be resolve on button click or modal dismiss
     * Example : TalendConfirmService.confirm({disableEnter: true}, ['First text', 'Second text'], {translateArg: 'value'})
     */
	this.confirm = function (options, textIds, textArgs) {
		createScope(options, textIds, textArgs);
		createElement();

		return $q(function (resolve, reject) {
			self.confirmResolve = resolve;
			self.confirmReject = reject;
		});
	};
}
