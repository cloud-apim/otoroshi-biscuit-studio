"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[287],{83023:(e,i,s)=>{s.r(i),s.d(i,{assets:()=>c,contentTitle:()=>o,default:()=>h,frontMatter:()=>d,metadata:()=>n,toc:()=>l});const n=JSON.parse('{"id":"plugins/verifiers","title":"Verifier plugin","description":"Integrate verifiers plugins into your Otoroshi routes to check the validity of Biscuit tokens and add your own rules to verify those tokens.","source":"@site/docs/plugins/verifiers.mdx","sourceDirName":"plugins","slug":"/plugins/verifiers","permalink":"/otoroshi-biscuit-studio/docs/plugins/verifiers","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_position":1},"sidebar":"tutorialSidebar","previous":{"title":"Plugins","permalink":"/otoroshi-biscuit-studio/docs/category/plugins"},"next":{"title":"Attenuator plugin","permalink":"/otoroshi-biscuit-studio/docs/plugins/attenuators"}}');var t=s(74848),r=s(28453);s(16438);const d={sidebar_position:1},o="Verifier plugin",c={},l=[{value:"<strong>Example Request Before Using a Biscuit Verifier</strong>",id:"example-request-before-using-a-biscuit-verifier",level:3},{value:"<strong>Original Request (Token in Header)</strong>",id:"original-request-token-in-header",level:4},{value:"<strong>Using a Biscuit Verifier</strong>",id:"using-a-biscuit-verifier",level:3},{value:"Step 1 : Create the Otoroshi Route",id:"step-1--create-the-otoroshi-route",level:3},{value:"Step 2 : Select the Biscuit verifier plugin",id:"step-2--select-the-biscuit-verifier-plugin",level:3},{value:"Step 3 : Add the plugin to your Route",id:"step-3--add-the-plugin-to-your-route",level:3},{value:"Step 4 : Configure the Plugin",id:"step-4--configure-the-plugin",level:3},{value:"Example : Verifier Plugin configuration",id:"example--verifier-plugin-configuration",level:2},{value:"Fields explanation :",id:"fields-explanation-",level:3},{value:"Common Default Facts",id:"common-default-facts",level:2},{value:"1. Resource Information",id:"1-resource-information",level:3},{value:"2. Request Metadata",id:"2-request-metadata",level:3},{value:"3. User Context",id:"3-user-context",level:3},{value:"4. Temporal Information",id:"4-temporal-information",level:3},{value:"5. Route Identifiers",id:"5-route-identifiers",level:3},{value:"Default Facts Table",id:"default-facts-table",level:2},{value:"Explanation of Key Facts for Biscuit Verifier",id:"explanation-of-key-facts-for-biscuit-verifier",level:2},{value:"<code>hostname</code>",id:"hostname",level:3},{value:"<code>req_path</code>",id:"req_path",level:3},{value:"<code>req_domain</code>",id:"req_domain",level:3},{value:"<code>req_method</code>",id:"req_method",level:3},{value:"<code>route_id</code>",id:"route_id",level:3},{value:"<code>ip_address</code>",id:"ip_address",level:3},{value:"<code>user_name</code> (if present)",id:"user_name-if-present",level:3},{value:"<code>user_email</code> (if present)",id:"user_email-if-present",level:3},{value:"<code>user_tag</code> (if present)",id:"user_tag-if-present",level:3},{value:"<code>user_metadata</code> (if present)",id:"user_metadata-if-present",level:3},{value:"<code>apikey_client_id</code> (if present)",id:"apikey_client_id-if-present",level:3},{value:"<code>apikey_client_name</code> (if present)",id:"apikey_client_name-if-present",level:3},{value:"<code>apikey_tag</code> (if present)",id:"apikey_tag-if-present",level:3},{value:"<code>apikey_metadata</code> (if present)",id:"apikey_metadata-if-present",level:3},{value:"<code>req_headers</code>",id:"req_headers",level:3},{value:"Demo",id:"demo",level:2}];function a(e){const i={code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",header:"header",hr:"hr",img:"img",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",ul:"ul",...(0,r.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(i.header,{children:(0,t.jsx)(i.h1,{id:"verifier-plugin",children:"Verifier plugin"})}),"\n",(0,t.jsx)(i.p,{children:"Integrate verifiers plugins into your Otoroshi routes to check the validity of Biscuit tokens and add your own rules to verify those tokens."}),"\n",(0,t.jsx)(i.p,{children:"This ensures that only authorized requests are processed, providing additional layers of security and control over your API traffic."}),"\n",(0,t.jsx)(i.h3,{id:"example-request-before-using-a-biscuit-verifier",children:(0,t.jsx)(i.strong,{children:"Example Request Before Using a Biscuit Verifier"})}),"\n",(0,t.jsx)(i.h4,{id:"original-request-token-in-header",children:(0,t.jsx)(i.strong,{children:"Original Request (Token in Header)"})}),"\n",(0,t.jsx)(i.pre,{children:(0,t.jsx)(i.code,{className:"language-http",children:"GET /api/resource HTTP/1.1\nHost: example.com\nAuthorization: Biscuit BISCUIT_TOKEN...originalTokenData\n"})}),"\n",(0,t.jsx)(i.hr,{}),"\n",(0,t.jsx)(i.h3,{id:"using-a-biscuit-verifier",children:(0,t.jsx)(i.strong,{children:"Using a Biscuit Verifier"})}),"\n",(0,t.jsxs)(i.ol,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Extract Token"}),": The ",(0,t.jsx)(i.code,{children:"Authorization"})," header contains the Biscuit token."]}),"\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Create a Verifier"}),": Instantiate a Biscuit verifier and attach specific constraints (e.g., check user roles, request path, or expiration)."]}),"\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Validate the Token"}),": Use the verifier to validate the token against the attached constraints. If valid, proceed; otherwise, deny the request."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"step-1--create-the-otoroshi-route",children:"Step 1 : Create the Otoroshi Route"}),"\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.img,{src:s(63956).A+"",width:"2824",height:"1568"})}),"\n",(0,t.jsx)(i.h3,{id:"step-2--select-the-biscuit-verifier-plugin",children:"Step 2 : Select the Biscuit verifier plugin"}),"\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.img,{src:s(3024).A+"",width:"2824",height:"1568"})}),"\n",(0,t.jsx)(i.h3,{id:"step-3--add-the-plugin-to-your-route",children:"Step 3 : Add the plugin to your Route"}),"\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.img,{src:s(51817).A+"",width:"2824",height:"1568"})}),"\n",(0,t.jsx)(i.h3,{id:"step-4--configure-the-plugin",children:"Step 4 : Configure the Plugin"}),"\n",(0,t.jsx)(i.p,{children:(0,t.jsx)(i.img,{src:s(24391).A+"",width:"2824",height:"1568"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsx)(i.p,{children:"Select a Biscuit Verifier entity you would like to use."}),"\n"]}),"\n",(0,t.jsxs)(i.li,{children:["\n",(0,t.jsxs)(i.p,{children:["Finally, choose the name of the extractor (Example : could be ",(0,t.jsx)(i.code,{children:"Authorization"})," for headers)"]}),"\n"]}),"\n"]}),"\n",(0,t.jsx)(i.h2,{id:"example--verifier-plugin-configuration",children:"Example : Verifier Plugin configuration"}),"\n",(0,t.jsx)(i.p,{children:"Here is a demo configuration :"}),"\n",(0,t.jsx)(i.pre,{children:(0,t.jsx)(i.code,{className:"language-js",children:'{\n  "verifier_refs": [], \n  "enforce": true\n}\n'})}),"\n",(0,t.jsx)(i.h3,{id:"fields-explanation-",children:"Fields explanation :"}),"\n",(0,t.jsxs)(i.table,{children:[(0,t.jsx)(i.thead,{children:(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.th,{children:"Item"}),(0,t.jsx)(i.th,{children:"Type"}),(0,t.jsx)(i.th,{children:"Default value"}),(0,t.jsx)(i.th,{children:"Explanation"})]})}),(0,t.jsxs)(i.tbody,{children:[(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"verifier_refs"})}),(0,t.jsx)(i.td,{children:"Array"}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"[]"})}),(0,t.jsx)(i.td,{children:"A list a of verifier entities"})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"enforce"})}),(0,t.jsx)(i.td,{children:"Boolean"}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"true"})}),(0,t.jsx)(i.td,{children:"If enabled and neither verifier could verify rightly the token, then should return forbidden."})]})]})]}),"\n",(0,t.jsx)(i.h1,{id:"biscuit-verifier-plugin-default-facts",children:"Biscuit Verifier Plugin Default Facts"}),"\n",(0,t.jsx)(i.p,{children:"In the context of a Biscuit Verifier, the following data points are typically used to verify requests, ensuring proper authorization and access control. The facts are provided by the authorizer and give insight into the request, the user, and the environment to assess the legitimacy of the request."}),"\n",(0,t.jsx)(i.h2,{id:"common-default-facts",children:"Common Default Facts"}),"\n",(0,t.jsx)(i.h3,{id:"1-resource-information",children:"1. Resource Information"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Purpose"}),": Identifies the resource being accessed, such as a service, domain, or a specific API endpoint. This helps the verifier determine whether the request is targeting an authorized resource."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"2-request-metadata",children:"2. Request Metadata"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Purpose"}),": Contains details like the HTTP method, requested path, and the domain name from which the request originated. This is crucial for ensuring the right HTTP request is processed according to rules."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"3-user-context",children:"3. User Context"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Purpose"}),": Identifies the user or entity making the request, including details like their assigned permissions. It helps verify if the requester has the proper authorization to access the requested resource."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"4-temporal-information",children:"4. Temporal Information"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Purpose"}),": Captures timestamps related to the request. This is helpful for enforcing time-based access control policies, ensuring that requests are within a valid timeframe."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"5-route-identifiers",children:"5. Route Identifiers"}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Purpose"}),": Associates requests with unique route descriptors for more granular access control. This enables detailed access control based on the specific path or service the request is targeting."]}),"\n"]}),"\n",(0,t.jsx)(i.h2,{id:"default-facts-table",children:"Default Facts Table"}),"\n",(0,t.jsxs)(i.table,{children:[(0,t.jsx)(i.thead,{children:(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.th,{children:"Fact Name"}),(0,t.jsx)(i.th,{children:"Definition"}),(0,t.jsx)(i.th,{children:"Example"})]})}),(0,t.jsxs)(i.tbody,{children:[(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"hostname"})}),(0,t.jsx)(i.td,{children:"The hostname of the server processing the request."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'hostname("server-123.example.com")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"req_path"})}),(0,t.jsx)(i.td,{children:"The path being requested."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'req_path("/documents")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"req_domain"})}),(0,t.jsx)(i.td,{children:"The domain name of the requested service."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'req_domain("api.example.com")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"req_method"})}),(0,t.jsx)(i.td,{children:"The HTTP method used in the request."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'req_method("post")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"route_id"})}),(0,t.jsx)(i.td,{children:"The unique identifier of the route descriptor."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'route_id("route_518e34e7a-e2cb-4687-952c-37a3d98b8001")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"ip_address"})}),(0,t.jsx)(i.td,{children:"The IP address of the requester."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'ip_address("192.168.1.1")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"user_name"})," (if present)"]}),(0,t.jsx)(i.td,{children:"The authenticated username of the requester."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'user_name("alice")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"user_email"})," (if present)"]}),(0,t.jsx)(i.td,{children:"The authenticated email of the requester."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'user_email("alice@example.com")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"user_tag"})," (if present)"]}),(0,t.jsx)(i.td,{children:"The user's associated tag for additional categorization."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'user_tag("premium_user")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"user_metadata"})," (if present)"]}),(0,t.jsx)(i.td,{children:"Additional metadata associated with the user."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'user_metadata({"role": "admin", "department": "HR"})'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"apikey_client_id"})," (if present)"]}),(0,t.jsx)(i.td,{children:"The unique identifier of the API key's client."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'apikey_client_id("client_1234")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"apikey_client_name"})," (if present)"]}),(0,t.jsx)(i.td,{children:"The name of the client associated with the API key."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'apikey_client_name("ClientOne")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"apikey_tag"})," (if present)"]}),(0,t.jsx)(i.td,{children:"A tag associated with the API key for categorization."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'apikey_tag("v1_api_key")'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:"apikey_metadata"})," (if present)"]}),(0,t.jsx)(i.td,{children:"Additional metadata associated with the API key."}),(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:'apikey_metadata({"scope": "read", "region": "US"})'})})]}),(0,t.jsxs)(i.tr,{children:[(0,t.jsx)(i.td,{children:(0,t.jsx)(i.code,{children:"req_headers"})}),(0,t.jsx)(i.td,{children:"The HTTP headers sent with the request, including various key details like authorization tokens, user-agent, etc."}),(0,t.jsxs)(i.td,{children:[(0,t.jsx)(i.code,{children:'req_headers("custom-header", "<value>")'}),(0,t.jsx)("br",{}),(0,t.jsx)(i.code,{children:'req_headers("remote-address", "192.168.1.1:56246")'}),(0,t.jsx)("br",{}),(0,t.jsx)(i.code,{children:'req_headers("authorization", "Bearer <token>")'}),(0,t.jsx)("br",{}),(0,t.jsx)(i.code,{children:'req_headers("request-start-time", "<timestamp>")'}),(0,t.jsx)("br",{}),(0,t.jsx)(i.code,{children:'req_headers("accept-encoding", "gzip, deflate")'})]})]})]})]}),"\n",(0,t.jsx)(i.h2,{id:"explanation-of-key-facts-for-biscuit-verifier",children:"Explanation of Key Facts for Biscuit Verifier"}),"\n",(0,t.jsx)(i.h3,{id:"hostname",children:(0,t.jsx)(i.code,{children:"hostname"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": This represents the specific server handling the request. By using the hostname, the verifier can ensure the request is being processed by the correct server within the authorized infrastructure."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"req_path",children:(0,t.jsx)(i.code,{children:"req_path"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": This is the path the request is targeting. It is essential to verify that the request is trying to access a valid and permitted path, based on the verifier's rules."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"req_domain",children:(0,t.jsx)(i.code,{children:"req_domain"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The domain helps verify if the request is targeting the right service. The verifier checks the domain to ensure the request is aligned with the expected API or service domain."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"req_method",children:(0,t.jsx)(i.code,{children:"req_method"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The HTTP method (GET, POST, PUT, DELETE, etc.) tells the verifier what kind of operation is being requested. Some methods may be restricted based on the user's permissions or resource settings."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"route_id",children:(0,t.jsx)(i.code,{children:"route_id"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": A unique identifier for the route that can be associated with specific access control rules. By using ",(0,t.jsx)(i.code,{children:"route_id"}),", the verifier can enforce policies that are specific to a particular route or service."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"ip_address",children:(0,t.jsx)(i.code,{children:"ip_address"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The IP address allows the verifier to track where the request is coming from. In some cases, geographical or network restrictions might be applied based on the IP."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"user_name-if-present",children:[(0,t.jsx)(i.code,{children:"user_name"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": This is the authenticated username of the requester. It is used by the verifier to determine whether the user is authorized to perform the requested action based on their permissions."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"user_email-if-present",children:[(0,t.jsx)(i.code,{children:"user_email"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": If the user\u2019s email is present, it can help identify the user uniquely and enable further context about their authentication and permissions."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"user_tag-if-present",children:[(0,t.jsx)(i.code,{children:"user_tag"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The user's associated tag provides additional categorization of the user, which may be used for access control or other policies."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"user_metadata-if-present",children:[(0,t.jsx)(i.code,{children:"user_metadata"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": Additional metadata tied to the user, offering further context such as roles, groups, or other important attributes."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"apikey_client_id-if-present",children:[(0,t.jsx)(i.code,{children:"apikey_client_id"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The client ID associated with the API key. It is important for verifying the specific client making the request."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"apikey_client_name-if-present",children:[(0,t.jsx)(i.code,{children:"apikey_client_name"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": The name of the client associated with the API key, often used for easier identification and access control."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"apikey_tag-if-present",children:[(0,t.jsx)(i.code,{children:"apikey_tag"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": A tag tied to the API key, which might be used to distinguish between different versions or categories of API keys."]}),"\n"]}),"\n",(0,t.jsxs)(i.h3,{id:"apikey_metadata-if-present",children:[(0,t.jsx)(i.code,{children:"apikey_metadata"})," (if present)"]}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": Metadata related to the API key, providing extra details such as the scope of access or permissions granted."]}),"\n"]}),"\n",(0,t.jsx)(i.h3,{id:"req_headers",children:(0,t.jsx)(i.code,{children:"req_headers"})}),"\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Explanation"}),": This contains various HTTP headers that can hold important context about the request. Key headers used in Biscuit Verifiers include:","\n",(0,t.jsxs)(i.ul,{children:["\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Authorization"}),": Used for passing authorization tokens (like Biscuit tokens) to verify the user\u2019s credentials and permissions."]}),"\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Remote-address"}),": Denotes the IP address and port of the requester."]}),"\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Request Start Time"}),": Can be used for validating time-based conditions in access control."]}),"\n",(0,t.jsxs)(i.li,{children:[(0,t.jsx)(i.strong,{children:"Accept-Encoding"}),": Specifies what encoding methods the client supports, which might impact how the server processes the request."]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,t.jsx)(i.h2,{id:"demo",children:"Demo"}),"\n",(0,t.jsxs)(i.p,{children:["We need to create a route with our ",(0,t.jsx)(i.code,{children:"Biscuit Verifier Plugin"})]}),"\n",(0,t.jsx)(i.pre,{children:(0,t.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Otoroshi-Client-Id: admin-api-apikey-id\' \\\n  -H \'Otoroshi-Client-Secret: admin-api-apikey-secret\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/proxy.otoroshi.io/v1/routes" \\\n  -d \'{\n  "_loc" : {\n    "tenant" : "default",\n    "teams" : [ "default" ]\n  },\n  "id" : "4874704c-56a2-4460-9a21-ff8055a19c75",\n  "name" : "test route",\n  "description" : "test route",\n  "tags" : [ ],\n  "metadata" : { },\n  "enabled" : true,\n  "groups" : [ "default" ],\n  "bound_listeners" : [ ],\n  "frontend" : {\n    "domains" : [ "biscuit-verifier.oto.tools" ],\n    "strip_path" : true,\n    "exact" : false,\n    "headers" : { },\n    "query" : { },\n    "methods" : [ ]\n  },\n  "backend" : {\n    "targets" : [ {\n      "id" : "www.otoroshi.io",\n      "hostname" : "www.otoroshi.io",\n      "port" : 443,\n      "tls" : true,\n      "weight" : 1,\n      "predicate" : {\n        "type" : "AlwaysMatch"\n      },\n      "protocol" : "HTTP/1.1",\n      "ip_address" : null,\n      "tls_config" : {\n        "certs" : [ ],\n        "trusted_certs" : [ ],\n        "enabled" : false,\n        "loose" : false,\n        "trust_all" : false\n      }\n    } ],\n    "root" : "/",\n    "rewrite" : false,\n    "load_balancing" : {\n      "type" : "RoundRobin"\n    }\n  },\n  "backend_ref" : null,\n  "plugins" : [ \n      {\n      "enabled" : true,\n      "debug" : false,\n      "plugin" : "cp:otoroshi.next.plugins.EchoBackend",\n      "include" : [ ],\n      "exclude" : [ ],\n      "config" : {\n        "limit": 524288\n      },\n      "bound_listeners" : [ ]\n    },\n    {\n      "enabled" : true,\n      "debug" : false,\n      "plugin" : "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator",\n      "include" : [ ],\n      "exclude" : [ ],\n      "config" : {\n        "verifier_refs" : [\n          "biscuit_verifier_6f5f20a5-2c65-4860-8ad1-7b6495ee03bf"\n        ],\n        "enforce": true\n      },\n      "bound_listeners" : [ ]\n    } \n  ]\n}\'\n'})}),"\n",(0,t.jsxs)(i.p,{children:["Now we can call the route with our token in ",(0,t.jsx)(i.code,{children:"Authorization"})," header."]}),"\n",(0,t.jsx)(i.pre,{children:(0,t.jsx)(i.code,{className:"language-sh",children:"curl -X POST \\\n  -H 'Content-Type: application/json' \\\n  -H 'Authorization: Biscuit: Eo0BCiMKBDEyMzQYAyIJCgcIChIDGIAIMg4KDAoCCBsSBggDEgIYABIkCAASIPCKFpXk1RhZiJoXZ0BHvsic65rH5MDSWZJt-8Rn1_XBGkDGK2CcUBcIdt7p3XmDCAEvYrFpB8w6nVPYz9vYFLUQ8M1wTSNaoP7M1UdD5S6AkA0ZJAaVkWsUHdcOgwNpiPwMIiIKIMHFnoE_nPPkAxIDCZ102kwX3z3SoXHp2xQCKik_38Fd' \\\n  \"http://biscuit-verifier.oto.tools:8080\"\n"})}),"\n",(0,t.jsx)(i.p,{children:"The request should pass successfully."})]})}function h(e={}){const{wrapper:i}={...(0,r.R)(),...e.components};return i?(0,t.jsx)(i,{...e,children:(0,t.jsx)(a,{...e})}):a(e)}},16438:(e,i,s)=>{s(96540),s(74848)},3024:(e,i,s)=>{s.d(i,{A:()=>n});const n=s.p+"assets/images/biscuit-verifier-route-add-plugin-7d568a1b6af13e10b0dc8b1c88ecd198.png"},51817:(e,i,s)=>{s.d(i,{A:()=>n});const n=s.p+"assets/images/biscuit-verifier-route-add-to-flow-ccd64f81d671dab87c4cdd44e497c419.png"},63956:(e,i,s)=>{s.d(i,{A:()=>n});const n=s.p+"assets/images/biscuit-verifier-route-creation-263f0559c189c136f41aeecca5b9a12e.png"},24391:(e,i,s)=>{s.d(i,{A:()=>n});const n=s.p+"assets/images/biscuit-verifier-route-setup-flow-a630526da69bbc25516fccf3575bba21.png"},28453:(e,i,s)=>{s.d(i,{R:()=>d,x:()=>o});var n=s(96540);const t={},r=n.createContext(t);function d(e){const i=n.useContext(r);return n.useMemo((function(){return"function"==typeof e?e(i):{...i,...e}}),[i,e])}function o(e){let i;return i=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:d(e.components),n.createElement(r.Provider,{value:i},e.children)}}}]);