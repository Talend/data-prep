/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation cache service', function() {
    'use strict';

    var transformationsMock = function() {
        return [
            {
                'name':'uppercase',
                'category':'case',
                'items':null,
                'parameters':null
            },
            {
                'name':'rename',
                'category':'columns',
                'items':null,
                'parameters':null
            },
            {
                'name':'lowercase',
                'category':'case',
                'items':null,
                'parameters':null
            },
            {
                'name':'withParam',
                'category':'case',
                'items':null,
                'parameters':[
                    {
                        'name':'param',
                        'type':'string',
                        'default':'.',
                        'inputType':'text'
                    }
                ]
            },
            {
                'name':'split',
                'category':'columns',
                'parameters':null,
                'items':[
                    {
                        'name':'mode',
                        'values':[
                            {
                                'name':'noparam'
                            },
                            {
                                'name':'regex',
                                'parameters':[
                                    {
                                        'name':'regexp',
                                        'type':'string',
                                        'default':'.',
                                        'inputType':'text'
                                    }
                                ]
                            },
                            {
                                'name':'index',
                                'parameters':[
                                    {
                                        'name':'index',
                                        'type':'integer',
                                        'default':'5',
                                        'inputType':'number'
                                    }
                                ]
                            },
                            {
                                'name':'threeParams',
                                'parameters':[
                                    {
                                        'name':'index',
                                        'type':'numeric',
                                        'default':'5',
                                        'inputType':'number'
                                    },
                                    {
                                        'name':'index2',
                                        'type':'float',
                                        'default':'5',
                                        'inputType':'number'
                                    },
                                    {
                                        'name':'index3',
                                        'type':'double',
                                        'default':'5',
                                        'inputType':'number'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
    };
    var column = {id: '0002', name: 'Firstname'};

    beforeEach(angular.mock.module('data-prep.services.transformation'));

    describe('transformations', function() {
        beforeEach(inject(function($q, TransformationService) {
            spyOn(TransformationService, 'getColumnTransformations').and.returnValue($q.when(transformationsMock()));
            spyOn(TransformationService, 'getLineTransformations').and.returnValue($q.when(transformationsMock()));
        }));

        describe('column', function() {
            it('should call TransformationService when column is not in cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                var result = null;

                //when
                TransformationCacheService.getColumnTransformations(column)
                    .then(function(transformations) {
                        result = transformations;
                    });
                $rootScope.$digest();

                //then
                expect(TransformationService.getColumnTransformations).toHaveBeenCalledWith(column);
                expect(result).toEqual(transformationsMock());
            }));

            it('should return the same result from cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                var oldResult = null;
                var newResult = null;
                TransformationCacheService.getColumnTransformations(column)
                    .then(function(transformations) {
                        oldResult = transformations;
                    });

                expect(TransformationService.getColumnTransformations.calls.count()).toBe(1);

                //when
                TransformationCacheService.getColumnTransformations(column)
                    .then(function(transformations) {
                        newResult = transformations;
                    });
                $rootScope.$digest();

                //then
                expect(newResult).toBe(oldResult);
                expect(TransformationService.getColumnTransformations.calls.count()).toBe(1);
            }));

            it('should remove all cache entries', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                TransformationCacheService.getColumnTransformations(column);
                $rootScope.$digest();

                expect(TransformationService.getColumnTransformations.calls.count()).toBe(1);

                //when
                TransformationCacheService.invalidateCache();

                TransformationCacheService.getColumnTransformations(column);
                $rootScope.$digest();

                //then
                expect(TransformationService.getColumnTransformations.calls.count()).toBe(2);
            }));
        });

        describe('line', function() {
            it('should call TransformationService when line is not in cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                var result = null;

                //when
                TransformationCacheService.getLineTransformations()
                    .then(function(transformations) {
                        result = transformations;
                    });
                $rootScope.$digest();

                //then
                expect(TransformationService.getLineTransformations).toHaveBeenCalledWith();
                expect(result).toEqual(transformationsMock());
            }));

            it('should return the same result from cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                var oldResult = null;
                var newResult = null;
                TransformationCacheService.getLineTransformations()
                    .then(function(transformations) {
                        oldResult = transformations;
                    });

                expect(TransformationService.getLineTransformations.calls.count()).toBe(1);

                //when
                TransformationCacheService.getLineTransformations()
                    .then(function(transformations) {
                        newResult = transformations;
                    });
                $rootScope.$digest();

                //then
                expect(newResult).toBe(oldResult);
                expect(TransformationService.getLineTransformations.calls.count()).toBe(1);
            }));

            it('should remove all cache entries', inject(function($rootScope, TransformationCacheService, TransformationService) {
                //given
                TransformationCacheService.getLineTransformations(column);
                $rootScope.$digest();

                expect(TransformationService.getLineTransformations.calls.count()).toBe(1);

                //when
                TransformationCacheService.invalidateCache();

                TransformationCacheService.getLineTransformations(column);
                $rootScope.$digest();

                //then
                expect(TransformationService.getLineTransformations.calls.count()).toBe(2);
            }));
        });
    });

    describe('suggestions', function() {
        beforeEach(inject(function($q, TransformationService) {
            spyOn(TransformationService, 'getColumnSuggestions').and.returnValue($q.when(transformationsMock()));
        }));

        it('should call TransformationService when column is not in cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
            //given
            var result = null;

            //when
            TransformationCacheService.getColumnSuggestions(column)
                .then(function(transformations) {
                    result = transformations;
                });
            $rootScope.$digest();

            //then
            expect(TransformationService.getColumnSuggestions).toHaveBeenCalledWith(column);
            expect(result).toEqual(transformationsMock());
        }));

        it('should return the same result from cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
            //given
            var oldResult = null;
            var newResult = null;
            TransformationCacheService.getColumnSuggestions(column)
                .then(function(transformations) {
                    oldResult = transformations;
                });

            expect(TransformationService.getColumnSuggestions.calls.count()).toBe(1);

            //when
            TransformationCacheService.getColumnSuggestions(column)
                .then(function(transformations) {
                    newResult = transformations;
                });
            $rootScope.$digest();

            //then
            expect(newResult).toBe(oldResult);
            expect(TransformationService.getColumnSuggestions.calls.count()).toBe(1);
        }));

        it('should remove all cache entries', inject(function($rootScope, TransformationCacheService, TransformationService) {
            //given
            TransformationCacheService.getColumnSuggestions(column);
            $rootScope.$digest();

            expect(TransformationService.getColumnSuggestions.calls.count()).toBe(1);

            //when
            TransformationCacheService.invalidateCache();

            TransformationCacheService.getColumnSuggestions(column);
            $rootScope.$digest();

            //then
            expect(TransformationService.getColumnSuggestions.calls.count()).toBe(2);
        }));
    });
});