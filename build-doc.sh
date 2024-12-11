cd ./documentation
rm -rf ./node-modules
npm install
npm run build
cd ..
rm -rf ./docs
mv ./documentation/build ./docs 
git add --all
git commit -am 'build otoroshi-biscuit-studio documentation website'
git push origin main