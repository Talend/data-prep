export const LOADING_ROUTE = 'loading';

export const HOME_ROUTE = 'home';
export const HOME_403_ROUTE = 'home.forbidden';
export const HOME_404_ROUTE = 'home.notfound';
export const HOME_PREPARATIONS_ROUTE = 'home.preparations';
export const HOME_DATASETS_ROUTE = 'home.datasets';

export const PLAYGROUND_ROUTE = 'playground';
export const PLAYGROUND_PREPARATION_ROUTE = 'playground.preparation';
export const PLAYGROUND_DATASET_ROUTE = 'playground.dataset';

export const DEFAULT_HOME_URL = '/home/preparations/';

export function routeConfig($stateProvider, $urlRouterProvider) {
	'ngInject';

	$stateProvider
		.state(LOADING_ROUTE, {
			url: '/loading',
			template: '',
			resolve: {
				translateReady($translate) {
					'ngInject';

					return $translate.onReady();
				},
			},
		})
		.state(HOME_ROUTE, {
			abstract: true,
			template: '<home></home>',
			resolve: {
				translateReady($translate) {
					'ngInject';

					return $translate.onReady();
				},
			},
		})
		.state(HOME_404_ROUTE, {
			views: {
				'home-content': { template: '<access-error status="404"></access-error>' },
			},
		})
		.state(HOME_PREPARATIONS_ROUTE, {
			url: '/home/preparations/{folderId}',
			views: {
				'home-content': { template: '<home-preparation></home-preparation>' },
			},
		})
		.state(HOME_DATASETS_ROUTE, {
			url: '/home/datasets',
			views: {
				'home-content': { template: '<home-dataset></home-dataset>' },
			},
		})
		.state(PLAYGROUND_ROUTE, {
			abstract: true,
			url: '/playground',
			template: '<playground></playground>',
			resolve: {
				translateReady($translate) {
					'ngInject';

					return $translate.onReady();
				},
			},
		})
		.state(PLAYGROUND_PREPARATION_ROUTE, { url: '/preparation?prepid&{reload:bool}' })
		.state(PLAYGROUND_DATASET_ROUTE, { url: '/dataset?datasetid' });

	$urlRouterProvider.when('', DEFAULT_HOME_URL);

	$urlRouterProvider.otherwise(($injector, $location) => {
		const state = $injector.get('$state');
		state.go(HOME_404_ROUTE);
		return $location.path();
	});
}

export function routeInterceptor($rootScope, TitleService) {
	'ngInject';

	$rootScope.$on('$stateChangeSuccess', () => {
		TitleService.reset();
	});
}
