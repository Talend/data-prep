/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('InventoryItem component', () => {
    'use strict';

    function strEndsWith(str, suffix) {
        return str.match(suffix + '$')[0] === suffix;
    }

    let scope;
    let createElement;
    let element;
    let ctrl;

    const dataset = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States',
        author: 'anonymousUser',
        created: '1437020219741',
        type: 'text/csv',
        certificationStep: 'NONE',
        preparations: [{ name: 'US States prepa' }, { name: 'US States prepa 2' }],
        favorite: true,
        owner: {
            displayName: 'anonymousUser',
        },
    };

    const job_created_dataset = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States',
        type: 'text/csv',
        tag: 'components',
    };

    const csv_dataset = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States',
        type: 'text/csv',
    };

    const xls_dataset = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States',
        type: 'application/vnd.ms-excel',
    };

    const job_dataset = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States',
        type: 'application/vnd.remote-ds.job',
    };

    const preparation = {
        id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        name: 'US States prep',
        author: 'anonymousUser',
        creationDate: '1437020219741',
        type: 'text/csv',
        certificationStep: 'NONE',
        steps: [{ name: 'US States prepa' }, { name: 'US States prepa 2' }],
        owner: {
            displayName: 'anonymousUser',
        },
    };

    const folder = {
        path: 'folder 1',
        name: 'folder 1',
        author: 'anonymousUser',
        creationDate: '1437020219741',
        nbPreparations: 3,
        owner: {
            displayName: 'anonymousUser',
        },
    };

    const certifiedDataset = {
        id: '888888-bf80-41c8-92e5-66d70f22ec1f',
        name: 'cars',
        author: 'root',
        created: '1437020219741',
        type: 'text/csv',
        certificationStep: 'certified',
        preparations: [{ name: 'US States prepa' }, { name: 'US States prepa 2' }],
    };

    beforeEach(angular.mock.module('data-prep.inventory-item'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            DATASET_DETAILS: 'owned by {{owner.displayName}}, created {{created | TDPMoment}}, contains {{records}} lines',
            PREPARATION_DETAILS: 'owned by {{owner.displayName}}, created {{creationDate | TDPMoment}}, contains {{steps.length -1}} step(s)',
            FOLDER_DETAILS: 'owned by {{owner.displayName}}, created {{creationDate | TDPMoment}}, contains {{nbPreparations}} preparation(s)',
        });
        $translateProvider.preferredLanguage('en');
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('dataset icon', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();
            createElement = (newDataSet) => {
                scope.dataset = newDataSet;

                element = angular.element(`
                    <inventory-item
                        item="dataset"
                        details="DATASET_DETAILS"
                        type="dataset"></inventory-item>
                `);
                $compile(element)(scope);
                scope.$digest();
                ctrl = element.controller('inventoryItem');
                return element;
            };
        }));

        it('should select CSV icon', () => {
            // when
            createElement(csv_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconName = icon.find('icon').attr('name');
            expect(iconName).toBe('\'talend-file-csv-o\'');
        });

        it('should select XLS icon', () => {
            // when
            createElement(xls_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconName = icon.find('icon').attr('name');
            expect(iconName).toBe('\'talend-file-xls-o\'');
        });

        it('should select JOB icon', () => {
            // when
            createElement(job_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconName = icon.find('icon').attr('name');
            expect(iconName).toBe('\'talend-file-connect-o\'');
        });

        it('should select JOB icon for components tag', () => {
            // when
            createElement(job_created_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconName = icon.find('icon').attr('name');
            expect(iconName).toBe('\'talend-file-job-o\'');
        });

        it('should not display update for job dataset', () => {
            // when
            createElement(job_dataset);

            // then
            expect(element.find('talend-file-selector').length).toBe(0);
        });
    });

    describe('dataset', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();
            scope.dataset = dataset;
            scope.preparations = [];
            createElement = () => {
                element = angular.element('<inventory-item ' +
                    'item="dataset" ' +
                    'details="DATASET_DETAILS" ' +
                    'type="dataset" ' +
                    '></inventory-item>');
                $compile(element)(scope);
                scope.$digest();
                ctrl = element.controller('inventoryItem');
                return element;
            };
        }));

        describe('display inventory components', () => {
            it('should display inventory icon without certification pin', () => {
                // when
                createElement();

                // then
                const icon = element.find('.inventory-icon').eq(0);
                const certificationIcon = icon.find('.pin');
                expect(certificationIcon.length).toBe(0);
            });
            it('should display inventory icon with certification pin', () => {
                // when
                scope.dataset = certifiedDataset;
                createElement();

                // then
                const icon = element.find('.inventory-icon').eq(0);
                const certificationIcon = icon.find('.pin');
                expect(certificationIcon.length).toBe(1);
            });

            it('should display inventory details', inject(($filter) => {
                // given
                const momentize = $filter('TDPMoment');

                // when
                createElement();

                // then
                expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');
            }));

            it('should NOT display favorite icon', () => {
                // when
                scope.toggleFavorite = null;
                createElement();

                // then
                expect(element.find('.favorite').length).toBe(0);
            });
        });
    });

    describe('preparation', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();

            scope.preparation = preparation;

            scope.preparations = [];
            createElement = () => {
                element = angular.element('<inventory-item ' +
                    'item="preparation" ' +
                    'details="PREPARATION_DETAILS" ' +
                    'type="preparation" ' +
                    '></inventory-item>');
                $compile(element)(scope);
                scope.$digest();
                ctrl = element.controller('inventoryItem');
                return element;
            };
        }));

        it('display inventory components', inject(($filter) => {
            const momentize = $filter('TDPMoment');

            createElement();

            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('.preparation-icon-div');
            expect(iconSrc.length).toBe(1);
            expect(element.find('.inventory-title').eq(0).text().indexOf('US States prep')).toBe(0);
            expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains 1 step(s)');
        }));
    });

    describe('folder', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();

            scope.folder = folder;
            scope.preparations = [];

            createElement = () => {
                element = angular.element(`
                    <inventory-item
                            item="folder"
                            details="FOLDER_DETAILS"
                            type="folder">
                    </inventory-item>
                `);
                $compile(element)(scope);
                scope.$digest();
            };
        }));

        it('should display inventory components', inject(($filter) => {
            //given
            const momentize = $filter('TDPMoment');

            //when
            createElement();

            //then
            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('img')[0].src;
            expect(strEndsWith(iconSrc, 'assets/images/folder/folder-icon.png')).toBe(true);
            expect(element.find('.inventory-title').eq(0).text().indexOf('folder 1')).toBe(0);
            expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains 3 preparation(s)');

            expect($(element).find('.folder-icon')[0].hasAttribute('insertion-folder-icon')).toBe(true);
            expect(element.find('.folder-icon').eq(0).attr('folder')).toBe('$ctrl.item');
        }));

        it('should check folder icon attributes existence', inject(($filter) => {
            //when
            createElement();

            //then
            expect($(element).find('.folder-icon')[0].hasAttribute('insertion-folder-icon')).toBe(true);
            expect(element.find('.folder-icon').eq(0).attr('folder')).toBe('$ctrl.item');
        }));
    });

    describe('documentation', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();

            createElement = () => {
                element = angular.element('<inventory-item item="doc" details="{{details}}" type="documentation"></inventory-item>');
                $compile(element)(scope);
                scope.$digest();
                return element;
            };
        }));

        it('display inventory components', () => {
            // given
            scope.doc = { name: 'What is a recipe ?' };
            scope.details = 'This is a recipe';

            // when
            createElement();

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('.documentation-icon-div');
            expect(iconSrc.length).toBe(1);
            expect(element.find('.inventory-title').eq(0).text().indexOf('What is a recipe ?')).toBe(0);
            expect(element.find('.inventory-description').eq(0).text()).toBe('This is a recipe');
        });
    });
});
