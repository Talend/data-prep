/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendButtonSwitch
 * @description This directive create a badge with editable content switch on click.
 * @restrict E
 * @usage
 *
 <talend-button-switch
 button-values="valuesArray"
 button-current-text="texts"
 button-action="clickAction">
 </talend-button-switch>

 * @param {string[]}  button-values The possible values
 * @param {string} button-current-text The current value
 * @param {function} button-action The action that is triggered on switch
 */
export default function TalendButtonSwitch() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/widgets/button-switch/button-switch.html',
        scope: {
            currentValue: '=',
            displayKey: '@',
            values: '=',
            changeAction: '&'
        },
        bindToController: true,
        controller: function () {
        },
        controllerAs: 'buttonSwitchCtrl',
        link: function (scope, iElement, attrs, ctrl) {
            function next() {
                var index = ctrl.values.indexOf(ctrl.currentValue);
                return (index === -1 || index >= ctrl.values.length - 1) ?
                    ctrl.values[0] :
                    ctrl.values[index + 1];
            }

            iElement.on('click', function () {
                ctrl.changeAction({selected: next()});
            });
        }
    };
}