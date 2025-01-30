node change-release-version.js
cd ../documentation
rm -rf ./node-modules
npm install
npm run build
cd ..
rm -rf ../docs
mv ./documentation/build ./docs 
git add ./documentation
git add ./docs
git add README.md
git commit -m 'change-biscuit-studio-release-version'
git push origin main