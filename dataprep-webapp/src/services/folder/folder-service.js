(function () {
    'use strict';

    /**
     * @ngdoc service
     * @name data-prep.services.folder.service:FolderService
     * @description Folder service. This service provide the entry point to the Folder service.
     * @requires data-prep.services.state.constant:state
     * @requires data-prep.services.state.service:StateService
     * @requires data-prep.services.folder.service:FolderRestService
     * @requires data-prep.services.dataset.service:DatasetListSortService
     * @requires data-prep.services.preparation.service:PreparationListService
     */
    function FolderService($translate, state, StateService, FolderRestService, DatasetListSortService, PreparationListService) {

        var ROOT_FOLDER = {
            id: '/',
            path: '/',
            name: '/'
        };
        $translate('HOME_FOLDER').then(function(homeName) {
            ROOT_FOLDER.name = homeName;
        });

        return {
            // folder operations
            children: FolderRestService.children,
            create: FolderRestService.create,
            rename: FolderRestService.rename,
            remove: FolderRestService.remove,
            search: FolderRestService.search,
            getContent: getContent,
            refreshDefaultPreparation: refreshDefaultPreparation,

            // shared folder ui mngt
            populateMenuChildren: populateMenuChildren
        };

        /**
         * @ngdoc method
         * @name buildStackFromId
         * @methodOf data-prep.services.folder.service:FolderService
         * @description Build the folder stack from the the given id
         * @param {string} folderId The folder id
         * @returns {Array} the folder stack
         */
        function buildStackFromId(folderId) {
            var foldersStack = [];
            foldersStack.push(ROOT_FOLDER);

            if (folderId) {
                var paths = folderId.split('/');
                for (var i = 1; i <= paths.length + 1; i++) {
                    if (paths[i - 1]) {
                        if (i > 1) {
                            foldersStack.push({
                                id: foldersStack[i - 1].id + '/' + paths[i - 1],
                                path: foldersStack[i - 1].id + '/' + paths[i - 1],
                                name: paths[i - 1]
                            });
                        } else {
                            foldersStack.push({id: paths[i - 1], path: paths[i - 1], name: paths[i - 1]});
                        }
                    }
                }
            }

            return foldersStack;
        }

        /**
         * @ngdoc method
         * @name populateMenuChildren
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description Init the state with the folder's children
         * @param {object} folder The folder definition
         */
        function populateMenuChildren(folder) {
            return FolderRestService.getContent(folder && folder.id)
                .then(function (content) {
                    StateService.setMenuChildren(content.data.folders);
                });
        }

        /**
         * @ngdoc method
         * @name refreshDefaultPreparation
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @description Inject the default preparation in the current folder datasets
         * @param {object} preparations The whole list of preparations
         */
        function refreshDefaultPreparation(preparations) {
            // group preparation per dataset
            var datasetPreps = _.groupBy(preparations, function (preparation) {
                return preparation.dataSetId;
            });

            // reset default preparation for all datasets
            _.forEach(state.folder.currentFolderContent.datasets, function (dataset) {
                var preparations = datasetPreps[dataset.id];
                dataset.defaultPreparation = preparations && preparations.length === 1 ? preparations[0] : null;
            });

            return preparations;
        }

        /**
         * @ngdoc method
         * @name getContent
         * @methodOf data-prep.folder.controller:FolderCtrl
         * @param {object} folder The folder to list
         * @returns {Promise} The GET promise
         */
        function getContent(folder) {
            var sort = DatasetListSortService.getSort();
            var order = DatasetListSortService.getOrder();
            var promise = FolderRestService.getContent(folder && folder.id, sort, order);

            promise.then(function (result) {
                    var content = result.data;
                    var foldersStack = buildStackFromId(folder ? folder.id : '');
                    var currentFolder = folder ? folder : ROOT_FOLDER;

                    StateService.setCurrentFolder(currentFolder);
                    StateService.setCurrentFolderContent(content);
                    StateService.setFoldersStack(foldersStack);
                })
                .then(PreparationListService.getPreparationsPromise)
                .then(refreshDefaultPreparation);
            return promise;
        }
    }

    angular.module('data-prep.services.folder')
        .service('FolderService', FolderService);
})();