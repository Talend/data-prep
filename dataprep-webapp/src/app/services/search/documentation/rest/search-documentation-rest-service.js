/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class SearchDocumentationRestService {

	constructor($http, HelpService) {
		'ngInject';

		this.$http = $http;

		const { searchUrl, versionFacet, languageFacet } = HelpService;
		this.url = searchUrl;
		this.parameters = {
			contentLocale: languageFacet,
			filters: [
				{
					key: 'version',
					values: [versionFacet],
				},
				{
					key: 'EnrichPlatform',
					values: ['Talend Data Preparation'],
				},
			],
			paging: {
				page: 1,
				perPage: 5,
			},

		};
	}

	/**
	 * @ngdoc method
	 * @name search
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationRestService
	 * @description search documentation with keyword
	 */
	search(keyword) {
		return this.$http({
			url: this.url,
			data: {
				...this.parameters,
				query: keyword,
			},
			method: 'POST',
			failSilently: true,
			withCredentials: false,
		});
	}
}

export default SearchDocumentationRestService;
