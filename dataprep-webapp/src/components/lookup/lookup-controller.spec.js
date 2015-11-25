describe('Lookup controller', function () {
	'use strict';

	var createController, scope, stateMock;
	var dsActions = [
		{
			'category': 'data_blending',
			'name': 'lookup',
			'parameters': [
				{
					'name': 'column_id',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'filter',
					'type': 'filter',
					'default': ''
				},
				{
					'name': 'lookup_ds_name',
					'type': 'string',
					'default': 'lookup_2'
				},
				{
					'name': 'lookup_ds_id',
					'type': 'string',
					'default': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7'
				},
				{
					'name': 'lookup_ds_url',
					'type': 'string',
					'default': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true'
				},
				{
					'name': 'lookup_join_on',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'lookup_join_on_name',
					'type': 'string',
					'default': ''
				},
				{
					'name': 'lookup_selected_cols',
					'type': 'list',
					'default': ''
				}
			]
		}
	];

	var lookupParams = {
		'column_id': '',
		'filter': '',
		'lookup_ds_name': 'lookup_2',
		'lookup_ds_id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
		'lookup_ds_url': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true',
		'lookup_join_on': '',
		'lookup_join_on_name': '',
		'lookup_selected_cols': ''
	};

	beforeEach(module('data-prep.lookup', function ($provide) {
		stateMock = {
			playground: {
				grid: {
					selectedColumn: {
						id:'mainGridColId',
						name:'mainGridColName'
					}
				},
				lookupGrid:{
					selectedColumn: {
						id:'lookupGridColId',
						name:'lookupGridColName'
					},
					lookupColumnsToAdd: ['0002','0003']
				},
				dataset:null
			}
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(function ($rootScope, $controller) {
		scope = $rootScope.$new();

		createController = function () {
			return $controller('LookupCtrl', {
				$scope: scope
			});
		};
	}));

	describe('as soon as the main dataset changes', function () {
		it('should trigger $http GET query of the possible lookup datasets when dataset change', inject(function (LookupService, $q) {
			//given
			var ctrl      = createController();
			spyOn(LookupService, 'getLookupPossibleActions').and.returnValue($q.when({data: dsActions}));
			spyOn(ctrl, 'loadSelectedLookupContent').and.returnValue();

			//when
			stateMock.playground.dataset = {
				'id': '14d116a0-b180-4c5f-ba25-46807fc61e42',
				'records': 101,
				'parameters': {},
				'name': 'customers_100_with_pb',
				'author': 'anonymousUser',
				'created': 1447888871218,
				'encoding': 'UTF-8'
			};
			scope.$digest();

			//then
			expect(LookupService.getLookupPossibleActions).toHaveBeenCalledWith(stateMock.playground.dataset.id);
			expect(ctrl.loadSelectedLookupContent).toHaveBeenCalledWith(dsActions[0]);
		}));
	});

	describe('on loading a dataset lookup content manually or automatically', function(){
		it('should load a specific dataset lookup content', inject(function (StateService, LookupService) {
			//given
			var ctrl      = createController();
			spyOn(StateService, 'resetLookup').and.returnValue();
			spyOn(LookupService, 'loadLookupContent').and.returnValue();

			//when
			ctrl.loadSelectedLookupContent(dsActions[0]);

			//then
			expect(StateService.resetLookup).toHaveBeenCalled();
			expect(ctrl.lookupParams).toEqual(lookupParams);
			expect(ctrl.lookupAction).toBe(dsActions[0]);
			/*jshint camelcase: false */
			expect(LookupService.loadLookupContent).toHaveBeenCalledWith(ctrl.lookupParams.lookup_ds_url);
		}));
	});

	describe('Confirm button interaction', function(){
		it('should trigger lookup preview', inject(function (EarlyPreviewService) {
			//given
			var ctrl      = createController();
			spyOn(EarlyPreviewService, 'earlyPreview').and.returnValue(function(){});
			ctrl.lookupAction = dsActions[0];
			ctrl.lookupParams = lookupParams;

			//when
			ctrl.hoverSubmitBtn();

			//then
			expect(ctrl.lookupParams).toEqual({
				'column_id': 'mainGridColId',
				'column_name': 'mainGridColName',
				'filter': '',
				'lookup_ds_name': 'lookup_2',
				'lookup_ds_id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
				'lookup_ds_url': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true',
				'lookup_join_on': 'lookupGridColId',
				'lookup_join_on_name': 'lookupGridColName',
				'lookup_selected_cols': ['0002','0003']
			});
			expect(EarlyPreviewService.earlyPreview).toHaveBeenCalledWith(ctrl.lookupAction, 'dataset');
		}));

		it('should submit lookup action', inject(function (TransformationApplicationService, EarlyPreviewService, $q) {
			//given
			jasmine.clock().install();
			var ctrl      = createController();
			spyOn(TransformationApplicationService, 'append').and.returnValue($q.when(true));
			spyOn(EarlyPreviewService, 'activatePreview').and.returnValue();
			spyOn(EarlyPreviewService, 'deactivatePreview').and.returnValue();
			spyOn(EarlyPreviewService, 'cancelPendingPreview').and.returnValue();
			ctrl.lookupAction = dsActions[0];
			ctrl.lookupParams = lookupParams;

			//when
			ctrl.submitLookup();
			expect(EarlyPreviewService.activatePreview).not.toHaveBeenCalled();
			scope.$digest();
			jasmine.clock().tick(500);

			//then
			expect(ctrl.lookupParams).toEqual({
				'column_id': 'mainGridColId',
				'column_name': 'mainGridColName',
				'filter': '',
				'lookup_ds_name': 'lookup_2',
				'lookup_ds_id': '9e739b88-5ec9-4b58-84b5-2127a7e2eac7',
				'lookup_ds_url': 'http://172.17.0.211:8080/datasets/9ee2eac7/content?metadata=true',
				'lookup_join_on': 'lookupGridColId',
				'lookup_join_on_name': 'lookupGridColName',
				'lookup_selected_cols': ['0002','0003']
			});
			expect(TransformationApplicationService.append).toHaveBeenCalledWith(ctrl.lookupAction, 'dataset', ctrl.lookupParams);
			expect(EarlyPreviewService.deactivatePreview).toHaveBeenCalled();
			expect(EarlyPreviewService.cancelPendingPreview).toHaveBeenCalled();
			expect(EarlyPreviewService.activatePreview).toHaveBeenCalled();
			jasmine.clock().uninstall();
		}));
	});
});