/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const properties = ['url', 'name', 'description'];

class DocumentationService {

    constructor(DocumentationRestService, TextFormatService) {
        'ngInject';
        this.documentationRestService = DocumentationRestService;
        this.textFormatService = TextFormatService;
    }

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.services.documentation.service:DocumentationService
     * @description search documentation with keyword
     */
    search(keyword) {
        return this.documentationRestService.search(keyword)
            .then((response) => {
                return _.chain(this._thcParser(response.data))
                    .map((item) => {
                        this.textFormatService.highlight(item, 'name', keyword, 'highlighted');
                        this.textFormatService.highlight(item, 'description', keyword, 'highlighted');
                        return item;
                    })
                    .value();
            });
    }

    /**
     * @ngdoc method
     * @name _thcParser
     * @methodOf data-prep.services.documentation.service:DocumentationService
     * @description Convert Talend help center csv to documentation elements
     * @param {string} thcCsv The THC search result to adapt
     * @returns {Array} The array of documentation elements
     */
    _thcParser(thcCsv) {
        return thcCsv
            .replace(/[^\x00-\x7F]/g, " ")                  // remove non ascii du to THC encoding
            .split('\n')
            .map((line) => line.trim())
            .filter((line) => line)                         // remove empty lines
            .map((line) => line.replace(/^"(.*)"$/, '$1'))  // strip leading/trailing quotes
            .map((line) => line.split('","'))
            .filter((lineParts) => lineParts.length === properties.length)
            .map((lineParts) => this._createDocElement(lineParts));
    }

    /**
     * @ngdoc method
     * @name _createDocElement
     * @methodOf data-prep.services.documentation.service:DocumentationService
     * @description Create a document element from array ['url', 'name', 'description']
     * @param {Array} parts The documentation parts ['url', 'name', 'description']
     * @returns {object} The documentation elements
     */
    _createDocElement(parts) {
        const doc = {inventoryType: 'documentation'};
        for(let i = 0; i < properties.length; ++i) {
            const name = properties[i];
            const value = parts[i];
            doc[name] = value;
        }
        doc.tooltipName = doc.name;
        return doc;
    }
}

export default DocumentationService;