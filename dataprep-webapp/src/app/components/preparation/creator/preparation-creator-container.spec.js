/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation creator container', () => {
	let scope;
	let createElement;
	let element;
	let stateMock;
	const body = angular.element('body');

	beforeEach(angular.mock.module('data-prep.preparation-creator', ($provide) => {
		stateMock = {
			home: {
				preparations: {
					creator: {
						isVisible: true,
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		createElement = () => {
			element = angular.element('<preparation-creator></preparation-creator>');
			body.append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(inject(() => {
		scope.$destroy();
		element.remove();
	}));

	it('should render preparation creator form in a modal', inject(($q, DatasetService) => {
		// given
		spyOn(DatasetService, 'getFilteredDatasets').and.returnValue($q.when())
		
		//when
		createElement();

		//then
		expect(body.find('preparation-creator-form').length).toBe(1);
	}));
});
