import { HTTPError } from '@talend/react-cmf/lib/sagas/http';
import * as effects from '../../effects/preparation.effects';
import { IMMUTABLE_SETTINGS } from './preparation.effects.mock';
import http from '../http';
import { REDIRECT_WINDOW } from '../../../constants/actions';


describe('preparation', () => {
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
});
