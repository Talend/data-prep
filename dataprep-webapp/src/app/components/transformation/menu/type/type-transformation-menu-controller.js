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
 * @requires data-prep.services.column-types.service:ColumnTypesService
 * @requires data-prep.services.playground.service:PlaygroundService
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.services.utils.service:ConverterService
 */
export default class TypeTransformMenuCtrl {
	constructor($scope, state, PlaygroundService, ColumnTypesService, ConverterService) {
		'ngInject';

		this.state = state;
		this.$scope = $scope;
		this.PlaygroundService = PlaygroundService;
		this.ConverterService = ConverterService;
		this.ColumnTypesService = ColumnTypesService;
		this.types = [];
		this.state = state;
	}

	$onInit() {
		this.$scope.$watch(() => this.column, this.getTypesAndDomains.bind(this));
	}

	/**
	 * @ngdoc method
	 * @name getTypesAndDomains
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description get the domains then the types of the column
	 */
	getTypesAndDomains() {
		let inventoryType = '';
		let inventoryId = '';
		if (this.state.playground.preparation) {
			inventoryType = 'preparation';
			inventoryId = this.state.playground.preparation.id;
		}
		else {
			inventoryType = 'dataset';
			inventoryId = this.state.playground.dataset.id;
		}
		this.ColumnTypesService.getColSemanticDomains(inventoryType, inventoryId, this.column.id)
			.then(this._adaptDomains.bind(this))
			// should be after setting the currentDomain
			.then(this._loadPrimitiveTypes.bind(this));
	}

	/**
	 * @ngdoc method
	 * @name _adaptDomains
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Adapts the semantic domain list for ui.
	 */
	_adaptDomains(semanticDomains) {
		const domains = _.chain(semanticDomains)
			.filter('id')
			.sortBy('frequency')
			.reverse()
			.value();
		this.semanticDomains = domains;
		this._refreshCurrentDomain();
	}

	/**
	 * @ngdoc method
	 * @name _loadPrimitiveTypes
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description load the primitive types
	 */
	_loadPrimitiveTypes() {
		this.ColumnTypesService.getTypes()
			.then((types) => {
				const ignoredTypes = ['double', 'numeric', 'any'];
				this.types = _.filter(types, type => ignoredTypes.indexOf(type.id.toLowerCase()) === -1);
			});
	}

	/**
	 * @ngdoc method
	 * @name shouldBeChecked
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description return if the type must be checked. We compare the simplified types because (for now), 'double' and 'float' match the single 'decimal' type.
	 * @param {object} type the current type to be displayed in the ui menu list
	 */
	shouldBeChecked(type) {
		return this.ConverterService.simplifyType(type.id) === this.ConverterService.simplifyType(this.currentDomain);
	}

	/**
	 * @ngdoc method
	 * @name changeDomain
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Change the column domain. It change it on the ui, call the backend and revert the ui if the backend fails
	 * @param {object} domain The new domain information
	 */
	changeDomain(domain) {
		const originalDomain = this._getOriginalDomain();
		this.setColumnDomainAndType(domain, null);

		const parameters = {
			scope: 'column',
			column_id: this.column.id,
			column_name: this.column.name,
			new_domain_id: domain.id,
			new_domain_label: domain.label,
			new_domain_frequency: domain.frequency,
		};

		this.PlaygroundService.appendStep([{ action: 'domain_change', parameters }])
			.catch(this.setColumnDomainAndType.bind(this, originalDomain));
	}

	/**
	 * @ngdoc method
	 * @name changeType
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Change the column type. It change it on the ui, call the backend and revert the ui if the backend fails
	 * @param {object} type The new type information
	 */
	changeType(type) {
		const originalType = this.column.type;
		const originalDomain = this._getOriginalDomain();
		this.setColumnDomainAndType({ id: '', label: '', frequency: 0 }, type.id);

		const parameters = {
			scope: 'column',
			column_id: this.column.id,
			column_name: this.column.name,
			new_type: type.id,
		};
		this.PlaygroundService.appendStep([{ action: 'type_change', parameters }])
			.catch(this.setColumnDomainAndType.bind(this, originalDomain, originalType));
	}

	/**
	 * @ngdoc method
	 * @name _refreshCurrentDomain
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Refreshes current domain and simplified domain variables
	 */
	_refreshCurrentDomain() {
		this.currentDomain = this.column.domain ? this.column.domain : this.column.type.toUpperCase();
		this.currentSimplifiedDomain = this.column.domain ? this.column.domain : this.ConverterService.simplifyType(this.column.type);
	}

	/**
	 * @ngdoc method
	 * @name setColumnDomainAndType
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Update current column domain and type
	 * @param {object} domain The semantic domain
	 * @param {object} type The type
	 */
	setColumnDomainAndType(domain, type) {
		this.column.domain = domain.id;
		this.column.domainLabel = domain.label;
		this.column.domainFrequency = domain.frequency;
		if (type) {
			this.column.type = type;
		}
		this._refreshCurrentDomain();
	}

	/**
	 * @ngdoc method
	 * @name _getOriginalDomain
	 * @methodOf data-prep.transformation-menu.controller:TransformMenuCtrl
	 * @description Get the column original domain infos
	 * @return {object} the current column domain infos
	 */
	_getOriginalDomain() {
		return {
			id: this.column.domain,
			label: this.column.domainLabel,
			frequency: this.column.domainFrequency,
		};
	}
}
