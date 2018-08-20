import React from 'react';
import PropTypes from 'prop-types';
import ImmutablePropTypes from 'react-immutable-proptypes';
import { Dialog, Icon } from '@talend/react-components';
import { cmfConnect, Inject } from '@talend/react-cmf';

import './AboutModal.scss';

const COPYRIGHT = '© 2018 Talend. All Rights Reserved';


export default class AboutModal extends React.Component {
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
		const services = [];
		const displayVersion = 'TEST';
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
					<div>Version : {displayVersion}</div>
					<div>{COPYRIGHT}</div>
				</div>
				{
					expanded && (
						services.map(service => (
							<div>
								<dt>{service.serviceName}</dt>
								<dd>{`${service.versionId} (${service.buildId})`}</dd>
							</div>
						))
					)
				}
			</Inject>
		);
	}
}
AboutModal.displayName = 'AboutModal';
AboutModal.propTypes = {
	state: ImmutablePropTypes.contains({ show: PropTypes.bool }).isRequired,
	setState: PropTypes.func.isRequired,
	...cmfConnect.INJECTED_PROPS,
};
