/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search Documentation Service', () => {

	const searchResults = {
		results: [
			{
				mapId: 'BQeTe_Nh1Je0PGocPxyLRw',
				mapTitle: 'Filtering values using charts',
				occurrences: [
					{
						tocId: 'UcTCE_YnY9J3irxcTPX_VQ',
						readerUrl: 'https://talend-rc.fluidtopics.net/#/reader/BQeTe_Nh1Je0PGocPxyLRw/UcTCE_YnY9J3irxcTPX_VQ',
						breadcrumb: [
							'Filtering values using charts',
						],
					},
				],
				contentId: 'U32fELq1VSICEDFjwK9tJg',
				topicUrl: 'https://talend-rc.fluidtopics.net/api/khub/maps/BQeTe_Nh1Je0PGocPxyLRw/topics/U32fELq1VSICEDFjwK9tJg',
				contentUrl: 'https://talend-rc.fluidtopics.net/api/khub/maps/BQeTe_Nh1Je0PGocPxyLRw/topics/U32fELq1VSICEDFjwK9tJg/content',
				htmlTitle: '<span class=\"kwicstring\">Filtering values using </span><span class=\"kwicmatch\">charts</span>',
				htmlExcerpt: '<span class=\"kwicstring\">The </span><span class=\"kwicmatch\">Chart</span><span class=\"kwicstring\"> tab shows a graphical representation of your data. It is also a quick and easy way to apply filter on your data. According to the type of data that you select, the type of graphical representation in the tab will be different: Vertical bar </span><span class=\"kwicmatch\">charts</span><span class=\"kwicstring\"> for numerical data Horizontal bar </span><span class=\"kwicmatch\">charts</span><span class=\"kwictruncate\">...</span>',
			},
			{
				mapId: 'DLaNYicBDiA9S5hdjFK9LQ',
				mapTitle: 'Vertical bar chart',
				occurrences: [
					{
						tocId: 'pMwTjnd3xR7t~4egfVk3Nw',
						readerUrl: 'https://talend-rc.fluidtopics.net/#/reader/DLaNYicBDiA9S5hdjFK9LQ/pMwTjnd3xR7t%7E4egfVk3Nw',
						breadcrumb: [
							'Vertical bar chart',
						],
					},
				],
				contentId: 'ZgrgZ3OR2ykx1wlL2Jr7JQ',
				topicUrl: 'https://talend-rc.fluidtopics.net/api/khub/maps/DLaNYicBDiA9S5hdjFK9LQ/topics/ZgrgZ3OR2ykx1wlL2Jr7JQ',
				contentUrl: 'https://talend-rc.fluidtopics.net/api/khub/maps/DLaNYicBDiA9S5hdjFK9LQ/topics/ZgrgZ3OR2ykx1wlL2Jr7JQ/content',
				htmlTitle: '<span class=\"kwicstring\">Vertical bar </span><span class=\"kwicmatch\">chart</span>',
				htmlExcerpt: '<span class=\"kwicstring\">The vertical bar </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\"> is a histogram displayed in the </span><span class=\"kwicmatch\">Chart</span><span class=\"kwicstring\"> tab when the selected column contains numerical or date data. This</span><span class=\"kwictruncate\">...</span><span class=\"kwicstring\">displayed using the range slider. It is an interactive </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\">: you can create a new filter by clicking one of the bars of the </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\">. Also, if you point your mouse over one of</span><span class=\"kwictruncate\">...</span>',
			},
		],
	};

	const cleanedSearchResults = [
		{
			inventoryType: 'documentation',
			description: 'The Chart tab shows a graphical representation of your data. It is also a quick and easy way to apply filter on your data. According to the type of data that you select, the type of graphical representation in the tab will be different: Vertical bar charts for numerical data Horizontal bar charts...',
			name: 'Filtering values using charts',
			url: 'https://talend-rc.fluidtopics.net/#/reader/BQeTe_Nh1Je0PGocPxyLRw/UcTCE_YnY9J3irxcTPX_VQ',
			tooltipName: 'Filtering values using charts',
		},
		{
			inventoryType: 'documentation',
			description: 'The vertical bar chart is a histogram displayed in the Chart tab when the selected column contains numerical or date data. This...displayed using the range slider. It is an interactive chart: you can create a new filter by clicking one of the bars of the chart. Also, if you point your mouse over one of...',
			name: 'Vertical bar chart',
			url: 'https://talend-rc.fluidtopics.net/#/reader/DLaNYicBDiA9S5hdjFK9LQ/pMwTjnd3xR7t%7E4egfVk3Nw',
			tooltipName: 'Vertical bar chart',
		},
	];

	beforeEach(angular.mock.module('data-prep.services.search.documentation'));

	beforeEach(inject(($q, SearchDocumentationRestService) => {
		spyOn(SearchDocumentationRestService, 'search').and.returnValue($q.when({ data: searchResults }));
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

	it('should call documentation search rest service and highlight data', inject(($rootScope, SearchDocumentationService) => {
		// given
		let result = null;
		const highlightedResult = [
			{
				inventoryType: 'documentation',
				description: 'The <span class="highlighted">Chart</span> tab shows a graphical representation of your data. It is also a quick and easy way to apply filter on your data. According to the type of data that you select, the type of graphical representation in the tab will be different: Vertical bar <span class="highlighted">chart</span>s for numerical data Horizontal bar <span class="highlighted">chart</span>s...',
				name: 'Filtering values using <span class="highlighted">chart</span>s',
				url: 'https://talend-rc.fluidtopics.net/#/reader/BQeTe_Nh1Je0PGocPxyLRw/UcTCE_YnY9J3irxcTPX_VQ',
				tooltipName: 'Filtering values using charts',
			},
			{
				inventoryType: 'documentation',
				description: 'The vertical bar <span class="highlighted">chart</span> is a histogram displayed in the <span class="highlighted">Chart</span> tab when the selected column contains numerical or date data. This...displayed using the range slider. It is an interactive <span class="highlighted">chart</span>: you can create a new filter by clicking one of the bars of the <span class="highlighted">chart</span>. Also, if you point your mouse over one of...',
				name: 'Vertical bar <span class="highlighted">chart</span>',
				url: 'https://talend-rc.fluidtopics.net/#/reader/DLaNYicBDiA9S5hdjFK9LQ/pMwTjnd3xR7t%7E4egfVk3Nw',
				tooltipName: 'Vertical bar chart',
			},
		];

		// when
		SearchDocumentationService.searchAndHighlight('chart').then(response => result = response);
		$rootScope.$digest();

		// then
		expect(result).toEqual(highlightedResult);
	}));
});
