/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/


describe('AccessError controller', () => {
	'use strict';

	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.access-error'));
	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			ERROR_666_TITLE: 'Accès refusé',
			ERROR_666_MESSAGE: 'Vous n\'êtes pas autorisé à accéder à cette page',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($q, $rootScope, $componentController) => {
		scope = $rootScope.$new();
		createController = () => $componentController(
			'accessError',
			{ $scope: scope },
			{ status: 666 },
		);
	}));

	it('should return the translated title', () => {
		const ctrl = createController();
		expect(ctrl.title).toEqual('Accès refusé');
	});

	it('should return the translated message', () => {
		const ctrl = createController();
		expect(ctrl.message).toEqual('Vous n\'êtes pas autorisé à accéder à cette page');
	});
});
