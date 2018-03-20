/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Bozo the clown directive', () => {
	let scope;
	let createElement;
	let element;

	beforeEach(angular.mock.module('data-prep.easter-eggs'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();
		createElement = () => {
			element = angular.element('<bozo-the-clown></bozo-the-clown>');
			$compile(element)(scope);
			scope.$digest();
			return element;
		};
	}));

	it('should render bozo the clown gif', () => {
		// when
		createElement();

		// then
		expect(element.find('#bozo').attr('src')).toBe('assets/images/bozo-the-clown/bozo-the-clown.gif');
	});
});
