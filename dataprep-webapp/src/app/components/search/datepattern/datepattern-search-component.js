/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

/**
 * @ngdoc directive
 * @name data-prep.data-prep.datepattern-search
 * @description This directive display a datepattern search
 * @restrict E
 * @usage <datepattern-search></datepattern-search>
 */

import DatepatternSearchCtrl from './datepattern-search-controller';

const DatepatternSearch = {
    templateUrl: 'app/components/search/datepattern/datepattern-search.html',
    bindings: {
        choose: '&',
        submit: '&'
    },    
    controller: DatepatternSearchCtrl
};

export default DatepatternSearch;