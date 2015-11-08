(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name talend.widget.controller:TalendTabsCtrl
     * @description Tabs directive controller
     */
    function TalendTabsCtrl() {
        var vm = this;

        /**
         * @ngdoc property
         * @name tabs
         * @propertyOf talend.widget.controller:TalendTabsCtrl
         * @description The array containing all its tabs items
         * @type {Array}
         */
        vm.tabs = [];

        /**
         * @ngdoc method
         * @name register
         * @methodOf talend.widget.controller:TalendTabsCtrl
         * @description Register a tab
         */
        vm.register = function register(tab) {
            if (vm.tabs.length === 0) {
                tab.active = true;
            }
            vm.tabs.push(tab);
        };

        /**
         * @ngdoc method
         * @name select
         * @methodOf talend.widget.controller:TalendTabsCtrl
         * @description Select a tab
         */
        vm.select = function select(tab) {
            _.forEach(vm.tabs, function(tabToDeactivate) {
                tabToDeactivate.active = false;
            });
            tab.active = true;
            vm.onTabChange();
        };

        /**
         * @ngdoc method
         * @name setSelectedTab
         * @methodOf talend.widget.controller:TalendTabsCtrl
         * @description Set selected tab
         */
        vm.setSelectedTab = function setSelectedTab(index) {
            var tabToSelect = vm.tabs[index];
            if(tabToSelect) {
                vm.select(tabToSelect);
            }
        };

        /**
         * @ngdoc method
         * @name unregister
         * @methodOf talend.widget.controller:TalendTabsCtrl
         * @param {object} tab The tab to delete
         * @description Delete a tab
         */
        vm.unregister = function unregister(tab) {
            var index = vm.tabs.indexOf(tab);
            vm.tabs.splice(index, 1);
        };
    }

    angular.module('talend.widget')
        .controller('TalendTabsCtrl', TalendTabsCtrl);
})();