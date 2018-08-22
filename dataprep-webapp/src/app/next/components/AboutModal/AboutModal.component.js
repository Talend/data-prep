import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Map } from 'immutable';
import { Dialog, Icon } from '@talend/react-components';
import { cmfConnect, Inject } from '@talend/react-cmf';
import { translate } from 'react-i18next';

import I18N from './../../constants/i18n';

import './AboutModal.scss';

const COPYRIGHT = '© 2018 Talend. All Rights Reserved';

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
		const state = this.props.state;
		const show = state.get('show', false);
		const expanded = state.get('expanded', false);
		const bar = {
			actions: {
				center: [
					{
						actionId: 'help:about:toggle',
						label: expanded ? 'Less' : 'More',
						onClick: this.toggle,
					},
				],
			},
		};


		return (
			<Inject
				component="Dialog"
				header={'About Talend Data Preparation'}
				type={Dialog.TYPES.INFORMATIVE}
				onHide={this.close}
				actionbar={bar}
				show={show}
			>
				<Icon name="talend-tdp-colored" className={'about-logo'} />
				<div>
					<div>Version : {this.props.displayVersion}</div>
					<div>{COPYRIGHT}</div>
				</div>
				{
					expanded && (
						<table className={'services-versions'}>
							<tr>
								<th>SERVICE</th>
								<th>BUILD ID</th>
								<th>VERSION ID</th>
							</tr>
							{
								this.props.services.map((service) => {
									return <TableRow {...service.toJS()} />;
								})
							}
						</table>
					)
				}
			</Inject>
		);
	}
}

function TableRow({ serviceName, buildId, versionId }) {
	return (
		<tr>
			<td>{serviceName}</td>
			<td>{buildId}</td>
			<td>{versionId}</td>
		</tr>
	);
}

TableRow.propTypes = {
	serviceName: PropTypes.string,
	buildId: PropTypes.string,
	versionId: PropTypes.string,
};

AboutModal.displayName = 'AboutModal';
AboutModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
	...cmfConnect.propTypes,
};


export default translate(I18N.TDP_APP_NAMESPACE)(AboutModal);
