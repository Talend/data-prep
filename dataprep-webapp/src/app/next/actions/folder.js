import {
	OPEN_ADD_FOLDER_MODAL,
	CLOSE_ADD_FOLDER_MODAL,
	OPEN_FOLDER,
	ADD_FOLDER,
	REMOVE_FOLDER,
	OPEN_REMOVE_FOLDER_MODAL,
	CLOSE_REMOVE_FOLDER_MODAL,
} from '../constants/actions';

function remove() {
	return {
		type: REMOVE_FOLDER,
	};
}

function openRemoveFolderModal(event, { model }) {
	return {
		type: OPEN_REMOVE_FOLDER_MODAL,
		payload: model,
	};
}

function closeRemoveFolderModal() {
	return {
		type: CLOSE_REMOVE_FOLDER_MODAL,
	};
}

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
	remove,
	openAddFolderModal,
	closeAddFolderModal,
	openRemoveFolderModal,
	closeRemoveFolderModal,
};
