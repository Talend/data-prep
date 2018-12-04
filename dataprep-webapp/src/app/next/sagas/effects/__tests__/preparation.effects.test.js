import { all, call } from 'redux-saga/effects';
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
import { REDIRECT_WINDOW } from '../../../constants/actions';


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

	describe('create', () => {
		it('should open preparation with default folder id', () => {
			const payload = {
				id: 'DATASET_ID',
			};
			const gen = effects.create(payload);

			const effectPUT = gen.next().value.PUT.action;
			expect(effectPUT.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effectPUT.collectionId).toBe('currentFolderId');
			expect(effectPUT.data).toBe(effects.DEFAULT_FOLDER_ID);

			expect(gen.next().value.SELECT).toBeDefined();

			const effectCALL = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effectCALL.fn).toEqual(http.post);
			expect(effectCALL.args[0]).toEqual(
				`/api/preparations?folder=${effects.DEFAULT_FOLDER_ID}`,
			);

			const PREPARATION_ID = 'PREPARATION_ID';
			const effectPUT2 = gen.next({ data: PREPARATION_ID }).value.PUT.action;
			expect(effectPUT2.type).toBe(REDIRECT_WINDOW);
			expect(effectPUT2.payload).toEqual({ url: `/#/playground/preparation?prepid=${PREPARATION_ID}` });

			expect(gen.next().done).toBeTruthy();
		});

		it('should open preparation with custom folder id', () => {
			const CUSTOM_FOLDER_ID = 'FAKE_CUSTOM_FOLDER_ID';
			const payload = {
				id: 'DATASET_ID',
				folderId: CUSTOM_FOLDER_ID,
			};
			const gen = effects.create(payload);

			const effectPUT = gen.next().value.PUT.action;
			expect(effectPUT.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effectPUT.collectionId).toBe('currentFolderId');
			expect(effectPUT.data).toBe(CUSTOM_FOLDER_ID);

			expect(gen.next().value.SELECT).toBeDefined();

			const effectCALL = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effectCALL.fn).toEqual(http.post);
			expect(effectCALL.args[0]).toEqual(
				`/api/preparations?folder=${CUSTOM_FOLDER_ID}`,
			);

			const PREPARATION_ID = 'PREPARATION_ID';
			const effectPUT2 = gen.next({ data: PREPARATION_ID }).value.PUT.action;
			expect(effectPUT2.type).toBe(REDIRECT_WINDOW);
			expect(effectPUT2.payload).toEqual({ url: `/#/playground/preparation?prepid=${PREPARATION_ID}` });

			expect(gen.next().done).toBeTruthy();
		});

		it('should not trying to open preparation if error', () => {
			const error = new HTTPError({
				data: { message: 'err message' },
				response: { statusText: 'err' },
			});
			const CUSTOM_FOLDER_ID = 'FAKE_CUSTOM_FOLDER_ID';
			const payload = {
				id: 'DATASET_ID',
				folderId: CUSTOM_FOLDER_ID,
			};
			const gen = effects.create(payload);

			const effectPUT = gen.next().value.PUT.action;
			expect(effectPUT.type).toBe('REACT_CMF.COLLECTION_ADD_OR_REPLACE');
			expect(effectPUT.collectionId).toBe('currentFolderId');
			expect(effectPUT.data).toBe(CUSTOM_FOLDER_ID);

			expect(gen.next().value.SELECT).toBeDefined();

			const effectCALL = gen.next(IMMUTABLE_SETTINGS).value.CALL;
			expect(effectCALL.fn).toEqual(http.post);
			expect(effectCALL.args[0]).toEqual(
				`/api/preparations?folder=${CUSTOM_FOLDER_ID}`,
			);

			expect(gen.next(error).done).toBeTruthy();
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
			expect(gen.next(IMMUTABLE_SETTINGS).value).toEqual(call(http.get, `/api/folders/${effects.DEFAULT_FOLDER_ID}`));

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
});
