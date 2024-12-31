"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[511],{43680:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>d,contentTitle:()=>o,default:()=>h,frontMatter:()=>a,metadata:()=>t,toc:()=>l});const t=JSON.parse('{"id":"entities/remote_facts","title":"Remote Facts Loader","description":"The Remote Facts Loader entity facilitates the loading of external facts provided by an API. This entity is particularly useful for dynamic role and permission management based on external data sources.","source":"@site/docs/entities/remote_facts.mdx","sourceDirName":"entities","slug":"/entities/remote_facts","permalink":"/otoroshi-biscuit-studio/docs/entities/remote_facts","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":5,"frontMatter":{"sidebar_position":5},"sidebar":"tutorialSidebar","previous":{"title":"Biscuit RBAC Policies","permalink":"/otoroshi-biscuit-studio/docs/entities/rbac_policies"},"next":{"title":"Plugins","permalink":"/otoroshi-biscuit-studio/docs/category/plugins"}}');var s=i(74848),r=i(28453);i(89229);const a={sidebar_position:5},o="Remote Facts Loader",d={},l=[{value:"Overview",id:"overview",level:2},{value:"API Input Example",id:"api-input-example",level:2},{value:"Key Fields in the JSON:",id:"key-fields-in-the-json",level:3},{value:"Entity Configuration Example",id:"entity-configuration-example",level:2},{value:"Key Fields in the Configuration:",id:"key-fields-in-the-configuration",level:3},{value:"Usage",id:"usage",level:2}];function c(e){const n={code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",hr:"hr",img:"img",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,r.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.header,{children:(0,s.jsx)(n.h1,{id:"remote-facts-loader",children:"Remote Facts Loader"})}),"\n",(0,s.jsxs)(n.p,{children:["The ",(0,s.jsx)(n.strong,{children:"Remote Facts Loader"})," entity facilitates the loading of external facts provided by an API. This entity is particularly useful for dynamic role and permission management based on external data sources."]}),"\n",(0,s.jsx)(n.hr,{}),"\n",(0,s.jsx)(n.h2,{id:"overview",children:"Overview"}),"\n",(0,s.jsx)(n.p,{children:"The Remote Facts Loader connects to an API endpoint to fetch data, such as roles and their associated permissions, and integrates it into your system. This entity is configured with metadata and API details to ensure smooth interaction."}),"\n",(0,s.jsx)(n.hr,{}),"\n",(0,s.jsx)(n.p,{children:(0,s.jsx)(n.img,{src:i(81263).A+"",width:"2824",height:"1568"})}),"\n",(0,s.jsx)(n.p,{children:(0,s.jsx)(n.img,{src:i(50202).A+"",width:"2824",height:"1568"})}),"\n",(0,s.jsx)(n.h2,{id:"api-input-example",children:"API Input Example"}),"\n",(0,s.jsx)(n.p,{children:"Below is an example of the data structure that your API might return. This JSON array defines roles and their associated permissions:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-json",children:'{\n  "roles": [\n    {\n      "admin": ["billing:read", "billing:write", "address:read", "address:write"]\n    },\n    {\n      "accounting": ["billing:read", "billing:write", "address:read"]\n    },\n    {\n      "support": ["address:read", "address:write"]\n    },\n    {\n      "pilot":  ["spaceship:drive", "address:read"]\n    },\n    {\n      "delivery":  ["address:read", "package:load", "package:unload", "package:deliver"]\n    }\n  ]\n}\n'})}),"\n",(0,s.jsx)(n.h3,{id:"key-fields-in-the-json",children:"Key Fields in the JSON:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"roles"}),": An array of objects, each representing a role."]}),"\n",(0,s.jsxs)(n.li,{children:["Each role object contains a key-value pair where the key is the role name (e.g., ",(0,s.jsx)(n.code,{children:"admin"}),") and the value is an array of permissions associated with the role."]}),"\n"]}),"\n",(0,s.jsx)(n.hr,{}),"\n",(0,s.jsx)(n.h2,{id:"entity-configuration-example",children:"Entity Configuration Example"}),"\n",(0,s.jsx)(n.p,{children:"Below is an example configuration for the Remote Facts Loader entity:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-json",children:'{\n  "enabled": true,\n  "id": "biscuit_remote_facts_2ae76425-194d-436b-9977-0cdeb8680fbb",\n  "name": "Remote fact loader example",\n  "description": "",\n  "metadata": {\n    "created_at": "2024-12-23T15:30:01.241+01:00"\n  },\n  "tags": [],\n  "config": {\n    "apiUrl": "http://localhost:3333/api/facts",\n    "headers": {\n      "Accept": "application/json",\n      "Authorization": "Bearer: xxxxx"\n    }\n  },\n  "kind": "BiscuitRemoteFactsLoader",\n  "_loc": {\n    "tenant": "default"\n  }\n}\n'})}),"\n",(0,s.jsx)(n.h3,{id:"key-fields-in-the-configuration",children:"Key Fields in the Configuration:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"enabled"}),": A boolean indicating if the loader is active."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"id"}),": A unique identifier for the entity."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"name"}),": A human-readable name for the loader."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"description"}),": Optional, for documenting the purpose or details of the loader."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"metadata"}),": Contains system-generated metadata such as creation time."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"tags"}),": Optional tags for categorization or searching."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"config"}),":","\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"apiUrl"}),": The endpoint URL where the API data is fetched."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"headers"}),": HTTP headers to include in the request, such as ",(0,s.jsx)(n.code,{children:"Authorization"})," or ",(0,s.jsx)(n.code,{children:"Accept"}),"."]}),"\n"]}),"\n"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"kind"}),": Specifies the type of loader, in this case, ",(0,s.jsx)(n.code,{children:"BiscuitRemoteFactsLoader"}),"."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"_loc"}),": Location or tenant-specific details, useful for multi-tenant applications."]}),"\n"]}),"\n",(0,s.jsx)(n.hr,{}),"\n",(0,s.jsx)(n.h2,{id:"usage",children:"Usage"}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Define the API Endpoint"}),": Ensure the API serving the roles data is operational and adheres to the required JSON format."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Configure the Entity"}),": Update the ",(0,s.jsx)(n.code,{children:"config.apiUrl"})," and ",(0,s.jsx)(n.code,{children:"headers"})," fields with the API endpoint and authentication details."]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Activate the Loader"}),": Set the ",(0,s.jsx)(n.code,{children:"enabled"})," field to ",(0,s.jsx)(n.code,{children:"true"})," to activate the entity."]}),"\n"]})]})}function h(e={}){const{wrapper:n}={...(0,r.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(c,{...e})}):c(e)}},89229:(e,n,i)=>{i(96540),i(74848)},50202:(e,n,i)=>{i.d(n,{A:()=>t});const t=i.p+"assets/images/biscuit-remote-facts-loader-create-entity-configuration-5cf155b475513700f06035f22c135252.png"},81263:(e,n,i)=>{i.d(n,{A:()=>t});const t=i.p+"assets/images/biscuit-remote-facts-loader-create-entity-2e7034440dd8f66814dbbb6a5a90338e.png"},28453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>o});var t=i(96540);const s={},r=t.createContext(s);function a(e){const n=t.useContext(r);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:a(e.components),t.createElement(r.Provider,{value:n},e.children)}}}]);