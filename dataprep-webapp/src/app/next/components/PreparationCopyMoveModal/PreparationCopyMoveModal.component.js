import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { SelectObject } from '@talend/react-containers';
import Form from '@talend/react-containers/lib/Form';

import './PreparationCopyMoveModal.scss';

const FORM_ID = 'preparation:copy:move:form';
const SELECT_OBJECT_ID = 'preparation:copy:move:form';

export default class PreparationCopyMoveModal extends React.Component {
	static getContent(state) {
		const select = SelectObject.getState(state, SELECT_OBJECT_ID);
		const form = Form.getState(state, FORM_ID);

		return {
			title: form.getIn(['data', 'text'], ''),
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
		const text = model.get('name', '');
		const selectedId = model.get('folderId', '');

		const bar = {
			actions: {
				left: [
					{ actionId: 'preparation:copy:move:cancel' },
				],
				right: [
					{
						actionId: `preparation:${action}`,
						onClick: this.proceed,
					},
				],
			},
		};

		const form = {
			formId: FORM_ID,
			jsonSchema: {
				type: 'object',
				properties: {
					text: {
						type: 'string',
					},
				},
			},
			uiSchema: [
				{
					key: 'text',
					title: 'Name',
				},
			],
			actions: [],
			initialState: Immutable.fromJS({
				data: {
					text,
				},
			}),
		};

		return (
			<Inject
				component="Dialog"
				header={`${action} preparation - Select target folder`}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<SelectObject
					source={'folders'}
					id={'folders'}
					componentId={SELECT_OBJECT_ID}
					tree={{
						initialState: {
							selectedId,
						},
					}}
				/>
				<Form {...form} />
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
