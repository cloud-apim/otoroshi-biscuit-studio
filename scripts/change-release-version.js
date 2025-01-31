const fs = require('fs');

const args = process.argv;
const newVersionIndex = args.indexOf('--version');

if (newVersionIndex === -1 || !args[newVersionIndex + 1]) {
    console.error('❌ Error: --version argument is required.');
    process.exit(1);
}

const NEW_STUDIO_VERSION = args[newVersionIndex + 1];
console.log(`✅ New otoroshi-biscuit-studio version: ${NEW_STUDIO_VERSION}`);

const files = [
  '../documentation/docs/install.mdx',
  '../README.md'
];

async function fetchLatestOtoroshiRelease() {
	const response = await fetch(
		"https://api.github.com/repos/MAIF/otoroshi/releases/latest"
	);

	const latestReleaseResp = await response.json();

  console.log(`✅ Got latest version of otoroshi :  ${latestReleaseResp.name}`);

	return latestReleaseResp.name;
}

async function replaceVersionInFiles(filePaths) {
    // Regex for Otoroshi Biscuit Studio
    const biscuitStudioVersion = /https:\/\/github\.com\/cloud-apim\/otoroshi-biscuit-studio\/releases\/download\/([^\/]+)\/otoroshi-biscuit-studio-\1\.jar/g;
    
    // Regex for Otoroshi Core
    const latestOtoVersion = await fetchLatestOtoroshiRelease()
    const otoroshiVersionRegex =
		/https:\/\/github\.com\/MAIF\/otoroshi\/releases\/download\/[^\/]+\/otoroshi\.jar/g;

    filePaths.forEach(filePath => {
        try {
            // Read the file content
            let content = fs.readFileSync(filePath, 'utf8');

            // Replace all occurrences of both URLs
            let updatedContent = 
            content.replace(biscuitStudioVersion, `https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/download/${NEW_STUDIO_VERSION}/otoroshi-biscuit-studio-${NEW_STUDIO_VERSION}.jar`);


            updatedContent = updatedContent.replace(
              otoroshiVersionRegex,
              `https://github.com/MAIF/otoroshi/releases/download/${latestOtoVersion}/otoroshi.jar`
            );

            // Write back the updated content
            fs.writeFileSync(filePath, updatedContent, 'utf8');

            console.log(`✅ Updated all occurrences in ${filePath}`);
        } catch (error) {
            console.error(`❌ Error processing file ${filePath}:`, error.message);
        }
    });
}

replaceVersionInFiles(files);