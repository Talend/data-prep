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
 * @name data-prep.data-prep.search-bar.controller:DocumentationSearchCtrl
 * @description DocumentationSearchCtrl controller.
 * @requires data-prep.services.documentation.service:DocumentationService
 *
 */
class SearchBarCtrl {

    /**
     * @ngdoc method
     * @name triggerSearch
     * @methodOf data-prep.data-prep.search-bar.controller:DocumentationSearchCtrl
     * @description Trigger search implementation based on searchInput
     */
    triggerSearch(searchInput) {
        this.results = null;

        if (searchInput) {
            return this.search({value: searchInput});
        }
    }
}

export default SearchBarCtrl;

