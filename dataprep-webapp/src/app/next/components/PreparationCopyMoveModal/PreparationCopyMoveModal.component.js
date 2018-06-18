import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import { Dialog } from '@talend/react-components';
import { SelectObject } from '@talend/react-containers';

import './PreparationCopyMoveModal.scss';

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

	copy() {
		console.log('[NC] copy');
	}

	move() {
		console.log('[NC] move');
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
						onClick: this.close,
					},
				],
				right: [
					{
						label: 'Move',
						onClick: this.move,
						bsStyle: 'primary',
					},
					{
						label: 'Copy',
						onClick: this.copy,
						bsStyle: 'primary',
					},
				],
			},
		};

		return (
			<Dialog
				header={'Copy/Move preparation - Select target folder'}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<SelectObject
					source={'folders'}
					id={'folders'}
					tree={{}}
				/>
				<div>
					<span>Name</span>
					<input
						value={model.get('name', '')}
						id="copy-move-name-input"
						className="form-control"
						type="text"
						required
					/>
				</div>
			</Dialog>
		);
	}
}
PreparationCopyMoveModal.displayName = 'PreparationCopyMoveModal';
PreparationCopyMoveModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
};
