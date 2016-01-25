describe('Easter Eggs', function () {
	'use strict';

	beforeEach(module('data-prep.services.state'));

	describe('state service', function() {

		it('should enable an easter egg', inject(function (easterEggsState, EasterEggsStateService) {
			//given
			//when
			EasterEggsStateService.enableEasterEgg('back to the future');

			//then
			expect(easterEggsState.currentEasterEgg).toBe('back to the future');
			expect(easterEggsState.displayEasterEgg).toBe(true);
		}));

		it('should disable an easter egg', inject(function (easterEggsState, EasterEggsStateService) {
			//given
			//when
			EasterEggsStateService.disableEasterEgg();

			//then
			expect(easterEggsState.currentEasterEgg).toBe('');
			expect(easterEggsState.displayEasterEgg).toBe(false);
		}));

	});


});
