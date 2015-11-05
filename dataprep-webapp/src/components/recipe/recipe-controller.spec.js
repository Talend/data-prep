/*jshint camelcase: false */

describe('Recipe controller', function() {
    'use strict';

    var createController, scope;
    var lastActiveStep = {inactive: false};
    var stateMock;

    beforeEach(module('data-prep.recipe', function($provide) {
        stateMock = {playground: {preparation: {id: '132da49ef87694ab64e6'}}};
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function($rootScope, $controller, $q, $timeout, RecipeService, PlaygroundService, PreparationService, PreviewService) {
        scope = $rootScope.$new();

        createController = function() {
            return $controller('RecipeCtrl', {
                $scope: scope
            });
        };

        spyOn($rootScope, '$emit').and.returnValue();
        spyOn(RecipeService, 'refresh').and.callFake(function() {
            var recipe = RecipeService.getRecipe();
            recipe.splice(0, recipe.length);
            recipe.push(lastActiveStep);
        });
        spyOn(PreviewService, 'getPreviewDiffRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'cancelPreview').and.returnValue(null);
        spyOn($timeout, 'cancel').and.returnValue();
    }));

    it('should bind recipe getter with RecipeService', inject(function(RecipeService) {
        //given
        var ctrl = createController();
        expect(ctrl.recipe).toEqual([]);

        var column = {id: 'colId'};
        var transformation = {
            name: 'split',
            category: 'split',
            parameters: [],
            items: []
        };

        //when
        RecipeService.getRecipe().push({
            column: column,
            transformation: transformation
        });

        //then
        expect(ctrl.recipe.length).toBe(1);
        expect(ctrl.recipe[0].column).toBe(column);
        expect(ctrl.recipe[0].transformation).toEqual(transformation);
    }));

    describe('update step', function() {
        beforeEach(inject(function(PlaygroundService, $q) {
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
        }));
        it('should create a closure that update the step parameters', inject(function ($rootScope, PlaygroundService) {
            //given
            var ctrl       = createController();
            var step       = {
                column: {id: 'state'},
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: {pattern: '.', column_name: 'state'}
                }
            };
            var parameters = {pattern: '-'};

            //when
            var updateClosure = ctrl.stepUpdateClosure(step);
            updateClosure(parameters);
            $rootScope.$digest();

            //then
            expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, parameters);
        }));

        it('should update step when parameters are different', inject(function (PlaygroundService) {
            //given
            var ctrl       = createController();
            var step       = {
                column: {id: 'state'},
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: {pattern: '.', column_name: 'state', column_id: '0001', scope: 'column'}
                }
            };
            var parameters = {pattern: '-'};

            //when
            ctrl.updateStep(step, parameters);

            //then
            expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, {
                pattern: '-',
                column_name: 'state',
                column_id: '0001',
                scope: 'column'
            });
        }));

        it('should hide parameters modal on update step when parameters are different', inject(function ($rootScope) {
            //given
            var ctrl       = createController();
            var step       = {
                column: {id: 'state'},
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: {pattern: '.', column_name: 'state'}
                }
            };
            var parameters = {pattern: '-'};
            ctrl.showModal = {'a598bc83fc894578a8b823': true};

            //when
            ctrl.updateStep(step, parameters);
            $rootScope.$digest();

            //then
            expect(ctrl.showModal).toEqual({});
        }));

        it('should do nothing if parameters are unchanged', inject(function (PlaygroundService) {
            //given
            var ctrl       = createController();
            var step       = {
                column: {id: '0', name: 'state'},
                transformation: {
                    stepId: 'a598bc83fc894578a8b823',
                    name: 'cut'
                },
                actionParameters: {
                    action: 'cut',
                    parameters: {pattern: '.', column_id: '0', column_name: 'state'}
                }
            };
            var parameters = {pattern: '.'};

            //when
            ctrl.updateStep(step, parameters);

            //then
            expect(PlaygroundService.updateStep).not.toHaveBeenCalled();
        }));
    });

    it('should do nothing on update preview if the step is inactive', inject(function($rootScope, PreviewService) {
        //given
        var ctrl = createController();
        var step = {
            column: {id: 'state'},
            transformation: {
                stepId: 'a598bc83fc894578a8b823',
                name: 'cut'
            },
            actionParameters: {
                action: 'cut',
                parameters: {pattern: '.', column_name: 'state'}
            },
            inactive: true
        };
        var parameters = {pattern: '--'};
        var closure = ctrl.previewUpdateClosure(step);

        //when
        closure(parameters);
        $rootScope.$digest();

        //then
        expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
    }));

    it('should do nothing on update preview if the params have not changed', inject(function($rootScope, PreviewService) {
        //given
        var ctrl = createController();
        var step = {
            column: {id: '0', name:'state'},
            transformation: {
                stepId: 'a598bc83fc894578a8b823',
                name: 'cut'
            },
            actionParameters: {
                action: 'cut',
                parameters: {pattern: '.', column_id:'0', column_name: 'state'}
            }
        };
        var parameters = {pattern: '.'};
        var closure = ctrl.previewUpdateClosure(step);

        //when
        closure(parameters);
        $rootScope.$digest();

        //then
        expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
    }));

    it('should call update preview', inject(function($rootScope, PreviewService, RecipeService) {
        //given
        RecipeService.refresh(); //set last active step for the test : see mock
        $rootScope.$digest();

        var ctrl = createController();
        var step = {
            column: {id: '0', name:'state'},
            transformation: {
                stepId: 'a598bc83fc894578a8b823',
                name: 'cut'
            },
            actionParameters: {
                action: 'cut',
                parameters: {pattern: '.', column_id: '0', column_name: 'state', scope: 'column'}
            }
        };
        var parameters = {pattern: '--'};
        var closure = ctrl.previewUpdateClosure(step);

        //when
        closure(parameters);
        $rootScope.$digest();

        //then
        expect(PreviewService.getPreviewUpdateRecords).toHaveBeenCalledWith(
            stateMock.playground.preparation.id,
            lastActiveStep,
            step,
            {pattern: '--', column_id: '0', column_name: 'state', scope: 'column'});
    }));

    describe('step parameters', function () {
        it('should return that step has dynamic parameters when it has cluster', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {
                    cluster: {}
                }
            };

            //when
            var hasDynamicParams = ctrl.hasDynamicParams(step);

            //then
            expect(hasDynamicParams).toBeTruthy();
        });

        it('should return that step has NO dynamic parameters', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {}
            };

            //when
            var hasDynamicParams = ctrl.hasDynamicParams(step);

            //then
            expect(hasDynamicParams).toBeFalsy();
        });

        it('should return that step has static parameters when it has simple params', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {
                    parameters: [{}]
                }
            };

            //when
            var hasStaticParams = ctrl.hasStaticParams(step);

            //then
            expect(hasStaticParams).toBeTruthy();
        });

        it('should return that step has static parameters when it has choice params', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {
                    items: [{}]
                }
            };

            //when
            var hasStaticParams = ctrl.hasStaticParams(step);

            //then
            expect(hasStaticParams).toBeTruthy();
        });

        it('should return that step has NO static parameters', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {}
            };

            //when
            var hasStaticParams = ctrl.hasStaticParams(step);

            //then
            expect(hasStaticParams).toBeFalsy();
        });

        it('should return that step has parameters when it has static params', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {
                    parameters: [{}]
                }
            };

            //when
            var hasParams = ctrl.hasParameters(step);

            //then
            expect(hasParams).toBeTruthy();
        });

        it('should return that step has parameters when it has dybamic params', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {
                    cluster: []
                }
            };

            //when
            var hasParams = ctrl.hasParameters(step);

            //then
            expect(hasParams).toBeTruthy();
        });

        it('should return that step has NO parameters', function() {
            //given
            var ctrl = createController();
            var step = {
                transformation: {}
            };

            //when
            var hasParams = ctrl.hasParameters(step);

            //then
            expect(hasParams).toBeFalsy();
        });
    });

    describe('remove step', function() {
        var step = {
            transformation: {label: 'Replace empty value ...'},
            actionParameters: {parameters: {column_name: 'firstname'}}
        };

        beforeEach(inject(function(PlaygroundService) {
            spyOn(PlaygroundService, 'removeStep').and.returnValue();
        }));

        it('should remove step', inject(function(PlaygroundService) {
            //given
            var ctrl = createController();

            //when
            ctrl.remove(step);
            scope.$digest();

            //then
            expect(PlaygroundService.removeStep).toHaveBeenCalledWith(step);
        }));
    });


    describe('remove filter step', function() {
        var filters = [
            {
                'type': 'exact',
                'colId': '0000',
                'colName': 'name',
                'args': {
                    'phrase': '        AMC  ',
                    'caseSensitive': true
                },
                'value': '        AMC  '
            }
        ];
        var step = {
            transformation: {label: 'Replace empty value ...'},
            actionParameters: {parameters: {column_name: 'firstname'}},
            filters:filters
        };

        beforeEach(inject(function(FilterService, PlaygroundService, $q) {
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
            spyOn(FilterService, 'convertFiltersArrayToTreeFormat').and.returnValue({
                filter:{
                    valid:{
                        field:'0001'
                    }
                }
            });
        }));

        it('should remove step filter', inject(function(FilterService) {
            //given
            var ctrl = createController();

            //when
            ctrl.removeStepFilter(step, filters[0]);
            scope.$digest();

            //then
            expect(FilterService.convertFiltersArrayToTreeFormat).toHaveBeenCalledWith(filters);
            expect(step.filters.length).toBe(0);
        }));

        it('should show warning message on last step filter removal for a delete_lines action', inject(function(FilterService, MessageService) {
            //given
            spyOn(MessageService, 'warning').and.returnValue();
            var ctrl = createController();
            var filters = [
                {
                    'type': 'exact',
                    'colId': '0000',
                    'colName': 'name',
                    'args': {
                        'phrase': '        AMC  ',
                        'caseSensitive': true
                    },
                    'value': '        AMC  '
                }
            ];
            var step = {
                transformation: {label: 'Replace empty value ...'},
                actionParameters: {
                    parameters: {column_name: 'firstname'},
                    action: 'delete_lines'
                },
                filters:filters
            };

            //when
            ctrl.removeStepFilter(step, filters[0]);
            scope.$digest();

            //then
            expect(MessageService.warning).toHaveBeenCalled();
            expect(FilterService.convertFiltersArrayToTreeFormat).not.toHaveBeenCalled();
            expect(step.filters.length).toBe(1);
        }));
    });

    describe('remove filter step in case of failure', function() {
        var filters = [
            {
                'type': 'exact',
                'colId': '0000',
                'colName': 'name',
                'args': {
                    'phrase': '        AMC  ',
                    'caseSensitive': true
                },
                'value': '        AMC  '
            }
        ];
        var step = {
            transformation: {label: 'Replace empty value ...'},
            actionParameters: {parameters: {column_name: 'firstname'}},
            filters:filters
        };

        beforeEach(inject(function(FilterService, PlaygroundService, $q) {
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.reject());
            spyOn(FilterService, 'convertFiltersArrayToTreeFormat').and.returnValue({
                filter:{
                    valid:{
                        field:'0001'
                    }
                }
            });
        }));


        it('should fail while removing step filter', inject(function() {
            //given
            var ctrl = createController();

            //when
            ctrl.removeStepFilter(step, filters[0]);
            scope.$digest();

            //then
            expect(step.filters.length).toBe(1);
        }));
    });


    describe('filters', function() {
        var filters = [
            {
                'type': 'exact',
                'colId': '0000',
                'colName': 'name',
                'args': {
                    'phrase': '        AMC  ',
                    'caseSensitive': true
                },
                'value': '        AMC  '
            },{
                'type': 'exact',
                'colId': '0000',
                'colName': 'id',
                'args': {
                    'phrase': '        AMC  ',
                    'caseSensitive': true
                },
                'value': '        AMC  '
            }
        ];

        it('should display all filter name on hover', inject(function() {
            //given
            var ctrl = createController();
            //then
            expect(ctrl.getAllFiltersNames(filters)).toBe('(NAME, ID)');
        }));
    });
});
