import api, { store as cmfstore, sagaRouter, actions as cmfActions } from '@talend/react-cmf';
import reduxLocalStorage from '@talend/react-cmf/lib/reduxstorage/reduxLocalStorage';
import { registerAllContainers } from '@talend/react-containers/lib/register';
import dataset from '@talend/dataset';
import { browserHistory } from 'react-router';
import { routerMiddleware } from 'react-router-redux';
import createSagaMiddleware from 'redux-saga';
import { all, call, fork } from 'redux-saga/effects';
import actions from './next/actions';
import components from './next/components/index';
import App from './next/components/App.container';
import { ALERT } from './next/constants/actions';
import { default as constants } from './next/constants';
import sagas from './next/sagas/watchers';


const registerActionCreator = api.actionCreator.register;
const registerComponent = api.component.register;
const registerComponents = api.component.registerMany;
const registerExpressions = api.expression.registerMany;
const registerRouteFunction = api.route.registerFunction;

/**
 * Initialize CMF configuration
 * - Register your components in the CMF dictionary
 * - Register action creators in CMF actions dictionary
 */
export default function initialize(additionalConfiguration = {}) {
	// register all saga api
	api.registry.addToRegistry(
		'SEARCH_CATEGORIES_BY_PROVIDER',
		constants.search.SEARCH_CATEGORIES_BY_PROVIDER,
	);

	const rootSagas = [
		fork(sagaRouter, browserHistory, {}),
		fork(api.sagas.component.handle),
	];
	const rootSagasToStart = {
		...sagas.help,
		...sagas.http,
		...sagas.search,
		...sagas.preparation,
		...sagas.redirect,
	};
	const additionalRootSagas = additionalConfiguration.rootSagas;
	if (additionalRootSagas) {
		Object.assign(rootSagasToStart, additionalRootSagas);
	}
	Object.keys(rootSagasToStart).forEach((rootSagaToStartName) => {
		rootSagas.push(call(rootSagasToStart[rootSagaToStartName]));
	});

	api.saga.registerMany(sagas.bootstrap);
	api.saga.registerMany(sagas.preparation);
	// Use for EE additional configuration
	const additionalManySagas = additionalConfiguration.manySagas;
	if (additionalManySagas) {
		additionalManySagas.forEach((additionalManySaga) => {
			api.saga.registerMany(additionalManySaga);
		});
	}

	function* rootSaga() {
		yield all(rootSagas);
	}

	function appFactory(storage = {}) {
		const { initialState, engine } = storage;

		const preReducers = [dataset.preReducers.notificationReducer, ...dataset.hors];
		const additionalPreReducers = additionalConfiguration.preReducers;
		if (additionalPreReducers) {
			preReducers.push(...additionalPreReducers);
		}
		cmfstore.addPreReducer(preReducers);

		/**
		 * Register react-router-redux router reducer (see https://github.com/reactjs/react-router-redux)
		 */
		cmfstore.setRouterMiddleware(routerMiddleware(browserHistory));

		/**
		 * Register your app reducers
		 */
		const sagaMiddleware = createSagaMiddleware();
		const store = cmfstore.initialize(undefined, initialState, undefined, [sagaMiddleware]);
		sagaMiddleware.run(rootSaga);

		api.registerInternals();

		/**
		 * Register route functions
		 */
		const additionalRouteFunctions = additionalConfiguration.routeFunctions;
		if (additionalRouteFunctions) {
			Object.keys(additionalRouteFunctions).map(k =>
				registerRouteFunction(k, additionalRouteFunctions[k]),
			);
		}

		registerAllContainers();

		dataset.configure();

		/**
		 * Register expressions in CMF expressions dictionary
		 */
		registerExpressions(api.expressions);
		const additionalExpressions = additionalConfiguration.expressions;
		if (additionalExpressions) {
			registerExpressions(additionalExpressions);
		}

		/**
		 * Register components in CMF Components dictionary
		 */
		registerComponent('App', App);
		registerComponents(components);
		const additionalComponents = additionalConfiguration.components;
		if (additionalComponents) {
			registerComponents(additionalComponents);
		}

		/**
		 * Register action creators in CMF Actions dictionary
		 */
		registerActionCreator('preparation:duplicate', actions.preparation.duplicate);
		registerActionCreator('preparation:edit:submit', actions.preparation.rename);
		registerActionCreator('preparation:edit:cancel', actions.preparation.cancelRename);
		registerActionCreator('preparation:open', actions.preparation.open);
		registerActionCreator('folder:open', actions.folder.open);
		registerActionCreator('preparation:fetch', actions.preparation.fetch);
		registerActionCreator('preparation:rename', actions.preparation.setTitleEditionMode);
		registerActionCreator('preparation:add:open', actions.preparation.openCreator);
		registerActionCreator('help:tour', () => ({ type: ALERT, payload: 'help:tour' }));
		registerActionCreator('help:feedback:open', () => ({ type: ALERT, payload: 'help:feedback:open' }));
		registerActionCreator('redirect', actions.redirect);
		registerActionCreator('version:fetch', actions.version.fetch);
		registerActionCreator('headerbar:search:start', actions.search.start);
		registerActionCreator('headerbar:search:select', actions.search.select);
		registerActionCreator('headerbar:search:reset', actions.search.reset);

		const additionalActionCreators = additionalConfiguration.actionCreators;
		if (additionalActionCreators) {
			Object.keys(additionalActionCreators).map(k =>
				registerActionCreator(k, additionalActionCreators[k]),
			);
		}

		/**
		 * Fetch the CMF settings and configure the CMF app
		 */
		store.dispatch(
			cmfActions.settingsActions.fetchSettings(`/settings.${constants.I18N.DEFAULT_LOCALE}.json`),
		);

		reduxLocalStorage.saveOnReload({
			engine,
			store,
		});

		const additionalCallback = additionalConfiguration.callback;
		if (additionalCallback) {
			additionalCallback();
		}

		return {
			store,
			browserHistory,
		};
	}

	const additionalLocalStorage = additionalConfiguration.localStorage || {};
	return reduxLocalStorage
		.loadInitialState({
			key: additionalLocalStorage.key || 'data-prep',
			whitelist: [
				['cmf', 'components', 'Container(SidePanel)'],
				['cmf', 'components', 'Container(List)', 'preparations'],
				['cmf', 'components', 'Container(List)', 'datasets'],
				['cmf', 'components', 'Container(List)', 'datastores'],
			].concat(additionalLocalStorage.whitelist || []),
		})
		.then(
			storage => appFactory(storage),
			(error) => {
				console.error(error); // eslint-disable-line no-console
				return appFactory();
			},
		);
}
