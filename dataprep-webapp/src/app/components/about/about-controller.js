/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
export default class AboutCtrl {
	constructor(state, $translate, AboutService, HomeStateService) {
		'ngInject';

		this.state = state;
		this.$translate = $translate;
		this.HomeStateService = HomeStateService;
		this.AboutService = AboutService;

		this.loading = true;
		this.expanded = false;

		this.hide = this.hide.bind(this);
		this.toggle = this.toggle.bind(this);
		this.getCopyrights = this.getCopyrights.bind(this);
	}

	$onInit() {
		this.AboutService
			.loadBuilds()
			.then(() => {
				this.loading = false;
			});
	}

	/**
	 * @ngdoc method
	 * @name toggle
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description toggles the builds details list
	 */
	toggle() {
		this.expanded = !this.expanded;
	}

	/**
	 * @ngdoc method
	 * @name hide
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description hides the about modal
	 */
	hide() {
		this.HomeStateService.setAboutVisibility(false);
	}

	/**
	 * @ngdoc method
	 * @name hide
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description returns and converts the services
	 */
	getServices() {
		if (!this.state.home.about.builds.services) {
			return [];
		}

		return this.state.home.about.builds.services.map(service => ({
			name: service.serviceName,
			build: service.buildId,
			version: service.versionId,
		}));
	}

	/**
	 * @ngdoc method
	 * @name getCopyrights
	 * @methodOf data-prep.about.controller:AboutCtrl
	 * @description get translated copyrights
	 * @returns {string} copyrights with current year
	 */
	getCopyrights() {
		return this.$translate.instant('COPYRIGHTS', {
			year: new Date().getFullYear(),
		});
	}
}
