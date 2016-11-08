/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/
const AppLayoutContainer = {
	template: `<div class="app">
					<div class="header">
						<app-header-bar><app-header-bar/>
					</div>
					<div class="content">
						<div class="sidemenu">
							<side-panel><side-panel/>
						</div>
						<div class="main"></div>
					</div>
				</div>
	`,
};

export default AppLayoutContainer;
