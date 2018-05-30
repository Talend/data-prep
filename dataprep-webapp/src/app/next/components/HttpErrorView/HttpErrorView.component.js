import React from 'react';
import PropTypes from 'prop-types';
import { Inject } from '@talend/react-cmf';
import { HttpError, Layout } from '@talend/react-components';

import './HttpErrorView.scss'

function HttpErrorView({ status, title, message }) {
	return (
		<Layout
			hasTheme
			header={<Inject component="HeaderBar" />}
			one={<Inject component="SidePanel" />}
			mode="TwoColumns"
		>
			<div className="http-error-container-style">
				<HttpError {...{ status, title, message }} />
			</div>
		</Layout>
	);
}

HttpErrorView.displayName = 'HttpErrorView';
HttpErrorView.propTypes = {
	status: PropTypes.number,
	title: PropTypes.string,
	message: PropTypes.string,
};

export default HttpErrorView;
