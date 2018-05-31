import folder from './folder';
import {
	CANCEL_RENAME_PREPARATION,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
	FETCH_PREPARATIONS,
	OPEN_PREPARATION_CREATOR,
	REDIRECT_WINDOW,
} from '../constants/actions';


// FIXME [NC]: folder management has nothing to do here
// we're in the `preparation` action creators file,
// so I think that the `type` argument should not exists
function open(event, { type, id }) {
	switch (type) {
	case 'folder':
		return folder.open(event, { id });
	case 'preparation':
		return {
			type: REDIRECT_WINDOW,
			payload: {
				url: `${window.location.origin}/#/playground/preparation?prepid=${id}`,
			},
		};
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
	open,
	fetchAll,
	duplicate,
	rename,
	cancelRename,
	setTitleEditionMode,
	openCreator,
};
