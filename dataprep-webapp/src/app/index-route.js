export const OLD_NAV_ROUTE = 'nav';
export const OLD_INDEX_ROUTE = 'nav.index';
export const OLD_INDEX_DATASETS_ROUTE = 'nav.index.datasets';
export const OLD_INDEX_PREPARATIONS_ROUTE = 'nav.index.preparations';

export const LOADING_ROUTE = 'loading';

export const HOME_ROUTE = 'home';
export const HOME_PREPARATIONS_ROUTE = 'home.preparations';
export const HOME_DATASETS_ROUTE = 'home.datasets';

export const PLAYGROUND_ROUTE = 'playground';
export const PLAYGROUND_PREPARATION_ROUTE = 'playground.preparation';
export const PLAYGROUND_DATASET_ROUTE = 'playground.dataset';

export const DEFAULT_HOME_URL = '/home/preparations/';

export default ($stateProvider, $urlRouterProvider) => {
	'ngInject';

	$stateProvider
		.state(OLD_NAV_ROUTE, {
			abstract: true,
			template: '<navbar></navbar>',
		})
		.state(OLD_INDEX_ROUTE, {
			abstract: true,
			url: '/index',
			template: '<home></home>',
		})
		.state(OLD_INDEX_DATASETS_ROUTE, {
			url: '/datasets',
			views: {
				'home-content': { template: '<home-dataset></home-dataset>' },
			},
		})
		.state(OLD_INDEX_PREPARATIONS_ROUTE, {
			url: '/preparations/{folderId}',
			views: {
				'home-content': { template: '<home-preparation></home-preparation>' },
			},
		})
		.state(LOADING_ROUTE, {
			url: '/loading',
			template: '',
		})
		.state(HOME_ROUTE, {
			abstract: true,
			url: '/home',
			template: '<react-home></react-home>',
		})
		.state(HOME_PREPARATIONS_ROUTE, {
			url: '/preparations/{folderId}',
			views: {
				'home-content': { template: '<react-home-preparation></react-home-preparation>' },
			},
		})
		.state(HOME_DATASETS_ROUTE, {
			url: '/datasets',
			views: {
				'home-content': { template: '<react-home-dataset></react-home-dataset>' },
			},
		})
		.state(PLAYGROUND_ROUTE, {
			abstract: true,
			url: '/playground',
			template: '<playground></playground>',
		})
		.state(PLAYGROUND_PREPARATION_ROUTE, { url: '/preparation?prepid&{reload:bool}' })
		.state(PLAYGROUND_DATASET_ROUTE, { url: '/dataset?datasetid' });

	$urlRouterProvider.otherwise(DEFAULT_HOME_URL);
};
