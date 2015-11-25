(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.lookup.controller:LookupDatagridCtrl
     * @description Dataset grid controller.
     * @requires data-prep.services.state.constant:state
     * @requires data-prep.lookup.service:DatagridTooltipService
     */
    function LookupDatagridCtrl(state, LookupDatagridTooltipService) {
        this.datagridTooltipService = LookupDatagridTooltipService;
        this.state = state;
    }

    /**
     * @ngdoc property
     * @name tooltip
     * @propertyOf data-prep.lookup.controller:LookupDatagridCtrl
     * @description The tooltip infos
     * This is bound to {@link data-prep.lookup.service:LookupDatagridTooltipServicee LookupDatagridTooltipServicee}.tooltip
     */
    Object.defineProperty(LookupDatagridCtrl.prototype,
        'tooltip', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datagridTooltipService.tooltip;
            }
        });

    /**
     * @ngdoc property
     * @name showTooltip
     * @propertyOf data-prep.lookup.controller:LookupDatagridCtrl
     * @description The tooltip display flag
     * This is bound to {@link data-prep.lookup.service:LookupDatagridTooltipServicee LookupDatagridTooltipServicee}.showTooltip
     */
    Object.defineProperty(LookupDatagridCtrl.prototype,
        'showTooltip', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datagridTooltipService.showTooltip;
            }
        });

    angular.module('data-prep.lookup')
        .controller('LookupDatagridCtrl', LookupDatagridCtrl);
})();