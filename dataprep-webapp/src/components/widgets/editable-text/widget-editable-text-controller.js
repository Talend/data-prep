(function() {
    'use strict';

    /**
     * @ngdoc controller
     * @name talend.widgets.controller:TalendEditableTextCtrl
     * @description Editable Text controller
     */
    function TalendEditableTextCtrl() {
        var vm = this;
        vm.edit = edit;
        vm.reset = reset;
        vm.validate = validate;
        vm.cancel = cancel;

        /**
         * @ngdoc method
         * @name reset
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Set the edition text with the original value
         */
        function reset() {
            vm.editionText = vm.text;
        }

        /**
         * @ngdoc method
         * @name edit
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Reset the edition text and set edition mode flag
         */
        function edit() {
            reset();
            vm.editionMode = true;
        }

        /**
         * @ngdoc method
         * @name validate
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Execute the validation callback if the value has changed, and set the edition mode to false
         */
        function validate() {
            if(vm.editionText !== vm.text) {
                vm.onValidate({text: vm.editionText});
            }
            vm.editionMode = false;
        }

        /**
         * @ngdoc method
         * @name cancel
         * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl
         * @description Execute the cancel callback and set the edition mode to false
         */
        function cancel() {
            vm.onCancel({text: vm.editionText});
            vm.editionMode = false;
        }
    }

    angular.module('talend.widget')
        .controller('TalendEditableTextCtrl', TalendEditableTextCtrl);
})();