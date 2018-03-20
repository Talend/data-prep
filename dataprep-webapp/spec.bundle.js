/*  ============================================================================

 Copyright (C) 2006-2018 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

import 'babel-polyfill';

import './src/vendor';
import './src/app/index-module';

import 'angular-mocks';

const context = require.context('./src/', true, /\.spec\.js/);
context.keys().forEach(context);
