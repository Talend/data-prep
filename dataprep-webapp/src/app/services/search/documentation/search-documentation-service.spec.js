/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import docSearchResults from '../../../../mocks/Documentation.mock';

describe('Search Documentation Service', () => {
	const cleanedSearchResults = [
		{
			inventoryType: 'documentation',
			description: 'The Chart tab shows a graphical representation of your data. It is also a quick and easy way to apply filter on your data. According to the type of data that you select, the type of graphical representation in the tab will be different: Vertical bar charts for numerical data Horizontal bar charts...',
			name: 'Filtering values using charts',
			url: 'https://help.talend.com/reader/BQeTe_Nh1Je0PGocPxyLRw/UcTCE_YnY9J3irxcTPX_VQ',
			tooltipName: 'Filtering values using charts',
		},
		{
			inventoryType: 'documentation',
			description: 'The vertical bar chart is a histogram displayed in the Chart tab when the selected column contains numerical or date data. This...displayed using the range slider. It is an interactive chart: you can create a new filter by clicking one of the bars of the chart. Also, if you point your mouse over one of...',
			name: 'Vertical bar chart',
			url: 'https://help.talend.com/reader/DLaNYicBDiA9S5hdjFK9LQ/pMwTjnd3xR7t%7E4egfVk3Nw',
			tooltipName: 'Vertical bar chart',
		},
		{
			inventoryType: 'documentation',
			description: '< Row > Main link...chart',
			name: ' Row > Main link...chart',
			url: 'https://help.talend.com/reader/DLaNYicBDiA9S5hdjFK9LQ/DLaNYicBDiA9S5hdjFK9AA',
			tooltipName: ' Row > Main link...chart',
		},
	];

	beforeEach(angular.mock.module('data-prep.services.search.documentation'));

	describe('success', () => {
		beforeEach(inject(($q, SearchDocumentationRestService) => {
			spyOn(SearchDocumentationRestService, 'search').and.returnValue($q.when({ data: docSearchResults }));
		}));

		it('should call documentation search rest service and process data', inject(($rootScope, SearchDocumentationService) => {
			// given
			let result = null;

			// when
			SearchDocumentationService.search('chart').then(response => result = response);
			$rootScope.$digest();

			// then
			expect(result).toEqual(cleanedSearchResults);
		}));
	});

	describe('failure', () =>{
		beforeEach(inject(($q, SearchDocumentationRestService) => {
			spyOn(SearchDocumentationRestService, 'search').and.returnValue($q.reject());
		}));

		it('should return empty array', inject(($rootScope, SearchDocumentationService) => {
			// given
			let result = null;

			// when
			SearchDocumentationService.search('chart').then(response => result = response);
			$rootScope.$digest();

			// then
			expect(result).toEqual([]);
		}));
	});
});
