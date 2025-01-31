"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[916],{91179:(i,e,s)=>{s.r(e),s.d(e,{assets:()=>j,contentTitle:()=>t,default:()=>o,frontMatter:()=>r,metadata:()=>M,toc:()=>N});const M=JSON.parse('{"id":"entities/verifiers","title":"Verifiers","description":"A Biscuit Verifier is a key component in the Biscuit token framework, used to validate and enforce additional constraints on a Biscuit token.","source":"@site/docs/entities/verifiers.mdx","sourceDirName":"entities","slug":"/entities/verifiers","permalink":"/otoroshi-biscuit-studio/docs/entities/verifiers","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":2,"frontMatter":{"sidebar_position":2},"sidebar":"tutorialSidebar","previous":{"title":"KeyPairs","permalink":"/otoroshi-biscuit-studio/docs/entities/keypairs"},"next":{"title":"Attenuators","permalink":"/otoroshi-biscuit-studio/docs/entities/attenuators"}}');var c=s(74848),n=s(28453);s(89229);const r={sidebar_position:2},t="Verifiers",j={},N=[{value:"Overview",id:"overview",level:2},{value:"Key Features",id:"key-features",level:3},{value:"Common Use Cases",id:"common-use-cases",level:3},{value:"Example Configuration",id:"example-configuration",level:2},{value:"Explanation of Fields",id:"explanation-of-fields",level:3},{value:"Creating a Biscuit Verifier from Command Line",id:"creating-a-biscuit-verifier-from-command-line",level:2},{value:"Bulk creation",id:"bulk-creation",level:3},{value:"Response",id:"response",level:4},{value:"Configuration Examples",id:"configuration-examples",level:2},{value:"Rules",id:"rules",level:3},{value:"Example Rule",id:"example-rule",level:3},{value:"Facts",id:"facts",level:3},{value:"Policies",id:"policies",level:3},{value:"Example Policy",id:"example-policy",level:3},{value:"Combining Policies",id:"combining-policies",level:3},{value:"Contextual Restrictions",id:"contextual-restrictions",level:2},{value:"Example of Caveats",id:"example-of-caveats",level:3},{value:"Example with IP Restriction",id:"example-with-ip-restriction",level:3},{value:"Advanced Examples",id:"advanced-examples",level:2},{value:"Hierarchical Rights",id:"hierarchical-rights",level:3},{value:"Read-Only Role",id:"read-only-role",level:3},{value:"Resource-Specific Rights",id:"resource-specific-rights",level:3},{value:"Example as CURL call with Rules and Policies",id:"example-as-curl-call-with-rules-and-policies",level:3}];function l(i){const e={code:"code",h1:"h1",h2:"h2",h3:"h3",h4:"h4",header:"header",hr:"hr",img:"img",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,n.R)(),...i.components};return(0,c.jsxs)(c.Fragment,{children:[(0,c.jsx)(e.header,{children:(0,c.jsx)(e.h1,{id:"verifiers",children:"Verifiers"})}),"\n",(0,c.jsx)(e.p,{children:(0,c.jsx)(e.img,{src:s(88936).A+"",width:"826",height:"541"})}),"\n",(0,c.jsx)(e.p,{children:"A Biscuit Verifier is a key component in the Biscuit token framework, used to validate and enforce additional constraints on a Biscuit token."}),"\n",(0,c.jsx)(e.p,{children:"While Biscuit tokens inherently carry embedded rules and checks, a verifier allows you to apply context-specific restrictions during token validation without modifying the token itself."}),"\n",(0,c.jsx)(e.p,{children:"Using a verifier means you don\u2019t need to modify or reissue tokens for every new constraint. Instead, constraints can be added at runtime when validating the token."}),"\n",(0,c.jsx)(e.p,{children:(0,c.jsx)(e.img,{src:s(93126).A+"",width:"2824",height:"1558"})}),"\n",(0,c.jsx)(e.p,{children:(0,c.jsx)(e.img,{src:s(60383).A+"",width:"2818",height:"1694"})}),"\n",(0,c.jsx)(e.h2,{id:"overview",children:"Overview"}),"\n",(0,c.jsx)(e.h3,{id:"key-features",children:"Key Features"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:"Dynamic Constraint Management"}),": Add new constraints at runtime without altering the original token."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:"Enhanced Security"}),": Enforce strict validation policies tailored to specific contexts."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:"Extensibility"}),": Easily integrate with various APIs and frameworks."]}),"\n"]}),"\n",(0,c.jsx)(e.h3,{id:"common-use-cases",children:"Common Use Cases"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsx)(e.li,{children:"Limiting token usage to specific timeframes."}),"\n",(0,c.jsx)(e.li,{children:"Restricting token access to particular resources."}),"\n",(0,c.jsx)(e.li,{children:"Enforcing domain-specific rules dynamically."}),"\n"]}),"\n",(0,c.jsx)(e.h2,{id:"example-configuration",children:"Example Configuration"}),"\n",(0,c.jsx)(e.p,{children:"The following JSON configuration defines a Biscuit Verifier with a time constraint:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-json",children:'{\n  "enabled": true,\n  "id": "biscuit_verifier_6f5f20a5-2c65-4860-8ad1-7b6495ee03bf",\n  "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "name": "Biscuit Verifier",\n  "description": "Biscuit Verifier",\n  "strict": true,\n  "tags": [],\n  "config": {\n    "checks": [\n      "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n    ],\n    "facts": [],\n    "resources": [],\n    "rules": [],\n    "revocation_ids": []\n  },\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitVerifier"\n}\n'})}),"\n",(0,c.jsx)(e.h3,{id:"explanation-of-fields",children:"Explanation of Fields"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"enabled"})}),": Indicates if the verifier is active."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"id"})}),": Unique identifier for the verifier."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"keypair_ref"})}),": Reference to the cryptographic keypair used for token verification."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"name"})}),": Human-readable name for the verifier."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"description"})}),": Description of the verifier\u2019s purpose."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"strict"})}),": Enforces strict checking of constraints."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"tags"})}),": Metadata tags for organizing verifiers."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"config"})}),": Contains the specific rules, checks, facts, and other configurations applied by the verifier.","\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"checks"})}),": Constraints enforced during validation (e.g., time-based restrictions)."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"facts"})}),": Contextual facts provided to the verifier."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"resources"})}),": Specifies resources associated with the token."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"rules"})}),": Defines custom rules for validation."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"revocation_ids"})}),": List of IDs marking revoked tokens."]}),"\n"]}),"\n"]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.strong,{children:(0,c.jsx)(e.code,{children:"kind"})}),": Specifies the resource type for integration with APIs."]}),"\n"]}),"\n",(0,c.jsx)(e.h2,{id:"creating-a-biscuit-verifier-from-command-line",children:"Creating a Biscuit Verifier from Command Line"}),"\n",(0,c.jsxs)(e.p,{children:["You can create a Biscuit Verifier using the Otoroshi API with the following ",(0,c.jsx)(e.code,{children:"curl"})," command:"]}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/json\' \\\n  \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers\' \\\n  -u admin-api-apikey-id:admin-api-apikey-secret \\\n  -d \'{\n    "enabled": true,\n    "id": "biscuit_verifier_6f5f20a5-2c65-4860-8ad1-7b6495ee03bf",\n    "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n    "name": "Biscuit Verifier CURL",\n    "description": "A Biscuit Verifier created from Otoroshi API",\n    "strict": true,\n    "tags": [],\n    "config": {\n      "checks": [\n        "check if time($date), $date <= 2024-12-30T19:00:10Z;"\n      ],\n      "facts": [],\n      "resources": [],\n      "rules": [],\n      "revocation_ids": []\n    },\n    "kind": "biscuit.extensions.cloud-apim.com/BiscuitVerifier"\n  }\'\n'})}),"\n",(0,c.jsx)(e.h3,{id:"bulk-creation",children:"Bulk creation"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/x-ndjson\' \\\n  \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers/_bulk\' \\\n  -u admin-api-apikey-id:admin-api-apikey-secret \\\n  -d \'{"enabled":true,"id":"verifier_bulk_1","keypair_ref":"biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9","name":"Biscuit Verifier FROM CURL BULK 1","description":"A Biscuit Verifier created from Otoroshi API","strict":true,"tags":[],"config.checks":["check if time($date), $date <= 2024-12-30T19:00:10Z;"],"config.facts":[],"config.resources":[],"config.rules":[],"config.revocation_ids":[],"kind":"biscuit.extensions.cloud-apim.com/BiscuitVerifier"}\n{"enabled":true,"id":"verifier_bulk_2","keypair_ref":"biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9","name":"Biscuit Verifier FROM CURL BULK 2","description":"A Biscuit Verifier created from Otoroshi API","strict":true,"tags":[],"config.checks":["check if time($date), $date <= 2024-12-30T19:00:10Z;"],"config.facts":[],"config.resources":[],"config.rules":[],"config.revocation_ids":[],"kind":"biscuit.extensions.cloud-apim.com/BiscuitVerifier"}\'\n'})}),"\n",(0,c.jsx)(e.h4,{id:"response",children:"Response"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'{"status":201,"created":true,"id":"verifier_bulk_1","id_field":"id"}\n{"status":201,"created":true,"id":"verifier_bulk_2","id_field":"id"}\n'})}),"\n",(0,c.jsx)(e.h2,{id:"configuration-examples",children:"Configuration Examples"}),"\n",(0,c.jsx)(e.h3,{id:"rules",children:"Rules"}),"\n",(0,c.jsx)(e.p,{children:"Rules are logical expressions used to define conditions under which certain actions are authorized. They can combine facts, predicates, and other rules to evaluate permissions."}),"\n",(0,c.jsx)(e.h3,{id:"example-rule",children:"Example Rule"}),"\n",(0,c.jsx)(e.p,{children:"The following rule determines whether a user is allowed to perform an operation on a resource:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:"is_allowed($user, $res, $op) <- \n    user($user),          // Declares the user\n    resource($res),       // Declares the resource\n    operation($op),       // Declares the operation\n    right($user, $res, $op); // Verifies the user has the necessary rights\n"})}),"\n",(0,c.jsx)(e.p,{children:"In this example:"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.code,{children:"user($user)"})," identifies the user."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.code,{children:"resource($res)"})," identifies the resource being accessed."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.code,{children:"operation($op)"})," specifies the action (e.g., read, write)."]}),"\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.code,{children:"right($user, $res, $op)"})," asserts that the user has the permission for the operation on the resource."]}),"\n"]}),"\n",(0,c.jsx)(e.hr,{}),"\n",(0,c.jsx)(e.h3,{id:"facts",children:"Facts"}),"\n",(0,c.jsx)(e.p,{children:"Facts are data points or assertions stored in the Biscuit token. They are used as inputs to evaluate the rules. For example:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'user("alice");\nresource("file1");\noperation("read");\nright("alice", "file1", "read");\n'})}),"\n",(0,c.jsx)(e.p,{children:"These facts state that:"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsx)(e.li,{children:"Alice is the user."}),"\n",(0,c.jsx)(e.li,{children:'The resource is "file1".'}),"\n",(0,c.jsx)(e.li,{children:'The operation is "read".'}),"\n",(0,c.jsx)(e.li,{children:"Alice has the right to perform the read operation on file1."}),"\n"]}),"\n",(0,c.jsx)(e.hr,{}),"\n",(0,c.jsx)(e.h3,{id:"policies",children:"Policies"}),"\n",(0,c.jsxs)(e.p,{children:["Policies are used to define the decision logic for allowing or denying access. Biscuit tokens support ",(0,c.jsx)(e.code,{children:"allow"})," and ",(0,c.jsx)(e.code,{children:"deny"})," policies to control access."]}),"\n",(0,c.jsx)(e.h3,{id:"example-policy",children:"Example Policy"}),"\n",(0,c.jsxs)(e.p,{children:["The following policy allows access if the ",(0,c.jsx)(e.code,{children:"is_allowed"})," rule evaluates to true:"]}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:"allow if is_allowed($user, $resource, $op);\n"})}),"\n",(0,c.jsx)(e.p,{children:"This policy specifies that:"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:["Access is granted (",(0,c.jsx)(e.code,{children:"allow"}),") if the ",(0,c.jsx)(e.code,{children:"is_allowed"})," rule matches."]}),"\n"]}),"\n",(0,c.jsx)(e.h3,{id:"combining-policies",children:"Combining Policies"}),"\n",(0,c.jsx)(e.p,{children:"Policies can also include deny conditions, such as:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:"deny if user($user), resource($res), operation($op), not right($user, $res, $op);\n"})}),"\n",(0,c.jsx)(e.p,{children:"This policy denies access if:"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsx)(e.li,{children:"A user, resource, and operation are defined."}),"\n",(0,c.jsx)(e.li,{children:"The user does not have the corresponding right for the resource and operation."}),"\n"]}),"\n",(0,c.jsx)(e.hr,{}),"\n",(0,c.jsx)(e.h2,{id:"contextual-restrictions",children:"Contextual Restrictions"}),"\n",(0,c.jsx)(e.p,{children:"Biscuit tokens support additional contextual restrictions using caveats. Caveats allow tokens to impose extra conditions that must be satisfied for access to be granted."}),"\n",(0,c.jsx)(e.h3,{id:"example-of-caveats",children:"Example of Caveats"}),"\n",(0,c.jsx)(e.p,{children:"To restrict access to a specific time range:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:"time($t), $t <= 1672531200, $t >= 1672444800;\n"})}),"\n",(0,c.jsx)(e.p,{children:"Here:"}),"\n",(0,c.jsxs)(e.ul,{children:["\n",(0,c.jsxs)(e.li,{children:[(0,c.jsx)(e.code,{children:"$t"})," represents the current time."]}),"\n",(0,c.jsx)(e.li,{children:"Access is allowed only if the current time falls between the specified timestamps."}),"\n"]}),"\n",(0,c.jsx)(e.h3,{id:"example-with-ip-restriction",children:"Example with IP Restriction"}),"\n",(0,c.jsx)(e.p,{children:"To restrict access based on IP address:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'ip("192.168.1.1");\n'})}),"\n",(0,c.jsxs)(e.p,{children:["Access is allowed only if the IP matches ",(0,c.jsx)(e.code,{children:"192.168.1.1"}),"."]}),"\n",(0,c.jsx)(e.hr,{}),"\n",(0,c.jsx)(e.h2,{id:"advanced-examples",children:"Advanced Examples"}),"\n",(0,c.jsx)(e.h3,{id:"hierarchical-rights",children:"Hierarchical Rights"}),"\n",(0,c.jsx)(e.p,{children:"Defining a hierarchy where admin users have access to all resources and operations:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'right($user, $res, $op) <- role($user, "admin");\n'})}),"\n",(0,c.jsx)(e.h3,{id:"read-only-role",children:"Read-Only Role"}),"\n",(0,c.jsx)(e.p,{children:'Defining a "read-only" role:'}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'right($user, $res, "read") <- role($user, "read-only");\n'})}),"\n",(0,c.jsx)(e.h3,{id:"resource-specific-rights",children:"Resource-Specific Rights"}),"\n",(0,c.jsx)(e.p,{children:"Granting a user specific rights on a specific resource:"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-js",children:'right("bob", "file2", "write");\n'})}),"\n",(0,c.jsx)(e.h3,{id:"example-as-curl-call-with-rules-and-policies",children:"Example as CURL call with Rules and Policies"}),"\n",(0,c.jsx)(e.pre,{children:(0,c.jsx)(e.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/json\' \\\n  \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers\' \\\n  -u admin-api-apikey-id:admin-api-apikey-secret \\\n  -d \'{\n    "enabled": true,\n    "keypair_ref": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n    "name": "Biscuit Verifier FROM CURL 1",\n    "description": "A Biscuit Verifier created from Otoroshi API",\n    "strict": true,\n    "tags": [],\n    "config": {\n      "checks": [\n        "check if time($date), $date <= 2025-12-30T19:00:10Z;"\n      ],\n      "facts": [\n        "user(\\"alice\\");",\n        "resource(\\"file1\\");",\n        "operation(\\"read\\");",\n        "right(\\"alice\\",\\"file1\\",\\"read\\");"\n      ],\n      "resources": [],\n      "rules": [\n        "is_allowed($user, $res, $op) <- user($user), resource($res), operation($op), right($user, $res, $op);"\n      ],\n      "policies": [\n         "allow if is_allowed($user, $resource, $op);"\n      ],\n      "revokedIds": []\n    },\n    "kind": "biscuit.extensions.cloud-apim.com/BiscuitVerifier"\n  }\'\n'})})]})}function o(i={}){const{wrapper:e}={...(0,n.R)(),...i.components};return e?(0,c.jsx)(e,{...i,children:(0,c.jsx)(l,{...i})}):l(i)}},89229:(i,e,s)=>{s(96540),s(74848)},88936:(i,e,s)=>{s.d(e,{A:()=>M});const M="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI4MjYiIGhlaWdodD0iNTQxIiB2aWV3Qm94PSIwIDAgODI2IDU0MSIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHJvbGU9ImltZyIgYXJ0aXN0PSJLYXRlcmluYSBMaW1waXRzb3VuaSIgc291cmNlPSJodHRwczovL3VuZHJhdy5jby8iPjxwYXRoIGQ9Ik05OTAuNjM3MzMsNzE5LjMwNTA2bDEuMjI0OTItLjAyMzkzYTI4NC4wODAxOCwyODQuMDgwMTgsMCwwLDAtNC4zNDgyMy00MC41OTgyNGMtNS4yOTY4My0yOC43MTI2OC0xNC4xMDQ1NC00Ny41Njk3Ny0yNi4xNzg1MS01Ni4wNDczMmwtLjcwMzM3LDEuMDAyNDJDOTg4LjgzNyw2NDMuNDQxMjgsOTkwLjYyMyw3MTguNTQ4NDUsOTkwLjYzNzMzLDcxOS4zMDUwNloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik05NzUuMzI1ODEsNzE5LjAxMmwxLjIyNDkyLS4wMjM5M2MtLjAyNjMzLTEuMzU2NS0uNzkxOTEtMzMuMzI2NDctMTMuMzc3ODMtNDIuMTYyODlsLS43MDMzOCwxLjAwMjQzQzk3NC41NDc2OCw2ODYuMzA3NTUsOTc1LjMxOTgzLDcxOC42ODYsOTc1LjMyNTgxLDcxOS4wMTJaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTg3IC0xNzkuNSkiIGZpbGw9IiNmMWYxZjEiLz48Y2lyY2xlIGN4PSI3NjYuNjYzNjEiIGN5PSI0MzguMTI0NjMiIHI9IjYuMTI0NjEiIGZpbGw9IiNmMWYxZjEiLz48Y2lyY2xlIGN4PSI3NjkuNjk2NiIgY3k9IjQ5MC43OTYyNiIgcj0iNi4xMjQ2MSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik05NzguMTgwNjMsNjI3LjQyMDY4YTI3LjIwNDgxLDI3LjIwNDgxLDAsMCwwLDEuODQ5LDEzLjkyNzcyLDI0Ljc5Mjg4LDI0Ljc5Mjg4LDAsMCwwLDQuMTY4MjUtMjYuNjIzOTVBMjcuMjA1MTEsMjcuMjA1MTEsMCwwLDAsOTc4LjE4MDYzLDYyNy40MjA2OFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik05NTguMTYxLDY0NS4yNzYyNGEyNy4yMDQ4NCwyNy4yMDQ4NCwwLDAsMCwxNC4wMzY4NS42MDYxLDI0Ljc5Mjk0LDI0Ljc5Mjk0LDAsMCwwLTI1LjQ5MDQxLTguNzQzNDVBMjcuMjA1LDI3LjIwNSwwLDAsMCw5NTguMTYxLDY0NS4yNzYyNFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik05NTcuOTM0ODgsNjkxLjkyMTkzYTE5LjA2ODY2LDE5LjA2ODY2LDAsMCwwLDkuODM4NjkuNDI0ODQsMTcuMzc3NzcsMTcuMzc3NzcsMCwwLDAtMTcuODY2NjktNi4xMjg0NUExOS4wNjgzOCwxOS4wNjgzOCwwLDAsMCw5NTcuOTM0ODgsNjkxLjkyMTkzWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjZjFmMWYxIi8+PHBhdGggZD0iTTQ2OS4wMjU5LDcxOS4zMDUwNWwtMS43Nzc5My0uMDM0NzJhNDEyLjMzMTA5LDQxMi4zMzEwOSwwLDAsMSw2LjMxMTMxLTU4LjkyN2M3LjY4ODE2LTQxLjY3NTQ5LDIwLjQ3MjI2LTY5LjA0NTkxLDM3Ljk5NzIxLTgxLjM1MDhsMS4wMjA5MiwxLjQ1NUM0NzEuNjM5LDYwOS4xOTEzNSw0NjkuMDQ2NzQsNzE4LjIwNjg3LDQ2OS4wMjU5LDcxOS4zMDUwNVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik00OTEuMjUwMDUsNzE4Ljg3OTY3bC0xLjc3NzkzLS4wMzQ3M2MuMDM4MjItMS45Njg5MiwxLjE0OTQzLTQ4LjM3MjI0LDE5LjQxNzQ4LTYxLjE5OGwxLjAyMDkyLDEuNDU1QzQ5Mi4zNzk0OSw2NzEuNDEwMjgsNDkxLjI1ODczLDcxOC40MDY1NCw0OTEuMjUwMDUsNzE4Ljg3OTY3WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjZjFmMWYxIi8+PGNpcmNsZSBjeD0iMzM1LjY5MjAxIiBjeT0iMzkyLjIxOTM1IiByPSI4Ljg4OTY3IiBmaWxsPSIjZjFmMWYxIi8+PGNpcmNsZSBjeD0iMzMxLjI4OTcxIiBjeT0iNDY4LjY3MDQzIiByPSI4Ljg4OTY2IiBmaWxsPSIjZjFmMWYxIi8+PHBhdGggZD0iTTQ4Ny4xMDYzOCw1ODUuOTM4YTM5LjQ4NywzOS40ODcsMCwwLDEtMi42ODM3MSwyMC4yMTU2MiwzNS45ODYsMzUuOTg2LDAsMCwxLTYuMDUwMDctMzguNjQzNzZBMzkuNDg3MjMsMzkuNDg3MjMsMCwwLDEsNDg3LjEwNjM4LDU4NS45MzhaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTg3IC0xNzkuNSkiIGZpbGw9IiNmMWYxZjEiLz48cGF0aCBkPSJNNTE2LjE2NDE1LDYxMS44NTQ3MmEzOS40ODY5MiwzOS40ODY5MiwwLDAsMS0yMC4zNzQuODc5NzQsMzUuOTg2MDgsMzUuOTg2MDgsMCwwLDEsMzYuOTk4NDctMTIuNjkwODJBMzkuNDg3MTQsMzkuNDg3MTQsMCwwLDEsNTE2LjE2NDE1LDYxMS44NTQ3MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2YxZjFmMSIvPjxwYXRoIGQ9Ik01MTYuNDkyNCw2NzkuNTU5MzdhMjcuNjc3NTQsMjcuNjc3NTQsMCwwLDEtMTQuMjgwNTMuNjE2NjQsMjUuMjIzMjgsMjUuMjIzMjgsMCwwLDEsMjUuOTMyOTEtOC44OTUyNUEyNy42NzcyNiwyNy42NzcyNiwwLDAsMSw1MTYuNDkyNCw2NzkuNTU5MzdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTg3IC0xNzkuNSkiIGZpbGw9IiNmMWYxZjEiLz48cGF0aCBkPSJNMzMxLjY3ODg5LDM0MC43MzQwOGwtNC4yNzc0OS0yNi4xNjgyNEwzMTUuMzI0LDMxMS41NDY0MXMtOS41ODU1NC0yLjk1MjQ4LTExLjgyNiwxNi4zNTUwN2MtMi4yNDA0MiwxOS4zMDc2OS0uNTAzNTQsNTUuMzU2MTItLjUwMzU0LDU1LjM1NjEybC0yNi41MTY3NCw4My45NzA2NWExMS43Mjg1OCwxMS43Mjg1OCwwLDEsMCwxNi4xOTM5NSw2Ljk4NDkybDM0LjQ3ODI0LTc4Ljg3Nzg1WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjZmZiN2I3Ii8+PHBhdGggZD0iTTQwNS4xMjIyNSwzMjAuMTQxMTFsLTYuMzQwNjctMjUuNzQ2MjUsOS45MjItNy41MTlzNy42NTY0OS02LjQ3OSwxNy4yOTc4NywxMC4zOTgzMmM5LjY0MTQ0LDE2Ljg3NzQ3LDIyLjE5OCw1MC43MTI5MywyMi4xOTgsNTAuNzEyOTNsNTcuMzU3MzQsNjYuODE1NzVhMTEuNzI4NjEsMTEuNzI4NjEsMCwxLDEtMTIuMTUwOTIsMTIuNzgyMzVMNDMwLjcyNiwzNjguNTc5MjVaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTg3IC0xNzkuNSkiIGZpbGw9IiNmZmI3YjciLz48cG9seWdvbiBwb2ludHM9IjI2Ny42ODkgNTI2LjA5MyAyODIuMDcyIDUyNi4wOTIgMjg4LjkxNSA0NzAuNjE0IDI2Ny42ODYgNDcwLjYxNSAyNjcuNjg5IDUyNi4wOTMiIGZpbGw9IiNmZmI3YjciLz48cGF0aCBkPSJNNDUxLjAxOTg3LDcwMC44OTY3N2wyOC4zMjU4MS0uMDAxMTVoLjAwMTE1YTE4LjA1MjM4LDE4LjA1MjM4LDAsMCwxLDE4LjA1MTQyLDE4LjA1MTE0di41ODY2bC00Ni4zNzc1Mi4wMDE3MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iIzJmMmU0MSIvPjxwb2x5Z29uIHBvaW50cz0iODcuMDE2IDUyNi4wOTMgMTAxLjM5OSA1MjYuMDkyIDEwOC4yNDIgNDcwLjYxNCA4Ny4wMTMgNDcwLjYxNSA4Ny4wMTYgNTI2LjA5MyIgZmlsbD0iI2ZmYjdiNyIvPjxwYXRoIGQ9Ik0yNzAuMzQ2OTQsNzAwLjg5Njc3bDI4LjMyNTgxLS4wMDExNWguMDAxMTVhMTguMDUyMzgsMTguMDUyMzgsMCwwLDEsMTguMDUxNDIsMTguMDUxMTR2LjU4NjZsLTQ2LjM3NzUyLjAwMTcyWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjMmYyZTQxIi8+PGNpcmNsZSBjeD0iMTc1LjIzMDA5IiBjeT0iNTIuNDMxMTMiIHI9IjM4LjkwMTc2IiBmaWxsPSIjMmYyZTQxIi8+PGVsbGlwc2UgY3g9IjMyMy4zMjgzMyIgY3k9IjIwMi40MTk0NSIgcng9IjE2LjA5NzI4IiByeT0iMTIuMDcyOTYiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0yMzUuNDMxNDkgMTA4LjQxNDk0KSByb3RhdGUoLTQ1KSIgZmlsbD0iIzJmMmU0MSIvPjxlbGxpcHNlIGN4PSIzODcuOTcyODEiIGN5PSIxOTIuMjc5MTQiIHJ4PSIxMi4wNzI5NiIgcnk9IjE2LjA5NzI4IiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTI4LjI1NTMgMjk0LjAzMTczKSByb3RhdGUoLTY2Ljg2OTU2KSIgZmlsbD0iIzJmMmU0MSIvPjxjaXJjbGUgY3g9IjM2My40NjM4NyIgY3k9IjI0MS4yMTc4NyIgcj0iMzMuMDE1NTEiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0yMDkuNTMyODMgMjY0LjkzODQ0KSByb3RhdGUoLTYxLjMzNjgyKSIgZmlsbD0iI2ZmYjdiNyIvPjxwYXRoIGQ9Ik0zMjguNzcyMjUsMjIwLjQ1MTIxYTQ0LjkwMzczLDQ0LjkwMzczLDAsMCwwLDI1LjY2MjE1LDcuOTMwNzIsMjcuNTE3MzYsMjcuNTE3MzYsMCwwLDEtMTAuOTA2NSw0LjQ4Nyw5MC41NDUzNiw5MC41NDUzNiwwLDAsMCwzNi45ODQ5MS4yMDc4MSwyMy45MzcsMjMuOTM3LDAsMCwwLDcuNzQyNDMtMi42NTkxOSw5Ljc5ODMyLDkuNzk4MzIsMCwwLDAsNC43NzktNi4zOTEzOWMuODExNDQtNC42MzU1OC0yLjgwMDY2LTguODQ3MTEtNi41NTQ0NC0xMS42ODU0NGE0OC4zNDgxNyw0OC4zNDgxNywwLDAsMC00MC42Mjg0NC04LjExODY5Yy00LjUzODQ2LDEuMTczMTMtOS4wODUsMy4xNTUyNS0xMi4wMzI4Myw2LjhzLTMuODIxMDUsOS4yNjM3Mi0xLjAxMjQ5LDEzLjAxNjgzWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTM0Ni45MTc2NSwyODQuMTUxMjJsLTMyLjY3NzksMjYuNjg3LDI4LjU1MjE1LDY4LjA3OS41NDQ2NCwxMC44OTI2M3MtMjkuNjM5NzksMjcuMzI4LTQ3Ljk3OTA5LDEwMy42NjgyOC01NS4zNzk1OCwxNzcuNzc4MDctNTUuMzc5NTgsMTc3Ljc3ODA3bDc0LjEwMDcxLDE4LjEyNzY0LDUxLjUyMDY1LTE3Mi42ODk2MSw2NS42NzY3LDEzNS40ODAyNSw5LjM0Myw0MC4wNzE2Miw1Ny4yODAyMy0xNS45MDE0NEw0NzUuNzkyNDQsNTY4LjgwODI5LDQyMS41NzIzLDQwMS44ODU3Nmw0LjQ1MjQtNDYuMTE0MTdzOS45MjE4Ni0yMy4wMTczMy03LjMxNDY2LTQ3LjM4NjI4bC01LjQwNjQ5LTIyLjg5ODA3TDM4NS4zMTcsMjgwLjcxNjgxWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTg5Ni40MTQ1NSw1MjYuNDI5Mkg2NjkuNTQzMDVhNi44NDE0Miw2Ljg0MTQyLDAsMCwxLTYuODMzNDgtNi44MzM0OHYtNzYuNTM1YTYuODQxNDIsNi44NDE0MiwwLDAsMSw2LjgzMzQ4LTYuODMzNDhoMjI2Ljg3MTVhNi44NDEzMSw2Ljg0MTMxLDAsMCwxLDYuODMzNDgsNi44MzM0OHY3Ni41MzVBNi44NDEzMSw2Ljg0MTMxLDAsMCwxLDg5Ni40MTQ1NSw1MjYuNDI5MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2U1ZTVlNSIvPjxyZWN0IHg9IjUxMS4yNDM2NiIgeT0iMjc3LjIyNzcxIiB3aWR0aD0iNjAuMTM0NjIiIGhlaWdodD0iNi44MzM0OCIgZmlsbD0iI2ZmZiIvPjxjaXJjbGUgY3g9IjY1MC42NDY2MyIgY3k9IjI4MS4zMjc4IiByPSI0LjEwMDA5IiBmaWxsPSIjNmM2M2ZmIi8+PGNpcmNsZSBjeD0iNjY0LjMxMzU5IiBjeT0iMjgxLjMyNzgiIHI9IjQuMTAwMDkiIGZpbGw9IiM2YzYzZmYiLz48Y2lyY2xlIGN4PSI2NzcuOTgwNTUiIGN5PSIyODEuMzI3OCIgcj0iNC4xMDAwOSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik04OTYuNDE0NTUsNjIzLjQ2NDZINjY5LjU0MzA1YTYuODQxNDIsNi44NDE0MiwwLDAsMS02LjgzMzQ4LTYuODMzNDh2LTc2LjUzNWE2Ljg0MTQyLDYuODQxNDIsMCwwLDEsNi44MzM0OC02LjgzMzQ4aDIyNi44NzE1YTYuODQxMzEsNi44NDEzMSwwLDAsMSw2LjgzMzQ4LDYuODMzNDh2NzYuNTM1QTYuODQxMzEsNi44NDEzMSwwLDAsMSw4OTYuNDE0NTUsNjIzLjQ2NDZaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTg3IC0xNzkuNSkiIGZpbGw9IiNlNWU1ZTUiLz48cmVjdCB4PSI1MTEuMjQzNjYiIHk9IjM3NC4yNjMxMSIgd2lkdGg9IjYwLjEzNDYyIiBoZWlnaHQ9IjYuODMzNDgiIGZpbGw9IiNmZmYiLz48Y2lyY2xlIGN4PSI2NTAuNjQ2NjMiIGN5PSIzNzguMzYzMiIgcj0iNC4xMDAwOSIgZmlsbD0iIzZjNjNmZiIvPjxjaXJjbGUgY3g9IjY2NC4zMTM1OSIgY3k9IjM3OC4zNjMyIiByPSI0LjEwMDA5IiBmaWxsPSIjNmM2M2ZmIi8+PGNpcmNsZSBjeD0iNjc3Ljk4MDU1IiBjeT0iMzc4LjM2MzIiIHI9IjQuMTAwMDkiIGZpbGw9IiM2YzYzZmYiLz48cGF0aCBkPSJNODk2LjQxNDU1LDcyMC41SDY2OS41NDMwNWE2Ljg0MTQyLDYuODQxNDIsMCwwLDEtNi44MzM0OC02LjgzMzQ4di03Ni41MzVhNi44NDE0Miw2Ljg0MTQyLDAsMCwxLDYuODMzNDgtNi44MzM0OGgyMjYuODcxNWE2Ljg0MTMxLDYuODQxMzEsMCwwLDEsNi44MzM0OCw2LjgzMzQ4djc2LjUzNUE2Ljg0MTMxLDYuODQxMzEsMCwwLDEsODk2LjQxNDU1LDcyMC41WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBmaWxsPSIjZTVlNWU1Ii8+PHJlY3QgeD0iNTExLjI0MzY2IiB5PSI0NzEuMjk4NTEiIHdpZHRoPSI2MC4xMzQ2MiIgaGVpZ2h0PSI2LjgzMzQ4IiBmaWxsPSIjZmZmIi8+PGNpcmNsZSBjeD0iNjUwLjY0NjYzIiBjeT0iNDc1LjM5ODYiIHI9IjQuMTAwMDkiIGZpbGw9IiM2YzYzZmYiLz48Y2lyY2xlIGN4PSI2NjQuMzEzNTkiIGN5PSI0NzUuMzk4NiIgcj0iNC4xMDAwOSIgZmlsbD0iIzZjNjNmZiIvPjxjaXJjbGUgY3g9IjY3Ny45ODA1NSIgY3k9IjQ3NS4zOTg2IiByPSI0LjEwMDA5IiBmaWxsPSIjNmM2M2ZmIi8+PHBhdGggZD0iTTYyNC4xNjk3NSw1NTUuNDc1MWE1MS42NTc1OCw1MS42NTc1OCwwLDAsMS0xMi44MDU5Mi0xLjY1NjUzbC0uODI0MzctLjIyODA5LS43NjQ3My0uMzgzMTVjLTI3LjY0NzQxLTEzLjg2MTg4LTUwLjk3Mi0zMi4xNzE0OC02OS4zMjYxMy01NC40MjA1MUEyMDYuMDE1NzYsMjA2LjAxNTc2LDAsMCwxLDUwNS40NDUsNDM2LjYzMTE5YTIzOS4yMzA1OCwyMzkuMjMwNTgsMCwwLDEtMTMuNTI4MTktODQuMjc0MTFjLjAxMTY2LS42MDE5MS4wMjE1Ny0xLjA2NjY2LjAyMTU3LTEuMzg2ODUsMC0xMy45MzkyMSw3LjczNzM1LTI2LjE2OTgzLDE5LjcxMTctMzEuMTU5MjIsOS4xNjQ2LTMuODE4NTksOTIuMzc0NzctMzcuOTk2MzIsOTguMzg2OTEtNDAuNDY1NzYsMTEuMzIyNS01LjY3MzMsMjMuNDAxNTctLjkzOCwyNS4zMzQ1Ny0uMTEsNC4zMzU4LDEuNzcyNzEsODEuMjU4NTIsMzMuMjM1LDk3Ljg4MTQ3LDQxLjE1MDQ0LDE3LjEzMTYxLDguMTU3ODksMjEuNzAyNSwyMi44MTMyNiwyMS43MDI1LDMwLjE4NjYxLDAsMzMuMzgxNTItNS43ODEzMiw2NC41NzkyMS0xNy4xODM0OCw5Mi43MjY1M2EyMTQuNzA4MzUsMjE0LjcwODM1LDAsMCwxLTM4LjU4NSw2Mi4xODM2MWMtMzEuNDk4MDYsMzUuNDQ2NDUtNjMuMDA0NDgsNDguMDEyNDEtNjMuMzA4NTUsNDguMTIzMTZBMzQuNDI3MDgsMzQuNDI3MDgsMCwwLDEsNjI0LjE2OTc1LDU1NS40NzUxWk02MTYuNzYwNDYsNTM3LjEyMmMyLjczMTU0LjYxMjQxLDkuMDIwMzUsMS41MzEsMTMuMTE5MzEuMDM1NzUsNS4yMDcxOS0xLjg5OSwzMS41Nzc1My0xNS41NzM1OSw1Ni4yMTk4NC00My4zMDQ3NCwzNC4wNDcxNy0zOC4zMTUsNTEuMzIyNzQtODYuNDgwMDgsNTEuMzQ4LTE0My4xNTc0OC0uMDYwODEtMS4xNDgyNi0uODc2MjUtOS4zMzgtMTEuNzIxNzctMTQuNTAyMjctMTYuMzAwNjItNy43NjIzMi05Ni4yNTUyNS00MC40NTU2Ni05Ny4wNjEtNDAuNzg1MThsLS4yMjA5LS4wOTM2NWMtMS42NzU1Ny0uNzAyMTYtNy4wMDgwOC0yLjE4MTExLTEwLjY4Mzg3LS4yNTQ5MWwtLjczNi4zNDMxMmMtLjg5MTIxLjM2Ni04OS4yMTk3OCwzNi42NDQ0NS05OC42NDAwNyw0MC41Njk1MS02LjU4OTc3LDIuNzQ1NzItOC45Mzc2OCw5LjU0Nzg1LTguOTM3NjgsMTQuOTk4MSwwLC4zOTgzLS4wMTAzLjk3NzY4LS4wMjQ4NywxLjcyNjQ3QzUwOC42Njg2MSwzOTEuNDc3NTcsNTE3LjY0MjcyLDQ4Ni45MDUxNSw2MTYuNzYwNDYsNTM3LjEyMloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iIzNmM2Q1NiIvPjxwYXRoIGQ9Ik02MTMuNjk4MjQsMjg3LjMwNXMtODkuMTMxMzgsMzYuNjA3NTMtOTguNjgxMTcsNDAuNTg2NjEtMTQuMzI0NjgsMTMuNTI4ODctMTQuMzI0NjgsMjMuMDc4NjZTNDkzLjUzLDQ4NS4xMzIsNjEzLjY5ODI0LDU0NS4zODE2N2MwLDAsMTAuOTA2NDQsMy4wMTc3MiwxOS4xODEsMHMxMTMuMzIyMzEtNTMuOTQ5OTMsMTEzLjMyMjMxLTE5NC44MDkzNGMwLDAsMC0xNC4zMjQ2OS0xNi43MTIxNC0yMi4yODI4NVM2MzEuOTc2OTQsMjg3LjMwNSw2MzEuOTc2OTQsMjg3LjMwNSw2MjIuMDU0MzEsMjgyLjkyOCw2MTMuNjk4MjQsMjg3LjMwNVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik02MjMuMjQ4LDMxNS45NTQzM1Y1MTEuMjQyNTRzODkuOTI3Mi00My4yODczNyw4OS4xMzEzOC0xNTcuMDg5WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTE4NyAtMTc5LjUpIiBvcGFjaXR5PSIwLjIiLz48cGF0aCBkPSJNMTAxMiw3MjAuNUgxODhhMSwxLDAsMCwxLDAtMmg4MjRhMSwxLDAsMCwxLDAsMloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xODcgLTE3OS41KSIgZmlsbD0iI2NiY2JjYiIvPjwvc3ZnPg=="},93126:(i,e,s)=>{s.d(e,{A:()=>M});const M=s.p+"assets/images/biscuit-verifier-creation-cfb3db4685a8f743cb39f7ec37190788.png"},60383:(i,e,s)=>{s.d(e,{A:()=>M});const M=s.p+"assets/images/biscuit-verifier-entity-config2-68c8f362f43bbf400cfb2c5f2cbdf032.png"},28453:(i,e,s)=>{s.d(e,{R:()=>r,x:()=>t});var M=s(96540);const c={},n=M.createContext(c);function r(i){const e=M.useContext(n);return M.useMemo((function(){return"function"==typeof i?i(e):{...e,...i}}),[e,i])}function t(i){let e;return e=i.disableParentContext?"function"==typeof i.components?i.components(c):i.components||c:r(i.components),M.createElement(n.Provider,{value:e},i.children)}}}]);