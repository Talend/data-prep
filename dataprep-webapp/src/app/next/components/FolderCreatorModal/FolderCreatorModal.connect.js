import React from 'react';
import { cmfConnect } from '@talend/react-cmf';
import { Map } from 'immutable';
import FolderCreatorModal from './FolderCreatorModal.component';

export function FolderCreatorContainer(props) {
	const state = props.state.toJS();
	const newProps = {
		...props,
		state: {
			...state,
			validateAction: {
				...state.validateAction,
				onClick: (event, data) => props.dispatchActionCreator(state.validateAction.actionCreator, event, data),
			},
			cancelAction: {
				...state.cancelAction,
				onClick: (event, data) => props.dispatchActionCreator(state.cancelAction.actionCreator, event, data),
			},
		},
	};

	return <FolderCreatorModal {...newProps} />;
}

FolderCreatorContainer.displayName = 'FolderCreatorModal';
FolderCreatorContainer.propTypes = {
	...cmfConnect.propTypes,
};

export default cmfConnect({
	componentId: 'add:folder:modal',
	defaultState: new Map({ show: false }),
})(FolderCreatorContainer);
