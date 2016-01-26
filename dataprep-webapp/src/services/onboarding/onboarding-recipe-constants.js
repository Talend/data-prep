(function() {
    'use strict';

    var recipeTour = [
        {
            element: '#help-preparation-name',
            title: '',
            content: 'You can give a name to your brand new preparation.</br>It will be listed in the <b>All Preparations</b> view.',
            position: 'right',
            tooltipPosition: 'right'
        },
        {
            element: '#help-recipe > ul',
            title: '',
            content: 'Here is your recipe. A recipe is literally defined as "a set of directions with a list of ingredients for making or preparing something".</br>In Talend Data Preparation, the ingredients are the raw data, called datasets, and the directions are the set of functions applied to the dataset.</br>Here you can preview, edit, delete, activate or deactivate every function included in the recipe you created.',
            position: 'right'
        },
        {
            element: '#help-history',
            title: '',
            content: 'And don\'t worry, at any time, you can undo or redo your last changes.',
            position: 'left'
        },
	{
            element: '.no-js',
            title: '',
            content: 'Don\'t look for a save button: every change you make is automatically saved.',
            position: 'right'
        }
    ];

    angular.module('data-prep.services.onboarding')
        .constant('recipeTour', recipeTour);
})();
