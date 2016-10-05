/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const recipeTour = [
	{
		element: '#help-recipe > .recipe',
		title: '<center>Recipe</center>',
		content: 'Here is your recipe. A recipe is literally defined as "a set of directions with a list of ingredients for making or preparing something".</br>In Talend Data Preparation, the ingredients are the raw data, called datasets, and the directions are the set of functions applied to the dataset.</br>Here you can preview, edit, delete, activate or deactivate every function included in the recipe you created.',
		position: 'right',
	},
	{
		element: '#help-history',
		title: '<center>Undo / Redo</center>',
		content: 'Don\'t worry, you can undo or redo your last changes at any time.',
		position: 'left',
	},
	{
		element: '.no-js',
		title: '<center>Save</center>',
		content: 'Don\'t look for a save button: every change you make is automatically saved.',
		position: 'right',
	},
	{
		element: '#export-data',
		title: '<center>Export</center>',
		content: 'Click here to export the result of your preparation.',
		position: 'left',
	},
];

export default recipeTour;
