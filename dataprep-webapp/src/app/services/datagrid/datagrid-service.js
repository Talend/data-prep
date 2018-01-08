/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import _ from 'lodash';

/**
 * @ngdoc service
 * @name data-prep.services.playground.service:DatagridService
 * @description Datagrid service. This service holds the datagrid (SlickGrid) view and the (SlickGrid) filters<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly for FILTERS.
 * {@link data-prep.services.filter.service:FilterService FilterService} must be the only entry point for datagrid filters</b>
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:ConverterService
 * @requires data-prep.services.utils.service:TextFormatService
 */
export default function DatagridService(state, StateService, ConverterService, TextFormatService) {
	'ngInject';

	const DELETE = 'DELETE';
	const REPLACE = 'REPLACE';
	const INSERT = 'INSERT';

	const service = {
		focusedColumn: null, // TODO JSO : put this in state

        // grid data
		updateData, // updata data in the current dataset
		getColumns,
		getColumnsContaining,

        // preview
		execute,
		previewDataExecutor,
	};
	return service;

    //------------------------------------------------------------------------------------------------------
    // ---------------------------------------------------DATA-----------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getLastNewColumnId
     * @methodOf data-prep.services.playground.service:DatagridService
     * @param {object} columns The new columns
     * @description Get the last new created column
     */
	function getLastNewColumnId(columns) {
		const ancientColumnsIds = _.map(state.playground.data.metadata.columns, 'id');
		const newColumnsIds = _.map(columns, 'id');
		const diffIds = _.difference(newColumnsIds, ancientColumnsIds);

		return diffIds[diffIds.length - 1];
	}

    /**
     * @ngdoc method
     * @name updateData
     * @methodOf data-prep.services.playground.service:DatagridService
     * @param {Object} data - the new data (columns and records)
     * @description Update the data in the datagrid
     */
	function updateData(data) {
		if (state.playground.data.metadata.columns.length < data.metadata.columns.length) {
			service.focusedColumn = getLastNewColumnId(data.metadata.columns);
		}

		StateService.setCurrentData(data);
	}

    //------------------------------------------------------------------------------------------------------
    // --------------------------------------------------PREVIEW---------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name execute
     * @methodOf data-prep.services.playground.service:DatagridService
     * @param {Object} executor The info to apply on the dataset
     * @description Update the data in the datagrid with a set of instructions and the column list to apply.
     * This allows to update the dataset, with limited SlickGrid computation, for more performant operations than
     * setItems which compute everything on the whole dataset.
     */
	function execute(executor) {
		if (!executor) {
			return;
		}

		const revertInstructions = [];

		state.playground.grid.dataView.beginUpdate();
		_.forEach(executor.instructions, function (step) {
			switch (step.type) {
			case INSERT:
				state.playground.grid.dataView.insertItem(step.index, step.row);
				revertInstructions.push({
					type: DELETE,
					row: step.row,
				});
				break;
			case DELETE: {
				const index = state.playground.grid.dataView.getIdxById(step.row.tdpId);
				state.playground.grid.dataView.deleteItem(step.row.tdpId);
				revertInstructions.push({
					type: INSERT,
					row: step.row,
					index,
				});
				break;
			}
			case REPLACE: {
				const originalRow = state.playground.grid.dataView.getItemById(step.row.tdpId);
				state.playground.grid.dataView.updateItem(step.row.tdpId, step.row);
				revertInstructions.push({
					type: REPLACE,
					row: originalRow,
				});
				break;
			}
			}
		});

		state.playground.grid.dataView.endUpdate();

		const reverter = {
			instructions: revertInstructions,
			preview: state.playground.data.preview,
			metadata: state.playground.data.metadata,
		};

		if (state.playground.data.metadata.columns.length < executor.metadata.columns.length) {
			service.focusedColumn = getLastNewColumnId(executor.metadata.columns);
		}

		StateService.setCurrentData({
			metadata: executor.metadata,
			records: state.playground.data.records,
			preview: executor.preview,
		});

		return reverter;
	}

    /**
     * @ngdoc method
     * @name previewDataExecutor
     * @methodOf data-prep.services.playground.service:DatagridService
     * @param {Object} data The new preview data to insert
     * @description Create an executor that reflect the provided preview data, in order to update the current dataset
     */
	function previewDataExecutor(data) {
		const executor = {
			metadata: data.metadata,
			instructions: [],
			preview: true,
		};

		let nextInsertionIndex = state.playground.grid.dataView.getIdxById(data.records[0].tdpId);
		_.forEach(data.records, function (row) {
			if (row.__tdpRowDiff || row.__tdpDiff) { // eslint-disable-line no-underscore-dangle
				if (row.__tdpRowDiff === 'new') { // eslint-disable-line no-underscore-dangle
					executor.instructions.push({
						type: INSERT,
						row,
						index: nextInsertionIndex,
					});
				}
				else {
					executor.instructions.push({
						type: REPLACE,
						row,
					});
				}
			}

			nextInsertionIndex++;
		});

		return executor;
	}

    //------------------------------------------------------------------------------------------------------
    // ------------------------------------------------DATA UTILS--------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getColumns
     * @methodOf data-prep.services.playground.service:DatagridService
     * @param {boolean} excludeNumeric - filter the numeric columns
     * @param {boolean} excludeBoolean - filter the boolean columns
     * @description Filter the column ids
     * @returns {Object[]} - the column list that match the desired filters (id & name)
     */
	function getColumns(excludeNumeric, excludeBoolean) {
		let cols = state.playground.data.metadata.columns;

		if (excludeNumeric) {
			cols = _.filter(cols, function (col) {
				const simplifiedType = ConverterService.simplifyType(col.type);
				return simplifiedType !== 'integer' && simplifiedType !== 'decimal';
			});
		}

		if (excludeBoolean) {
			cols = _.filter(cols, function (col) {
				return col.type !== 'boolean';
			});
		}

		return _.map(cols, function (col) {
			return { id: col.id, name: col.name };
		});
	}

    /**
     * @ngdoc method
     * @name getColumnsContaining
     * @methodOf data-prep.services.playground.service:DatagridService
     * @description Return the column id list that has a value that match the regexp
     * @returns {Object[]} The column list that contains a value that match the regexp (col.id & col.name)
     */
	function getColumnsContaining(phrase) {
		const results = [];

		if (!phrase) {
			return results;
		}

		const regexp = new RegExp(TextFormatService.escapeRegexpExceptStar(phrase));
		const canBeNumeric = !isNaN(phrase.replace(/\*/g, ''));
		const canBeBoolean = 'true'.match(regexp) || 'false'.match(regexp);

		const data = state.playground.data.records;
		let potentialColumns = getColumns(!canBeNumeric, !canBeBoolean);

        // we loop over data while there is data and potential columns that can contains the searched term
        // if a col value for a row contains the term, we add it to result
		let dataIndex = 0;
		while (dataIndex < data.length && potentialColumns.length) {
			const record = data[dataIndex];

			for (const col of potentialColumns) {
				if (record[col.id].toLowerCase().match(regexp)) {
					potentialColumns = potentialColumns.filter(nextCol => nextCol !== col);
					results.push(col);
				}
			}

			potentialColumns = _.difference(potentialColumns, results);
			dataIndex++;
		}

		return results;
	}
}
