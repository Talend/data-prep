/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.lookup.service:LookupDatagridTooltipService
 * @description Datagrid private service that manages the grid tooltip
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function LookupDatagridTooltipService($timeout, state, TextFormatService) {
	'ngInject';

	let grid;
	let tooltipTimeout;
	let tooltipShowPromise;
	const tooltipDelay = 300;

	const service = {
		showTooltip: false,
		tooltip: {},
		tooltipRuler: null,
		init,
	};
	return service;

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name cancelTooltip
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @description Cancels the current tooltip promise
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
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @param {object} event The Slickgrid cell enter event
     * @description Update the tooltip component and display with a delay
     */
	function createTooltip(event) {
		tooltipTimeout = $timeout(function () {
			const cell = grid.getCellFromEvent(event);
			if (!cell) {
				return;
			}

			const row = cell.row;
			const item = state.playground.lookup.dataView.getItem(row);

			const column = grid.getColumns()[cell.cell];
			const value = item[column.id] + '';

			if (shouldShowTooltip(value, cell)) {
				tooltipShowPromise = $timeout(function () {
					service.tooltip = {
						position: {
							x: event.clientX,
							y: event.clientY,
						},
						htmlStr: TextFormatService.adaptToGridConstraints(value),
					};
					service.showTooltip = true;
				});
			}
		}, tooltipDelay, false);
	}

    /**
     * @ngdoc method
     * @name updateTooltip
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @param {object} event The Slickgrid cell enter event
     * @description Cancel the old tooltip promise if necessary and create a new one
     */
	function updateTooltip(event) {
		cancelTooltip();
		createTooltip(event);
	}

    /**
     * @ngdoc method
     * @name hideTooltip
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @description Cancel the old tooltip promise if necessary and hide the tooltip
     */
	function hideTooltip() {
		cancelTooltip();
		if (service.showTooltip) {
			$timeout(function () {
				service.showTooltip = false;
			});
		}
	}

    /**
     * @ngdoc method
     * @name shouldShowTooltip
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @description Check if the text should be shown in a tooltip (content too long)
     * @param {string} text the text to display
     * @param {object} cell The cell containing the text
     */
	function shouldShowTooltip(text, cell) {
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
		const ruler = service.tooltipRuler;
		ruler.text(textConverted);

        // return if the content is bigger than the displayed box by computing the diff between the displayed box
        // and the hidden tooltip ruler size minus the cell padding
		return (box.right - box.left - 12) <= ruler.width() || (box.bottom - box.top) < ruler.height();
	}

    /**
     * @ngdoc method
     * @name attachTooltipListener
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @description Attach cell hover for tooltips listeners
     */
	function attachTooltipListener() {
        // show tooltip on hover
		grid.onMouseEnter.subscribe(updateTooltip);

        // hide tooltip on leave
		grid.onMouseLeave.subscribe(hideTooltip);
	}

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.lookup.service:LookupDatagridTooltipService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the tooltips listeners
     */
	function init(newGrid) {
		grid = newGrid;
		attachTooltipListener();
	}
}
