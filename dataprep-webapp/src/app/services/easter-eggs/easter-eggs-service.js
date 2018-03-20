/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.easter-eggs.service:EasterEggsService
 * @description Easter Eggs service. This service that deals with easter eggs.
 * @requires data-prep.services.state.service:StateService
 */
export default function EasterEggsService(StateService) {
	'ngInject';

    /**
     * @ngdoc method
     * @name enableEasterEgg
     * @methodOf data-prep.services.easter-eggs.service:EasterEggsService
     * @description Enable easter egg depending on the given input.
     */
	this.enableEasterEgg = function enableEasterEgg(input) {
		StateService.enableEasterEgg(input);
	};
}
