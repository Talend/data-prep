describe('Lookup directive', function () {
    'use strict';

    var scope, createElement, element, StateMock;

    var sortList = [
        {id: 'name', name: 'NAME_SORT', property: 'name'},
        {id: 'date', name: 'DATE_SORT', property: 'created'}
    ];

    var orderList = [
        {id: 'asc', name: 'ASC_ORDER'},
        {id: 'desc', name: 'DESC_ORDER'}
    ];

    beforeEach(module('data-prep.lookup', function ($provide) {
        StateMock = {
            playground: {
                lookup: {
                    columnsToAdd: [],
                    selectedColumn: {},
                    datasets: [],
                    sortList: sortList,
                    orderList: orderList
                },
                grid: {
                    selectedColumn: {}
                }
            }
        };
        $provide.constant('state', StateMock);
    }));

    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<lookup></lookup>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(function () {
        StateMock.playground.lookup.columnsToAdd = [];
        StateMock.playground.lookup.selectedColumn = null;
        StateMock.playground.grid.selectedColumn = null;

        scope.$destroy();
        element.remove();
    });

    it('should disable submit button when the lookup is initiated', function () {
        //when
        createElement();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe('disabled');
    });

    it('should enable submit button when the 2 columns are selected', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];

        //when
        createElement();
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe(undefined);
    });

    it('should disable submit button when there is no more selected columns', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];

        createElement();
        scope.$digest();
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe(undefined);

        //when
        StateMock.playground.lookup.columnsToAdd = [];
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe('disabled');
    });

    it('should disable submit button when the tdpId column is selected', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];
        StateMock.playground.lookup.selectedColumn = null;

        //when
        createElement();
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe('disabled');
    });

    it('should enable submit button when the there are 2 columns selected and the tdpId is not selected', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];
        StateMock.playground.lookup.selectedColumn = {id: '0001'};

        //when
        createElement();
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe(undefined);
    });

    it('should disable submit button when in the main dataset the tdpId is selected', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];
        StateMock.playground.lookup.selectedColumn = {id: '0000'};
        StateMock.playground.grid.selectedColumn = null;

        //when
        createElement();
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe('disabled');
    });

    it('should enable submit button when there are columns checked, the tdpId is not selected neither in the main nor in the lookup', function () {
        //given
        StateMock.playground.lookup.columnsToAdd = [1, 2];
        StateMock.playground.lookup.selectedColumn = {id: '0000'};
        StateMock.playground.grid.selectedColumn = {id: '0000'};

        //when
        createElement();
        scope.$digest();

        //then
        expect(element.find('#lookup-submit-btn-id').attr('disabled')).toBe(undefined);
    });
});