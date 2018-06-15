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
	const t = item => ({
		name: item.folder.name || 'Home',
		children: item.children.map(t),
	});
	return [t(input)];
}

export default {
	transform,
	transformTree,
};
