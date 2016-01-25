describe('folder directive', function() {
	'use strict';

	var scope, createElement, element, stateMock, controller;

	beforeEach(module('data-prep.folder', function($provide){
		stateMock = {
			folder : {
				foldersStack : [
					{id:'', path:'', name: 'HOME_FOLDER'},
					{id : '1', path: '1', name: '1'},
					{id : '1/2', path: '1/2', name: '2'}
				],
				menuChildren:[
					{'id':'TDP-714','path':'TDP-714','name':'TDP-714','creationDate':1448984715000,'modificationDate':1448984715000},
					{'id':'lookups','path':'lookups','name':'lookups','creationDate':1448895776000,'modificationDate':1448895776000}
				]
			}
		};
		$provide.constant('state', stateMock);
	}));

	beforeEach(module('htmlTemplates'));
	beforeEach(module('pascalprecht.translate', function ($translateProvider) {
		$translateProvider.translations('en', {
			'LOADING': 'Loading...'
		});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(inject(function($rootScope, $compile, FolderService) {
		scope = $rootScope.$new();
		spyOn(FolderService, 'getContent').and.returnValue();

		createElement = function() {
			element = angular.element('<folder></folder>');
			$compile(element)(scope);
			scope.$digest();

			controller = element.controller('folder');
		};
	}));

	afterEach(function() {
		scope.$destroy();
		element.remove();
	});

	it('should render folders', function() {
		//when
		createElement();

		//then
		expect(element.find('.breadcrumb > ul > li').length).toBe(3);
		expect(element.find('.breadcrumb > ul > li').eq(1).attr('id')).toBe('folder_1');
		expect(element.find('talend-dropdown').length).toBe(3);
		expect(element.find('.dropdown-menu').length).toBe(3);
	});

	it('should get folder content on element creation', inject(function(FolderService) {
		//when
		createElement();

		//then
		expect(FolderService.getContent).toHaveBeenCalled();
	}));

	it('should change current folder on different folder click', inject(function(FolderService) {
		//given
		createElement();
		expect(FolderService.getContent.calls.count()).toBe(1);

		//when
		element.find('#folder_1 .dropdown-container > a').eq(0).click();
		scope.$digest();

		//then
		expect(FolderService.getContent.calls.count()).toBe(2);
	}));

	describe('folder children', function(){

		beforeEach(inject(function($q, FolderService) {
			spyOn(FolderService, 'populateMenuChildren').and.returnValue($q.when(true));
		}));

		it('should load menu children content', inject(function(FolderService){
			//given
			createElement();

			//when
			element.find('#folder_1 .dropdown-button').eq(0).click();
			scope.$digest();

			//then
			expect(FolderService.populateMenuChildren).toHaveBeenCalledWith(stateMock.folder.foldersStack[1]);
		}));

		it('should show menu children', function(){
			//given
			createElement();

			//when
			element.find('#folder_1 .dropdown-button').eq(0).click();
			scope.$digest();

			//then
			expect(element.find('#folder_1 .dropdown-menu > li').eq(0).text().trim()).toBe('TDP-714');
			expect(element.find('#folder_1 .dropdown-menu > li').eq(1).text().trim()).toBe('lookups');
		});

		it('should go to subfolder menu children', inject(function(FolderService){
			//given
			createElement();

			//when
			element.find('#folder_1 .dropdown-button').eq(0).click();
			scope.$digest();

			element.find('#folder_1 .dropdown-menu > li').eq(0).click();
			scope.$digest();

			//then
			expect(FolderService.getContent).toHaveBeenCalledWith(stateMock.folder.menuChildren[0]);
		}));
	});
});