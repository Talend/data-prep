/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './datagrid.html';

/**
 * @ngdoc directive
 * @name data-prep.datagrid.directive:Datagrid
 * @description This directive create the SlickGrid datagrid<br/>
 * Watchers :
 * <ul>
 *         <li>Grid header directive list : insert grid header in SlickGrid headers</li>
 *         <li>Grid Metadata : it means the loaded dataset has changed, the styles are reset</li>
 *         <li>Grid data : update grid headers and styles</li>
 *         <li>Filters : reset the styles</li>
 * </ul>
 *
 * @requires data-prep.state.service:state
 * @requires data-prep.datagrid.service:DatagridGridService
 * @requires data-prep.datagrid.service:DatagridColumnService
 * @requires data-prep.datagrid.service:DatagridStyleService
 * @requires data-prep.datagrid.service:DatagridSizeService
 * @requires data-prep.datagrid.service:DatagridExternalService
 * @restrict E
 */
export default function Datagrid($timeout, state, DatagridGridService, DatagridColumnService, DatagridStyleService, DatagridSizeService, DatagridExternalService) {
	'ngInject';

	return {
		restrict: 'E',
		templateUrl: template,
		bindToController: true,
		controllerAs: 'datagridCtrl',
		controller() {
			this.state = state;
			this.datagridHeight = '100%';
		},

		link(scope, iElement, attr, ctrl) {
			let grid;
			let columnTimeout;
			let columnStyleTimeout;
			let cellHighlightTimeout;
			let externalTimeout;
			let focusTimeout;

            //------------------------------------------------------------------------------------------------------
            // --------------------------------------------------GETTERS---------------------------------------------
            //------------------------------------------------------------------------------------------------------
            /**
             * @ngdoc method
             * @name getMetadata
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the loaded metadata
             */
			function getMetadata() {
				return state.playground.dataset;
			}

            /**
             * @ngdoc method
             * @name getSelectedColumns
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the selected columns
             */
			function getSelectedColumns() {
				return state.playground.grid.selectedColumns;
			}

            /**
             * @ngdoc method
             * @name getSelectedLine
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the selected line
             */
			function getSelectedLine() {
				return state.playground.grid.selectedLine;
			}

            /**
             * @ngdoc method
             * @name getData
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the loaded data
             */
			function getData() {
				return state.playground.data;
			}

            /**
             * @ngdoc method
             * @name getFilters
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the filters list
             */
			function getFilters() {
				return state.playground.filter.gridFilters;
			}

            /**
             * @ngdoc method
             * @name getFiltersState
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Get the filters state
             */
			function getFiltersState() {
				return state.playground.filter.enabled;
			}

            /**
             * @ngdoc method
             * @name getLookupVisibility
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Return condition that trigger a grid resize
             */
			function getLookupVisibility() {
				return state.playground.lookup.visibility;
			}

            //------------------------------------------------------------------------------------------------------
            // ---------------------------------------------------UTILS----------------------------------------------
            //------------------------------------------------------------------------------------------------------
            /**
             * @ngdoc method
             * @name onMetadataChange
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Reset cell styles, scroll to top and expect to recreate all columns on next update
             */
			const onMetadataChange = function onMetadataChange() {
				if (grid) {
					grid.scrollRowToTop(0);
					DatagridColumnService.renewAllColumns(true);
				}
			};

            /**
             * @ngdoc method
             * @name onDataChange
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Update and resize the columns with its headers, set grid styles
             */
			const onDataChange = function onDataChange(data) {
				if (data) {
					initGridIfNeeded();

                    // create columns
					$timeout.cancel(columnTimeout);
					columnTimeout = $timeout(function () {
						const columns = DatagridColumnService.createColumns(data.metadata.columns, data.preview);
						DatagridSizeService.autosizeColumns(columns); // IMPORTANT : this set columns in the grid
						DatagridColumnService.renewAllColumns(false);
						DatagridStyleService.updateColumnsClass(getSelectedColumns());
						grid.invalidate();
					}, 0, false);

                    // focus specific column
					$timeout.cancel(focusTimeout);
					focusTimeout = $timeout(
                        () => DatagridGridService.navigateToFocusedColumn(),
                        500,
                        false
                    );
				}
			};

            /**
             * @ngdoc method
             * @name onFiltersChange
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Refresh cell styles and scroll to top
             */
			const onFiltersChange = function onFiltersChange() {
				if (grid) {
					DatagridStyleService.resetCellStyles();
					grid.scrollRowToTop(0);
					DatagridExternalService.updateGridRangeIndex();
					// resize grid
					$timeout(grid.resizeCanvas, 500, false);
				}
			};

            /**
             * @ngdoc method
             * @name onColumnSelectionChange
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Refresh selected column grid styles
             */
			const onColumnSelectionChange = function onColumnSelectionChange() {
				if (grid) {
                    // Update cell highlights
					DatagridStyleService.resetHighlightStyles();
					$timeout.cancel(columnStyleTimeout);
					columnStyleTimeout = $timeout(function () {
						if (getSelectedColumns().length === 1) {
							if (getSelectedLine()) { // Update selected column style + the selected line style is managed by the grid
								DatagridStyleService.updateColumnsClass(getSelectedColumns());
							}
							else {
								DatagridStyleService.resetStyles(getSelectedColumns());
							}
						}
						else if (getSelectedColumns().length > 1) {
                            // Update selected columns style if getSelectedColumns().length > 1
                            // Remove selected columns style + the selected line style is managed by the grid if getSelectedColumns().length === 0
							DatagridStyleService.resetStyles(getSelectedColumns());
						}
						else {
							DatagridStyleService.updateColumnsClass(getSelectedColumns());
						}
					}, 0, false);

                    // manage column selection (external)
					$timeout.cancel(externalTimeout);
					if (getData() && !getData().preview) {
						externalTimeout = $timeout(
                            () => DatagridExternalService.updateSuggestionPanel(),
                            500
                        );
					}
				}
			};

            /**
             * @ngdoc method
             * @name onSelectionChange
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Refresh the cell highlight
             */
			const onSelectionChange = function onSelectionChange() {
				if (grid) {
					if (getSelectedColumns().length === 1) {
						const stateSelectedLine = getSelectedLine();
						const stateGridData = getData();
						const stateSelectedColumn = getSelectedColumns()[0];
						$timeout.cancel(cellHighlightTimeout);
						if (stateSelectedLine && stateSelectedColumn) {
							const lineIndex = state.playground.grid.dataView.getRowById(stateSelectedLine.tdpId);
							const columnIndex = grid.getColumnIndex(stateSelectedColumn.id);
							grid.setActiveCell(lineIndex, columnIndex);
						}
						if (stateSelectedLine && stateGridData && !stateGridData.preview) {
							cellHighlightTimeout = $timeout(() => {
								const colId = stateSelectedColumn && stateSelectedColumn.id;
								const content = stateSelectedLine[colId];
								DatagridStyleService.highlightCellsContaining(colId, content);
							},

                                500,
                                false
                            );
						}
					}
				}
			};

            /**
             * @ngdoc method
             * @name resize
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Resize grid canvas
             */
			function resize() {
				if (grid) {
					ctrl.datagridHeight = getLookupVisibility() ? 'calc(100% - 315px)' : '100%';
					$timeout(grid.resizeCanvas, 0, false);
				}
			}

            //------------------------------------------------------------------------------------------------------
            // ---------------------------------------------------INIT-----------------------------------------------
            //------------------------------------------------------------------------------------------------------
            /**
             * @ngdoc method
             * @name initGridIfNeeded
             * @methodOf data-prep.datagrid.directive:Datagrid
             * @description Init Slick grid and init datagrid private services.
             */
			function initGridIfNeeded() {
				if (!grid) {
					grid = DatagridGridService.initGrid('#datagrid');

                    // the tooltip ruler is used compute a cell text regardless of the font and zoom used.
                    // To do so, the text is put into an invisible span so that the span can be measured.
					state.playground.grid.tooltipRuler = iElement.find('#tooltip-ruler').eq(0);
				}
			}

            //------------------------------------------------------------------------------------------------------
            // -------------------------------------------------WATCHERS---------------------------------------------
            //------------------------------------------------------------------------------------------------------

            /**
             * Scroll to top when loaded dataset change
             */
			scope.$watch(getMetadata, onMetadataChange);

            /**
             * Update grid columns and invalidate grid on data change
             */
			scope.$watch(getData, onDataChange);

            /**
             * When filter change, displayed values change, so we reset active cell and cell styles
             */
			scope.$watchGroup([getFilters, getFiltersState], onFiltersChange);

            /**
             * When lookup is displayed/hidden changes, the grid should be resized fit available space
             */
			scope.$watch(getLookupVisibility, resize);

            /**
             * when new columns are selected
             */
			scope.$watch(getSelectedColumns, onColumnSelectionChange);

            /**
             * when the active cell change
             */
			scope.$watchGroup([getSelectedLine, () => getSelectedColumns()[0]], onSelectionChange);
		},
	};
}
