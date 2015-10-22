(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.playground.controller:PlaygroundCtrl
     * @description Playground controller.<br/>
     * Watchers :
     * <ul>
     *     <li>Recipe length : display recipe panel on first step application</li>
     * </ul>
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.playground.service:PreviewService
     * @requires data-prep.services.onboarding.service:OnboardingService
     */
    function PlaygroundCtrl($state, $stateParams, state, StateService, PlaygroundService, PreparationService,
                            PreviewService, RecipeService, RecipeBulletService, OnboardingService) {
        var vm = this;
        vm.playgroundService = PlaygroundService;
        vm.previewService = PreviewService;
        vm.recipeService = RecipeService;
        vm.onboardingService = OnboardingService;
        vm.state = state;

        /**
         * @ngdoc property
         * @name showNameValidation
         * @propertyOf data-prep.playground.controller:PlaygroundCtrl
         * @description Flag that controls the display of the save/discard window on implicit preparation close.
         */
        vm.showNameValidation = false;

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------------RECIPE--------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name toggleRecipe
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description Toggle all the steps of the recipe
         */
        vm.toggleRecipe = RecipeBulletService.toggleRecipe;

        //--------------------------------------------------------------------------------------------------------------
        //--------------------------------------------------RECIPE HEADER-----------------------------------------------
        //--------------------------------------------------------------------------------------------------------------

        /**
         * @ngdoc method
         * @name confirmPrepNameEdition
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description Change the preparation name
         */
        vm.confirmPrepNameEdition = function confirmPrepNameEdition(name){
            var cleanName = name.trim();
            if(!vm.changeNameInProgress && cleanName) {
                changeName(cleanName)
                    .then(function() {
                        return $state.go('nav.home.preparations', {prepid : state.playground.preparation.id}, {location:'replace', inherit:false} );
                    });
            }
        };

        /**
         * @ngdoc method
         * @name changeName
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description [PRIVATE] Create a preparation or update existing preparation name if it already exists
         * @param {string} name The preparation name
         * @returns {Promise} The create/update promise
         */
        function changeName(name) {
            vm.changeNameInProgress = true;
            return PlaygroundService.createOrUpdatePreparation(name)
                .finally(function() {
                    vm.changeNameInProgress = false;
                });
        }

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------------GRID----------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        vm.toggleLookup = function toggleLookup() {
            StateService.setLookupVisibility(!state.playground.lookupVisibility);
        };

        //--------------------------------------------------------------------------------------------------------------
        //------------------------------------------------------CLOSE---------------------------------------------------
        //--------------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name hideAll
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description [PRIVATE] Hide all the modals (playground and save/discard)
         */
        function hideAll() {
            vm.showNameValidation = false;
            StateService.hidePlayground();
        }

        /**
         * @ngdoc method
         * @name beforeClose
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description When the preparation is an implicit preparation, we show the save/discard modal and block the playground close.
         * @returns {boolean} True if the playground can be closed (no implicit preparation), False otherwise
         */
        vm.beforeClose = function beforeClose() {
            var isDraft = state.playground.preparation && state.playground.preparation.draft;
            if(isDraft) {
                vm.showNameValidation = true;
                return false;
            }
            return true;
        };

        /**
         * @ngdoc method
         * @name discardSaveOnClose
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description Discard implicit preparation save. This trigger a preparation delete.
         */
        vm.discardSaveOnClose = function discardSaveOnClose() {
            PreparationService.delete(state.playground.preparation)
                .then(hideAll);
        };

        /**
         * @ngdoc method
         * @name confirmSaveOnClose
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description Save implicit preparation with provided name. The playground is then closed.
         */
        vm.confirmSaveOnClose = function confirmSaveOnClose() {
            vm.saveInProgress = true;
            var cleanName = vm.preparationName.trim();
            changeName(cleanName)
                .then(hideAll)
                .finally(function() {
                    vm.saveInProgress = false;
                });
        };

        /**
         * @ngdoc method
         * @name close
         * @methodOf data-prep.playground.controller:PlaygroundCtrl
         * @description Playground close callback. It change the location and refresh the preparations if needed
         */
        vm.close = function() {
            PreparationService.refreshPreparations();
            if($stateParams.prepid) {
                $state.go('nav.home.preparations', {prepid: null});
            }
            else if($stateParams.datasetid) {
                $state.go('nav.home.datasets', {datasetid: null});
            }
        };
    }

    /**
     * @ngdoc property
     * @name preparationName
     * @propertyOf data-prep.playground.controller:PlaygroundCtrl
     * @description The preparation name
     * It is bound to {@link data-prep.services.playground.service:PlaygroundService PlaygroundService} property
     */
    Object.defineProperty(PlaygroundCtrl.prototype,
        'preparationName', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.playgroundService.preparationName;
            },
            set: function(value) {
                this.playgroundService.preparationName = value;
            }
        });

    /**
     * @ngdoc property
     * @name previewInProgress
     * @propertyOf data-prep.playground.controller:PlaygroundCtrl
     * @description Flag that defines if a preview is in progress
     * It is bound to {@link data-prep.services.dataset.service:PreviewService PreviewService} property
     */
    Object.defineProperty(PlaygroundCtrl.prototype,
        'previewInProgress', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.previewService.previewInProgress();
            }
        });

    /**
     * @ngdoc property
     * @name hasRecipe
     * @propertyOf data-prep.playground.controller:PlaygroundCtrl
     * @description Checks if there are steps in the preparation
     * It is bound to {@link data-prep.services.recipe.service:RecipeService RecipeService}.getRecipe() length
     * @type boolean
     */
    Object.defineProperty(PlaygroundCtrl.prototype,
        'hasRecipe', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.recipeService.getRecipe().length;
            }
        });

    /**
     * @ngdoc property
     * @name hasActiveStep
     * @propertyOf data-prep.playground.controller:PlaygroundCtrl
     * @description checks if there is at least 1 active step, by checking the 1st step in the recipe
     * It is bound to {@link data-prep.services.recipe.service:RecipeService RecipeService} status of the 1st step in the returned recipe array by the getRecipe() function
     * @type boolean
     */
    Object.defineProperty(PlaygroundCtrl.prototype,
        'hasActiveStep', {
            enumerable: true,
            configurable: false,
            get: function () {
                var firstStep = this.recipeService.getRecipe()[0];
                return firstStep && !firstStep.inactive;
            },
            set: function () {}
        });

    angular.module('data-prep.playground')
        .controller('PlaygroundCtrl', PlaygroundCtrl);
})();

