import React from 'react';
import { ConfirmDialog } from '@talend/react-components';
import { translate } from 'react-i18next';
import ImmutablePropTypes from 'react-immutable-proptypes';
import PropTypes from 'prop-types';
import { cmfConnect } from '@talend/react-cmf/lib/index';
import TextService from '../../services/text.service';
import I18N from '../../constants/i18n';

class FolderCreatorModal extends React.Component {
	constructor(props) {
		super(props);
		this.onChange = this.onChange.bind(this);
		this.onSubmit = this.onSubmit.bind(this);
	}

	onChange() {
		const name = this.folderNameInput.value;
		const validateAction = { ...this.props.state.validateAction };
		validateAction.disabled = !TextService.sanitize(name);
		this.props.setState({ name, error: '', validateAction });
	}

	onSubmit(event, data) {
		if (TextService.sanitize(this.folderNameInput.value)) {
			this.props.state.validateAction.onClick(event, data);
		}
		event.preventDefault();
	}

	render() {
		const addFolderLabel = this.props.t('tdp-app:ADD_FOLDER_NAME_LABEL');
		return (
			<ConfirmDialog {...this.props.state}>
				<form onSubmit={this.onSubmit}>
					<div className="form-group field field-string">
						<input
							className="form-control"
							id="add-folder-input"
							type="text"
							autoFocus
							value={this.props.state.name}
							ref={(input) => {
								this.folderNameInput = input;
							}}
							onChange={this.onChange}
						/>
						<label className="control-label" htmlFor="add-folder-input">
							{ addFolderLabel }
						</label>
					</div>
				</form>
			</ConfirmDialog>
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

export default translate(I18N.TDP_APP_NAMESPACE)(FolderCreatorModal);
