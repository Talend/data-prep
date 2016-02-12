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
 * @name data-prep.recipe.controller:RecipeBulletCtrl
 * @description The recipeBullet controller
 * @requires data-prep.services.recipe.service:RecipeService
 * @requires data-prep.services.recipe.service:RecipeBulletService
 */
export default function RecipeBulletCtrl(RecipeService, RecipeBulletService) {
    'ngInject';

    var vm = this;

    /**
     * @ngdoc property
     * @name height
     * @propertyOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description the height by default of a recipe bullet
     * @type {number}
     */
    vm.height = 36;

    /**
     * @ngdoc property
     * @name stepIndex
     * @propertyOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description The step index in the recipe
     * @type {number}
     */
    vm.stepIndex = RecipeService.getStepIndex(vm.step);

    /**
     * @ngdoc method
     * @name getBulletsToChange
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @param {object} allSvgs The list of bullets
     * @description Get the bullets to modify, according to the current bullet
     * @returns {Array} The bullets elements to changed
     */
    vm.getBulletsToChange = function (allSvgs) {
        //current step active : we should deactivate all the steps from current to the end
        if (!vm.step.inactive) {
            return allSvgs.slice(vm.stepIndex);
        }
        //current step inactive : we should activate the steps from last inactive to the current
        else {
            var lastActiveStepIndex = RecipeService.getActiveThresholdStepIndex();
            return allSvgs.slice(lastActiveStepIndex + 1, vm.stepIndex + 1);
        }
    };

    /**
     * @ngdoc method
     * @name isStartChain
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description Test if step is the first element of the chain
     * @returns {boolean} true if step is the first step
     */
    vm.isStartChain = function () {
        return RecipeService.isFirstStep(vm.step);
    };

    /**
     * @ngdoc method
     * @name isEndChain
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description Test if step is the last element of the chain
     * @returns {boolean} true if step is the last step
     */
    vm.isEndChain = function () {
        return RecipeService.isLastStep(vm.step);
    };

    /**
     * @ngdoc method
     * @name stepHoverStart
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description Trigger actions called at mouse enter
     */
    vm.stepHoverStart = function () {
        RecipeBulletService.stepHoverStart(vm.step);
    };

    /**
     * @ngdoc method
     * @name stepHoverEnd
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description Trigger actions called at mouse leave
     */
    vm.stepHoverEnd = function () {
        RecipeBulletService.stepHoverEnd(vm.step);
    };

    /**
     * @ngdoc method
     * @name toggleStep
     * @methodOf data-prep.recipe.controller:RecipeBulletCtrl
     * @description Enable/disable step
     */
    vm.toggleStep = function () {
        RecipeBulletService.toggleStep(vm.step);
    };
}