import matchPath from '@talend/react-cmf/lib/sagaRouter/matchPath';
import folder from './folder';
import PreparationCopyMoveModal from '../components/PreparationCopyMoveModal';
import {
	REDIRECT_WINDOW,
	OPEN_COPY_MODAL,
	OPEN_MOVE_MODAL,
	PREPARATION_COPY,
	PREPARATION_MOVE,
	RENAME_PREPARATION,
	CREATE_PREPARATIONS,
	FETCH_PREPARATIONS,
	CLOSE_COPY_MOVE_MODAL,
	SET_TITLE_EDITION_MODE,
	OPEN_PREPARATION_CREATOR,
	CANCEL_RENAME_PREPARATION,
	OPEN_REMOVE_FOLDER_MODAL,
	REMOVE_PREPARATION,
} from '../constants/actions';

const TYPES = {
	FOLDER: 'folder',
	PREPARATION: 'preparation',
};

// FIXME [NC]: folder management has nothing to do here
// we're in the `preparation` action creators file,
// so I think that the `type` argument should not exists
function open(event, { type, id }) {
	switch (type) {
	case TYPES.FOLDER:
		return folder.open(event, { id });
	case TYPES.PREPARATION:
		return {
			type: REDIRECT_WINDOW,
			payload: {
				url: `${window.location.origin}/#/playground/preparation?prepid=${id}`,
			},
		};
	}
}

function create(event, payload) {
	return {
		type: CREATE_PREPARATIONS,
		payload,
	};
}

function fetch(payload) {
	let folderId;
	const match = matchPath(window.location.pathname, { path: '/preparations/:folderId' });
	if (payload) {
		folderId = payload.folderId;
	}
	else if (match) {
		folderId = match.params.folderId;
	}
	return {
		type: FETCH_PREPARATIONS,
		payload: {
			folderId,
		},
	};
}

function rename(event, { model, value }) {
	return {
		type: RENAME_PREPARATION,
		payload: {
			id: model.id,
			type: model.type,
			name: value,
		},
	};
}

function remove(event, payload) {
	switch (payload.model.type) {
	case TYPES.FOLDER:
		return {
			type: OPEN_REMOVE_FOLDER_MODAL,
			payload: payload.model,
		};
	case TYPES.PREPARATION:
		return {
			type: REMOVE_PREPARATION,
			payload: payload.model.id,
		};
	}
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

function openPreparationCreatorModal() {
	return { type: OPEN_PREPARATION_CREATOR };
}

function openCopyModal(event, { model }) {
	return {
		type: OPEN_COPY_MODAL,
		payload: model,
	};
}

function openMoveModal(event, { model }) {
	return {
		type: OPEN_MOVE_MODAL,
		payload: model,
	};
}

function closeCopyMoveModal() {
	return {
		type: CLOSE_COPY_MOVE_MODAL,
	};
}

function copy(event, { id, folderId }, context) {
	return {
		type: PREPARATION_COPY,
		payload: {
			...PreparationCopyMoveModal.getContent(context.store.getState()),
			folderId,
			id,
		},
	};
}

function move(event, { id, folderId }, context) {
	return {
		type: PREPARATION_MOVE,
		payload: {
			...PreparationCopyMoveModal.getContent(context.store.getState()),
			folderId,
			id,
		},
	};
}

export default {
	open,
	copy,
	move,
	create,
	fetch,
	remove,
	rename,
	cancelRename,
	openCopyModal,
	openMoveModal,
	closeCopyMoveModal,
	setTitleEditionMode,
	openPreparationCreatorModal,
};
