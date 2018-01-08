/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Button Dropdown directive', () => {

	let scope;
	let element;
	let createElement;

	beforeEach(angular.mock.module('talend.widget'));

	afterEach(() => {
		scope.$destroy();
		element.remove();
		jasmine.clock().uninstall();
	});

	beforeEach(inject(($rootScope, $compile, $timeout) => {
		jasmine.clock().install();
		scope = $rootScope.$new();

		createElement = () => {
			scope.buttonAction = jasmine.createSpy('buttonAction');
			element = angular.element(`
                <talend-button-dropdown button-icon="m" 
                                        button-text="Click Me" 
                                        button-action="buttonAction()" 
                                        button-title="test"
                                        dropdown-menu-direction="{{dropdownMenuDirection}}">
                   <li>Menu 1</li>
                   <li>Menu 2</li>
                </talend-button-dropdown>
            `);
			$compile(element)(scope);
			$timeout.flush();
			scope.$digest();
		};
	}));

	it('should call action on main button click', () => {
		// given
		createElement();

		// when
		element.find('.button-dropdown-main').eq(0).click();

		// then
		expect(scope.buttonAction).toHaveBeenCalled();
	});

	it('should render button title', () => {
		// when
		createElement();

		// then
		const button = element.find('.btn').eq(0);
		expect(button[0].title).toBe('test');
	});

	it('should render dropdown menu at right by default', () => {
		// when
		createElement();

		// then
		const dropdownMenu = element.find('.dropdown-menu').eq(0);
		expect(dropdownMenu.hasClass('dropdown-menu-right')).toBeTruthy();
		expect(dropdownMenu.hasClass('dropdown-menu-left')).toBeFalsy();
	});

	it('should render dropdown menu at right', () => {
		// given
		scope.dropdownMenuDirection = 'right';

		// when
		createElement();

		// then
		const dropdownMenu = element.find('.dropdown-menu').eq(0);
		expect(dropdownMenu.hasClass('dropdown-menu-right')).toBeTruthy();
		expect(dropdownMenu.hasClass('dropdown-menu-left')).toBeFalsy();
	});

	it('should render dropdown menu at left', () => {
		// given
		scope.dropdownMenuDirection = 'left';

		// when
		createElement();

		// then
		const dropdownMenu = element.find('.dropdown-menu').eq(0);
		expect(dropdownMenu.hasClass('dropdown-menu-right')).toBeFalsy();
		expect(dropdownMenu.hasClass('dropdown-menu-left')).toBeTruthy();
	});
});
