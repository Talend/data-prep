/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
export default class AboutCtrl {
	constructor(state, AboutService) {
		'ngInject';
		this.state = state;
		this.showBuildDetails = false;
		AboutService.loadBuilds();
	}

	/**
	 * @ngdoc method
	 * @name toggleDetailsDisplay
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description toggles the builds details list
	 */
	toggleDetailsDisplay() {
		this.showBuildDetails = !this.showBuildDetails;
	}

	/**
	 * @ngdoc method
	 * @name getFullYear
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description get copyrights full year
	 * @returns {number} full year
	 */
	getFullYear() {
		return (new Date()).getFullYear();
	}
}
