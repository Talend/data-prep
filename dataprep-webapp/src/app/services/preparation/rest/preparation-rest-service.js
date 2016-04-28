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
 * @name data-prep.services.preparation.service:PreparationRestService
 * @description Preparation service. This service provides the entry point to preparation REST api. It holds the loaded preparation.<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.preparation.service:PreparationService PreparationService} must be the only entry point for preparations</b>
 */
export default function PreparationRestService($http, RestURLs) {
    'ngInject';

    return {
        //lifecycle
        create: create,
        copy: copy,
        move: move,
        update: update,
        delete: deletePreparation,

        //step
        appendStep: appendStep,
        updateStep: updateStep,
        removeStep: removeStep,
        setHead: setHead,
        copySteps: copySteps,

        //getter : list, content, details
        getPreparations: getPreparations,
        getContent: getContent,
        getDetails: getDetails,

        //preview
        getPreviewDiff: getPreviewDiff,
        getPreviewUpdate: getPreviewUpdate,
        getPreviewAdd: getPreviewAdd
    };

    //---------------------------------------------------------------------------------
    //----------------------------------------GETTERS----------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getPreparations
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @description Get All the user's preparations
     * @returns {promise} The GET promise
     */
    function getPreparations() {
        return $http.get(RestURLs.preparationUrl);
    }

    /**
     * @ngdoc method
     * @name getContent
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id to load
     * @param {string} stepId The step id to load
     * @description Get preparation records at the specific step
     * @returns {promise} The GET promise
     */
    function getContent(preparationId, stepId) {
        const url = `${RestURLs.preparationUrl}/${preparationId}/content?version=${stepId}`;
        return $http.get(url).then((res) => res.data);
    }

    /**
     * @ngdoc method
     * @name getDetails
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id to load
     * @description Get current preparation details
     * @returns {promise} The GET promise
     */
    function getDetails(preparationId) {
        return $http.get(`${RestURLs.preparationUrl}/${preparationId}/details`);
    }

    //---------------------------------------------------------------------------------
    //---------------------------------------LIFECYCLE---------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} datasetId The dataset id
     * @param {string} name The preparation name
     * @description Create a new preparation
     * @returns {promise} The POST promise
     */
    function create(datasetId, name) {
        const request = {
            method: 'POST',
            url: RestURLs.preparationUrl,
            data: {
                name: name,
                dataSetId: datasetId,
            },
        };

        return $http(request);
    }

    /**
     * @ngdoc method
     * @name copy
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {string} folderPath The destination folder path
     * @param {string} name The preparation name
     * @description Copy the preparation
     * @returns {promise} The POST promise
     */
    function copy(preparationId, folderPath, name) {
        const request = {
            method: 'POST',
            url: `${RestURLs.preparationUrl}/${preparationId}/copy?destination=${encodeURIComponent(folderPath)}&newName=${encodeURIComponent(name)}`,
        };
        return $http(request).then((resp) => resp.data);
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {string} fromPath The origin folder path
     * @param {string} folderPath The destination folder path
     * @param {string} name The preparation name
     * @description Move the preparation
     * @returns {promise} The PUT promise
     */
    function move(preparationId, fromPath, folderPath, name) {
        const origin = encodeURIComponent(fromPath);
        const destination = encodeURIComponent(folderPath);
        const newName = encodeURIComponent(name);
        const request = {
            method: 'PUT',
            url: `${RestURLs.preparationUrl}/${preparationId}/move?folder=${origin}&destination=${destination}&newName=${newName}`,
        };
        return $http(request).then((resp) => resp.data);
    }

    /**
     * @ngdoc method
     * @name update
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {Object} newData The new preparation data to update
     * @description Update the current preparation with the given data
     * @returns {promise} The PUT promise
     */
    function update(preparationId, newData) {
        const request = {
            method: 'PUT',
            url: `${RestURLs.preparationUrl}/${preparationId}`,
            headers: { 'Content-Type': 'application/json' },
            data: newData,
        };

        return $http(request).then((resp) => resp.data);
    }

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object} preparationId The preparation id to delete
     * @description Delete a preparation
     * @returns {promise} The DELETE promise
     */
    function deletePreparation(preparationId) {
        return $http.delete(RestURLs.preparationUrl + '/' + preparationId);
    }

    //---------------------------------------------------------------------------------
    //-----------------------------------------STEPS-----------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name adaptTransformAction
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
     * @param {string} insertionStepId The insertion point step id. (Head = 'head' | falsy | head_step_id)
     * @description Adapt transformation action to api
     * @returns {object} - the adapted action
     */
    function adaptTransformAction(actionParams, insertionStepId) {
        return {
            insertionStepId: insertionStepId,
            actions: actionParams instanceof Array ? actionParams : [actionParams],
        };
    }

    /**
     * @ngdoc method
     * @name appendStep
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object} preparationId The preparation id
     * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
     * @param {string} insertionStepId The insertion point step id. (Head = 'head' | falsy | head_step_id)
     * @description Append a new transformation in the current preparation.
     * @returns {promise} - the POST promise
     */
    function appendStep(preparationId, actionParams, insertionStepId) {
        const actionParam = adaptTransformAction(actionParams, insertionStepId);
        const request = {
            method: 'POST',
            url: `${RestURLs.preparationUrl}/${preparationId}/actions`,
            headers: { 'Content-Type': 'application/json' },
            data: actionParam,
        };

        return $http(request);
    }

    /**
     * @ngdoc method
     * @name updateStep
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preaparation id to update
     * @param {string} stepId The step to update
     * @param {object | array} actionParams The transformation(s) configuration {action: string, parameters: {object}}
     * @description Update a step with new parameters
     * @returns {promise} The PUT promise
     */
    function updateStep(preparationId, stepId, actionParams) {
        const request = {
            method: 'PUT',
            url: `${RestURLs.preparationUrl}/${preparationId}/actions/${stepId}`,
            headers: { 'Content-Type': 'application/json' },
            data: {actions: [actionParams]},
        };

        return $http(request);
    }

    /**
     * @ngdoc method
     * @name removeStep
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preaparation id to update
     * @param {string} stepId The step to delete
     * @description Delete a step
     * @returns {promise} The DELETE promise
     */
    function removeStep(preparationId, stepId) {
        const url = `${RestURLs.preparationUrl}/${preparationId}/actions/${stepId}`;
        return $http.delete(url);
    }

    /**
     * @ngdoc method
     * @name setHead
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {string} stepId The head step id
     * @description Move the preparation head to the specified step
     * @returns {promise} The PUT promise
     */
    function setHead(preparationId, stepId) {
        const url = `${RestURLs.preparationUrl}/${preparationId}/head/${stepId}`;
        return $http.put(url);
    }

    /**
     * @ngdoc method
     * @name copySteps
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {string} referenceId The preparation id containing the steps to copy
     * @description Apply the reference steps to the preparation
     * @returns {promise} The PUT promise
     */
    function copySteps(preparationId, referenceId) {
        const url = `${RestURLs.preparationUrl}/${preparationId}/steps/copy?from=${referenceId}`;
        return $http.put(url);
    }

    //---------------------------------------------------------------------------------
    //----------------------------------------PREVIEW----------------------------------
    //---------------------------------------------------------------------------------
    /**
     * @ngdoc method
     * @name getPreviewDiff
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object} params The preview parameters
     * @param {string} canceler The canceler promise
     * @description POST Preview diff between 2 unchanged steps of a recipe
     * @returns {promise} The POST promise
     */
    function getPreviewDiff(params, canceler) {
        const request = {
            method: 'POST',
            url: `${RestURLs.previewUrl}/diff`,
            headers: { 'Content-Type': 'application/json' },
            data: params,
            timeout: canceler.promise,
        };

        return $http(request);
    }

    /**
     * @ngdoc method
     * @name getPreviewUpdate
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object} params The preview parameters
     * @param {string} canceler The canceler promise
     * @description POST preview diff between 2 same actions but with 1 updated step
     * @returns {promise} The POST promise
     */
    function getPreviewUpdate(params, canceler) {
        const request = {
            method: 'POST',
            url: `${RestURLs.previewUrl}/update`,
            headers: { 'Content-Type': 'application/json' },
            data: params,
            timeout: canceler.promise,
        };

        return $http(request);
    }

    /**
     * @ngdoc method
     * @name getPreviewAdd
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {object} params The preview parameters
     * @param {string} canceler The canceler promise
     * @description POST preview diff between the preparation head and a new added transformation
     * @returns {promise} The POST promise
     */
    function getPreviewAdd(params, canceler) {
        const request = {
            method: 'POST',
            url: `${RestURLs.previewUrl}/add`,
            headers: { 'Content-Type': 'application/json' },
            data: params,
            timeout: canceler.promise,
        };

        return $http(request);
    }
}