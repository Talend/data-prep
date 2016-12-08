/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
export default class AboutCtrl {
	constructor(state, version, copyRights, AboutService) {
		'ngInject';
		this.state = state;
		this.version = version;
		this.copyRights = copyRights;
		this.aboutService = AboutService;
		this.buildDetails = [];
		this.showBuildDetails = false;
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
	 * @name getBuildDetails
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description fetches the builds list and toggles the builds details list
	 */
	getBuildDetails() {
		this.aboutService.buildDetails().then((details) => {
			this.toggleDetailsDisplay();
			this.buildDetails = details;
		});
	}
}
