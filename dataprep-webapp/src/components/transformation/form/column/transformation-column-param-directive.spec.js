describe('Transformation column param directive', function () {
    'use strict';
    var scope, createElement;

    var stateMock = {
        playground: {
            // available dataset/preparation columns
            data: {
                columns: [
                    {id: '0001', name: 'first name'},
                    {id: '0002', name: 'last name'},
                    {id: '0003', name: 'birth date'}
                ]
            },
            grid: {
                // selected column
                selectedColumn: {}
            }
        }
    };

    beforeEach(module('data-prep.transformation-form', function ($provide) {

        // set the selected column to the first one
        stateMock.playground.grid.selectedColumn = stateMock.playground.data.columns[0];

        $provide.constant('state', stateMock);
    }));

    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            var element = angular.element('<transform-column-param parameter="parameter"></transform-column-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    
    it('should render an action with a column parameter', function() {
        //given
        scope.parameter = {
            name: 'selected_column',
            type: 'column',
            implicit: false,
            canBeBlank: false,
            description: 'Combine the content of this column with the current one',
            label: 'The Column to concatenate',
            default: ''
        };

        //when
        var element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('The Column to concatenate:');
        expect(element.find('.param-input').find('option').length).toBe(stateMock.playground.data.columns.length - 1);

    });
});
