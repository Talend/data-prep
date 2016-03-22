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
 * @name data-prep.data-prep.documentation-search
 * @description This directive display an documentation search
 * @restrict E
 *
 * @usage
 * <documentation-search
 * </documentation-search>

 */

import DocumentationSearchCtrl from './documentation-search-controller';

const DocumentationSearch = {
    templateUrl: 'app/components/playground/documentation-search/documentation-search.html',
    controller: DocumentationSearchCtrl
};

export default DocumentationSearch;