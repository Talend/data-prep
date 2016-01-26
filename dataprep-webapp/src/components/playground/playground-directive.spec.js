describe('Playground directive', function () {
    'use strict';

    var scope, createElement, element;
    var stateMock;

    var metadata = {
        'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        'name': 'US States',
        'author': 'anonymousUser',
        'created': '02-03-2015 14:52',
        'records': '3'
    };

    var $httpBackend;
    var datasetActions = [
        {
            'category': 'data_blending',
            'name': 'lookup',
            'parameters': [
                {
                    'name': 'column_id',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'filter',
                    'type': 'filter',
                    'default': ''
                },
                {
                    'name': 'lookup_ds_name',
                    'type': 'string',
                    'default': 'lookup_2'
                },
                {
                    'name': 'lookup_ds_id',
                    'type': 'string',
                    'default': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7'
                },
                {
                    'name': 'lookup_ds_url',
                    'type': 'string',
                    'default': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true'
                },
                {
                    'name': 'lookup_join_on',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_join_on_name',
                    'type': 'string',
                    'default': ''
                },
                {
                    'name': 'lookup_selected_cols',
                    'type': 'list',
                    'default': ''
                }
            ]
        }
    ];

    beforeEach(module('data-prep.playground', function($provide) {
        stateMock = {
            playground: {
                visible: true,
                filter: {gridFilters: []},
                lookup: {visibility: false},
                grid: {
                    selectedColumn: {'id': '0001'},
                    selectedLine: {'0001': '1'}
                }
            }
        };
        $provide.constant('state', stateMock);
    }));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'FILE_DETAILS_NAME': 'File: {{name}}',
            'FILE_DETAILS_LINES': '- {{records}} lines',
            'FILE_DETAILS_LIMIT': '- cut at {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));


    beforeEach(inject(function ($state, $rootScope, $compile, $q, $timeout, PreparationService, PlaygroundService, ExportService) {
        stateMock.playground.visible = true;
        scope = $rootScope.$new();

        createElement = function () {
            element = angular.element('<playground></playground>');
            angular.element('body').append(element);

            $compile(element)(scope);
            scope.$digest();
            $timeout.flush();
        };

        spyOn(PreparationService, 'refreshPreparations').and.returnValue();
        spyOn(PlaygroundService, 'createOrUpdatePreparation').and.returnValue($q.when(true));
        spyOn($state, 'go').and.returnValue();
        spyOn(ExportService, 'refreshTypes').and.returnValue($q.when([]));
        spyOn(ExportService, 'getParameters').and.returnValue({});

    }));

    beforeEach(inject(function ($injector, RestURLs) {
        $httpBackend = $injector.get('$httpBackend');
        $httpBackend
            .expectGET(RestURLs.datasetActionsUrl+ '/' + metadata.id +'/actions')
            .respond(200, datasetActions);
    }));

    afterEach(inject(function ($stateParams) {
        scope.$destroy();
        element.remove();

        $stateParams.prepid = null;
        $stateParams.datasetid = null;
    }));

    describe('header', function() {
        it('should render default playground header', function () {
            //given
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then
            var playground = angular.element('body').find('.playground').eq(0);
            var playgroundModal = playground.parent();

            //check header is present and contains description
            expect(playgroundModal.find('.modal-header').length).toBe(1);
            expect(playgroundModal.find('.modal-header').eq(0).find('.left-header > li').length).toBe(1);
            expect(playgroundModal.find('.modal-header').eq(0).find('li').eq(0).find('span').eq(0).text().trim()).toBe('File: US States');
            expect(playgroundModal.find('.modal-header').eq(0).find('li').eq(0).find('span').eq(1).text().trim()).toBe('- 3 lines');
        });

        it('should render playground header when dataset is truncated', function () {
            //given
            stateMock.playground.dataset = metadata;
            stateMock.playground.dataset.limit = 50;

            //when
            createElement();

            //then
            var playground = angular.element('body').find('.playground').eq(0);
            var playgroundModal = playground.parent();

            //check header is present and contains description
            expect(playgroundModal.find('.modal-header').length).toBe(1);
            expect(playgroundModal.find('.modal-header').eq(0).find('.left-header > li').length).toBe(1);
            expect(playgroundModal.find('.modal-header').eq(0).find('li').eq(0).find('span').eq(0).text().trim()).toBe('File: US States');
            expect(playgroundModal.find('.modal-header').eq(0).find('li').eq(0).find('span').eq(1).text().trim()).toBe('- cut at 50 lines');
        });

        it('should render insertion playground left header', function () {
            //given
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then
            var playground = angular.element('body').find('.playground').eq(0);
            var playgroundModal = playground.parent();
            expect(playgroundModal.find('.modal-header').eq(0).find('.left-header').eq(0)[0].hasAttribute('insertion-playground-left-header')).toBe(true);
        });
    });

    describe('suggestions', function() {
        it('should render right slidable panel', function () {
            //given
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then: check right slidable is displayed transformations with right slide action
            var playground = angular.element('body').find('.playground').eq(0);
            expect(playground.eq(0).find('.suggestions').eq(0).hasClass('slide-hide')).toBe(false);
            expect(playground.eq(0).find('.suggestions').eq(0).find('.action').eq(0).hasClass('right')).toBe(true);
        });
    });

    describe('recipe header', function () {
        beforeEach(inject(function(StateService) {
            stateMock.playground.nameEditionMode = true;
            spyOn(StateService, 'setNameEditionMode').and.callFake(function(value) {
                stateMock.playground.nameEditionMode = value;
            });
        }));

        it('should render left slidable panel', function () {
            //given
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then : check left slidable is hidden recipe with left slide action
            var playground = angular.element('body').find('.playground').eq(0);
            expect(playground.eq(0).find('.recipe').eq(0).hasClass('slide-hide')).toBe(true);
            expect(playground.eq(0).find('.recipe').eq(0).find('.action').eq(0).hasClass('right')).toBe(false);
        });

        it('should render editable text on preparation title', function () {
            //given
            stateMock.playground.preparation = {id: '3e41168465e15d4'};
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then
            var stepsHeader = angular.element('body > talend-modal').find('.steps-header').eq(0);
            var title = stepsHeader.find('talend-editable-text');
            expect(title.length).toBe(1);

        });

        it('should toggle recipe on click on the On/Off switch', inject(function (RecipeBulletService) {
            //given
            spyOn(RecipeBulletService, 'toggleRecipe').and.returnValue();

            createElement();
            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');

            //when
            chkboxOnOff.trigger('click');

            //then
            expect(RecipeBulletService.toggleRecipe).toHaveBeenCalled();
        }));

        it('should switch OFF the On/Off switch when the 1st step is INACTIVE', inject(function (RecipeService) {
            //given
            stateMock.playground.dataset = metadata;
            var step = {
                inactive: false,
                transformation: {
                    stepId: '92771a304130e9',
                    name: 'propercase',
                    parameters: [],
                    items: [],
                    dynamic: false
                }
            };
            RecipeService.getRecipe().push(step);
            createElement();

            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');
            expect(chkboxOnOff.prop('checked')).toBe(true);

            //when
            step.inactive = true;
            scope.$digest();

            //then
            expect(chkboxOnOff.prop('checked')).toBe(false);
        }));

        it('should switch ON the On/Off switch when the 1st step is ACTIVE', inject(function (RecipeService) {
            //given
            stateMock.playground.dataset = metadata;
            var step = {
                inactive: true,
                transformation: {
                    stepId: '92771a304130e9',
                    name: 'propercase',
                    parameters: [],
                    items: [],
                    dynamic: false
                }
            };
            RecipeService.getRecipe().push(step);
            createElement();

            var chkboxOnOff = angular.element('body').find('.label-switch > input[type="checkbox"]');
            expect(chkboxOnOff.prop('checked')).toBe(false);

            //when
            step.inactive = false;
            scope.$digest();

            //then
            expect(chkboxOnOff.prop('checked')).toBe(true);
        }));
    });

    describe('datagrid', function() {
        it('should render datagrid with filters', function () {
            //given
            stateMock.playground.dataset = metadata;

            //when
            createElement();

            //then : check datagrid and filters are present
            var playground = angular.element('body').find('.playground').eq(0);
            expect(playground.eq(0).find('filter-bar').length).toBe(1);
            expect(playground.eq(0).find('filter-bar').find('#filter-search').length).toBe(1);
            expect(playground.eq(0).find('datagrid').length).toBe(1);
        });
    });

    describe('hide playground', function () {
        beforeEach(inject(function (PlaygroundService, PreparationService) {
            stateMock.playground.dataset = metadata;
            createElement();

            scope.$apply();
            expect(PreparationService.refreshPreparations).not.toHaveBeenCalled();
        }));

        it('should change route to preparations list on preparation playground hide', inject(function ($state, $stateParams) {
            //given: simulate playground route with preparation id
            $stateParams.prepid = '1234';

            //when
            stateMock.playground.visible = false;
            scope.$apply();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.preparations', {prepid: null});
        }));

        it('should change route to datasets list on dataset playground hide', inject(function ($state, $stateParams) {
            //given: simulate playground route with preparation id
            $stateParams.datasetid = '1234';

            //when
            stateMock.playground.visible = false;
            scope.$apply();

            //then
            expect($state.go).toHaveBeenCalledWith('nav.home.datasets', {datasetid: null});
        }));

        it('should do nothing if playground is not routed', inject(function ($state, $stateParams, PreparationService) {
            //given: simulate no preparation id in route
            $stateParams.prepid = null;
            $stateParams.datasetid = null;

            //when
            stateMock.playground.visible = false;
            scope.$apply();

            //then
            expect(PreparationService.refreshPreparations).toHaveBeenCalled();
            expect($state.go).not.toHaveBeenCalled();
        }));

        it('should refresh preparations on playground hide', inject(function (PreparationService) {
            //when
            stateMock.playground.visible = false;
            scope.$apply();

            //then
            expect(PreparationService.refreshPreparations).toHaveBeenCalled();
        }));
    });

});