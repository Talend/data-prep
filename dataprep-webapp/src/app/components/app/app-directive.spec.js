/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('App directive', function() {
    'use strict';

    var scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.app'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function () {
            element = angular.element('<dataprep-app></dataprep-app>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    beforeEach(inject(function($injector, RestURLs) {
        RestURLs.setServerUrl('');

        var $httpBackend = $injector.get('$httpBackend');
        $httpBackend
            .expectGET(RestURLs.exportUrl + '/formats')
            .respond(200, {});
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should hold toaster container', function() {
        //when
        createElement();

        //then
        expect(element.find('#toast-container').length).toBe(1);
    });

    it('should hold loader element', function() {
        //when
        createElement();

        //then
        expect(element.find('talend-loading').length).toBe(1);
    });

    it('should hold playground element', function() {
        //when
        createElement();

        //then
        expect(element.find('playground').length).toBe(1);
    });

    it('should render router insertion point', function() {
        //when
        createElement();

        //then
        expect(element.find('ui-view.main-layout').length).toBe(1);
    });
});