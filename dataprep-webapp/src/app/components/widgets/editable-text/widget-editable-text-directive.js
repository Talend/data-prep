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
 * @name talend.widget.directive:TalendEditableText
 * @description This directive create a text that user can edit
 * @restrict E
 * @usage <talend-editable-text placeholder="placeholder"
 *                              text="text"
 *                              text-title="textTitle"
 *                              text-class="textClass"
 *                              edition-mode="editionMode"
 *                              on-text-click="onTextClick()"
 *                              on-validate="onValidate()"
 *                              on-cancel="onCancel()"
 *                              ></talend-editable-text>
 * @param {string} placeholder The placeholder to insert in the edition input
 * @param {string} text The text to display
 * @param {string} textTitle The text tooltip
 * @param {string} textClass The text div class
 * @param {boolean} editionMode The flag that switch between text and input
 * @param {function} onTextClick The action triggered by a text click
 * @param {function} onValidate The action triggered by an edition validation
 * @param {function} onCancel The action triggered by an edition cancelation
 */
export default function TalendEditableText($timeout) {
    'ngInject';

    return {
        restrict: 'E',
        templateUrl: 'app/components/widgets/editable-text/editable-text.html',
        scope: {
            placeholder: '@',
            text: '=',
            textTitle: '@',
            textClass: '@',
            editionMode: '=?',
            onTextClick: '&',
            onValidate: '&',
            onCancel: '&'
        },
        bindToController: true,
        controller: 'TalendEditableTextCtrl',
        controllerAs: 'editableTextCtrl',
        link: function (scope, iElement, iAttrs, ctrl) {
            $timeout(function () {
                var inputElement = iElement.find('.edition-text-input').eq(0);

                inputElement.keydown(function (e) {
                    if (e.keyCode === 27) {
                        e.stopPropagation();
                        ctrl.cancel();
                        scope.$digest();
                    }
                });

                iElement.find('.edit-btn').eq(0).click(function () {
                    inputElement.focus();
                    inputElement.select();
                });
            }, 0, false);

            scope.$watch(
                function () {
                    return ctrl.text;
                },
                function () {
                    ctrl.reset();
                }
            );
        }
    };
}