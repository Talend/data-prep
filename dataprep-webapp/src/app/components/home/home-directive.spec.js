/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Home directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.home'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile, $q, FolderService) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<home></home>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
        spyOn(FolderService, 'getContent').and.returnValue($q.when(true));
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render subheader bar', inject(function () { // FolderService
        //when
        createElement();

        //then
        expect(element.find('header.subheader').length).toBe(1);
    }));

    it('should render home main panel', inject(function () { //FolderService
        //when
        createElement();

        //then
        var home = element.find('.home');
        expect(home.length).toBe(1);
        expect(home.find('.side-menu').length).toBe(1);
        expect(home.find('ui-view[name="home-content"]').length).toBe(1);
        expect(home.find('.inventory-data').length).toBe(1);
    }));
});