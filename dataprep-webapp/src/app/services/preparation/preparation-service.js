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
 * @name data-prep.services.preparation.service:PreparationService
 * @description Preparation list service. this service manage the operations that touches the preparations
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.state.service:StateService
 * @requires data-prep.services.utils.service:StorageService
 * @requires data-prep.services.preparation.service:PreparationListService
 * @requires data-prep.services.preparation.service:PreparationRestService
 */
export default function PreparationService($q, $state, $stateParams, state, StateService, StorageService, PreparationListService, PreparationRestService) {
    'ngInject';

    return {
        //get, refresh preparations
        getPreparations: getPreparations,
        refreshPreparations: PreparationListService.refreshPreparations,

        //details, content
        getContent: PreparationRestService.getContent,
        getDetails: PreparationRestService.getDetails,

        //preparation lifecycle
        create: create,
        copy: PreparationListService.copy,
        move: PreparationListService.move,
        update: PreparationRestService.update,
        delete: deletePreparation,
        setName: setName,
        open: open,

        //preparation steps lifecycle
        copyImplicitParameters: copyImplicitParameters,
        paramsHasChanged: paramsHasChanged,
        appendStep: PreparationRestService.appendStep,
        updateStep: updateStep,
        removeStep: PreparationRestService.removeStep,
        setHead: PreparationRestService.setHead,
        copySteps: PreparationRestService.copySteps,

        //preview
        getPreviewDiff: PreparationRestService.getPreviewDiff,
        getPreviewUpdate: PreparationRestService.getPreviewUpdate,
        getPreviewAdd: PreparationRestService.getPreviewAdd
    };

    //---------------------------------------------------------------------------------
    //------------------------------------GET/REFRESH----------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getPreparations
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @description Return preparation promise that resolve current preparation list if not empty, or call GET service
     * @returns {promise} The process promise
     */
    function getPreparations() {
        if (PreparationListService.hasPreparationsPromise()) {
            return PreparationListService.getPreparationsPromise();
        }
        else {
            return state.inventory.preparations !== null ?
                $q.when(state.inventory.preparations) :
                PreparationListService.refreshPreparations();
        }
    }

    //---------------------------------------------------------------------------------
    //-----------------------------------------LIFE------------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {string} datasetId The dataset id
     * @param {string} name The preparation name
     * @description Create a new preparation
     * @returns {promise} The POST promise
     */
    function create(datasetId, name) {
        return PreparationListService.create(datasetId, name)
            .then((preparation) => {
                //get all dataset aggregations per columns from localStorage and save them for the new preparation
                StorageService.savePreparationAggregationsFromDataset(datasetId, preparation.id);
                return preparation;
            });
    }

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {object} preparation The preparation to delete
     * @description Delete a preparation
     * @returns {promise} The DELETE promise
     */
    function deletePreparation(preparation) {
        return PreparationListService.delete(preparation)
            .then((response) => {
                //get remove all preparation aggregations per columns in localStorage
                StorageService.removeAllAggregations(preparation.dataSetId, preparation.id);
                return response;
            });
    }

    //---------------------------------------------------------------------------------
    //----------------------------------------UPDATE-----------------------------------
    //---------------------------------------------------------------------------------

    /**
     * @ngdoc method
     * @name setName
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {string} preparationId The preparation id
     * @param {string} name The preparation name
     * @description Create a new preparation if no preparation is loaded, update the name otherwise
     * @returns {promise} The POST promise
     */
    function setName(preparationId, name) {
        return PreparationListService.update(preparationId, name)
            .then((preparation) => {
                StorageService.moveAggregations(preparation.dataSetId, preparationId, preparation.id);
                return preparation;
            });
    }

    /**
     * @ngdoc method
     * @name copyImplicitParameters
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {object} parameters The parameters to copy into
     * @param {object} originalParameters The original parameters containing the implicit parameters values
     * @description Copy the original implicit parameters values into new parameters
     */
    function copyImplicitParameters(parameters, originalParameters) {
        parameters.scope = originalParameters.scope;

        if ('column_id' in originalParameters) {
            parameters.column_id = originalParameters.column_id;
        }

        if ('column_name' in originalParameters) {
            parameters.column_name = originalParameters.column_name;
        }

        if ('row_id' in originalParameters) {
            parameters.row_id = originalParameters.row_id;
        }
    }

    /**
     * @ngdoc method
     * @name updateStep
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {string} preparationId The preparation id
     * @param {object} step The step to update
     * @param {object} parameters The new action parameters
     * @description Update a step with new parameters
     * @returns {promise} The PUT promise
     */
    function updateStep(preparationId, step, parameters) {
        return PreparationRestService.updateStep(
            preparationId,
            step.transformation.stepId,
            {action: step.transformation.name, parameters: parameters}
        );
    }

    /**
     * @ngdoc method
     * @name paramsHasChanged
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {object} step The step to update
     * @param {object} newParams The new action parameters
     * @description Check if the parameters has changed
     * @returns {boolean} true if parameters has changed, false otherwise
     */
    function paramsHasChanged(step, newParams) {
        return JSON.stringify(newParams) !== JSON.stringify(step.actionParameters.parameters);
    }


    /**
     * @ngdoc method
     * @name open
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @param {object} preparation
     * @description open a preparation
     */
    function open(preparation) {
        StateService.setPreviousRoute('nav.index.preparations', {folderPath: $stateParams.folderPath});
        $state.go('playground.preparation', {prepid: preparation.id});
    }
}