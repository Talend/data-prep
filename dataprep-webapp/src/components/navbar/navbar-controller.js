(function () {
    'use strict';

    function NavbarCtrl($state, version, OnboardingService, DatasetService, StateService, EasterEggsService) {
        var vm = this;

        //--------------------------------------------------------------------------------------------------------------
        //-------------------------------------------ONBOARDING---------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        var tourId = 'dataset';
        this.startTour = OnboardingService.startTour;
        this.version = version;

        if ($state.current.name === 'nav.home.datasets' && !$state.params.datasetid && OnboardingService.shouldStartTour(tourId)) {
            DatasetService.getDatasets().then(function () {
                setTimeout(OnboardingService.startTour.bind(null, tourId), 100);
            });
        }


        //--------------------------------------------------------------------------------------------------------------
        //---------------------------------------------FEEDBACK---------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        vm.openFeedbackForm = StateService.showFeedback;


        //--------------------------------------------------------------------------------------------------------------
        //----------------------------------------------SEARCH----------------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        vm.searchInput='';
        vm.search = function() {
            // just in case something funny happens...
            EasterEggsService.enableEasterEgg(vm.searchInput);
        };

    }




    angular.module('data-prep.navbar')
        .controller('NavbarCtrl', NavbarCtrl);
})();
