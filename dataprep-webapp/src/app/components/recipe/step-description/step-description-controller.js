/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class StepDescriptionCtrl {

    constructor($translate) {
        'ngInject';

        this.$translate = $translate;
        this.stepDescription = '';
    }

    $onChanges() {
        switch (this.step.actionParameters.parameters.scope) {
            case 'column':
                this.stepDescription = this.$translate.instant('RECIPE_ITEM_ON_COL', { columnName: this.step.column.name.toUpperCase() });
                break;

            case 'cell':
                this.stepDescription = this.$translate.instant('RECIPE_ITEM_ON_CELL');
                break;

            case 'line':
                this.stepDescription = this.$translate.instant('RECIPE_ITEM_ON_LINE', { rowId: this.step.row.id });
                break;

            case 'dataset':
                if (this.step.transformation.name === 'lookup') {
                    const lookupStepDetails = this._getAddedColumnsInLookup(this.step);
                    this._updateLookupDescription(lookupStepDetails);
                }
                if (this.step.transformation.name === 'reorder') {
                    this.stepDescription = this.$translate.instant('RECIPE_ITEM_ON_COL', { columnName: this.step.column.name.toUpperCase() });
                }
                break;
        }
    }

    /**
     * @ngdoc method
     * @name _updateLookupDescription
     * @methodOf data-prep.step-description.controller:_updateLookupDescription
     * @param {object} lookupStepDetails The new step
     * @description creates the lookup step description
     */
    _updateLookupDescription(lookupStepDetails) {
        this.stepDescription = this.$translate.instant('LOOKUP_STEP_DESCRIPTION', {
            lookupDsName: this.step.actionParameters.parameters.lookup_ds_name,
            mainColName: this.step.column.name,
            lookupColName: this.step.actionParameters.parameters.lookup_join_on_name,
        });

        switch (lookupStepDetails.initialColsNbr) {
            case 1:
                this.stepDescription += this.$translate.instant('ONLY_1_ADDED_COL', lookupStepDetails);
                break;

            case 2:
                this.stepDescription += this.$translate.instant('ONLY_2_ADDED_COLS', lookupStepDetails);
                break;

            default:
                this.stepDescription += this.$translate.instant('MORE_THEN_2_ADDED_COLS', lookupStepDetails);
        }
    }

    /**
     * @ngdoc method
     * @name _getAddedColumnsInLookup
     * @methodOf data-prep.step-description.controller:StepDescriptionCtrl
     * @param {object} step The current step
     * @description having the Ids of the added columns, it collects the responding names
     * @returns {Object} The lookup added columns arguments
     */
    _getAddedColumnsInLookup(step) {
        const allAddedCols = _.map(step.actionParameters.parameters.lookup_selected_cols, 'name');
        return {
            initialColsNbr: allAddedCols.length,
            firstCol: allAddedCols.splice(0, 1).join(),//tab = []; tab[0]==>undefined, tab.join()==>''
            secondCol: allAddedCols.splice(0, 1).join(),
            restOfColsNbr: allAddedCols.length,
            restOfCols: allAddedCols.join(', '),
        };
    }
}

export default StepDescriptionCtrl;