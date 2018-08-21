import i18n from '../../i18n';

function transform({ folders = [], preparations = [] }) {
	const adaptedFolders = folders.map(folder => ({
		author: folder.ownerId,
		className: 'list-item-folder',
		icon: 'talend-folder',
		id: folder.id,
		name: folder.name,
		type: 'folder',
	}));
	const adaptedPreparations = preparations.map(prep => ({
		author: prep.author,
		className: 'list-item-preparation',
		datasetName: prep.dataset.dataSetName,
		icon: 'talend-dataprep',
		id: prep.id,
		name: prep.name,
		nbSteps: prep.steps.length - 1,
		type: 'preparation',
	}));
	return adaptedFolders.concat(adaptedPreparations);
}

function transformTree(input) {
	const t = (item) => {
		return {
			id: item.folder.id,
			name: item.folder.name || 'Home',
			children: item.children.map(t),
		};
	};

	return [t(input)];
}

function transformFolder({ folder, hierarchy }) {
	return [
		...hierarchy,
		folder,
	].map(folder =>
		({
			id: folder.id,
			text: folder.name || i18n.t('tdp-app:FOLDER_HOME'),
			title: folder.name || i18n.t('tdp-app:FOLDER_HOME'),
			actionCreator: 'folder:open',
		}));
}

export default {
	transform,
	transformTree,
	transformFolder,
};
