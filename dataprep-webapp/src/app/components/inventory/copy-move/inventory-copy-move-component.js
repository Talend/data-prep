/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import InventoryCopyMoveCtrl from './inventory-copy-move-controller';

const InventoryCopyMoveComponent = {
    bindings: {
        initialFolder: '<',
        item: '<',
        onCopy: '&',
        onMove: '&'
    },
    controller: InventoryCopyMoveCtrl,
    template: `
    <div>
        <div class="modal-title" 
             translate-once="CHOOSE_FOLDER_DESTINATION" 
             translate-values="{type: 'item', name: item.name}"></div>

        <form name="$ctrl.copyMoveForm">
            <folder-selection ng-model="$ctrl.destinationFolder"></folder-selection>

            <div>
                <span translate-once="NAME"></span>
                <input id="copy-move-name-input"
                       type="text"
                       ng-model="$ctrl.newName"
                       required/>
            </div>
            <div class="modal-buttons">
                <button id="copy-move-cancel-btn"
                        type="button"
                        class="talend-modal-close btn-secondary modal-secondary-button"
                        ng-disabled="$ctrl.isMoving || $ctrl.isCopying"
                        translate-once="CANCEL">
                </button>

                <talend-button-loader
                        id="copy-move-move-btn"
                        button-class="btn-primary modal-primary-button"
                        disable-condition="$ctrl.copyMoveForm.$invalid  || $ctrl.isMoving || $ctrl.isCopying"
                        loading="$ctrl.isMoving"
                        loading-class="icon"
                        ng-click="$ctrl.move()">
                    <span translate-once="MOVE_HERE_ACTION"></span>
                </talend-button-loader>

                <talend-button-loader
                        id="copy-move-copy-btn"
                        button-class="btn-primary modal-primary-button"
                        disable-condition="$ctrl.copyMoveForm.$invalid || $ctrl.isMoving || $ctrl.isCopying"
                        loading="$ctrl.isCopying"
                        loading-class="icon"
                        ng-click="$ctrl.copy()">
                    <span translate-once="COPY_HERE_ACTION"></span>
                </talend-button-loader>
            </div>
        </form>
    </div>`
};

export default InventoryCopyMoveComponent;