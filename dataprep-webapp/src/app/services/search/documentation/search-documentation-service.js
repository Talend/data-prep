/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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
		this.domEl = document.createElement('p');
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
				return response.data.results.map(this._createDocElement.bind(this));
			})
			.catch(() => []);
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
			description: this._convertHtmlSpecialCharacters(topic.htmlExcerpt.replace(/(<[^>]*>)/g, '')),
			name: this._convertHtmlSpecialCharacters(topic.htmlTitle.replace(/(<[^>]*>)/g, '')),
			url: topic.occurrences[0].readerUrl,
		};
		doc.tooltipName = doc.name;
		return doc;
	}

	/**
	 * @ngdoc method
	 * @name _convertHtmlSpecialCharacters
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationService
	 * @description converts special html characters into readable characters
	 * @param {String} The raw string
	 * @returns {String} readable string
	 */
	_convertHtmlSpecialCharacters(unreadableStr) {
		this.domEl.innerHTML = unreadableStr;
		return this.domEl.innerText;
	}
}

export default SearchDocumentationService;
