/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.access-error.controller:AccessErrorCtrl
 * @description AccessError controller.
 */
export default class AccessErrorCtrl {
	constructor($translate) {
		'ngInject';
		this.$translate = $translate;
	}

	get title() {
		return this.$translate.instant(`ERROR_${this.status}_TITLE`);
	}

	get message() {
		return this.$translate.instant(`ERROR_${this.status}_MESSAGE`);
	}
}
