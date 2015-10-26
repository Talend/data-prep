(function() {
    'use strict';

    var playgroundTour = [
        {
            element: '.no-js',
            title: '<center>Welcome to the dataset view</center>',
            content: 'This grid corresponds to a sample extracted from your data.</br>From this view you can prepare the transformations to apply to your data.',
            position: 'right'
        },
        {
            element: '#datagrid .slick-header-columns-right > .slick-header-column',
            title: '',
            content: 'Select a column to discover the transformation actions you can apply to the data.',
            position: 'right'
        },
        {
            element: '#datagrid .quality-bar',
            title: '',
            content: 'Use the bar below to identify:<ul><li> - valid records (in green),</li><li> - empty records (in white)</li><li> - invalid records (in orange).</li></ul>Click one of the record types to perform actions on it.',
            position: 'right'
        },
        {
            element: '#help-suggestions > .actions-suggestions',
            title: '',
            content: 'Click one of the available actions to perform it on the column you selected.</br>Do not worry, you will be able to undo or change it whenever you want.',
            position: 'left'
        },
        {
            element: '#help-stats > .actions-suggestions',
            title: '',
            content: 'In this panel, you will find some basic analysis of your data to help you have a better idea of its content.',
            position: 'left'
        },
        {
            element: 'talend-modal[state="playgroundCtrl.state.playground.visible"] .modal-header-close',
            title: '',
            content: 'Click here to close your dataset.',
            position: 'left'
        }
    ];

    angular.module('data-prep.services.onboarding')
        .constant('playgroundTour', playgroundTour);
})();

