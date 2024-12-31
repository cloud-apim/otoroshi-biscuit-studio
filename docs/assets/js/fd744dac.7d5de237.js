"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[748],{2149:(t,e,i)=>{i.r(e),i.d(e,{assets:()=>c,contentTitle:()=>r,default:()=>l,frontMatter:()=>o,metadata:()=>n,toc:()=>u});const n=JSON.parse('{"id":"entities/attenuators","title":"Attenuators","description":"What biscuit attenuators are ?","source":"@site/docs/entities/attenuators.mdx","sourceDirName":"entities","slug":"/entities/attenuators","permalink":"/otoroshi-biscuit-studio/docs/entities/attenuators","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":3,"frontMatter":{"sidebar_position":3},"sidebar":"tutorialSidebar","previous":{"title":"Verifiers","permalink":"/otoroshi-biscuit-studio/docs/entities/verifiers"},"next":{"title":"Biscuit RBAC Policies","permalink":"/otoroshi-biscuit-studio/docs/entities/rbac_policies"}}');var s=i(74848),a=i(28453);i(89229);const o={sidebar_position:3},r="Attenuators",c={},u=[{value:"What biscuit attenuators are ?",id:"what-biscuit-attenuators-are-",level:2},{value:"Attenuator Example",id:"attenuator-example",level:2},{value:"Create a Biscuit Attenuator from command line",id:"create-a-biscuit-attenuator-from-command-line",level:2}];function d(t){const e={a:"a",code:"code",h1:"h1",h2:"h2",header:"header",img:"img",p:"p",pre:"pre",...(0,a.R)(),...t.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(e.header,{children:(0,s.jsx)(e.h1,{id:"attenuators",children:"Attenuators"})}),"\n",(0,s.jsx)(e.h2,{id:"what-biscuit-attenuators-are-",children:"What biscuit attenuators are ?"}),"\n",(0,s.jsx)(e.p,{children:"One of Biscuit's core strengths is its ability to attenuate tokens by appending blocks with specific checks, effectively restricting their scope of use."}),"\n",(0,s.jsx)(e.p,{children:"This allows developers to tailor token permissions to suit different use cases."}),"\n",(0,s.jsxs)(e.p,{children:["See ",(0,s.jsx)(e.a,{href:"/docs/plugins/attenuators",children:"this article"})," to integrate your attenuator entity to your route's plugins."]}),"\n",(0,s.jsx)(e.p,{children:(0,s.jsx)(e.img,{src:i(86925).A+"",width:"2824",height:"1558"})}),"\n",(0,s.jsx)(e.h2,{id:"attenuator-example",children:"Attenuator Example"}),"\n",(0,s.jsx)(e.pre,{children:(0,s.jsx)(e.code,{className:"language-js",children:'{\n  "enabled": true,\n  "id": "biscuit_attenuator_0a8d24de-426a-4baf-9b53-e9e70f38d935",\n  "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "name": "Biscuit Attenuator",\n  "description": "A Biscuit Attenuator created from Otoroshi API",\n  "tags": [],\n  "config": {\n    "checks": [\n      "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n    ]\n  },\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitAttenuator"\n}\n'})}),"\n",(0,s.jsx)(e.h2,{id:"create-a-biscuit-attenuator-from-command-line",children:"Create a Biscuit Attenuator from command line"}),"\n",(0,s.jsx)(e.pre,{children:(0,s.jsx)(e.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/json\' \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-attenuators\' -u admin-api-apikey-id:admin-api-apikey-secret -d \'{\n  "enabled": true,\n  "id": "biscuit_attenuator_0a8d24de-426a-4baf-9b53-e9e70f38d935",\n  "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "name": "Biscuit Attenuator CURL",\n  "description": "A Biscuit Attenuator created from Otoroshi API",\n  "tags": [],\n  "config": {\n    "checks": [\n      "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n    ]\n  },\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitAttenuator"\n}\'\n'})})]})}function l(t={}){const{wrapper:e}={...(0,a.R)(),...t.components};return e?(0,s.jsx)(e,{...t,children:(0,s.jsx)(d,{...t})}):d(t)}},89229:(t,e,i)=>{i(96540),i(74848)},86925:(t,e,i)=>{i.d(e,{A:()=>n});const n=i.p+"assets/images/biscuit-attenuator-creation-c223f9b0f6bf5a8e4392d557cddc7a69.png"},28453:(t,e,i)=>{i.d(e,{R:()=>o,x:()=>r});var n=i(96540);const s={},a=n.createContext(s);function o(t){const e=n.useContext(a);return n.useMemo((function(){return"function"==typeof t?t(e):{...e,...t}}),[e,t])}function r(t){let e;return e=t.disableParentContext?"function"==typeof t.components?t.components(s):t.components||s:o(t.components),n.createElement(a.Provider,{value:e},t.children)}}}]);