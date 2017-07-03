/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const preparationTour = [
	{
		element: '#side-panel-nav-preparations',
		title: '<center>Preparations</center>',
		content: 'Here you can browse through and manage the preparations you created.</br>A preparation is the outcome of the different steps applied to cleanse your data.',
		position: 'right',
		tooltipPosition: 'right',
	},
	{
		element: '#side-panel-nav-datasets',
		title: '<center>Datasets</center>',
		content: 'Here you can browse through and manage the datasets you added.</br>A dataset holds the raw data that can be used as raw material without affecting your original data.',
		position: 'right',
		tooltipPosition: 'right',
	},
	{
		element: '#preparations-list-actions-preparation\\:create',
		title: '<center>Add preparation</center>',
		content: 'Click here to add a preparation and start cleansing your data.',
		position: 'right',
		tooltipPosition: 'right',
	},
	{
		element: '#headerbar\\:help',
		title: '<center>Help</center>',
		content: 'Click here to get some help (Online Documentation, Guided tour, About Data Preparation)',
		position: 'left',
	},
];

export default preparationTour;
