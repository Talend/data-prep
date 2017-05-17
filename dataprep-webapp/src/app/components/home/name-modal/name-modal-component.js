import NameModalCtrl from './name-modal-controller';

import template from './name-modal.html';
/**
 * @ngdoc component
 * @name data-prep.name-modal.component:NameModal
 * @description This component displays the modal used to rename a dataset in case of conflict
 * @usage <name-modal></name-modal>
 * @restrict E
 */
const NameModalComponent = {
	templateUrl: template,
	controller: NameModalCtrl,
};

export default NameModalComponent;
