export default class SearchProvider {
	static KEY = 'SHOULD_BE_OVERRIDED';

	static build() {
		throw new Error('build(): Not implemented');
	}

	static transform() {
		throw new Error('convert(): Not implemented');
	}
}
