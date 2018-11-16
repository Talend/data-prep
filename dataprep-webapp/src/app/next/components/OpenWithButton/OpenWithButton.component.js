import React from 'react';
import { Action } from '@talend/react-components';
import PropTypes from 'prop-types';
import { cmfConnect } from '@talend/react-cmf';
import { translate } from 'react-i18next';
import I18N from '../../constants/i18n';

function OpenWithButton(props) {
	return (
		<Action
			{...props}
		/>
	);
}

OpenWithButton.displayName = 'OpenWithButton';
OpenWithButton.propTypes = {
	t: PropTypes.func,
	...cmfConnect.propTypes,
};

export default translate(I18N.TDP_APP_NAMESPACE)(OpenWithButton);
