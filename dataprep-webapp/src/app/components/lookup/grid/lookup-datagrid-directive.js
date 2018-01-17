/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './lookup-datagrid.html';

/**
 * @ngdoc directive
 * @name data-prep.lookup.directive:LookupDatagrid
 * @description This directive create the SlickGrid lookup-datagrid<br/>
 * Watchers :
 * <ul>
 *         <li>Grid data : update grid headers and styles</li>
 * </ul>
 *
 * @requires data-prep.state.service:state
 * @requires data-prep.lookup.service:LookupDatagridGridService
 * @requires data-prep.lookup.service:LookupDatagridStyleService
 * @restrict E
 */
export default function LookupDatagrid($timeout, state, DatagridColumnService, DatagridTooltipService, LookupDatagridGridService,
    LookupDatagridStyleService) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'lookupDatagridCtrl',
		controller() {
			this.state = state;
		},
		link(scope, iElement, iAttrs, ctrl) {
			let grid;
			let columnTimeout;

            //------------------------------------------------------------------------------------------------------
            // --------------------------------------------------GETTERS---------------------------------------------
            //------------------------------------------------------------------------------------------------------

            /**
             * @ngdoc method
             * @name getData
             * @methodOf data-prep.lookup.directive:LookupDatagrid
             * @description [PRIVATE] Get the lookup loaded data
             */
			const getData = function getData() {
				return state.playground.lookup.data;
			};

            //------------------------------------------------------------------------------------------------------
            // ---------------------------------------------------UTILS----------------------------------------------
            //------------------------------------------------------------------------------------------------------

            /**
             * @ngdoc method
             * @name onDataChange
             * @methodOf data-prep.lookup.directive:LookupDatagrid
             * @description [PRIVATE] Update and resize the columns with its headers, set grid styles
             */
			const onDataChange = function onDataChange(data) {
				if (data) {
					if (grid) {
						grid.scrollRowToTop(0);
					}

					initGridIfNeeded();
					let columns;
					let selectedColumn;
					const stateSelectedColumn = ctrl.state.playground.lookup.selectedColumn;

                    // create columns, manage style and size, set columns in grid
					$timeout.cancel(columnTimeout);
					columnTimeout = $timeout(function () {
						const colIndexNameTemplate = '<div class="lookup-slick-header-column-index"></div>';
						columns = DatagridColumnService.createColumns(data.metadata.columns, null, colIndexNameTemplate);

						selectedColumn = stateSelectedColumn ? _.find(columns, { id: stateSelectedColumn.id }) : null;

						LookupDatagridStyleService.updateColumnClass(columns, selectedColumn);
						grid.setColumns(columns); // IMPORTANT : this set columns in the grid
					}, 0, false);
				}
			};

            //------------------------------------------------------------------------------------------------------
            // ---------------------------------------------------INIT-----------------------------------------------
            //------------------------------------------------------------------------------------------------------
            /**
             * @ngdoc method
             * @name initGridIfNeeded
             * @methodOf data-prep.lookup.directive:LookupDatagrid
             * @description [PRIVATE] Init Slick grid and init lookup-datagrid private services.
             */
			const initGridIfNeeded = function () {
				if (!grid) {
					grid = LookupDatagridGridService.initGrid('#lookup-datagrid');

                    // the tooltip ruler is used compute a cell text regardless of the font and zoom used.
                    // To do so, the text is put into an invisible span so that the span can be measured.
					state.playground.lookup.tooltipRuler = iElement.find('#lookup-tooltip-ruler').eq(0);
				}
			};

            //------------------------------------------------------------------------------------------------------
            // -------------------------------------------------WATCHERS---------------------------------------------
            //------------------------------------------------------------------------------------------------------
            /**
             * Update grid columns and invalidate grid on data change
             */
			scope.$watch(getData, onDataChange);
		},
	};
}
