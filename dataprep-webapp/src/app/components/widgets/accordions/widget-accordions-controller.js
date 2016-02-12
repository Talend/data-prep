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
 * @name talend.widget.controller:TalendAccordionsCtrl
 * @description Accordions directive controller
 */
export default function TalendAccordionsCtrl() {
    var vm = this;

    /**
     * @ngdoc property
     * @name accordions
     * @propertyOf talend.widget.controller:TalendAccordionsCtrl
     * @description The array containing all its accordions items
     * @type {Array}
     */
    vm.accordions = [];

    /**
     * @ngdoc method
     * @name register
     * @methodOf talend.widget.controller:TalendAccordionsCtrl
     * @description Register an accordions item
     */
    vm.register = function register(accordion) {
        vm.accordions.push(accordion);
    };

    /**
     * @ngdoc method
     * @name toggle
     * @methodOf talend.widget.controller:TalendAccordionsCtrl
     * @description Open an accordion and hide the others
     */
    vm.toggle = function toggle(accordion) {
        var wasActive = accordion.active;
        _.forEach(vm.accordions, function (accordionToDeactivate) {
            accordionToDeactivate.close();
        });

        if (!wasActive) {
            accordion.open();
        }
    };

    /**
     * @ngdoc method
     * @name unregister
     * @methodOf talend.widget.controller:TalendAccordionsCtrl
     * @param {object} accordion The accordion to delete
     * @description Delete an accordion
     */
    vm.unregister = function unregister(accordion) {
        var index = vm.accordions.indexOf(accordion);
        vm.accordions.splice(index, 1);
    };
}