/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';
import settings from '../../../../mocks/Settings.mock';

describe('Breadcrumb container', () => {
	let scope;
	let createElement;
	let element;

	const exportFullRun = {
		header: [
			{
				displayMode: 'status',
				status: 'successful',
				label: 'Successful',
				icon: 'talend-check',
				actions: [],
			}
		],
		content: [],
	};
	beforeEach(angular.mock.module('react-talend-components.containers'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();

		createElement = () => {
			scope.exportFullRun = exportFullRun;
			element = angular.element('<collapsible-panel item="exportFullRun"></collapsible-panel>');
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	describe('render', () => {
		it('should create the collapsible-panel', () => {
			//given
			createElement();

			//then
			expect(element.find('.panel-heading').length).toBe(1);
			expect(element.find('.tc-status').length).toBe(1);
		});
	});
});
