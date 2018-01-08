/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import templateUrl from './list-item-handler.html';

/**
 * @ngdoc component
 * @name talend.widget.component:ListItemHandler
 * @usage
 <list-item-handler show-top-button="true|false"
                    show-bottom-button="true|false"
                    on-top-click="onTopClick()"
                    on-bottom-click="onBottomClick()"></list-item-handler>
 * @param {boolean} showTopButton If we display top button
 * @param {boolean} showBottomButton If we display bottom button
 * @param {function} onTopClick The callback that is triggered on top button click
 * @param {function} onBottomClick The callback that is triggered on bottom button click
 */
const ListItemHandler = {
	bindings: {
		showTopButton: '<',
		showBottomButton: '<',
		onTopClick: '&',
		onBottomClick: '&',
	},
	templateUrl,
};

export default ListItemHandler;
