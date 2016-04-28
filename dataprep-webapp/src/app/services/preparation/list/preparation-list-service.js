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
 * @name data-prep.services.preparation.service:PreparationListService
 * @description Preparation list service. This service holds the preparations list and adapt them for the application.<br/>
 * <b style="color: red;">WARNING : do NOT use this service directly.
 * {@link data-prep.services.preparation.service:PreparationService PreparationService} must be the only entry point for preparations</b>
 * @requires data-prep.services.preparation.service:PreparationRestService
 * @requires data-prep.services.state.service:StateService
 */
export default function PreparationListService(PreparationRestService, StateService) {
    'ngInject';

    let preparationsPromise;

    return {
        refreshPreparations: refreshPreparations,
        getPreparationsPromise: getPreparationsPromise,
        hasPreparationsPromise: hasPreparationsPromise,

        create: create,
        copy: copy,
        move: move,
        update: update,
        delete: deletePreparation
    };

    /**
     * @ngdoc method
     * @name refreshPreparations
     * @methodOf data-prep.services.preparation.service:PreparationListService
     * @description Refresh the preparations list
     * @returns {promise} The process promise
     */
    function refreshPreparations() {
        preparationsPromise = PreparationRestService.getPreparations()
            .then((response) => {
                StateService.setPreparations(response.data);
                return response.data;
            })
            .catch(() => {
                StateService.setPreparations([]);
                return [];
            })
            .finally(() => preparationsPromise = null);
        return preparationsPromise;
    }

    /**
     * @ngdoc method
     * @name getPreparationsPromise
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @description Return preparation promise that resolve current preparation list if not empty, or call GET service
     * @returns {promise} The process promise
     */
    function getPreparationsPromise() {
        return preparationsPromise ? preparationsPromise : refreshPreparations();
    }

    /**
     * @ngdoc method
     * @name hasPreparationsPromise
     * @methodOf data-prep.services.preparation.service:PreparationService
     * @description Check if there is a fetch in progress
     * @returns {promise} Truthy when a fetch is in progress
     */
    function hasPreparationsPromise() {
        return preparationsPromise;
    }

    /**
     * @ngdoc method
     * @name create
     * @methodOf data-prep.services.preparation.service:PreparationListService
     * @param {string} datasetId The dataset id
     * @param {string} name The preparation name
     * @description Create a new preparation
     * @returns {promise} The POST promise
     */
    function create(datasetId, name) {
        let createdPreparationId;
        return PreparationRestService.create(datasetId, name)
            .then((response) => createdPreparationId = response.data)
            .then(refreshPreparations)
            .then((preparations) => _.find(preparations, { id: createdPreparationId }));
    }

    /**
     * @ngdoc method
     * @name copy
     * @methodOf data-prep.services.preparation.service:PreparationListService
     * @param {string} preparationId The preparation id
     * @param {string} folderPath The destination folder path
     * @param {string} name The preparation name
     * @description Copy the preparation
     * @returns {promise} The request promise
     */
    function copy(preparationId, folderPath, name) {
        return PreparationRestService.copy(preparationId, folderPath, name)
            .then((preparationCloneId) => {
                refreshPreparations();
                return preparationCloneId;
            });
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.services.preparation.service:PreparationListService
     * @param {string} preparationId The preparation id
     * @param {string} fromPath The origin folder path
     * @param {string} folderPath The destination folder path
     * @param {string} name The preparation name
     * @description Move the preparation
     * @returns {promise} The request promise
     */
    function move(preparationId, fromPath, folderPath, name) {
        return PreparationRestService.move(preparationId, fromPath, folderPath, name)
            .then(() => refreshPreparations());
    }

    /**
     * @ngdoc method
     * @name update
     * @methodOf data-prep.services.preparation.service:PreparationRestService
     * @param {string} preparationId The preparation id
     * @param {string} name The new preparation name
     * @description Update the current preparation name
     * @returns {promise} The PUT promise
     */
    function update(preparationId, name) {
        let updatedPreparationId;
        return PreparationRestService.update(preparationId, { name: name })
            .then((result) => updatedPreparationId = result)
            .then(refreshPreparations)
            .then((preparations) => _.find(preparations, { id: updatedPreparationId }));
    }

    /**
     * @ngdoc method
     * @name delete
     * @methodOf data-prep.services.preparation.service:PreparationListService
     * @param {object} preparation The preparation to delete
     * @description Delete a preparation from backend and from its internal list
     * @returns {promise} The DELETE promise
     */
    function deletePreparation(preparation) {
        return PreparationRestService.delete(preparation.id)
            .then(() => StateService.removePreparation(preparation));
    }
}