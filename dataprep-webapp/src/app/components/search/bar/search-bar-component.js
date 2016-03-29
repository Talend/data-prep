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
 * @name data-prep.data-prep.search-bar.component:SearchBarComponent
 * @description This directive display a search bar
 * @restrict E
 * @usage <search-bar></search-bar>
 */

import SearchBarCtrl from './search-bar-controller';

const SearchBarComponent = {
    templateUrl: 'app/components/search/bar/search-bar.html',
    bindings: {
        items: '<',
        placeholder: '@',
        search: '&'
    },
    controller: SearchBarCtrl
};

export default SearchBarComponent;