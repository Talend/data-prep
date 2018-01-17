/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/
import { COLUMN_INDEX_ID } from '../../../datagrid/services/datagrid-column-service';
/**
 * @ngdoc service
 * @name data-prep.lookup.service:LookupDatagridColumnService
 * @description Datagrid private service that manages the grid columns and columns headers
 * LEXICON :
 * <ul>
 *     <li>Column : the slick grid column</li>
 *     <li>Header : the lookup-datagrid header directive. The header created by SlickGrid will be called by 'SlickGird Header'</li>
 * </ul>
 * @requires data-prep.lookup.service:LookupDatagridStyleService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.state.constant:state
 */
export default function LookupDatagridColumnService(state, $rootScope, DatagridColumnService) {
	'ngInject';

	let grid;

	return {
		init,
	};

    //------------------------------------------------------------------------------------------------------
    // -----------------------------------------------GRID HEADERS-------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name createAndAttachHeader
     * @methodOf data-prep.lookup.service:LookupDatagridColumnService
     * @param {object} event The Slickgrid header creation event
     * @param {object} columnsArgs The column header arguments passed by SlickGrid
     * @description creates and attaches column header
     * The existing header is then updated with the new column metadata.
     */
	function createAndAttachHeader(event, columnsArgs) {
		const columnDef = columnsArgs.column;
		if (columnDef.id === COLUMN_INDEX_ID) {
			return;
		}

		const headerScope = $rootScope.$new(true);
		headerScope.column = columnDef.tdpColMetadata;
		headerScope.added = _.find(state.playground.lookup.columnCheckboxes, { id: columnDef.tdpColMetadata.id });
		const headerElement = angular.element('<lookup-datagrid-header column="column" added="added"></lookup-datagrid-header>');
		const headerDefinition = DatagridColumnService.createHeader(columnDef.tdpColMetadata, headerElement, headerScope);

        // Append the header
		const node = angular.element(columnsArgs.node);
		node.append(headerDefinition.header);
	}

    //------------------------------------------------------------------------------------------------------
    // --------------------------------------------------INIT------------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.lookup.service:LookupDatagridColumnService
     * @param {object} newGrid The new grid
     * @description Initializes the grid and attach the column listeners
     */
	function init(newGrid) {
		grid = newGrid;
		grid.onHeaderCellRendered.subscribe(createAndAttachHeader);
	}
}
