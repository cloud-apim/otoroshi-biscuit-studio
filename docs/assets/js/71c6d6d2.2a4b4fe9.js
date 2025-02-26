"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[616],{76363:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>c,contentTitle:()=>a,default:()=>u,frontMatter:()=>o,metadata:()=>t,toc:()=>l});const t=JSON.parse('{"id":"plugins/biscuit-user-extractor","title":"Biscuit User Extractor plugin","description":"The Biscuit User Extractor plugin enables you to extract user-related information from a Biscuit token and inject it into the request context as an authenticated user.","source":"@site/docs/plugins/biscuit-user-extractor.mdx","sourceDirName":"plugins","slug":"/plugins/biscuit-user-extractor","permalink":"/otoroshi-biscuit-studio/docs/plugins/biscuit-user-extractor","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":5,"frontMatter":{"sidebar_position":5},"sidebar":"tutorialSidebar","previous":{"title":"Exposing Biscuit Public Keys","permalink":"/otoroshi-biscuit-studio/docs/plugins/keypairsexposition"},"next":{"title":"API Documentation","permalink":"/otoroshi-biscuit-studio/docs/api"}}');var r=i(74848),s=i(28453);i(16438);const o={sidebar_position:5},a="Biscuit User Extractor plugin",c={},l=[{value:"Configuration",id:"configuration",level:2},{value:"Configuration Options:",id:"configuration-options",level:3},{value:"Example Use Case",id:"example-use-case",level:2},{value:"User Profile Endpoint Configuration",id:"user-profile-endpoint-configuration",level:2},{value:"User Profile Data Format",id:"user-profile-data-format",level:3}];function d(e){const n={code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,s.R)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(n.header,{children:(0,r.jsx)(n.h1,{id:"biscuit-user-extractor-plugin",children:"Biscuit User Extractor plugin"})}),"\n",(0,r.jsxs)(n.p,{children:["The ",(0,r.jsx)(n.strong,{children:"Biscuit User Extractor"})," plugin enables you to extract user-related information from a Biscuit token and inject it into the request context as an authenticated user."]}),"\n",(0,r.jsx)(n.p,{children:"This allows seamless integration of user authentication and authorization into your application, ensuring secure and streamlined access management."}),"\n",(0,r.jsx)(n.h2,{id:"configuration",children:"Configuration"}),"\n",(0,r.jsx)(n.p,{children:"Here is a sample configuration for the plugin:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-js",children:'{\n  "keypair_ref": "YOUR_KEYPAIR_ID",    // The reference to your keypair ID.\n  "extractor_type": "header",          // Type of extractor: \'header\', \'query\', or \'cookie\'.\n  "extractor_name": "Authorization",   // The name of the extractor, usually the header name (e.g., "Authorization").\n  "enforce": true,                     // Boolean value: Whether to enforce the extraction (true/false).\n  "username_key": "name"               // The key in the Biscuit token that represents the username.\n}\n'})}),"\n",(0,r.jsx)(n.h3,{id:"configuration-options",children:"Configuration Options:"}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.code,{children:"keypair_ref"})}),":"]}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsx)(n.li,{children:"The ID of your keypair used for signing and verifying Biscuit tokens."}),"\n"]}),"\n"]}),"\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.code,{children:"extractor_type"})}),":"]}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["Specifies where to extract the Biscuit token from:","\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:[(0,r.jsx)(n.code,{children:'"header"'}),": Extract from HTTP headers."]}),"\n",(0,r.jsxs)(n.li,{children:[(0,r.jsx)(n.code,{children:'"query"'}),": Extract from URL query parameters."]}),"\n",(0,r.jsxs)(n.li,{children:[(0,r.jsx)(n.code,{children:'"cookie"'}),": Extract from cookies."]}),"\n"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.code,{children:"extractor_name"})}),":"]}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["The name of the field from where the token should be extracted. For example, if you use the ",(0,r.jsx)(n.code,{children:"Authorization"})," header, the value should be ",(0,r.jsx)(n.code,{children:'"Authorization"'}),"."]}),"\n"]}),"\n"]}),"\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.code,{children:"enforce"})}),":"]}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["A boolean value that specifies whether to enforce the extraction of the Biscuit token. If set to ",(0,r.jsx)(n.code,{children:"true"}),", the plugin will ensure the token is present and valid. If set to ",(0,r.jsx)(n.code,{children:"false"}),", the absence or invalid token may be ignored."]}),"\n"]}),"\n"]}),"\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:(0,r.jsx)(n.code,{children:"username_key"})}),":"]}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["The key name in the Biscuit token that holds the username. Typically, this is ",(0,r.jsx)(n.code,{children:'"name"'})," or another identifier used for the user."]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,r.jsx)(n.h2,{id:"example-use-case",children:"Example Use Case"}),"\n",(0,r.jsxs)(n.p,{children:["If you want to extract the ",(0,r.jsx)(n.code,{children:"Authorization"})," header, enforce token verification, and store the user's username under the key ",(0,r.jsx)(n.code,{children:"name"}),", your configuration would look like the example provided."]}),"\n",(0,r.jsx)(n.p,{children:"This configuration ensures that the user's identity (extracted from the Biscuit token) is included in your application's request context, making it easy to access authenticated user details across your services."}),"\n",(0,r.jsx)(n.h2,{id:"user-profile-endpoint-configuration",children:"User Profile Endpoint Configuration"}),"\n",(0,r.jsxs)(n.p,{children:["In addition to extracting user information from the Biscuit token, you can configure a ",(0,r.jsx)(n.strong,{children:"UserProfileEndpoint"})," to retrieve detailed user profile data from your application."]}),"\n",(0,r.jsx)(n.p,{children:"This user profile information can be accessed by configuring the endpoint route and it provides essential details such as the user's name, email, role, and more extracted from Biscuit Token's facts."}),"\n",(0,r.jsx)(n.h3,{id:"user-profile-data-format",children:"User Profile Data Format"}),"\n",(0,r.jsxs)(n.p,{children:["The response from the ",(0,r.jsx)(n.strong,{children:"UserProfileEndpoint"})," typically includes the following data:"]}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-json",children:'{\n  "name": "JohnDoe",  \n  "email": "randomuser123@example.com",  \n  "profile": {\n    "user_id": "john.doe@example.com",\n    "username": "JohnDoe", \n    "role": "guest"\n  },\n  "metadata": {}, \n  "tags": []\n}\n'})})]})}function u(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,r.jsx)(n,{...e,children:(0,r.jsx)(d,{...e})}):d(e)}},16438:(e,n,i)=>{i(96540),i(74848)},28453:(e,n,i)=>{i.d(n,{R:()=>o,x:()=>a});var t=i(96540);const r={},s=t.createContext(r);function o(e){const n=t.useContext(s);return t.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(r):e.components||r:o(e.components),t.createElement(s.Provider,{value:n},e.children)}}}]);