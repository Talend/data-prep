/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

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

	const body = angular.element('body');
	const breadcrumbNoEllipsis = [
		{
			id: 'abcd',
			name: 'HOME'
		},
		{
			id: 'abce',
			name: 'CHARLES'
		}
	];

	const breadcrumb = [
		{
			id: 'abcd',
			name: 'HOME'
		},
		{
			id: 'abce',
			name: 'CHARLES'
		},
		{
			id: 'abcef',
			name: 'folderF'
		},
		{
			id: 'abcefg',
			name: 'folderG'
		},
		{
			id: 'abcefgh',
			name: 'folderH'
		},
		{
			id: 'abcefghi',
			name: 'folderI'
		}
	];
	beforeEach(angular.mock.module('@talend/react-components.containers'));

	beforeEach(inject(($rootScope, $compile, SettingsService) => {
		scope = $rootScope.$new();

		createElement = (breadcrumb) => {
			scope.breadcrumb = breadcrumb;
			element = angular.element('<breadcrumbs items="breadcrumb"></breadcrumbs>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};

		SettingsService.setSettings(settings);
	}));

	afterEach(inject((SettingsService) => {
		SettingsService.clearSettings();
		scope.$destroy();
		element.remove();
	}));

	describe('render', () => {
		it('should create the breadcrumb', () => {
			// when
			createElement(breadcrumbNoEllipsis);

			// then
			const parentFolder = element.find('.breadcrumb > li').eq(0);
			expect(parentFolder.find('button').eq(0).text()).toBe('HOME');
			expect(parentFolder.find('button').eq(0).attr('title')).toBe('HOME');

			const currentFolder = element.find('.breadcrumb > li').eq(2);
			expect(currentFolder.find('span').eq(0).text()).toBe('CHARLES');
		});

		it('should create the breadcrumb with ellipsis', () => {
			// when
			createElement(breadcrumb);

			// then
			const parentFolder = element.find('.breadcrumb > li').eq(0);
			expect(parentFolder.find('button').eq(0).text()).toBe('...');

			const currentFolder = element.find('.breadcrumb > li').eq(2);
			expect(currentFolder.find('button').eq(0).text()).toBe('CHARLES');
		});

		it('should create the dropdown within breadcrumb', () => {
			// when
			createElement(breadcrumb);

			// then
			const dropdownItems = element.find('.breadcrumb .dropdown-menu > li');
			expect(dropdownItems.length).toBe(1); // maxItems = 5
			expect(dropdownItems.eq(0).text()).toBe('HOME');
		});
	});

	describe('onClick', () => {
		beforeEach(inject((SettingsActionsService) => {
			spyOn(SettingsActionsService, 'dispatch').and.returnValue();
		}));

		it('should dispatch button click', inject((SettingsActionsService) => {
			// given
			createElement(breadcrumbNoEllipsis);

			// when
			const parentFolderButton = element.find('.breadcrumb > li').eq(0).find('button');
			parentFolderButton[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@router/GO_FOLDER');
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].payload.id).toBe('abcd');
		}));

		it('should dispatch dropdown item click', inject((SettingsActionsService) => {
			// given
			createElement(breadcrumb);

			// when
			const parentFolderButton = element.find('.breadcrumb .dropdown-menu > li > a').eq(0);
			parentFolderButton[0].click();

			// then
			expect(SettingsActionsService.dispatch).toHaveBeenCalled();
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].type).toBe('@@router/GO_FOLDER');
			expect(SettingsActionsService.dispatch.calls.argsFor(0)[0].payload.id).toBe('abcd');
		}));
	});
});
