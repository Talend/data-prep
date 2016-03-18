/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class DatasetCopyMoveCtrl {
    constructor($element) {
        'ngInject';

        this.$element = $element;
        this.isCloningDs = false;
        this.isMovingDs = false;
        this.newDsName = this.dataset.name;
        this.destinationFolder = this.initialFolder;
    }

    _focusOnNameInput() {
        this.$element.find('#new-name-input-id').eq(0)[0].focus();
    }

    /**
     * @ngdoc method
     * @name clone
     * @methodOf d????????????????????????????????????r:DatasetListCtrl
     * @description perform the dataset cloning to the folder destination
     */
    clone() {
        this.isCloningDs = true;
        this.copyMoveForm.$commitViewValue();

        return this.onCopy({
                dataset: this.dataset,
                destination: this.destinationFolder,
                name: this.newDsName
            })
            .catch(() => {
                this._focusOnNameInput();
            })
            .finally(() => {
                this.isCloningDs = false;
            });
    }

    /**
     * @ngdoc method
     * @name move
     * @methodOf data-prep.dataset-list.controller:DatasetListCtrl
     * @description perform the dataset moving to the folder destination
     */
    move() {
        this.isMovingDs = true;
        this.copyMoveForm.$commitViewValue();

        return this.onMove({
                dataset: this.dataset,
                destination: this.destinationFolder,
                name: this.newDsName
            })
            .catch(() => {
                this._focusOnNameInput();
            })
            .finally(() => {
                this.isMovingDs = false;
            });
    }
}

export default DatasetCopyMoveCtrl;