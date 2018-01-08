/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Folder Tree Component', () => {
    let createElement;
    let scope;
    let element;
    let node;

    beforeEach(angular.mock.module('data-prep.folder-selection'));

    beforeEach(inject(($rootScope, $compile) => {
        node = {
            folder: {
                id: '1',
                name: 'toto',
                path: '/path/to/toto',
                opened: false,
                selected: true,
            },
            children: [
                {
                    folder: {
                        id: '2',
                        name: 'tata',
                        path: '/path/to/toto/tata,',
                    },
                },
            ],
        };

        scope = $rootScope.$new(true);
        scope.node = node;

        createElement = () => {
            element = angular.element(
                `<folder-tree node="node"
                              level="level"
                              on-toggle="onToggle(node)"
                              on-select="onSelect(node)"></folder-tree>`
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

    it('should render node', () => {
        //given
        scope.level = 2;

        //when
        createElement();

        //then
        expect(element.find('.folder-tree-node').length).toBe(1);
        expect(element.find('.folder-tree-node').eq(0).hasClass('folder-selected')).toBe(true);
        expect(element.find('.folder-tree-node').eq(0).text().trim()).toBe('toto'); // name
        expect(element.find('.folder-tree-node').eq(0).css('padding-left')).toBe(20 * 2 + 'px'); // level
        expect(element.find('.folder-tree-node').eq(0).find('.caret').length).toBe(1); // caret
    });

    describe('children', () => {
        it('should render children when node has some and is opened', () => {
            //given
            node.folder.opened = true;

            //when
            createElement();

            //then
            expect(element.find('folder-tree').length).toBe(1);
        });

        it('should NOT render children when node has some and is closed', () => {
            //given
            node.folder.opened = false;

            //when
            createElement();

            //then
            expect(element.find('folder-tree').length).toBe(0);
        });

        it('should NOT render children when node has no children', () => {
            //given
            node.folder.opened = true;
            node.children = [];

            //when
            createElement();

            //then
            expect(element.find('folder-tree').length).toBe(0);
        });
    });

    describe('toggle', () => {
        it('should trigger callback on current node toggle', () => {
            //given
            scope.onToggle = jasmine.createSpy('onToggle');
            createElement();

            expect(scope.onToggle).not.toHaveBeenCalled();

            //when
            element.find('.folder-tree-node').eq(0).dblclick();

            //then
            expect(scope.onToggle).toHaveBeenCalledWith(node);
        });

        it('should trigger callback on children toggle', () => {
            //given
            scope.onToggle = jasmine.createSpy('onToggle');
            node.folder.opened = true;
            createElement();

            expect(scope.onToggle).not.toHaveBeenCalled();

            //when
            element.find('.folder-tree-node').eq(1).dblclick();

            //then
            expect(scope.onToggle).toHaveBeenCalledWith(node.children[0]);
        });
    });

    describe('select', () => {
        it('should trigger callback on current node selection', () => {
            //given
            scope.onSelect = jasmine.createSpy('onSelect');
            createElement();

            expect(scope.onSelect).not.toHaveBeenCalled();

            //when
            element.find('.folder-icon').eq(0).click();

            //then
            expect(scope.onSelect).toHaveBeenCalledWith(node);
        });

        it('should trigger callback on children selection', () => {
            //given
            scope.onSelect = jasmine.createSpy('onSelect');
            node.folder.opened = true;
            createElement();

            expect(scope.onSelect).not.toHaveBeenCalled();

            //when
            element.find('.folder-icon').eq(1).click();

            //then
            expect(scope.onSelect).toHaveBeenCalledWith(node.children[0]);
        });
    });
});
