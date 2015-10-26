(function() {
    'use strict';

    var datasetTour = [
        {
            element: '.no-js',
            title: '<center>Welcome to</br>Talend Data Preparation</center>',
            content: 'To quickly learn how to use it, click <b>Next</b>.',
            position: 'right'
        },
        {
            element: '#nav_home_datasets',
            title: '',
            content: 'Here you can browse through and manage the datasets you created.<br/>A dataset corresponds to a sample you can play with, without modifying your original file.',
            position: 'right'
        },
        {
            element: '#dataset_0',
            title: '',
            content: 'Here you can find some ready-to-use datasets to get familiar with Data Prep.',
            position: 'bottom'
        },
        {
            element: '#help-import-local',
            title: '',
            content: 'Here you can create your own datasets from your files.',
            position: 'right'
        },
        {
            element: '#onboarding-icon',
            title: '',
            content: 'Click here to get help.',
            position: 'bottom'
        }
    ];

    angular.module('data-prep.services.onboarding')
        .constant('datasetTour', datasetTour);
})();
