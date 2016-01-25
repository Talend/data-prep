(function() {
    'use strict';

    var feedbackState = {
        visible: false
    };

    /**
     * @ngdoc service
     * @name data-prep.services.state.service:FeedbackStateService
     * @description Manage the state of the feedback
     */
    function FeedbackStateService() {

        return {
            show: show,
            hide: hide
        };

        /**
         * @ngdoc method
         * @name show
         * @methodOf data-prep.services.state.service:FeedbackStateService
         * @description Display the feedback
         */
        function show() {
            feedbackState.visible = true;
        }

        /**
         * @ngdoc method
         * @name hide
         * @methodOf data-prep.services.state.service:FeedbackStateService
         * @description Hide the feedback
         */
        function hide () {
            feedbackState.visible = false;
        }
    }

    angular.module('data-prep.services.state')
        .service('FeedbackStateService', FeedbackStateService)
        .constant('feedbackState', feedbackState);
})();