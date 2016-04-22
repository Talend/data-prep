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
 * @name data-prep.data-prep.import.component:ImportComponent
 * @description This directive display a import component
 * @restrict E
 * @usage <import></import>
 */

import ImportCtrl from './import-controller';

const ImportComponent = {
    templateUrl: 'app/components/import/import.html',
    controller: ImportCtrl
};

export default ImportComponent;