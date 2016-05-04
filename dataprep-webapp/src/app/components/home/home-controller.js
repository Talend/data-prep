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
 * @name data-prep.home.controller:HomeCtrl
 * @description Home controller.
 * @requires data-prep.services.state.constant:state
 */
export default function HomeCtrl($window, $document, $state, state) {
    'ngInject';

    var vm = this;
    var DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';
    vm.$state = $state;
    vm.state = state;

    /**
     * @ngdoc property
     * @name showRightPanel
     * @propertyOf data-prep.home.controller:HomeCtrl
     * @description Flag that control the right panel display
     * @type {boolean}
     */
    vm.showRightPanel = getRightPanelState();

    /**
     * @ngdoc property
     * @name showRightPanelIcon
     * @propertyOf data-prep.home.controller:HomeCtrl
     * @description data icon of the state of the right panel
     * @type {string}
     */
    vm.showRightPanelIcon = 'u';
    updateRightPanelIcon();

    /**
     * @ngdoc property
     * @name uploadingDatasets
     * @propertyOf data-prep.home.controller:HomeCtrl
     * @description The current uploading datasets
     * @type {object[]}
     */
    vm.uploadingDatasets = state.dataset.uploadingDatasets;

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------Right panel------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggleRightPanel
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Toggle the right panel containing inventory data
     */
    vm.toggleRightPanel = () => {
        vm.showRightPanel = !vm.showRightPanel;

        saveRightPanelState();
        updateRightPanelIcon();
    };

    /**
     * @ngdoc method
     * @name updateRightPanelIcon
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Update the displayed icon to toggle right panel
     */
    function updateRightPanelIcon() {
        vm.showRightPanelIcon = vm.showRightPanel ? 't' : 'u';
    }

    /**
     * @ngdoc method
     * @name getRightPanelState
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Get the data inventory panel parameters from localStorage
     */
    function getRightPanelState() {
        let params = $window.localStorage.getItem(DATA_INVENTORY_PANEL_KEY);
        return params ? JSON.parse(params) : false;
    }

    /**
     * @ngdoc method
     * @name saveRightPanelState
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Save the data inventory panel parameters in localStorage
     */
    function saveRightPanelState() {
        $window.localStorage.setItem(DATA_INVENTORY_PANEL_KEY, JSON.stringify(vm.showRightPanel));
    }
}