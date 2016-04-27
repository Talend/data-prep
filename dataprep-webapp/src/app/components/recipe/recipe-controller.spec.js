/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Recipe controller', function () {
    'use strict';

    var createController, scope;
    var lastActiveStep = {inactive: false};
    var stateMock;

    beforeEach(angular.mock.module('data-prep.recipe', function ($provide) {
        stateMock = {
            playground: {
                preparation: {
                    id: '132da49ef87694ab64e6'
                },
                lookupData: {
                    columns: [{
                        id: '0000',
                        name: 'id'
                    }, {
                        id: '0001',
                        name: 'firstName'
                    }, {
                        id: '0002',
                        name: 'lastName'
                    }]
                },
                lookup: {
                    visibility: false
                }
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(inject(function ($rootScope, $controller, $q, $timeout, RecipeService, PlaygroundService, PreparationService, PreviewService) {
        scope = $rootScope.$new();

        createController = function () {
            return $controller('RecipeCtrl', {
                $scope: scope
            });
        };

        spyOn($rootScope, '$emit').and.returnValue();
        spyOn(RecipeService, 'refresh').and.callFake(function () {
            var recipe = RecipeService.getRecipe();
            recipe.splice(0, recipe.length);
            recipe.push(lastActiveStep);
        });
        spyOn(PreviewService, 'getPreviewDiffRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when(true));
        spyOn(PreviewService, 'cancelPreview').and.returnValue(null);
        spyOn($timeout, 'cancel').and.returnValue();
    }));

    it('should bind recipe getter with RecipeService', inject(function (RecipeService) {
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

    describe('update step', function () {
        beforeEach(inject(function (PlaygroundService, $q) {
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
        }));

        it('should create a closure that update the step parameters', inject(function ($rootScope, PlaygroundService) {
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

        it('should update step', inject(function (PlaygroundService) {
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
                    parameters: {pattern: '.', column_name: 'state', column_id: '0001', scope: 'column'}
                }
            };
            var parameters = {pattern: '-'};

            //when
            ctrl.updateStep(step, parameters);

            //then
            expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, parameters);
        }));

        describe('preview', function() {
            it('should do nothing on update preview if the step is inactive', inject(function ($rootScope, PreviewService) {
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

            it('should do nothing on update preview if the params have not changed', inject(function ($rootScope, PreviewService) {
                //given
                var ctrl = createController();
                var step = {
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
                var closure = ctrl.previewUpdateClosure(step);

                //when
                closure(parameters);
                $rootScope.$digest();

                //then
                expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
            }));

            it('should call update preview', inject(function ($rootScope, PreviewService, RecipeService) {
                //given
                RecipeService.refresh(); //set last active step for the test : see mock
                $rootScope.$digest();

                var ctrl = createController();
                var step = {
                    column: {id: '0', name: 'state'},
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
        });
    });

    describe('step parameters', function () {

        it('should return that step has dynamic parameters when it has cluster', function () {
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

        it('should return that step has NO dynamic parameters', function () {
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

        it('should return that step has static parameters when it has simple params', function () {
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

        it('should return that step has static parameters when it has choice params', function () {
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

        it('should return that step has NO static parameters', function () {
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

        it('should return that step has parameters when it has static params', function () {
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

        it('should return that step has parameters when it has dybamic params', function () {
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

        it('should return that step has NO parameters', function () {
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

    describe('remove step', function () {
        var step = {
            transformation: {label: 'Replace empty value ...', name: 'lookup', stepId: '0001'},
            actionParameters: {parameters: {column_name: 'firstname'}}
        };

        beforeEach(inject(function ($q, PlaygroundService, StateService) {
            spyOn(PlaygroundService, 'removeStep').and.returnValue($q.when());
            spyOn(StateService, 'setLookupVisibility').and.returnValue();
        }));

        it('should remove step', inject(function (PlaygroundService) {
            //given
            var ctrl = createController();
            var event = angular.element.Event('click');
            //when
            ctrl.remove(step, event);
            scope.$digest();

            //then
            expect(PlaygroundService.removeStep).toHaveBeenCalledWith(step);
        }));

        it('should stop click propagation', function () {
            //given
            var ctrl = createController();
            var event = angular.element.Event('click');
            spyOn(event, 'stopPropagation').and.returnValue();

            stateMock.playground.lookup.visibility = true;
            stateMock.playground.lookup.step = step;

            //when
            ctrl.remove(step, event);
            scope.$digest();

            //then
            expect(event.stopPropagation).toHaveBeenCalled();
        });

        it('should hide lookup if it is in update mode', inject(function (StateService) {
            //given
            var ctrl = createController();
            var event = angular.element.Event('click');

            stateMock.playground.lookup.visibility = true;
            stateMock.playground.lookup.step = step;

            //when
            ctrl.remove(step, event);
            scope.$digest();

            //then
            expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
        }));

        it('should NOT hide lookup when it is NOT in update mode', inject(function (StateService) {
            //given
            var ctrl = createController();
            var event = angular.element.Event('click');

            stateMock.playground.lookup.visibility = true;
            stateMock.playground.lookup.step = null;

            //when
            ctrl.remove(step, event);
            scope.$digest();

            //then
            expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
        }));

        it('should NOT hide lookup when it is already already hidden', inject(function (StateService) {
            //given
            var ctrl = createController();
            var event = angular.element.Event('click');

            stateMock.playground.lookup.visibility = false;
            stateMock.playground.lookup.step = step;

            //when
            ctrl.remove(step, event);
            scope.$digest();

            //then
            expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
        }));
    });

    describe('select step', function () {
        var lookupStep = {
            transformation: {label: 'Replace empty value ...', name: 'lookup', stepId: '0001'},
            actionParameters: {parameters: {column_name: 'firstname'}}
        };
        var notLookupStep = {
            transformation: {label: 'Change case to UPPER ...', name: 'uppercase', stepId: '0002'},
            actionParameters: {parameters: {column_name: 'firstname'}}
        };
        var clusterStep = {
            transformation: {label: 'Cluster ...', name: 'cluster', stepId: '0003', cluster: {}},
            actionParameters: {parameters: {column_name: 'firstname'}}
        };

        beforeEach(inject(function ($q, StateService, LookupService) {
            spyOn(LookupService, 'loadFromStep').and.returnValue($q.when());
            spyOn(StateService, 'setLookupVisibility').and.returnValue();
        }));

        it('should close lookup if it is opened in selected step update mode', inject(function (StateService) {
            //given
            var ctrl = createController();
            stateMock.playground.lookup.visibility = true;
            stateMock.playground.lookup.step = lookupStep;

            //when
            ctrl.select(lookupStep);
            scope.$digest();

            //then
            expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
        }));

        it('should open lookup in update mode', inject(function (LookupService, StateService) {
            //given
            var ctrl = createController();
            stateMock.playground.lookup.visibility = false;

            //when
            ctrl.select(lookupStep);
            scope.$digest();

            //then
            expect(LookupService.loadFromStep).toHaveBeenCalledWith(lookupStep);
            expect(StateService.setLookupVisibility).toHaveBeenCalledWith(true, undefined);
        }));

        it('should do nothing on lookup when the selected step is not a lookup', inject(function (LookupService, StateService) {
            //given
            var ctrl = createController();

            //when
            ctrl.select(notLookupStep);
            scope.$digest();

            //then
            expect(LookupService.loadFromStep).not.toHaveBeenCalled();
            expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
        }));

        it('should show dynamic params modal',function () {
            //given
            var ctrl = createController();
            ctrl.showModal[clusterStep.transformation.stepId] = false;

            //when
            ctrl.select(clusterStep);
            scope.$digest();

            //then
            expect(ctrl.showModal[clusterStep.transformation.stepId]).toBe(true);
        });
    });

    describe('remove step filter', function () {
        var filter1 = {
            type: 'exact',
            colId: '0000',
            colName: 'name',
            args: {
                phrase: '        AMC  ',
                caseSensitive: true
            },
            value: '        AMC  '
        };
        var filter2 = {
            type: 'contains',
            colId: '0002',
            args: {
                phrase: 'toto'
            }
        };
        var stepDeleteLinesWithSingleFilter, stepWithMultipleFilters;

        beforeEach(inject(function (FilterAdapterService) {
            spyOn(FilterAdapterService, 'toTree').and.returnValue({
                filter: {
                    valid: {
                        field: '0001'
                    }
                }
            });
            stepDeleteLinesWithSingleFilter = {
                transformation: {label: 'Delete lines'},
                actionParameters: {
                    action: 'delete_lines',
                    parameters: {column_name: 'firstname', scope: 'column'}
                },
                filters: [filter1]
            };
            stepWithMultipleFilters = {
                transformation: {label: 'Replace empty value ...'},
                actionParameters: {parameters: {column_name: 'firstname', scope: 'column'}},
                filters: [filter1, filter2]
            };
        }));

        it('should remove step filter', inject(function ($q, PlaygroundService, FilterAdapterService) {
            //given
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
            var ctrl = createController();

            //when
            ctrl.removeStepFilter(stepWithMultipleFilters, filter1);
            scope.$digest();

            //then
            expect(FilterAdapterService.toTree).toHaveBeenCalledWith([filter2]);
            expect(PlaygroundService.updateStep).toHaveBeenCalledWith(stepWithMultipleFilters, {
                column_name: 'firstname',
                filter: {
                    valid: {
                        field: '0001'
                    }
                },
                scope: 'column'
            });
            expect(stepWithMultipleFilters.filters.length).toBe(1);
        }));

        it('should show warning message on delete lines step with last filter removal', inject(function ($q, PlaygroundService, MessageService) {
            //given
            spyOn(MessageService, 'warning').and.returnValue();
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
            var ctrl = createController();

            //when
            ctrl.removeStepFilter(stepDeleteLinesWithSingleFilter, filter1);
            scope.$digest();

            //then
            expect(MessageService.warning).toHaveBeenCalled();
            expect(PlaygroundService.updateStep).not.toHaveBeenCalled();
            expect(stepDeleteLinesWithSingleFilter.filters.length).toBe(1);
        }));

        it('should insert the filter back when the update fails', inject(function ($q, PlaygroundService) {
            //given
            spyOn(PlaygroundService, 'updateStep').and.returnValue($q.reject());
            var ctrl = createController();

            //when
            ctrl.removeStepFilter(stepWithMultipleFilters, filter1);
            scope.$digest();

            //then
            expect(stepWithMultipleFilters.filters.length).toBe(2);
        }));
    });

    describe('filters', function () {
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
            }, {
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

        it('should display all filter name on hover', inject(function () {
            //given
            var ctrl = createController();
            //then
            expect(ctrl.getAllFiltersNames(filters)).toBe('(NAME, ID)');
        }));
    });
});
