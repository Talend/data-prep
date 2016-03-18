/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import DatasetCopyMoveCtrl from './dataset-copy-move-controller';

const DatasetCopyMove = {
    bindings: {
        initialFolder: '<',
        dataset: '<',
        onCopy: '&',
        onMove: '&'
    },
    controllerAs: 'datasetCopyMoveCtrl',
    controller: DatasetCopyMoveCtrl,
    template: `
    <div class="folders-modal-content">
        <div class="modal-title" translate-once="CHOOSE_FOLDER_DESTINATION" translate-values="{type: 'dataset', name: dataset.name}">
        </div>

        <form name="datasetCopyMoveCtrl.copyMoveForm">

            <folder-selection
                    ng-model="datasetCopyMoveCtrl.destinationFolder"></folder-selection>

            <div class="clone-name">
                <span translate-once="DATASET_NAME"></span>
                <input required type="text"
                       ng-model="datasetCopyMoveCtrl.newDsName"
                       id="new-name-input-id"/>
            </div>
            <div class="modal-buttons">
                <button type="button"
                        id="cancel-move-copy-btn"
                        class="talend-modal-close btn-secondary modal-secondary-button"
                        ng-disabled="datasetCopyMoveCtrl.isMovingDs || datasetCopyMoveCtrl.isCloningDs"
                        translate-once="CANCEL">
                </button>

                <talend-button-loader
                        button-class="btn-primary modal-primary-button"
                        id="move-ds-btn"
                        disable-condition="datasetCopyMoveCtrl.copyMoveForm.$invalid  || datasetCopyMoveCtrl.isMovingDs || datasetCopyMoveCtrl.isCloningDs"
                        loading="datasetCopyMoveCtrl.isMovingDs"
                        loading-class="icon"
                        ng-click="datasetCopyMoveCtrl.move()">
                    <span translate-once="MOVE_HERE_ACTION"></span>
                </talend-button-loader>

                <talend-button-loader
                        button-class="btn-primary modal-primary-button"
                        id="clone-ds-btn"
                        disable-condition="datasetCopyMoveCtrl.copyMoveForm.$invalid || datasetCopyMoveCtrl.isMovingDs || datasetCopyMoveCtrl.isCloningDs"
                        loading="datasetCopyMoveCtrl.isCloningDs"
                        loading-class="icon"
                        ng-click="datasetCopyMoveCtrl.clone()">
                    <span translate-once="COPY_HERE_ACTION"></span>
                </talend-button-loader>

            </div>
        </form>
    </div>`
};

export default DatasetCopyMove;