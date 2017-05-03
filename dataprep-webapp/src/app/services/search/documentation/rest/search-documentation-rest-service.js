/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class SearchDocumentationRestService {

	constructor($http, documentationSearchURL) {
		'ngInject';
		this.$http = $http;
		this.documentationSearchURL = documentationSearchURL;
	}

	/**
	 * @ngdoc method
	 * @name search
	 * @methodOf data-prep.services.search.documentation:SearchDocumentationRestService
	 * @description search documentation with keyword
	 */
	search(keyword) {
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
		return this.$http({
			url: this.documentationSearchURL,
			data: parameters,
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
		});
		//return this.$q.when({
		//	data: {"results":[{"mapId":"DFVIXGWDotFtyos~fcjlDw","mapTitle":"Filtering values using charts","occurrences":[{"tocId":"gewkM1x7S_lvP7asuPFggA","readerUrl":"https://talend.fluidtopics.net/#/reader/DFVIXGWDotFtyos%7EfcjlDw/gewkM1x7S_lvP7asuPFggA","breadcrumb":["Filtering values using charts"]}],"contentId":"LF_PtstJ6pBVwwsCbYdCWQ","topicUrl":"https://talend.fluidtopics.net/api/khub/maps/DFVIXGWDotFtyos~fcjlDw/topics/LF_PtstJ6pBVwwsCbYdCWQ","contentUrl":"https://talend.fluidtopics.net/api/khub/maps/DFVIXGWDotFtyos~fcjlDw/topics/LF_PtstJ6pBVwwsCbYdCWQ/content","htmlTitle":"<span class=\"kwicstring\">Filtering values using </span><span class=\"kwicmatch\">charts</span>","htmlExcerpt":"<span class=\"kwicstring\">The </span><span class=\"kwicmatch\">Chart</span><span class=\"kwicstring\"> tab shows a graphical representation of your data. It is also a quick and easy way to apply filter on your data. According to the type of data that you select, the type of graphical representation in the tab will be different: Vertical bar </span><span class=\"kwicmatch\">charts</span><span class=\"kwicstring\"> for numerical data Horizontal bar </span><span class=\"kwicmatch\">charts</span><span class=\"kwictruncate\">...</span>","metadata":[{"key":"hub","label":"hub","values":["notHub"]},{"key":"category","label":"Task","values":["https://talend.poolparty.biz/coretaxonomy/139"]},{"key":"task","label":"Task","values":["Filtering data","Filtering data"]},{"key":"version","label":"Version","values":["2.0"]},{"key":"EnrichVersion","label":"Version","values":["2.0"]},{"key":"dita:id","label":"dita:id","values":["t-filtering_values_using_chart"]},{"key":"dita:ditavalPath","label":"dita:ditavalPath","values":["ft:empty"]},{"key":"dita:topicPath","label":"dita:topicPath","values":["63/development/prepare_data/filter_data/en/t-filtering_values_using_chart.dita"]},{"key":"author","label":"author","values":["nrioseco"]},{"key":"platform","label":"Module","values":["https://talend.poolparty.biz/coretaxonomy/49"]},{"key":"EnrichPlatform","label":"Module","values":["Talend Data Preparation"]},{"key":"dita:mapPath","label":"dita:mapPath","values":["63/development/prepare_data/filter_data/en/dm-filtering_values_using_charts.ditamap"]}],"resources":[{"id":"aLO~g4OOqU7FB9OJrj8mew","filename":"filtering_values_using_charts_1.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/aLO%7Eg4OOqU7FB9OJrj8mew","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/aLO~g4OOqU7FB9OJrj8mew","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/aLO~g4OOqU7FB9OJrj8mew/content"},{"id":"7rk22Pqgjf_879UGEsDQSA","filename":"filtering_values_using_charts_2.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/7rk22Pqgjf_879UGEsDQSA","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/7rk22Pqgjf_879UGEsDQSA","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/7rk22Pqgjf_879UGEsDQSA/content"},{"id":"vOXNf7y0zsEchdefOkVPYQ","filename":"filtering_values_using_charts_3.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/vOXNf7y0zsEchdefOkVPYQ","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/vOXNf7y0zsEchdefOkVPYQ","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/vOXNf7y0zsEchdefOkVPYQ/content"},{"id":"PvAhuD72gYutGilv5jTlKQ","filename":"filtering_values_using_charts_4.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/PvAhuD72gYutGilv5jTlKQ","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/PvAhuD72gYutGilv5jTlKQ","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/PvAhuD72gYutGilv5jTlKQ/content"}]},{"mapId":"OE8Tq35gCVx5zyR9QT5uTg","mapTitle":"Vertical bar chart","occurrences":[{"tocId":"2mUcojCk7xfkfR7Ne4e02g","readerUrl":"https://talend.fluidtopics.net/#/reader/OE8Tq35gCVx5zyR9QT5uTg/2mUcojCk7xfkfR7Ne4e02g","breadcrumb":["Vertical bar chart"]}],"contentId":"1R~KMG0ace7c~CcHAHJWaQ","topicUrl":"https://talend.fluidtopics.net/api/khub/maps/OE8Tq35gCVx5zyR9QT5uTg/topics/1R~KMG0ace7c~CcHAHJWaQ","contentUrl":"https://talend.fluidtopics.net/api/khub/maps/OE8Tq35gCVx5zyR9QT5uTg/topics/1R~KMG0ace7c~CcHAHJWaQ/content","htmlTitle":"<span class=\"kwicstring\">Vertical bar </span><span class=\"kwicmatch\">chart</span>","htmlExcerpt":"<span class=\"kwicstring\">The vertical bar </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\"> is a histogram displayed in the </span><span class=\"kwicmatch\">Chart</span><span class=\"kwicstring\"> tab when the selected column contains numerical or date data. This</span><span class=\"kwictruncate\">...</span><span class=\"kwicstring\">displayed using the range slider. It is an interactive </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\">: you can create a new filter by clicking one of the bars of the </span><span class=\"kwicmatch\">chart</span><span class=\"kwicstring\">. Also, if you point your mouse over one of</span><span class=\"kwictruncate\">...</span>","metadata":[{"key":"platform","label":"Module","values":["https://talend.poolparty.biz/coretaxonomy/49"]},{"key":"EnrichPlatform","label":"Module","values":["Talend Data Preparation"]},{"key":"version","label":"Version","values":["2.0"]},{"key":"EnrichVersion","label":"Version","values":["2.0"]},{"key":"author","label":"author","values":["nrioseco"]},{"key":"dita:topicPath","label":"dita:topicPath","values":["63/development/prepare_data/data_charts/en/c-vertical_bar_chart.dita"]},{"key":"dita:ditavalPath","label":"dita:ditavalPath","values":["ft:empty"]},{"key":"dita:mapPath","label":"dita:mapPath","values":["63/development/prepare_data/data_charts/en/dm-vertical_bar_chart.ditamap"]},{"key":"category","label":"Task","values":["https://talend.poolparty.biz/coretaxonomy/138"]},{"key":"task","label":"Task","values":["Visualizing data"]},{"key":"dita:id","label":"dita:id","values":["c-vertical_bar_chart"]},{"key":"hub","label":"hub","values":["notHub"]}],"resources":[{"id":"hq0A2n~IoFRWOLuQeZ5wXg","filename":"vertical_bar_chart_1.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/hq0A2n%7EIoFRWOLuQeZ5wXg","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/hq0A2n~IoFRWOLuQeZ5wXg","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/hq0A2n~IoFRWOLuQeZ5wXg/content"},{"id":"nqXy7RtjyFuBK~U6uA0Qew","filename":"vertical_bar_chart_2.png","mimeType":"image/png","viewerUrl":"https://talend.fluidtopics.net/#/viewer/attachment/nqXy7RtjyFuBK%7EU6uA0Qew","resourceUrl":"https://talend.fluidtopics.net/api/khub/resources/nqXy7RtjyFuBK~U6uA0Qew","resourceContentUrl":"https://talend.fluidtopics.net/api/khub/resources/nqXy7RtjyFuBK~U6uA0Qew/content"}]}],"paging":{"currentPage":1,"totalResultsCount":9,"isLastPage":false}}
		//});
	}
}

export default SearchDocumentationRestService;
