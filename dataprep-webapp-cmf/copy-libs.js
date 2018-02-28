#!/usr/bin/env node
const path = require('path');
const cpx = require('cpx');
const program = require('commander');

program
	.version('0.0.1')
	.option('-c, --clean', 'remove files that copied on past before')
	.option('-d, --debug', 'display more info')
	.option('-q, --quiet', 'display nothing')
	.option('-s, --scope [scope]', 'only one deps')
	.option('-w, --watch', 'copy and watch to copy again')
	.parse(process.argv);

const command = program.watch ? 'watch' : 'copy';

const libs = {
	'react-components': 'lib',
	'react-containers': 'lib',
	'react-cmf': 'lib',
	'icons': 'dist',
	'bootstrap-theme': 'src',
	'react-forms': 'lib',
};

const srcs = {
	'react-components': 'components',
	'react-containers': 'containers',
	'react-cmf': 'cmf',
	'icons': 'icons',
	'bootstrap-theme': 'theme',
	'react-forms': 'forms',
};

const GIT_PATH = path.join('..', '..', '..', 'ui', 'packages');

Object.keys(srcs).forEach((target) => {
	const src = srcs[target];
	const lib = libs[target];
	const source = path.resolve(
		GIT_PATH,
		src,
		lib,
		'**',
		'*'
	);

	const dest = path.resolve(
		__dirname,
		'node_modules',
		`@talend/${target}`,
		lib
	);

	if (!program.quiet) {
		console.log(`Copy ${source} -> ${dest}`);
	}

	const options = {
		clean: program.clean,
	};
	cpx[command](source, dest, options);
});
