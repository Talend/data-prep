/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Filter search directive', () => {
    'use strict';

    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('data-prep.filter-search'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element('<filter-search></filter-search>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render input with auto-complete', () => {
        //when
        createElement();

        //then
        expect(element.find('div[mass-autocomplete]').length).toBe(1);
        expect(element.find('input[type="search"]').length).toBe(1);
    });

    it('should stop propagation on ESC key down', () => {
        //given
        createElement();

        let bodyEscEvent = false;
        const escEventListener = (event) => {
            if (event.keyCode === 27) {
                bodyEscEvent = true;
            }
        };

        const body = angular.element('body');
        body.append(element);
        body.keydown(escEventListener);

        const event = angular.element.Event('keydown');
        event.keyCode = 27;

        //when
        element.find('input[type="search"]').eq(0).trigger(event);
        scope.$digest();

        //then
        expect(bodyEscEvent).toBe(false);

        //finally
        body.off('keydown', escEventListener);
    });

    it('should propagate on key down other than ESC', () => {
        //given
        createElement();

        let bodyEnterEvent = false;
        const escEventListener = (event) => {
            if (event.keyCode === 13) {
                bodyEnterEvent = true;
            }
        };

        const body = angular.element('body');
        body.append(element);
        body.keydown(escEventListener);

        const event = angular.element.Event('keydown');
        event.keyCode = 13;

        //when
        element.find('input[type="search"]').eq(0).trigger(event);
        scope.$digest();

        //then
        expect(bodyEnterEvent).toBe(true);

        //finally
        body.off('keydown', escEventListener);
    });

    it('should empty the input filter search', () => {
        //given
        createElement();
        const ctrl = element.controller('filterSearch');
        ctrl.filterSearch = 'toto';
        const event2 = angular.element.Event('blur');

        //when
        element.find('input[type="search"]').eq(0).trigger(event2);
        scope.$digest();

        //then
        expect(ctrl.filterSearch).toBe('');
    });
});
