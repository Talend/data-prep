import React from 'react';
import { translate } from 'react-i18next';
import { Map } from 'immutable';
import ImmutablePropTypes from 'react-immutable-proptypes';
import PropTypes from 'prop-types';
import { cmfConnect, Inject } from '@talend/react-cmf';
import TextService from '../../services/text.service';
import I18N from '../../constants/i18n';

class FolderCreatorModal extends React.Component {
	constructor(props) {
		super(props);
		this.onChange = this.onChange.bind(this);
		this.onHide = this.onHide.bind(this);
		this.onSubmit = this.onSubmit.bind(this);
	}

	onChange() {
		const name = TextService.sanitize(this.input.value);
		this.props.setState({ name, error: '', disabled: !name.length, inProgress: false });
	}

	onHide() {
		this.props.dispatchActionCreator('FolderCreatorModal#hide');
	}

	onSubmit(event) {
		const { state } = this.props;
		const name = state.get('name', '');

		this.props.setState({
			inProgress: true,
			error: '',
			disabled: false,
		});

		this.props.dispatchActionCreator('folder:add', event, name);

		event.preventDefault();
	}

	render() {
		const { state, t } = this.props;

		const show = state.get('show', false);
		const error = state.get('error', '');
		const disabled = state.get('disabled', true);
		const inProgress = state.get('inProgress', false);

		const bar = {
			actions: {
				left: [
					{
						label: t('tdp-cmf:CANCEL'),
						bsStyle: 'default btn-inverse',
						onClick: this.onHide,
					},
				],
				right: [
					{
						label: t('tdp-app:ADD'),
						bsStyle: 'primary',
						onClick: this.onSubmit,
						disabled: disabled || error.length,
						inProgress: inProgress && !error.length,
					},
				],
			},
		};

		const addFolderLabel = t('tdp-app:ADD_FOLDER_NAME_LABEL');
		return (
			<Inject
				component="Dialog"
				header={t('tdp-app:ADD_FOLDER_HEADER')}
				error={error}
				actionbar={bar}
				show={show}
				closeButton={false}
				onHide={this.onHide}
			>
				<form onSubmit={this.onSubmit}>
					<div className="form-group field field-string">
						<input
							className="form-control"
							id="add-folder-input"
							type="text"
							autoFocus
							value={this.props.state.name}
							ref={input => {
								this.input = input;
							}}
							onChange={this.onChange}
						/>
						<label className="control-label" htmlFor="add-folder-input">
							{addFolderLabel}
						</label>
					</div>
				</form>
			</Inject>
		);
	}
}

FolderCreatorModal.displayName = 'FolderCreatorModal';
FolderCreatorModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool, name: PropTypes.string }).isRequired,
	setState: PropTypes.func.isRequired,
	t: PropTypes.func,
	...cmfConnect.propTypes,
};

export const DEFAULT_STATE = new Map({
	error: '',
	show: false,
	disabled: false,
	inProgress: false,
});

export default translate(I18N.TDP_APP_NAMESPACE)(FolderCreatorModal);
