/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Quality bar directive', () => {

    let scope;
    let element;
    let createElement;
    let controller;

    beforeEach(angular.mock.module('talend.widget'));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();

        createElement = () => {
            const html = `<quality-bar  quality="quality"
                                        valid-items-has-menu="validItemsHasMenu"
                                        invalid-items-has-menu="invalidItemsHasMenu"
                                        empty-items-has-menu="emptyMenuItems"
                                        enter-animation="enterAnimation"></quality-bar>`;
            element = $compile(html)(scope);
            scope.$digest();

            controller = element.controller('qualityBar');
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('with enter animation', () => {
        it(' should enable transition', () => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.enterAnimation = true;
            createElement();

            // when
            scope.$digest();

            // then
            expect(controller.blockTransition).toBe(false);
        });

        it(' should reset the width object', () => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.enterAnimation = true;
            createElement();

            // when
            scope.$digest();

            // then
            expect(controller.width).toEqual({
                invalid: 0,
                empty: 0,
                valid: 0,
            });
        });

        it(' compute percentage and width after a 300ms timeout', inject(($timeout) => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.enterAnimation = true;
            createElement();

            // when
            scope.$digest();
            $timeout.flush(300);

            // then
            expect(controller.percent).toEqual({ invalid: 20, empty: 70, valid: 10 });
            expect(controller.width).toEqual({ invalid: 20, empty: 70, valid: 10, isVariableInvalid: true, isVariableEmpty: true, isVariableValid: false });
        }));
    });

    describe('without enter animation', () => {
        beforeEach(inject(($compile) => {
            createElement = () => {
                const html = '<quality-bar quality="quality" enter-animation="false"></quality-bar>';
                element = $compile(html)(scope);
                scope.$digest();

                controller = element.controller('qualityBar');
            };
        }));

        it(' should not enable transition', () => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            createElement();

            // when
            scope.$digest();

            // then
            expect(controller.blockTransition).toBe(true);
        });

        it('compute percentage and width with no animation', () => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            createElement();

            // when
            scope.$digest();

            // then
            expect(controller.percent).toEqual({ invalid: 20, empty: 70, valid: 10 });
            expect(controller.width).toEqual({ invalid: 20, empty: 70, valid: 10, isVariableInvalid: true, isVariableEmpty: true, isVariableValid: false });
        });
    });

    describe('without menu', () => {
        beforeEach(inject(($compile) => {
            createElement = () => {
                const html = '<quality-bar quality="quality" is-trusted="true" enter-animation="false" on-click="onclick"></quality-bar>';
                element = $compile(html)(scope);
                scope.$digest();

                controller = element.controller('qualityBar');
            };
        }));

        it('should render only the 3 partitions', inject(($timeout) => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            createElement();

            // when
            scope.$digest();
            $timeout.flush(300);

            // then
            expect(element.find('.valid-partition').eq(0)[0].hasAttribute('talend-dropdown')).toBe(false);
        }));

        it('should call onclick callback for valid selection', inject(($timeout) => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.onclick = () => {};
            spyOn(scope, 'onclick');
            createElement();

            // when
            scope.$digest();
            $timeout.flush(300);
            element.find('.valid-partition').eq(0).click();

            // then
            expect(scope.onclick).toHaveBeenCalledWith('valid_records');
        }));

        it('should call onclick callback for empty selection', inject(($timeout) => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.onclick = () => {};
            spyOn(scope, 'onclick');
            createElement();

            // when
            scope.$digest();
            $timeout.flush(300);
            element.find('.empty-partition').eq(0).click();

            // then
            expect(scope.onclick).toHaveBeenCalledWith('empty_records');
        }));

        it('should call onclick callback for invalid selection', inject(($timeout) => {
            // given
            scope.quality = {
                valid: 10,
                invalid: 20,
                empty: 70,
            };
            scope.onclick = () => {};
            spyOn(scope, 'onclick');
            createElement();

            // when
            scope.$digest();
            $timeout.flush(300);
            element.find('.invalid-partition').eq(0).click();

            // then
            expect(scope.onclick).toHaveBeenCalledWith('invalid_records');
        }));
    });
    describe('with valid menu', () => {
        beforeEach(inject(($compile) => {
            createElement = () => {
                const html = `<quality-bar
                                    quality="quality"
                                    is-trusted="true"
                                    valid-items-has-menu="validItemsHasMenu"
                                    invalid-items-has-menu="invalidItemsHasMenu"
                                    empty-items-has-menu="emptyMenuItems"
                                    enter-animation="false">
                                        <valid-menu-items><li class="column-action">valid</li></valid-menu-items>
                                        <empty-menu-items><li class="column-action">empty</li></empty-menu-items>
                                        <invalid-menu-items><li class="column-action">invalid</li></invalid-menu-items></quality-bar>`;
                element = $compile(html)(scope);
                scope.$digest();

                controller = element.controller('qualityBar');
            };
        }));

        it('should render menu and its content', () => {
            // given
            scope.quality = {
                valid: 10,
                isVariableValid: false,
                invalid: 20,
                isVariableInvalid: true,
                empty: 70,
                isVariableEmpty: true,
            };
            scope.validItemsHasMenu = true;
            scope.invalidItemsHasMenu = true;
            scope.emptyMenuItems = true;

            createElement();

            // when
            scope.$digest();

            // then
            const validPartitionElm = element.find('.valid-partition').eq(0);
            expect(validPartitionElm[0].hasAttribute('talend-dropdown')).toBeTruthy();
            expect(validPartitionElm.hasClass('fixed-width')).toBeTruthy();
            expect(validPartitionElm.hasClass('not-fixed-width')).toBeFalsy();
            expect(element.find('.valid-partition .dropdown-container .dropdown-menu .column-action').text()).toBe('valid');

            const emptyPartitionElm = element.find('.empty-partition').eq(0);
            expect(emptyPartitionElm[0].hasAttribute('talend-dropdown')).toBeTruthy();
            expect(emptyPartitionElm.hasClass('fixed-width')).toBeFalsy();
            expect(emptyPartitionElm.hasClass('not-fixed-width')).toBeTruthy();
            expect(element.find('.empty-partition .dropdown-container .dropdown-menu .column-action').text()).toBe('empty');

            const invalidPartitionElm = element.find('.invalid-partition').eq(0);
            expect(invalidPartitionElm[0].hasAttribute('talend-dropdown')).toBeTruthy();
            expect(invalidPartitionElm.hasClass('fixed-width')).toBeFalsy();
            expect(invalidPartitionElm.hasClass('not-fixed-width')).toBeTruthy();
            expect(element.find('.invalid-partition .dropdown-container .dropdown-menu .column-action').text()).toBe('invalid');
        });
    });
});
