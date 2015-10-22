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

    beforeEach(module('data-prep.services.transformation'));

    beforeEach(inject(function($q, TransformationService) {
        spyOn(TransformationService, 'getTransformations').and.returnValue($q.when(transformationsMock()));
    }));

    it('should call TransformationService when column is not in cache and showAll is true', inject(function($rootScope, TransformationCacheService, TransformationService) {
        //given
        var result = null;
        var stringifiedColumn = JSON.stringify(column);

        //when
        TransformationCacheService.getTransformations(column, true)
            .then(function(transformations) {
                result = transformations;
            });
        $rootScope.$digest();

        //then
        expect(TransformationService.getTransformations).toHaveBeenCalledWith(stringifiedColumn, true);
        expect(result).toEqual(transformationsMock());
    }));


    it('should call TransformationService when column is not in cache and showAll is false', inject(function($rootScope, TransformationCacheService, TransformationService) {
        //given
        var result = null;
        var stringifiedColumn = JSON.stringify(column);

        //when
        TransformationCacheService.getTransformations(column, false)
            .then(function(transformations) {
                result = transformations;
            });
        $rootScope.$digest();

        //then
        expect(TransformationService.getTransformations).toHaveBeenCalledWith(stringifiedColumn, false);
        expect(result).toEqual(transformationsMock());
    }));

    it('should return the same result from cache', inject(function($rootScope, TransformationCacheService, TransformationService) {
        //given
        var oldResult = null;
        var newResult = null;
        TransformationCacheService.getTransformations(column)
            .then(function(transformations) {
                oldResult = transformations;
            });

        expect(TransformationService.getTransformations.calls.count()).toBe(1);

        //when
        TransformationCacheService.getTransformations(column)
            .then(function(transformations) {
                newResult = transformations;
            });
        $rootScope.$digest();

        //then
        expect(newResult).toBe(oldResult);
        expect(TransformationService.getTransformations.calls.count()).toBe(1);
    }));

    it('should remove all cache entries', inject(function($rootScope, TransformationCacheService, TransformationService) {
        //given
        TransformationCacheService.getTransformations(column);
        $rootScope.$digest();

        expect(TransformationService.getTransformations.calls.count()).toBe(1);

        //when
        TransformationCacheService.invalidateCache();

        TransformationCacheService.getTransformations(column);
        $rootScope.$digest();

        //then
        expect(TransformationService.getTransformations.calls.count()).toBe(2);
    }));
});