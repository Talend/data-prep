/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Recipe controller', () => {
	let createController;
	let scope;
	const lastActiveStep = { inactive: false };
	let stateMock;
	const steps = [{ inactive: false }, { inactive: false }, { inactive: true }, { inactive: true }];

	beforeEach(angular.mock.module('data-prep.recipe', ($provide) => {
		stateMock = {
			playground: {
				preparation: {
					id: '132da49ef87694ab64e6',
				},
				lookupData: {
					columns: [{
						id: '0000',
						name: 'id',
					}, {
						id: '0001',
						name: 'firstName',
					}, {
						id: '0002',
						name: 'lastName',
					},],
				},
				grid: { nbLines: 1000 },
				lookup: { visibility: false },
				data: { metadata: {} },
				recipe: {
					current: {
						steps,
						reorderedSteps: steps,
						lastActiveStep: steps[1],
					},
				},
			},
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($componentController, $rootScope, $q, $timeout, RecipeService, PlaygroundService, PreparationService, PreviewService) => {
		scope = $rootScope.$new();

		createController = () => $componentController('recipe', {
			$scope: scope,
		});

		spyOn($rootScope, '$emit').and.returnValue();
		spyOn(RecipeService, 'refresh').and.callFake(() => {
			stateMock.playground.recipe.current.reorderedSteps = [lastActiveStep];
		});
		spyOn(PreviewService, 'getPreviewDiffRecords').and.returnValue($q.when());
		spyOn(PreviewService, 'getPreviewUpdateRecords').and.returnValue($q.when());
		spyOn(PreviewService, 'cancelPreview').and.returnValue(null);
		spyOn($timeout, 'cancel').and.returnValue();
	}));

	describe('update step', () => {
		beforeEach(inject((PlaygroundService, $q) => {
			spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
		}));

		it('should create a closure that update the step parameters', inject(($rootScope, PlaygroundService) => {
			// given
			const ctrl = createController();
			const step = {
				column: { id: 'state' },
				transformation: {
					stepId: 'a598bc83fc894578a8b823',
					name: 'cut',
				},
				actionParameters: {
					action: 'cut',
					parameters: { pattern: '.', column_name: 'state' },
				},
			};
			const parameters = { pattern: '-' };

			// when
			const updateClosure = ctrl.stepUpdateClosure(step);
			updateClosure(parameters);
			$rootScope.$digest();

			// then
			expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, parameters);
		}));

		it('should update updateStepInProgress', inject(($rootScope, $timeout) => {
			// given
			const ctrl = createController();
			const step = {
				column: { id: 'state' },
				transformation: {
					stepId: 'a598bc83fc894578a8b823',
					name: 'cut',
				},
				actionParameters: {
					action: 'cut',
					parameters: { pattern: '.', column_name: 'state' },
				},
			};
			const parameters = { pattern: '-' };

			// when
			const updateClosure = ctrl.stepUpdateClosure(step);
			updateClosure(parameters);
			$rootScope.$digest();

			expect(ctrl.updateStepInProgress).toEqual(true);
			$timeout.flush(500);

			// then
			expect(ctrl.updateStepInProgress).toEqual(false);
		}));

		it('should update step', inject((PlaygroundService) => {
			// given
			const ctrl = createController();
			const step = {
				column: { id: 'state' },
				transformation: {
					stepId: 'a598bc83fc894578a8b823',
					name: 'cut',
				},
				actionParameters: {
					action: 'cut',
					parameters: {
						pattern: '.',
						column_name: 'state',
						column_id: '0001',
						scope: 'column',
					},
				},
			};
			const parameters = { pattern: '-' };

			// when
			ctrl.updateStep(step, parameters);

			// then
			expect(PlaygroundService.updateStep).toHaveBeenCalledWith(step, parameters);
		}));

		it('should return true if recipe has steps', inject((PlaygroundService) => {
			// given
			const ctrl = createController();


			// when
			stateMock.playground.recipe.current.reorderedSteps = steps;
			const hasSteps = ctrl.hasSteps();

			// then
			expect(hasSteps).toBeTruthy();
		}));

		it('should return false if recipe has no steps', inject((PlaygroundService) => {
			// given
			const ctrl = createController();

			// when
			stateMock.playground.recipe.current.reorderedSteps = [];
			const hasSteps = ctrl.hasSteps();

			// then
			expect(hasSteps).toBeFalsy();
		}));

		it('should return false if reorderedSteps in not defined', inject((PlaygroundService) => {
			// given
			const ctrl = createController();

			// when
			stateMock.playground.recipe.current = null;
			const hasSteps = ctrl.hasSteps();

			// then
			expect(hasSteps).toBeFalsy();
		}));

		describe('preview', () => {
			it('should do nothing on update preview if the step is inactive', inject(($rootScope, PreviewService) => {
				// given
				const ctrl = createController();
				const step = {
					column: { id: 'state' },
					transformation: {
						stepId: 'a598bc83fc894578a8b823',
						name: 'cut',
					},
					actionParameters: {
						action: 'cut',
						parameters: { pattern: '.', column_name: 'state' },
					},
					inactive: true,
				};
				const parameters = { pattern: '--' };
				const closure = ctrl.previewUpdateClosure(step);

				// when
				closure(parameters);
				$rootScope.$digest();

				// then
				expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
			}));

			it('should do nothing on update preview if the params have not changed', inject(($rootScope, PreviewService) => {
				// given
				const ctrl = createController();
				const step = {
					column: { id: '0', name: 'state' },
					transformation: {
						stepId: 'a598bc83fc894578a8b823',
						name: 'cut',
					},
					actionParameters: {
						action: 'cut',
						parameters: { pattern: '.', column_id: '0', column_name: 'state' },
					},
				};
				const parameters = { pattern: '.' };
				const closure = ctrl.previewUpdateClosure(step);

				// when
				closure(parameters);
				$rootScope.$digest();

				// then
				expect(PreviewService.getPreviewUpdateRecords).not.toHaveBeenCalled();
			}));

			it('should call update preview', inject(($rootScope, PreviewService, RecipeService) => {
				// given
				RecipeService.refresh(); // set last active step for the test : see mock
				$rootScope.$digest();

				const ctrl = createController();
				const step = {
					column: { id: '0', name: 'state' },
					transformation: {
						stepId: 'a598bc83fc894578a8b823',
						name: 'cut',
					},
					actionParameters: {
						action: 'cut',
						parameters: {
							pattern: '.',
							column_id: '0',
							column_name: 'state',
							scope: 'column'
						},
					},
				};
				const parameters = { pattern: '--' };
				const closure = ctrl.previewUpdateClosure(step);

				// when
				closure(parameters);
				$rootScope.$digest();

				// then
				expect(PreviewService.getPreviewUpdateRecords).toHaveBeenCalledWith(
					stateMock.playground.preparation.id,
					lastActiveStep,
					step,
					{ pattern: '--', column_id: '0', column_name: 'state', scope: 'column' });
			}));
		});
	});

	describe('step parameters', () => {
		it('should return that step has parameters when it has static params', () => {
			// given
			const ctrl = createController();
			const step = {
				transformation: {
					parameters: [{}],
				},
			};

			// when
			const hasParams = ctrl.hasParameters(step);

			// then
			expect(hasParams).toBeTruthy();
		});

		it('should return that step has parameters when it has dybamic params', () => {
			// given
			const ctrl = createController();
			const step = {
				transformation: {
					cluster: [],
				},
			};

			// when
			const hasParams = ctrl.hasParameters(step);

			// then
			expect(hasParams).toBeTruthy();
		});

		it('should return that step has NO parameters', () => {
			// given
			const ctrl = createController();
			const step = {
				transformation: {},
			};

			// when
			const hasParams = ctrl.hasParameters(step);

			// then
			expect(hasParams).toBeFalsy();
		});
	});

	describe('remove step', () => {
		const step = {
			transformation: { label: 'Replace empty value ...', name: 'lookup', stepId: '0001' },
			actionParameters: { parameters: { column_name: 'firstname' } },
		};

		beforeEach(inject(($q, PlaygroundService, StateService) => {
			spyOn(PlaygroundService, 'removeStep').and.returnValue($q.when());
			spyOn(StateService, 'setLookupVisibility').and.returnValue();
			spyOn(StateService, 'setStepInEditionMode').and.returnValue();
		}));

		it('should remove step', inject((PlaygroundService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');
			ctrl.stepToBeDeleted = {};

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(ctrl.stepToBeDeleted).toBe(null);
			expect(PlaygroundService.removeStep).toHaveBeenCalledWith(step);
		}));

		it('should NOT call removeStep if the deletion is already in progress', inject((PlaygroundService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');

			// when
			ctrl.remove(step, event);
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(PlaygroundService.removeStep.calls.count()).toBe(1);
		}));

		it('should hide lookup panel when the deleted step is a lookup step',
			inject((StateService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');
			ctrl.stepToBeDeleted = {};
			stateMock.playground.lookup.visibility = true;
			stateMock.playground.stepInEditionMode = {};

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
			expect(StateService.setStepInEditionMode).toHaveBeenCalledWith(null);
		}));

		it('should stop click propagation', () => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');
			spyOn(event, 'stopPropagation').and.returnValue();

			stateMock.playground.lookup.visibility = true;
			stateMock.playground.stepInEditionMode = step;

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(event.stopPropagation).toHaveBeenCalled();
		});

		it('should hide lookup if it is in update mode', inject((StateService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');

			stateMock.playground.lookup.visibility = true;
			stateMock.playground.stepInEditionMode = step;

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
		}));

		it('should NOT hide lookup when it is NOT in update mode', inject((StateService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');

			stateMock.playground.lookup.visibility = true;
			stateMock.playground.stepInEditionMode = null;

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
		}));

		it('should NOT hide lookup when it is already already hidden', inject((StateService) => {
			// given
			const ctrl = createController();
			const event = angular.element.Event('click');

			stateMock.playground.lookup.visibility = false;
			stateMock.playground.stepInEditionMode = step;

			// when
			ctrl.remove(step, event);
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
		}));
	});

	describe('select step', () => {
		const lookupStep = {
			transformation: { label: 'Replace empty value ...', name: 'lookup', stepId: '0001' },
			actionParameters: { parameters: { column_name: 'firstname' } },
		};
		const notLookupStep = {
			transformation: {
				label: 'Change case to UPPER ...',
				name: 'uppercase',
				stepId: '0002'
			},
			actionParameters: { parameters: { column_name: 'firstname' } },
		};
		const clusterStep = {
			transformation: { label: 'Cluster ...', name: 'cluster', stepId: '0003', cluster: {} },
			actionParameters: { parameters: { column_name: 'firstname' } },
		};

		beforeEach(inject(($q, StateService, LookupService) => {
			spyOn(LookupService, 'loadFromStep').and.returnValue($q.when());
			spyOn(StateService, 'setLookupVisibility').and.returnValue();
		}));

		it('should close lookup if it is opened', inject((StateService) => {
			// given
			const ctrl = createController();
			stateMock.playground.lookup.visibility = true;
			stateMock.playground.stepInEditionMode = lookupStep;

			// when
			ctrl.select(lookupStep);
			scope.$digest();

			// then
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(false);
		}));

		it('should open lookup to update it', inject((LookupService, StateService) => {
			// given
			const ctrl = createController();
			stateMock.playground.lookup.visibility = false;

			// when
			ctrl.select(lookupStep);
			scope.$digest();

			// then
			expect(LookupService.loadFromStep).toHaveBeenCalledWith(lookupStep);
			expect(StateService.setLookupVisibility).toHaveBeenCalledWith(true);
		}));

		it('should do nothing on lookup when the selected step is not a lookup', inject((LookupService, StateService) => {
			// given
			const ctrl = createController();

			// when
			ctrl.select(notLookupStep);
			scope.$digest();

			// then
			expect(LookupService.loadFromStep).not.toHaveBeenCalled();
			expect(StateService.setLookupVisibility).not.toHaveBeenCalled();
		}));

		it('should show dynamic params modal', () => {
			// given
			const ctrl = createController();
			ctrl.showModal[clusterStep.transformation.stepId] = false;

			// when
			ctrl.select(clusterStep);
			scope.$digest();

			// then
			expect(ctrl.showModal[clusterStep.transformation.stepId]).toBe(true);
		});
	});

	describe('update step filter', () => {
		const multiValuedFilter = {
			type: 'contains',
			colId: '0002',
			colName: '0002',
			editable: false,
			args: {
				phrase: [
					{ value: 'toto' },
					{ value: 'tata' },
				],
			},
			filterFn: null,
			removeFilterFn: null,
			value: [
				{ value: 'toto' },
				{ value: 'tata' },
			],
		};
		const stepWithMultipleFilters = {
			transformation: {
				label: 'Replace empty value ...',
			},
			actionParameters: {
				parameters: {
					column_name: 'firstname',
					filter: {
						or: [
							{ contains: { field: '0002', value: 'toto' } },
							{ contains: { field: '0002', value: 'tata' } },
						],
					},
					scope: 'column',
				},
			},
			filters: [multiValuedFilter],
		};

		it('should update step filters', inject(($q, PlaygroundService) => {
			// given
			const ctrl = createController();
			const newFilterValue = [{ value: ['toto'] }];

			spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when());

			// when
			ctrl.updateStepFilter(stepWithMultipleFilters, multiValuedFilter, newFilterValue);
			scope.$digest();

			// then
			expect(PlaygroundService.updateStep).toHaveBeenCalled();
			const callArgs = PlaygroundService.updateStep.calls.argsFor(0);
			expect(callArgs[0]).toBe(stepWithMultipleFilters);
			expect(callArgs[1].filter).toEqual({ contains: { field: '0002', value: ['toto'] } });
		}));
	});

	describe('remove step filter', () => {
		const filter1 = {
			type: 'exact',
			colId: '0000',
			colName: 'name',
			args: {
				phrase: '        AMC  ',
				caseSensitive: true,
			},
			value: '        AMC  ',
		};
		const filter2 = {
			type: 'contains',
			colId: '0002',
			args: {
				phrase: ['toto'],
			},
		};
		let stepDeleteLinesWithSingleFilter;
		let stepWithMultipleFilters;

		beforeEach(inject((FilterAdapterService) => {
			spyOn(FilterAdapterService, 'toTree').and.returnValue({
				filter: {
					contains: {
						field: '0002',
						value: ['toto'],
					},
				},
			});
			stepDeleteLinesWithSingleFilter = {
				transformation: { label: 'Delete lines' },
				actionParameters: {
					action: 'delete_lines',
					parameters: { column_name: 'firstname', scope: 'column' },
				},
				filters: [filter1],
			};
			stepWithMultipleFilters = {
				transformation: { label: 'Replace empty value ...' },
				actionParameters: { parameters: { column_name: 'firstname', scope: 'column' } },
				filters: [filter1, filter2],
			};
		}));

		it('should remove step filter', inject(($q, PlaygroundService) => {
			// given
			spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
			const ctrl = createController();

			// when
			ctrl.removeStepFilter(stepWithMultipleFilters, filter1);
			scope.$digest();

			// then
			expect(PlaygroundService.updateStep).toHaveBeenCalled();
			const callArgs = PlaygroundService.updateStep.calls.argsFor(0);
			expect(callArgs[0]).toBe(stepWithMultipleFilters);
			expect(callArgs[1].filter).toEqual({ contains: { field: '0002', value: ['toto'] } });
		}));

		it('should show warning message on delete lines step with last filter removal', inject(($q, PlaygroundService, MessageService) => {
			// given
			spyOn(MessageService, 'warning').and.returnValue();
			spyOn(PlaygroundService, 'updateStep').and.returnValue($q.when(true));
			const ctrl = createController();

			// when
			ctrl.removeStepFilter(stepDeleteLinesWithSingleFilter, filter1);
			scope.$digest();

			// then
			expect(MessageService.warning).toHaveBeenCalled();
			expect(PlaygroundService.updateStep).not.toHaveBeenCalled();
			expect(stepDeleteLinesWithSingleFilter.filters.length).toBe(1);
		}));
	});

	describe('knots', () => {
		describe('Show Top Line', () => {
			it('should return false when step is first', () => {
				// given
				const ctrl = createController();

				// when
				const showTopLine = ctrl.showTopLine(steps[0]);

				// then
				expect(showTopLine).toBe(false);
			});

			it('should return true when step is not first and is active', () => {
				// given
				const ctrl = createController();

				// when
				const showTopLine = ctrl.showTopLine(steps[1]);

				// then
				expect(showTopLine).toBe(true);
			});

			it('should return true when step is not first and it will be activated', () => {
				// given
				const ctrl = createController();
				stateMock.playground.recipe.hoveredStep = steps[2];

				// when
				const showTopLine = ctrl.showTopLine(ctrl.step = steps[2]);

				// then
				expect(showTopLine).toBe(true);
			});
		});

		describe('Show Bottom Line', () => {
			it('should return false when step is last', () => {
				// given
				const ctrl = createController();

				// when
				const showBottomLine = ctrl.showBottomLine(steps[3]);

				// then
				expect(showBottomLine).toBe(false);
			});

			it('should return true when step is not last, it is active, and not the last active', () => {
				// given
				stateMock.playground.recipe.current.lastActiveStep = steps[0];
				const ctrl = createController();

				// when
				const showBottomLine = ctrl.showBottomLine(steps[1]);

				// then
				expect(showBottomLine).toBe(true);
			});

			it('should return true when step is not last, it is inactive, it will NOT be activated and it is not hovered', () => {
				// given
				const ctrl = createController();
				stateMock.playground.recipe.hoveredStep = steps[1];

				// when
				const showBottomLine = ctrl.showBottomLine(steps[2]);

				// then
				expect(showBottomLine).toBe(false);
			});

			it('should return true when step is not last, it will be activated and it is not hovered', () => {
				// given
				const ctrl = createController();
				stateMock.playground.recipe.hoveredStep = steps[3];

				// when
				const showBottomLine = ctrl.showBottomLine(steps[2]);

				// then
				expect(showBottomLine).toBe(true);
			});
		});

		describe('Step will be activated', () => {
			it('should return true for the 3rd step (inactive), when hovering on the 4th step (inactive)', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[3];
				const ctrl = createController();

				// when
				const _toBeActivated = ctrl._toBeActivated(steps[2]);

				// then
				expect(_toBeActivated).toBe(true);
			});

			it('should return false for the 4th step (inactive), when the 3rd step (inactive) is hovered', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[2];
				const ctrl = createController();

				// when
				const _toBeActivated = ctrl._toBeActivated(steps[3]);

				// then
				expect(_toBeActivated).toBe(false);
			});
		});

		describe('Step will be deactivated', () => {
			it('should return true for the 2nd step (active), when the 1st step (active) is hovered', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[0];
				const ctrl = createController();

				// when
				const _toBeDeactivated = ctrl._toBeDeactivated(steps[1]);

				// then
				expect(_toBeDeactivated).toBe(true);
			});

			it('should return false for the 1st step (active) when the 2nd step (active) is hovered', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[1];
				const ctrl = createController();

				// when
				const _toBeDeactivated = ctrl._toBeDeactivated(steps[0]);

				// then
				expect(_toBeDeactivated).toBe(false);
			});
		});

		describe('Step will switch its status', () => {
			it('should return true when inactive step will be activated', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[3];
				const ctrl = createController();

				// when
				const toBeSwitched = ctrl.toBeSwitched(steps[3]);

				// then
				expect(toBeSwitched).toBe(true);
			});

			it('should return true when active step will be deactivated', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[1];
				const ctrl = createController();

				// when
				const toBeSwitched = ctrl.toBeSwitched(steps[1]);

				// then
				expect(toBeSwitched).toBe(true);
			});
		});

		describe('Hovered status', () => {
			it('should return true if the step is hovered', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[1];
				const ctrl = createController();

				// when
				const hovered = ctrl.isHoveredStep(steps[1]);

				// then
				expect(hovered).toBe(true);
			});

			it('should return true if the step is hovered', () => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[1];
				const ctrl = createController();

				// when
				const hovered = ctrl.isHoveredStep(steps[0]);

				// then
				expect(hovered).toBe(false);
			});
		});

		describe('Hover Start/End', () => {

			beforeEach(inject((StateService, RecipeKnotService) => {
				spyOn(StateService, 'setHoveredStep').and.returnValue();
				spyOn(RecipeKnotService, 'stepHoverStart').and.returnValue();
				spyOn(RecipeKnotService, 'stepHoverEnd').and.returnValue();
			}));

			it('should start hover', inject((StateService, RecipeKnotService) => {
				// given
				const ctrl = createController();

				// when
				ctrl.stepHoverStart(steps[1]);

				// then
				expect(StateService.setHoveredStep).toHaveBeenCalledWith(steps[1]);
				expect(RecipeKnotService.stepHoverStart).toHaveBeenCalledWith(steps[1]);
			}));

			it('should return true if the step is hovered', inject((StateService, RecipeKnotService) => {
				// given
				stateMock.playground.recipe.hoveredStep = steps[1];
				const ctrl = createController();

				// when
				ctrl.stepHoverEnd(steps[0]);

				// then
				expect(StateService.setHoveredStep).toHaveBeenCalledWith(null);
				expect(RecipeKnotService.stepHoverEnd).toHaveBeenCalledWith(steps[0]);
			}));
		});

		describe('Drag Start/dragMove/End', () => {
			it('should have isDrag to false by default', () => {
				// given
				const ctrl = createController();

				// then
				expect(ctrl.isDragStart).toBeFalsy();
			});

			it('should enable isDrag', () => {
				// given
				const ctrl = createController();

				// when
				ctrl.dragStart();

				// then
				expect(ctrl.isDragStart).toBeTruthy();
			});

			it('should update mouse position while dragging', () => {
				// given
				const ctrl = createController();
				const eventClientY = { clientY: 500 };

				// when
				ctrl.dragMove(null, null, eventClientY);

				// then
				expect(ctrl.eventClientY).toBe(eventClientY.clientY);
			});

			it('should disable isDrag', () => {
				// given
				const ctrl = createController();
				ctrl.isDragStart = false;

				// when
				ctrl.dragEnd();

				// then
				expect(ctrl.isDragStart).toBeFalsy();
			});
		});

		describe('click on a knot', () => {
			it('should toggle step', inject((PlaygroundService) => {
				// given
				spyOn(PlaygroundService, 'toggleStep').and.returnValue();
				const ctrl = createController();

				// when
				ctrl.toggleStep(steps[0]);

				// then
				expect(PlaygroundService.toggleStep).toHaveBeenCalledWith(steps[0]);
			}));
		});
	});

	describe('setting the step that will be deleted', () => {
		it('should set the step that will be deleted', () => {
			// given
			const ctrl = createController();
			const step = {};
			expect(ctrl.stepToBeDeleted).toBe(null);

			// when
			ctrl.setStepToBeDeleted(step);

			// then
			expect(ctrl.stepToBeDeleted).toBe(step);
		});

		it('should reset the step that will be deleted', () => {
			const ctrl = createController();
			ctrl.stepToBeDeleted = {};

			// when
			ctrl.resetStepToBeDeleted();

			// then
			expect(ctrl.stepToBeDeleted).toBe(null);
		});
	});

	describe('steps that should be removed', () => {
		it('should return true when the current step should be removed', () => {
			// given
			const ctrl = createController();
			const step = { transformation: { stepId: 'abc-def' } };
			ctrl.stepToBeDeleted = step;

			// when
			const shouldBeRemoved = ctrl.shouldBeRemoved(step);

			// then
			expect(shouldBeRemoved).toBe(true);
		});

		it('should return true when the step parent should be also removed', () => {
			// given
			const ctrl = createController();
			const parentStep = {
				transformation: {
					stepId: 'abc-def',
				},
				diff: {
					createdColumns: ['0008'],
				},
			};
			ctrl.stepToBeDeleted = parentStep;
			const childStep = {
				transformation: {
					stepId: '123-abd',
				},
				actionParameters: {
					parameters: {
						column_id: '0008',
					},
				},
			};

			// when
			const shouldBeRemoved = ctrl.shouldBeRemoved(childStep);

			// then
			expect(shouldBeRemoved).toBe(true);
		});
	});
});
