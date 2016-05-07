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
 * @name data-prep.type-transformation-menu.controller:TypeTransformMenuCtrl
 * @description Type Transformation menu controller.
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.dataset.service:ColumnTypesService
 * @requires data-prep.services.utils.service:ConverterService
 */
export default function TypeTransformMenuCtrl($scope, PlaygroundService, ColumnTypesService, ConverterService) {
    'ngInject';

    var vm = this;

    /**
     * @ngdoc property
     * @name types
     * @propertyOf data-prep.type-transformation-menu.controller:TypeTransformMenuCtrl
     * @description The supported primitive types
     * @type {array}
     */
    vm.types = [];

    ColumnTypesService.getTypes()
        .then(function (types) {
            var ignoredTypes = ['double', 'numeric', 'any'];
            vm.types = _.filter(types, function (type) {
                return ignoredTypes.indexOf(type.id.toLowerCase()) === -1;
            });

        });

    /**
     * @ngdoc method
     * @name shouldBeChecked
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description return if the type must be checked. We compare the simplified types because (for now), 'double' and 'float' match the single 'decimal' type.
     * @param {object} type the current type to be displayed in the ui menu list
     */
    vm.shouldBeChecked = function shouldBeChecked(type) {
        return ConverterService.simplifyType(type.id) === ConverterService.simplifyType(vm.currentDomain);
    };

    /**
     * @ngdoc method
     * @name changeDomain
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Change the column domain. It change it on the ui, call the backend and revert the ui if the backend fails
     * @param {object} domain The new domain information
     */
    vm.changeDomain = function changeDomain(domain) {
        var originalDomain = getOriginalDomain();
        setColumnDomainAndType(domain, null);

        var parameters = {
            scope: 'column',
            column_id: vm.column.id,
            column_name: vm.column.name,
            new_domain_id: domain.id,
            new_domain_label: domain.label,
            new_domain_frequency: domain.frequency
        };
        PlaygroundService.appendStep('domain_change', parameters)
            .catch(setColumnDomainAndType.bind(vm, originalDomain));
    };

    /**
     * @ngdoc method
     * @name changeType
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Change the column type. It change it on the ui, call the backend and revert the ui if the backend fails
     * @param {object} type The new type information
     */
    vm.changeType = function changeType(type) {
        var originalType = vm.column.type;
        var originalDomain = getOriginalDomain();
        setColumnDomainAndType({id: '', label: '', frequency: 0}, type.id);

        var parameters = {
            scope: 'column',
            column_id: vm.column.id,
            column_name: vm.column.name,
            new_type: type.id
        };
        PlaygroundService.appendStep('type_change', parameters)
            .catch(setColumnDomainAndType.bind(vm, originalDomain, originalType));
    };

    /**
     * @ngdoc method
     * @name adaptDomains
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Adapt the semantic domain list for ui.
     */
    vm.adaptDomains = function adaptDomains() {
        vm.domains = _.chain(vm.column.semanticDomains)
            .filter('id')
            .sortBy('frequency')
            .reverse()
            .value();
        refreshCurrentDomain();
    };

    /**
     * @ngdoc method
     * @name chooseDatePattern
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @param {string} value the date pattern to apply
     * @description Create an action to force a date pattern for a column
     */    
    vm.chooseDatePattern =  function chooseDatePattern(value){

        PlaygroundService.appendStep('force_date_pattern',
                                     {
                                         scope: 'dataset',
                                         column_id: vm.column.id,
                                         column_name: vm.column.name,
                                         new_pattern: value
                                     });
    };
    
    vm.displayChangeDatePattern = function displayChangeDatePattern(){
        return vm.column.type === 'string';  
    }; 

    /**
     * @ngdoc method
     * @name refreshCurrentDomain
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Refresh current domain and simplified domain variables
     */
    function refreshCurrentDomain() {
        vm.currentDomain = vm.column.domain ? vm.column.domain : vm.column.type.toUpperCase();
        vm.currentSimplifiedDomain = vm.column.domain ? vm.column.domain : ConverterService.simplifyType(vm.column.type);
    }

    /**
     * @ngdoc method
     * @name setColumnDomainAndType
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Update current column domain and type
     * @param {object} domain The semantic domain
     * @param {object} type The type
     */
    function setColumnDomainAndType(domain, type) {
        vm.column.domain = domain.id;
        vm.column.domainLabel = domain.label;
        vm.column.domainFrequency = domain.frequency;
        if (type) {
            vm.column.type = type;
        }

        refreshCurrentDomain();
    }

    /**
     * @ngdoc method
     * @name getOriginalDomain
     * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
     * @description Get the column original domain infos
     * @return {object} the current column domain infos
     */
    function getOriginalDomain() {
        return {
            id: vm.column.domain,
            label: vm.column.domainLabel,
            frequency: vm.column.domainFrequency
        };
    }

    $scope.$watch(
        () => vm.column,
        vm.adaptDomains
    );
}