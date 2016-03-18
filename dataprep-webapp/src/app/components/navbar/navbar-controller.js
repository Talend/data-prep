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

export default function NavbarCtrl($timeout, $state, version, copyRights, OnboardingService, DatasetService, StateService, EasterEggsService) {
    'ngInject';

    var vm = this;

    //--------------------------------------------------------------------------------------------------------------
    //-------------------------------------------ONBOARDING---------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    var tourId = 'dataset';
    this.startTour = OnboardingService.startTour;
    this.version = version;
    this.copyRights = copyRights;

    if ($state.current.name === 'nav.index.datasets' && !$state.params.datasetid && OnboardingService.shouldStartTour(tourId)) {
        DatasetService.getDatasets().then(function () {
            $timeout(OnboardingService.startTour.bind(null, tourId), 100, false);
        });
    }


    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------FEEDBACK---------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    vm.openFeedbackForm = StateService.showFeedback;


    //--------------------------------------------------------------------------------------------------------------
    //----------------------------------------------SEARCH----------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------

    vm.searchInput = '';
    vm.search = function () {
        // just in case something funny happens...
        EasterEggsService.enableEasterEgg(vm.searchInput);
    };

}