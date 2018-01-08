/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder Tree Node Component', () => {
    let createElement;
    let scope;
    let element;

    beforeEach(angular.mock.module('data-prep.folder-selection'));

    beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
        $translateProvider.translations('en', {
            FOLDER_PATH: '(Path: {{path}})',
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            element = angular.element(
                `<folder-tree-node
                    name="name"
                    path="path"
                    level="level"
                    is-opened="isOpened"
                    has-children="hasChildren"
                    is-selected="isSelected"
                    on-toggle="onToggle()"
                    on-select="onSelect()"></folder-tree-node>`
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

    it('should display the name', () => {
        //given
        scope.name = 'toto';

        //when
        createElement();

        //then
        expect(element.text().trim()).toBe('toto');
    });

    it('should display the path next to the name if provided', () => {
        //given
        scope.name = 'toto';
        scope.path = '/a/b/c';

        //when
        createElement();

        //then
        expect(element.text().trim()).toEqual('toto (Path: /a/b/c)');
    });

    describe('selected', () => {
        it('should highlight selected folder', () => {
            //given
            scope.isSelected = true;

            //when
            createElement();

            //then
            expect(element.find('.folder-tree-node').eq(0).hasClass('folder-selected')).toBe(true);
        });

        it('should NOT highlight NOT selected folder', () => {
            //given
            scope.isSelected = false;

            //when
            createElement();

            //then
            expect(element.find('.folder-tree-node').eq(0).hasClass('folder-selected')).toBe(false);
        });
    });

    describe('caret', () => {
        it('should render an empty caret when there is no children', () => {
            //given
            scope.hasChildren = false;

            //when
            createElement();

            //then
            expect(element.find('.folder-caret').hasClass('caret')).toBe(false);
        });

        it('should render a collapsed caret', () => {
            //given
            scope.hasChildren = true;
            scope.isOpened = false;

            //when
            createElement();

            //then
            expect(element.find('.folder-caret').hasClass('closed')).toBe(true);
            expect(element.find('.folder-caret').hasClass('caret')).toBe(true);
        });

        it('should render an opened caret', () => {
            //given
            scope.hasChildren = true;
            scope.isOpened = true;

            //when
            createElement();

            //then
            expect(element.find('.folder-caret').hasClass('closed')).toBe(false);
            expect(element.find('.folder-caret').hasClass('caret')).toBe(true);
        });
    });

    describe('icon', () => {
        it('should render a closed folder', () => {
            //given
            scope.isOpened = false;

            //when
            createElement();

            //then
            expect(element.find('.icon-img').eq(0).attr('src')).toBe('assets/images/folder/folder_close_small-icon.png');
        });

        it('should render an opened folder', () => {
            //given
            scope.isOpened = true;

            //when
            createElement();

            //then
            expect(element.find('.icon-img').eq(0).attr('src')).toBe('assets/images/folder/folder_open_small-icon.png');
        });
    });

    describe('level padding', () => {
        it('should render a left space corresponding to the level', () => {
            //given
            scope.level = 5;

            //when
            createElement();

            //then
            expect(element.find('.folder-tree-node').eq(0).css('padding-left')).toBe(20 * 5 + 'px');
        });
    });

    describe('toggle', () => {
        it('should trigger callback on caret simple click', () => {
            //given
            scope.onToggle = jasmine.createSpy('onToggle');
            scope.hasChildren = true;
            scope.isOpened = true;
            createElement();

            expect(scope.onToggle).not.toHaveBeenCalled();

            //when
            element.find('.caret').eq(0).click();

            //then
            expect(scope.onToggle).toHaveBeenCalled();
        });

        it('should trigger callback on line double click', () => {
            //given
            scope.onToggle = jasmine.createSpy('onToggle');
            createElement();

            expect(scope.onToggle).not.toHaveBeenCalled();

            //when
            element.find('.folder-tree-node').eq(0).dblclick();

            //then
            expect(scope.onToggle).toHaveBeenCalled();
        });
    });

    describe('select', () => {
        it('should trigger callback on name click', () => {
            //given
            scope.onSelect = jasmine.createSpy('onSelect');
            createElement();

            expect(scope.onSelect).not.toHaveBeenCalled();

            //when
            element.find('.folder-name').eq(0).click();

            //then
            expect(scope.onSelect).toHaveBeenCalled();
        });

        it('should trigger callback on icon click', () => {
            //given
            scope.onSelect = jasmine.createSpy('onSelect');
            createElement();

            expect(scope.onSelect).not.toHaveBeenCalled();

            //when
            element.find('.folder-icon').eq(0).click();

            //then
            expect(scope.onSelect).toHaveBeenCalled();
        });

        it('should trigger callback on folder image simple click', () => {
            //given
            scope.onSelect = jasmine.createSpy('onSelect');
            createElement();

            expect(scope.onSelect).not.toHaveBeenCalled();

            //when
            element.find('.icon-img').eq(0).click();

            //then
            expect(scope.onSelect).toHaveBeenCalled();
        });
    });
});
