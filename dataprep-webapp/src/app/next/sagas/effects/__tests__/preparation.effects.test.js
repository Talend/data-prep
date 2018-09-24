import { all, call } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { HTTPError } from '@talend/react-cmf/lib/sagas/http';
import { Map } from 'immutable';
import * as effects from '../../effects/preparation.effects';
import {
	IMMUTABLE_STATE,
	IMMUTABLE_SETTINGS,
	API_PAYLOAD,
	API_RESPONSE,
} from './preparation.effects.mock';
import http from '../http';
import PreparationService from '../../../services/preparation.service';
import PreparationCopyMoveModal from '../../../components/PreparationCopyMoveModal';
import preparationWatcher from '../../watchers/preparation.saga';

describe('preparation', () => {
	describe('cancelRename', () => {
		it('should update preparations in the cmf store', () => {
			const preparation = 'id0';
			const gen = effects.cancelRename(preparation);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(IMMUTABLE_STATE).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('preparations');
			const prepUpdated = effect.data.find(prep => prep.get('id') === preparation);
			expect(prepUpdated.get('display')).toEqual('text');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('setTitleEditionMode', () => {
		it('should update preparations in the cmf store', () => {
			const preparation = 'id0';
			const gen = effects.setTitleEditionMode(preparation);
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next(IMMUTABLE_STATE).value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('preparations');
			const prepUpdated = effect.data.find(prep => prep.get('id') === preparation);
			expect(prepUpdated.get('display')).toEqual('input');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('fetch', () => {
		beforeEach(() => {
			PreparationService.transform = jest.fn(() => 'rofl');
		});

		it('should update cmf store with default folder id', () => {
			const payload = {};
			const gen = effects.fetch(payload);

			const effect = gen.next().value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('currentFolderId');
			expect(effect.data).toBe('Lw==');

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(
				call(http.get, '/api/folders/Lw==/preparations'),
			);

			const effectPUT = gen.next(API_RESPONSE).value.PUT.action;
			expect(effectPUT.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effectPUT.collectionId).toBe('preparations');
			expect(effectPUT.data).toEqual('rofl');
			expect(PreparationService.transform).toHaveBeenCalledWith(API_PAYLOAD);
			expect(gen.next().done).toBeTruthy();
		});
		it('should update cmf store with folder id', () => {
			const folderId = 'FOLDER_ID';
			const payload = {
				folderId,
			};
			const gen = effects.fetch(payload);

			const effect = gen.next().value.PUT.action;
			expect(effect.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effect.collectionId).toBe('currentFolderId');
			expect(effect.data).toBe('FOLDER_ID');

			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(
				call(http.get, '/api/folders/FOLDER_ID/preparations'),
			);

			const effectPUT = gen.next(API_RESPONSE).value.PUT.action;
			expect(effectPUT.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effectPUT.collectionId).toBe('preparations');
			expect(effectPUT.data).toEqual('rofl');
			expect(PreparationService.transform).toHaveBeenCalledWith(API_PAYLOAD);
			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('fetchFolder', () => {
		beforeEach(() => {
			PreparationService.transformFolder = jest.fn(() => 'folders');
		});

		it('should update Breadcrumb cmf store with default folder id', () => {
			const payload = {};
			const gen = effects.fetchFolder(payload);
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(call(http.get, '/api/folders/Lw=='));

			const effect = gen.next(API_RESPONSE).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('Breadcrumbs');
			expect(effect.componentState).toEqual(new Map({ items: 'folders', maxItems: 5 }));
			expect(PreparationService.transformFolder).toHaveBeenCalledWith(API_PAYLOAD);
			expect(gen.next().done).toBeTruthy();
		});
		it('should update Breadcrumb cmf store with folder id', () => {
			const folderId = 'FOLDER_ID';
			const payload = {
				folderId,
			};
			const gen = effects.fetchFolder(payload);
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(call(http.get, '/api/folders/FOLDER_ID'));

			const effect = gen.next(API_RESPONSE).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('Breadcrumbs');
			expect(effect.componentState).toEqual(new Map({ items: 'folders', maxItems: 5 }));
			expect(PreparationService.transformFolder).toHaveBeenCalledWith(API_PAYLOAD);
			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('rename', () => {
		it('should rename the preparation and fetch the new preparations list', () => {
			const gen = effects.rename({ id: 'id0', name: 'newPrep0' });

			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effect.fn).toEqual(http.put);
			expect(effect.args[0]).toEqual('/api/preparations/id0');
			expect(effect.args[1]).toEqual({ name: 'newPrep0' });
			expect(gen.next().value).toEqual(call(effects.refreshCurrentFolder));
			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('copy', () => {
		it('should copy the preparation', () => {
			const gen = effects.copy({
				id: 'id0',
				folderId: 'abcd',
				destination: 'efgh',
				title: 'newPrep0',
			});

			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effect.fn).toEqual(http.post);
			expect(effect.args[0]).toEqual(
				'/api/preparations/id0/copy?destination=efgh&newName=newPrep0',
			);
			expect(gen.next().value).toEqual(call(effects.fetch, { folderId: 'abcd' }));
			expect(gen.next().value).toEqual(call(effects.closeCopyMoveModal));
			expect(gen.next().value.PUT.action.type).toBe('TDP_SUCCESS_NOTIFICATION');
			expect(gen.next().done).toBeTruthy();
		});

		it('should set error mode if necessary', () => {
			const error = new HTTPError({
				data: { message: 'err message' },
				response: { statusText: 'err' },
			});
			const gen = effects.copy({
				id: 'id0',
				folderId: 'abcd',
				destination: 'efgh',
				title: 'newPrep0',
			});

			expect(gen.next().value.SELECT).toBeDefined();

			let effect = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effect.fn).toEqual(http.post);
			expect(effect.args[0]).toEqual(
				'/api/preparations/id0/copy?destination=efgh&newName=newPrep0',
			);

			effect = gen.next(error).value.CALL;
			expect(effect.fn).toEqual(effects.setCopyMoveErrorMode);
			expect(effect.args[0]).toEqual('err message');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('move', () => {
		it('should move the preparation', () => {
			const gen = effects.move({
				id: 'id0',
				folderId: 'abcd',
				destination: 'efgh',
				title: 'newPrep0',
			});

			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effect.fn).toEqual(http.put);
			expect(effect.args[0]).toEqual(
				'/api/preparations/id0/move?folder=abcd&destination=efgh&newName=newPrep0',
			);
			expect(gen.next().value).toEqual(call(effects.fetch, { folderId: 'abcd' }));
			expect(gen.next().value).toEqual(call(effects.closeCopyMoveModal));
			expect(gen.next().value.PUT.action.type).toBe('TDP_SUCCESS_NOTIFICATION');
			expect(gen.next().done).toBeTruthy();
		});

		it('should set error mode if necessary', () => {
			const error = new HTTPError({
				data: { message: 'err message' },
				response: { statusText: 'err' },
			});
			const gen = effects.move({
				id: 'id0',
				folderId: 'abcd',
				destination: 'efgh',
				title: 'newPrep0',
			});

			expect(gen.next().value.SELECT).toBeDefined();

			let effect = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effect.fn).toEqual(http.put);
			expect(effect.args[0]).toEqual(
				'/api/preparations/id0/move?folder=abcd&destination=efgh&newName=newPrep0',
			);

			effect = gen.next(error).value.CALL;
			expect(effect.fn).toEqual(effects.setCopyMoveErrorMode);
			expect(effect.args[0]).toEqual('err message');

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('copy/move error mode', () => {
		it('should set copy/move error mode', () => {
			const gen = effects.setCopyMoveErrorMode('nopnop');

			let effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual('Translate(PreparationCopyMoveModal)');
			expect(effect.componentState).toEqual({ error: 'nopnop' });

			effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('preparation:copy:move:editable:text');
			expect(effect.componentName).toEqual('Container(EditableText)');
			expect(effect.componentState).toEqual({ editMode: true });

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('fetchTree', () => {
		it('should fetch the folder Tree', () => {
			const gen = effects.fetchTree();

			expect(gen.next().value.SELECT).toBeDefined();

			const effect = gen.next(IMMUTABLE_SETTINGS).value.PUT.action;
			expect(effect.type).toEqual('GET');
			expect(effect.url).toEqual('/api/folders/tree');
			expect(effect.cmf).toEqual({ collectionId: 'folders' });
			expect(effect.transform).toEqual(PreparationService.transformTree);
		});
	});

	describe('closeCopyMoveModal', () => {
		it('should close CopyMove Modal', () => {
			const gen = effects.closeCopyMoveModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual(PreparationCopyMoveModal.DISPLAY_NAME);
			expect(effect.componentState).toEqual({ show: false });

			expect(gen.next().done).toBeTruthy();
		});
	});

	describe('openCopyMoveModal', () => {
		it('should open CopyMove Modal', () => {
			const gen = effects.openCopyMoveModal({ id: '0000' });
			expect(gen.next().value.SELECT).toBeDefined();
			const effect = gen.next('abcd').value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('default');
			expect(effect.componentName).toEqual(PreparationCopyMoveModal.DISPLAY_NAME);
			expect(effect.componentState).toEqual({
				show: true,
				error: null,
				model: { id: '0000', folderId: 'abcd' },
			});

			expect(gen.next().done).toBeTruthy();
		});
	});
	describe('refresh', () => {
		it('should fetch the new preparations list and folders', () => {
			const payload = { folderId: 'folderId' };
			const gen = effects.refresh(payload);
			expect(gen.next(payload).value).toEqual(
				all([
					call(effects.fetch, payload),
					call(effects.fetchFolder, payload),
				])
			);
		});

		it('should refresh the current folder', () => {
			const gen = effects.refreshCurrentFolder();
			expect(gen.next().value.SELECT).toBeDefined();
			const currentFolderId = '123456789';
			expect(gen.next(currentFolderId).value).toEqual(call(effects.refresh, { folderId: currentFolderId }));
		});
	});

	describe('add', () => {
		it('should open add folder modal', () => {
			const gen = effects.openAddFolderModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('add_folder_modal');
			expect(effect.componentName).toEqual('FolderCreatorModal');
			expect(effect.componentState).toEqual(new Map({
				header: 'Add a folder',
				show: true,
				name: '',
				error: '',
				validateAction: {
					label: 'Add',
					id: 'folder:add',
					disabled: true,
					bsStyle: 'primary',
					actionCreator: 'folder:add',
				},
				cancelAction: {
					label: 'Cancel',
					id: 'folder:add:close',
					bsStyle: 'default btn-inverse',
					actionCreator: 'folder:add:close',
				},
			}));

			expect(gen.next().done).toBeTruthy();
		});

		it('should close add folder modal', () => {
			const gen = effects.closeAddFolderModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('add_folder_modal');
			expect(effect.componentName).toEqual('FolderCreatorModal');
			expect(effect.componentState).toEqual(new Map({ show: false }));

			expect(gen.next().done).toBeTruthy();
		});

		it('should not add folder when it already exists', () => {
			const gen = effects.addFolder();
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next('folderName').value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value.SELECT).toBeDefined();
			const effect = gen.next('folderId').value.CALL;
			expect(effect.fn).toEqual(http.get);
			expect(effect.args[0]).toEqual('/api/folders/folderId/preparations');
			const response = {
				data: {
					folders: [
						{
							name: 'folderName',
						},
					],
				},
			};
			const effectError = gen.next(response).value.PUT.action;
			expect(effectError.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effectError.key).toEqual('add_folder_modal');
			expect(effectError.componentName).toEqual('FolderCreatorModal');
			expect(effectError.componentState).toEqual({ error: 'Folder exists already' });
		});

		it('should not add folder when new name is empty', () => {
			const gen = effects.addFolder();
			expect(gen.next().value.SELECT).toBeDefined();

			const effectError = gen.next('').value.PUT.action;
			expect(effectError.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effectError.key).toEqual('add_folder_modal');
			expect(effectError.componentName).toEqual('FolderCreatorModal');
			expect(effectError.componentState).toEqual({ error: 'Folder name is empty' });
		});

		it('should add folder with success', () => {
			const gen = effects.addFolder();
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next('folderName').value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value.SELECT).toBeDefined();
			const effect = gen.next('folderId').value.CALL;
			expect(effect.fn).toEqual(http.get);
			expect(effect.args[0]).toEqual('/api/folders/folderId/preparations');
			const response = {
				data: {
					folders: [
						{
							name: 'folderName1',
						},
					],
				},
			};
			const effectSuccess = gen.next(response).value.CALL;
			expect(effectSuccess.fn).toEqual(http.put);
			expect(effectSuccess.args[0]).toEqual('/api/folders?parentId=folderId&path=folderName');

			expect(gen.next(API_RESPONSE).value).toEqual(
				call(effects.refreshCurrentFolder),
			);
			expect(gen.next().value.PUT.action.type).toBe('TDP_SUCCESS_NOTIFICATION');
			expect(gen.next().value).toEqual(
				call(effects.closeAddFolderModal),
			);

		});
	});
});
