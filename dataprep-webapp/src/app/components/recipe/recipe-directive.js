/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.recipe.directive:Recipe
 * @description This directive display the recipe with the step params as accordions.
 * @restrict E
 * @usage <recipe></recipe>
 */
export default function Recipe($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/recipe/recipe.html',
        controllerAs: 'recipeCtrl',
        controller: 'RecipeCtrl',
        link: function (scope, iElement, iAttrs, ctrl) {
            function attachDeleteMouseOver(recipe) {
                _.forEach(recipe, function (step) {
                    var stepId = step.transformation.stepId;
                    var hasDiff = step.diff && step.diff.createdColumns && step.diff.createdColumns.length;

                    function shouldBeRemoved(stepToTest) {
                        return stepToTest.transformation.stepId === stepId || // current step
                            (hasDiff && stepToTest.actionParameters &&
                            step.diff.createdColumns.indexOf(stepToTest.actionParameters.parameters.column_id) > -1); //step on a column that will be removed
                    }

                    var stepsToRemove = _.chain(recipe)
                        .filter(shouldBeRemoved)
                        .map(function (step) {
                            return iElement.find('#step-' + step.transformation.stepId);
                        })
                        .value();

                    var removeElement = iElement.find('#step-' + stepId).find('#step-remove-' + stepId);
                    removeElement.on('mouseover', function () {
                        _.forEach(stepsToRemove, function (stepElement) {
                            stepElement.addClass('remove');
                        });
                    });
                    removeElement.on('mouseout', function () {
                        _.forEach(stepsToRemove, function (stepElement) {
                            stepElement.removeClass('remove');
                        });
                    });
                });
            }

            scope.$watch(function () {
                return ctrl.recipe;
            }, function (recipe) {
                $timeout(attachDeleteMouseOver.bind(null, recipe), 0, false);
            });
        }
    };
}