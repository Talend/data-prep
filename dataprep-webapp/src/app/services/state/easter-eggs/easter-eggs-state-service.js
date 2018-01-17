/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const easterEggsState = {
	currentEasterEgg: '',
	displayEasterEgg: false,
};

/**
 * @ngdoc service
 * @name data-prep.services.state.service:EasterEggsStateService
 * @description Manage the state of the easter eggs
 */
export function EasterEggsStateService() {
	return {
		enableEasterEgg,
		disableEasterEgg,
	};

    /**
     * @ngdoc method
     * @name enableEasterEgg
     * @methodOf data-prep.services.state.service:EasterEggsStateService
     * @description Set the easter egg to display
     * @param easterEgg the easter egg to display
     */
	function enableEasterEgg(easterEgg) {
		easterEggsState.currentEasterEgg = easterEgg;
		easterEggsState.displayEasterEgg = true;
	}

    /**
     * @ngdoc method
     * @name disableEasterEgg
     * @methodOf data-prep.services.state.service:EasterEggsStateService
     * @description disable the easter egg to display
     */
	function disableEasterEgg() {
		easterEggsState.currentEasterEgg = '';
		easterEggsState.displayEasterEgg = false;
	}
}
