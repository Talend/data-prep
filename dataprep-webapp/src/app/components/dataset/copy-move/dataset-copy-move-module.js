/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetCopyMove from './dataset-copy-move-component';

(() => {

    /**
     * @ngdoc object
     * @name data-prep.dataset-copy-move
     * @description This module contains the controller and directives to manage the dataset copy move
     * @requires data-prep.folder-selection
     */
    angular.module('data-prep.dataset-copy-move', [
            'data-prep.folder-selection',
            'talend.widget'
        ])
        .component('datasetCopyMove', DatasetCopyMove);
})();