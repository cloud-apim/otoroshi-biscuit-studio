"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[618],{29953:(M,i,n)=>{n.r(i),n.d(i,{assets:()=>j,contentTitle:()=>D,default:()=>c,frontMatter:()=>t,metadata:()=>e,toc:()=>L});const e=JSON.parse('{"id":"plugins/clientcredentials","title":"Client Credentials plugin","description":"Client Credentials plugin","source":"@site/docs/plugins/clientcredentials.mdx","sourceDirName":"plugins","slug":"/plugins/clientcredentials","permalink":"/otoroshi-biscuit-studio/docs/plugins/clientcredentials","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":3,"frontMatter":{"sidebar_position":3},"sidebar":"tutorialSidebar","previous":{"title":"Attenuator plugin","permalink":"/otoroshi-biscuit-studio/docs/plugins/attenuators"},"next":{"title":"API Documentation","permalink":"/otoroshi-biscuit-studio/docs/api"}}');var s=n(74848),N=n(28453);n(89229);const t={sidebar_position:3},D="Client Credentials plugin",j={},L=[{value:"Demo",id:"demo",level:2},{value:"Setup the <code>client_credentials</code> plugin",id:"setup-the-client_credentials-plugin",level:3},{value:"Setup API route",id:"setup-api-route",level:3}];function T(M){const i={code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",img:"img",p:"p",pre:"pre",...(0,N.R)(),...M.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(i.header,{children:(0,s.jsx)(i.h1,{id:"client-credentials-plugin",children:"Client Credentials plugin"})}),"\n",(0,s.jsx)(i.p,{children:(0,s.jsx)(i.img,{alt:"Client Credentials plugin",src:n(4406).A+"",width:"574",height:"411"})}),"\n",(0,s.jsxs)(i.p,{children:["this plugin can be used implement the OAuth2 ",(0,s.jsx)(i.code,{children:"client_credentials"})," flow with the ",(0,s.jsx)(i.code,{children:"access_token"})," being a biscuit.\nthis plugin is of kind ",(0,s.jsx)(i.code,{children:"Backend"}),". It expects a ",(0,s.jsx)(i.code,{children:"POST"})," with a typical OAuth2 ",(0,s.jsx)(i.code,{children:"client_credentials"})," flow payload."]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-javascript",children:'{\n  "grant_type": "client_credentials",\n  "client_id": "apikey_client_id",\n  "client_secret": "apikey_client_secret",\n  "bearer_kind": "biscuit",\n  "aud": "https://api.foo.bar" // optional\n}\n'})}),"\n",(0,s.jsx)(i.h2,{id:"demo",children:"Demo"}),"\n",(0,s.jsxs)(i.p,{children:["let's try to implement a route protected by a biscuit tokens verifier where the token is issued by the ",(0,s.jsx)(i.code,{children:"client_credentials"})," plugin"]}),"\n",(0,s.jsxs)(i.h3,{id:"setup-the-client_credentials-plugin",children:["Setup the ",(0,s.jsx)(i.code,{children:"client_credentials"})," plugin"]}),"\n",(0,s.jsx)(i.p,{children:"first let's create an apikey"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/apim.otoroshi.io/v1/apikeys" \\\n  -d \'{\n  "_loc" : {\n    "tenant" : "default",\n    "teams" : [ "default" ]\n  },\n  "clientId" : "N5COev7kEpPBrbVg",\n  "clientSecret" : "iDba0Ahz2AMYU7Ao",\n  "clientName" : "test",\n  "description" : "",\n  "authorizedGroup" : null,\n  "authorizedEntities" : [ "route_bfda6474-0f81-4880-966e-8dae5c2683de", "route_4874704c-56a2-4460-9a21-ff8055a19c75" ],\n  "authorizations" : [ {\n    "kind" : "route",\n    "id" : "bfda6474-0f81-4880-966e-8dae5c2683de"\n  }, {\n    "kind" : "route",\n    "id" : "4874704c-56a2-4460-9a21-ff8055a19c75"\n  } ],\n  "enabled" : true,\n  "readOnly" : false,\n  "allowClientIdOnly" : false,\n  "throttlingQuota" : 10000000,\n  "dailyQuota" : 10000000,\n  "monthlyQuota" : 10000000,\n  "constrainedServicesOnly" : false,\n  "restrictions" : {\n    "enabled" : false,\n    "allowLast" : true,\n    "allowed" : [ ],\n    "forbidden" : [ ],\n    "notFound" : [ ]\n  },\n  "rotation" : {\n    "enabled" : false,\n    "rotationEvery" : 744,\n    "gracePeriod" : 168,\n    "nextSecret" : null\n  },\n  "validUntil" : null,\n  "tags" : [ ],\n  "metadata" : { }\n}\'\n'})}),"\n",(0,s.jsx)(i.p,{children:"then let's create a keypair"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs" \\\n  -d \'{\n  "id" : "biscuit-keypair_dev_d25612c6-b4d0-43ed-a711-16b6c09a5155",\n  "name" : "New Biscuit Key Pair",\n  "description" : "New biscuit KeyPair",\n  "metadata" : { },\n  "pubKey" : "771F9E7FE62784502FE34CE862220586D3DB637D6A5ABAD254F7330369D3B357",\n  "privKey" : "4379BE5B9AFA1A84F59D2417C20020EF1E47E0805945535B45616209D8867E50",\n  "tags" : [ ]\n}\'\n'})}),"\n",(0,s.jsx)(i.p,{children:"then let's create a forge"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-forges" \\\n  -d \'{\n  "id" : "biscuit-forge_dev_7580094c-47e0-495e-80fc-b9c9e8fb8129",\n  "name" : "New biscuit token",\n  "description" : "New biscuit token",\n  "metadata" : { },\n  "keypair_ref" : "biscuit-keypair_dev_d25612c6-b4d0-43ed-a711-16b6c09a5155",\n  "config" : {\n    "checks" : [ ],\n    "facts" : [ ],\n    "resources" : [ ],\n    "rules" : [ ]\n  },\n  "tags" : [ ],\n  "remoteFactsLoaderRef" : null\n}\'\n'})}),"\n",(0,s.jsxs)(i.p,{children:["and finally let's create a route that uses the ",(0,s.jsx)(i.code,{children:"client_credentials"})," plugin"]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/proxy.otoroshi.io/v1/routes" \\\n  -d \'{\n  "_loc" : {\n    "tenant" : "default",\n    "teams" : [ "default" ]\n  },\n  "id" : "4874704c-56a2-4460-9a21-ff8055a19c75",\n  "name" : "test route",\n  "description" : "test route",\n  "tags" : [ ],\n  "metadata" : { },\n  "enabled" : true,\n  "groups" : [ "default" ],\n  "bound_listeners" : [ ],\n  "frontend" : {\n    "domains" : [ "test.oto.tools/token" ],\n    "strip_path" : true,\n    "exact" : false,\n    "headers" : { },\n    "query" : { },\n    "methods" : [ ]\n  },\n  "backend" : {\n    "targets" : [ {\n      "id" : "www.otoroshi.io",\n      "hostname" : "www.otoroshi.io",\n      "port" : 443,\n      "tls" : true,\n      "weight" : 1,\n      "predicate" : {\n        "type" : "AlwaysMatch"\n      },\n      "protocol" : "HTTP/1.1",\n      "ip_address" : null,\n      "tls_config" : {\n        "certs" : [ ],\n        "trusted_certs" : [ ],\n        "enabled" : false,\n        "loose" : false,\n        "trust_all" : false\n      }\n    } ],\n    "root" : "/",\n    "rewrite" : false,\n    "load_balancing" : {\n      "type" : "RoundRobin"\n    }\n  },\n  "backend_ref" : null,\n  "plugins" : [ {\n    "enabled" : true,\n    "debug" : false,\n    "plugin" : "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.ClientCredentialBiscuitTokenEndpoint",\n    "include" : [ ],\n    "exclude" : [ ],\n    "config" : {\n      "expiration" : 21600000,\n      "forge_ref" : "biscuit-forge_dev_7580094c-47e0-495e-80fc-b9c9e8fb8129"\n    },\n    "bound_listeners" : [ ]\n  } ]\n}\'\n'})}),"\n",(0,s.jsxs)(i.p,{children:["now we can call this route to get an ",(0,s.jsx)(i.code,{children:"access_token"})]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST -H \'Content-Type: application/json\' "http://test.oto.tools:8080/token" -d \'{\n  "grant_type": "client_credentials",\n  "client_id": "apikey_client_id",\n  "client_secret": "apikey_client_secret",\n  "bearer_kind": "biscuit",\n  "aud": "http://test.oto.tools:8080"\n}\'\n'})}),"\n",(0,s.jsx)(i.p,{children:"the result of this call might look something like"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-json",children:'{\n  "access_token" : "EosCCqABCgljbGllbnRfaWQKEHdWc0R3bTJYWmxWd0RCQjkKC2NsaWVudF9uYW1lCgR0ZXN0CgNhdWQKG2h0dHA6Ly90ZXN0Lm90by50b29sczo1MDQ1MxgDIgoKCAiACBIDGIEIIgoKCAiCCBIDGIMIIgoKCAiECBIDGIUIMiYKJAoCCBsSBggFEgIIBRoWCgQKAggFCggKBiCi9ei8BgoEGgIIAhIkCAASIO_5F8o1lXRmahr6IPCxyW1X6Mu1Xsk_AsXtNYEySbTLGkDS_usafk7IFXbiXHwJao7_dFt_6CLB6k6dyK56PHP6Pbl-O9Jn3TxbYT4KNVgIW6DAjkHiisM8sB1YSXeTYqAFIiIKINaTmcs4QrNLiGZ45qvOn_ov589DIwNSfLhAeyiWj9bB",\n  "token_type" : "Bearer",\n  "expires_in" : 21600\n}\n'})}),"\n",(0,s.jsx)(i.h3,{id:"setup-api-route",children:"Setup API route"}),"\n",(0,s.jsx)(i.p,{children:"let's create a biscuit verifier that check if the biscuit token is valid and issued for the right domain with the right apikey"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-verifiers" \\\n  -d \'{\n  "enabled" : true,\n  "id" : "biscuit-verifier_dev_1808df91-1878-4be8-9510-c5ea19098852",\n  "keypair_ref" : "biscuit-keypair_dev_d25612c6-b4d0-43ed-a711-16b6c09a5155",\n  "name" : "New biscuit verifier",\n  "description" : "New biscuit verifier",\n  "metadata" : { },\n  "strict" : true,\n  "tags" : [ ],\n  "config" : {\n    "checks" : [ "check if client_id(\\"apikey_client_id\\")", "check if aud(\\"http://test.oto.tools:8080\\")" ],\n    "facts" : [ ],\n    "resources" : [ ],\n    "rules" : [ ],\n    "policies" : [ "allow if true" ],\n    "revokedIds" : [ ]\n  }\n}\'\n'})}),"\n",(0,s.jsx)(i.p,{children:"then let's create the route with the verifier"}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl -X POST \\\n  -H \'Content-Type: application/json\' \\\n  -H \'Authorization: Basic xxxx\' \\\n  "http://otoroshi-api.oto.tools:8080/apis/proxy.otoroshi.io/v1/routes" \\\n  -d \'{\n  "_loc" : {\n    "tenant" : "default",\n    "teams" : [ "default" ]\n  },\n  "id" : "bfda6474-0f81-4880-966e-8dae5c2683de",\n  "name" : "test route",\n  "description" : "test route",\n  "tags" : [ ],\n  "metadata" : { },\n  "enabled" : true,\n  "groups" : [ "default" ],\n  "bound_listeners" : [ ],\n  "frontend" : {\n    "domains" : [ "test.oto.tools/api" ],\n    "strip_path" : false,\n    "exact" : false,\n    "headers" : { },\n    "query" : { },\n    "methods" : [ ]\n  },\n  "backend" : {\n    "targets" : [ {\n      "id" : "mirror.otoroshi.io",\n      "hostname" : "mirror.otoroshi.io",\n      "port" : 443,\n      "tls" : true,\n      "weight" : 1,\n      "predicate" : {\n        "type" : "AlwaysMatch"\n      },\n      "protocol" : "HTTP/1.1",\n      "ip_address" : null,\n      "tls_config" : {\n        "certs" : [ ],\n        "trusted_certs" : [ ],\n        "enabled" : false,\n        "loose" : false,\n        "trust_all" : false\n      }\n    } ],\n    "root" : "/",\n    "rewrite" : false,\n    "load_balancing" : {\n      "type" : "RoundRobin"\n    },\n    "health_check" : null\n  },\n  "backend_ref" : null,\n  "plugins" : [ {\n    "enabled" : true,\n    "debug" : false,\n    "plugin" : "cp:otoroshi_plugins.com.cloud.apim.otoroshi.extensions.biscuit.plugins.BiscuitTokenValidator",\n    "include" : [ ],\n    "exclude" : [ ],\n    "config" : {\n      "verifier_ref" : "biscuit-verifier_dev_1808df91-1878-4be8-9510-c5ea19098852"\n    },\n    "bound_listeners" : [ ]\n  }, {\n      "enabled": true,\n      "debug": false,\n      "plugin": "cp:otoroshi.next.plugins.OverrideHost",\n      "include": [],\n      "exclude": [],\n      "config": {},\n      "bound_listeners": [],\n      "plugin_index": {\n        "transform_request": 1\n      }\n  } ]\n}\'\n'})}),"\n",(0,s.jsxs)(i.p,{children:["and now let call the route with the biscuit ",(0,s.jsx)(i.code,{children:"access_token"})]}),"\n",(0,s.jsx)(i.pre,{children:(0,s.jsx)(i.code,{className:"language-sh",children:'curl "http://test.oto.tools:8080/api" \\\n  -H \'Authorization: Bearer EosCCqABCgljbGllbnRfaWQKEHdWc0R3bTJYWmxWd0RCQjkKC2NsaWVudF9uYW1lCgR0ZXN0CgNhdWQKG2h0dHA6Ly90ZXN0Lm90by50b29sczo1MDQ1MxgDIgoKCAiACBIDGIEIIgoKCAiCCBIDGIMIIgoKCAiECBIDGIUIMiYKJAoCCBsSBggFEgIIBRoWCgQKAggFCggKBiCi9ei8BgoEGgIIAhIkCAASIO_5F8o1lXRmahr6IPCxyW1X6Mu1Xsk_AsXtNYEySbTLGkDS_usafk7IFXbiXHwJao7_dFt_6CLB6k6dyK56PHP6Pbl-O9Jn3TxbYT4KNVgIW6DAjkHiisM8sB1YSXeTYqAFIiIKINaTmcs4QrNLiGZ45qvOn_ov589DIwNSfLhAeyiWj9bB\'\n\n{\n  "method" : "GET",\n  "path" : "/api",\n  "headers" : {\n    "host" : "mirror.otoroshi.io",\n    "accept" : "*/*",\n    "user-agent" : "AHC/2.1",\n    "authorization" : "Bearer EosCCqABCgljbGllbnRfaWQKEHdWc0R3bTJYWmxWd0RCQjkKC2NsaWVudF9uYW1lCgR0ZXN0CgNhdWQKG2h0dHA6Ly90ZXN0Lm90by50b29sczo1MDQ1MxgDIgoKCAiACBIDGIEIIgoKCAiCCBIDGIMIIgoKCAiECBIDGIUIMiYKJAoCCBsSBggFEgIIBRoWCgQKAggFCggKBiCi9ei8BgoEGgIIAhIkCAASIO_5F8o1lXRmahr6IPCxyW1X6Mu1Xsk_AsXtNYEySbTLGkDS_usafk7IFXbiXHwJao7_dFt_6CLB6k6dyK56PHP6Pbl-O9Jn3TxbYT4KNVgIW6DAjkHiisM8sB1YSXeTYqAFIiIKINaTmcs4QrNLiGZ45qvOn_ov589DIwNSfLhAeyiWj9bB",\n    "x-forwarded-for" : "45.80.20.1",\n    "forwarded" : "proto=https;for=45.80.20.1:51029;by=91.208.207.223",\n    "x-forwarded-port" : "443",\n    "x-forwarded-proto" : "https",\n    "sozu-id" : "01JJRK7TERSK9776XHKM7590BS"\n  },\n  "body" : null\n}\n'})})]})}function c(M={}){const{wrapper:i}={...(0,N.R)(),...M.components};return i?(0,s.jsx)(i,{...M,children:(0,s.jsx)(T,{...M})}):T(M)}},89229:(M,i,n)=>{n(96540),n(74848)},4406:(M,i,n)=>{n.d(i,{A:()=>e});const e="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI1NzQuMTcwNzkiIGhlaWdodD0iNDExLjA4NDU3IiB2aWV3Qm94PSIwIDAgNTc0LjE3MDc5IDQxMS4wODQ1NyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHJvbGU9ImltZyIgYXJ0aXN0PSJLYXRlcmluYSBMaW1waXRzb3VuaSIgc291cmNlPSJodHRwczovL3VuZHJhdy5jby8iPjxwYXRoIGQ9Ik04NzAuMTA5ODMsMzY0LjkyMjI4SDU1NS45MTYyM2ExNi42OTU0MSwxNi42OTU0MSwwLDAsMC0xNi42ODAyOSwxNi42ODAzdjUwLjkxODc5YTE2LjY5NTQxLDE2LjY5NTQxLDAsMCwwLDE2LjY4MDI5LDE2LjY4MDI5aDMxNC4xOTM2YTE2LjY5NTQxLDE2LjY5NTQxLDAsMCwwLDE2LjY4MDI5LTE2LjY4MDI5VjM4MS42MDI1OEExNi42OTU0MSwxNi42OTU0MSwwLDAsMCw4NzAuMTA5ODMsMzY0LjkyMjI4WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjZjJmMmYyIi8+PHBhdGggZD0iTTQzOC45MTQ2LDY1NS41NDIyOWgtMTI1YTEsMSwwLDEsMSwwLTJoMTI1YTEsMSwwLDAsMSwwLDJaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiMzZjNkNTYiLz48cGF0aCBkPSJNNDIwLjU2MTExLDM0Mi40MzYxM2E5LjM3Nyw5LjM3NywwLDAsMC0xLjkyMjExLDE0LjI0OTQxbC05LjA4NzQ2LDE5LjQwNTU5LDEwLjQ0NTgxLDguMzk4NjUsMTIuNDQ4MzctMjcuNTg0NjlhOS40Mjc3OSw5LjQyNzc5LDAsMCwwLTExLjg4NDYxLTE0LjQ2OVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iI2ZmYjhiOCIvPjxwYXRoIGQ9Ik0zNjguMzM5MzgsMzUwLjMxMTA1bDAsMGExMy4wNTgxMiwxMy4wNTgxMiwwLDAsMSwxNy41ODI5NSwzLjk0MTU5bDE4LjU5NDc2LDI3Ljg0MjMxLDYuNDU1NDYtMTQuMjMxNDFhNCw0LDAsMCwxLDQuODM1NDEtMi4xNjU2OGw5Ljc0MzMyLDMuMDQzNTVhNCw0LDAsMCwxLDIuNTk2LDUuMTAxMWwtNi42Njk2MSwxOS42OTQzYTE3LjAxOTEzLDE3LjAxOTEzLDAsMCwxLTI1Ljc1ODQ1LDguNTY3NmwwLDBhMTcuMDE4ODQsMTcuMDE4ODQsMCwwLDEtMi43ODc0Mi0yLjM5NzIyTDM2NS41MjkzLDM3MC40Mjc3OUExMy4wNTgxMSwxMy4wNTgxMSwwLDAsMSwzNjguMzM5MzgsMzUwLjMxMTA1WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjY2NjIi8+PHBhdGggZD0iTTQyOC40OTI0NywzMjEuNTQyODdhMS44ODI0NSwxLjg4MjQ1LDAsMCwxLDEuODM1MTQsMS45Mjg1OWwtLjE2MDUxLDYuNDYyNjYuMzI0MDYuMDI5NTItLjExNjM0LDQuMzg5NC0uMzE2NTItLjAzOTUtLjU3NTA3LDIzLjE1MjQzYTMuMjY0NTMsMy4yNjQ1MywwLDAsMS0yLjkyMTc0LDMuMTY1NTRsLTE0LjcyMTgzLDEuNTQ5ODVhMi40NzEsMi40NzEsMCwwLDEtMi43MjQxNC0yLjYyNDQ0bDIuMjMyOC0zMi45Njc0MWEzLjA1NzQsMy4wNTc0LDAsMCwxLDIuNTgxMDctMi44MTQ1OWwxNC4yMTE5NC0yLjIwNzg2aDBBMS44ODAzOSwxLjg4MDM5LDAsMCwxLDQyOC40OTI0NywzMjEuNTQyODdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiMzZjNkNTYiLz48cGF0aCBkPSJNNDE3LjA5ODkzLDMyNC45NzQwOGw3LjQ3MTM1LTEuMTYwN2EuNjE4NzcuNjE4NzcsMCwwLDAsLjUyMDE2LS41NDQyMWgwYS43MDA4OS43MDA4OSwwLDAsMSwuNTg5MTQtLjYxNjQ0bDEuNTczNzktLjI0NDQ5YTEuNDg1MTYsMS40ODUxNiwwLDAsMSwxLjcxMjcsMS41MDMyNmwtLjgwODksMzMuNTY3MTFhMS42OTU1OSwxLjY5NTU5LDAsMCwxLTEuNTQ1LDEuNjQ4MWwtMTQuNzExLDEuMzA3OTFhMS4zMDkyNiwxLjMwOTI2LDAsMCwxLTEuNDIyNzgtMS4zODM1MWwxLjk4NDkzLTMyLjY2Mjc3YTIuMTEyOTMsMi4xMTI5MywwLDAsMSwxLjc4NDY5LTEuOTU5NzFsMS42NTktLjI1NzczQTEuMDU3NjksMS4wNTc2OSwwLDAsMCw0MTcuMDk4OTMsMzI0Ljk3NDA4WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjZmZmIi8+PHBvbHlnb24gcG9pbnRzPSI2OS41MjIgMzk4Ljk0NiA4MC45NTQgMzk4Ljk0NSA4Ni4zOTEgMzU0Ljg0OSA2OS41MTggMzU0Ljg1MSA2OS41MjIgMzk4Ljk0NiIgZmlsbD0iI2ZmYjhiOCIvPjxwYXRoIGQ9Ik0zNzkuOTg2ODksNjQwLjEzNjM4aDM1LjkyOTc1YTAsMCwwLDAsMSwwLDB2MTMuODgxOTVhMCwwLDAsMCwxLDAsMEgzOTMuODY4ODJhMTMuODgxOTMsMTMuODgxOTMsMCwwLDEtMTMuODgxOTMtMTMuODgxOTN2MEEwLDAsMCwwLDEsMzc5Ljk4Njg5LDY0MC4xMzYzOFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDQ4My4wNDczIDEwNDkuNjYxMDkpIHJvdGF0ZSgxNzkuOTk0ODMpIiBmaWxsPSIjMmYyZTQxIi8+PHBvbHlnb24gcG9pbnRzPSIzNC4zODYgMzk4Ljk0NiA0NS44MTggMzk4Ljk0NSA1MS4yNTUgMzU0Ljg0OSAzNC4zODIgMzU0Ljg1MSAzNC4zODYgMzk4Ljk0NiIgZmlsbD0iI2ZmYjhiOCIvPjxwYXRoIGQ9Ik0zNDQuODUxMTksNjQwLjEzNjM4aDM1LjkyOTc1YTAsMCwwLDAsMSwwLDB2MTMuODgxOTVhMCwwLDAsMCwxLDAsMEgzNTguNzMzMTJhMTMuODgxOTMsMTMuODgxOTMsMCwwLDEtMTMuODgxOTMtMTMuODgxOTN2MEEwLDAsMCwwLDEsMzQ0Ljg1MTE5LDY0MC4xMzYzOFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDQxMi43NzU5MSAxMDQ5LjY2NDI2KSByb3RhdGUoMTc5Ljk5NDgzKSIgZmlsbD0iIzJmMmU0MSIvPjxjaXJjbGUgY3g9IjY3Ljg2Njk2IiBjeT0iNjYuMzA1NzQiIHI9IjI0LjU2MTAzIiBmaWxsPSIjZmZiOGI4Ii8+PHBhdGggZD0iTTM5My40MjIxMSwzNzYuNDc2YTM3Ljg1OTA2LDM3Ljg1OTA2LDAsMCwwLTE4LjI3OTU4LTMxLjIxMjIzYy02LjM0NTMzLTMuNzM5MDgtMTMuMjkwODEtNS4xOTcyOC0xOC44MTY0OC4zMjgzOWEzOC4zNTQwOSwzOC4zNTQwOSwwLDAsMC03LjgzNiwxMS43NjExNCw1MC41MDUxLDUwLjUwNTEsMCwwLDAtLjkzNCwzNy40OTA0bDEzLjE1MTU0LDM2LjMzMzkxLDMwLjQzOTE5LDMuMjk3NThhNCw0LDAsMCwwLDQuNDI3NjItNC4xMzY0NFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iIzJmMmU0MSIvPjxwYXRoIGQ9Ik0zNjIuNjM3NTgsNDI2LjcwMzM5cy0yMi43NDEzNSw4LjQ4OTY5LTE2Ljc5NTY3LDQ0LjQzODUzYzUuMzM1MzIsMzIuMjU4MzgsMS4wOTU0MywxMzkuMzI5NzUuMTY4NjksMTYwLjk3MzM0YTMuOTk2MzMsMy45OTYzMywwLDAsMCwzLjY2NDMsNC4xNTM1MmwxMy4yNzUsMS4xMDYyNWE0LDQsMCwwLDAsNC4zMDg2OS0zLjU1MzI3bDYuODU1OTQtNjIuOTczNjlhMSwxLDAsMCwxLDEuOTkwNTkuMDI0MjJsNS4xMDg3MSw2MC41OTUyN2E0LDQsMCwwLDAsNC4xMTIzNiwzLjY2MmwxMS4xNTAxOC0uMzUyNzlhNCw0LDAsMCwwLDMuODcxMzUtNC4xMjk0NmwtNi41NjgyLTE5OS43NDVaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiMyZjJlNDEiLz48cGF0aCBkPSJNMzg1LjcxMDg0LDMwOS4xNDUyM2M4LjIxNDMyLDMuMzY4OTQsMTguNzA0MzktLjU2NjQ5LDIyLjY3NTg4LTguNTA3cy44Mjg0NC0xOC42OTQ2MS02Ljc5NDE4LTIzLjI0NjU5LTE4LjU4MS0yLjIxODc3LTIzLjY4ODEyLDUuMDQzNTdjLTQuMTU0NDMtNy40ODQ2LTE1LjAzNzktOS4zOTI3NC0yMi41MTAzMy01LjIxNjQ1cy0xMS41NDI2LDEyLjkyMzk0LTEyLjAyNTM0LDIxLjQ3MDYxLDIuMTE1NjQsMTYuOTY2Myw1LjI4NTM0LDI0LjkxODEzYzUuMTI3MTYsMTIuODYyNTcsMjEuNjMzLDE5Ljc4ODI4LDM0LjQwMzIyLDE0LjQzNTM1QzM3Ny4yMTExMSwzMjkuMzM1NTQsMzc4LjM2NTE4LDMxNi41OTMzNywzODUuNzEwODQsMzA5LjE0NTIzWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggaWQ9ImI3N2M2OTY1LWNkZmMtNDcxMy1iZDlhLTAzZmFmZDljN2M5OC0xMTQzIiBkYXRhLW5hbWU9IlBhdGggMzk1IiBkPSJNNDE4LjgxMjA1LDM0NS40MTRhMS4wMDc4MSwxLjAwNzgxLDAsMCwxLS42MDYyOS0uMjAxNDlsLS4wMTA4NC0uMDA4MTQtMi4yODM2LTEuNzQ2ODdhMS4wMTQ4MSwxLjAxNDgxLDAsMCwxLDEuMjM0ODctMS42MTA3NGwxLjQ3OTEzLDEuMTM0MjYsMy40OTUyNS00LjU2YTEuMDE0MzksMS4wMTQzOSwwLDAsMSwxLjQyMjIyLS4xODc4NWwuMDAwMjkuMDAwMjItLjAyMTY5LjAzMDEyLjAyMjI4LS4wMzAxMmExLjAxNTYsMS4wMTU2LDAsMCwxLC4xODc2MywxLjQyMjQ5bC00LjExMTE4LDUuMzYxMTFhMS4wMTUsMS4wMTUsMCwwLDEtLjgwNzE4LjM5NTc2WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjNmM2M2ZmIi8+PHBhdGggZD0iTTg3MC40MDUxLDI0NC40NTc3MUg1NTYuMjExNTFhMTYuNjk1NDMsMTYuNjk1NDMsMCwwLDAtMTYuNjgwMywxNi42ODAzVjMxMi4wNTY4YTE2LjY5NTQyLDE2LjY5NTQyLDAsMCwwLDE2LjY4MDMsMTYuNjgwMjlIODcwLjQwNTFhMTYuNjk1NDIsMTYuNjk1NDIsMCwwLDAsMTYuNjgwMy0xNi42ODAyOVYyNjEuMTM4QTE2LjY5NTQzLDE2LjY5NTQzLDAsMCwwLDg3MC40MDUxLDI0NC40NTc3MVptMTQuOTI0NDgsNjcuNTk5MDlhMTQuOTQ1NjUsMTQuOTQ1NjUsMCwwLDEtMTQuOTI0NDgsMTQuOTI0NDdINTU2LjIxMTUxQTE0Ljk0NTY1LDE0Ljk0NTY1LDAsMCwxLDU0MS4yODcsMzEyLjA1NjhWMjYxLjEzOGExNC45NDU2NSwxNC45NDU2NSwwLDAsMSwxNC45MjQ0OC0xNC45MjQ0OEg4NzAuNDA1MUExNC45NDU2NSwxNC45NDU2NSwwLDAsMSw4ODUuMzI5NTgsMjYxLjEzOFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iIzNmM2Q1NiIvPjxwYXRoIGQ9Ik02MjAuMTUwNCwzMTUuNjI5NTlhMjkuMDMyMjMsMjkuMDMyMjMsMCwxLDEsMjkuMDMyLTI5LjAzMjIzQTI5LjA2NTE5LDI5LjA2NTE5LDAsMCwxLDYyMC4xNTA0LDMxNS42Mjk1OVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik04MjcuNTM2MTUsMzEwLjMwMUg2NzYuNjMzODVhNy4wMjMyOCw3LjAyMzI4LDAsMSwxLDAtMTQuMDQ2NTZoMTUwLjkwMjNhNy4wMjMyOCw3LjAyMzI4LDAsMCwxLDAsMTQuMDQ2NTZaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiNjY2MiLz48cGF0aCBkPSJNNzI2LjU3NjQ4LDI4MC40NTJINjc2LjYzMzg1YTcuMDIzMjgsNy4wMjMyOCwwLDEsMSwwLTE0LjA0NjU2aDQ5Ljk0MjYzYTcuMDIzMjgsNy4wMjMyOCwwLDAsMSwwLDE0LjA0NjU2WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjY2NjIi8+PHBhdGggaWQ9ImI4MzdjNGUxLWUzMzYtNDM3MC1iYTljLWYwNmZiZDMzM2QzMy0xMTQ0IiBkYXRhLW5hbWU9IlBhdGggMzk1IiBkPSJNNjE4LjY2NzU2LDI5Ni44MDk0NmEzLjMyMDgyLDMuMzIwODIsMCwwLDEtMS45OTc4MS0uNjYzOTVsLS4wMzU3NC0uMDI2ODEtNy41MjQ3NS01Ljc1NjJhMy4zNDM5NSwzLjM0Mzk1LDAsMSwxLDQuMDY5MDUtNS4zMDc2MWw0Ljg3MzkzLDMuNzM3NTYsMTEuNTE3MzctMTUuMDI1NjhhMy4zNDI1MywzLjM0MjUzLDAsMCwxLDQuNjg2MzktLjYxOWwuMDAxLjAwMDcyLS4wNzE0OC4wOTkyNi4wNzM0Mi0uMDk5MjZhMy4zNDY1NywzLjM0NjU3LDAsMCwxLC42MTgyOCw0LjY4NzM0bC0xMy41NDY5NCwxNy42NjU2YTMuMzQ0NTgsMy4zNDQ1OCwwLDAsMS0yLjY1OTc2LDEuMzA0MDhaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiNmZmYiLz48cGF0aCBkPSJNNzM1LjQwNTEsMzgxLjQ1NzcxSDQyMS4yMTE1MWExNi42OTU0MywxNi42OTU0MywwLDAsMC0xNi42ODAzLDE2LjY4MDNWNDQ5LjA1NjhhMTYuNjk1NDIsMTYuNjk1NDIsMCwwLDAsMTYuNjgwMywxNi42ODAyOUg3MzUuNDA1MWExNi42OTU0MiwxNi42OTU0MiwwLDAsMCwxNi42ODAzLTE2LjY4MDI5VjM5OC4xMzhBMTYuNjk1NDMsMTYuNjk1NDMsMCwwLDAsNzM1LjQwNTEsMzgxLjQ1NzcxWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjZmZmIi8+PHBhdGggZD0iTTczNS40MDUxLDM4MS40NTc3MUg0MjEuMjExNTFhMTYuNjk1NDMsMTYuNjk1NDMsMCwwLDAtMTYuNjgwMywxNi42ODAzVjQ0OS4wNTY4YTE2LjY5NTQyLDE2LjY5NTQyLDAsMCwwLDE2LjY4MDMsMTYuNjgwMjlINzM1LjQwNTFhMTYuNjk1NDIsMTYuNjk1NDIsMCwwLDAsMTYuNjgwMy0xNi42ODAyOVYzOTguMTM4QTE2LjY5NTQzLDE2LjY5NTQzLDAsMCwwLDczNS40MDUxLDM4MS40NTc3MVptMTQuOTI0NDgsNjcuNTk5MDlhMTQuOTQ1NjUsMTQuOTQ1NjUsMCwwLDEtMTQuOTI0NDgsMTQuOTI0NDdINDIxLjIxMTUxQTE0Ljk0NTY1LDE0Ljk0NTY1LDAsMCwxLDQwNi4yODcsNDQ5LjA1NjhWMzk4LjEzOGExNC45NDU2NSwxNC45NDU2NSwwLDAsMSwxNC45MjQ0OC0xNC45MjQ0OEg3MzUuNDA1MUExNC45NDU2NSwxNC45NDU2NSwwLDAsMSw3NTAuMzI5NTgsMzk4LjEzOFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iIzNmM2Q1NiIvPjxwYXRoIGQ9Ik00ODUuMTUwNCw0NTIuNjI5NTlhMjkuMDMyMjMsMjkuMDMyMjMsMCwxLDEsMjkuMDMyLTI5LjAzMjIzQTI5LjA2NTE5LDI5LjA2NTE5LDAsMCwxLDQ4NS4xNTA0LDQ1Mi42Mjk1OVoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0zMTIuOTE0NiAtMjQ0LjQ1NzcxKSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik02OTIuNTM2MTUsNDQ3LjMwMUg1NDEuNjMzODVhNy4wMjMyOCw3LjAyMzI4LDAsMSwxLDAtMTQuMDQ2NTZoMTUwLjkwMjNhNy4wMjMyOCw3LjAyMzI4LDAsMCwxLDAsMTQuMDQ2NTZaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiNjY2MiLz48cGF0aCBkPSJNNTkxLjU3NjQ4LDQxNy40NTJINTQxLjYzMzg1YTcuMDIzMjgsNy4wMjMyOCwwLDEsMSwwLTE0LjA0NjU2aDQ5Ljk0MjYzYTcuMDIzMjgsNy4wMjMyOCwwLDAsMSwwLDE0LjA0NjU2WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjY2NjIi8+PHBhdGggaWQ9ImFlMTAzZTFhLWExZDAtNDM2Ni1iZGJmLWFkYWFjNTU2NzYzMC0xMTQ1IiBkYXRhLW5hbWU9IlBhdGggMzk1IiBkPSJNNDgzLjY2NzU2LDQzMy44MDk0NmEzLjMyMDgyLDMuMzIwODIsMCwwLDEtMS45OTc4MS0uNjYzOTVsLS4wMzU3NC0uMDI2ODEtNy41MjQ3NS01Ljc1NjJhMy4zNDM5NSwzLjM0Mzk1LDAsMSwxLDQuMDY5MDUtNS4zMDc2MWw0Ljg3MzkzLDMuNzM3NTYsMTEuNTE3MzctMTUuMDI1NjhhMy4zNDI1MywzLjM0MjUzLDAsMCwxLDQuNjg2MzktLjYxOWwuMDAxLjAwMDcyLS4wNzE0OC4wOTkyNi4wNzM0Mi0uMDk5MjZhMy4zNDY1NywzLjM0NjU3LDAsMCwxLC42MTgyOCw0LjY4NzM0bC0xMy41NDY5NCwxNy42NjU2YTMuMzQ0NTgsMy4zNDQ1OCwwLDAsMS0yLjY1OTc2LDEuMzA0MDhaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMzEyLjkxNDYgLTI0NC40NTc3MSkiIGZpbGw9IiNmZmYiLz48cGF0aCBkPSJNNDExLjAyMzA4LDM5Mi45NDM4OGE5LjM3NjkyLDkuMzc2OTIsMCwwLDAtMTIuNzk2MjMsNi41NTcxOGwtMjEuMTIwMzMsMy42MTgxMS0uOTMxNjUsMTMuMzcxLDI5Ljc1NDIyLTUuNTI4MzhhOS40Mjc3OSw5LjQyNzc5LDAsMCwwLDUuMDk0LTE4LjAxNzkzWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjZmZiOGI4Ii8+PHBhdGggZD0iTTM3NC43NDM0MiwzNTQuNTY1MzhsMCwwYTEzLjA1ODEyLDEzLjA1ODEyLDAsMCwxLDYuODAyMzQsMTYuNjg2MDVsLTEyLjI0MjY4LDMxLjE2MjEsMTUuMzY5NjMtMi44MjVhNCw0LDAsMCwxLDQuNTM4NjYsMi43MzM0OGwzLjA2Mzg2LDkuNzM2OTVhNCw0LDAsMCwxLTIuNzA2LDUuMDQzNjNsLTE5Ljk3Njk1LDUuNzY4YTE3LjAxOTEzLDE3LjAxOTEzLDAsMCwxLTIxLjc0LTE2LjI1NjQ4djBhMTcuMDE4OTEsMTcuMDE4OTEsMCwwLDEsLjM3Njc0LTMuNjU3MWw4LjM5NC0zOS4yMTM3NUExMy4wNTgxMSwxMy4wNTgxMSwwLDAsMSwzNzQuNzQzNDIsMzU0LjU2NTM4WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTMxMi45MTQ2IC0yNDQuNDU3NzEpIiBmaWxsPSIjY2NjIi8+PC9zdmc+"},28453:(M,i,n)=>{n.d(i,{R:()=>t,x:()=>D});var e=n(96540);const s={},N=e.createContext(s);function t(M){const i=e.useContext(N);return e.useMemo((function(){return"function"==typeof M?M(i):{...i,...M}}),[i,M])}function D(M){let i;return i=M.disableParentContext?"function"==typeof M.components?M.components(s):M.components||s:t(M.components),e.createElement(N.Provider,{value:i},M.children)}}}]);