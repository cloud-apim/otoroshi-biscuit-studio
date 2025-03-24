"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[162],{45033:(M,e,i)=>{i.r(e),i.d(e,{assets:()=>c,contentTitle:()=>j,default:()=>d,frontMatter:()=>n,metadata:()=>s,toc:()=>T});const s=JSON.parse('{"id":"entities/remotefacts","title":"Remote Facts Loader","description":"The Remote Facts Loader entity facilitates the loading of external facts (checks, rules, acl, rbac) provided by an API.","source":"@site/docs/entities/remotefacts.mdx","sourceDirName":"entities","slug":"/entities/remotefacts","permalink":"/otoroshi-biscuit-studio/docs/entities/remotefacts","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":6,"frontMatter":{"sidebar_position":6},"sidebar":"tutorialSidebar","previous":{"title":"Biscuit RBAC Policies","permalink":"/otoroshi-biscuit-studio/docs/entities/rbac"},"next":{"title":"Plugins","permalink":"/otoroshi-biscuit-studio/docs/category/plugins"}}');var N=i(74848),t=i(28453);i(16438);const n={sidebar_position:6},j="Remote Facts Loader",c={},T=[{value:"Overview",id:"overview",level:2},{value:"<strong>1. API URL</strong>",id:"1-api-url",level:3},{value:"<strong>2. HTTP Method</strong>",id:"2-http-method",level:3},{value:"<strong>3. HTTP Headers</strong>",id:"3-http-headers",level:3},{value:"<strong>4. HTTP Timeout</strong>",id:"4-http-timeout",level:3},{value:"API Input Fields",id:"api-input-fields",level:2},{value:"Schema Details",id:"schema-details",level:2},{value:"API Input Example",id:"api-input-example",level:2},{value:"Entity Configuration Example",id:"entity-configuration-example",level:2},{value:"Key Fields in the Configuration :",id:"key-fields-in-the-configuration-",level:3},{value:"Usage",id:"usage",level:2}];function L(M){const e={a:"a",code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",img:"img",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",table:"table",tbody:"tbody",td:"td",th:"th",thead:"thead",tr:"tr",ul:"ul",...(0,t.R)(),...M.components};return(0,N.jsxs)(N.Fragment,{children:[(0,N.jsx)(e.header,{children:(0,N.jsx)(e.h1,{id:"remote-facts-loader",children:"Remote Facts Loader"})}),"\n",(0,N.jsx)(e.p,{children:(0,N.jsx)(e.img,{src:i(84728).A+"",width:"845",height:"576"})}),"\n",(0,N.jsxs)(e.p,{children:["The ",(0,N.jsx)(e.strong,{children:"Remote Facts Loader"})," entity facilitates the loading of external facts (checks, rules, acl, rbac) provided by an API."]}),"\n",(0,N.jsx)(e.p,{children:"This entity is particularly useful for dynamic role and permission management based on external data sources."}),"\n",(0,N.jsx)(e.h2,{id:"overview",children:"Overview"}),"\n",(0,N.jsx)(e.p,{children:"The Remote Facts Loader connects to an API endpoint to fetch data, such as roles and their associated permissions, and integrates it into your system. This entity is configured with metadata and API details to ensure smooth interaction."}),"\n",(0,N.jsxs)(e.p,{children:["Go to ",(0,N.jsx)(e.a,{href:"http://otoroshi.oto.tools:8080/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts",children:"http://otoroshi.oto.tools:8080/bo/dashboard/extensions/cloud-apim/biscuit/remote-facts"})," and click on ",(0,N.jsx)(e.code,{children:"Add Item"})," to cerate a new entity."]}),"\n",(0,N.jsx)(e.p,{children:(0,N.jsx)(e.img,{src:i(43617).A+"",width:"1681",height:"894"})}),"\n",(0,N.jsxs)(e.p,{children:["Go under in the section ",(0,N.jsx)(e.code,{children:"Configuration"})]}),"\n",(0,N.jsx)(e.p,{children:"You can configure various aspects of your API request, including the URL, HTTP method, headers, and timeout settings."}),"\n",(0,N.jsx)(e.h3,{id:"1-api-url",children:(0,N.jsx)(e.strong,{children:"1. API URL"})}),"\n",(0,N.jsx)(e.p,{children:"Set the endpoint for your API request. This should be a valid URL pointing to the desired API resource."}),"\n",(0,N.jsx)(e.p,{children:(0,N.jsx)(e.strong,{children:"Example:"})}),"\n",(0,N.jsx)(e.pre,{children:(0,N.jsx)(e.code,{className:"language-plaintext",children:"https://api.example.com/data\n"})}),"\n",(0,N.jsx)(e.h3,{id:"2-http-method",children:(0,N.jsx)(e.strong,{children:"2. HTTP Method"})}),"\n",(0,N.jsx)(e.p,{children:"Specify the HTTP method to be used for the request. Common methods include:"}),"\n",(0,N.jsxs)(e.ul,{children:["\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.code,{children:"GET"})," \u2013 Retrieve data"]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.code,{children:"POST"})," \u2013 Submit data"]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.code,{children:"PUT"})," \u2013 Update existing data"]}),"\n"]}),"\n",(0,N.jsx)(e.h3,{id:"3-http-headers",children:(0,N.jsx)(e.strong,{children:"3. HTTP Headers"})}),"\n",(0,N.jsx)(e.p,{children:"Define custom headers for the request. Headers allow you to send metadata such as authentication tokens, content types, or custom fields."}),"\n",(0,N.jsx)(e.p,{children:(0,N.jsx)(e.strong,{children:"Example:"})}),"\n",(0,N.jsx)(e.pre,{children:(0,N.jsx)(e.code,{className:"language-json",children:'{\n  "Content-Type": "application/json",\n  "Authorization": "Bearer your_token_here"\n}\n'})}),"\n",(0,N.jsx)(e.h3,{id:"4-http-timeout",children:(0,N.jsx)(e.strong,{children:"4. HTTP Timeout"})}),"\n",(0,N.jsx)(e.p,{children:"Set the maximum time (in seconds) the request should wait for a response before timing out."}),"\n",(0,N.jsx)(e.p,{children:(0,N.jsx)(e.img,{src:i(2538).A+"",width:"1681",height:"894"})}),"\n",(0,N.jsx)(e.h2,{id:"api-input-fields",children:"API Input Fields"}),"\n",(0,N.jsx)(e.p,{children:'This document describes the schema of the "Remote facts data" retrieved from a JSON response within the context of Biscuit tokens. The schema consists of multiple fields, each containing a specific type of data used for authentication, authorization, and policy enforcement.'}),"\n",(0,N.jsx)(e.h2,{id:"schema-details",children:"Schema Details"}),"\n",(0,N.jsxs)(e.table,{children:[(0,N.jsx)(e.thead,{children:(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.th,{children:"Field"}),(0,N.jsx)(e.th,{children:"Data Type"}),(0,N.jsx)(e.th,{children:"Description"})]})}),(0,N.jsxs)(e.tbody,{children:[(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"roles"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[Map[String, List[String]]]"})}),(0,N.jsx)(e.td,{children:"A list of maps, where each key is a string representing a role type, and its value is a list of associated role values."})]}),(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"revoked"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[String]"})}),(0,N.jsx)(e.td,{children:"A list of revoked roles or permissions represented as strings."})]}),(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"facts"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[Map[String, String]]"})}),(0,N.jsx)(e.td,{children:"A list of key-value pairs representing factual data. Each entry is a map where keys and values are both strings."})]}),(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"acl"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[Map[String, String]]"})}),(0,N.jsx)(e.td,{children:"A list of access control list (ACL) rules. Each entry is a map where keys are ACL identifiers, and values are their respective permissions."})]}),(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"user_roles"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[Map[String, JsValue]]"})}),(0,N.jsx)(e.td,{children:"A list of user role mappings. Each entry is a map where keys represent user role identifiers, and values are JSON values (which could be strings, objects, or arrays)."})]}),(0,N.jsxs)(e.tr,{children:[(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"checks"})}),(0,N.jsx)(e.td,{children:(0,N.jsx)(e.code,{children:"List[String]"})}),(0,N.jsx)(e.td,{children:"A list of security or validation checks, represented as strings."})]})]})]}),"\n",(0,N.jsxs)(e.p,{children:["Each field is extracted from the JSON response using Play JSON's validation methods (",(0,N.jsx)(e.code,{children:"validate"}),"). If parsing fails, a default empty list is used as a fallback."]}),"\n",(0,N.jsx)(e.h2,{id:"api-input-example",children:"API Input Example"}),"\n",(0,N.jsx)(e.p,{children:"Below is an example of the data structure that your API might return. This JSON array defines roles and their associated permissions:"}),"\n",(0,N.jsx)(e.pre,{children:(0,N.jsx)(e.code,{className:"language-json",children:'{\n  "facts": [\n    {\n      "name": "role",\n      "value": "admin"\n    },\n    {\n      "name": "resource",\n      "value": "file1"\n    }\n  ],\n  "checks": [\n    "check if user(\\"demo\\")",\n    "check if resource(\\"file1\\")",\n    "check if role(\\"admin\\")"\n  ],\n  "revoked": [\n    "e5e58ecad81377019ddeb1b7cf2afbc1a54321a29cd5f8c8d3ed2bd1237ebd8b5bc3e855e4ba1d44d18705394698beb9a46c6413510a0842907c1eb867d7a90a",\n    "20c81040a822e4cf7b028b938fa1b350a36d612c52a800f75f3fba96ca5b2b5db597e468be09c404d6948a5a209031d40bc1c7b33897b912c3aceae8ba30df07"\n  ],\n  "roles": [\n    {\n      "admin": [\n        "billing:read",\n        "billing:write",\n        "address:read",\n        "address:write"\n      ]\n    },\n    {\n      "accounting": [\n        "billing:read",\n        "billing:write",\n        "address:read"\n      ]\n    },\n    {\n      "support": [\n        "address:read",\n        "address:write"\n      ]\n    },\n    {\n      "pilot": [\n        "spaceship:drive",\n        "address:read"\n      ]\n    },\n    {\n      "delivery": [\n        "address:read",\n        "package:load",\n        "package:unload",\n        "package:deliver"\n      ]\n    }\n  ],\n  "user_roles": [\n    {\n      "id": "0",\n      "name": "Professor Farnsworth",\n      "roles": [\n        "admin"\n      ]\n    },\n    {\n      "id": "1",\n      "name": "Hermes Conrad",\n      "roles": [\n        "accounting"\n      ]\n    },\n    {\n      "id": "2",\n      "name": "Amy Wong",\n      "roles": [\n        "support"\n      ]\n    },\n    {\n      "id": "3",\n      "name": "Leela",\n      "roles": [\n        "pilot",\n        "delivery"\n      ]\n    },\n    {\n      "id": "4",\n      "name": "Fry",\n      "roles": [\n        "delivery"\n      ]\n    }\n  ],\n  "acl": [\n    {\n      "user": "1234",\n      "resource": "admin.doc",\n      "action": "read"\n    },\n    {\n      "user": "1234",\n      "resource": "admin.doc",\n      "action": "write"\n    }\n  ]\n}\n'})}),"\n",(0,N.jsx)(e.h2,{id:"entity-configuration-example",children:"Entity Configuration Example"}),"\n",(0,N.jsx)(e.p,{children:"Below is an example configuration for the Remote Facts Loader entity:"}),"\n",(0,N.jsx)(e.pre,{children:(0,N.jsx)(e.code,{className:"language-json",children:'{\n  "enabled": true,\n  "id": "biscuit_remote_facts_2ae76425-194d-436b-9977-0cdeb8680fbb",\n  "name": "Remote fact loader example",\n  "description": "",\n  "metadata": {},\n  "tags": [],\n  "config": {\n    "api_url": "http://localhost:3333/api/facts",\n    "tls_config": {\n      "certs": [],\n      "trusted_certs": [],\n      "enabled": false,\n      "loose": false,\n      "trust_all": false\n    },\n    "headers": {\n      "Content-Type": "application/json",\n      "Authorization": "Bearer: xxxxx"\n    },\n    "method": "GET",\n    "timeout": 10000\n  }\n}\n'})}),"\n",(0,N.jsx)(e.h3,{id:"key-fields-in-the-configuration-",children:"Key Fields in the Configuration :"}),"\n",(0,N.jsxs)(e.ul,{children:["\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"enabled"}),": A boolean indicating if the loader is active."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"id"}),": A unique identifier for the entity."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"name"}),": A human-readable name for the loader."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"description"}),": Optional, for documenting the purpose or details of the loader."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"metadata"}),": Contains system-generated metadata such as creation time."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"tags"}),": Optional tags for categorization or searching."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"config"}),":","\n",(0,N.jsxs)(e.ul,{children:["\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"api_url"}),": The endpoint URL where the API data is fetched."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"headers"}),": HTTP headers to include in the request, such as ",(0,N.jsx)(e.code,{children:"Authorization"})," or ",(0,N.jsx)(e.code,{children:"Accept"}),"."]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,N.jsx)(e.h2,{id:"usage",children:"Usage"}),"\n",(0,N.jsxs)(e.ol,{children:["\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"Define the API Endpoint"}),": Ensure the API serving the roles data is operational and adheres to the required JSON format."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"Configure the Entity"}),": Update the ",(0,N.jsx)(e.code,{children:"config.apiUrl"})," and ",(0,N.jsx)(e.code,{children:"headers"})," fields with the API endpoint and authentication details."]}),"\n",(0,N.jsxs)(e.li,{children:[(0,N.jsx)(e.strong,{children:"Activate the Loader"}),": Set the ",(0,N.jsx)(e.code,{children:"enabled"})," field to ",(0,N.jsx)(e.code,{children:"true"})," to activate the entity."]}),"\n"]})]})}function d(M={}){const{wrapper:e}={...(0,t.R)(),...M.components};return e?(0,N.jsx)(e,{...M,children:(0,N.jsx)(L,{...M})}):L(M)}},16438:(M,e,i)=>{i(96540),i(74848)},84728:(M,e,i)=>{i.d(e,{A:()=>s});const s="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4NDQuNjY5MyIgaGVpZ2h0PSI1NzYiIHZpZXdCb3g9IjAgMCA4NDQuNjY5MyA1NzYiIHhtbG5zOnhsaW5rPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5L3hsaW5rIiByb2xlPSJpbWciIGFydGlzdD0iS2F0ZXJpbmEgTGltcGl0c291bmkiIHNvdXJjZT0iaHR0cHM6Ly91bmRyYXcuY28vIj48cGF0aCBpZD0iZmZiNTNlMTUtOTZhOC00OWU0LTg0NzQtZGRlMzFmNDdjODM4LTM4NSIgZGF0YS1uYW1lPSJQYXRoIDEzMyIgZD0iTTY4MC4zOTMxNyw3MjIuNjQwMzJhNTAuODA3MzksNTAuODA3MzksMCwwLDEtMi4zNzM2NiwxNC4wNDQ2OWMtLjAzMjA4LjEwNTUzLS4wNjc2OC4yMDk0Ni0uMTAyMzMuMzE1aC04Ljg1NzI5Yy4wMDkzLS4wOTQ2Mi4wMTg5My0uMjAwMTYuMDI4MjQtLjMxNS41OTA1Mi02Ljc4OTMyLTIuNzgzMjgtNDcuNjE4NTQtNi44NTQ0My01NC42ODVDNjYyLjU5MDM4LDY4Mi41NzMyMiw2ODEuMDc5OTIsNzAxLjA2NzU3LDY4MC4zOTMxNyw3MjIuNjQwMzJaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjZTZlNmU2Ii8+PHBhdGggaWQ9ImIwZDUzNzM2LTkzZTctNDA1Ny1iZTE4LTllOWNkMTdmMzkxYi0zODYiIGRhdGEtbmFtZT0iUGF0aCAxMzQiIGQ9Ik02NzkuNzI4NTMsNzM2LjY4NWMtLjA3NDEuMTA1NTMtLjE1MTA5LjIxMTA3LS4yMy4zMTVoLTYuNjQ1Yy4wNTAzNy0uMDg5ODIuMTA4NzQtLjE5NTM1LjE3NjQyLS4zMTUsMS4wOTc2Ni0xLjk4MSw0LjM0NjY5LTcuOTA0Myw3LjM2MjUyLTE0LjA0NDY5LDMuMjM5NzMtNi41OTg3OSw2LjIxNDUtMTMuNDQ3NzYsNS45NjQtMTUuOTI2NjJDNjg2LjQzNDEyLDcwNy4yNzI0NSw2ODguNjc2OSw3MjQuMzE2LDY3OS43Mjg1Myw3MzYuNjg1WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iI2U2ZTZlNiIvPjxwYXRoIGQ9Ik0xMDIwLjk3ODU1LDU1Ny41Mzk0OWExLjM1NDQ2LDEuMzU0NDYsMCwwLDEtMS4zNDk3My0xLjI1OTYyLDYwLjQ0Niw2MC40NDYsMCwwLDAtNjAuMTIwMjUtNTYuMDFjLTEuODI2MjcsMC0zLjc0MTUuMDkyNTktNS42OTI2LjI3NTQ3YTEuMzU4ODMsMS4zNTg4MywwLDAsMS0xLjMwMTc5LS42NzUyOCw3MS43NTcsNzEuNzU3LDAsMCwwLTEzMS4yMjIzNCwxNS42MDcyMSwxLjM2Mjc4LDEuMzYyNzgsMCwwLDEtMS4zMzEzLjk3NzJsLS40NTMzLS4wMTM1NmMtLjIzNzUyLS4wMDc5My0uNDc1LS4wMTYyLS43MTQ1NS0uMDE2MkE1MC43MTE4OCw1MC43MTE4OCwwLDAsMCw3NjkuMzc1MSw1NTYuNDY0NGExLjM1NDQzLDEuMzU0NDMsMCwwLDEtMS4zMjUzNSwxLjA3NTA5SDY1NC4xMjU4OWExLjM1NDUzLDEuMzU0NTMsMCwxLDEsMC0yLjcwOTA2SDc2Ni45NjI3NWE1My40NDM1Niw1My40NDM1NiwwLDAsMSw1MS44Mjk5NC00MS4xMTQ4Yy4wNjU2NCwwLC4xMzExMi4wMDA2Ni4xOTY1Mi4wMDEzMmE3NS4wOTE4OCw3NS4wOTE4OCwwLDAsMSwyNi4xMDk3MS0zNy40OTA3QTczLjc1NTI5LDczLjc1NTI5LDAsMCwxLDg5MC4yMTM0OSw0NjFhNzQuNzA0LDc0LjcwNCwwLDAsMSw2NC4yMTU0MiwzNi43NzI0M2MxLjczNjE2LS4xNDA1NSwzLjQ0MTA2LS4yMTE2NSw1LjA3OTY2LS4yMTE2NWE2My4xNjQxNCw2My4xNjQxNCwwLDAsMSw2Mi44MjI3LDU4LjUyOTI3LDEuMzU0NDQsMS4zNTQ0NCwwLDAsMS0xLjI1NjQ4LDEuNDQ2MTRDMTAyMS4wNDI1NCw1NTcuNTM4NSwxMDIxLjAxMDQ3LDU1Ny41Mzk0OSwxMDIwLjk3ODU1LDU1Ny41Mzk0OVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiNlNWU1ZTUiLz48cGF0aCBkPSJNOTU1Ljc0NTU3LDU3OS4zNDNINzIxLjg2NDQ2YTEuMzU0NTQsMS4zNTQ1NCwwLDAsMSwwLTIuNzA5MDdIOTU1Ljc0NTU3YTEuMzU0NTQsMS4zNTQ1NCwwLDAsMSwwLDIuNzA5MDdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjZTVlNWU1Ii8+PHBhdGggZD0iTTc4OS44ODQzLDYwNEg2OTYuMzU3YTEuMzU0NTMsMS4zNTQ1MywwLDEsMSwwLTIuNzA5MDZINzg5Ljg4NDNhMS4zNTQ1MywxLjM1NDUzLDAsMCwxLDAsMi43MDkwNloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiNlNWU1ZTUiLz48cGF0aCBkPSJNNjExLjEzMDY4LDI3Ni4zNTY0NWExLjk5OTg5LDEuOTk5ODksMCwwLDEtMS45OTI5Mi0xLjg1OTg3QTcwLjY4NCw3MC42ODQsMCwwLDAsNTM4LjgzNDU0LDIwOWMtMi4xMzIwOCwwLTQuMzcxODMuMTA4NC02LjY1NzQ3LjMyMjI3YTEuOTk5MjYsMS45OTkyNiwwLDAsMS0xLjkyMTg4LS45OTcwNyw4My45ODg1NCw4My45ODg1NCwwLDAsMC0xNTMuNTkwMzMsMTguMjY3MDksMS45OTA3NCwxLjk5MDc0LDAsMCwxLTEuOTY2OCwxLjQ0Mjg3bC0uNTMxNzQtLjAxNjEyYy0uMjc2NjEtLjAwOTI3LS41NTMtLjAxOS0uODMxNzgtLjAxOWE1OS4yMzUxNCw1OS4yMzUxNCwwLDAsMC01Ny43MjMxNSw0Ni43NjksMiwyLDAsMCwxLTEuOTU3LDEuNTg3NDFoLTEzMy45ODlhMiwyLDAsMCwxLDAtNEgzMTIuMDUzNzhBNjMuMjcwNzIsNjMuMjcwNzIsMCwwLDEsMzczLjI2NTQ1LDIyNGE4Ny45ODc3OSw4Ny45ODc3OSwwLDAsMSwxNTkuODE2MTYtMTguNzY5NTNjMS45NjcyOC0uMTUzMzIsMy44OTcyMS0uMjMwNDcsNS43NTI5My0uMjMwNDdBNzQuNjk3MDYsNzQuNjk3MDYsMCwwLDEsNjEzLjEyOCwyNzQuMjE2MzFhMS45OTk4NCwxLjk5OTg0LDAsMCwxLTEuODU1MjIsMi4xMzUyNUM2MTEuMjI1MTYsMjc2LjM1NSw2MTEuMTc3OCwyNzYuMzU2NDUsNjExLjEzMDY4LDI3Ni4zNTY0NVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiNlNWU1ZTUiLz48cGF0aCBkPSJNNTg3LjMzNDU0LDMwMmgtMzI4YTIsMiwwLDAsMSwwLTRoMzI4YTIsMiwwLDAsMSwwLDRaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjZTVlNWU1Ii8+PHBhdGggZD0iTTMzOS4zMzQ1NCwzMzFoLTExMGEyLDIsMCwwLDEsMC00aDExMGEyLDIsMCwxLDEsMCw0WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iI2U1ZTVlNSIvPjxwb2x5Z29uIHBvaW50cz0iMzY4Ljk2NSA1NTEuNzQ4IDM3My4xNjQgNTQwLjk0NiAzMzMuNTAxIDUxOS42MDkgMzI3LjMwMyA1MzUuNTUxIDM2OC45NjUgNTUxLjc0OCIgZmlsbD0iIzlmNjE2YSIvPjxwYXRoIGQ9Ik01NjkuMTI5NDcsNjg1LjU3MDE4YTE0LjUzNywxNC41MzcsMCwwLDAtMTguODI1NDEsOC4yODYxMWwtMi44MzI4Nyw3LjI4MTQxTDU0My4xMDM4Niw3MTIuMzc3bC0xLjA2NzY0LDIuNzU1ODMsMTMuOTk1NjksNS40NDQyOSwxMy41Mzc2LTM0LjgzMjMyWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iIzJmMmU0MSIvPjxwYXRoIGQ9Ik01NDMuMTA1NTMsNjkyLjk2MDQ1bC0xLjgyNjQsNC44NDMxMi00LjU1NzkxLDEyLjA4ODMxLS4xMDkzMy4zMDIzMmMtMTEuNjEyNTMsMS40OTIwOC0yNC4zNDI5LTEuNDMzLTM2LjExNzU1LTUuOTc3MTVhMTQ2LjgxMDM1LDE0Ni44MTAzNSwwLDAsMS0xMy44MjQtNi4xNjg1OWMtNi42NTg3MS0zLjM2NDQ5LTEyLjYyMDc0LTYuOTE1NDYtMTcuMzc3MDYtOS45NTI1LTcuMTYxMjMtNC41OTYyMS0xMS41ODkzLTguMDQ3MjktMTEuNTg5My04LjA0NzI5cy0xLjE2OTgzLDEuMDU3MTMtMy4yODY2NywyLjg2OTkzYy0yLjgzNzIzLDIuNDI3NTgtNy4zNzA4OSw2LjIxODQxLTEzLjExMzU4LDEwLjY4MDcxcS0zLjI4NiwyLjU3MTI0LTcuMDU2MjEsNS4zNzg5Yy0xOC41MjgzLDEzLjc0MzA4LTQyLjMwNi0yMi45Mjg0OS00Mi4zMDYtMjIuOTI4NDlzNi42ODczNS00LjI1NzUxLDguNDM0MzgtNS4wODQyYzUuNjI1NDUtMi42NjE0NiwxOC44NDEzMy04Ljk0MTU2LDMwLjI0MzE4LTE0LjUyMjU2LDUuODc0MTMtMi44Nzk2OCwxMS4yNTk4NC01LjU3OTQ5LDE0Ljg3NTc5LTcuNDk4MzEsMTIuODA0MzktNi44MTcyMiwyOC43MjExOSw2LjA4MjUyLDI4LjcyMTE5LDYuMDgyNTJaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTU0My4xMDU1Myw2OTIuOTYwNDVsLTEuODI2NCw0Ljg0MzEyLTQuNTU3OTEsMTIuMDg4MzEtLjEwOTMzLjMwMjMyYy0xMS42MTI1MywxLjQ5MjA4LTI0LjM0MjktMS40MzMtMzYuMTE3NTUtNS45NzcxNWExNDYuODEwMzUsMTQ2LjgxMDM1LDAsMCwxLTEzLjgyNC02LjE2ODU5Yy02LjY1ODcxLTMuMzY0NDktMTIuNjIwNzQtNi45MTU0Ni0xNy4zNzcwNi05Ljk1MjUtNy4xNjEyMy00LjU5NjIxLTExLjU4OTMtOC4wNDcyOS0xMS41ODkzLTguMDQ3MjlzLTEuMTY5ODMsMS4wNTcxMy0zLjI4NjY3LDIuODY5OTNjLTIuODM3MjMsMi40Mjc1OC03LjM3MDg5LDYuMjE4NDEtMTMuMTEzNTgsMTAuNjgwNzFxLTMuMjg2LDIuNTcxMjQtNy4wNTYyMSw1LjM3ODljLTE4LjUyODMsMTMuNzQzMDgtNDIuMzA2LTIyLjkyODQ5LTQyLjMwNi0yMi45Mjg0OXM2LjY4NzM1LTQuMjU3NTEsOC40MzQzOC01LjA4NDJjNS42MjU0NS0yLjY2MTQ2LDE4Ljg0MTMzLTguOTQxNTYsMzAuMjQzMTgtMTQuNTIyNTYsNS44NzQxMy0yLjg3OTY4LDExLjI1OTg0LTUuNTc5NDksMTQuODc1NzktNy40OTgzMSwxMi44MDQzOS02LjgxNzIyLDI4LjcyMTE5LDYuMDgyNTIsMjguNzIxMTksNi4wODI1MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIG9wYWNpdHk9IjAuMTQiLz48cGF0aCBkPSJNNDA5LjkxNTI0LDYyMC45Nzg3OUE4Ljg2Mzc0LDguODYzNzQsMCwwLDAsNDAzLjEyMyw2MzIuNzUxNGwtMTQuOTA4MywxMy43MTE4NSw2LjEyMywxMS4wOTIsMjAuNzgxNDQtMTkuNjU5NDhhOC45MTE3OCw4LjkxMTc4LDAsMCwwLTUuMjAzOTMtMTYuOTE3WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iIzlmNjE2YSIvPjxwYXRoIGQ9Ik0zNjUuMDU3LDY4My4zMjA2OXEtMS4wMTAxMSwwLTIuMDIzMjMtLjA4NzI0YTIzLjgyLDIzLjgyLDAsMCwxLTE5LjE4NjY2LTEyLjgxMzI2bC0yMy44ODQzOC00NS45NzFhMTMuMTkwNDIsMTMuMTkwNDIsMCwwLDEsMjIuODA5MTgtMTMuMjAwNWwyMi4wMzQ5Myw0OS40NDEsMjkuOTgzMTUtMjQuMzYxMzksMTcuMzYwNTEsNi43NTgxTDM4Mi44MzIsNjc1LjQ1MzkyQTI0LjA4OTg5LDI0LjA4OTg5LDAsMCwxLDM2NS4wNTcsNjgzLjMyMDY5WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iI2NjYyIvPjxwYXRoIGQ9Ik0yNjkuMjk4MzksNzM3LjI5MmE4Ljg2MzczLDguODYzNzMsMCwwLDAsNi4xNzQ0NC0xMi4xMDhsMTQuMTc5NDEtMTQuNDY0MzJMMjgyLjk2MzkyLDY5OS45NTlsLTE5LjczNzE3LDIwLjcwNzY3YTguOTExNzcsOC45MTE3NywwLDAsMCw2LjA3MTY0LDE2LjYyNTMzWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iIzlmNjE2YSIvPjxwYXRoIGQ9Ik00MDQuMjA2ODEsNDgyLjA5MzZoMS4zODk0NFY0NDQuMDMwMTJBMjIuMDMwMSwyMi4wMzAxLDAsMCwxLDQyNy42MjYzNCw0MjJINTA4LjI2OWEyMi4wMzAxMSwyMi4wMzAxMSwwLDAsMSwyMi4wMzAxNSwyMi4wMzAwNVY2NTIuODQ5MzFhMjIuMDMwMTEsMjIuMDMwMTEsMCwwLDEtMjIuMDMwMDksMjIuMDMwMTJINDI3LjYyNjQ0YTIyLjAzMDEsMjIuMDMwMSwwLDAsMS0yMi4wMzAxNi0yMi4wM1Y1MDkuMTg3ODNoLTEuMzg5NDdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjM2YzZDU2Ii8+PHBhdGggZD0iTTQyNi43Mzc1LDQyNy43MzE0N0g0MzcuMjY0YTcuODE2MjMsNy44MTYyMywwLDAsMCw3LjIzNjc2LDEwLjc2ODIxaDQ2LjE5OTE0YTcuODE2MjEsNy44MTYyMSwwLDAsMCw3LjIzNjc2LTEwLjc2ODIyaDkuODMxODFhMTYuNDUxODQsMTYuNDUxODQsMCwwLDEsMTYuNDUxODUsMTYuNDUxODJWNjUyLjY5NjEyQTE2LjQ1MTg0LDE2LjQ1MTg0LDAsMCwxLDUwNy43Njg1Miw2NjkuMTQ4aC04MS4wMzFhMTYuNDUxODQsMTYuNDUxODQsMCwwLDEtMTYuNDUxODUtMTYuNDUxODNoMFY0NDQuMTgzMjlBMTYuNDUxODMsMTYuNDUxODMsMCwwLDEsNDI2LjczNzUsNDI3LjczMTQ3WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iI2ZmZiIvPjxjaXJjbGUgY3g9IjI4OS42MzA2NCIgY3k9IjM4Ni42ODUxOSIgcj0iNDYuNjkxIiBmaWxsPSIjNmM2M2ZmIi8+PHBvbHlnb24gcG9pbnRzPSIyODUuMTgxIDQwNi4zOTUgMjcxLjIwNyAzODguNDI2IDI3OS4zMzMgMzgyLjEwNSAyODUuOTUgMzkwLjYxMyAzMDguMzA0IDM2Ny4wMTYgMzE1Ljc3OSAzNzQuMDk4IDI4NS4xODEgNDA2LjM5NSIgZmlsbD0iI2ZmZiIvPjxwYXRoIGQ9Ik0zNTcuNzc2NTksNTkxLjI5NTdIMzAyLjk1MTFhNC4yNTg1LDQuMjU4NSwwLDAsMS00LjI1MzctNC4yNTM3VjU2My40MTAzMmEzMS42NjY0NSwzMS42NjY0NSwwLDAsMSw2My4zMzI4OSwwVjU4Ny4wNDJBNC4yNTg1LDQuMjU4NSwwLDAsMSwzNTcuNzc2NTksNTkxLjI5NTdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjMmYyZTQxIi8+PHBvbHlnb24gcG9pbnRzPSIzNjUuNjE1IDU2Ni45NCAzNzAuNjMxIDU1Ni40OTMgMzMyLjcyNCA1MzIuMTcxIDMyNS4zMiA1NDcuNTkgMzY1LjYxNSA1NjYuOTQiIGZpbGw9IiM5ZjYxNmEiLz48cGF0aCBkPSJNNTY3Ljg3NzQ1LDcwMi41NzM4MWExNC41MzcsMTQuNTM3LDAsMCwwLTE5LjQwNjM2LDYuODE1MzRsLTMuMzgzOSw3LjA0MjI1LTUuMjE3OSwxMC44NzA1Ny0xLjI3NjIsMi42NjU2NiwxMy41MzYwNiw2LjUwMzQ1LDE2LjE3MzYyLTMzLjY4OTM0WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iIzJmMmU0MSIvPjxwYXRoIGQ9Ik01NDEuMzYyNjYsNzA3Ljk0MjkybC0yLjE5MzA4LDQuNjg4NDktNS40NzMxNCwxMS43MDI0MS0uMTMyMjMuMjkzYy0xMS42OTI4NC41OTU1Mi0yNC4xNjA4NS0zLjI5OS0zNS41NTE2LTguNzM0MjZhMTQ2LjgwOTA4LDE0Ni44MDkwOCwwLDAsMS0xMy4zMDkyLTcuMjEyMzljLTYuMzgwNTYtMy44NjYxMS0xMi4wNTIxNi03Ljg2NDYzLTE2LjU2MTEtMTEuMjU4MTEtNi43ODctNS4xMzI3OS0xMC45MzY4MS04LjkxMzg2LTEwLjkzNjgxLTguOTEzODZzLTEuMjQ3NTguOTY0MTQtMy40OTc0NCwyLjYwODk1Yy0zLjAxNTM1LDIuMjAyNDMtNy44MjY4NCw1LjYzMzc1LTEzLjg5NTM4LDkuNjQxNjhxLTMuNDczNzksMi4zMTExOS03LjQ0ODU5LDQuODIwODljLTE5LjUyOTM3LDEyLjI3OS00Ny4zMDEyMSwyNy4yMzMxNS03MC45MjM2NSwzMC4zOTAzMS00MS40ODc2Nyw1LjU0ODcxLTMxLjcyMy00OS44MTU1My0zMS43MjMtNDkuODE1NTNsMzMuMDE4MTItMTcuMTQ3MTYsMTQuMDE4MzgsMi44NDUyNiwxNS42MDYzNywzLjE1NzE2LDUuNTI5NjcsMS4xMjQ4N3MxLjA0OTM1LS4zOTcsMi44NTQ3My0xLjA4N2M1LjgxMzMtMi4yMjE0MSwxOS40NzI1OS03LjQ2NzY0LDMxLjI2OTUxLTEyLjE1NjE4LDYuMDc4LTIuNDE5ODgsMTEuNjU1MjEtNC42OTgsMTUuNDA3OS02LjMzMzMsMTMuMjkwMjgtNS44MTMzNiwyOC4xNjksOC4yNzEwOSwyOC4xNjksOC4yNzEwOVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiMyZjJlNDEiLz48Y2lyY2xlIGN4PSIxNjAuNDYwOTIiIGN5PSI0MDguMjQ0NjEiIHI9IjIyLjEwNjI1IiBmaWxsPSIjOWY2MTZhIi8+PHBhdGggZD0iTTMwOS4zNjY3NSw2MzEuNjA4ODVsMTkuMjU2ODEsNTQuNTk0NzItLjExNTYyLjE5MTA4Yy0yLjY3ODQxLDQuNDI3NzEtMy41NTM3NSw3Ljk5OTIzLTIuNTMxMTcsMTAuMzI4MjRhNC41MDA1Myw0LjUwMDUzLDAsMCwwLDIuODUzMzQsMi40NjE0OGw0NS41NzAxMy0zMS44NTI0NS0xLjU5NTYtMTIuNzY2MTguMDg4ODUtLjE0MjYzYzQuNDA0NjMtNy4wNDc1LDUuODMwODQtMTMuNDI5OSw0LjIzOTYzLTE4Ljk2OS0yLjA2NDc3LTcuMTg3ODItOC42MjYtMTAuNDUxLTguNjkyLTEwLjQ4MzMzbC0uMTU4NzctLjEyNTA4TDM0NS41NTM0NCw1OTcuMTgyM2wtMzAuMzgyODYsNS4zOTc5WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iI2NjYyIvPjxwYXRoIGQ9Ik0yODAuMDgyMzksNzI3LjQzNTc3bC0xMi45MDA1LTE1Ljg3NzU0LDMxLjA5ODc0LTUxLjMzNywxNC40OTkwOS00NC44MTc1Ni40NDk3OS4xNDUzOS0uNDQ5NzktLjE0NTM5YTE4LjAwNDA2LDE4LjAwNDA2LDAsMSwxLDMyLjEwNDcsMTUuNTM2OUwzMTguMjY2LDY3MC44MjAzNloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiNjY2MiLz48cGF0aCBkPSJNMzY5LjUyMTgxLDU2NS43NzM0OUgzMzUuOTk3MzdsLS4zNDM4Ni00LjgxMzExLTEuNzE4ODQsNC44MTMxMWgtNS4xNjJsLS42ODEyNi05LjUzOTQ0LTMuNDA2NzQsOS41Mzk0NGgtOS45ODgzMXYtLjQ3MjYzYTI1LjA3NzcyLDI1LjA3NzcyLDAsMCwxLDI1LjA0OTM1LTI1LjA0OTU4aDQuNzI2NTZhMjUuMDc3OTEsMjUuMDc3OTEsMCwwLDEsMjUuMDQ5NTgsMjUuMDQ5NThaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTc3LjY2NTM1IC0xNjIpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTMzNS43MjMsNTk1LjcwN2E0LjM0NTEyLDQuMzQ1MTIsMCwwLDEtLjc1MjgtLjA2NjQ2bC0yNC41NDgxLTQuMzMxMjVWNTUwLjczODUzaDI3LjAyMjczbC0uNjY5Ljc4Yy05LjMwODIxLDEwLjg1NTgxLTIuMjk1NTUsMjguNDU4NjQsMi43MTMsMzcuOTg1MTZhNC4xOTA3NCw0LjE5MDc0LDAsMCwxLS4zMzMsNC40NDk0QTQuMjM2Niw0LjIzNjYsMCwwLDEsMzM1LjcyMyw1OTUuNzA3WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE3Ny42NjUzNSAtMTYyKSIgZmlsbD0iIzJmMmU0MSIvPjxwYXRoIGQ9Ik03NzIuMDg2NTEsNzM4aC01MzlhMSwxLDAsMSwxLDAtMmg1MzlhMSwxLDAsMSwxLDAsMloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xNzcuNjY1MzUgLTE2MikiIGZpbGw9IiNjYmNiY2IiLz48L3N2Zz4="},43617:(M,e,i)=>{i.d(e,{A:()=>s});const s=i.p+"assets/images/remote-facts-entity-config-1-622b6594fb38123b4d8ff85b80d8e585.png"},2538:(M,e,i)=>{i.d(e,{A:()=>s});const s=i.p+"assets/images/remote-facts-entity-config-2-2c7aa51b740d8db6a4c5d941c0436952.png"},28453:(M,e,i)=>{i.d(e,{R:()=>n,x:()=>j});var s=i(96540);const N={},t=s.createContext(N);function n(M){const e=s.useContext(t);return s.useMemo((function(){return"function"==typeof M?M(e):{...e,...M}}),[e,M])}function j(M){let e;return e=M.disableParentContext?"function"==typeof M.components?M.components(N):M.components||N:n(M.components),s.createElement(t.Provider,{value:e},M.children)}}}]);