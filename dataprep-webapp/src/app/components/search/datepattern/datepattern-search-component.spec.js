/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Datepattern Search component', () => {
    let scope, createElement, element;

    beforeEach(angular.mock.module('data-prep.datepattern-search'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            const template = `<datepattern-search></datepattern-search>`;
            element = $compile(template)(scope);
            scope.$digest();

        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render typeahead', () => {
        //when
        createElement();

        //then
        expect(element.find('typeahead').length).toBe(1);
    });
});