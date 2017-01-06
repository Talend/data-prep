/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

const FORMS_MOCK = {
	datastoreForm: {
		jsonSchema: {
			title: 'Database data store',
			type: 'object',
			properties: {
				dbTypes: {
					title: 'Database type',
					type: 'string',
					enum: ['MYSQL', 'DERBY'],
				},
				jdbcUrl: {
					title: 'JDBC URL',
					type: 'string',
				},
				userId: {
					title: 'Username',
					type: 'string',
				},
				password: {
					title: 'Password',
					type: 'string',
				},
				tdp_name: {
					title: 'Dataset name',
					type: 'string',
				},
			},
			required: ['dbTypes', 'jdbcUrl', 'userId', 'password', 'tdp_name'],
		},
		properties: {
			tdp_name: (new Date()).toString(),
			dbTypes: 'MYSQL',
			jdbcUrl: 'jdbc:mysql://localhost:3306/database_name',
			userId: 'username',
			password: 'password',
			'@definitionName': 'JDBCDatastore',
		},
		uiSchema: {
			dbTypes: {
				'ui:trigger': ['after'],
			},
			password: {
				'ui:widget': 'password',
			},
			'ui:order': ['tdp_name', 'dbTypes', 'jdbcUrl', 'userId', 'password'],
		},
	},
	datasetForm: {
		jsonSchema: {
			title: 'Database data set',
			type: 'object',
			properties: {
				sourceType: {
					title: 'Source type',
					type: 'string',
					enumNames: ['Table name', 'Query'],
					enum: ['TABLE_NAME', 'QUERY'],
				},
				tableName: {
					title: 'Table name',
					type: 'string',
				},
				sql: {
					title: 'Query',
					type: 'string',
				},
				datastore: {
					title: 'properties.datastore.displayName',
					type: 'string',
				},
				main: {
					title: 'Database data set',
					type: 'object',
					properties: {
						schema: {
							title: 'Schema',
							type: 'string',
						},
					},
				},
			},
		},
		properties: {
			sourceType: 'QUERY',
			sql: 'select * from table_name',
			datastore: 'JDBCDatastore',
			main: {
				schema: '{"type":"record","name":"EmptyRecord","fields":[]}',
			},
			'@definitionName': 'JDBCDataset',
		},
		uiSchema: {
			'ui:order': ['sql', 'sourceType', 'tableName', 'datastore', 'main'],
			sourceType: {
				'ui:widget': 'hidden',
			},
			tableName: {
				'ui:widget': 'hidden',
			},
			datastore: {
				'ui:widget': 'hidden',
			},
			main: {
				schema: {
					'ui:widget': 'hidden',
				},
			},
		},
	},
};

describe('Dataset Import TCOMP component', () => {
	let scope;
	let createElement;
	let element;
	let stateMock;

	beforeEach(angular.mock.module('pascalprecht.translate', ($translateProvider) => {
		$translateProvider.translations('en', {});
		$translateProvider.preferredLanguage('en');
	}));

	beforeEach(angular.mock.module('data-prep.dataset-import', ($provide) => {
		stateMock = {};
		$provide.constant('state', stateMock);
	}));

	beforeEach(inject(($rootScope, $compile) => {
		scope = $rootScope.$new();
		createElement = () => {
			element = angular.element('<tcomp-dataset-import></tcomp-dataset-import>');
			$compile(element)(scope);
			scope.$digest();
		};
	}));

	afterEach(() => {
		scope.$destroy();
		element.remove();
	});
});
