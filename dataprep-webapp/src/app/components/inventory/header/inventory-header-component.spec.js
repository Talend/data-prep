/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory header component', () => {
	let createElement;
	let scope;
	let element;

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			SORTED_BY: 'Sorted by',
			SORT_IN: 'in',
			SORT_ORDER: 'order',
			NAME_SORT: 'name',
			ASC_ORDER: 'asc',
		});
		$translateProvider.preferredLanguage('en');
	}));
	beforeEach(angular.mock.module('data-prep.inventory-header'));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new(true);
		scope.sortList = [
			{ id: 'name', name: 'NAME_SORT', property: 'name' },
			{ id: 'date', name: 'DATE_SORT', property: 'created' },
		];
		scope.orderList = [
			{ id: 'asc', name: 'ASC_ORDER' },
			{ id: 'desc', name: 'DESC_ORDER' },
		];
		scope.sort = scope.sortList[0];
		scope.order = scope.orderList[0];
		scope.onSortChange = jasmine.createSpy('onSortChange');
		scope.onOrderChange = jasmine.createSpy('onOrderChange');

		createElement = () => {
			const html = `
                <inventory-header
                    sort="sort"
                    order="order"
                    sort-list="sortList"
                    order-list="orderList"
                    on-sort-change="onSortChange(sort)"
                    on-order-change="onOrderChange(order)">
                </inventory-header>
            `;
			element = angular.element(html);
			angular.element('body').append(element);
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	describe('sort control', () => {
		it('should render sort switch', () => {
			//given
			scope.sort = { id: 'name', name: 'NAME_SORT', property: 'name' };
			scope.order = { id: 'asc', name: 'ASC_ORDER' };

			// when
			createElement();

			// then
			expect(element.find('.inventory-sort').text().replace(/[\s]+/g, ' ').trim()).toBe('Sorted by name - order asc');
		});

		it('should call sort change callback on sort switch click', () => {
			//given
			createElement();
			expect(scope.onSortChange).not.toHaveBeenCalled();

			// when
			element.find('talend-button-switch').eq(0).click();

			// then
			expect(scope.onSortChange).toHaveBeenCalledWith(scope.sortList[1]);
		});

		it('should call order change callback on order switch click', () => {
			//given
			createElement();
			expect(scope.onOrderChange).not.toHaveBeenCalled();

			// when
			element.find('talend-button-switch').eq(1).click();

			// then
			expect(scope.onOrderChange).toHaveBeenCalledWith(scope.orderList[1]);
		});
	});
});
