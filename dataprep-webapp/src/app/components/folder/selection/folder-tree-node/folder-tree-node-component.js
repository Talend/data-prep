/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import template from './folder-tree-node.html';

/**
 * @ngdoc component
 * @name data-prep.folder-selection.component:FolderTreeNodeComponent
 * @description This component display a folder tree node
 * @restrict E
 *
 * @usage
 *<folder-tree-node has-children="false"
 *                  is-opened="false"
 *                  is-selected="node.folder.selected"
 *                  level="0"
 *                  name="node.folder.name"
 *                  path="node.folder.path"
 *                  on-toggle="$ctrl.toggle(node)"
 *                  on-select="$ctrl.chooseFolder(node.folder)">
 *</folder-tree-node>
 *
 * @param {boolean}     hasChildren check whether a node has children or not
 * @param {boolean}     isOpened check whether a node is opened or not
 * @param {boolean}     isSelected whether a node is selected or not
 * @param {integer}     level the level of the node
 * @param {string}      name the name of the tree node
 * @param {string}      path the path of the tree node
 * @param {function}    onSelect function to call when selecting a node
 * @param {function}    onToggle function to call when opening/closing a node
 */

const FolderTreeNodeComponent = {
	bindings: {
		hasChildren: '<',
		isOpened: '<',
		isSelected: '<',
		level: '<',
		name: '<',
		path: '<',
		onSelect: '&',
		onToggle: '&',
	},
	templateUrl: template,
};

export default FolderTreeNodeComponent;
