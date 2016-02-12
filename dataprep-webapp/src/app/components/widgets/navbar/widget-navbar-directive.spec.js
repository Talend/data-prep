/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

'use strict';

describe('Navbar directive', function() {
    var scope, element;

    beforeEach(angular.mock.module('talend.widget'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        
        element = angular.element('<talend-navbar>' + 
        '   <a href="javascript:void(0)" class="navigation-menu-button">MENU</a>' +
        '   <nav role="navigation">' +
        '       <ul class="navigation-menu show">' +
        '           <li class="nav-link"><a href="javascript:void(0)" class="icon t-top-bar_notification"></a></li>' +
        '           <li class="nav-link more"><a href="javascript:void(0)" class="icon t-top-bar_profile nomore"></a>' +
        '               <ul class="submenu">' +
        '                   <li><a href="javascript:void(0)">Logout</a></li>' +
        '                   <li class="divider"></li>' +
        '                   <li><a href="javascript:void(0)">Edit profil</a></li>' +
        '               </ul>' +
        '           </li>' +
        '       </ul>' +
        '   </nav>' +
        '</talend-navbar>');
        $compile(element)(scope);
        scope.$digest();
    }));
    
    it('should remove navigation-menu "show" class', function() {
        //then
        expect(element.find('.navigation-menu').hasClass('show')).toBe(false);
    });
});