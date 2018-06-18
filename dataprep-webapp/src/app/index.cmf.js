import React from 'react';
import { render } from 'react-dom';
import { syncHistoryWithStore } from 'react-router-redux';

import { App } from '@talend/react-cmf';

import configure from './configure.cmf';

function bootstrap() {
	return configure()
		.then(({ browserHistory, store }) => {
			/**
			 * Render the CMF App
			 */
			render(
				<App
					store={store}
					history={syncHistoryWithStore(browserHistory, store)}
				/>,
				document.getElementById('app'),
			);
		});
}

export default bootstrap;
