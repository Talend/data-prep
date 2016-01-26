(function() {
    'use strict';

    var playgroundTour = [
        {
            element: '.no-js',
            title: '<center>Welcome to the dataset view</center>',
            content: 'This table represents the raw data contained in your dataset.</br>From this view, you can prepare the modifications to apply on your dataset.',
            position: 'right'
        },
        {
            element: '#datagrid .slick-header-columns-right > .slick-header-column',
            title: '',
            content: 'Select a column to discover the transformation functions you can apply to your data.',
            position: 'right'
        },
        {
            element: '#datagrid .quality-bar',
            title: '',
            content: 'Use this quality bar to identify:<ul><li> - valid records (in green),</li><li> - empty records (in white)</li><li> - invalid records (in orange).</li></ul>Click one of the record types to apply functions on it.',
            position: 'right'
        },
        {
            element: '#help-suggestions > .actions-suggestions',
            title: '',
            content: 'Click one of the available functions to perform it on the column you selected.</br>Don\'t worry, you will be able to undo or change it whenever you want.',
            position: 'left'
        },
        {
            element: '#help-stats > .actions-suggestions',
            title: '',
            content: 'In this panel, you will find some basic analysis of your data to help you have a better idea of its content.',
            position: 'left'
        },
        {
            element: '#playground-lookup-icon',
            title: '',
            content: 'Click here to link two datasets. It will help you to dynamically use the data from the second dataset to complement the main one.</br>For example, you can use it to add all US State abbreviations alongside the full name of the State.',
            position: 'bottom'
        }
    ];

    angular.module('data-prep.services.onboarding')
        .constant('playgroundTour', playgroundTour);
})();

