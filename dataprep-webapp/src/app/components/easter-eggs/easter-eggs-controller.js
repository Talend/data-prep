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
 * @name data-prep.easter-eggs
 * @description Easter eggs controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 */
export default function EasterEggsCtrl(state, StateService) {
    'ngInject';

    var vm = this;
    vm.state = state;
    vm.disableEasterEgg = StateService.disableEasterEgg;
}