(function() {
    'use strict';

    var recipeTour = [
        {
            element: '#help-preparation-name',
            title: '',
            content: 'You can give a name to your brand new preparation.</br>It will be listed in the <b>All preparations</b> view.',
            position: 'right',
            tooltipPosition: 'right'
        },
        {
            element: '#help-recipe > ul',
            title: '',
            content: 'Here is your recipe, it represents the steps of your preparation.</br> This is the place where you can:<ul><li> - delete a step,</li><li> - preview changes performed by each step,</li><li> - change an existing step,</li><li> - activate or deactivate steps.</li></ul>',
            position: 'right'
        },
        {
            element: '#help-history',
            title: '',
            content: 'And don\'t worry, at any time, you can undo or redo your last changes.',
            position: 'left'
        }
    ];

    angular.module('data-prep.services.onboarding')
        .constant('recipeTour', recipeTour);
})();
