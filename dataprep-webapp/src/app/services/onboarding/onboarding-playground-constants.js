/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const playgroundTour = [
	{
		element: '.no-js',
		title: '<center>Welcome to the preparation view</center>',
		content: 'In this view, you can apply preparation steps to your dataset.</br>This table represents the result of your preparation.',
		position: 'right',
	},
	{
		element: '#datagrid .slick-header-columns-right > .slick-header-column',
		title: '<center>Columns</center>',
		content: 'Select a column to discover the transformation functions you can apply to your data.',
		position: 'right',
	},
	{
		element: '#datagrid .quality-bar',
		title: '<center>Quality bar</center>',
		content: 'Use this quality bar to identify and filter:<ul><li> - valid records (in green),</li><li> - empty records (in white),</li><li> - invalid records (in orange).</li></ul>Click one of the record types to apply functions on it.',
		position: 'right',
	},
	{
		element: '#help-suggestions',
		title: '<center>Functions</center>',
		content: 'Click one of the available functions to apply it on the column you selected.',
		position: 'left',
	},
	{
		element: '#help-stats',
		title: '<center>Data profiling</center>',
		content: 'Click the profiled data to create a filter.<br/>This panel gives you some analysis of your data to help you have a better idea of its content.',
		position: 'left',
	},
	{
		element: '#playground-lookup-icon',
		title: '<center>Data lookup</center>',
		content: 'Click here to link a dataset to your preparation. It will help you to dynamically use the data from a second dataset to complement your preparation.</br>For example, you can use it to add all US State abbreviations alongside the full name of the State.',
		position: 'bottom',
	},
];

export default playgroundTour;
