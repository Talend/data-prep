/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
class DatepatternRestService {

    constructor($http, RestURLs) {
        'ngInject';
        this.$http = $http;
        this.url = RestURLs.datepatternsUrl;
    }

    /**
     * @ngdoc method
     * @name search
     * @methodOf data-prep.services.datepattern.service:DatepatternRestService
     * @description search datepattern
     */
    search(keyword) {
        return this.$http({
            method: 'GET',
            url: this.url + '?keyword=' + encodeURIComponent(keyword)
        });
    }
    
    create(pattern){
        return this.$http({
            method: 'PUT',
            url: this.url + '?pattern=' + encodeURIComponent(pattern)
        });
    }
}

export default DatepatternRestService;