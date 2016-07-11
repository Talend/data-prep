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
            OPEN_ACTION: 'Open {{type}} \"{{name}}\"',
            DATASET_DETAILS: 'owned by {{owner.displayName}}, created {{created | TDPMoment}}, contains {{records}} lines',
            PREPARATION_DETAILS: 'owned by {{owner.displayName}}, created {{creationDate | TDPMoment}}, contains {{steps.length -1}} step(s)',
            FOLDER_DETAILS: 'owned by {{owner.displayName}}, created {{creationDate | TDPMoment}}, contains {{nbPreparations}} preparation(s)',
            COPY_MOVE_ACTION: 'Copy or Move {{type}} \"{{name}}\"',
            COPY_TO_ACTION: 'Copy {{type}} \"{{name}}\"',
            DELETE_ACTION: 'Delete {{type}} \"{{name}}\"',
            CERTIFY_ACTION: 'Certify {{type}} \"{{name}}\"',
            FAVORITE_ACTION: 'Add {{type}} \"{{name}}\" in your favorites',
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
            const iconSrc = icon.find('img')[0].src;
            expect(strEndsWith(iconSrc, '/assets/images/inventory/csv_file.png')).toBe(true);
        });

        it('should select XLS icon', () => {
            // when
            createElement(xls_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('img')[0].src;
            expect(strEndsWith(iconSrc, '/assets/images/inventory/xls_file.png')).toBe(true);
        });

        it('should select JOB icon', () => {
            // when
            createElement(job_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('img')[0].src;
            expect(strEndsWith(iconSrc, '/assets/images/inventory/live_dataset_file.png')).toBe(true);
        });

        it('should select JOB icon for components tag', () => {
            // when
            createElement(job_created_dataset);

            // then
            const icon = element.find('.inventory-icon').eq(0);
            const iconSrc = icon.find('img')[0].src;
            expect(strEndsWith(iconSrc, '/assets/images/inventory/job_file.png')).toBe(true);
        });

        it('should not display update for job dattaset', () => {
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
            scope.openDataset = () => {
            };

            scope.openRelatedInventory = () => {
            };

            scope.copy = () => {
            };

            scope.processCertif = () => {
            };

            scope.rename = () => {
            };

            scope.open = () => {
            };

            scope.update = () => {
            };

            scope.remove = () => {
            };

            scope.openRelatedInv = () => {
            };

            scope.toggleFavorite = () => {
            };

            scope.preparations = [];
            createElement = () => {
                element = angular.element('<inventory-item ' +
                    'item="dataset" ' +
                    'details="DATASET_DETAILS" ' +
                    'type="dataset" ' +
                    'related-inventories="preparations" ' +
                    'related-inventories-type="preparation" ' +
                    'open-related-inventory="openRelatedInventory" ' +
                    'open="open" ' +
                    'open-enabled="open" ' +
                    'process-certification="processCertif" ' +
                    'copy="copy" ' +
                    'rename="rename" ' +
                    'rename-enabled="rename"' +
                    'remove="remove" ' +
                    'toggle-favorite="toggleFavorite" ' +
                    'update="update" ' +
                    'process-certification-enabled="true"' +
                    'toggle-favorite-enabled="toggleFavorite"' +
                    'remove-enabled="remove"' +
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

            it('should display inventory icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('.inventory-icon').eq(0).attr('title').trim();
                expect(title).toBe('Open ' + ctrl.type + ' "' + dataset.name + '"');
            });

            it('should display inventory details', inject(($filter) => {
                // given
                const momentize = $filter('TDPMoment');

                // when
                createElement();

                // then
                expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created ' + momentize('1437020219741') + ', contains  lines');
            }));

            it('should display inventory title', () => {
                // when
                createElement();

                // then
                const title = element.find('talend-editable-text').eq(0).text().trim();
                expect(title).toBe(dataset.name);
            });

            it('should NOT display bottle icon: no related inventories', () => {
                // when
                createElement();

                // then
                expect(element.find('.inventory-actions-related-item').length).toBe(0);
            });

            it('should display bottle icon: at least 1 related inventory', () => {
                // given
                scope.preparations = dataset.preparations;

                // when
                createElement();

                // then
                expect(element.find('.inventory-actions-related-item').length).toBe(1);
                const menuItems = element.find('.inventory-actions-related-item-menu > li');
                expect(menuItems.length).toBe(3);
                const relatedPrepsList = menuItems.eq(2).text().trim();
                expect(relatedPrepsList.indexOf(dataset.preparations[1].name) > -1).toBeTruthy();
            });

            it('should display update icon', () => {
                // when
                createElement();

                // then
                const icon = element.find('talend-file-selector').attr('button-data-icon');
                expect(icon).toBe('E');
            });
            it('should display update icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('talend-file-selector').attr('button-title');
                expect(title).toBe('REPLACE_FILE_CONTENT');
            });

            it('should display 2 dividers', () => {
                // when
                createElement();

                // then
                expect(element.find('.divider').length).toBe(2);
            });

            it('should display inventory-title when only open is enabled', () => {
                // given
                scope.rename = null;

                // when
                createElement();

                // then
                const icon = element.find('a');
                expect(icon.hasClass('inventory-title')).toBe(true);
            });

            it('should display copy icon', () => {
                // when
                createElement();

                // then
                const icon = element.find('a').eq(1).attr('data-icon');
                expect(icon).toBe('B');
            });
            it('should display copy icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('a').eq(1).attr('title');
                expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
            });

            it('should display remove icon', () => {
                // when
                createElement();

                // then
                const icon = element.find('a').eq(2).attr('data-icon');
                expect(icon).toBe('e');
            });
            it('should display remove icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('a').eq(2).attr('title');
                expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
            });

            it('should display certify icon', () => {
                // when
                createElement();

                // then
                const icon = element.find('a').eq(3).attr('data-icon');
                expect(icon).toBe('n');
            });
            it('should display certify icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('a').eq(3).attr('title');
                expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
            });

            it('should display favorite icon', () => {
                // when
                createElement();

                // then
                const icon = element.find('a').eq(4).attr('data-icon');
                expect(icon).toBe('f');
            });

            it('should NOT display favorite icon', () => {
                // when
                scope.toggleFavorite = null;
                createElement();

                // then
                expect(element.find('.favorite').length).toBe(0);
            });

            it('should display favorite icon tooltip', () => {
                // when
                createElement();

                // then
                const title = element.find('a').eq(4).attr('title');
                expect(title.indexOf(dataset.name) >= 0).toBeTruthy();
            });

            it('should display READONLY title', () => {
                // given
                scope.rename = null;
                scope.open = null;

                // when
                createElement();

                // then
                const title = element.find('.inventory-text > span').eq(0).text().trim();
                expect(title).toBe(dataset.name);
            });

            it('should NOT display remove icon', () => {
                // given
                scope.remove = null;

                // when
                createElement();

                var links = element.find('a');

                // then
                for (var i = 0; i < links.length; i++) {
                    const icon = element.find('a').eq(i).attr('data-icon');
                    expect(icon).not.toBe('e');
                }
            });
        });

        describe('actions on inventory components', () => {
            beforeEach(inject(() => {
                scope.openRelatedInventory = jasmine.createSpy('openRelatedInventory');
                scope.open = jasmine.createSpy('open');
                scope.rename = jasmine.createSpy('rename');
                scope.update = jasmine.createSpy('update');
                scope.copy = jasmine.createSpy('copy');
                scope.remove = jasmine.createSpy('remove');
                scope.processCertif = jasmine.createSpy('processCertif');
                scope.toggleFavorite = jasmine.createSpy('toggleFavorite');

                dataset.draft = false;
            }));

            it('should open inventory item on file icon click', () => {
                // given
                createElement();
                const icon = element.find('.inventory-icon');
                const event = angular.element.Event('click');

                // when
                icon.eq(0).trigger(event);

                // then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            });

            it('should open draft dataset on inventory title click', () => {
                scope.rename = null;
                dataset.draft = true;

                // given
                createElement();
                const click = angular.element.Event('click');
                const title = element.find('.inventory-text > span').eq(0);

                // when
                title.trigger(click);

                // then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            });

            it('should open inventory item on element dblclick', () => {
                // given
                createElement();
                const inventory = element.find('.inventory-item');

                // when
                inventory.dblclick();

                // then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            });
            it('should open related inventory item on bottle click', () => {
                // given
                scope.preparations = [{}, {}];
                createElement();
                const bottle = element.find('.inventory-actions-related-item .button-dropdown-main').eq(0);

                // when
                bottle.click();

                // then
                expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[0]);
            });
            it('should open new inventory item and not the related inventory', () => {
                // given
                scope.preparations = [{}, {}];
                createElement();
                const newPreparation = element.find('.inventory-actions-related-item-menu > li').eq(0);
                const event = angular.element.Event('click');

                // when
                newPreparation.trigger(event);

                // then
                expect(ctrl.open).toHaveBeenCalledWith(dataset);
            });
            it('should open 2nd related inventory item', () => {
                // given
                scope.preparations = [{}, {}];
                createElement();
                const secRelatedInv = element.find('.inventory-actions-related-item-menu > li').eq(2);

                // when
                secRelatedInv.click();

                // then
                expect(ctrl.openRelatedInventory).toHaveBeenCalledWith(scope.preparations[1]);
            });

            it('should copy/clone inventory item on clone button click', () => {
                // given
                createElement();
                const cloneBtn = element.find('a').eq(1);

                // when
                cloneBtn.click();

                // then
                expect(ctrl.copy).toHaveBeenCalled();
            });
            it('should remove inventory item on basket button click', () => {
                // given
                createElement();
                const basketBtn = element.find('a').eq(2);

                // when
                basketBtn.click();

                // then
                expect(ctrl.remove).toHaveBeenCalled();
            });
            it('should certify inventory item on certification button click', () => {
                // given
                createElement();
                const certificationBtn = element.find('a').eq(3);

                // when
                certificationBtn.click();

                // then
                expect(ctrl.processCertification).toHaveBeenCalled();
            });
            it('should favorite inventory item on favorite button click', () => {
                // given
                createElement();
                const favoriteBtn = element.find('a').eq(4);

                // when
                favoriteBtn.click();

                // then
                expect(ctrl.toggleFavorite).toHaveBeenCalled();
            });
        });
    });

    describe('preparation', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();

            scope.preparation = preparation;

            scope.rename = () => {
            };

            scope.open = () => {
            };

            scope.remove = () => {
            };

            scope.preparations = [];
            createElement = () => {
                element = angular.element('<inventory-item ' +
                    'item="preparation" ' +
                    'details="PREPARATION_DETAILS" ' +
                    'type="preparation" ' +
                    'open="open" ' +
                    'rename="rename" ' +
                    'remove="remove" ' +
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

            scope.rename = () => {
            };

            scope.open = () => {
            };

            scope.remove = () => {
            };

            createElement = () => {
                element = angular.element(`
                    <inventory-item
                        item="folder"
                        details="FOLDER_DETAILS"
                        type="folder"
                        open="open"
                        rename="rename"
                        remove="remove"></inventory-item>
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

    describe('insertion points', () => {
        beforeEach(inject(($rootScope, $compile) => {
            scope = $rootScope.$new();

            createElement = () => {
                element = angular.element('<inventory-item item="folder" type="folder"></inventory-item>');
                $compile(element)(scope);
                scope.$digest();
                return element;
            };
        }));

        it('should render insertion inventory actions attribute', () => {
            // given
            scope.folder = folder;

            // when
            createElement();

            // then
            const actions = element.find('.inventory-actions').eq(0);
            expect(actions[0].hasAttribute('insertion-inventory-actions')).toBe(true);
        });
    });
});
