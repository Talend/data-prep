import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Dialog } from '@talend/react-components';
import { SelectObject } from '@talend/react-containers';

export default class PreparationCopyMoveModal extends React.Component {
	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	render() {
		const infos = {
			actions: {
				left: [
					{
						label: 'Cancel',
						onClick: () => console.log('Preparations clicked'),
					},
				],
				right: [
					{
						label: 'Move',
						onClick: () => console.log('Datasets clicked'),
						bsStyle: 'primary',
					},
					{
						label: 'Copy',
						onClick: () => console.log('Favorites clicked'),
						bsStyle: 'primary',
					},
				],
			},
		};

		const selectObjectProps = {
			id: 'folders',
			source: 'folders',
			tree: {},
			// selectedId: props.preparationId,
		};

		const cmfState = this.props.state;
		return (
			<Dialog
				actionbar={infos}
				onHide={this.close}
				show={cmfState && cmfState.get('show')}
				header={'Copy/Move preparation - Select target folder'}
			>
				<SelectObject {...selectObjectProps} />
			</Dialog>
		);
	}
}
PreparationCopyMoveModal.displayName = 'PreparationCopyMoveModal';
PreparationCopyMoveModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
};
