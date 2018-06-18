import React from 'react';
import PropTypes from 'prop-types';
import { Modal, Button } from 'react-bootstrap';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Inject } from '@talend/react-components';

export default class AboutModal extends React.Component {
	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	render() {
		const { state, getComponent, components } = this.props;
		const cmfState = state;
		const injected = Inject.all(getComponent, components);
		return (
			<Modal
				show={cmfState && cmfState.get('show')}
				onHide={this.close}
			>
				<Modal.Header>
					<Modal.Title>Talend Data Preparation</Modal.Title>
				</Modal.Header>

				<Modal.Body>{injected('content')}</Modal.Body>

				<Modal.Footer>
					<Button onClick={this.close}>Close</Button>
				</Modal.Footer>
			</Modal>
		);
	}
}
AboutModal.displayName = 'AboutModal';
AboutModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
	getComponent: PropTypes.func.isRequired,
	components: PropTypes.shape().isRequired,
};
