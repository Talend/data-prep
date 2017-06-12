/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class SearchDocumentationService {

	constructor(SearchDocumentationRestService, TextFormatService) {
		'ngInject';

		this.searchDocumentationRestService = SearchDocumentationRestService;
		this.textFormatService = TextFormatService;
	}

	/**
	 * @ngdoc method
	 * @name search
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationService
	 * @description search documentation with keyword
	 */
	search(keyword) {
		return this.searchDocumentationRestService.search(keyword)
			.then((response) => {
				return response.data.results.map(this._createDocElement);
			})
			.catch(() => []);
	}

	/**
	 * @ngdoc method
	 * @name searchAndHighlight
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationService
	 * @description search documentation with keyword and highlight terms
	 */
	searchAndHighlight(keyword) {
		return this.search(keyword)
			.then((results) => {
				return results.map((item) => {
					this.textFormatService.highlight(item, 'name', keyword, 'highlighted');
					this.textFormatService.highlight(item, 'description', keyword, 'highlighted');
					return item;
				});
			});
	}

	/**
	 * @ngdoc method
	 * @name _createDocElement
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationService
	 * @description Creates a document element following the Typeahead props
	 * @param {Array} topic the matched result
	 * @returns {object} The documentation elements
	 */
	_createDocElement(topic) {
		const doc = {
			inventoryType: 'documentation',
			description: topic.htmlExcerpt.replace(/(<[^>]*>)/g, '').replace('&lt;', '<').replace('&gt;', '>'),
			name: topic.htmlTitle.replace(/(<[^>]*>)/g, '').replace('&lt;', '<').replace('&gt;', '>'),
			url: topic.occurrences[0].readerUrl,
		};
		doc.tooltipName = doc.name;
		return doc;
	}
}

export default SearchDocumentationService;
