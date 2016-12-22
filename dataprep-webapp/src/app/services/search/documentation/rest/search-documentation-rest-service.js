/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class SearchDocumentationRestService {

	constructor($http, documentationSearchURL) {
		'ngInject';
		this.$http = $http;
		this.documentationSearchURL = documentationSearchURL;
	}

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.services.search.documentation:SearchDocumentationRestService
     * @description search documentation with keyword
     */
	search(keyword) {
		return this.$http({
			method: 'GET',
			url: this.documentationSearchURL + '&keywords=' + encodeURIComponent(keyword),
		});
	}
}

export default SearchDocumentationRestService;
