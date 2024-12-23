import nodeResolve from "@rollup/plugin-node-resolve";
import commonjs from "@rollup/plugin-commonjs";
import copy from "rollup-plugin-copy";
import { importMetaAssets } from "@web/rollup-plugin-import-meta-assets";

const outDir = "../src/main/resources/cloudapim/extensions/biscuit/webcomponents"

export default {
	input: "index.js",
	output: {
		dir: outDir,
		format: "esm",
	},
	plugins: [
		nodeResolve({ browser: true }),
		commonjs({
			include: "node_modules/**",
		}),
		copy({
			targets: [
				{
					src: "node_modules/@biscuit-auth/web-components/dist/assets/*",
					dest: `${outDir}/assets`,
				},
			],
		}),
		importMetaAssets(),
	],
};