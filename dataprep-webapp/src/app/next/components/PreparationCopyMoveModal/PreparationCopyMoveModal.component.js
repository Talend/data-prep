import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { SelectObject } from '@talend/react-containers';
import Form from '@talend/react-containers/lib/Form';

import './PreparationCopyMoveModal.scss';

const FORM_ID = 'preparation:copy:move:form';

export default class PreparationCopyMoveModal extends React.Component {
	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
		this.copy = this.copy.bind(this);
		this.move = this.move.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	move() {
		this._proceed('preparation:move');
	}

	copy() {
		this._proceed('preparation:copy');
	}

	_proceed(action) {
		const state = this.props.state;
		const model = state.get('model', new Immutable.Map());

		this.props.dispatchActionCreator(
			action,
			{
				name: model.get('name'),
			}
		);
	}

	render() {
		const state = this.props.state;
		const show = state.get('show', false);
		const model = state.get('model', new Immutable.Map());
		const bar = {
			actions: {
				left: [
					{
						label: 'Cancel',
						bsStyle: 'link',
						onClick: this.close,
					},
				],
				right: [
					{
						label: 'Move',
						bsStyle: 'primary',
						onClick: this.move,
					},
					{
						label: 'Copy',
						bsStyle: 'primary',
						onClick: this.copy,
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
			data: {
				text: model.get('name', ''),
			},
			actions: [],
		};

		// initialState
		return (
			<Inject
				component="Dialog"
				header={'Copy/Move preparation - Select target folder'}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<SelectObject source={'folders'} id={'folders'} tree={{}} />
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
