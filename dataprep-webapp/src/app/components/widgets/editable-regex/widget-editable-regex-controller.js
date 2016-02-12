/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name talend.widget.controller:EditableRegexCtrl
 * @description Editable regex controller. It manage the entered value adaptation to match the wanted regex type
 */
export default function TalendEditableRegexCtrl($translate) {
    'ngInject';

    var vm = this;

    var equals = {
        key: '=',
        label: $translate.instant('EQUALS'),
        operator: 'equals'
    };

    var contains = {
        key: '≅',
        label: $translate.instant('CONTAINS'),
        operator: 'contains'
    };

    var startsWith = {
        key: '>',
        label: $translate.instant('STARTS_WITH'),
        operator: 'starts_with'
    };

    var endsWith = {
        key: '<',
        label: $translate.instant('ENDS_WITH'),
        operator: 'ends_with'
    };

    var regex = {
        key: '^\\',
        label: $translate.instant('REGEX'),
        operator: 'regex'
    };

    //TODO should be removed as backend must initialize it
    vm.value = vm.value ? vm.value : {
        token: '',
        operator: 'contains'
    };

    /**
     * @ngdoc property
     * @name types
     * @propertyOf talend.widget.controller:EditableRegexCtrl
     * @description The array of regex types
     */
    vm.types = [equals, contains, startsWith, endsWith, regex];

    /**
     * @ngdoc method
     * @name setSelectedType
     * @methodOf talend.widget.controller:EditableRegexCtrl
     * @description Change selected type and trigger model update
     */
    vm.setSelectedType = function setSelectedType(type) {
        vm.value.operator = type.operator;
    };

    /**
     * @ngdoc method
     * @name getTypeKey
     * @methodOf talend.widget.controller:EditableRegexCtrl
     * @description Change selected type and trigger model update
     * @return {String} the type key
     */
    vm.getTypeKey = function getTypeKey() {
        var currentType = _.find(vm.types, {operator: vm.value.operator});
        return currentType.key;
    };
}