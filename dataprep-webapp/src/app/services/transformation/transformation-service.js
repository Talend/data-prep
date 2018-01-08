/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { chain } from 'lodash';

const COLUMN = 'column';

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationService
 * @description Transformation service.
 * This service provide the entry point to get and manipulate transformations
 * @requires data-prep.services.parameters.service:ParametersService
 * @requires data-prep.services.transformation.service:TransformationCacheService
 * @requires data-prep.services.transformation.service:TransformationUtilsService
 * @requires data-prep.services.transformation.service:TransformationRestService
 */
export default class TransformationService {

	constructor($q, state, StateService,
				ParametersService, TransformationCacheService,
				TransformationUtilsService, TransformationRestService) {
		'ngInject';
		this.$q = $q;
		this.state = state;
		this.StateService = StateService;
		this.ParametersService = ParametersService;
		this.TransformationCacheService = TransformationCacheService;
		this.TransformationUtilsService = TransformationUtilsService;
		this.TransformationRestService = TransformationRestService;
	}

	/**
	 * @ngdoc method
	 * @name getTransformations
	 * @methodOf data-prep.services.transformation.service:TransformationService
	 * @description Get transformations from REST call, clean and adapt them
	 * @param {string} scope The transformations scope
	 * @param {object} entity The transformations target entity
	 * @return {Object} An object {allTransformations, allCategories} .
	 * "allTransformations" is the array of all transformations (cleaned and adapted for UI)
	 * "allCategories" is the array of all transformations grouped by category
	 */
	getTransformations(scope, entity) {
		const fromCache = this.TransformationCacheService.getTransformations(scope, entity);
		if (fromCache) {
			return this.$q.when(fromCache);
		}

		return this.TransformationRestService.getTransformations(scope, entity)
			.then((response) => {
				const allTransformations = this.TransformationUtilsService.adaptTransformations(response);
				const allCategories = this.TransformationUtilsService.sortAndGroupByCategory(allTransformations);
				return {
					allTransformations,
					allCategories,
				};
			})
			.then((transformations) => {
				this.TransformationCacheService.setTransformations(scope, entity, transformations);
				return transformations;
			});
	}

	/**
	 * @ngdoc method
	 * @name getSuggestions
	 * @methodOf data-prep.services.transformation.service:TransformationService
	 * @param {string} scope The transformations scope
	 * @param {object} entity The transformations target entity
	 * @description Get suggestions from REST call, clean and adapt them
	 * @returns {Array} All the suggestions, cleaned and adapted for UI
	 */
	getSuggestions(scope, entity) {
		const fromCache = this.TransformationCacheService.getSuggestions(scope, entity);
		if (fromCache) {
			return this.$q.when(fromCache);
		}

		return this.TransformationRestService.getSuggestions(scope, entity)
			.then(response => this.TransformationUtilsService.adaptTransformations(response))
			.then((suggestions) => {
				this.TransformationCacheService.setSuggestions(scope, entity, suggestions);
				return suggestions;
			});
	}

	/**
	 * @ngdoc method
	 * @name initDynamicParameters
	 * @methodOf data-prep.services.transformation.service:TransformationService
	 * @description Fetch the dynamic parameter and set them in transformation
	 */
	initDynamicParameters(transformation, infos) {
		this.ParametersService.resetParameters(transformation);

		const action = transformation.name;
		return this.TransformationRestService
			.getDynamicParameters(
				action,
				infos.columnId,
				infos.datasetId,
				infos.preparationId,
				infos.stepId
			)
			.then((parameters) => {
				transformation[parameters.type] = parameters.details;
				return transformation;
			});
	}

	/**
	 * Fetch the suggestions and transformations
	 * @param scope The transformation scope
	 * @param entity The target entity
	 * @returns {Promise} The fetch promise
	 */
	fetchSuggestionsAndTransformations(scope, entity) {
		const fetchSuggestions = scope === COLUMN ?
			this.getSuggestions(scope, entity) :
			this.$q.when([]);
		const fetchTransformations = this.getTransformations(scope, entity);

		return this.$q.all([fetchSuggestions, fetchTransformations]);
	}

	/**
	 * Init transformations and suggestions on the scope/entity (ex: column)
	 *
	 * @param scope The transformation scope
	 * @param entity The target entity
	 * @returns {Promise} The process promise
	 */
	initTransformations(scope, entity) {
		if (this.state.playground.isReadOnly) {
			return this.$q.when([]);
		}
		this.StateService.setTransformationsLoading(true);

		return this.fetchSuggestionsAndTransformations(scope, entity)
			.then(([allSuggestions, { allCategories, allTransformations }]) => {
				const adaptedCategories = this.TransformationUtilsService.adaptCategories(allSuggestions, allCategories);
				const actions = {
					allSuggestions,
					allTransformations,
					filteredTransformations: adaptedCategories,
					allCategories: adaptedCategories,
					searchActionString: '',
				};

				this.StateService.setTransformations(scope, actions);
			})
			.finally(() => {
				this.StateService.setTransformationsLoading(false);
			});
	}

	/**
	 * Filter the transformations on the provided scope
	 * @param scope The transformations scope
	 * @param search The search term
	 */
	filter(scope, search) {
		const searchValue = search.toLowerCase();
		const actionsPayload = this.state.playground.suggestions[scope];

		const filteredCategories = search ?
			chain(actionsPayload.allCategories)
				.map(this.TransformationUtilsService.extractTransfosThatMatch(searchValue))
				.filter(category => category.transformations.length)
				.map(this.TransformationUtilsService.highlightDisplayedLabels(searchValue))
				.value() :
			actionsPayload.allCategories;

		this.StateService.updateFilteredTransformations(scope, filteredCategories);
	}
}
