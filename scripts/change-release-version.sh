#!/bin/sh

if [ $# -eq 0 ] || [ "$1" != "--version" ]; then
    echo "❌ Error: --version argument is required."
    exit 1
fi

npm install
node change-release-version.js "$@"
cd ../documentation
rm -rf ./node-modules
npm install
npm run build
cd ..
rm -rf ./docs
mv ./documentation/build ./docs 
git add ./documentation
git add ./docs
git add README.md
git commit -m 'change-biscuit-studio-release-version'
git push origin main