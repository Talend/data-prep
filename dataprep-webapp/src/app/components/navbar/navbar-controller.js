/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
/**
 * @ngdoc controller
 * @name data-prep.navbar.controller:NavbarCtrl
 * @description navbar controller
 * @requires data-prep.services.utils.contant:version
 * @requires data-prep.services.utils.contant:copyRights
 * @requires data-prep.services.dataset.service:DatasetService
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.onboarding.service:OnboardingService
 * @requires data-prep.services.easter-eggs.service::EasterEggsService
 */

export default function NavbarCtrl($timeout, $state, state, version, copyRights, OnboardingService, DatasetService, StateService) {
	'ngInject';

	const vm = this;
	const tourId = 'preparation';
	this.state = state;
    //--------------------------------------------------------------------------------------------------------------
    // -------------------------------------------ONBOARDING---------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
	this.startTour = tourId => OnboardingService.startTour(tourId);
	this.version = version;
	this.copyRights = copyRights;

	if (($state.current.name === 'nav.index.preparations' || $state.current.name === 'nav.index.datasets') &&
        !$state.params.prepid &&
        !$state.params.datasetid &&
        OnboardingService.shouldStartTour(tourId)) {
		OnboardingService.startTour(tourId);
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------FEEDBACK---------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
	vm.openFeedbackForm = StateService.showFeedback;
}
