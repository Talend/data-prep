/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Navbar directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.navbar'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<navbar></navbar>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render navigation bar', function() {
        //when
        createElement();

        //then
        expect(element.find('talend-navbar').length).toBe(1);
    });

    it('should render footer bar', function() {
        //when
        createElement();

        //then
        expect(element.find('footer').length).toBe(1);
    });

    it('should render content insertion point', function() {
        //when
        createElement();

        //then
        expect(element.find('ui-view.content').length).toBe(1);
    });
});