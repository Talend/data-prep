import {
	OPEN_ADD_FOLDER_MODAL,
	CLOSE_ADD_FOLDER_MODAL,
	OPEN_FOLDER,
	ADD_FOLDER,
} from '../constants/actions';

function open(event, { id }) {
	return {
		type: OPEN_FOLDER,
		id,
		cmf: {
			routerPush: `/preparations/${id}`,
		},
	};
}

function add() {
	return {
		type: ADD_FOLDER,
	};
}

function openAddFolderModal() {
	return {
		type: OPEN_ADD_FOLDER_MODAL,
	};
}

function closeAddFolderModal() {
	return {
		type: CLOSE_ADD_FOLDER_MODAL,
	};
}

export default {
	add,
	open,
	openAddFolderModal,
	closeAddFolderModal,
};
