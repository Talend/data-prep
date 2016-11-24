/**
 * Generate a set of transformations
 * Category "filtered"
 *      - keep_only : no params
 *      - delete_lines : no params
 * Category "numbers"
 *      - compare_numbers : select and simple params
 *      - round : simple param
 * Category "strings"
 *      - lowercase : no params
 *      - uppercase : no params
 * Category "column_metadata"
 *      - type_change : no param
 */
function generateTransformations() {
    return [
        {
            "category": "filtered",
            "name": "keep_only",
            "dynamic": false,
            "description": "Keep only the lines that match the current filters",
            "label": "Keep these Filtered Lines",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
        {
            "category": "filtered",
            "name": "delete_lines",
            "dynamic": false,
            "description": "Delete only the lines that match the current filters",
            "label": "Delete these Filtered Lines",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
        {
            "category": "Numbers",
            "name": "compare_numbers",
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                },
                {
                    "name": "compare_mode",
                    "type": "select",
                    "implicit": false,
                    "canBeBlank": false,
                    "placeHolder": "",
                    "configuration": {
                        "values": [
                            {
                                "value": "eq",
                                "label": "equals"
                            },
                            {
                                "value": "ne",
                                "label": "not equals"
                            },
                            {
                                "value": "gt",
                                "label": "greater than"
                            },
                            {
                                "value": "ge",
                                "label": "greater or equals than"
                            },
                            {
                                "value": "lt",
                                "label": "lower than"
                            },
                            {
                                "value": "le",
                                "label": "lower or equals than"
                            }
                        ],
                        "multiple": false
                    },
                    "radio": false,
                    "description": "Choose your compare mode in this list",
                    "label": "Compare mode",
                    "default": "eq"
                },
                {
                    "name": "mode",
                    "type": "select",
                    "implicit": false,
                    "canBeBlank": false,
                    "placeHolder": "",
                    "configuration": {
                        "values": [
                            {
                                "value": "constant_mode",
                                "label": "No other column",
                                "parameters": [
                                    {
                                        "name": "constant_value",
                                        "type": "string",
                                        "implicit": false,
                                        "canBeBlank": true,
                                        "placeHolder": "",
                                        "description": "Value to compare with",
                                        "label": "Constant",
                                        "default": "2"
                                    }
                                ]
                            },
                            {
                                "value": "other_column_mode",
                                "label": "Another column",
                                "parameters": [
                                    {
                                        "name": "selected_column",
                                        "type": "column",
                                        "implicit": false,
                                        "canBeBlank": false,
                                        "placeHolder": "",
                                        "description": "Combine the current column with this one",
                                        "label": "Column",
                                        "default": ""
                                    }
                                ]
                            }
                        ],
                        "multiple": false
                    },
                    "radio": false,
                    "description": "Select if you want to use a constant value or another column",
                    "label": "Use with",
                    "default": "constant_mode"
                }
            ],
            "dynamic": false,
            "description": "Compare this column to another column or a constant",
            "label": "Compare numbers",
            "docUrl": "",
            "actionScope": []
        },
        {
            "category": "strings",
            "name": "lowercase",
            "dynamic": false,
            "description": "Converts all of the cell text in this column to lower case",
            "label": "Change Style to lower Case",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
        {
            "category": "strings",
            "name": "uppercase",
            "dynamic": false,
            "description": "Converts all of the cell text in this column to UPPER case (capitalize)",
            "label": "Change Style to UPPER Case",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
        {
            "name": "round",
            "category": "Numbers",
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                },
                {
                    "name": "precision",
                    "type": "integer",
                    "implicit": false,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "Number of digit to add after decimal symbol",
                    "label": "Precision",
                    "default": "0"
                }
            ],
            "dynamic": false,
            "description": "Round value to the closest integer (3.14 -> 3)",
            "label": "Round Value using HalfUp mode",
            "docUrl": "",
            "actionScope": []
        },
        {
            "category": "column_metadata",
            "name": "type_change",
            "dynamic": false,
            "description": "Change type of this column (number, text, date, etc.)",
            "label": "Change Data Type",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        }
    ];
}

/**
 * Generate the transformations corresponding categories
 * Category "filtered"
 *      - keep_only : no params
 *      - delete_lines : no params
 * Category "numbers"
 *      - compare_numbers : select and simple params
 *      - round : simple param
 * Category "strings"
 *      - lowercase : no params
 *      - uppercase : no params
 */
function generateCategories() {
    const transformations = generateTransformations();
    return [
        {
            category: 'filtered',
            categoryHtml: 'FILTERED',
            transformations: [
                transformations[1],
                transformations[0],
            ],
        },
        {
            category: 'Numbers',
            categoryHtml: 'NUMBERS',
            transformations: [
                transformations[2],
                transformations[5],
            ],
        },
        {
            category: 'strings',
            categoryHtml: 'STRINGS',
            transformations: [
                transformations[3],
                transformations[4],
            ],
        },
    ];
}

/**
 * Generate a set of simple suggestions
 *      - lowercase
 *      - uppercase
 */
function generateSuggestions() {
    return [
        {
            "category": "strings",
            "name": "lowercase",
            "dynamic": false,
            "description": "Converts all of the cell text in this column to lower case",
            "label": "Change Style to lower Case",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
        {
            "category": "strings",
            "name": "uppercase",
            "dynamic": false,
            "description": "Converts all of the cell text in this column to UPPER case (capitalize)",
            "label": "Change Style to UPPER Case",
            "docUrl": "",
            "actionScope": [],
            "parameters": [
                {
                    "name": "column_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The column to which you want to apply this action",
                    "label": "Column",
                    "default": ""
                },
                {
                    "name": "row_id",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The row to which you want to apply this action",
                    "label": "Row",
                    "default": ""
                },
                {
                    "name": "scope",
                    "type": "string",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "The transformation scope (CELL | LINE | COLUMN | DATASET)",
                    "label": "Scope",
                    "default": ""
                },
                {
                    "name": "filter",
                    "type": "filter",
                    "implicit": true,
                    "canBeBlank": true,
                    "placeHolder": "",
                    "description": "An optional filter to apply action on matching values only.",
                    "label": "Filter",
                    "default": ""
                }
            ]
        },
    ];
}

describe('Transformation Utils Service', () => {
    beforeEach(angular.mock.module('data-prep.services.transformation'));

    describe('adaptTransformations', () => {
        it('should clean parameters',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                TransformationUtilsService.adaptTransformations(transformations);

                // then
                const implicitParams = ['column_id', 'row_id', 'scope', 'filter'];
                transformations.forEach((transformation) => {
                    if (transformation.parameters) {
                        transformation.parameters.forEach((param) => {
                            expect(implicitParams.indexOf(param.name)).toBe(-1);
                        });
                    }
                });
            })
        );

        it('should insert input types',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                TransformationUtilsService.adaptTransformations(transformations);

                // then : check simple parameters
                const round = transformations[5];
                expect(round.parameters[0].inputType).toBe('number');

                // then : check nested parameters in choice params
                const compare = transformations[2];
                expect(compare.parameters[1].inputType).toBe('text');
                expect(compare.parameters[1].configuration.values[0].parameters[0].inputType).toBe('text');
            })
        );

        it('should insert html labels',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                TransformationUtilsService.adaptTransformations(transformations);

                // then
                expect(transformations[0].labelHtml).toBe('Keep these Filtered Lines');
                expect(transformations[1].labelHtml).toBe('Delete these Filtered Lines');
                expect(transformations[2].labelHtml).toBe('Compare numbers...');
                expect(transformations[3].labelHtml).toBe('Change Style to lower Case');
                expect(transformations[4].labelHtml).toBe('Change Style to UPPER Case');
                expect(transformations[5].labelHtml).toBe('Round Value using HalfUp mode...');
            })
        );
    });

    describe('sortAndGroupByCategory', () => {
        it('should filter "column_metadata" category',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                const categories =
                    TransformationUtilsService.sortAndGroupByCategory(transformations);

                // then
                categories.forEach((item) => {
                    expect(item.category).not.toBe('column_metadata');
                });
            })
        );

        it('should group transformations by category, sorted by name',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                const categories =
                    TransformationUtilsService.sortAndGroupByCategory(transformations);

                // then
                expect(categories.length).toBe(3);
                expect(categories[0].category).toBe('filtered');
                expect(categories[1].category).toBe('Numbers');
                expect(categories[2].category).toBe('strings');
            })
        );

        it('should sort transformations within a category',
            inject((TransformationUtilsService) => {
                // given
                const transformations = generateTransformations();

                // when
                const categories =
                    TransformationUtilsService.sortAndGroupByCategory(transformations);

                // then
                expect(categories.length).toBe(3);
                expect(categories[0].transformations[0].name).toBe('delete_lines');
                expect(categories[0].transformations[1].name).toBe('keep_only');

                expect(categories[1].transformations[0].name).toBe('compare_numbers');
                expect(categories[1].transformations[1].name).toBe('round');

                expect(categories[2].transformations[0].name).toBe('lowercase');
                expect(categories[2].transformations[1].name).toBe('uppercase');
            })
        );
    });

    describe('adaptCategories', () => {
        it('should concat filtered, then suggestions, then other categories',
            inject((TransformationUtilsService) => {
                // given
                const suggestions = generateSuggestions();
                const categories = generateCategories();

                // when
                const adaptedCategories =
                    TransformationUtilsService.adaptCategories(suggestions, categories);

                // then :
                // - "suggestions" category before the others
                // - "filtered" category non present
                expect(adaptedCategories[0].category).toBe('suggestions');
                expect(adaptedCategories[1].category).toBe('Numbers');
                expect(adaptedCategories[2].category).toBe('strings');

                // then : filtered transformations are in suggestions as first actions
                expect(adaptedCategories[0].transformations[0].name).toBe('delete_lines');
                expect(adaptedCategories[0].transformations[1].name).toBe('keep_only');
                expect(adaptedCategories[0].transformations[2].name).toBe('lowercase');
                expect(adaptedCategories[0].transformations[3].name).toBe('uppercase');
            })
        );
    });

    describe('transfosMatchSearch', () => {
        it('should return true when transformation labelHtml matches',
            inject((TransformationUtilsService) => {
                // given
                const search = 'upper';
                const transformations = generateSuggestions();
                TransformationUtilsService.setHtmlDisplayLabels(transformations);
                const lowercase = transformations[0];
                const uppercase = transformations[1];

                // when
                const predicate = TransformationUtilsService.transfosMatchSearch(search);

                // then
                expect(predicate(lowercase)).toBe(false);
                expect(predicate(uppercase)).toBe(true);
            })
        );

        it('should return true when transformation description matches',
            inject((TransformationUtilsService) => {
                // given
                const search = '(capitalize)';
                const transformations = generateSuggestions();
                TransformationUtilsService.setHtmlDisplayLabels(transformations);
                const lowercase = transformations[0];
                const uppercase = transformations[1];

                // when
                const predicate = TransformationUtilsService.transfosMatchSearch(search);

                // then
                expect(predicate(lowercase)).toBe(false);
                expect(predicate(uppercase)).toBe(true);
            })
        );

        it('should return false',
            inject((TransformationUtilsService) => {
                // given
                const search = 'not_in_any_transformation';
                const transformations = generateSuggestions();
                TransformationUtilsService.setHtmlDisplayLabels(transformations);
                const lowercase = transformations[0];
                const uppercase = transformations[1];

                // when
                const predicate = TransformationUtilsService.transfosMatchSearch(search);

                // then
                expect(predicate(lowercase)).toBe(false);
                expect(predicate(uppercase)).toBe(false);
            })
        );
    });

    describe('extractTransfosThatMatch', () => {
        it('should filter transformations that match the search term',
            inject((TransformationUtilsService) => {
                // given
                const search = 'upper';
                const categories = generateCategories();
                const numbersCategory = categories[1];
                const stringsCategory = categories[2];
                TransformationUtilsService.setHtmlDisplayLabels(numbersCategory.transformations);
                TransformationUtilsService.setHtmlDisplayLabels(stringsCategory.transformations);

                // when
                const mapper = TransformationUtilsService.extractTransfosThatMatch(search);
                const mappedNumbersCategory = mapper(numbersCategory);
                const mappedStringsCategory = mapper(stringsCategory);

                // then
                expect(mappedNumbersCategory.transformations.length).toBe(0);
                expect(mappedStringsCategory.transformations.length).toBe(1);
                expect(mappedStringsCategory.transformations[0].name).toBe('uppercase');
            })
        );

        it('should return all transformations within a category that matches the search term',
            inject((TransformationUtilsService) => {
                // given
                const search = 'string';
                const categories = generateCategories();
                const stringsCategory = categories[2];
                TransformationUtilsService.setHtmlDisplayLabels(stringsCategory.transformations);

                // when
                const mapper = TransformationUtilsService.extractTransfosThatMatch(search);
                const mappedStringsCategory = mapper(stringsCategory);

                // then
                expect(mappedStringsCategory.transformations.length).toBe(2);
                expect(mappedStringsCategory.transformations[0].name).toBe('lowercase');
                expect(mappedStringsCategory.transformations[1].name).toBe('uppercase');
            })
        );
    });

    describe('highlightDisplayedLabels', () => {
        it('should highlight the search term in category names',
            inject((TransformationUtilsService) => {
                // given
                const search = 'string';
                const categories = generateCategories();
                const stringsCategory = categories[2];
                TransformationUtilsService.setHtmlDisplayLabels(stringsCategory.transformations);

                // when
                const mapper = TransformationUtilsService.highlightDisplayedLabels(search);
                const mappedStringsCategory = mapper(stringsCategory);

                // then
                expect(mappedStringsCategory.categoryHtml)
                    .toBe('<span class="highlighted">STRING</span>S');
            })
        );

        it('should highlight the search term in transformations name',
            inject((TransformationUtilsService) => {
                // given
                const search = 'upper';
                const categories = generateCategories();
                const stringsCategory = categories[2];
                TransformationUtilsService.setHtmlDisplayLabels(stringsCategory.transformations);

                // when
                const mapper = TransformationUtilsService.highlightDisplayedLabels(search);
                const mappedStringsCategory = mapper(stringsCategory);

                // then
                expect(mappedStringsCategory.transformations[0].labelHtml)
                    .toBe('Change Style to lower Case...');
                expect(mappedStringsCategory.transformations[1].labelHtml)
                    .toBe('Change Style to <span class="highlighted">UPPER</span> Case...');
            })
        );
    });
});
