import Immutable from 'immutable';

export const VERSIONS = {
	displayVersion: '7.1.1.M2',
	services: [
		{
			versionId: '2.8.0-SNAPSHOT',
			buildId: '87d0dcd',
			serviceName: 'Dataset',
		},
		{
			versionId: '2.8.0-SNAPSHOT',
			buildId: '87d0dcd',
			serviceName: 'API',
		},
		{
			versionId: '2.8.0-SNAPSHOT',
			buildId: '87d0dcd',
			serviceName: 'Preparation',
		},
		{
			versionId: '2.8.0-SNAPSHOT',
			buildId: '87d0dcd',
			serviceName: 'Transformation',
		},
	],
};

export const IMMUTABLE_VERSIONS = Immutable.fromJS(VERSIONS);

export const SETTINGS = {
	apiVersion: '/api/version',
};

export const IMMUTABLE_SETTINGS = Immutable.fromJS(SETTINGS);
