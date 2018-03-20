/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Step Description controller', () => {
	let createController;
	let scope;

	beforeEach(angular.mock.module('data-prep.step-description'));

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {
			RECIPE_ITEM_ON_COL: '<span class="step-number">{{index}}</span> <span class="step-label">{{label}}</span> on column <div class="step-scope" title="{{columnName}}">{{columnName}}</div>',
			RECIPE_ITEM_ON_CELL: '<span class="step-number">{{index}}</span> <span class="step-label">{{label}}</span> on cell',
			RECIPE_ITEM_ON_LINE: '<span class="step-number">{{index}}</span> <span class="step-label">{{label}}</span> <span class="step-scope">#{{rowId}}</span>',
			RECIPE_ITEM_ON_DATASET: '<span class="step-number">{{index}}</span> <span class="step-label">{{label}}</span> on table',
			LOOKUP_STEP_DESCRIPTION: '<span class="step-number">{{index}}</span> <span class="step-label">{{label}}</span> done with dataset <div class="step-scope" title="{{lookupDsName}}">{{lookupDsName}}</div>. Join has been set between <div class="step-scope" title="{{mainColName}}">{{mainColName}}</div> and <div class="step-scope" title="{{lookupColName}}">{{lookupColName}}</div>. ',
			ONLY_1_ADDED_COL: 'The column <div class="step-scope" title="{{firstCol}}">{{firstCol}}</div> has been added.',
			ONLY_2_ADDED_COLS: 'The columns <div class="step-scope" title="{{firstCol}}">{{firstCol}}</div> and <div class="step-scope" title="{{secondCol}}">{{secondCol}}</div> have been added.',
			MORE_THEN_2_ADDED_COLS: 'The columns <div class="step-scope" title="{{firstCol}}">{{firstCol}}</div>, <div class="step-scope" title="{{secondCol}}">{{secondCol}}</div> and <span title="{{restOfCols}}">{{restOfColsNbr}}</span> other(s) have been added.',
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(($rootScope, $componentController) => {
		scope = $rootScope.$new();

		createController = () => $componentController('stepDescription');
	}));

	describe('update description', () => {
		it('should translate description on scope: column', () => {
			//given
			const ctrl = createController();
			ctrl.index = 5;
			ctrl.step = {
				column: { id: '0', name: 'col1' },
				transformation: {
					stepId: '13a24e8765ef4',
					name: 'split',
					label: 'Split',
					category: 'split',
					parameters: [{ name: 'pattern', type: 'string' }],
					items: [],
				},
				actionParameters: {
					action: 'split',
					parameters: {
						scope: 'column',
						column_id: '0',
						pattern: '/',
					},
				},
			};

			//when
			ctrl.$onChanges();
			scope.$digest();

			//then
			expect(ctrl.stepDescription).toBe('<span class="step-number">6</span> <span class="step-label">Split</span> on column <div class="step-scope" title="col1">col1</div>');
		});

		it('should translate description on scope: cell', () => {
			//given
			const ctrl = createController();
			ctrl.index = 2;
			ctrl.step = {
				column: { id: '1', name: 'col2' },
				transformation: {
					stepId: '456bb784a9674e532fc446',
					name: 'replace_on_value',
					label: 'Replace value',
					category: 'quickfix',
					parameters: [
						{ name: 'cell_value', type: 'string' },
						{ name: 'replace_value', type: 'string' },
					],
				},
				actionParameters: {
					action: 'quickfix',
					parameters: {
						scope: 'cell',
						column_id: '1',
						row_id: 56,
					},
				},
				inactive: true,
			};

			//when
			ctrl.$onChanges();
			scope.$digest();

			//then
			expect(ctrl.stepDescription).toBe('<span class="step-number">3</span> <span class="step-label">Replace value</span> on cell');
		});

		it('should translate description on scope: line', () => {
			//given
			const ctrl = createController();
			ctrl.index = 0;
			ctrl.step = {
				column: { id: undefined, name: undefined },
				row: { id: 125 },
				transformation: {
					stepId: '3213ca58454a58d436',
					name: 'delete',
					label: 'Delete Line',
					category: 'clean',
					parameters: null,
				},
				actionParameters: {
					action: 'delete',
					parameters: {
						scope: 'line',
						column_id: undefined,
						row_id: 125,
					},
				},
				inactive: true,
			};

			//when
			ctrl.$onChanges();
			scope.$digest();

			//then
			expect(ctrl.stepDescription).toBe('<span class="step-number">1</span> <span class="step-label">Delete Line</span> <span class="step-scope">#125</span>');
		});


        describe('translate description on scope: dataset,', () => {
            it('should translate description for reordering step', () => {
                //given
                const ctrl = createController();
                ctrl.index = 0;
                ctrl.step = {
                    column: { id: '0', name: 'col1' },
                    transformation: {
                        stepId: '13a24e8765ef4',
                        name: 'reorder',
                        label: 'reorder',
                        category: 'columns',
                        parameters: [{ name: 'pattern', type: 'string' }],
                        items: [],
                    },
                    actionParameters: {
                        action: 'reorder',
                        parameters: {
                            selected_column: '0003',
                            column_id: '0000',
                            scope: 'dataset',
                            column_name: 'name',
                            dataset_action_display_type: 'column',
                        },
                    },
                };

                //when
                ctrl.$onChanges();
                scope.$digest();

                //then
                expect(ctrl.stepDescription).toBe('<span class="step-number">1</span> <span class="step-label">reorder</span> on column <div class="step-scope" title="col1">col1</div>');
            });
		});

		describe('translate description on scope: dataset,', () => {
            it('on action changes', () => {
                //given
                const ctrl = createController();
                ctrl.index = 6;
                ctrl.step = {
                    column: { id: '0', name: 'col1' },
                    transformation: {
                        stepId: '13a24e8765ef4',
                        name: 'Dedup',
                        label: 'Dedup',
                        category: 'Dedup',
                        parameters: [],
                        items: [],
                    },
                    actionParameters: {
                        action: 'Dedup',
                        parameters: {
                            scope: 'dataset',
                            column_id: '0',
                            pattern: '/',
                        },
                    },
                };

                //when
                ctrl.$onChanges();
                scope.$digest();

                //then
                expect(ctrl.stepDescription).toBe('<span class="step-number">7</span> <span class="step-label">Dedup</span> on table');
            });

			describe('on lookup action', () => {
				it('should translate description with 1 column', () => {
					//given
					const ctrl = createController();
					ctrl.index = 10;
					ctrl.step = {
						column: {
							id: '0000',
							name: 'id',
						},
						transformation: {
							parameters: [],
							label: 'Lookup',
							name: 'lookup',
						},
						actionParameters: {
							action: 'lookup',
							parameters: {
								column_id: '0000',
								filter: '',
								lookup_ds_name: 'customers_100_with_pb',
								lookup_ds_id: '14d116a0-b180-4c5f-ba25-46807fc61e42',
								lookup_ds_url: 'http://172.17.0.30:8080/datasets/14d116a0-b180-4c5f-ba25-46807fc61e42/content?metadata=true',
								lookup_join_on: '0000',
								lookup_join_on_name: 'id',
								lookup_selected_cols: [
									{
										name: 'firstname',
										id: '0001',
									},
								],
								column_name: 'id',
								scope: 'dataset',
							},
						},
					};

					//when
					ctrl.$onChanges();
					scope.$digest();

					//then
					expect(ctrl.stepDescription).toBe('<span class="step-number">11</span> <span class="step-label">Lookup</span> done with dataset <div class="step-scope" title="customers_100_with_pb">customers_100_with_pb</div>. Join has been set between <div class="step-scope" title="id">id</div> and <div class="step-scope" title="id">id</div>. The column <div class="step-scope" title="firstname">firstname</div> has been added.');
				});

				it('should translate description with 2 columns', () => {
					//given
					const ctrl = createController();
					ctrl.index = 1;
					ctrl.step = {
						column: {
							id: '0000',
							name: 'id',
						},
						transformation: {
							parameters: [],
							label: 'Lookup',
							name: 'lookup',
						},
						actionParameters: {
							action: 'lookup',
							parameters: {
								column_id: '0000',
								filter: '',
								lookup_ds_name: 'customers_100_with_pb',
								lookup_ds_id: '14d116a0-b180-4c5f-ba25-46807fc61e42',
								lookup_ds_url: 'http://172.17.0.30:8080/datasets/14d116a0-b180-4c5f-ba25-46807fc61e42/content?metadata=true',
								lookup_join_on: '0000',
								lookup_join_on_name: 'id',
								lookup_selected_cols: [
									{
										name: 'firstname',
										id: '0001',
									},
									{
										name: 'lastname',
										id: '0002',
									},
								],
								column_name: 'id',
								scope: 'dataset',
							},
						},
					};

					//when
					ctrl.$onChanges();
					scope.$digest();

					//then
					expect(ctrl.stepDescription).toBe('<span class="step-number">2</span> <span class="step-label">Lookup</span> done with dataset <div class="step-scope" title="customers_100_with_pb">customers_100_with_pb</div>. Join has been set between <div class="step-scope" title="id">id</div> and <div class="step-scope" title="id">id</div>. The columns <div class="step-scope" title="firstname">firstname</div> and <div class="step-scope" title="lastname">lastname</div> have been added.');
				});

				it('should translate description with more than 2 columns', () => {
					//given
					const ctrl = createController();
					ctrl.index = 2;
					ctrl.step = {
						column: {
							id: '0000',
							name: 'id',
						},
						transformation: {
							parameters: [],
							label: 'Lookup',
							name: 'lookup',
						},
						actionParameters: {
							action: 'lookup',
							parameters: {
								column_id: '0000',
								filter: '',
								lookup_ds_name: 'customers_100_with_pb',
								lookup_ds_id: '14d116a0-b180-4c5f-ba25-46807fc61e42',
								lookup_ds_url: 'http://172.17.0.30:8080/datasets/14d116a0-b180-4c5f-ba25-46807fc61e42/content?metadata=true',
								lookup_join_on: '0000',
								lookup_join_on_name: 'id',
								lookup_selected_cols: [
									{
										name: 'firstname',
										id: '0001',
									},
									{
										name: 'lastname',
										id: '0002',
									},
									{
										name: 'state',
										id: '0003',
									},
									{
										name: 'registration',
										id: '0004',
									},
								],
								column_name: 'id',
								scope: 'dataset',
							},
						},
					};

					//when
					ctrl.$onChanges();
					scope.$digest();

					//then
					expect(ctrl.stepDescription).toBe('<span class="step-number">3</span> <span class="step-label">Lookup</span> done with dataset <div class="step-scope" title="customers_100_with_pb">customers_100_with_pb</div>. Join has been set between <div class="step-scope" title="id">id</div> and <div class="step-scope" title="id">id</div>. The columns <div class="step-scope" title="firstname">firstname</div>, <div class="step-scope" title="lastname">lastname</div> and <span title="state, registration">2</span> other(s) have been added.');
				});
			});
		});
	});
});
