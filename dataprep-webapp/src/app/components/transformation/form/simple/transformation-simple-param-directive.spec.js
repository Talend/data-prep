/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation simple params directive', () => {
    'use strict';
    let scope;
    let createElement;

    beforeEach(angular.mock.module('data-prep.transformation-form'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            COLON: ': ',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = () => {
            let element = angular.element('<transform-simple-param parameter="parameter" is-readonly="isReadonly"></transform-simple-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with a parameter', () => {
        //given
        scope.parameter =   {
            name: 'param1',
            label: 'Param 1',
            type: 'string',
            inputType: 'text',
            default: '.',
        };

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.param-input').find('input[type="text"]').length).toBe(1);
    });

    it('should render an action with a parameter in read only mode', () => {
        //given
        scope.parameter =   {
            name: 'param1',
            label: 'Param 1',
            type: 'string',
            inputType: 'text',
            default: '.',
            value: 'hello',
        };

        scope.isReadonly = true;

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.param-input .param-input-label').eq(0).text().trim()).toBe('hello');

    });

    it('should render an action with a checkbox and a parameter in read only mode', () => {
        //given
        scope.parameter =   {
            name: 'param1',
            label: 'Param 1',
            type: 'boolean',
            inputType: 'text',
            default: '.',
            value: true,
        };

        scope.isReadonly = true;

        //when
        let element = createElement();

        //then
        expect(element.find('.param-name').length).toBe(0);
        expect(element.find('.param-input').find('input[type="checkbox"]').length).toBe(1);
        expect(element.find('.param-input span').eq(0).text().trim()).toBe('Param 1');
    });

	it('should render disabled input text if readonly', () => {
		//given
		scope.parameter =   {
			name: 'param1',
			label: 'Param 1',
			type: 'text',
			inputType: 'text',
			default: '.',
			value: true,
			readonly: true,
		};

		//when
		let element = createElement();

		//then
        let input = element.find('.param-input').find('input[type="text"]');
		expect(input.length).toBe(1);
		expect(input.prop('disabled')).toBe(true);
	});

	it('should render disabled input checkbox if readonly', () => {
		//given
		scope.parameter =   {
			name: 'param1',
			label: 'Param 1',
			type: 'boolean',
			inputType: 'checkbox',
			default: '.',
			value: true,
			readonly: true,
		};

		//when
		let element = createElement();

		//then
		let input = element.find('.param-input').find('input[type="checkbox"]');
		expect(input.length).toBe(1);
		expect(input.prop('disabled')).toBe(true);
	});

	it('should render input checkbox text if not readonly', () => {
		//given
		scope.parameter =   {
			name: 'param1',
			label: 'Param 1',
			type: 'boolean',
			inputType: 'checkbox',
			default: '.',
			value: true,
		};

		//when
		let element = createElement();

		//then
		let input = element.find('.param-input').find('input[type="checkbox"]');
		expect(input.length).toBe(1);
		expect(input.prop('disabled')).toBe(false);
	});
});
