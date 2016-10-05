import React from 'react';
// import AppHeaderBar from 'react-cmf-bootstrap/src/pure/AppHeaderBar';
// import { Nav, NavItem } from 'react-bootstrap';

// export default function AppBar(props) {
const AppBar = () => (
	<div>
		App Bar
	</div>
);

AppBar.propTypes = {
	onOnboardingClick: React.PropTypes.func.isRequired,
	onFeedbackClick: React.PropTypes.func.isRequired,
};

export default AppBar;
