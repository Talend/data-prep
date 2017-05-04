/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Search Documentation Rest Service', () => {
	let $httpBackend;

	beforeEach(angular.mock.module('data-prep.services.search.documentation'));

	beforeEach(inject(($rootScope, $injector) => {
		$httpBackend = $injector.get('$httpBackend');
	}));

	it('should call external documentation rest service ',
		inject(($rootScope, SearchDocumentationRestService, documentationSearchURL) => {
		// given
		const keyword = 'chart';
		const parameters = {
			query: keyword,
			contentLocale: 'en',
			filters: [
				{
					key: 'version',
					values: ['2.0'],
				},
				{
					key: 'EnrichPlatform',
					values: ['Talend Data Preparation'],
				},
			],
			paging: {
				page: 1,
				perPage: 5,
			},
		};
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
		let result = null;
		$httpBackend
			.expectPOST(documentationSearchURL)
			.respond(200, searchResults);

		// when
		SearchDocumentationRestService.search(keyword).then((response) => {
			result = response.data;
		});
		$httpBackend.flush();
		$rootScope.$digest();

		// then
		expect(result).toEqual(searchResults);
	}));
});
