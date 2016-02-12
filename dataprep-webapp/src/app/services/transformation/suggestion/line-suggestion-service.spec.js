/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Line suggestion service', function () {
    'use strict';

    var allTransformations = [
        {name: 'delete', category: 'clean', label: 'c', labelHtml: 'c', description: 'test'},
        {name: 'tolowercase', category: 'case', label: 'v', labelHtml: 'v', description: 'test'},
        {name: 'touppercase', category: 'case', label: 'u', labelHtml: 'u', description: 'test',  actionScope: ['unknown']},
        {name: 'totitlecase', category: 'case', label: 't', labelHtml: 't', description: 'test', actionScope: ['invalid']}
    ];

    var allCategories = [
        {
            category: 'case',
            transformations: [
                {name: 'totitlecase', category: 'case', label: 't', labelHtml: 't', description: 'test', actionScope: ['invalid']},
                {name: 'touppercase', category: 'case', label: 'u', labelHtml: 'u', description: 'test', actionScope: ['unknown']},
                {name: 'tolowercase', category: 'case', label: 'v', labelHtml: 'v', description: 'test'}
            ]
        },
        {
            category: 'clean',
            transformations: [{name: 'delete', category: 'clean', label: 'c', labelHtml: 'c', description: 'test'}]
        }
    ];

    beforeEach(angular.mock.module('data-prep.services.transformation'));

    beforeEach(inject(function ($q, StateService, TransformationCacheService) {
        spyOn(TransformationCacheService, 'getLineTransformations').and.returnValue($q.when({
            allTransformations: allTransformations,
            allCategories: allCategories
        }));

        spyOn(StateService, 'setSuggestionsLoading').and.returnValue();
        spyOn(StateService, 'setLineTransformations').and.returnValue();
    }));

    describe('fetch line transformations', function() {
        it('should get transformations from TransformationCacheService', inject(function ($rootScope, LineSuggestionService, TransformationCacheService) {
            //when
            LineSuggestionService.initTransformations();
            $rootScope.$digest();

            //then
            expect(TransformationCacheService.getLineTransformations).toHaveBeenCalled();
        }));

        it('should manage loading flag', inject(function ($rootScope, LineSuggestionService, StateService) {
            //given
            expect(StateService.setSuggestionsLoading).not.toHaveBeenCalled();

            //when
            LineSuggestionService.initTransformations();
            expect(StateService.setSuggestionsLoading).toHaveBeenCalledWith(true);
            $rootScope.$digest();

            //then
            expect(StateService.setSuggestionsLoading).toHaveBeenCalledWith(false);
        }));

        it('should set line transformations in state', inject(function ($rootScope, LineSuggestionService, StateService) {
            //given
            expect(StateService.setLineTransformations).not.toHaveBeenCalled();

            //when
            LineSuggestionService.initTransformations();
            $rootScope.$digest();

            //then
            expect(StateService.setLineTransformations).toHaveBeenCalledWith({
                allTransformations: allTransformations,
                allCategories: allCategories,
                filteredTransformations: allCategories
            });
        }));
    });
});