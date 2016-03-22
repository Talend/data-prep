/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
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
                return _.chain(this.thcParser(response.data))
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
     * @name thcParser
     * @methodOf data-prep.services.documentation.service:DocumentationService
     * @description convert Talend help center csv to docuumentation object
     */
    thcParser(csv) {
        let lines=csv.split("\n");
        let result = [];
        let properties = ['url', 'name', 'description'];

        for(let i=0; i<lines.length; i++) {
            let obj = {};

            let row = lines[i],
                propertyIndex = 0,
                startValueIndex = 0,
                index = 0;

            //Skip empty lines
            if (row.trim() === '') { continue; }

            while (index < row.length) {
                /* if we meet a double quote we skip until the next one */
                let character = row[index];

                if (character === '"') {
                    do { character = row[++index]; } while (character !== '"' && index < row.length - 1);
                }

                if (character === ',' || /* handle end of line with no comma */ index === row.length - 1) {
                    /* we've got a value */
                    let value = row.substr(startValueIndex, index - startValueIndex).trim();

                    /* skip first double quote */
                    if (value[0] === '"') { value = value.substr(1); }
                    /* skip last comma */
                    if (value[value.length - 1] === ',') { value = value.substr(0, value.length - 1); }
                    /* skip last double quote */
                    if (value[value.length - 1] === '"') { value = value.substr(0, value.length - 1); }

                    var key = properties[propertyIndex++];
                    obj[key] = value;
                    startValueIndex = index + 1;
                }
                ++index;
            }

            //Add tooltipName for tooltip
            obj.tooltipName = obj.name;

            result.push(obj);
        }
        return result;
    }
}

export default DocumentationService;