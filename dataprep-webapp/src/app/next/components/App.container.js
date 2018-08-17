import React from 'react';
import PropTypes from 'prop-types';
import { List, Map } from 'immutable';
import { IconsProvider } from '@talend/react-components';
import { Notification, ShortcutManager } from '@talend/react-containers';
import AppLoader from '@talend/react-containers/lib/AppLoader/index';

import { I18nextProvider } from 'react-i18next';
import i18n from './../../i18n';
import AboutModal from './AboutModal';
import PreparationCreatorModal from '../../components/preparation-creator/index';

const initialNotificationsState = new Map({
	notifications: new List([]),
});

export default function App(props) {
	return (
		<I18nextProvider i18n={i18n}>
			<AppLoader>
				<div className="tdp">
					<IconsProvider />
					<ShortcutManager view="shortcuts" />
					<Notification initialState={initialNotificationsState} />
					<AboutModal />
					<PreparationCreatorModal />
					{props.children}
				</div>
			</AppLoader>
		</I18nextProvider>
	);
}

App.displayName = 'App';
App.propTypes = {
	children: PropTypes.element.isRequired,
};
