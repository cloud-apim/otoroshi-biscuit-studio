"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[235],{70257:(e,i,s)=>{s.r(i),s.d(i,{assets:()=>a,contentTitle:()=>c,default:()=>u,frontMatter:()=>o,metadata:()=>n,toc:()=>d});const n=JSON.parse('{"id":"overview","title":"Overview","description":"Otoroshi Biscuit Studio","source":"@site/docs/overview.mdx","sourceDirName":".","slug":"/overview","permalink":"/otoroshi-biscuit-studio/docs/overview","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_position":1},"sidebar":"tutorialSidebar","next":{"title":"Introduction","permalink":"/otoroshi-biscuit-studio/docs/introduction"}}');var t=s(74848),r=s(28453);s(16438);const o={sidebar_position:1},c="Overview",a={},d=[{value:"Supported Entities in Otoroshi Biscuit Studio",id:"supported-entities-in-otoroshi-biscuit-studio",level:3},{value:"Supported Plugins in Otoroshi Biscuit Studio",id:"supported-plugins-in-otoroshi-biscuit-studio",level:3}];function l(e){const i={a:"a",br:"br",code:"code",h1:"h1",h3:"h3",header:"header",img:"img",li:"li",p:"p",strong:"strong",ul:"ul",...(0,r.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(i.header,{children:(0,t.jsx)(i.h1,{id:"overview",children:"Overview"})}),"\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.img,{alt:"Otoroshi Biscuit Studio",src:s(80872).A+"",width:"652",height:"613"})}),"\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.a,{href:"https://github.com/cloud-apim/otoroshi-biscuit-studio",children:(0,t.jsx)(i.strong,{children:"Otoroshi Biscuit Studio"})})," is a powerful extension for ",(0,t.jsx)(i.a,{href:"https://maif.github.io/otoroshi/manual/index.html",children:"Otoroshi"}),", designed to integrate and manage ",(0,t.jsx)(i.a,{href:"https://biscuitsec.org",children:"Eclipse Biscuit Tokens"})," seamlessly within your beloved API Gateway."]}),"\n",(0,t.jsxs)(i.p,{children:["Biscuit tokens offer a cutting-edge approach to secure and efficient access control. By combining advanced cryptographic techniques with a compact, extensible format, ",(0,t.jsx)(i.strong,{children:"Eclipse Biscuit tokens"})," empower developers to create robust, scalable security solutions."]}),"\n",(0,t.jsx)(i.p,{children:"Their versatility and unique features make them an ideal choice for modern token-based authentication and authorization systems, enabling fine-grained control over user access and permissions."}),"\n",(0,t.jsx)(i.h3,{id:"supported-entities-in-otoroshi-biscuit-studio",children:"Supported Entities in Otoroshi Biscuit Studio"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/keypairs",children:"KeyPairs"})}),(0,t.jsx)(i.br,{}),"\n","Create Biscuit Keypairs to forge, attenuate and verify tokens."]}),"\n",(0,t.jsx)(i.p,{children:"Keypairs are essential for signing and verifying tokens, ensuring the integrity and authenticity of requests."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/forges",children:"Forges"})}),(0,t.jsx)(i.br,{}),"\n","Define a Forge to generate some tokens based on the facts and rules you provided in the forge configuration."]}),"\n",(0,t.jsx)(i.p,{children:"It's kind a template to generate some tokens with given data."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/verifiers",children:"Verifiers"})}),(0,t.jsx)(i.br,{}),"\n","Manage and configure verifiers that check the validity of incoming Eclipse Biscuit tokens against defined rules and policies, ensuring proper authorization and security."]}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/attenuators",children:"Attenuators"})}),(0,t.jsx)(i.br,{}),"\n",'Configure attenuators to modify and return Biscuit tokens that have been "attenuated" (limited in scope or permissions), ensuring fine-grained control over access levels in your API routes.']}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/rbac",children:"RBAC Policies"})}),(0,t.jsx)(i.br,{}),"\n","Implement Role-Based Access Control (RBAC) policies using Eclipse Biscuit tokens to enforce structured, flexible access control mechanisms within your application. This allows for secure, role-based user management."]}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/entities/remotefacts",children:"Remote Facts Loader"})}),(0,t.jsx)(i.br,{}),"\n","Integrate external data sources (remote facts) to enhance the authorization decisions made by tokens, allowing dynamic and context-aware access control."]}),"\n"]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"supported-plugins-in-otoroshi-biscuit-studio",children:"Supported Plugins in Otoroshi Biscuit Studio"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/verifiers",children:"Verifier"})}),(0,t.jsx)(i.br,{}),"\n","Integrate verifiers plugins into your Otoroshi routes to check the validity of a provided token."]}),"\n",(0,t.jsx)(i.p,{children:"This ensures that only authorized tokens are accessing to the route, providing additional layers of security and control over your API traffic."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:[(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/attenuators",children:"Attenuator"})}),(0,t.jsx)(i.br,{}),"\n","Add attenuator plugins to your Otoroshi routes that apply attenuation to a token, allowing you to reduce or modify the scope of access granted by a token."]}),"\n",(0,t.jsx)(i.p,{children:"This can be used to tailor access permissions dynamically based on the specific needs of your routes or services."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/clientcredentials",children:"Client Credentials"})})}),"\n",(0,t.jsxs)(i.p,{children:["The Client Credentials Plugin is a ",(0,t.jsx)(i.code,{children:"Backend"})," plugin that enables the OAuth2 ",(0,t.jsx)(i.code,{children:"client_credentials"})," flow, using an ",(0,t.jsx)(i.a,{href:"/docs/introduction",children:"Eclipse Biscuit Token"})," as the ",(0,t.jsx)(i.code,{children:"access_token"}),"."]}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/biscuit-user-extractor",children:"Biscuit User Extractor"})})}),"\n",(0,t.jsx)(i.p,{children:"The Biscuit User Extractor plugin allows extracting user information from an Eclipse Biscuit token and passing it along with the request to backend services.This helps identify users and enforce user-specific policies without additional authentication mechanisms."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/user-to-biscuit",children:"User to Biscuit Extractor"})})}),"\n",(0,t.jsx)(i.p,{children:"This plugin will allow you to forge an Eclipse Biscuit Token using the authenticated user from the request context. The token will be added into headers."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/apikeybridge",children:"ApiKey Bridge"})})}),"\n",(0,t.jsx)(i.p,{children:"The Biscuit API Key Bridge Plugin will extract an API key from the request."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.strong,{children:(0,t.jsx)(i.a,{href:"/docs/plugins/keypairsexposition",children:"Public Keys exposition"})})}),"\n",(0,t.jsxs)(i.p,{children:["Expose your public keys through a dedicated route.\nDefault route will be ",(0,t.jsx)(i.code,{children:"${YOUR_OTOROSHI_DOMAIN}/.well-known/biscuit-web-keys"})]}),"\n"]}),"\n"]})]})}function u(e={}){const{wrapper:i}={...(0,r.R)(),...e.components};return i?(0,t.jsx)(i,{...e,children:(0,t.jsx)(l,{...e})}):l(e)}},16438:(e,i,s)=>{s(96540),s(74848)},80872:(e,i,s)=>{s.d(i,{A:()=>n});const n=s.p+"assets/images/undraw_features-overview_uone-56ac9a0423f3d2ac06bc3cdbc60908e0.svg"},28453:(e,i,s)=>{s.d(i,{R:()=>o,x:()=>c});var n=s(96540);const t={},r=n.createContext(t);function o(e){const i=n.useContext(r);return n.useMemo((function(){return"function"==typeof e?e(i):{...i,...e}}),[i,e])}function c(e){let i;return i=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:o(e.components),n.createElement(r.Provider,{value:i},e.children)}}}]);