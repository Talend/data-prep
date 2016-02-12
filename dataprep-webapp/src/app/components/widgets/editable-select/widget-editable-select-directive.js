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
 * @name talend.widget.directive:EditableSelect
 * @description This directive create an editable combobox
 * @restrict E
 * @usage
 <editable-select
     list="selectValues"
     ng-model="value"></editable-select>
 * @param {Array} list The list of selectable values in the combobox
 * @param {object} ngModel The model to bind
 */
export default function EditableSelect() {
    return {
        restrict: 'E',
        templateUrl: 'app/components/widgets/editable-select/editable-select.html',
        scope: {
            list: '=',
            value: '=ngModel'
        },
        bindToController: true,
        controller: () => {},
        controllerAs: 'editableSelectCtrl'
    };
}