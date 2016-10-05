/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

import template from './navbar.html';

/**
 * @ngdoc directive
 * @name talend.widget.directive:TalendNavbar
 * @description Navbar widget
 * @restrict E
 * @usage
 * <talend-navbar>
 *      <a href="http://dev.talend.com" class="logo-wrapper">
 *          <span class="logo"></span>
 *      </a>
 *      <a href="javascript:void(0)" class="navigation-menu-button">MENU</a>
 *      <nav role="navigation">
 *          <ul class="navigation-menu show">
 *              <li class="nav-link"><a href="http://dev.talend.com">item</a></li>
 *              <li class="nav-link more">
 *                  <a href="javascript:void(0)" class="icon t-top-bar_profile nomore"></a>
 *                  <ul class="submenu">
 *                      <li><a href="javascript:void(0)">Logout</a></li>
 *                      <li class="divider"></li>
 *                      <li><a href="javascript:void(0)">Edit profil</a></li>
 *                  </ul>
 *              </li>
 *          </ul>
 *      </nav>
 *      <div class="navigation-tools">
 *          <div class="search-bar">
 *              <form role="search">
 *                  <input type="search" placeholder="Search anything here" />
 *              </form>
 *          </div>
 *      </div>
 * </talend-navbar>
 * @param {class} logo-wrapper Logo container
 * @param {class} navigation-menu-button Menu toggle on mobile
 * @param {class} navigation-menu Menu
 * @param {class} nav-link `navigation-menu > li.nav-link` Menu items
 * @param {class} more `navigation-menu > li.more` Dropdown menu items with caret
 * @param {class} submenu `navigation-menu > li.more > ul.submenu` Dropdown submenu
 * @param {class} navigation-tools Tools (ex: search bar)
 */
export default function TalendNavbar() {
	return {
		restrict: 'EA',
		transclude: true,
		templateUrl: template,
		link: {
			post: (scope, iElement) => {
				const menuToggle = iElement.find('.navigation-menu-button').unbind();
				const menu = iElement.find('.navigation-menu');

				menu.removeClass('show');
				menuToggle.on('click', (e) => {
					e.preventDefault();
					iElement.find('.navigation-menu').slideToggle(() => {
						if (menu.css('display') === 'none') {
							menu.removeAttr('style');
						}
					});
				});
			},
		},
	};
}
