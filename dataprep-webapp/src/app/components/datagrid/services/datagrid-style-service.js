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
 * @name data-prep.datagrid.service:DatagridStyleService
 * @description Datagrid private service that manage the grid style
 * @requires data-prep.services.playground.service:DatagridService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function DatagridStyleService(DatagridService, ConverterService, TextFormatService) {
    'ngInject';
    var grid;

    return {
        init: init,
        columnFormatter: columnFormatter,
        getColumnPreviewStyle: getColumnPreviewStyle,
        highlightCellsContaining: highlightCellsContaining,
        resetCellStyles: resetCellStyles,
        resetStyles: resetStyles,
        updateColumnClass: updateColumnClass
    };

    //--------------------------------------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name resetCellStyles
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Reset the cells css
     */
    function resetCellStyles() {
        grid.resetActiveCell();
        grid.setCellCssStyles('highlight', {});
    }

    /**
     * @ngdoc method
     * @name addClass
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Add a css class to a column
     * @param {object} column The target column
     * @param {string} newClass The class to add
     */
    function addClass(column, newClass) {
        column.cssClass = (column.cssClass || '') + ' ' + newClass;
    }

    /**
     * @ngdoc method
     * @name updateSelectionClass
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Add 'selected' class if the column is the selected one
     * @param {object} column The target column
     * @param {string} selectedColId The selected column id
     */
    function updateSelectionClass(column, selectedColId) {
        if (column.id === selectedColId) {
            addClass(column, 'selected');
        }
    }

    /**
     * @ngdoc method
     * @name updateNumbersClass
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Add the 'number' class to the column if its type is a number type
     * @param {object} column the target column
     */
    function updateNumbersClass(column) {
        var simplifiedType = ConverterService.simplifyType(column.tdpColMetadata.type);
        if (simplifiedType === 'integer' || simplifiedType === 'decimal') {
            addClass(column, 'numbers');
        }
    }

    /**
     * @ngdoc method
     * @name updateColumnClass
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Set style classes on columns depending on its state (type, selection, ...)
     * @param {string} selectedColId The grid selected column Id
     */
    function updateColumnClass(selectedColId) {
        _.forEach(grid.getColumns(), function (column) {
            if (column.id === 'tdpId') {
                column.cssClass = 'index-column';
            }
            else {
                column.cssClass = null;
                updateSelectionClass(column, selectedColId);
                updateNumbersClass(column);
            }
        });
    }

    /**
     * @ngdoc method
     * @name columnFormatter
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Value formatter used in SlickGrid column definition. This is called to get a cell formatted value
     * @param {object} col The column to format
     */
    function columnFormatter(col) {

        var invalidValues = col.quality.invalidValues;
        var isInvalid = function isInvalid(value) {
            return invalidValues.indexOf(value) >= 0;
        };

        return function formatter(row, cell, value, columnDef, dataContext) {
            //hidden characters need to be shown
            var returnStr = TextFormatService.adaptToGridConstraints(value);

            //entire row modification preview
            switch (dataContext.__tdpRowDiff) {
                case 'delete':
                    return '<div class="cellDeletedValue">' + (returnStr ? returnStr : ' ') + '</div>';
                case 'new':
                    return '<div class="cellNewValue">' + (returnStr ? returnStr : ' ') + '</div>';
            }

            //cell modification preview
            if (dataContext.__tdpDiff && dataContext.__tdpDiff[columnDef.id]) {
                switch (dataContext.__tdpDiff[columnDef.id]) {
                    case 'update':
                        return '<div class="cellUpdateValue">' + returnStr + '</div>';
                    case 'new':
                        return '<div class="cellNewValue">' + returnStr + '</div>';
                    case 'delete':
                        return '<div class="cellDeletedValue">' + (returnStr ? returnStr : ' ') + '</div>';
                }
            }

            return returnStr + (isInvalid(value) ? '<div title="Invalid Value" class="red-rect"></div>' : '<div class="invisible-rect"></div>');
        };
    }

    /**
     * @ngdoc method
     * @name getColumnPreviewStyle
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Get the column header preview style
     * @param {object} col The column metadata
     */
    function getColumnPreviewStyle(col) {
        switch (col.__tdpColumnDiff) {
            case 'new':
                return 'newColumn';
            case 'delete':
                return 'deletedColumn';
            case 'update':
                return 'updatedColumn';
            default:
                return '';
        }
    }

    /**
     * @ngdoc method
     * @name resetStyles
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @description Reset the cell styles and update the columns style
     * @param {string} selectedColId The selected column id
     */
    function resetStyles(selectedColId) {
        resetCellStyles();
        updateColumnClass(selectedColId);
    }

    /**
     * @ngdoc method
     * @name highlightCellsContaining
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @param {String} colId The column id
     * @param {String} content The value to highlight
     * @description Highlight the cells in column that contains the same value as content
     */
    function highlightCellsContaining(colId, content) {
        var sameContentConfig = DatagridService.getSameContentConfig(colId, content, 'highlight');
        grid.setCellCssStyles('highlight', sameContentConfig);
    }

    /**
     * @ngdoc method
     * @name init
     * @methodOf data-prep.datagrid.service:DatagridStyleService
     * @param {object} newGrid The new grid
     * @description Initialize the grid and attach the style listeners
     */
    function init(newGrid) {
        grid = newGrid;
    }
}