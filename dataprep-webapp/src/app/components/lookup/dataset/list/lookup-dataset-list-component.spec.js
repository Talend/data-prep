/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
describe('Lookup DatasetList component', () => {

	let scope;
	let createElement;
	let element;
	const datasets = [
		{
			id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
			model: {
				id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
				name: 'US States',
				author: 'anonymousUser',
				created: '1437020219741',
				type: 'text/csv',
				certificationStep: 'NONE',
				preparations: [{ name: 'US States prepa' }, { name: 'US States prepa 2' }],
				owner: {
					displayName: 'anonymousUser',
				},
			}
		},
		{
			id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
			model: {
				id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
				name: 'Customers',
				author: 'anonymousUser',
				created: '143702021974',
				type: 'application/vnd.ms-excel',
				certificationStep: 'PENDING',
				preparations: [{ name: 'Customers prepa' }],
				owner: {
					displayName: 'anonymousUser',
				},
			}
		},
		{
			id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a', model: {
			id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a',
			name: 'Customers 2',
			author: 'anonymousUser',
			created: '14370202197',
			certificationStep: 'CERTIFIED',
			preparations: [],
			owner: {
				displayName: 'anonymousUser',
			},
		}
		},
	];

	beforeEach(angular.mock.module('data-prep.lookup'));

	beforeEach(inject(($q, $rootScope, $compile) => {
		scope = $rootScope.$new();
		scope.datasets = datasets;
		scope.searchText = '';

		createElement = () => {
			element = angular.element('<lookup-dataset-list datasets="datasets"' +
				'search-text="searchText"></lookup-dataset-list>');
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});

	it('should render add dataset checkbox', inject(() => {
		// when
		createElement();
		// then
		expect(element.find('.add-dataset-input').length).toBe(3);
	}));

	it('should render dataset list', inject(() => {

		// when
		createElement();

		// then
		expect(element.find('inventory-item').length).toBe(3);
	}));

});
