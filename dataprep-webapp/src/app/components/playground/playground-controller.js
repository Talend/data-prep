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
 * @name data-prep.playground.controller:PlaygroundCtrl
 * @description Playground controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.preparation.service:PreparationService
 * @requires data-prep.services.playground.service:PreviewService
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.recipe.service:RecipeBulletService
 * @requires data-prep.services.onboarding.service:OnboardingService
 * @requires data-prep.services.lookup.service:LookupService
 * @requires data-prep.services.folder.service:FolderService
 */
export default function PlaygroundCtrl($state, $stateParams, state, StateService, PlaygroundService, PreparationService,
                                       PreviewService, RecipeService, RecipeBulletService, OnboardingService,
                                       LookupService, FolderService) {
    'ngInject';

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
    vm.confirmPrepNameEdition = function confirmPrepNameEdition(name) {
        var cleanName = name.trim();
        if (!vm.changeNameInProgress && cleanName) {
            changeName(cleanName)
                .then(function () {
                    return $state.go('nav.home.preparations', {prepid: state.playground.preparation.id}, {
                        location: 'replace',
                        inherit: false
                    });
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
            .finally(function () {
                vm.changeNameInProgress = false;
            });
    }

    //--------------------------------------------------------------------------------------------------------------
    //------------------------------------------------------LOOKUP--------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggleLookup
     * @methodOf data-prep.playground.controller:PlaygroundCtrl
     * @description show hides lookup panel and populates its grid
     */
    vm.toggleLookup = function toggleLookup() {
        if (state.playground.lookup.visibility) {
            StateService.setLookupVisibility(false);
        }
        else {
            LookupService.initLookups()
                .then(StateService.setLookupVisibility.bind(null, true));
        }
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
        if (isDraft) {
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
            .finally(function () {
                vm.saveInProgress = false;
                FolderService.getContent(state.folder.currentFolder);
            });
    };

    /**
     * @ngdoc method
     * @name close
     * @methodOf data-prep.playground.controller:PlaygroundCtrl
     * @description Playground close callback. It change the location and refresh the preparations if needed
     */
    vm.close = function () {
        PreparationService.refreshPreparations();
        if ($stateParams.prepid) {
            $state.go('nav.home.preparations', {prepid: null});
        }
        else if ($stateParams.datasetid) {
            $state.go('nav.home.datasets', {datasetid: null});
        }
    };

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------FEEDBACK---------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    vm.openFeedbackForm = StateService.showFeedback;

    //--------------------------------------------------------------------------------------------------------------
    //------------------------------------------DATASET PARAMS------------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    vm.toggleParameters = StateService.toggleDatasetParameters;

    vm.changeDatasetParameters = function changeDatasetParameters(parameters) {
        StateService.setIsSendingDatasetParameters(true);
        PlaygroundService.changeDatasetParameters(parameters)
            .then(StateService.hideDatasetParameters)
            .finally(StateService.setIsSendingDatasetParameters.bind(null, false));
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
        set: function (value) {
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
        set: function () {
        }
    });

