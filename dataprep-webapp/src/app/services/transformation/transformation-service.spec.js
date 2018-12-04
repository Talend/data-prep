function textClusteringParams() {
	return {
		type: 'cluster',
		details: {
			titles: [
				'We found these values',
				'And we\'ll keep this value',
			],
			clusters: [
				{
					parameters: [
						{
							name: 'Texa',
							type: 'boolean',
							description: 'parameter.Texa.desc',
							label: 'parameter.Texa.label',
							default: null,
						},
						{
							name: 'Tixass',
							type: 'boolean',
							description: 'parameter.Tixass.desc',
							label: 'parameter.Tixass.label',
							default: null,
						},
						{
							name: 'Tex@s',
							type: 'boolean',
							description: 'parameter.Tex@s.desc',
							label: 'parameter.Tex@s.label',
							default: null,
						},
					],
					replace: {
						name: 'replaceValue',
						type: 'string',
						description: 'parameter.replaceValue.desc',
						label: 'parameter.replaceValue.label',
						default: 'Texas',
					},
				},
				{
					parameters: [
						{
							name: 'Massachusetts',
							type: 'boolean',
							description: 'parameter.Massachusetts.desc',
							label: 'parameter.Massachusetts.label',
							default: null,
						},
						{
							name: 'Masachusetts',
							type: 'boolean',
							description: 'parameter.Masachusetts.desc',
							label: 'parameter.Masachusetts.label',
							default: null,
						},
						{
							name: 'Massachussetts',
							type: 'boolean',
							description: 'parameter.Massachussetts.desc',
							label: 'parameter.Massachussetts.label',
							default: null,
						},
						{
							name: 'Massachusets',
							type: 'boolean',
							description: 'parameter.Massachusets.desc',
							label: 'parameter.Massachusets.label',
							default: null,
						},
						{
							name: 'Masachussets',
							type: 'boolean',
							description: 'parameter.Masachussets.desc',
							label: 'parameter.Masachussets.label',
							default: null,
						},
					],
					replace: {
						name: 'replaceValue',
						type: 'string',
						description: 'parameter.replaceValue.desc',
						label: 'parameter.replaceValue.label',
						default: 'Massachussets',
					},
				},
				{
					parameters: [
						{
							name: 'Tato',
							type: 'boolean',
							description: 'parameter.Tato.desc',
							label: 'parameter.Tato.label',
							default: null,
						},
						{
							name: 'tata',
							type: 'boolean',
							description: 'parameter.tata.desc',
							label: 'parameter.tata.label',
							default: null,
						},
						{
							name: 't@t@',
							type: 'boolean',
							description: 'parameter.t@t@.desc',
							label: 'parameter.t@t@.label',
							default: null,
						},
					],
					replace: {
						name: 'replaceValue',
						type: 'string',
						description: 'parameter.replaceValue.desc',
						label: 'parameter.replaceValue.label',
						default: 'Tata',
					},
				},
			],
		},
	};
};

function generateSuggestions() {
	return [
		{
			name: 'uppercase',
			label: 'Change case to uppercase',
			category: 'case',
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
			],
			actionScope: [],
		},
		{
			name: 'negate',
			label: 'Negate boolean value',
			category: 'boolean',
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
			],
			actionScope: [],
		},
	];
}

function generateTransformations() {
	return [
		{
			name: 'uppercase',
			label: 'Change case to uppercase',
			labelHtml: 'Change case to uppercase',
			description: 'Change case to uppercase',
			category: 'case',
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
			],
			actionScope: [],
		},
		{
			name: 'lowercase',
			label: 'Change case to lowercase',
			labelHtml: 'Change case to lowercase',
			description: 'Change case to lowercase',
			category: 'case',
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
			],
			actionScope: [],
		},
		{
			name: 'cut',
			label: 'Remove part of the text',
			labelHtml: 'Remove part of the text',
			description: 'Remove part of the text',
			category: 'split',
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
				{ name: 'value', type: 'string' },
			],
			actionScope: [],
		},
		{
			name: 'split',
			label: 'Cut in parts',
			labelHtml: 'Cut in parts',
			description: 'Cut in parts',
			category: 'split',
			actionScope: [],
			parameters: [
				{ name: 'column_name', type: 'string', implicit: true },
				{ name: 'column_id', type: 'string', implicit: true },
				{
					name: 'mode',
					type: 'select',
					configuration: {
						values: [
							{ name: 'noparam', value: 'noparam' },
							{
								name: 'regex',
								value: 'regex',
								parameters: [
									{
										name: 'regexp',
										type: 'string',
										default: '.',
									},
								],
							},
							{
								name: 'index',
								value: 'index',
								parameters: [
									{
										name: 'index',
										type: 'integer',
										default: '5',
									},
								],
							},
							{
								name: 'threeParams',
								value: 'threeParams',
								parameters: [
									{
										name: 'index',
										type: 'numeric',
										default: '5',
									},
									{
										name: 'index2',
										type: 'float',
										default: '5',
									},
									{
										name: 'index3',
										type: 'double',
										default: '5',
									},
								],
							},
						],
					},
				},
			],
		},
	];
}

// transformations with categories containing sorted transformations
function generateCategories() {
	const transformations = generateTransformations();
	return [
		{
			category: 'case',
			categoryHtml: 'CASE',
			transformations: [
				transformations[1],
				transformations[0]
			]
		},
		{
			category: 'split',
			categoryHtml: 'SPLIT',
			transformations: [
				transformations[3],
				transformations[2]
			]
		}
	];
}

function generateCategoriesAndTransformations() {
	return {
		allTransformations: generateTransformations(),
		allCategories: generateCategories(),
	}
}

describe('Transformation Service', () => {
	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.transformation', ($provide) => {
		stateMock = {
			playground: {
				suggestions: {
					column: {
						allCategories: generateCategories(),
					},
				},
				grid: {
					selectedColumns: []
				}
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject((TransformationUtilsService) => {
		spyOn(TransformationUtilsService, 'adaptTransformations')
			.and
			.callFake((transformations) => transformations);
	}));

	describe('initDynamicParameters', () => {
		beforeEach(inject(($q, TransformationRestService) => {
			spyOn(TransformationRestService, 'getDynamicParameters')
				.and
				.returnValue($q.when(textClusteringParams()));
		}));

		it('should reset params before dynamic params fetch',
			inject((TransformationService) => {
				const transformation = {
					name: 'textclustering',
					category: 'quickfix',
					dynamic: true,
					parameters: [],
					cluster: {},
				};
				const infos = {
					datasetId: '78bae6345aef9965e22b54',
					preparationId: '721cd4455fb69e89543d4',
					stepId: '6b9312964fa564e864',
				};

				TransformationService.initDynamicParameters(transformation, infos);

				expect(transformation.parameters).toBe(null);
				expect(transformation.cluster).toBe(null);
			}));

		it('should fetch dynamic parameters and inject them into transformation',
			inject(($rootScope, TransformationService) => {
				const transformation = {
					name: 'textclustering',
					category: 'quickfix',
					dynamic: true,
					parameters: [],
					cluster: {},
				};
				const infos = {
					datasetId: '78bae6345aef9965e22b54',
					preparationId: '721cd4455fb69e89543d4',
					stepId: '6b9312964fa564e864',
				};

				TransformationService.initDynamicParameters(transformation, infos);
				$rootScope.$digest();

				expect(transformation.cluster).toEqual(textClusteringParams().details);
			}));
	});

	describe('getTransformations', () => {
		const transformationsFromRest = generateTransformations();
		const transformationsFromCache = generateCategoriesAndTransformations();
		beforeEach(inject(($q, TransformationRestService) => {
			spyOn(TransformationRestService, 'getTransformations')
				.and
				.returnValue($q.when(transformationsFromRest));
		}));

		it('should get transformations from cache',
			inject(($rootScope, TransformationService,
					TransformationCacheService, TransformationRestService) => {
				let result = null;
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue(transformationsFromCache);

				TransformationService.getTransformations(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(result).toBe(transformationsFromCache);
				expect(TransformationRestService.getTransformations)
					.not
					.toHaveBeenCalled();
			})
		);

		it('should fetch the transformations for the given scope and entity',
			inject(($rootScope, TransformationService,
					TransformationCacheService, TransformationRestService) => {
				let result = null;
				const expectedResult = generateCategoriesAndTransformations();
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue();

				TransformationService.getTransformations(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(TransformationRestService.getTransformations)
					.toHaveBeenCalledWith(scope, entity);
				expect(result).toEqual(expectedResult);
			})
		);

		it('should fetch the transformations for the multi_columns scope',
			inject(($rootScope, TransformationService,
					TransformationCacheService, TransformationRestService) => {
				let result = null;
				const scope = 'multi_columns';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue();
				stateMock.playground.grid.selectedColumns = ['0001', '0002'];

				TransformationService.getTransformations(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(TransformationRestService.getTransformations).toHaveBeenCalledWith('multi_columns', entity);
			})
		);

		it('should put the transformations in cache',
			inject(($rootScope, TransformationService, TransformationCacheService) => {
				let result = null;
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue();
				spyOn(TransformationCacheService, 'setTransformations')
					.and
					.returnValue();

				TransformationService.getTransformations(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(TransformationCacheService.setTransformations)
					.toHaveBeenCalledWith(scope, entity, result);
			})
		);
	});

	describe('getSuggestions', () => {
		const suggestionsFromRest = generateSuggestions();
		const suggestionsFromCache = generateSuggestions();
		beforeEach(inject(($q, TransformationRestService) => {
			spyOn(TransformationRestService, 'getSuggestions')
				.and
				.returnValue($q.when(suggestionsFromRest));
		}));

		it('should get suggestions from cache',
			inject(($rootScope, TransformationService,
					TransformationCacheService, TransformationRestService) => {
				let result = null;
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue(suggestionsFromCache);

				TransformationService.getSuggestions(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(result).toBe(suggestionsFromCache);
				expect(TransformationRestService.getSuggestions)
					.not
					.toHaveBeenCalled();
			}));

		it('should fetch the suggestions for the given scope and entity',
			inject(($rootScope, TransformationService,
					TransformationCacheService, TransformationRestService) => {
				let result = null;
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue();

				TransformationService.getSuggestions(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(TransformationRestService.getSuggestions)
					.toHaveBeenCalledWith(scope, entity);
				expect(result).toBe(suggestionsFromRest);
			})
		);

		it('should put the suggestions in cache',
			inject(($rootScope, TransformationService, TransformationCacheService) => {
				let result = null;
				const scope = 'column';
				const entity = { id: '0001' };
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue();
				spyOn(TransformationCacheService, 'setSuggestions')
					.and
					.returnValue();

				TransformationService.getSuggestions(scope, entity)
					.then((transformations) => {
						result = transformations;
					});
				$rootScope.$digest();

				expect(TransformationCacheService.setSuggestions)
					.toHaveBeenCalledWith(scope, entity, result);
			})
		);
	});

	describe('fetchSuggestionsAndTransformations', () => {
		it('should fetch suggestions and transformations for column scope',
			inject(($rootScope, TransformationService, TransformationCacheService) => {
				let result = null;
				const transformationsFromCache = generateCategoriesAndTransformations();
				const suggestionsFromCache = generateSuggestions();
				const scope = 'column';
				const entity = { id: '0001' };

				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue(transformationsFromCache);
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue(suggestionsFromCache);

				TransformationService.fetchSuggestionsAndTransformations(scope, entity)
					.then((response) => {
						result = response;
					});
				$rootScope.$digest();

				expect(result[0]).toBe(suggestionsFromCache);
				expect(result[1]).toBe(transformationsFromCache);
			})
		);

		it('should only fetch transformations for NON column scope',
			inject(($rootScope, TransformationService, TransformationCacheService) => {
				let result = null;
				const transformationsFromCache = generateCategoriesAndTransformations();
				const suggestionsFromCache = generateSuggestions();
				const scope = 'line';

				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue(transformationsFromCache);
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue(suggestionsFromCache);

				TransformationService.fetchSuggestionsAndTransformations(scope)
					.then((response) => {
						result = response;
					});
				$rootScope.$digest();

				expect(result[0]).toEqual([]);
				expect(result[1]).toBe(transformationsFromCache);
			})
		);
	});

	describe('initTransformations', () => {
		it('should fetch suggestions and transformations and set them in app state',
			inject(($rootScope, StateService,
					TransformationService, TransformationCacheService,
					TransformationUtilsService) => {
				const scope = 'column';
				const entity = { id: '0001' };
				const transformationsFromCache = generateCategoriesAndTransformations();
				const suggestionsFromCache = generateSuggestions();
				const adaptedCategories = {};

				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue(transformationsFromCache);
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue(suggestionsFromCache);
				spyOn(TransformationUtilsService, 'adaptCategories')
					.and
					.returnValue(adaptedCategories);
				spyOn(StateService, 'setTransformations').and.returnValue();

				TransformationService.initTransformations(scope, entity);
				$rootScope.$digest();

				const expectedTransformations = {
					allSuggestions: suggestionsFromCache,
					allTransformations: transformationsFromCache.allTransformations,
					filteredTransformations: adaptedCategories,
					allCategories: adaptedCategories,
					searchActionString: '',
				};
				expect(TransformationUtilsService.adaptCategories)
					.toHaveBeenCalledWith(suggestionsFromCache, transformationsFromCache.allCategories);
				expect(StateService.setTransformations)
					.toHaveBeenCalledWith(scope, expectedTransformations);
			})
		);

		it('should manage loading flag',
			inject(($rootScope, StateService, TransformationService,
					TransformationCacheService, TransformationUtilsService) => {
				const scope = 'column';
				const entity = { id: '0001' };
				const transformationsFromCache = generateCategoriesAndTransformations();
				const suggestionsFromCache = generateSuggestions();
				const adaptedCategories = {};

				spyOn(TransformationCacheService, 'getTransformations')
					.and
					.returnValue(transformationsFromCache);
				spyOn(TransformationCacheService, 'getSuggestions')
					.and
					.returnValue(suggestionsFromCache);
				spyOn(TransformationUtilsService, 'adaptCategories')
					.and
					.returnValue(adaptedCategories);
				spyOn(StateService, 'setTransformationsLoading').and.returnValue();

				TransformationService.initTransformations(scope, entity);
				expect(StateService.setTransformationsLoading).toHaveBeenCalledWith(true);
				$rootScope.$digest();

				expect(StateService.setTransformationsLoading).toHaveBeenCalledWith(false);
			}));
	});

	describe('filter', () => {
		it('should filter transformations/categories and update them in app state',
			inject((StateService, TransformationService) => {
				const scope = 'column';
				spyOn(StateService, 'updateFilteredTransformations').and.returnValue();

				TransformationService.filter(scope, 'upper');

				expect(StateService.updateFilteredTransformations)
					.toHaveBeenCalledWith(
						scope,
						[{
							category: 'case',
							categoryHtml: 'CASE',
							transformations: [
								{
									name: 'uppercase',
									label: 'Change case to uppercase',
									labelHtml: 'Change case to <span class="highlighted">upper</span>case',
									description: 'Change case to uppercase',
									category: 'case',
									parameters: [
										{
											name: 'column_name',
											type: 'string',
											implicit: true
										},
										{
											name: 'column_id',
											type: 'string',
											implicit: true
										}
									],
									actionScope: [],
								}
							],

						}]
					);
			})
		);

		it('should set back all categories in app state when search term is falsy',
			inject((StateService, TransformationService) => {
				const scope = 'column';
				spyOn(StateService, 'updateFilteredTransformations').and.returnValue();

				TransformationService.filter(scope, '');

				expect(StateService.updateFilteredTransformations)
					.toHaveBeenCalledWith(
						scope,
						stateMock.playground.suggestions.column.allCategories
					);
			})
		);
	});
});
