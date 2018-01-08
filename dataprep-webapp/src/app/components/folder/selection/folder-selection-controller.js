/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class FolderSelectionCtrl {
	constructor($translate) {
		'ngInject';

		this.$translate = $translate;
		this.searchFolderQuery = '';
	}

	/**
	 * @ngdoc method
	 * @name $onChange
	 * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
	 * @description initializes the folders tree on controller creation
	 **/
	$onChanges() {
		this._initTree();
	}

	// --------------------------------------------------------------------------------------------
	// ---------------------------------------------- INITIALIZATION ------------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name _initTree
	 * @description Adapt the folder tree
	 **/
	_initTree() {
		if (!this.tree) {
			return;
		}

		if (this.selectedFolder) {
			const id = this.selectedFolder.id;
			const hierarchy = this._locate([], this.tree, tree => tree.folder.id === id);

			if (hierarchy) {
				hierarchy.forEach((node) => {
					node.folder.opened = true;
				});
				this.selectedFolder = hierarchy.pop().folder;
				this.selectedFolder.selected = true;
			}
			else {
				this.selectedFolder = this.tree.folder; // home folder
				this.selectedFolder.selected = true;
			}
		}
		else {
			this.selectedFolder = this.tree.folder; // home folder
			this.selectedFolder.selected = true;
		}
	}

	_locate(accu, node, predicate) {
		const nextAccu = accu.concat(node);
		if (predicate(node)) {
			return nextAccu;
		}

		if (node.children.length) {
			for (const child of node.children) {
				const pathToFolder = this._locate(accu, child, predicate);
				if (pathToFolder) {
					return nextAccu.concat(pathToFolder);
				}
			}
		}
	}

	_search(accu, node, name) {
		let nextAccu = accu;
		if (node.folder.name.toLowerCase().indexOf(name.toLowerCase()) > -1) {
			nextAccu = nextAccu.concat(node);
		}

		if (node.children.length) {
			for (const child of node.children) {
				nextAccu = this._search(nextAccu, child, name);
			}
		}

		return nextAccu;
	}

	// --------------------------------------------------------------------------------------------
	// -------------------------------------------- ACTIONS ON FOLDERS ----------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name toggle
	 * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
	 * @description show/hides the children of a given folder in the tree
	 * @param {Object} treeNode The node to toggle
	 **/
	toggle(treeNode) {
		treeNode.folder.opened = !treeNode.folder.opened;
	}

	/**
	 * @ngdoc method
	 * @name chooseFolder
	 * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
	 * @description selects a folder
	 * @param {Object} folder to select
	 **/
	chooseFolder(folder) {
		this.selectedFolder.selected = false;
		this.selectedFolder = folder;
		this.selectedFolder.selected = true;
	}

	// --------------------------------------------------------------------------------------------
	// ------------------------------------------------ SEARCH FOLDERS ----------------------------
	// --------------------------------------------------------------------------------------------
	/**
	 * @ngdoc method
	 * @name performSearch
	 * @methodOf data-prep.folder-selection.controller:FolderSelectionCtrl
	 * @description perform folder search
	 **/
	performSearch() {
		if (this.searchFolderQuery) {
			// save tree selection
			this.lastTreeSelection = this.lastTreeSelection || this.selectedFolder;
			this.searchItems = this._search([], this.tree, this.searchFolderQuery);
			if (this.searchItems.length) {
				this.chooseFolder(this.searchItems[0].folder);
			}
		}
		else {
			// reset search result
			this.searchItems = null;
			// reset last tree selection as selected node
			this.chooseFolder(this.lastTreeSelection);
			this.lastTreeSelection = null;
		}
	}
}

export default FolderSelectionCtrl;
