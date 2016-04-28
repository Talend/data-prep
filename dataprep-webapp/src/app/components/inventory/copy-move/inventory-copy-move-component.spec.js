/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('InventoryCopyMove component', () => {
    let scope, createElement, element, controller;

    beforeEach(angular.mock.module('data-prep.inventory-copy-move'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.item = {name: 'my item'};
        scope.initialFolder = {path: '0-folder1/1-folder11'};

        createElement = () => {
            element = angular.element(
                `<inventory-copy-move
                    initial-folder="initialFolder"
                    item="item"
                    on-copy="copy(item, destination, name)"
                    on-move="move(item, destination, name)"
                ></inventory-copy-move>`
            );

            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('inventoryCopyMove');
        };
    }));

    beforeEach(inject(($q, FolderService) => {
        spyOn(FolderService, 'children').and.returnValue($q.when({data: []}));
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('render', () => {
        it('should render copy/move elements', () => {
            //when
            createElement();
            scope.$digest();

            //then
            expect(element.find('folder-selection').length).toBe(1);
            expect(element.find('input#copy-move-name-input').length).toBe(1);
            expect(element.find('button').length).toBe(3);
        });
    });

    describe('form', () => {
        it('should disable submit buttons when form is invalid', () => {
            //given
            createElement();
            const cancelBtn = element.find('#copy-move-cancel-btn').eq(0);
            const copyBtn = element.find('#copy-move-copy-btn').eq(0);
            const moveBtn = element.find('#copy-move-move-btn').eq(0);

            expect(cancelBtn.attr('disabled')).toBeFalsy();
            expect(copyBtn.find('button').attr('disabled')).toBeFalsy();
            expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

            //when
            controller.newName = '';
            scope.$digest();

            //then
            expect(controller.copyMoveForm.$invalid).toBe(true);
            expect(cancelBtn.attr('disabled')).toBeFalsy();
            expect(copyBtn.find('button').attr('disabled')).toBe('disabled');
            expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
        });

        describe('move', () => {
            beforeEach(() => {
                createElement();
                controller.move = jasmine.createSpy('move');
            });

            it('should move item on move button click', () => {
                //given
                const moveBtn = element.find('#copy-move-move-btn').eq(0);

                //when
                moveBtn.click();

                //then
                expect(controller.move).toHaveBeenCalled();
            });

            it('should disable submit buttons while moving', () => {
                //given
                const cancelBtn = element.find('#copy-move-cancel-btn').eq(0);
                const copyBtn = element.find('#copy-move-copy-btn').eq(0);
                const moveBtn = element.find('#copy-move-move-btn').eq(0);

                expect(cancelBtn.attr('disabled')).toBeFalsy();
                expect(copyBtn.find('button').attr('disabled')).toBeFalsy();
                expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

                //when
                controller.isMoving = true;
                scope.$digest();

                //then
                expect(cancelBtn.attr('disabled')).toBe('disabled');
                expect(copyBtn.find('button').attr('disabled')).toBe('disabled');
                expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
            });
        });

        describe('copy', () => {

            beforeEach(() => {
                createElement();
                controller.copy = jasmine.createSpy('copy');
            });

            it('should copy item on copy button click', () => {
                //given
                const copyBtn = element.find('#copy-move-copy-btn').eq(0);

                //when
                copyBtn.click();

                //then
                expect(controller.copy).toHaveBeenCalled();
            });

            it('should disable submit buttons while copying', () => {
                //given
                const cancelBtn = element.find('#copy-move-cancel-btn').eq(0);
                const copyBtn = element.find('#copy-move-copy-btn').eq(0);
                const moveBtn = element.find('#copy-move-move-btn').eq(0);

                expect(cancelBtn.attr('disabled')).toBeFalsy();
                expect(copyBtn.find('button').attr('disabled')).toBeFalsy();
                expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

                //when
                controller.isCopying = true;
                scope.$digest();

                //then
                expect(cancelBtn.attr('disabled')).toBe('disabled');
                expect(copyBtn.find('button').attr('disabled')).toBe('disabled');
                expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
            });
        });
    });
});