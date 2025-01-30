const fs = require('fs');

const oldBiscuitStudioVersion = '0.0.3';
const newBiscuitStudioVersion = '0.0.3';
const oldOtoroshiVersion = '16.22.0';
const newOtoroshiVersion = '16.22.0';

const files = [
  '../documentation/docs/install.mdx',
  '../README.md'
];

function replaceVersionInFiles(filePaths, oldVersion, newVersion, oldOtoroshiVersion, newOtoroshiVersion) {
    // Regex for Otoroshi Biscuit Studio
    const biscuitRegex = new RegExp(`(https://github\\.com/cloud-apim/otoroshi-biscuit-studio/releases/download/)${oldVersion}(\\/otoroshi-biscuit-studio-)${oldVersion}(\\.jar)`, 'g');
    
    // Regex for Otoroshi Core
    const otoroshiRegex = new RegExp(`(https://github\\.com/MAIF/otoroshi/releases/download/v)${oldOtoroshiVersion}(\\/otoroshi\\.jar)`, 'g');

    filePaths.forEach(filePath => {
        try {
            // Read the file content
            let content = fs.readFileSync(filePath, 'utf8');

            // Replace all occurrences of both URLs
            let updatedContent = content.replace(biscuitRegex, `$1${newVersion}$2${newVersion}$3`);
            updatedContent = updatedContent.replace(otoroshiRegex, `$1${newOtoroshiVersion}$2`);

            // Write back the updated content
            fs.writeFileSync(filePath, updatedContent, 'utf8');

            console.log(`✅ Updated all occurrences in ${filePath}`);
        } catch (error) {
            console.error(`❌ Error processing file ${filePath}:`, error.message);
        }
    });
}

replaceVersionInFiles(files, oldBiscuitStudioVersion, newBiscuitStudioVersion, oldOtoroshiVersion, newOtoroshiVersion);