/*  ============================================================================

  Copyright (C) 2006-2018 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

export default function htmlLang($rootScope) {
	'ngInject';

	return {
		restrict: 'A',
		link: (scope, element) => {
			const listener = function (event, translationResp) {
				const defaultLang = 'en';
				const currentlang = translationResp && translationResp.language;

				element
					.removeAttr('html-lang')
					.attr('lang', currentlang || defaultLang);
			};

			$rootScope.$on('$translateChangeSuccess', listener);
		},
	};
}
