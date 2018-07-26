import './confirm-modal.scss';
import template from './confirm-modal.html';

/**
 * @ngdoc component
 * @name data-prep.confirm-modal.component:ConfirmModal
 * @description This component displays the modal used to ask for confirmation
 * @usage <confirm-modal></confirm-modal>
 * @restrict E
 */
const ConfirmModalComponent = {
	templateUrl: template,
	controller(ConfirmService, state) {
		'ngInject';
		this.ConfirmService = ConfirmService;
		this.state = state;
	},
};

export default ConfirmModalComponent;
