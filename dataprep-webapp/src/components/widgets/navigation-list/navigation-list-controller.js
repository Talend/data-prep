(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name talend.widget.controller:NavigationListCtrl
     * @description navigation-list directive controller
     */
    function NavigationListCtrl($scope) {
        var vm = this;

        /**
         * @ngdoc property
         * @name firstLabelIndex
         * @propertyOf talend.widget.controller:NavigationListCtrl
         * @description index from which to start showing labels
         * @type {Number}
         */
        vm.firstLabelIndex = 0;

        /**
         * @ngdoc property
         * @name shownLabelsIndexes
         * @propertyOf talend.widget.controller:NavigationListCtrl
         * @description index from which to end showing labels
         * @type {Number}
         */
        vm.lastLabelIndex = +vm.nbreLabelsToShow;

        /**
         * @ngdoc method
         * @name showBack
         * @methodOf talend.widget.controller:NavigationListCtrl
         * @description navigate in the list to the left
         */
        vm.showBack = function showBack(){
            vm.firstLabelIndex--;
            vm.lastLabelIndex--;
        };

        /**
         * @ngdoc method
         * @name showForth
         * @methodOf talend.widget.controller:NavigationListCtrl
         * @description navigate in the list to the right
         */
        vm.showForth = function showForth(){
            vm.firstLabelIndex++;
            vm.lastLabelIndex++;
        };

        /**************************************************************************************************************/
        /******************************** Watcher to reset the navigation list  limits ********************************/
        /**************************************************************************************************************/
        /**
         * @ngdoc method
         * @name getSelectedItem
         * @methodOf talend.widget.controller:NavigationListCtrl
         * @description Get the selected Item
         */
        function getSelectedItem() {
            return vm.selectedItem;
        }

        /**
         * @ngdoc method
         * @name getList
         * @methodOf talend.widget.controller:NavigationListCtrl
         * @description Get the items list
         */
        function getList() {
            return vm.list;
        }

        $scope.$watchGroup([getList, getSelectedItem], function() {
            if(vm.list && vm.list.length > vm.nbreLabelsToShow && vm.selectedItem){
                for(var i= 0; i< vm.list.length; i++) {
                    if(vm.list[i] === vm.selectedItem){
                        if(i >= vm.lastLabelIndex)  {
                            vm.lastLabelIndex = i+1;
                            vm.firstLabelIndex = vm.lastLabelIndex - vm.nbreLabelsToShow;
                        }

                        if(i < vm.firstLabelIndex)  {
                            vm.firstLabelIndex = i;
                            vm.lastLabelIndex = vm.firstLabelIndex + vm.nbreLabelsToShow;
                        }
                        break;
                    }
                }
            } else {
                vm.firstLabelIndex = 0;
                vm.lastLabelIndex = +vm.nbreLabelsToShow;
            }
        });
    }

    angular.module('talend.widget')
        .controller('NavigationListCtrl', NavigationListCtrl);
})();