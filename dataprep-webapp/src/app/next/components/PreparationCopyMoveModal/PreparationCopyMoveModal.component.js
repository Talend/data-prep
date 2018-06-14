import React from 'react';
import PropTypes from 'prop-types';
import { Button, Modal } from 'react-bootstrap';
import ImmutablePropTypes from 'react-immutable-proptypes';

export default class PreparationCopyMoveModal extends React.Component {
	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	render() {
		const cmfState = this.props.state;
		return (
			<Modal
				show={cmfState && cmfState.get('show')}
				onHide={this.close}
			>
				<Modal.Header>
					<Modal.Title>Copy / Move</Modal.Title>
				</Modal.Header>

				<Modal.Body>Modal content</Modal.Body>

				<Modal.Footer>
					<Button onClick={this.close}>Close</Button>
				</Modal.Footer>
			</Modal>
		);
	}
}
PreparationCopyMoveModal.displayName = 'PreparationCopyMoveModal';
PreparationCopyMoveModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
};
