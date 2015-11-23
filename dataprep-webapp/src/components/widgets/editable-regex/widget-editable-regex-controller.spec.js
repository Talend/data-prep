describe('Editable regex widget controller', function() {
    'use strict';

    var scope, createController;

    beforeEach(module('talend.widget'));

    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'EQUALS': 'Equals',
            'CONTAINS': 'Contains',
            'STARTS_WITH': 'Starts With',
            'ENDS_WITH': 'Ends With',
            'REGEX': 'RegEx'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $controller) {
        scope = $rootScope.$new();
        createController = function() {
            var ctrlFn = $controller('TalendEditableRegexCtrl', {
                $scope: scope
            }, true);
            ctrlFn.instance.value = scope.value;
            return ctrlFn();
        };
    }));

    describe('init', function() {
        it('should init types array', function() {
            //when
            var ctrl = createController();

            //then
            expect(ctrl.types.length).toBe(5);
            expect(ctrl.types[0].label).toBe('Equals');
            expect(ctrl.types[1].label).toBe('Contains');
            expect(ctrl.types[2].label).toBe('Starts With');
            expect(ctrl.types[3].label).toBe('Ends With');
            expect(ctrl.types[4].label).toBe('RegEx');
        });

        it('should init selected type with "equals" type by default', function() {
            //when
            var ctrl = createController();

            //then
            expect(ctrl.selectedType.label).toBe('Equals');
        });

        it('should init entered text with empty string by default', function() {
            //when
            var ctrl = createController();

            //then
            expect(ctrl.regex).toBe('');
        });

        describe('with existing model value', function() {
            describe('and "equals" type', function() {
                it('should init selected type by matching current model pattern', function() {
                    //given
                    scope.value = 'aze[?]rty';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.selectedType.label).toBe('Equals');
                });

                it('should init entered text with unescaped value', function() {
                    //given
                    scope.value = 'aze[?]rty';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.regex).toBe('aze?rty');
                });
            });

            describe('and "contains" type', function() {
                it('should init selected type by matching current model pattern', function() {
                    //given
                    scope.value = '.*aze[?]rty.*';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.selectedType.label).toBe('Contains');
                });

                it('should init entered text with unescaped value', function() {
                    //given
                    scope.value = '.*aze[?]rty.*';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.regex).toBe('aze?rty');
                });
            });

            describe('and "Starts With" type', function() {
                it('should init selected type by matching current model pattern', function() {
                    //given
                    scope.value = '^aze[?]rty.*';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.selectedType.label).toBe('Starts With');
                });

                it('should init entered text with unescaped value', function() {
                    //given
                    scope.value = '^aze[?]rty.*';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.regex).toBe('aze?rty');
                });
            });

            describe('and "Ends With" type', function() {
                it('should init selected type by matching current model pattern', function() {
                    //given
                    scope.value = '.*aze[?]rty$';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.selectedType.label).toBe('Ends With');
                });

                it('should init entered text with unescaped value', function() {
                    //given
                    scope.value = '.*aze[?]rty$';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.regex).toBe('aze?rty');
                });
            });

            describe('and "RegEx" type', function() {
                it('should init selected type by matching current model pattern', function() {
                    //given
                    scope.value = 'Fr.*ce$';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.selectedType.label).toBe('RegEx');
                });

                it('should init entered text with value (without escape)', function() {
                    //given
                    scope.value = 'Fr.*ce$';

                    //when
                    var ctrl = createController();

                    //then
                    expect(ctrl.regex).toBe('Fr.*ce$');
                });
            });
        });
    });

    describe('update model', function() {
        it('should not adapt regex value if entered value is falsy', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'Contains'});
            ctrl.regex = '';

            ctrl.value = '';

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe(''); // not .*(empty).*
        });

        it('should escape and adapt "equals" regex', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'Equals'});
            ctrl.regex = 'aze?rty';

            expect(ctrl.value).toBeFalsy();

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe('aze[?]rty');
        });

        it('should escape and adapt "contains" regex', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'Contains'});
            ctrl.regex = 'aze?rty';

            expect(ctrl.value).toBeFalsy();

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe('.*aze[?]rty.*');
        });

        it('should escape and adapt "starts with" regex', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'Starts With'});
            ctrl.regex = 'aze?rty';

            expect(ctrl.value).toBeFalsy();

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe('^aze[?]rty.*');
        });

        it('should escape and adapt "ends with" regex', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'Ends With'});
            ctrl.regex = 'aze?rty';

            expect(ctrl.value).toBeFalsy();

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe('.*aze[?]rty$');
        });

        it('should return entered text on "regex" type adaptation', function() {
            //given
            var ctrl = createController();
            ctrl.selectedType = _.find(ctrl.types, {label: 'RegEx'});
            ctrl.regex = 'aze?rty';

            expect(ctrl.value).toBeFalsy();

            //when
            ctrl.updateModel();

            //then
            expect(ctrl.value).toBe('aze?rty');
        });
    });

    describe('regex type change', function() {
        it('should set selected type', function() {
            //given
            var ctrl = createController();
            var nextType = _.find(ctrl.types, {label: 'Starts With'});
            ctrl.selectedType = _.find(ctrl.types, {label: 'Contains'});

            //when
            ctrl.setSelectedType(nextType);

            //then
            expect(ctrl.selectedType).toBe(nextType);
        });

        it('should update model', function() {
            //given
            var ctrl = createController();
            var nextType = _.find(ctrl.types, {label: 'Starts With'});
            ctrl.selectedType = _.find(ctrl.types, {label: 'Contains'});
            ctrl.regex = 'aze?rty';
            ctrl.value = '';

            //when
            ctrl.setSelectedType(nextType);

            //then
            expect(ctrl.value).toBe('^aze[?]rty.*');
        });
    });
});