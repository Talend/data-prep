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
export default class HomeCtrl {

	constructor($window, $document, $state, state) {
		'ngInject';
		this.$window = $window;
		this.$document = $document;
		this.$state = $state;
		this.state = state;
		this.DATA_INVENTORY_PANEL_KEY = 'org.talend.dataprep.data_inventory_panel_display';

        /**
         * @ngdoc property
         * @name showRightPanel
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description Flag that control the right panel display
         * @type {boolean}
         */
		this.showRightPanel = this.getRightPanelState();

        /**
         * @ngdoc property
         * @name showRightPanelIcon
         * @propertyOf data-prep.home.controller:HomeCtrl
         * @description data icon of the state of the right panel
         * @type {string}
         */
		this.showRightPanelIcon = 'u';
	}

	$onInit() {
		this.updateRightPanelIcon();
	}

    //--------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------Right panel------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name toggleRightPanel
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Toggle the right panel containing inventory data
     */
	toggleRightPanel() {
		this.showRightPanel = !this.showRightPanel;

		this.saveRightPanelState();
		this.updateRightPanelIcon();
	}

    /**
     * @ngdoc method
     * @name updateRightPanelIcon
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Update the displayed icon to toggle right panel
     */
	updateRightPanelIcon() {
		this.showRightPanelIcon = this.showRightPanel ? 't' : 'u';
	}

    /**
     * @ngdoc method
     * @name getRightPanelState
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Get the data inventory panel parameters from localStorage
     */
	getRightPanelState() {
		const params = this.$window.localStorage.getItem(this.DATA_INVENTORY_PANEL_KEY);
		return params ? JSON.parse(params) : false;
	}

    /**
     * @ngdoc method
     * @name saveRightPanelState
     * @methodOf data-prep.home.controller:HomeCtrl
     * @description Save the data inventory panel parameters in localStorage
     */
	saveRightPanelState() {
		this.$window.localStorage.setItem(this.DATA_INVENTORY_PANEL_KEY, JSON.stringify(this.showRightPanel));
	}
}
