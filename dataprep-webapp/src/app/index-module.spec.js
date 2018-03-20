/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Dataprep app', () => {
    'use strict';

    beforeEach(angular.mock.module('pascalprecht.translate'));
    beforeEach(angular.mock.module('data-prep.services.state'));
    beforeEach(angular.mock.module('data-prep.services.utils'));

    describe('config', () => {
        it('should set $httpProvider useApplyAsync config to true', () => {
            //given
            let httpProviderIt = null;

            //when
            angular.mock.module('data-prep', ($httpProvider) => {
                httpProviderIt = $httpProvider;
            });

            inject(($injector) => {
                const $httpBackend = $injector.get('$httpBackend');
                $httpBackend.when('GET', 'i18n/en.json').respond({});
                $httpBackend.when('GET', 'i18n/fr.json').respond({});
            });

            //then
            expect(httpProviderIt.useApplyAsync()).toBe(true);
        });
    });
});
