"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[420],{7423:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>a,contentTitle:()=>c,default:()=>l,frontMatter:()=>r,metadata:()=>t,toc:()=>u});const t=JSON.parse('{"id":"tokens_forge","title":"Biscuit Tokens Forge","description":"Prerequisites","source":"@site/docs/tokens_forge.mdx","sourceDirName":".","slug":"/tokens_forge","permalink":"/otoroshi-biscuit-studio/docs/tokens_forge","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":5,"frontMatter":{"sidebar_position":5},"sidebar":"tutorialSidebar","previous":{"title":"Attenuator plugin","permalink":"/otoroshi-biscuit-studio/docs/plugins/attenuators"},"next":{"title":"API Routes for entities","permalink":"/otoroshi-biscuit-studio/docs/api/routes"}}');var s=i(4848),o=i(8453);i(9229);const r={sidebar_position:5},c="Biscuit Tokens Forge",a={},u=[{value:"Prerequisites",id:"prerequisites",level:2},{value:"Generate a Biscuit Token from command line (using the Forge entity)",id:"generate-a-biscuit-token-from-command-line-using-the-forge-entity",level:2}];function d(e){const n={a:"a",code:"code",h1:"h1",h2:"h2",header:"header",li:"li",p:"p",pre:"pre",ul:"ul",...(0,o.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.header,{children:(0,s.jsx)(n.h1,{id:"biscuit-tokens-forge",children:"Biscuit Tokens Forge"})}),"\n",(0,s.jsx)(n.h2,{id:"prerequisites",children:"Prerequisites"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:["\n",(0,s.jsxs)(n.p,{children:["A ",(0,s.jsx)(n.a,{href:"/docs/entities/keypairs",children:"Biscuit KeyPair"})]}),"\n"]}),"\n",(0,s.jsxs)(n.li,{children:["\n",(0,s.jsx)(n.p,{children:"A datalog :"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:"An array of biscuit Facts (could be empty)"}),"\n",(0,s.jsx)(n.li,{children:"An array of biscuit Checks (could be empty)"}),"\n",(0,s.jsx)(n.li,{children:"An array of biscuit Resources (could be empty)"}),"\n",(0,s.jsx)(n.li,{children:"An array of biscuit Rules (could be empty)"}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,s.jsx)(n.h1,{id:"example--a-generated-token-with-the-biscuit-forge",children:"Example : a Generated token with the Biscuit Forge"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-javascript",children:'{\n  "id": "biscuit_token_0531505a-ae44-4022-983d-9b035786f55a",\n  "name": "Biscuit Token Name",\n  "description": "A simple Biscuit Token",\n  "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "config": {\n    "checks": [\n      "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n    ],\n    "facts": [\n      "user(\\"demo\\")",\n      "role(\\"dev\\")",\n      "time(2024-12-18T20:00:00Z)"\n    ],\n    "resources": [],\n    "rules": []\n  },\n  "token": "GENERATED_TOKEN_HERE",\n  "tags": [],\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitTokenForge"\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"generate-a-biscuit-token-from-command-line-using-the-forge-entity",children:"Generate a Biscuit Token from command line (using the Forge entity)"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/json\' \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/tokens-forge\' -u admin-api-apikey-id:admin-api-apikey-secret -d \'{\n  "id": "biscuit_token_0531505a-ae44-4022-983d-9b035786f55a",\n  "name": "Biscuit Token Name",\n  "description": "A simple Biscuit Token",\n  "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "config": {\n    "checks": [\n      "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n    ],\n    "facts": [\n      "user(\\"demo\\")",\n      "role(\\"dev\\")",\n      "time(2024-12-18T20:00:00Z)"\n    ],\n    "resources": [],\n    "rules": []\n  },\n  "token": "GENERATED_TOKEN_HERE",\n  "tags": [],\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitTokenForge"\n}\'\n'})})]})}function l(e={}){const{wrapper:n}={...(0,o.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(d,{...e})}):d(e)}},9229:(e,n,i)=>{i(6540),i(4848)},8453:(e,n,i)=>{i.d(n,{R:()=>r,x:()=>c});var t=i(6540);const s={},o=t.createContext(s);function r(e){const n=t.useContext(o);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function c(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:r(e.components),t.createElement(o.Provider,{value:n},e.children)}}}]);