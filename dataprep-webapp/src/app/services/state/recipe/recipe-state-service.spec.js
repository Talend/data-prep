/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Recipe state service', () => {
	beforeEach(angular.mock.module('data-prep.services.state'));

	describe('visibility', () => {
		it('should set visibility to true', inject((recipeState, RecipeStateService) => {
			// given
			recipeState.visible = false;

			// when
			RecipeStateService.show();

			// then
			expect(recipeState.visible).toBe(true);
		}));

		it('should set visibility to false', inject((recipeState, RecipeStateService) => {
			// given
			recipeState.visible = true;

			// when
			RecipeStateService.hide();

			// then
			expect(recipeState.visible).toBe(false);
		}));
	});

	describe('current hovered step', () => {
		it('should set the currently hovered step', inject((recipeState, RecipeStateService) => {
			// given
			const step = {};
			recipeState.hoveredStep = null;

			// when
			RecipeStateService.setHoveredStep(step);

			// then
			expect(recipeState.hoveredStep).toBe(step);
		}));
	});

	describe('reset', () => {
		it('should reset recipe state', inject((recipeState, RecipeStateService) => {
			// given
			recipeState.visible = true;

			// when
			RecipeStateService.reset();

			// then
			expect(recipeState.visible).toBe(false);
		}));
	});

	describe('setAllowDistributedRun', () => {
		it('should set recipe could be distributed', inject((recipeState, RecipeStateService) => {
			// given
			recipeState.allowDistributedRun = true;

			// when
			RecipeStateService.setAllowDistributedRun(false);

			// then
			expect(recipeState.allowDistributedRun).toBeFalsy();
		}));
	});
});
