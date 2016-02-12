/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform menu controller', function () {
    'use strict';

    var createController, scope;

    var metadata = {
        'id': '44f5e4ef-96e9-4041-b86a-0bee3d50b18b',
        'name': 'customers_jso_light',
        'author': 'anonymousUser',
        'records': 15,
        'nbLinesHeader': 1,
        'nbLinesFooter': 0,
        'created': '02-16-2015 08:52'
    };
    var column = {
        'id': '0001',
        'name': 'MostPopulousCity',
        'quality': {
            'empty': 5,
            'invalid': 10,
            'valid': 72
        },
        'type': 'string',
        'domain': 'FR_POSTAL_CODE',
        'domainLabel': 'FR POSTAL CODE',
        'domainCount': 7,
        'semanticDomains': [
            {
                'id': 'CH_POSTAL_CODE',
                'label': 'CH  POSTAL CODE',
                'count': 5
            },
            {
                'id': 'FR_POSTAL_CODE',
                'label': 'FR POSTAL CODE',
                'count': 7
            },
            {
                'id': 'FR_CODE_COMMUNE_INSEE',
                'label': 'FR INSEE CODE',
                'count': 7
            },
            {
                'id': 'DE_POSTAL_CODE',
                'label': 'DE POSTAL CODE',
                'count': 7
            },
            {
                'id': 'US_POSTAL_CODE',
                'label': 'US POSTAL CODE',
                'count': 7
            }
        ]
    };

    var stateMock;

    beforeEach(angular.mock.module('data-prep.transformation-menu', function ($provide) {
        stateMock = {playground: {}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller, $q, PlaygroundService, TransformationService) {
        scope = $rootScope.$new();

        createController = function () {
            var ctrl = $controller('TransformMenuCtrl', {
                $scope: scope
            });
            ctrl.metadata = metadata;
            ctrl.column = column;
            return ctrl;
        };

        spyOn(PlaygroundService, 'appendStep').and.returnValue($q.when(true));
        spyOn(TransformationService, 'resetParamValue').and.returnValue();
        spyOn(TransformationService, 'initDynamicParameters').and.returnValue($q.when(true));

    }));

    it('should reset parameters/choices on select if items has parameters/choices', inject(function (TransformationService) {
        //given
        var ctrl = createController();
        var menu = {
            parameters: [{name: 'param1', type: 'text', default: '.'}],
            items: []
        };
        var scope = 'column';

        expect(TransformationService.resetParamValue).not.toHaveBeenCalled();

        //when
        ctrl.select(menu, scope);

        //then
        expect(TransformationService.resetParamValue).toHaveBeenCalledWith(menu.parameters);
        expect(TransformationService.resetParamValue).toHaveBeenCalledWith(menu.items, 'CHOICE');
    }));

    it('should open modal on select if item has parameters', inject(function (PlaygroundService) {
        //given
        var ctrl = createController();
        var menu = {parameters: [{name: 'param1', type: 'text', default: '.'}]};
        var scope = 'column';

        //when
        ctrl.select(menu, scope);

        //then
        expect(ctrl.showModal).toBeTruthy();
        expect(ctrl.selectedMenu).toBe(menu);
        expect(ctrl.selectedScope).toBe(scope);
        expect(PlaygroundService.appendStep).not.toHaveBeenCalled();
    }));

    it('should open modal on select if item has choice', inject(function (PlaygroundService) {
        //given
        var ctrl = createController();
        var menu = {items: [{name: 'choice', values: [{name: 'choice1'}, {name: 'choice2'}]}]};
        var scope = 'column';

        //when
        ctrl.select(menu, scope);

        //then
        expect(ctrl.showModal).toBeTruthy();
        expect(ctrl.selectedMenu).toBe(menu);
        expect(ctrl.selectedScope).toBe(scope);
        expect(PlaygroundService.appendStep).not.toHaveBeenCalled();
    }));

    it('should call transform on simple menu select', inject(function (PlaygroundService) {
        //given
        var ctrl = createController();
        var menu = {name: 'uppercase', category: 'case'};
        var scope = 'column';

        //when
        ctrl.select(menu, scope);

        //then
        expect(ctrl.showModal).toBeFalsy();
        var expectedParams = {
            scope: scope,
            column_id: column.id,
            column_name: column.name
        };
        expect(PlaygroundService.appendStep).toHaveBeenCalledWith('uppercase', expectedParams);
    }));


    it('should fetch dynamic parameters', inject(function (TransformationService) {
        //given
        var ctrl = createController();
        var menu = {name: 'textclustering', category: 'quickfix', dynamic: true};

        stateMock.playground.dataset = {id: '78bae6345aef9965e22b54'};
        stateMock.playground.preparation = {id: '721cd4455fb69e89543d4'};

        //when
        ctrl.select(menu);
        scope.$digest();

        //then
        expect(TransformationService.initDynamicParameters).toHaveBeenCalledWith(
            {name: 'textclustering', category: 'quickfix', dynamic: true},
            {
                columnId: '0001',
                datasetId: '78bae6345aef9965e22b54',
                preparationId: '721cd4455fb69e89543d4'
            }
        );
    }));


    it('should display modal and set flags on dynamic params fetch', function () {
        //given
        var ctrl = createController();
        var menu = {name: 'textclustering', category: 'quickfix', dynamic: true};

        stateMock.playground.dataset = {id: '78bae6345aef9965e22b54'};
        stateMock.playground.preparation = {id: '721cd4455fb69e89543d4'};

        //when
        expect(ctrl.showModal).toBeFalsy();
        ctrl.select(menu);
        expect(ctrl.showModal).toBeTruthy();
        expect(ctrl.dynamicFetchInProgress).toBeTruthy();
        scope.$digest();

        //then
        expect(ctrl.showModal).toBeTruthy();
        expect(ctrl.dynamicFetchInProgress).toBeFalsy();
    });


    it('should call playground service to append step and hide modal', inject(function (PlaygroundService) {
        //given
        var ctrl = createController();
        var menu = {name: 'transfo_name', category: 'case', parameters: [{name: 'param1', type: 'text', default: '.'}]};
        var params = {param1: 'value'};
        var transfoScope = 'column';

        //when
        var closure = ctrl.appendClosure(menu, transfoScope);
        expect(PlaygroundService.appendStep).not.toHaveBeenCalled();
        closure(params);

        //then
        var expectedParams = {
            param1: 'value',
            scope: transfoScope,
            column_id: column.id,
            column_name: column.name
        };
        expect(PlaygroundService.appendStep).toHaveBeenCalledWith('transfo_name', expectedParams);
    }));

    it('should hide modal after step append', function () {
        //given
        var ctrl = createController();
        var menu = {name: 'transfo_name', category: 'case', parameters: [{name: 'param1', type: 'text', default: '.'}]};
        var params = {param1: 'value'};
        var transfoScope = 'column';
        ctrl.showModal = true;

        //when
        ctrl.appendClosure(menu, transfoScope)(params);
        scope.$digest();

        //then
        expect(ctrl.showModal).toBe(false);
    });
});