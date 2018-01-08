/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
class UpgradeVersionCtrl {
	constructor(UpgradeVersionService) {
		'ngInject';
		this.UpgradeVersionService = UpgradeVersionService;
	}

    /**
     * @ngdoc method
     * @name $onInit
     * @methodOf data-prep.upgrade-version.controller:UpgradeVersionCtrl
     * @description Contact the back end service and checks whether new version is available or not.
     **/
	$onInit() {
		this.UpgradeVersionService.retrieveNewVersions()
            .then((data) => {
	this.visible = data.length;
	this.newVersion = data.pop();
});
	}

	close() {
		this.visible = false;
	}

}

export default UpgradeVersionCtrl;
