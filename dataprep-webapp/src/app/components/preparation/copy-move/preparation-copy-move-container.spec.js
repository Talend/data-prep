/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation copy/move container', () => {
	let scope;
	let createElement;
	let element;
	let stateMock;
	const body = angular.element('body');

	beforeEach(angular.mock.module('data-prep.preparation-copy-move', ($provide) => {
		stateMock = {
			home: {
				preparations: {
					copyMove: {
						isVisible: true,
						initialFolder: { id: 'L215L2ZvbGRlcg==', path: '/my/folder' },
						preparation: { id: '863a21ab23c66' },
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<preparation-copy-move></preparation-copy-move>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	it('should render inventory copy/move in a modal', inject(($q, FolderService) => {
		// given
		spyOn(FolderService, 'tree').and.returnValue($q.when({ folder: {}, children: [] }));
		
		// when
		createElement();

		// then
		expect(body.find('inventory-copy-move').length).toBe(1);
	}));
});
