/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import angular from 'angular';

describe('Folder creator form component', () => {
    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('data-prep.folder-creator'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            ENTER_FOLDER_NAME: 'Enter folder name',
            CANCEL: 'Cancel',
            OK: 'Ok',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new(true);

        createElement = () => {
            element = angular.element(`<folder-creator-form></folder-creator-form>`);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render folder creator content', () => {
            //when
            createElement();

            //then
            expect(element.find('label').eq(0).text()).toBe('Enter folder name');
            expect(element.find('input#create-folder-name').length).toBe(1);
            expect(element.find('button').eq(0).text()).toBe('Cancel');
            expect(element.find('button').eq(1).text()).toBe('Ok');
        });
    });
});
