/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transform choice params controller', () => {
    'use strict';

    let createController;
    let scope;
    let parameter;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(inject(($rootScope, $controller) => {
        scope = $rootScope.$new();

        createController = () => {
            let ctrlFn = $controller('TransformChoiceParamCtrl', {
                $scope: scope,
            }, true);
            ctrlFn.instance.parameter = parameter;
            return ctrlFn();
        };
    }));

    it('should Not initialize parameter value', () => {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [],
            },
        };

        //when
        let ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual(undefined);
    });

    it('should init choice default value', () => {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    { value: 'regex' },
                    { value: 'index' },
                ],
            },
            default: 'index',
        };

        //when
        let ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('index');
    });

    it('should init choice to first value if there is no default', () => {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    { value: 'regex' },
                    { value: 'index' },
                ],
            },
        };

        //when
        let ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('regex');
    });

    it('should not change current value if it is set', () => {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    { value: 'regex' },
                    { value: 'index' },
                ],
            },
            value: 'index',
        };

        //when
        let ctrl = createController();

        //then
        expect(ctrl.parameter.value).toEqual('index');
    });

    it('should return label from value', () => {
        //given
        parameter = {
            name: 'mode',
            type: 'select',
            configuration: {
                values: [
                    { value: 'regex', label: 'regex-label' },
                    { value: 'index', label: 'index-label' },
                ],
            },
            value: 'regex',
        };

        //when
        let ctrl = createController();

        //then
        expect(ctrl.getLabelByValue('regex')).toEqual('regex-label');
    });
});
