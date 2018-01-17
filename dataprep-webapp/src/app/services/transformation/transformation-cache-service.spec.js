/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Transformation cache service', () => {
    const transformationsMock = () => {
        return [
            {
                name: 'uppercase',
                category: 'case',
                items: null,
                parameters: null,
            },
            {
                name: 'rename',
                category: 'columns',
                items: null,
                parameters: null,
            },
            {
                name: 'lowercase',
                category: 'case',
                items: null,
                parameters: null,
            },
            {
                name: 'withParam',
                category: 'case',
                items: null,
                parameters: [
                    {
                        name: 'param',
                        type: 'string',
                        default: '.',
                        inputType: 'text',
                    },
                ],
            },
            {
                name: 'split',
                category: 'columns',
                parameters: null,
                items: [
                    {
                        name: 'mode',
                        values: [
                            {
                                name: 'noparam',
                            },
                            {
                                name: 'regex',
                                parameters: [
                                    {
                                        name: 'regexp',
                                        type: 'string',
                                        default: '.',
                                        inputType: 'text',
                                    },
                                ],
                            },
                            {
                                name: 'index',
                                parameters: [
                                    {
                                        name: 'index',
                                        type: 'integer',
                                        default: '5',
                                        inputType: 'number',
                                    },
                                ],
                            },
                            {
                                name: 'threeParams',
                                parameters: [
                                    {
                                        name: 'index',
                                        type: 'numeric',
                                        default: '5',
                                        inputType: 'number',
                                    },
                                    {
                                        name: 'index2',
                                        type: 'float',
                                        default: '5',
                                        inputType: 'number',
                                    },
                                    {
                                        name: 'index3',
                                        type: 'double',
                                        default: '5',
                                        inputType: 'number',
                                    },
                                ],
                            },
                        ],
                    },
                ],
            },
        ];
    };

    beforeEach(angular.mock.module('data-prep.services.transformation'));

    it('should put and get transformations in cache', inject((TransformationCacheService) => {
        // given
        const scope = 'column';
        const column = { id: '0002', name: 'Firstname' };
        const transformations = transformationsMock();

        let fromCache = TransformationCacheService.getTransformations(scope, column);
        expect(fromCache).toBeUndefined();
        
        // when
        TransformationCacheService.setTransformations(scope, column, transformations);
        fromCache = TransformationCacheService.getTransformations(scope, column);

        // then
        expect(fromCache).toBe(transformations);
    }));

    it('should put and get suggestions in cache', inject((TransformationCacheService) => {
        // given
        const scope = 'column';
        const column = { id: '0002', name: 'Firstname' };
        const suggestions = transformationsMock();

        let fromCache = TransformationCacheService.getSuggestions(scope, column);
        expect(fromCache).toBeUndefined();

        // when
        TransformationCacheService.setSuggestions(scope, column, suggestions);
        fromCache = TransformationCacheService.getSuggestions(scope, column);

        // then
        expect(fromCache).toBe(suggestions);
    }));

    it('should invalidate caches', inject((TransformationCacheService) => {
        // given
        const scope = 'column';
        const column = { id: '0002', name: 'Firstname' };
        const transformations = transformationsMock();
        const suggestions = transformationsMock();

        TransformationCacheService.setTransformations(scope, column, transformations);
        TransformationCacheService.setSuggestions(scope, column, suggestions);

        // when
        TransformationCacheService.invalidateCache();
        
        // then
        expect(TransformationCacheService.getTransformations(scope, column)).toBeUndefined();
        expect(TransformationCacheService.getSuggestions(scope, column)).toBeUndefined();
    }));
});
