import {
	CANCEL_RENAME_PREPARATION,
	OPEN_FOLDER,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
	FETCH_PREPARATIONS,
	OPEN_PREPARATION_CREATOR,
} from '../constants';

function openPreparation(event, { id, type }) {
	switch (type) {
	case 'folder':
		return {
			type: OPEN_FOLDER,
			id,
			cmf: {
				routerPush: `/preparations/${id}`,
			},
		};
	case 'preparation':
		/* TODO
		- get current url
		- pass it in the url as 'return' query param
		- playground must redirect to this return param on close
		*/
		window.location.href = `${window.location.origin}/#/playground/preparation?prepid=${id}`;
		break;
	default:
		break;
	}
}

function fetchAll() {
	// TODO [NC]: folderId
	return { type: FETCH_PREPARATIONS };
}

function duplicate(event, { model }) {
	return {
		type: PREPARATION_DUPLICATE,
		payload: {
			id: model.id,
		},
	};
}

function rename(event, data) {
	return {
		type: RENAME_PREPARATION,
		payload: {
			id: data.model.id,
			name: data.value,
		},
	};
}

function cancelRename(event, { id }) {
	return {
		type: CANCEL_RENAME_PREPARATION,
		payload: id,
	};
}

function setTitleEditionMode(event, { model }) {
	return {
		type: SET_TITLE_EDITION_MODE,
		payload: model.id,
	};
}

function openCreator() {
	return { type: OPEN_PREPARATION_CREATOR };
}

export default {
	openPreparation,
	fetchAll,
	duplicate,
	rename,
	cancelRename,
	setTitleEditionMode,
	openCreator,
};
