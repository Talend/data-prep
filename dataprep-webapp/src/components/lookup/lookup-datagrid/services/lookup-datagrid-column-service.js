(function () {
    'use strict';

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
    function LookupDatagridColumnService(state, $rootScope, $compile, LookupDatagridStyleService) {
        var grid;
        var availableHeaders = [];
        var colIndexName = 'tdpId';

        return {
            init: init,
            renewAllColumns: renewAllColumns,
            createColumns: createColumns
        };

        //------------------------------------------------------------------------------------------------------
        //-----------------------------------------------GRID COLUMNS-------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name createColumnDefinition
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @param {object} col The column metadata to adapt
         * @description Adapt column metadata to slick column.
         * @returns {object} The adapted column item
         */
        function createColumnDefinition(col) {
            return {
                id: col.id,
                field: col.id,
                name: '',
                formatter: LookupDatagridStyleService.columnFormatter(col),
                minWidth: 120,
                tdpColMetadata: col
            };
        }

        /**
         * @ngdoc method
         * @name createColumns
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @param {object[]} columnsMetadata Columns details
         * @description Two modes :
         * <ul>
         *     <li>Classic : we map each column to a header. This header can be a reused header if the column was
         * the same as before, or a new created one otherwise.</li>
         * </ul>
         */

        function createColumns(columnsMetadata) {
            //create new SlickGrid columns
            var colIndexArray = [];
            var colIndexNameTemplate = '<div class="lookup-slick-header-column-index">#</div>';

            //Add index column
            colIndexArray.push({
                id: colIndexName,
                name: colIndexNameTemplate,
                field: colIndexName,
                maxWidth: 45,
                formatter: function formatterIndex(row, cell, value) {
                    return '<div class="index-cell">' + value + '</div>';
                },
                resizable: false,
                selectable: false
            });

            return _.union(colIndexArray, _.map(columnsMetadata, function (col) {
                return createColumnDefinition(col);
            }));
        }

        //------------------------------------------------------------------------------------------------------
        //-----------------------------------------------GRID HEADERS-------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name destroyHeader
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @param {object} headerDefinition The header definition that contains scope (the angular scope) and header (the element)
         * @description Destroy the angular scope and header element
         */
        function destroyHeader(headerDefinition) {
            headerDefinition.scope.$destroy();
            headerDefinition.header.remove();
        }

        /**
         * @ngdoc method
         * @name renewAllColumns
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @description Set the 'renewAllFlag' with provided value to control whether the headers should be reused or recreated
         */
        function renewAllColumns() {
            _.forEach(availableHeaders, destroyHeader);
            availableHeaders = [];
        }

        /**
         * @ngdoc method
         * @name createHeader
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @description [PRIVATE] Create a column header object containing
         * <ul>
         *     <li>the element directive</li>
         *     <li>The directive scope</li>
         *     <li>The column metadata</li>
         * </ul>
         */
        function createHeader(col) {
            var headerScope = $rootScope.$new(true);
            headerScope.column = col;
            headerScope.added = _.find(state.playground.lookupGrid.addedToLookup, {id: col.id});
            var headerElement = angular.element('<lookup-datagrid-header column="column" added="added"></lookup-datagrid-header>');
            $compile(headerElement)(headerScope);

            return {
                id: col.id,
                scope: headerScope,
                header: headerElement
            };
        }

        /**
         * @ngdoc method
         * @name createAndAttachHeader
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @param {object} event The Slickgrid header creation event
         * @param {object} columnsArgs The column header arguments passed by SlickGrid
         * @description This is part of the process to avoid recreation od the lookup-datagrid header when it is not necessary.
         * It fetch an existing saved header to reuse it, or create it otherwise.
         * The existing header is then updated with the new column metadata.
         */
        function createAndAttachHeader(event, columnsArgs) {
            var columnDef = columnsArgs.column;
            if (columnDef.id === colIndexName) {
                return;
            }

            //Get existing header and remove it from available headers list
            var headerDefinition = _.find(availableHeaders, {id: columnDef.id});
            if (headerDefinition) {
                var headerIndex = availableHeaders.indexOf(headerDefinition);
                availableHeaders.splice(headerIndex, 1);
            }

            //Create the header if no available created header, update it otherwise
            if (headerDefinition) {
                headerDefinition.scope.column = columnDef.tdpColMetadata;
                headerDefinition.scope.$digest();
            }
            else {
                headerDefinition = createHeader(columnDef.tdpColMetadata);
            }

            //Update column definition
            columnDef.scope = headerDefinition.scope;
            columnDef.header = headerDefinition.header;

            //Append the header
            var node = angular.element(columnsArgs.node);
            node.append(headerDefinition.header);
        }

        //------------------------------------------------------------------------------------------------------
        //--------------------------------------------------INIT------------------------------------------------
        //------------------------------------------------------------------------------------------------------
        /**
         * @ngdoc method
         * @name attachColumnHeaderEvents
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @description Attach listeners for header creation/destroy. The handler detachs and save headers on destroy,
         * attach (create them if necessary) and update them on render
         */
        function attachColumnHeaderEvents() {
            grid.onHeaderCellRendered.subscribe(createAndAttachHeader);
        }

        /**
         * @ngdoc method
         * @name init
         * @methodOf data-prep.lookup.service:LookupDatagridColumnService
         * @param {object} newGrid The new grid
         * @description Initializes the grid and attach the column listeners
         */
        function init(newGrid) {
            grid = newGrid;
            attachColumnHeaderEvents();
        }
    }

    angular.module('data-prep.lookup')
        .service('LookupDatagridColumnService', LookupDatagridColumnService);
})();
