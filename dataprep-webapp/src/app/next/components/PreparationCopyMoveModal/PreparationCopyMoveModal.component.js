import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { SelectObject, EditableText } from '@talend/react-containers';

import './PreparationCopyMoveModal.scss';


export default class PreparationCopyMoveModal extends React.Component {
	static EDITABLE_TEXT_ID = 'preparation:copy:move:editable:text';
	static SELECT_OBJECT_ID = 'preparation:copy:move:select:object';

	static getContent(state) {
		const select = SelectObject.getState(state, PreparationCopyMoveModal.SELECT_OBJECT_ID);
		const title = state.cmf.components.getIn(['PreparationCopyMoveModal', 'default', 'name'], '');

		return {
			title,
			destination: select.get('selectedId', ''),
		};
	}

	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
		this.proceed = this.proceed.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	proceed(event, { action }) {
		const state = this.props.state;
		const model = state.get('model', new Immutable.Map());
		this.props.dispatchActionCreator(action.id, event, model.toJS());
	}

	render() {
		const state = this.props.state;
		const show = state.get('show', false);
		const action = state.get('action');
		const model = state.get('model', new Immutable.Map());
		const error = state.get('error', null);
		const text = state.get('name', '');
		const selectedId = model.get('folderId', '');

		const bar = {
			actions: {
				left: [{ actionId: 'preparation:copy:move:cancel' }],
				right: [
					{
						actionId: `preparation:${action}`,
						onClick: this.proceed,
						disabled: error && error.length,
					},
				],
			},
		};

		return (
			<Inject
				component="Dialog"
				header={`${action} your preparation`}
				subtitle={`Click on the folder you want to ${action} your preparation`}
				error={error}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<EditableText
					componentId={PreparationCopyMoveModal.EDITABLE_TEXT_ID}
					text={text}
					onSubmit={(_, { value }) => {
						this.props.setState({ name: value, error: null });
					}}
					onChange={() => this.props.setState({ error: null })}
					onCancel={() => this.props.setState({ error: null })}
				/>
				<hr className={'modal-separator'} />
				<SelectObject
					source={'folders'}
					id={'folders'}
					componentId={PreparationCopyMoveModal.SELECT_OBJECT_ID}
					tree={{
						initialState: {
							selectedId,
						},
					}}
				/>
			</Inject>
		);
	}
}
PreparationCopyMoveModal.displayName = 'PreparationCopyMoveModal';
PreparationCopyMoveModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
	...cmfConnect.INJECTED_PROPS,
};
