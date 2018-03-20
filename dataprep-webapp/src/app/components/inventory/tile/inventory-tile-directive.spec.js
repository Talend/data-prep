/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Tile Component', () => {
    let htmlConfig;
    let scope;
    let createElement;
    let element;

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en_US', {
            COLON: ': ',
            LAST_MODIFICATION: 'last modified',
            OWNED_BY: 'Owned by',
            ROWS: 'rows',
        });
        $translateProvider.preferredLanguage('en_US');
    }));

    beforeEach(angular.mock.module('data-prep.inventory-tile'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.dataset = {
            model: {
                id: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
                name: 'customers_jso_light',
                author: 'anonymousUser',
                records: 15,
                nbLinesHeader: 1,
                nbLinesFooter: 0,
                created: '03-30-2015 08:06',
            },
        };
        scope.preparation = {
            id: 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            dataSetId: 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            author: 'anonymousUser',
            name: 'my preparation',
            creationDate: 1427447300000,
            lastModificationDate: 1427447300300,
            steps: [
                '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82b',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82c',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82d',
            ],
            favorite: false,
            actions: [
                {
                    action: 'lowercase',
                    parameters: { column_name: 'birth' },
                },
            ],
        };
        scope.onClick = jasmine.createSpy('onClick');
        scope.onTitleClick = jasmine.createSpy('onTitleClick');
        scope.onClone = jasmine.createSpy('onClone');
        scope.onFavorite = jasmine.createSpy('onFavorite');
        scope.onRemove = jasmine.createSpy('onRemove');
        scope.onRename = jasmine.createSpy('onRename');

        htmlConfig = {};

        createElement = () => {
            const html = `
                <inventory-tile
                    dataset="dataset"
                    preparation="preparation"
                    on-click="onClick()"
                    on-title-click="onTitleClick()"

                    ${htmlConfig.clone ? 'on-clone="onClone(preparation)"' : ''}
                    ${htmlConfig.favorite ? 'on-favorite="onFavorite(preparation)"' : ''}
                    ${htmlConfig.remove ? 'on-remove="onRemove(preparation)"' : ''}
                    ${htmlConfig.rename ? 'on-rename="onRename(preparation, text)"' : ''}
                ></inventory-tile>`;

            element = angular.element(html);
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render an editable title', () => {
            // given
            htmlConfig.rename = true;

            // when
            createElement();

            // then
            expect(element.find('talend-editable-text').length).toBe(1);
        });

        it('should render an fixed (non editable) title', () => {
            // given
            htmlConfig.rename = false;

            // when
            createElement();

            // then
            expect(element.find('span.title').eq(0).text().trim()).toBe('my preparation');
        });

        it('should render the details', inject(($filter) => {
            // given
            const momentize = $filter('TDPMoment');

            // when
            createElement();

            // then
            const description = element.find('.description').eq(0).text().trim().replace(/\s+/g, ' ');
            const details = element.find('.details').eq(0).text().trim().replace(/\s+/g, ' ');

            expect(description).toBe(`Owned by anonymousUser last modified : ${momentize('1427447300300')}`);
            expect(details).toBe('customers_jso_light - 15 rows');
        }));

        it('should render the favorite icon', () => {
            // given
            htmlConfig.favorite = true;

            // when
            createElement();

            // then
            expect(element.find('a.favorite').length).toBe(1);
        });

        it('should NOT render the favorite icon', () => {
            // given
            htmlConfig.favorite = false;

            // when
            createElement();

            // then
            expect(element.find('a.favorite').length).toBe(0);
        });

        it('should render the clone icon', () => {
            // given
            htmlConfig.clone = true;

            // when
            createElement();

            // then
            expect(element.find('a.clone').length).toBe(1);
        });

        it('should NOT render the clone icon', () => {
            // given
            htmlConfig.clone = false;

            // when
            createElement();

            // then
            expect(element.find('a.clone').length).toBe(0);
        });

        it('should render the remove icon', () => {
            // given
            htmlConfig.remove = true;

            // when
            createElement();

            // then
            expect(element.find('a.remove').length).toBe(1);
        });

        it('should NOT render the remove icon', () => {
            // given
            htmlConfig.remove = false;

            // when
            createElement();

            // then
            expect(element.find('a.remove').length).toBe(0);
        });
    });

    describe('events', () => {
        it('should call click callback', () => {
            // given
            createElement();
            expect(scope.onClick).not.toHaveBeenCalled();

            // when
            element.find('.inventory-tile').eq(0).click();

            // then
            expect(scope.onClick).toHaveBeenCalled();
        });

        it('should call title click callback', () => {
            // given
            createElement();
            expect(scope.onTitleClick).not.toHaveBeenCalled();

            // when
            element.find('.title').eq(0).click();

            // then
            expect(scope.onTitleClick).toHaveBeenCalled();
        });

        it('should call clone callback', () => {
            // given
            htmlConfig.clone = true;
            createElement();
            expect(scope.onClone).not.toHaveBeenCalled();

            // when
            element.find('a.clone').eq(0).click();

            // then
            expect(scope.onClone).toHaveBeenCalled();
        });

        it('should call favorite callback', () => {
            // given
            htmlConfig.favorite = true;
            createElement();
            expect(scope.onFavorite).not.toHaveBeenCalled();

            // when
            element.find('a.favorite').eq(0).click();

            // then
            expect(scope.onFavorite).toHaveBeenCalled();
        });

        it('should call remove callback', () => {
            // given
            htmlConfig.remove = true;
            createElement();
            expect(scope.onRemove).not.toHaveBeenCalled();

            // when
            element.find('a.remove').eq(0).click();

            // then
            expect(scope.onRemove).toHaveBeenCalled();
        });
    });
});
