import { api, sagaRouter } from '@talend/react-cmf';
import { registerAllContainers } from '@talend/react-containers/lib/register';
import dataset from '@talend/dataset';
import rating from '@talend/rating';
import { all, call, fork } from 'redux-saga/effects';

import { FETCH_PREPARATIONS } from './constants';
import actions from './actions';
import sagas from './saga';

import App from './components/App.container';


const registerActionCreator = api.action.registerActionCreator;
const registerComponent = api.route.registerComponent;
const registerRouteFunction = api.route.registerFunction;

export default {
	initialize() {
		dataset.configure();
		rating.configure();

		/**
		 * Register components in CMF Components dictionary
		 */
		registerAllContainers();
		registerComponent('App', App);

		/**
		 * Register route functions
		 */
		registerRouteFunction('preparation:fetch', ({ router, dispatch }) => dispatch({
			type: FETCH_PREPARATIONS,
			folderId: router.nextState.params.folderId,
		}));

		/**
		 * Register action creators in CMF Actions dictionary
		 */
		registerActionCreator('preparation:fetchAll', actions.preparation.fetchAll);
		registerActionCreator('preparation:duplicate', actions.preparation.duplicate);
		registerActionCreator('preparation:edit:submit', actions.preparation.rename);
		registerActionCreator('preparation:edit:cancel', actions.preparation.cancelRename);
		registerActionCreator('preparation:open', actions.preparation.openPreparation);
		registerActionCreator('preparation:rename', actions.preparation.setTitleEditionMode);
		registerActionCreator('preparation:add:open', actions.preparation.openCreator);

		registerActionCreator('help:about:open', actions.help.openAbout);
		registerActionCreator('help:tour', () => ({ type: 'ALERT', payload: 'help:tour' }));
		registerActionCreator('help:feedback:open', () => ({ type: 'ALERT', payload: 'help:feedback:open' }));

		registerActionCreator('headerbar:search:start', actions.search.search);

		registerActionCreator('redirect', actions.redirect);
	},

	runSagas(sagaMiddleware, history) {
		function* rootSaga() {
			yield all([
				fork(sagaRouter, history, {}),
				...sagas.help.map(call),
				...sagas.search.map(call),
				...sagas.preparation.map(call),
			]);
		}
		sagaMiddleware.run(rootSaga);
	},
};
