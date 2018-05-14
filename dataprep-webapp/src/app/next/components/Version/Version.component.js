import React from 'react';
import PropTypes from 'prop-types';
import Icon from '@talend/react-components';

function Version(props) {
	return (
		<div>
			<p><Icon name="talend-tdp-colored" /></p>
			<dl className="dl-horizontal">
				<dt>VERSION</dt>
				<dd>{props.displayVersion}</dd>
				{
					props.services && props.services.map(s => (
						<div>
							<dt>{s.serviceName}</dt>
							<dd>{`${s.versionId} (${s.buildId})`}</dd>
						</div>
					))
				}
			</dl>
		</div>
	);
}
Version.displayName = 'Version';
Version.propTypes = {
	displayVersion: PropTypes.string.isRequired,
	services: PropTypes.arrayOf(PropTypes.shape()).isRequired,
};

export default Version;
