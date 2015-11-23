(function () {
    'use strict';

    /**
     * @ngdoc controller
     * @name data-prep.preparation-list.controller:PreparationListCtrl
     * @description Preparation list controller.
     * On creation, it fetch the user's preparations and load the requested one if a `prepid` is present as query param
     * @requires data-prep.services.preparation.service:PreparationService
     * @requires data-prep.services.playground.service:PlaygroundService
     * @requires data-prep.services.utils.service:MessageService
     * @requires data-prep.services.state.service:StateService
     * @requires talend.widget.service:TalendConfirmService
     */
    function PreparationListCtrl($rootScope, $stateParams, PlaygroundService, PreparationService, TalendConfirmService, MessageService, StateService) {
        var vm = this;
        vm.preparationService = PreparationService;

        /**
         * @ngdoc method
         * @name load
         * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
         * @param {object} preparation - the preparation to load
         * @description Load a preparation in the playground
         */
        vm.load = function (preparation) {
            PlaygroundService
                .load(preparation)
                .then(StateService.showPlayground);
        };

        /**
         * @ngdoc method
         * @name delete
         * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
         * @param {object} preparation - the preparation to delete
         * @description Delete a preparation
         */
        vm.delete = function (preparation) {
            TalendConfirmService.confirm({disableEnter: true}, ['DELETE_PERMANENTLY', 'NO_UNDONE_CONFIRM'], {
                    type: 'preparation',
                    name: preparation.name
                })
                .then(function () {
                    return PreparationService.delete(preparation);
                })
                .then(function () {
                    MessageService.success('REMOVE_SUCCESS_TITLE', 'REMOVE_SUCCESS', {
                        type: 'preparation',
                        name: preparation.name
                    });
                });
        };

        /**
         * @ngdoc method
         * @name rename
         * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
         * @param {object} preparation The preparation to rename
         * @param {string} newName The new name for the given preparation
         * @description Trigger backend call to update preparation name
         */
        vm.rename = function (preparation, newName) {
            var cleanName = newName ? newName.trim() : '';
            if (cleanName) {
                $rootScope.$emit('talend.loading.start');
                return PreparationService.setName(preparation.id, newName)
                    .then(function () {
                        MessageService.success('PREPARATION_RENAME_SUCCESS_TITLE', 'PREPARATION_RENAME_SUCCESS');
                    })
                    .finally(function () {
                        $rootScope.$emit('talend.loading.stop');
                    });
            }
        };

        /**
         * @ngdoc method
         * @name clone
         * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
         * @param {object} preparation - the preparation to clone
         * @description trigger backend call to clone preparation
         */
        vm.clone = function (preparation) {
            $rootScope.$emit('talend.loading.start');
            return PreparationService.clone(preparation.id)
                .then(function () {
                    MessageService.success('PREPARATION_CLONING_SUCCESS_TITLE', 'PREPARATION_CLONING_SUCCESS');
                })
                .finally(function () {
                    $rootScope.$emit('talend.loading.stop');
                });
        };

        /**
         * @ngdoc method
         * @name loadUrlSelectedPreparation
         * @methodOf data-prep.preparation-list.controller:PreparationListCtrl
         * @param {object} preparations - list of all user's preparation
         * @description [PRIVATE] Load playground with provided preparation id, if present in route param
         */
        var loadUrlSelectedPreparation = function (preparations) {
            if ($stateParams.prepid) {
                var selectedPrep = _.find(preparations, function (preparation) {
                    return preparation.id === $stateParams.prepid;
                });

                if (selectedPrep) {
                    vm.load(selectedPrep);
                }
                else {
                    MessageService.error('PLAYGROUND_FILE_NOT_FOUND_TITLE', 'PLAYGROUND_FILE_NOT_FOUND', {type: 'preparation'});
                }
            }
        };

        /**
         * Load preparations list if needed, and load playground if route has preparation id
         */
        PreparationService.getPreparations()
            .then(loadUrlSelectedPreparation);
    }

    /**
     * @ngdoc property
     * @name preparations
     * @propertyOf data-prep.preparation-list.controller:PreparationListCtrl
     * @description The preparations list.
     * It is bound to {@link data-prep.services.preparation.service:PreparationService PreparationService}.preparationsList()
     */
    Object.defineProperty(PreparationListCtrl.prototype,
        'preparations', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.preparationService.preparationsList();
            }
        });

    angular.module('data-prep.preparation-list')
        .controller('PreparationListCtrl', PreparationListCtrl);
})();