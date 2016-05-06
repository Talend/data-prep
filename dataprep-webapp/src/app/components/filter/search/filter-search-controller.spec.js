/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('filter search controller', function() {
    'use strict';

    var createController, scope;
    var stateMock;

    var data = {
        'metadata': {
            'columns': [
                {
                    'id': '0000',
                    'name': 'id',
                    'quality': {
                        'empty': 5,
                        'invalid': 10,
                        'valid': 72
                    },
                    'type': 'number'
                },
                {
                    'id': '0001',
                    'name': 'Postal',
                    'quality': {
                        'empty': 5,
                        'invalid': 10,
                        'valid': 72
                    },
                    'type': 'string'
                },
                {
                    'id': '0002',
                    'name': 'State',
                    'quality': {
                        'empty': 5,
                        'invalid': 10,
                        'valid': 72
                    },
                    'type': 'string'
                },
                {
                    'id': '0003',
                    'name': 'Capital',
                    'quality': {
                        'empty': 5,
                        'invalid': 10,
                        'valid': 72
                    },
                    'type': 'string'
                },
                {
                    'id': '0004',
                    'name': 'MostPopulousCity',
                    'quality': {
                        'empty': 5,
                        'invalid': 10,
                        'valid': 72
                    },
                    'type': 'string'
                }
            ]
        },
        'records': [
            {
                '0000': '1',
                '0001': 'AL',
                '0002': 'My Alabama',
                '0003': 'Montgomery',
                '0004': 'Birmingham city',
                tdpId: 0
            },
            {
                '0000': '2',
                '0001': 'AK',
                '0002': 'Alaska',
                '0003': 'Juneau',
                '0004': 'Anchorage',
                tdpId: 1
            },
            {
                '0000': '3',
                '0001': 'AL',
                '0002': 'My Alabama 2',
                '0003': 'Montgomery',
                '0004': 'Birmingham city',
                tdpId: 2
            },
            {
                '0000': '3',
                '0001': 'AL',
                '0002': 'My Alabama 3',
                '0003': 'Montgomery',
                '0004': 'Alabama city',
                tdpId: 3
            }
        ]
    };

    beforeEach(angular.mock.module('data-prep.filter-search', function ($provide) {
        stateMock = {playground: {}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function($rootScope, $controller, FilterService) {
        scope = $rootScope.$new();

        createController = function() {
            return $controller('FilterSearchCtrl', {
                $scope: scope
            });
        };

        spyOn(FilterService, 'addFilter').and.returnValue();
    }));

    it('should create sorted suggestions based on case insensitive typed word and current data from service', function() {
        //given
        stateMock.playground.data = data;
        var ctrl = createController();

        //when
        var suggestions = ctrl.filterSuggestOptions.suggest('ala');

        //then
        expect(suggestions.length).toBe(2);
        expect(suggestions[0]).toEqual({
            label: 'ala in <b>MostPopulousCity</b>',
            value: 'ala',
            columnId: '0004',
            columnName: 'MostPopulousCity'
        });
        expect(suggestions[1]).toEqual({
            label: 'ala in <b>State</b>',
            value: 'ala',
            columnId: '0002',
            columnName: 'State'
        });
    });

    it('should create sorted suggestions based on typed word with wildcard', function() {
        //given
        stateMock.playground.data = data;
        var ctrl = createController();

        //when
        var suggestions = ctrl.filterSuggestOptions.suggest('ala*ma');

        //then
        expect(suggestions.length).toBe(2);
        expect(suggestions[0]).toEqual({
            label: 'ala*ma in <b>MostPopulousCity</b>',
            value: 'ala*ma',
            columnId: '0004',
            columnName: 'MostPopulousCity'

        });
        expect(suggestions[1]).toEqual({
            label: 'ala*ma in <b>State</b>',
            value: 'ala*ma',
            columnId: '0002',
            columnName: 'State'
        });
    });

    it('should return empty array if typed string is empty', function() {
        //given
        stateMock.playground.data = data;
        var ctrl = createController();

        //when
        var suggestions = ctrl.filterSuggestOptions.suggest('');

        //then
        expect(suggestions.length).toBe(0);
    });

    it('should reset input search on item selection', function() {
        //given
        stateMock.playground.data = data;
        var ctrl = createController();
        ctrl.filterSearch = 'ala';

        //when
        ctrl.filterSuggestOptions.on_select({
            label: 'ala in <b>State</b>',
            value: 'ala',
            columnName: 'State',
            columnId: '0002'
        });

        //then
        expect(ctrl.filterSearch).toBe('');
    });

    it('should add filter on item selection', inject(function(FilterService) {
        //given
        stateMock.playground.data = data;
        var ctrl = createController();
        ctrl.filterSearch = 'ala';

        expect(FilterService.addFilter).not.toHaveBeenCalled();

        //when
        ctrl.filterSuggestOptions.on_select({
            label: 'ala in <b>State</b>',
            value: 'ala',
            columnName: 'State',
            columnId: '0002'
        });

        //then
        expect(FilterService.addFilter).toHaveBeenCalledWith('contains', '0002', 'State', {
            phrase: [
                {
                    value: 'ala'
                }
            ]
        });
    }));
});
