import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import Immutable from 'immutable';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { SelectObject, EditableText } from '@talend/react-containers';
import { translate } from 'react-i18next';

import I18N from './../../constants/i18n';

import './PreparationCopyMoveModal.scss';

class PreparationCopyMoveModal extends React.Component {
	static DISPLAY_NAME = 'Translate(PreparationCopyMoveModal)';
	static EDITABLE_TEXT_ID = 'preparation:copy:move:editable:text';
	static SELECT_OBJECT_ID = 'preparation:copy:move:select:object';

	static getContent(state) {
		const select = SelectObject.getState(state, PreparationCopyMoveModal.SELECT_OBJECT_ID);
		const title = state.cmf.components.getIn([PreparationCopyMoveModal.DISPLAY_NAME, 'default', 'name'], '');

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
		const { state, t } = this.props;
		const action = state.get('action');

		if (!action) {
			return null;
		}

		const show = state.get('show', false);
		const model = state.get('model', new Immutable.Map());
		const error = state.get('error', null);
		const text = state.get('name', '');
		const selectedId = model.get('folderId', '');
		const key = action.toUpperCase();
		const label = t(`tdp-cmf:${key}`);

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
		const title = t('tdp-app:COPY_MOVE_MODAL_TITLE', {
			action: label,
		});
		const subtitle = t('tdp-app:COPY_MOVE_MODAL_SUBTITLE', {
			action: label,
		});


		return (
			<Inject
				id="copy-move-modal"
				component="Dialog"
				header={title}
				subtitle={subtitle}
				error={error}
				onHide={this.close}
				actionbar={bar}
				show={show}
				closeButton={false}
			>
				<EditableText
					componentId={PreparationCopyMoveModal.EDITABLE_TEXT_ID}
					text={text}
					onSubmit={(_, { value }) => {
						this.props.setState({ name: value, error: null });
					}}
					onCancel={() => this.props.setState({ error: null })}
				/>
				<hr className="modal-separator" />
				<SelectObject
					source="folders"
					id="folders"
					componentId={PreparationCopyMoveModal.SELECT_OBJECT_ID}
					filterMode={SelectObject.FILTER_MODE.ALL}
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
	t: PropTypes.func,
	...cmfConnect.propTypes,
};

export default translate(I18N.TDP_APP_NAMESPACE)(PreparationCopyMoveModal);
