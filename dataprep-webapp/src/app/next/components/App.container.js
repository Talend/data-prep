import React from 'react';
import PropTypes from 'prop-types';
import { I18nextProvider } from 'react-i18next';
import { List, Map } from 'immutable';
import { IconsProvider } from '@talend/react-components';
import {
	Notification,
	ShortcutManager,
	AppLoader,
	AboutDialog,
} from '@talend/react-containers';

import { default as components } from './';
import i18n from './../../i18n';

const initialNotificationsState = new Map({
	notifications: new List([]),
});

export default function App(props) {
	return (
		<I18nextProvider i18n={i18n}>
			<AppLoader>
				<div className="tdp">
					<IconsProvider />
					<ShortcutManager />
					<Notification initialState={initialNotificationsState} />
					<AboutDialog />
					<components.PreparationCreatorModal />
					<components.PreparationCopyMoveModal />
					{ props.children }
				</div>
			</AppLoader>
		</I18nextProvider>
	);
}

App.displayName = 'App';
App.propTypes = {
	children: PropTypes.element.isRequired,
};
