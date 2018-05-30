import React from 'react';
import PropTypes from 'prop-types';
import { IconsProvider } from '@talend/react-components';
import { Notification, ShortcutManager } from '@talend/react-containers';
import AppLoader from '@talend/react-containers/lib/AppLoader/index';

import { I18nextProvider } from 'react-i18next';

import AboutModal from './AboutModal';
import PreparationCreatorModal from '../../components/preparation-creator/index';

export default function App(props) {
	return (
		<I18nextProvider>
			<AppLoader>
				<IconsProvider />
				<ShortcutManager />
				<Notification />
				<AboutModal />
				<PreparationCreatorModal />
				{props.children}
			</AppLoader>
		</I18nextProvider>
	);
}

App.displayName = 'App';
App.propTypes = {
	children: PropTypes.element.isRequired,
};
