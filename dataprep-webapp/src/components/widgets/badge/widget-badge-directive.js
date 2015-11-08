(function() {
    'use strict';

    /**
     * @ngdoc directive
     * @name talend.widget.directive:TalendBadge
     * @description This directive create a badge with editable content.<br/>
     * Key action :
     * <ul>
     *     <li>ENTER : validate the edition</li>
     *     <li>ESC : cancel the edition</li>
     * </ul>
     * Watchers :
     * <ul>
     *     <li>on obj change, the editable input size is recalculated</li>
     * </ul>
     * @restrict E
     * @usage
     <talend-badge
             on-close="closeCallback()"
             on-change="changeCallback()"
             text="text"
             obj="obj">
     </talend-badge>
     * @param {function} onClose The callback that is triggered on badge close
     * @param {function} onChange The callback that is triggered on content edit
     * @param {string} text The text to display before the editable part
     * @param {object} obj The object that contains the value {value: string}
     */
    function TalendBadge($timeout) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'components/widgets/badge/badge.html',
            scope: {
                onClose: '&',
                onChange: '&',
                text: '@',
                obj: '=',
                editable: '=',
                type :'='
            },
            bindToController: true,
            controllerAs: 'badgeCtrl',
            controller: 'BadgeCtrl',
            link: function(scope, iElement, iAttrs, ctrl) {
                /**
                 * Change input width, depending on its content
                 * @param input - the target input
                 */
                var adjustWidth = function(input) {
                    var width = ((ctrl.value.length + 1) * 7) + 'px';
                    input.css('width', width);
                };

                /**
                 * Attach keydown and blur events listeners
                 * @param input - the event target
                 */
                var attachListeners = function(input) {
                    //Keydown (ESC and ENTER) listeners
                    input.keydown(function(event) {
                        //ENTER : change filter
                        if(event.keyCode === 13) {
                            input.blur();
                        }

                        //ESC : reset value and stop ESC key propagation
                        if(event.keyCode === 27) {
                            event.stopPropagation();

                            ctrl.value = ctrl.obj.value;
                            input.val(ctrl.obj.value);
                            input.blur();
                        }

                        adjustWidth(input);
                    });
                };

                $timeout(function() {
                    var input = iElement.find('input.editable-input').eq(0);
                    adjustWidth(input);
                    attachListeners(input);

                    //Adjust input size on value change
                    if(ctrl.obj) {
                        scope.$watch(
                            function() {
                                return ctrl.obj.value;
                            },
                            function() {
                                adjustWidth(input);
                            });
                    }
                }, 0, false);
            }
        };
    }

    angular.module('talend.widget')
        .directive('talendBadge', TalendBadge);
})();