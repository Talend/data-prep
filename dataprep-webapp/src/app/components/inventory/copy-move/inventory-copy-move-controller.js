/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class InventoryCopyMoveCtrl {
	constructor($element) {
		'ngInject';

		this.$element = $element;
		this.isCopying = false;
		this.isMoving = false;
		this.newName = this.item.name;
		this.destinationFolder = this.initialFolder;

		this.copy = this.copy.bind(this);
		this.move = this.move.bind(this);
	}

	_focusOnNameInput() {
		this.$element.find('#copy-move-name-input').eq(0)[0].focus();
	}

	isActionDisabled() {
		return this.isLoading || this.copyMoveForm.$invalid || this.isMoving || this.isCopying;
	}

	/**
	 * @ngdoc method
	 * @name copy
	 * @methodOf data-prep.dataset-copy-move.controller:DatasetCopyMoveCtrl
	 * @description Perform a copy to the folder destination
	 */
	copy() {
		if (this.isActionDisabled()) {
			return;
		}
		this.isCopying = true;
		this.copyMoveForm.$commitViewValue();

		return this.onCopy(
			{
				item: this.item,
				destination: this.destinationFolder,
				name: this.newName,
			})
			.catch(() => {
				this._focusOnNameInput();
			})
			.finally(() => {
				this.isCopying = false;
			});
	}

	/**
	 * @ngdoc method
	 * @name move
	 * @methodOf data-prep.dataset-copy-move.controller:DatasetCopyMoveCtrl
	 * @description Perform a move to the folder destination
	 */
	move() {
		if (this.isActionDisabled()) {
			return;
		}
		this.isMoving = true;
		this.copyMoveForm.$commitViewValue();

		return this.onMove(
			{
				item: this.item,
				destination: this.destinationFolder,
				name: this.newName,
			})
			.catch(() => {
				this._focusOnNameInput();
			})
			.finally(() => {
				this.isMoving = false;
			});
	}
}

export default InventoryCopyMoveCtrl;
