/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder Item Component', () => {

    beforeEach(angular.mock.module('data-prep.folder-selection'));
    beforeEach(angular.mock.module('htmlTemplates'));

    let createElement, scope, element;

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        createElement = () => {
            element = angular.element(
                `<folder-item
                    item="item"
                    on-toggle="toggle(folder)"
                    on-select="chooseFolder(folder)"
                ></folder-item>`
            );

            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    it('should show the name of the item', () => {
        //given
        scope.item = {
            name: '1-folder1',
            path: '1-folder1',
            level: 1,
            searchResult: false
        };

        //when
        createElement();

        //then
        expect(element.text().trim()).toBe(scope.item.name);
    });

    describe('render a folder', () => {
        it('should highlight selected folder', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                selected: true
            };

            //when
            createElement();

            //then
            expect(element.find('li').eq(0).hasClass('folder-selected')).toBe(true);
        });

        it('should not highlight a non-selected folder', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                selected: false
            };

            //when
            createElement();

            //then
            expect(element.find('li').eq(0).hasClass('folder-selected')).toBe(false);
        });

        it('should render a collapsed folder', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                collapsed: true
            };

            //when
            createElement();

            //then
            expect(element.find('.icon-img').eq(0).attr('src')).toBe('assets/images/folder/folder_close_small-icon.png');
        });

        it('should render a not collapsed folder', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                collapsed: false
            };

            //when
            createElement();

            //then
            expect(element.find('.icon-img').eq(0).attr('src')).toBe('assets/images/folder/folder_open_small-icon.png');
        });
    });

    describe('render a caret', () => {

        it('should render a folder with no children', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                hasNoChildren: true
            };

            //when
            createElement();

            //then
            expect(element.find('.dropdown-caret-down').length).toBe(0);
            expect(element.find('.dropdown-caret-right').length).toBe(0);
            expect(element.find('.empty-caret').length).toBe(1);
        });

        it('should render a collapsed caret', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                collapsed: true
            };

            //when
            createElement();

            //then
            expect(element.find('.dropdown-caret-down').length).toBe(1);
            expect(element.find('.dropdown-caret-right').length).toBe(0);
            expect(element.find('.empty-caret').length).toBe(0);
        });

        it('should render a not collapsed caret', () => {
            //given
            scope.item = {
                name: '1-folder1',
                path: '1-folder1',
                level: 1,
                collapsed: false
            };

            //when
            createElement();

            //then
            expect(element.find('.dropdown-caret-down').length).toBe(0);
            expect(element.find('.dropdown-caret-right').length).toBe(1);
            expect(element.find('.empty-caret').length).toBe(0)
        });
    });

    describe('render folder item with right level', () => {
        it('should render the folder with a left space corresponding to its level', () => {
            //given
            scope.item = {
                name: '5-folder1',
                path: '///5-folder1',
                level: 5,
                showFolder: true
            };

            //when
            createElement();

            //then
            expect(element.find('li').eq(0).css('margin-left')).toBe(20 * scope.item.level + 'px');
        });
    });

    describe('clicks', () => {
        var controller;
        beforeEach(() => {
            createElement();
            controller = element.controller('folderItem');
            controller.onToggle = jasmine.createSpy('onToggle');
            controller.onSelect = jasmine.createSpy('onSelect');
        });

        describe('toggle', () => {
            it('should trigger callback on opened caret simple click', () => {
                //given
                controller.item = {
                    name: '1-folder1',
                    path: '1-folder1',
                    level: 1,
                    collapsed: true
                };
                scope.$digest();

                //when
                element.find('.dropdown-caret-down').eq(0).click();

                //then
                expect(controller.onToggle).toHaveBeenCalledWith({folder: controller.item});
            });

            it('should trigger callback on closed caret simple click', () => {
                //given
                controller.item = {
                    name: '1-folder1',
                    path: '1-folder1',
                    level: 1,
                    collapsed: false
                };
                scope.$digest();

                //when
                element.find('.dropdown-caret-right').eq(0).click();

                //then
                expect(controller.onToggle).toHaveBeenCalledWith({folder: controller.item});
            });

            it('should trigger callback on line double click', () => {
                //given
                controller.item = {
                    name: '1-folder1',
                    path: '1-folder1',
                    level: 1,
                    collapsed: true
                };
                scope.$digest();

                //when
                element.find('li').eq(0).dblclick();

                //then
                expect(controller.onToggle).toHaveBeenCalledWith({folder: controller.item});
            });
        });

        describe('select', () => {
            it('should trigger callback on name click', () => {
                //given
                controller.item = {
                    name: '1-folder1',
                    path: '1-folder1',
                    level: 1,
                    collapsed: true
                };
                scope.$digest();

                //when
                element.find('#1-folder1').click();

                //then
                expect(controller.onSelect).toHaveBeenCalledWith({folder: controller.item});
            });

            it('should trigger callback on folder image simple click', () => {
                //given
                controller.item = {
                    name: '1-folder1',
                    path: '1-folder1',
                    level: 1,
                    collapsed: true
                };
                scope.$digest();

                //when
                element.find('.icon-img').eq(0).click();

                //then
                expect(controller.onSelect).toHaveBeenCalledWith({folder: controller.item});
            });
        });
    });
});