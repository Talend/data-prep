/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder creator container', () => {
	let scope;
	let createElement;
	let element;
	let stateMock;
	const body = angular.element('body');

	beforeEach(angular.mock.module('data-prep.folder-creator', ($provide) => {
		stateMock = {
			home: {
				folders: {
					creator: {
						isVisible: false,
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<folder-creator></folder-creator>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	it('should render folder creator form in a modal', () => {
		// given
		expect(body.find('folder-creator-form').length).toBe(0);
		
		// when
		createElement();

		// then
		expect(body.find('folder-creator-form').length).toBe(0);
		
		// when
		stateMock.home.folders.creator.isVisible = true;
		scope.$digest();
		
		// then
		expect(body.find('folder-creator-form').length).toBe(1);
	});
});
