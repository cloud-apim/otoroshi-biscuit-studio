"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[218],{60221:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>l,contentTitle:()=>a,default:()=>u,frontMatter:()=>r,metadata:()=>i,toc:()=>c});const i=JSON.parse('{"id":"plugins/attenuators","title":"Attenuator plugin","description":"When attenuating a Biscuit token from sources such as headers, cookies, or query parameters, the process begins by extracting and validating the original token.","source":"@site/docs/plugins/attenuators.mdx","sourceDirName":"plugins","slug":"/plugins/attenuators","permalink":"/otoroshi-biscuit-studio/docs/plugins/attenuators","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":2,"frontMatter":{"sidebar_position":2},"sidebar":"tutorialSidebar","previous":{"title":"Verifier plugin","permalink":"/otoroshi-biscuit-studio/docs/plugins/verifiers"},"next":{"title":"Client Credentials plugin","permalink":"/otoroshi-biscuit-studio/docs/plugins/clientcredentials"}}');var o=t(74848),s=t(28453);t(16438);const r={sidebar_position:2},a="Attenuator plugin",l={},c=[{value:"Attenuator Plugin configuration",id:"attenuator-plugin-configuration",level:2},{value:"Explanation of Fields",id:"explanation-of-fields",level:3},{value:"Example",id:"example",level:2},{value:"<strong>Request Before Attenuation</strong>",id:"request-before-attenuation",level:3},{value:"<strong>Original Request (Token in Header)</strong>",id:"original-request-token-in-header",level:4},{value:"<strong>Process of Attenuation</strong>",id:"process-of-attenuation",level:3},{value:"<strong>Request After Attenuation</strong>",id:"request-after-attenuation",level:3},{value:"<strong>Attenuated Request (Token in Query Parameter)</strong>",id:"attenuated-request-token-in-query-parameter",level:4},{value:"<strong>Attenuated Request (Token in Cookie)</strong>",id:"attenuated-request-token-in-cookie",level:4},{value:"<strong>Explanation of Changes</strong>",id:"explanation-of-changes",level:3},{value:"Demo",id:"demo",level:2}];function d(e){const n={code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",header:"header",hr:"hr",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,s.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(n.header,{children:(0,o.jsx)(n.h1,{id:"attenuator-plugin",children:"Attenuator plugin"})}),"\n",(0,o.jsx)(n.p,{children:"When attenuating a Biscuit token from sources such as headers, cookies, or query parameters, the process begins by extracting and validating the original token."}),"\n",(0,o.jsx)(n.p,{children:"After validation, a new block is appended to enforce additional restrictions, such as limiting time, access rights, or endpoints."}),"\n",(0,o.jsx)(n.p,{children:'Once the token is attenuated, the request can be "cleaned" by removing the original token from its initial location.'}),"\n",(0,o.jsx)(n.p,{children:"The newly attenuated token can then be injected into the desired location, whether headers, cookies, or query parameters."}),"\n",(0,o.jsx)(n.p,{children:"This approach ensures secure, context-aware token usage while maintaining flexibility across various layers of your application."}),"\n",(0,o.jsx)(n.h2,{id:"attenuator-plugin-configuration",children:"Attenuator Plugin configuration"}),"\n",(0,o.jsx)(n.p,{children:"Here is a demo configuration :"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-js",children:'  {\n    "ref": "YOUR_BISCUIT_ATTENUATOR_REF",\n    "extractor_type": "header",\n    "extractor_name": "Authorization",\n    "token_replace_loc": "query",\n    "token_replace_name": "auth"\n  }\n'})}),"\n",(0,o.jsx)(n.h3,{id:"explanation-of-fields",children:"Explanation of Fields"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"ref"})}),": Identifier of the attenuator entity. It may looks like ",(0,o.jsx)(n.code,{children:"biscuit-attenuator_1d6e9abd-0c39-41ad-97ef-5f6e666736cc"})]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"extractor_type"})}),": Type of the Token extractor (could be ",(0,o.jsx)(n.code,{children:"header"}),", ",(0,o.jsx)(n.code,{children:"query"})," or ",(0,o.jsx)(n.code,{children:"cookie"}),"). Default type is set to ",(0,o.jsx)(n.code,{children:"header"}),"."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"extractor_name"})}),": The name of the field  where the token will be extracted from."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"token_replace_loc"})}),": The location where the token will be inserted (could be ",(0,o.jsx)(n.code,{children:"header"}),", ",(0,o.jsx)(n.code,{children:"query"})," or ",(0,o.jsx)(n.code,{children:"cookie"}),")"]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:(0,o.jsx)(n.code,{children:"token_replace_name"})}),": The name of the field  where the token will be inserted to."]}),"\n"]}),"\n",(0,o.jsx)(n.h2,{id:"example",children:"Example"}),"\n",(0,o.jsx)(n.h3,{id:"request-before-attenuation",children:(0,o.jsx)(n.strong,{children:"Request Before Attenuation"})}),"\n",(0,o.jsx)(n.h4,{id:"original-request-token-in-header",children:(0,o.jsx)(n.strong,{children:"Original Request (Token in Header)"})}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-http",children:"GET /api/resource HTTP/1.1\nHost: example.com\nAuthorization: Biscuit BISCUIT_TOKEN...originalTokenData\n"})}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h3,{id:"process-of-attenuation",children:(0,o.jsx)(n.strong,{children:"Process of Attenuation"})}),"\n",(0,o.jsxs)(n.ol,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Extract Token"}),": The ",(0,o.jsx)(n.code,{children:"Authorization"})," header contains the original Biscuit token."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Append Restrictions"}),": Add a new block to the token, restricting it (e.g., limit to ",(0,o.jsx)(n.code,{children:"GET /api/resource"})," and expire in 30 minutes)."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Generate New Token"}),": The new attenuated Biscuit token is created."]}),"\n"]}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h3,{id:"request-after-attenuation",children:(0,o.jsx)(n.strong,{children:"Request After Attenuation"})}),"\n",(0,o.jsx)(n.h4,{id:"attenuated-request-token-in-query-parameter",children:(0,o.jsx)(n.strong,{children:"Attenuated Request (Token in Query Parameter)"})}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-http",children:"GET /api/resource?auth=biscuit:NEW_ATTENUATED_BISCUIT_TOKEN...attenuatedTokenData HTTP/1.1\nHost: example.com\n"})}),"\n",(0,o.jsx)(n.p,{children:"Alternatively:"}),"\n",(0,o.jsx)(n.h4,{id:"attenuated-request-token-in-cookie",children:(0,o.jsx)(n.strong,{children:"Attenuated Request (Token in Cookie)"})}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-http",children:"GET /api/resource HTTP/1.1\nHost: example.com\nCookie: auth=biscuit:NEW_ATTENUATED_BISCUIT_TOKEN...attenuatedTokenData\n"})}),"\n",(0,o.jsx)(n.hr,{}),"\n",(0,o.jsx)(n.h3,{id:"explanation-of-changes",children:(0,o.jsx)(n.strong,{children:"Explanation of Changes"})}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Original Location"}),": The token was in the ",(0,o.jsx)(n.code,{children:"Authorization"})," header."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"New Location"}),": The attenuated token was moved to either the query parameter (",(0,o.jsx)(n.code,{children:"auth"}),") or a cookie."]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Token Content"}),": The attenuated token now includes additional constraints, such as time-bound access or endpoint-specific restrictions."]}),"\n"]}),"\n",(0,o.jsx)(n.p,{children:"This flexibility allows secure propagation of tokens while adapting to different application needs."}),"\n",(0,o.jsx)(n.h2,{id:"demo",children:"Demo"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs" \\\n  -d \'{\n  "id" : "biscuit-keypair_dev_d25612c6-b4d0-43ed-a711-16b6c09a5155",\n  "name" : "New Biscuit Key Pair",\n  "description" : "New biscuit KeyPair",\n  "metadata" : { },\n  "pubKey" : "771F9E7FE62784502FE34CE862220586D3DB637D6A5ABAD254F7330369D3B357",\n  "privKey" : "4379BE5B9AFA1A84F59D2417C20020EF1E47E0805945535B45616209D8867E50",\n  "tags" : [ ]\n}\'\n'})}),"\n",(0,o.jsx)(n.p,{children:"then let's create a forge"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges" \\\n  -d \'{\n  "id" : "biscuit-forge_dev_7580094c-47e0-495e-80fc-b9c9e8fb8129",\n  "name" : "New biscuit token",\n  "description" : "New biscuit token",\n  "metadata" : { },\n  "keypair_ref" : "biscuit-keypair_dev_d25612c6-b4d0-43ed-a711-16b6c09a5155",\n  "config" : {\n    "checks" : [ ],\n    "facts" : [ ],\n    "resources" : [ ],\n    "rules" : [ ]\n  },\n  "tags" : [ ],\n  "remoteFactsLoaderRef" : null\n}\'\n'})}),"\n",(0,o.jsxs)(n.p,{children:["and finally let's create a route that uses the ",(0,o.jsx)(n.code,{children:"client_credentials"})," plugin"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/proxy.otoroshi.io/v1/routes" \\\n  -d \'{\n  "_loc" : {\n    "tenant" : "default",\n    "teams" : [ "default" ]\n  },\n  "id" : "4874704c-56a2-4460-9a21-ff8055a19c75",\n  "name" : "test route",\n  "description" : "test route",\n  "tags" : [ ],\n  "metadata" : { },\n  "enabled" : true,\n  "groups" : [ "default" ],\n  "bound_listeners" : [ ],\n  "frontend" : {\n    "domains" : [ "test.oto.tools/token" ],\n    "strip_path" : true,\n    "exact" : false,\n    "headers" : { },\n    "query" : { },\n    "methods" : [ ]\n  },\n  "backend" : {\n    "targets" : [ {\n      "id" : "www.otoroshi.io",\n      "hostname" : "www.otoroshi.io",\n      "port" : 443,\n      "tls" : true,\n      "weight" : 1,\n      "predicate" : {\n        "type" : "AlwaysMatch"\n      },\n      "protocol" : "HTTP/1.1",\n      "ip_address" : null,\n      "tls_config" : {\n        "certs" : [ ],\n        "trusted_certs" : [ ],\n        "enabled" : false,\n        "loose" : false,\n        "trust_all" : false\n      }\n    } ],\n    "root" : "/",\n    "rewrite" : false,\n    "load_balancing" : {\n      "type" : "RoundRobin"\n    }\n  },\n  "backend_ref" : null,\n  "plugins" : [ {\n    "enabled" : true,\n    "debug" : false,\n    "plugin" : "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.ClientCredentialBiscuitTokenEndpoint",\n    "include" : [ ],\n    "exclude" : [ ],\n    "config" : {\n      "expiration" : 21600000,\n      "forge_ref" : "biscuit-forge_dev_7580094c-47e0-495e-80fc-b9c9e8fb8129"\n    },\n    "bound_listeners" : [ ]\n  } ]\n}\'\n'})})]})}function u(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(d,{...e})}):d(e)}},16438:(e,n,t)=>{t(96540),t(74848)},28453:(e,n,t)=>{t.d(n,{R:()=>r,x:()=>a});var i=t(96540);const o={},s=i.createContext(o);function r(e){const n=i.useContext(s);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:r(e.components),i.createElement(s.Provider,{value:n},e.children)}}}]);