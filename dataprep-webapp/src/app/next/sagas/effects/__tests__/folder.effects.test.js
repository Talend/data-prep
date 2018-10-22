import { call } from 'redux-saga/effects';
import { Map } from 'immutable';
import * as effects from '../../effects/folder.effects';
import { refreshCurrentFolder } from '../../effects/preparation.effects';
import {
	IMMUTABLE_SETTINGS,
	API_RESPONSE,
} from './preparation.effects.mock';
import http from '../http';


describe('folder', () => {
	describe('add', () => {
		it('should open add folder modal', () => {
			const gen = effects.openAddFolderModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('add:folder:modal');
			expect(effect.componentName).toEqual('FolderCreatorModal');
			expect(effect.componentState).toEqual(
				new Map({
					header: 'ADD_FOLDER_HEADER',
					show: true,
					name: '',
					error: '',
					validateAction: {
						label: 'ADD',
						id: 'folder:add',
						disabled: true,
						bsStyle: 'primary',
						actionCreator: 'folder:add',
					},
					cancelAction: {
						label: 'CANCEL',
						id: 'folder:add:close',
						bsStyle: 'default btn-inverse',
						actionCreator: 'folder:add:close',
					},
				}),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should close add folder modal', () => {
			const gen = effects.closeAddFolderModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('add:folder:modal');
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
			expect(effectError.key).toEqual('add:folder:modal');
			expect(effectError.componentName).toEqual('FolderCreatorModal');
			expect(effectError.componentState).toEqual({ error: 'FOLDER_EXIST_MESSAGE' });
		});

		it('should not add folder when new name is empty', () => {
			const gen = effects.addFolder();
			expect(gen.next().value.SELECT).toBeDefined();

			const effectError = gen.next('').value.PUT.action;
			expect(effectError.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effectError.key).toEqual('add:folder:modal');
			expect(effectError.componentName).toEqual('FolderCreatorModal');
			expect(effectError.componentState).toEqual({ error: 'FOLDER_EMPTY_MESSAGE' });
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
			expect(gen.next(API_RESPONSE).value).toEqual(call(refreshCurrentFolder));
			expect(gen.next().value.PUT.action.type).toBe('TDP_SUCCESS_NOTIFICATION');
			expect(gen.next().value).toEqual(call(effects.closeAddFolderModal));
		});
	});

	describe('remove', () => {
		it('should open remove folder modal', () => {
			const payload = { id: 'folderId', name: 'folderName' };
			const gen = effects.openRemoveFolderModal(payload);
			const effect = gen.next(payload).value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('ConfirmDialog');
			expect(effect.componentName).toEqual('CMFContainer(ConfirmDialog)');
			expect(effect.componentState).toEqual(
				new Map({
					header: 'REMOVE_FOLDER_MODAL_HEADER',
					children: 'REMOVE_FOLDER_MODAL_CONTENT',
					show: true,
					validateAction: 'folder:remove',
					cancelAction: 'folder:remove:close',
					folderId: 'folderId',
					folderName: 'folderName',
				}),
			);

			expect(gen.next().done).toBeTruthy();
		});

		it('should close remove folder modal', () => {
			const gen = effects.closeRemoveFolderModal();
			const effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('ConfirmDialog');
			expect(effect.componentName).toEqual('CMFContainer(ConfirmDialog)');
			expect(effect.componentState).toEqual(new Map({ show: false }));

			expect(gen.next().done).toBeTruthy();
		});

		it('should remove folder', () => {
			const gen = effects.removeFolder();
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next(IMMUTABLE_SETTINGS).value.SELECT).toBeDefined();

			let effect = gen.next('folderId').value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('ConfirmDialog');
			expect(effect.componentName).toEqual('CMFContainer(ConfirmDialog)');
			expect(effect.componentState).toEqual(new Map({ loading: true }));

			effect = gen.next().value.CALL;
			expect(effect.fn).toEqual(http.delete);
			expect(effect.args[0]).toEqual('/api/folders/folderId');
			expect(gen.next(API_RESPONSE).value).toEqual(call(refreshCurrentFolder));
			expect(gen.next().value.SELECT).toBeDefined();
			expect(gen.next().value.PUT.action.type).toBe('TDP_SUCCESS_NOTIFICATION');
			expect(gen.next().value).toEqual(call(effects.closeRemoveFolderModal));

			effect = gen.next().value.PUT.action;
			expect(effect.type).toEqual('REACT_CMF.COMPONENT_MERGE_STATE');
			expect(effect.key).toEqual('ConfirmDialog');
			expect(effect.componentName).toEqual('CMFContainer(ConfirmDialog)');
			expect(effect.componentState).toEqual(new Map({ loading: false }));

			expect(gen.next().done).toBeTruthy();
		});
	});
});
