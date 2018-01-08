/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import {
	HOME_403_ROUTE,
	HOME_404_ROUTE,
	HOME_DATASETS_ROUTE,
	HOME_PREPARATIONS_ROUTE,
} from '../../index-route';

import i18n from './../../../i18n/en.json';

describe('Onboarding service', () => {
	let stateMock;

	beforeEach(angular.mock.module('data-prep.services.onboarding', ($provide) => {
		stateMock = {
			inventory: {
				homeFolder: { id: 'Lw=='},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', i18n);
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($state) => {
		spyOn($state, 'go').and.returnValue();
	}));

	it('should return true when tour has not been completed yet', inject((OnboardingService, StorageService) => {
		// given
		spyOn(StorageService, 'getTourOptions').and.returnValue({ preparation: false });

		// when
		const result = OnboardingService.shouldStartTour('preparation');

		// then
		expect(result).toBe(true);
	}));

	it('should return false when tour has already been completed', inject((OnboardingService, StorageService) => {
		// given
		spyOn(StorageService, 'getTourOptions').and.returnValue({ preparation: true });

		// when
		const result = OnboardingService.shouldStartTour('preparation');

		// then
		expect(result).toBe(false);
	}));

	it('should configure intro.js options', inject(($timeout, OnboardingService) => {
		// given
		expect(OnboardingService.currentTour).toBeFalsy();

		// when
		OnboardingService.startTour('preparation');
		$timeout.flush(200);

		// then
		const options = OnboardingService.currentTour._options;
		expect(options.nextLabel).toBe('Next');
		expect(options.prevLabel).toBe('Back');
		expect(options.skipLabel).toBe('Skip');
		expect(options.doneLabel).toBe('Let me try');
	}));

	it('should create/adapt preparation tour step', inject(($timeout, OnboardingService) => {
		// given
		expect(OnboardingService.currentTour).toBeFalsy();

		// when
		OnboardingService.startTour('preparation');
		$timeout.flush(200);

		// then
		const options = OnboardingService.currentTour._options;
		expect(options.steps[0]).toEqual({
			element: '#side-panel-nav-menu-preparations',
			position: 'right',
			intro: '<div class="introjs-tooltiptitle">Preparations</div><div class="introjs-tooltipcontent">Here you can browse through and manage the preparations you created.</br>A preparation is the outcome of the different steps applied to cleanse your data.</div>',
		});
	}));

	it('should create/adapt playground step', inject(($timeout, OnboardingService) => {
		// given
		expect(OnboardingService.currentTour).toBeFalsy();

		// when
		OnboardingService.startTour('playground');
		$timeout.flush(200);

		// then
		const options = OnboardingService.currentTour._options;
		expect(options.steps[0]).toEqual({
			element: '.no-js',
			position: 'right',
			intro: '<div class="introjs-tooltiptitle">Welcome to the preparation view</div><div class="introjs-tooltipcontent">In this view, you can apply preparation steps to your dataset.</br>This table represents the result of your preparation.</div>',
		});
	}));

	it('should create/adapt column selection', inject(($timeout, OnboardingService) => {
		// given
		expect(OnboardingService.currentTour).toBeFalsy();

		// when
		OnboardingService.startTour('playground');
		$timeout.flush(200);

		// then
		const options = OnboardingService.currentTour._options;
		expect(options.steps[1]).toEqual({
			element: '#datagrid .slick-header-columns-right > .slick-header-column',
			position: 'right',
			intro: '<div class="introjs-tooltiptitle">Columns</div><div class="introjs-tooltipcontent">Select a column to discover the transformation functions you can apply to your data.</div>',
		});
	}));

	it('should create/adapt recipe tour step', inject(($timeout, OnboardingService) => {
		// given
		expect(OnboardingService.currentTour).toBeFalsy();

		// when
		OnboardingService.startTour('recipe');
		$timeout.flush(200);

		// then
		const options = OnboardingService.currentTour._options;
		expect(options.steps[0]).toEqual({
			element: '#help-recipe > .recipe',
			position: 'right',
			intro: '<div class="introjs-tooltiptitle">Recipe</div><div class="introjs-tooltipcontent">Here is your recipe. A recipe is literally defined as "a set of directions with a list of ingredients for making or preparing something".</br>In Talend Data Preparation, the ingredients are the raw data, called datasets, and the directions are the set of functions applied to the dataset.</br>Here you can preview, edit, delete, activate or deactivate every function included in the recipe you created.</div>',
		});
	}));

	it('should save "preparation" state in localstorage on tour complete', inject(($timeout, OnboardingService, StorageService) => {
		// given
		spyOn(StorageService, 'setTourOptions');

		expect(OnboardingService.currentTour).toBeFalsy();
		OnboardingService.startTour('preparation');
		$timeout.flush(200);

		const oncomplete = OnboardingService.currentTour._introCompleteCallback;

		// when
		oncomplete();

		// then
		expect(StorageService.setTourOptions).toHaveBeenCalledWith({ preparation: true });
	}));

	it('should save "preparation" state in localstorage on tour exit', inject(($timeout, OnboardingService, StorageService) => {
		// given
		spyOn(StorageService, 'setTourOptions');
		expect(OnboardingService.currentTour).toBeFalsy();
		OnboardingService.startTour('preparation');
		$timeout.flush(200);
		const onexit = OnboardingService.currentTour._introExitCallback;

		// when
		onexit();

		// then
		expect(StorageService.setTourOptions).toHaveBeenCalledWith({ preparation: true });
	}));

	it('should not start onboarding when 403', inject(($state, OnboardingService) => {
		// given
		$state.current = {
			name: HOME_403_ROUTE,
		};

		// when
		OnboardingService.startTour('preparation');

		// then
		expect($state.go).not.toHaveBeenCalled();
	}));

	it('should redirect to "preparations" before starting onboarding', inject(($state, OnboardingService) => {
		// given
		$state.current = {
			name: HOME_DATASETS_ROUTE,
		};

		// when
		OnboardingService.startTour('preparation');

		// then
		expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, { folderId: stateMock.inventory.homeFolder.id });
	}));

	it('should redirect BACK to "datasets" after redirecting to "preparations" ', inject(($timeout, $state, OnboardingService) => {
		// given
		$state.current = {
			name: HOME_DATASETS_ROUTE,
		};

		expect(OnboardingService.currentTour).toBeFalsy();
		OnboardingService.startTour('preparation');
		expect($state.go).toHaveBeenCalledWith(HOME_PREPARATIONS_ROUTE, { folderId: stateMock.inventory.homeFolder.id });

		// when
		$timeout.flush(200);
		const onexit = OnboardingService.currentTour._introExitCallback;

		// when
		onexit();

		// then
		expect($state.go).toHaveBeenCalledWith(HOME_DATASETS_ROUTE);
	}));
});
