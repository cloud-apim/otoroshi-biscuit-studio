"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[843],{6580:(e,t,i)=>{i.r(t),i.d(t,{assets:()=>a,contentTitle:()=>c,default:()=>d,frontMatter:()=>s,metadata:()=>n,toc:()=>u});const n=JSON.parse('{"id":"entities/rbac_policies","title":"Biscuit RBAC Policies","description":"Integrate RBAC to a verifier plugin","source":"@site/docs/entities/rbac_policies.mdx","sourceDirName":"entities","slug":"/entities/rbac_policies","permalink":"/otoroshi-biscuit-studio/docs/entities/rbac_policies","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":4,"frontMatter":{"sidebar_position":4},"sidebar":"tutorialSidebar","previous":{"title":"Attenuators","permalink":"/otoroshi-biscuit-studio/docs/entities/attenuators"},"next":{"title":"Plugins","permalink":"/otoroshi-biscuit-studio/docs/category/plugins"}}');var r=i(4848),o=i(8453);i(9229);const s={sidebar_position:4},c="Biscuit RBAC Policies",a={},u=[{value:"Integrate RBAC to a verifier plugin",id:"integrate-rbac-to-a-verifier-plugin",level:2}];function l(e){const t={code:"code",h1:"h1",h2:"h2",header:"header",p:"p",pre:"pre",...(0,o.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(t.header,{children:(0,r.jsx)(t.h1,{id:"biscuit-rbac-policies",children:"Biscuit RBAC Policies"})}),"\n",(0,r.jsx)(t.h2,{id:"integrate-rbac-to-a-verifier-plugin",children:"Integrate RBAC to a verifier plugin"}),"\n",(0,r.jsx)(t.p,{children:"You could use your RBAC entities into a verifier plugin."}),"\n",(0,r.jsx)(t.pre,{children:(0,r.jsx)(t.code,{className:"language-js",children:'  {\n    "verifier_ref": "YOUR_BISCUIT_VERIFIER_ENTITY_REF",\n    "rbac_ref": "RBAC_POLICY_ENTITY_REF" // optional\n    "enforce": false, // true or false\n    "extractor_type": "header", // header, query or cookies\n    "extractor_name": "Authorization"\n  }\n'})})]})}function d(e={}){const{wrapper:t}={...(0,o.R)(),...e.components};return t?(0,r.jsx)(t,{...e,children:(0,r.jsx)(l,{...e})}):l(e)}},9229:(e,t,i)=>{i(6540),i(4848)},8453:(e,t,i)=>{i.d(t,{R:()=>s,x:()=>c});var n=i(6540);const r={},o=n.createContext(r);function s(e){const t=n.useContext(o);return n.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function c(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:s(e.components),n.createElement(o.Provider,{value:t},e.children)}}}]);