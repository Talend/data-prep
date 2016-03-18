/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('dataset copy move component', () => {
    var scope, createElement, element, controller;

    beforeEach(angular.mock.module('data-prep.dataset-copy-move'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();
        scope.dataset = {name: 'my dataset'};
        scope.initialFolder = {path: '0-folder1/1-folder11'};

        createElement = () => {
            element = angular.element(
                `<dataset-copy-move
                    initial-folder="initialFolder"
                    dataset="dataset"
                    on-copy="clone(dataset, destination, name)"
                    on-move="move(dataset, destination, name)"
                ></dataset-copy-move>`
            );

            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('datasetCopyMove');
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
            expect(element.find('.clone-name > input').length).toBe(1);
            expect(element.find('button').length).toBe(3);
        });
    });

    describe('form', () => {
        it('should disable submit buttons when form is invalid', () => {
            //given
            createElement();
            var cancelBtn = element.find('#cancel-move-copy-btn').eq(0);
            var cloneBtn = element.find('#clone-ds-btn').eq(0);
            var moveBtn = element.find('#move-ds-btn').eq(0);

            expect(cancelBtn.attr('disabled')).toBeFalsy();
            expect(cloneBtn.find('button').attr('disabled')).toBeFalsy();
            expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

            //when
            controller.newDsName = '';
            scope.$digest();

            //then
            expect(controller.copyMoveForm.$invalid).toBe(true);
            expect(cancelBtn.attr('disabled')).toBeFalsy();
            expect(cloneBtn.find('button').attr('disabled')).toBe('disabled');
            expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
        });

        describe('move', () => {
            beforeEach(() => {
                createElement();
                controller.move = jasmine.createSpy('move');
            });

            it('should move dataset on move button click', () => {
                //given
                var moveBtn = element.find('#move-ds-btn').eq(0);

                //when
                moveBtn.click();

                //then
                expect(controller.move).toHaveBeenCalled();
            });

            it('should disable submit buttons while moving', () => {
                //given
                var cancelBtn = element.find('#cancel-move-copy-btn').eq(0);
                var cloneBtn = element.find('#clone-ds-btn').eq(0);
                var moveBtn = element.find('#move-ds-btn').eq(0);

                expect(cancelBtn.attr('disabled')).toBeFalsy();
                expect(cloneBtn.find('button').attr('disabled')).toBeFalsy();
                expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

                //when
                controller.isMovingDs = true;
                scope.$digest();

                //then
                expect(cancelBtn.attr('disabled')).toBe('disabled');
                expect(cloneBtn.find('button').attr('disabled')).toBe('disabled');
                expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
            });
        });

        describe('clone', () => {

            beforeEach(() => {
                createElement();
                controller.clone = jasmine.createSpy('clone');
            });

            it('should clone dataset on clone button click', () => {
                //given
                var cloneBtn = element.find('#clone-ds-btn').eq(0);

                //when
                cloneBtn.click();

                //then
                expect(controller.clone).toHaveBeenCalled();
            });

            it('should disable submit buttons while cloning', () => {
                //given
                var cancelBtn = element.find('#cancel-move-copy-btn').eq(0);
                var cloneBtn = element.find('#clone-ds-btn').eq(0);
                var moveBtn = element.find('#move-ds-btn').eq(0);

                expect(cancelBtn.attr('disabled')).toBeFalsy();
                expect(cloneBtn.find('button').attr('disabled')).toBeFalsy();
                expect(moveBtn.find('button').attr('disabled')).toBeFalsy();

                //when
                controller.isCloningDs = true;
                scope.$digest();

                //then
                expect(cancelBtn.attr('disabled')).toBe('disabled');
                expect(cloneBtn.find('button').attr('disabled')).toBe('disabled');
                expect(moveBtn.find('button').attr('disabled')).toBe('disabled');
            });
        });
    });
});