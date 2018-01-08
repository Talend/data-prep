/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
class UpgradeVersionService {
	constructor($http, RestURLs) {
		'ngInject';
		this.$http = $http;
		this.RestURLs = RestURLs;
	}

	retrieveNewVersions() {
		return this.$http.get(this.RestURLs.upgradeVersion).then(response => response.data);
	}
}

export default UpgradeVersionService;
