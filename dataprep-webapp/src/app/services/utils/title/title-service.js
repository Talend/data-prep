/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.utils.service:TitleService
 * @description Display message toasts
 */
export default class TitleService {

	constructor($window, $translate) {
		'ngInject';
		this.$window = $window;
		this.$translate = $translate;
	}

	/**
	 * @ngdoc method
	 * @name set
	 * @methodOf data-prep.services.utils.service:TitleService
	 * @param {string} titleKey The title translation key
	 * @description Set the window title
	 */
	set(titleKey) {
		return this.$translate(titleKey).then((title) => {
			this.$window.document.title = `${title} | Talend`;
		});
	}

	/**
	 * @ngdoc method
	 * @name setStrict
	 * @methodOf data-prep.services.utils.service:TitleService
	 * @param {string} title The title
	 * @description Set the window title
	 */
	setStrict(title) {
		this.$window.document.title = `${title} | Talend`;
	}

	/**
	 * @ngdoc method
	 * @name reset
	 * @methodOf data-prep.services.utils.service:TitleService
	 * @description Reset the window title
	 */
	reset() {
		return this.set('DATA_PREPARATION');
	}
}
