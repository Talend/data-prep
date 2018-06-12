import SearchService from '../search.service';

jest.mock('../search-providers', () => {
	class PA {
		build(value) {
			return `pa-build-${value}`;
		}
		transform(value) {
			return `pa-transform-${value}`;
		}
	}

	class PB {
		build(value) {
			return `pb-build-${value}`;
		}
		transform(value) {
			return `pb-transform-${value}`;
		}
	}

	return {
		pa: PA,
		pb: PB,
	};
});


describe('SearchService', () => {
	it('should instanciate providers', () => {
		const service = new SearchService({ pa: [], pb: [] });
		expect(Object.keys(service.providers)).toEqual(['pa', 'pb']);
	});

	it('should call the appropriate provider build method', () => {
		const service = new SearchService({ pa: [], pb: [] });
		expect(service.build('pa', 'test')).toBe('pa-build-test');
		expect(service.build('pb', 'test')).toBe('pb-build-test');
	});

	it('should call the appropriate provider transform method', () => {
		const service = new SearchService({ pa: [], pb: [] });
		expect(service.transform('pa', 'test')).toBe('pa-transform-test');
		expect(service.transform('pb', 'test')).toBe('pb-transform-test');
	});
});
