import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Map } from 'immutable';
import { Dialog, Icon } from '@talend/react-components';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { translate } from 'react-i18next';
import { getDefaultTranslate } from '../../../i18n';

import I18N from './../../constants/i18n';
import './AboutModal.scss';

export const DEFAULT_STATE = new Map({
	show: false,
	expanded: false,
});

export class AboutModal extends React.Component {
	static DISPLAY_NAME = 'Translate(AboutModal)';

	constructor(props) {
		super(props);
		this.close = this.close.bind(this);
		this.toggle = this.toggle.bind(this);
	}

	close() {
		this.props.setState({ show: false });
	}

	toggle() {
		this.props.setState(({ state }) => ({ expanded: !state.get('expanded', false) }));
	}

	render() {
		const { state, t } = this.props;
		const show = state.get('show', false);
		const expanded = state.get('expanded', false);
		const bar = {
			actions: {
				center: [
					{
						actionId: 'help:about:toggle',
						label: expanded ? t('tdp-app:LESS') : t('tdp-app:MORE'),
						onClick: this.toggle,
					},
				],
			},
		};

		return (
			<Inject
				component="Dialog"
				header={t('tdp-app:ABOUT_DIALOG_TITLE')}
				type={Dialog.TYPES.INFORMATIVE}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<Icon name="talend-tdp-colored" className={'about-logo'} />
				<div className="about-excerpt">
					<div>{t('tdp-app:VERSION_NAME', { version: this.props.displayVersion })}</div>
					<div>{t('tdp-app:COPYRIGHT', { year: new Date().getFullYear() })}</div>
				</div>
				{expanded && (
					<table className={'about-versions'}>
						<tr>
							<th>{t('tdp-app:SERVICE_NAME')}</th>
							<th>{t('tdp-app:BUILD_ID')}</th>
							<th>{t('tdp-app:VERSION')}</th>
						</tr>
						{this.props.services.map((service) => {
							const srv = service.toJS();
							return (
								<tr>
									<td>{srv.serviceName}</td>
									<td>{srv.buildId}</td>
									<td>{srv.versionId}</td>
								</tr>
							);
						})}
					</table>
				)}
			</Inject>
		);
	}
}

AboutModal.displayName = 'AboutModal';
AboutModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
	...cmfConnect.propTypes,
};
AboutModal.defaultProps = {
	t: getDefaultTranslate,
};

export default translate(I18N.TDP_APP_NAMESPACE)(AboutModal);
