import matchPath from '@talend/react-cmf/lib/sagaRouter/matchPath';
import folder from './folder';
import PreparationCopyMoveModal from '../components/PreparationCopyMoveModal';
import {
	PREPARATION_COPY,
	PREPARATION_MOVE,
	CANCEL_RENAME_PREPARATION,
	PREPARATION_DUPLICATE,
	RENAME_PREPARATION,
	SET_TITLE_EDITION_MODE,
	FETCH_PREPARATIONS,
	OPEN_PREPARATION_CREATOR,
	REDIRECT_WINDOW,
	OPEN_COPY_MOVE_MODAL,
	CLOSE_COPY_MOVE_MODAL,
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

function fetch(payload) {
	let folderId;
	if (payload) {
		folderId = payload.folderId;
	}
	else if (matchPath(window.location.pathname, { path: '/preparations/:folderId' })) {
		folderId = matchPath.params.folderId;
	}
	return {
		type: FETCH_PREPARATIONS,
		payload: {
			folderId,
		},
	};
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

function openPreparationCreatorModal() {
	return { type: OPEN_PREPARATION_CREATOR };
}

function openCopyMoveModal(event, { model }) {
	return {
		type: OPEN_COPY_MOVE_MODAL,
		payload: model,
	};
}

function closeCopyMoveModal() {
	return {
		type: CLOSE_COPY_MOVE_MODAL,
	};
}

function copy(event, { id }, context) {
	return {
		type: PREPARATION_COPY,
		payload: {
			...PreparationCopyMoveModal.getContent(context.store.getState()),
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
	fetch,
	rename,
	duplicate,
	cancelRename,
	openCopyMoveModal,
	closeCopyMoveModal,
	setTitleEditionMode,
	openPreparationCreatorModal,
};
