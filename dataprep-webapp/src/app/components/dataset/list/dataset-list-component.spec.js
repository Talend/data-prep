/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('DatasetList component', () => {
    function strEndsWith(str, suffix) {
        return str.match(suffix + '$')[0] === suffix;
    }

    let scope;
    let createElement;
    let element;
    let stateMock;
    const datasets = [
        {
            id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
            model: {
                id: '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
                name: 'US States',
                author: 'anonymousUser',
                created: '1437020219741',
                type: 'text/csv',
                certificationStep: 'NONE',
                preparations: [{ name: 'US States prepa' }, { name: 'US States prepa 2' }],
                owner: {
                    displayName: 'anonymousUser',
                },
            }
        },
        {
            id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
            model: {
                id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
                name: 'Customers',
                author: 'anonymousUser',
                created: '143702021974',
                type: 'application/vnd.ms-excel',
                certificationStep: 'PENDING',
                preparations: [{ name: 'Customers prepa' }],
                owner: {
                    displayName: 'anonymousUser',
                },
            }
        },
        {
            id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a',
            model: {
                id: 'e93b9c92-e054-4f6a-a38f-ca52f22ead3a',
                name: 'Customers 2',
                author: 'anonymousUser',
                created: '14370202197',
                certificationStep: 'CERTIFIED',
                preparations: [],
                owner: {
                    displayName: 'anonymousUser',
                },
            }
        },
    ];

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            DATASET_DETAILS: 'owned by {{owner.displayName}}, created {{created | TDPMoment}}, contains {{records}} lines',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(angular.mock.module('data-prep.dataset-list', ($provide) => {
        stateMock = {
            inventory: {
                datasets: datasets,
                isFetchingDatasets: false,
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, $rootScope, $compile, StateService, DatasetService) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
        };
        spyOn(StateService, 'setFetchingInventoryDatasets').and.returnValue();
        spyOn(DatasetService, 'init').and.returnValue($q.when());
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render dataset list', inject(($filter) => {
        //given
        const momentize = $filter('TDPMoment');

        //when
        createElement();

        //then
        expect(element.find('.inventory-item').length).toBe(3);

        let icon = element.find('.inventory-icon').eq(0);
        let iconSrc = icon.find('img')[0].src;
        let certificationIcon = icon.find('.pin');
        expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
        expect(certificationIcon.length).toBe(0);
        expect(element.find('.inventory-title').eq(0).text().indexOf('US States')).toBe(0);
        expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');

        icon = element.find('.inventory-icon').eq(1);
        iconSrc = icon.find('img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/xls_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/inventory/certification-pending.png')).toBe(true);
        expect(element.find('.inventory-title').eq(1).text().indexOf('Customers')).toBe(0);
        expect(element.find('.inventory-description').eq(1).text()).toBe('owned by anonymousUser, created ' + momentize('143702021974') + ', contains  lines');

        icon = element.find('.inventory-icon').eq(2);
        iconSrc = icon.find('img')[0].src;
        certificationIcon = icon.find('.pin')[0].src;
        expect(strEndsWith(iconSrc, '/assets/images/inventory/generic_file.png')).toBe(true);
        expect(strEndsWith(certificationIcon, '/assets/images/inventory/certification-certified.png')).toBe(true);
        expect(element.find('.inventory-title').eq(2).text().indexOf('Customers 2')).toBe(0);
        expect(element.find('.inventory-description').eq(2).text()).toBe('owned by anonymousUser, created ' + momentize('14370202197') + ', contains  lines');
    }));

    it('should not render loading flask', inject(() => {
        expect(element.find('.continuous-rotate').length).toBe(0);
        expect(element.find('.continuous-rotate-text').length).toBe(0);
    }));
});

describe('DatasetList component loading', () => {
    let scope;
    let createElement;
    let element;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.dataset-list', ($provide) => {
        stateMock = {
            inventory: {
                isFetchingDatasets: true,
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(($q, $rootScope, $compile, StateService, DatasetService) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
        };

        spyOn(StateService, 'setFetchingInventoryDatasets').and.returnValue();
        spyOn(DatasetService, 'init').and.returnValue($q.when());
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render loading flask', inject(() => {
        //when
        createElement();

        //then
        expect(element.find('.continuous-rotate').length).toBe(1);
        expect(element.find('.continuous-rotate-text').length).toBe(1);
    }));

    it('should hide loading flask when fetching datasets finishes', inject(() => {
        //given
        createElement();
        expect(element.find('.continuous-rotate').length).toBe(1);
        expect(element.find('.continuous-rotate-text').length).toBe(1);

        //when
        stateMock.inventory.isFetchingDatasets = false;
        scope.$digest();

        //then
        expect(element.find('.continuous-rotate').length).toBe(0);
        expect(element.find('.continuous-rotate-text').length).toBe(0);
    }));
});

describe('DatasetList component with no datasets', () => {
    let scope;
    let createElement;
    let element;
    let stateMock;

    beforeEach(angular.mock.module('data-prep.dataset-list', ($provide) => {
        stateMock = {
            inventory: {
                isFetchingDatasets: false,
            },
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            CLICK_ADD_DATASETS: 'There are no datasets to display. Click on \'Add Dataset\' to add a dataset.',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($q, $rootScope, $compile, StateService, DatasetService, PreparationService) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
        };

        spyOn(StateService, 'setFetchingInventoryDatasets').and.returnValue();
        spyOn(DatasetService, 'init').and.returnValue($q.when());
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should render no datset message', inject(() => {
        //when
        createElement();

        //then
        expect(element.find('.inventory-info-content').length).toBe(1);
        expect(element.find('.inventory-info-content').eq(0).text()).toBe('There are no datasets to display. Click on \'Add Dataset\' to add a dataset.');
    }));
});
