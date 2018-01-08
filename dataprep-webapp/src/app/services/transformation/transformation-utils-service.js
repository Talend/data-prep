/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import { chain, forEach, filter, map, find } from 'lodash';

const ACTION_SCOPE = 'hidden_in_action_list';
const CATEGORY = 'category';
const SUGGESTIONS_CATEGORY = 'suggestions';
const FILTERED_COLUMN = 'column_filtered';
const HIGHLIGHT_CLASS = 'highlighted';

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:TransformationUtilsService
 * @description Transformation utils service.
 * This service provide the entry point to manipulate transformations
 * @requires data-prep.services.utils.service:ConverterService
 */
export default class TransformationUtilsService {
	constructor(TextFormatService, ConverterService) {
		'ngInject';
		this.ConverterService = ConverterService;
		this.TextFormatService = TextFormatService;
	}

	// ---------------------------------------------------------------------------------------------
	// ---------------------------------------TRANSFORMATIONS---------------------------------------
	// ---------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name cleanParams
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @param {object[]} menus The menus to clean
	 * @description Remove 'column_id' and 'column_name' parameters (automatically sent),
	 * and clean empty arrays (choices and params)
	 */
	cleanParams(menus) {
		return forEach(menus, (menu) => {
			const filteredParameters = filter(menu.parameters, param => !param.implicit);
			menu.parameters = filteredParameters.length ? filteredParameters : null;
		});
	}


	/**
	 * @ngdoc method
	 * @name extractParams
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @description [PRIVATE] Inner function for recursively gather params
	 * @param {object} paramsAccu The parameters values accumulator
	 * @param {array} parameters The parameters array
	 * @param {string} paramsNamePrefix The parameter name prefix
	 * @returns {object} The parameters
	 */
	extractParams(paramsAccu, parameters, paramsNamePrefix) {
		if (!parameters) {
			return paramsAccu;
		}

		parameters.forEach((paramItem) => {
			paramsAccu[`${paramsNamePrefix || ''}${paramItem.name}`] =
				typeof (paramItem.value) !== 'undefined' ?
					paramItem.value :
					paramItem.default;

			// deal with select inline parameters
			if (paramItem.type === 'select') {
				const selectedValue = paramItem
					.configuration
					.values
					.find(({ value }) => value === paramItem.value);
				this.extractParams(paramsAccu, selectedValue.parameters, paramsNamePrefix);
			}
		});
		return paramsAccu;
	}

	/**
	 * @ngdoc method
	 * @name insertType
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @param {Array} transformation The transformation with parameters to adapt
	 * @description Insert adapted html input type in each parameter in the menu
	 */
	insertType(transformation) {
		if (!transformation.parameters) {
			return;
		}

		forEach(transformation.parameters, (param) => {
			param.inputType = this.ConverterService.toInputType(param.type);

			// also take care of select parameters...
			if (param.type === 'select' && param.configuration && param.configuration.values) {
				forEach(param.configuration.values, (selectItem) => {
					selectItem.inputType = this.ConverterService.toInputType(selectItem.type);
					// ...and its parameters
					if (selectItem.parameters) {
						this.insertType(selectItem);
					}
				});
			}
		});
	}

	/**
	 * @ngdoc method
	 * @name insertInputTypes
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @param {Array} transformations The transformations with parameters to adapt
	 * @description Insert parameter type to HTML input type in each transformations
	 */
	insertInputTypes(transformations) {
		forEach(transformations, transformation => this.insertType(transformation));
	}

	/**
	 * @ngdoc method
	 * @name setHtmlDisplayLabels
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @description Inject the UI label in each transformations
	 * @param {Array} transformations The list of transformations
	 */
	setHtmlDisplayLabels(transformations) {
		forEach(transformations, (transfo) => {
			transfo.labelHtml =
				transfo.label + (transfo.parameters || transfo.dynamic ? '...' : '');
		});
	}

	/**
	 * @ngdoc method
	 * @name adaptTransformations
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @description Adapt transformations in usable format :
	 * - clean and init parameters
	 * - insert input types for further form validation
	 * - init html labels
	 * @param {Array} transformations The list of transformations
	 */
	adaptTransformations(transformations) {
		const allTransformations = this.cleanParams(transformations);
		this.insertInputTypes(allTransformations);
		this.setHtmlDisplayLabels(allTransformations);
		return allTransformations;
	}

	/**
	 * @ngdoc method
	 * @name sortAndGroupByCategory
	 * @methodOf data-prep.services.transformation.service:TransformationUtilsService
	 * @description Sort and group transformations by category
	 * @return {Object} An object {category, categoryHtml, transformations} .
	 * "category" the category
	 * "categoryHtml" the adapted category for UI
	 * "transformations" the array of transformations for this category
	 */
	sortAndGroupByCategory(transformations) {
		const groupedTransformations = chain(transformations)
		// is not "column" category
			.filter(transfo => (!transfo.actionScope.includes(ACTION_SCOPE)))
			.sortBy(transfo => transfo.label.toLowerCase())
			.groupBy(CATEGORY)
			.value();
		return chain(Object.getOwnPropertyNames(groupedTransformations))
			.sortBy(key => key.toLowerCase())
			.map((key) => {
				return {
					category: key,
					categoryHtml: key.toUpperCase(),
					transformations: groupedTransformations[key],
				};
			})
			.value();
	}

	// ---------------------------------------------------------------------------------------------
	// ------------------------------------------CATEGORIES-----------------------------------------
	// ---------------------------------------------------------------------------------------------

	/**
	 * Create a "suggestions" category that contains
	 * - the transformations for filtered lines
	 * - the fetched suggestions
	 *
	 * "Suggestions" category is the first category.
	 *
	 * @param suggestions The suggestions actions
	 * @param categories All the categories
	 * @returns {Array} The array containing all the categories
	 */
	adaptCategories(suggestions, categories) {
		const {
			filterCategory,
			otherCategories,
		} = this.popFilteredCategory(categories);

		const filterTransformations = filterCategory ? filterCategory.transformations : [];
		const suggestionsCategory = {
			category: SUGGESTIONS_CATEGORY,
			categoryHtml: SUGGESTIONS_CATEGORY.toUpperCase(),
			transformations: filterTransformations.concat(suggestions),
		};

		return [suggestionsCategory].concat(otherCategories);
	}

	/**
	 * Extract "filtered" category.
	 * @param categories The categories
	 * @returns {{filterCategory: *, otherCategories: *}}
	 */
	popFilteredCategory(categories) {
		const filterCategory = find(categories, category => category.transformations.filter(transfo => (transfo.actionScope.includes(FILTERED_COLUMN))).length);
		const otherCategories = filter(categories, category => category.transformations.filter(transfo => (!transfo.actionScope.includes(FILTERED_COLUMN))).length);
		return { filterCategory, otherCategories };
	}

	// ---------------------------------------------------------------------------------------------
	// ------------------------------------------FILTER---------------------------------------------
	// ---------------------------------------------------------------------------------------------
	/**
	 * Create a closure that test if a transformation match the search term.
	 * It looks at the labelHtml and description fields
	 * @param search The search term
	 * @returns {function} The predicate closure
	 */
	transfosMatchSearch(search) {
		return (transfo) => {
			return transfo.labelHtml.toLowerCase().includes(search) ||
				transfo.description.toLowerCase().includes(search) ||
				(transfo.alternateLabel !== undefined && transfo.alternateLabel.toLowerCase().includes(search)) ||
				(transfo.alternateDescription !== undefined && transfo.alternateDescription.toLowerCase().includes(search));
		};
	}

	/**
	 * Create a closure that filter the transformations within a category
	 * @param search The search term
	 * @returns {function} the filter closure
	 */
	extractTransfosThatMatch(search) {
		return (categoryItem) => {
			const { category, transformations } = categoryItem;

			// category matches : display all this category transformations
			// category does NOT match : filter to only have matching displayed label or description
			const filteredTransformations = category.toLowerCase().includes(search) ?
				transformations :
				filter(transformations, this.transfosMatchSearch(search));

			return {
				category,
				categoryHtml: category.toUpperCase(),
				transformations: filteredTransformations,
			};
		};
	}

	/**
	 * Create a closure that highlight a search term in the labelHtml field of
	 * each transformation within a given category
	 * @param search The search term
	 * @returns {function} The mapping closure
	 */
	highlightDisplayedLabels(search) {
		return (category) => {
			const highlightedCategoryName = this.TextFormatService.highlightWords(
				category.categoryHtml,
				search,
				HIGHLIGHT_CLASS
			);

			const highlightedTransformations = map(category.transformations, (transfo) => {
				return {
					...transfo,
					labelHtml: this.TextFormatService.highlightWords(
						transfo.labelHtml,
						search,
						HIGHLIGHT_CLASS
					),
				};
			});

			return {
				...category,
				categoryHtml: highlightedCategoryName,
				transformations: highlightedTransformations,
			};
		};
	}
}
