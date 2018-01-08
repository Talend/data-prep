/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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
		onMove: '&',
		isLoading: '<',
		tree: '<',
	},
	controller: InventoryCopyMoveCtrl,
	template: `
    <div>
        <div class="modal-title"
             translate-once="CHOOSE_FOLDER_DESTINATION"
             translate-values="{type: 'item', name: item.name}"></div>

        <form name="$ctrl.copyMoveForm">
            <folder-selection
            	ng-model="$ctrl.destinationFolder"
            	is-loading="$ctrl.isLoading"
                tree="$ctrl.tree">
            </folder-selection>

            <div>
                <span translate-once="NAME"></span>
                <input id="copy-move-name-input"
                       class="form-control"
                       type="text"
                       ng-model="$ctrl.newName"
                       required/>
            </div>
            <div class="modal-buttons">
                <button id="copy-move-cancel-btn"
                        type="button"
                        class="btn talend-modal-close btn-default modal-secondary-button"
                        ng-disabled="$ctrl.isMoving || $ctrl.isCopying"
                        translate-once="CANCEL">
                </button>

                <talend-button-loader
                        id="copy-move-move-btn"
                        button-class="btn btn-primary modal-primary-button separated-button {{$ctrl.isActionDisabled() ? 'disabled' : ''}}"
                        disable-condition="$ctrl.isActionDisabled()"
                        loading="$ctrl.isMoving"
                        loading-class="icon"
                        ng-click="$ctrl.move()"
                        title="{{($ctrl.isActionDisabled() ? 'WAITING_FOLDERS_TITLE' : 'MOVE_HERE_ACTION') | translate}}">
                    <span translate-once="MOVE_HERE_ACTION"></span>
                </talend-button-loader>

                <talend-button-loader
                        id="copy-move-copy-btn"
                        button-class="btn btn-primary modal-primary-button {{$ctrl.isActionDisabled() ? 'disabled' : ''}}"
                        disable-condition="$ctrl.isActionDisabled()"
                        loading="$ctrl.isCopying"
                        loading-class="icon"
                        ng-click="$ctrl.copy()"
                        title="{{($ctrl.isActionDisabled() ? 'WAITING_FOLDERS_TITLE' : 'COPY_HERE_ACTION') | translate}}">
                    <span translate-once="COPY_HERE_ACTION"></span>
                </talend-button-loader>
            </div>
        </form>
    </div>`,
};

export default InventoryCopyMoveComponent;
