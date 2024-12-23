import {initialize} from '@biscuit-auth/web-components';

async function setup() {
  console.log("SETUP");
  await initialize();
  console.log("DONE");
}

setup();