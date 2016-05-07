/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class DatepatternService {

    constructor(DatepatternRestService) {
        'ngInject';
        this.datepatternRestService = DatepatternRestService;
    }

    /**
     * @ngdoc method
     * @name search
     * @param {string} keyword The keyword to search
     * @methodOf data-prep.services.datepattern.service:DatepatternService
     * @description search datepattern with keyword
     */
    search(keyword) {
        return this.datepatternRestService.search(keyword);
    }

    /**
     * @ngdoc method
     * @name create
     * @param {string} pattern The pattern to create
     * @methodOf data-prep.services.datepattern.service:DatepatternService
     * @description create a datepattern
     */    
    create(pattern){
        return this.datepatternRestService.create(pattern);
    }

}

export default DatepatternService;