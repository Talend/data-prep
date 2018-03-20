/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './folder-tree.html';

/**
 * @ngdoc component
 * @name data-prep.folder-selection.component:FolderTreeComponent
 * @description This component display a folder tree
 * @restrict E
 *
 * @usage
 * <folder-tree level="0"
 *              node="$ctrl.tree"
 *              on-select="$ctrl.chooseFolder(node.folder)"
 *              on-toggle="$ctrl.toggle(node)">
 * </folder-tree>
 *
 * @param {object}      node the root of the tree
 * @param {integer}     level the level of the node
 * @param {function}    onToggle function to call when opening/closing a node
 * @param {function}    onSelect function to call when selecting a node
 */

const FolderTreeComponent = {
	bindings: {
		level: '<',
		node: '<',
		onSelect: '&',
		onToggle: '&',
	},
	templateUrl: template,
};

export default FolderTreeComponent;
