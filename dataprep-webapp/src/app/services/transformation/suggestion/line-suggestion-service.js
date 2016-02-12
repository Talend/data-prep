/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc service
 * @name data-prep.services.transformation.service:LineSuggestionService
 * @description Transformation Line suggestion service. This service provide the current line suggestions
 * @requires data-prep.services.transformation.service:TransformationCacheService
 * @requires data-prep.services.state.service:StateService
 */
export default function LineSuggestionService(TransformationCacheService, StateService) {
    'ngInject';

    return {
        initTransformations: initTransformations
    };

    function initTransformations() {
        StateService.setSuggestionsLoading(true);
        TransformationCacheService.getLineTransformations()
            .then(function (lineTransformations) {
                StateService.setLineTransformations({
                    allTransformations: lineTransformations.allTransformations,
                    allCategories: lineTransformations.allCategories,
                    filteredTransformations: lineTransformations.allCategories
                });
            })
            .finally(function () {
                StateService.setSuggestionsLoading(false);
            });
    }
}