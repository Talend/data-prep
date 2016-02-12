/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
'use strict';

const datasetTour = [
    {
        element: '.no-js',
        title: '<center>Welcome to</br>Talend Data Preparation</center>',
        content: 'To quickly learn how to use it, click <b>Next</b>.',
        position: 'right'
    },
    {
        element: '#nav_home_datasets',
        title: '',
        content: 'Here you can browse through and manage the datasets you created.<br/>A dataset holds the raw data that can be used as raw material without affecting your original data.',
        position: 'right'
    },
    {
        element: '#dataset_0',
        title: '',
        content: 'Here you can find some ready-to-use datasets to get familiar with Talend Data Preparation.',
        position: 'bottom'
    },
    {
        element: '#help-import-local',
        title: '',
        content: 'Here you can create your own datasets from your own data.',
        position: 'right'
    },
    {
        element: '#message-icon',
        title: '',
        content: 'Click here to send feedback to Talend.',
        position: 'bottom'
    },
    {
        element: '#onboarding-icon',
        title: '',
        content: 'Click here to get help.',
        position: 'bottom'
    }
];

export default datasetTour;
