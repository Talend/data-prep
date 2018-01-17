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
 * @name data-prep.datagrid.service:DatagridTooltipService
 * @description Datagrid private service that manage the grid tooltip
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function DatagridTooltipService($timeout, state, TextFormatService) {
	'ngInject';

	let tooltipTimeout;
	let tooltipShowPromise;
	const tooltipDelay = 300;

	const service = {
		init,
	};
	return service;

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name cancelTooltip
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @description Cancel the current tooltip promise
     */
	function cancelTooltip() {
		if (tooltipTimeout) {
			$timeout.cancel(tooltipTimeout);
			tooltipTimeout = null;
		}

		if (tooltipShowPromise) {
			$timeout.cancel(tooltipShowPromise);
			tooltipShowPromise = null;
		}
	}

    /**
     * @ngdoc method
     * @name createTooltip
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @param {object} event The Slickgrid cell enter event
     * @description Update the tooltip component and display with a delay
     */
	function createTooltip(event, grid, gridState) {
		tooltipTimeout = $timeout(() => {
			const cell = grid.getCellFromEvent(event);
			if (!cell) {
				return;
			}

			const row = cell.row;
			const item = gridState.dataView.getItem(row);

			const column = grid.getColumns()[cell.cell];
			const value = item[column.id] + '';

			if (shouldShowTooltip(value, cell, grid, gridState)) {
				tooltipShowPromise = $timeout(() => {
					gridState.tooltip = {
						position: {
							x: event.clientX,
							y: event.clientY,
						},
						htmlStr: TextFormatService.adaptToGridConstraints(value),
					};
					gridState.showTooltip = true;
				});
			}
		}, tooltipDelay, false);
	}

    /**
     * @ngdoc method
     * @name updateTooltip
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @param {object} event The Slickgrid cell enter event
     * @description Cancel the old tooltip promise if necessary and create a new one
     */
	function updateTooltip(event, grid, gridState) {
		cancelTooltip();
		createTooltip(event, grid, gridState);
	}

    /**
     * @ngdoc method
     * @name hideTooltip
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @description Cancel the old tooltip promise if necessary and hide the tooltip
     */
	function hideTooltip(gridState) {
		cancelTooltip();
		if (gridState.showTooltip) {
			$timeout(() => {
				gridState.showTooltip = false;
			});
		}
	}

    /**
     * @ngdoc method
     * @name shouldShowTooltip
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @description Check if the text should be shown in a tooltip (content too long)
     * @param {string} text the text to display
     * @param {object} cell The cell containing the text
     */
	function shouldShowTooltip(text, cell, grid, gridState) {
        // do NOT show if content is empty
		if (text === '') {
			return false;
		}

        // show if content is multiline (avoid too loud check with div size)
		const textConverted = text + '';
		if (textConverted.indexOf('\n') > -1) {
			return true;
		}

        // heavy check based on div size
		const box = grid.getCellNodeBox(cell.row, cell.cell);
		const ruler = gridState.tooltipRuler;
		ruler.text(textConverted);

        // return if the content is bigger than the displayed box by computing the diff between the displayed box
        // and the hidden tooltip ruler size minus the cell padding
		return (box.right - box.left - 12) <= ruler.width() || (box.bottom - box.top) < ruler.height();
	}

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.datagrid.service:DatagridTooltipService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the tooltips listeners
     */
	function init(grid, gridState) {
		// show tooltip on hover
		grid.onMouseEnter.subscribe(event => updateTooltip(event, grid, gridState));

		// hide tooltip on leave
		grid.onMouseLeave.subscribe(() => hideTooltip(gridState));
	}
}
