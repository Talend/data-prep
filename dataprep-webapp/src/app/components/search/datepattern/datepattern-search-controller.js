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
 * @name data-prep.datepattern-search.controller:DatepatternSearchCtrl
 * @description DatepatternSearchCtrl controller.
 * @requires data-prep.services.datepattern.service:DatepatternService
 *
 */
class DatepatternSearchCtrl {

    constructor(DatepatternService) {
        'ngInject';
        this.datepatternService = DatepatternService;
    }


    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.datepattern-search.controller:DatepatternSearchCtrl
     * @description Search based on searchInput
     */
    search(searchInput) {
        this.items = null;
        this.currentInput = searchInput;

        return this.datepatternService.search(searchInput)
            .then((response) => (this.currentInput === searchInput) && (this.items = response.data));
    }

    /**
     * @ngdoc method
     * @name onSubmit
     * @methodOf data-prep.datepattern-search.controller:DatepatternSearchCtrl
     * @description Create a datepattern then call the submit method
     */    
    onSubmit(value){
        this.datepatternService.create(value)
            .then(this.submit({ datepattern: value }));
    }

    /**
     * @ngdoc method
     * @name onChoose
     * @methodOf data-prep.datepattern-search.controller:DatepatternSearchCtrl
     * @description Call the choose method
     */
    onChoose(value){
        this.choose({ datepattern: value });
    }
}

export default DatepatternSearchCtrl;

