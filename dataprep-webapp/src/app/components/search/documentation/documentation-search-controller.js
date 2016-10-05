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
 * @name data-prep.documentation-search.controller:DocumentationSearchCtrl
 * @description DocumentationSearchCtrl controller.
 * @requires data-prep.services.documentation.service:DocumentationService
 *
 */
class DocumentationSearchCtrl {

	constructor(DocumentationService) {
		'ngInject';
		this.documentationService = DocumentationService;
	}

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.documentation-search.controller:DocumentationSearchCtrl
     * @description Search based on searchInput
     */
	search(searchInput) {
		this.results = null;
		this.currentInput = searchInput;

		return this.documentationService.search(searchInput)
            .then(response => (this.currentInput === searchInput) && (this.results = response));
	}
}

export default DocumentationSearchCtrl;
