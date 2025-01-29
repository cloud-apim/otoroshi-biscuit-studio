"use strict";(self.webpackChunkdocumentation=self.webpackChunkdocumentation||[]).push([[179],{48685:(M,N,j)=>{j.r(N),j.d(N,{assets:()=>s,contentTitle:()=>e,default:()=>y,frontMatter:()=>D,metadata:()=>i,toc:()=>z});const i=JSON.parse('{"id":"entities/keypairs","title":"KeyPairs","description":"Biscuit Auth tokens are flexible, decentralized, and cryptographically secure authorization tokens.","source":"@site/docs/entities/keypairs.mdx","sourceDirName":"entities","slug":"/entities/keypairs","permalink":"/otoroshi-biscuit-studio/docs/entities/keypairs","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":1,"frontMatter":{"sidebar_position":1},"sidebar":"tutorialSidebar","previous":{"title":"Entities","permalink":"/otoroshi-biscuit-studio/docs/category/entities"},"next":{"title":"Verifiers","permalink":"/otoroshi-biscuit-studio/docs/entities/verifiers"}}');var L=j(74848),T=j(28453);j(89229);const D={sidebar_position:1},e="KeyPairs",s={},z=[{value:"Create your first Biscuit KeyPair",id:"create-your-first-biscuit-keypair",level:2},{value:"Example Keypair :",id:"example-keypair-",level:2},{value:"Private Key:",id:"private-key",level:3},{value:"Public Key:",id:"public-key",level:3},{value:"Example",id:"example",level:2},{value:"Create a keypair with Otoroshi&#39;s API",id:"create-a-keypair-with-otoroshis-api",level:2},{value:"Get a KeyPair template with Otoroshi&#39;s API",id:"get-a-keypair-template-with-otoroshis-api",level:2},{value:"Create bulk KeyPairs with Otoroshi&#39;s API",id:"create-bulk-keypairs-with-otoroshis-api",level:2}];function c(M){const N={a:"a",code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",img:"img",p:"p",pre:"pre",...(0,T.R)(),...M.components};return(0,L.jsxs)(L.Fragment,{children:[(0,L.jsx)(N.header,{children:(0,L.jsx)(N.h1,{id:"keypairs",children:"KeyPairs"})}),"\n",(0,L.jsx)(N.p,{children:(0,L.jsx)(N.img,{src:j(53487).A+"",width:"924",height:"531"})}),"\n",(0,L.jsxs)(N.p,{children:[(0,L.jsx)(N.a,{href:"https://doc.biscuitsec.org/",children:"Biscuit Auth tokens"})," are flexible, decentralized, and cryptographically secure authorization tokens."]}),"\n",(0,L.jsx)(N.p,{children:"They use ED25519 keypairs for digital signature generation and verification, ensuring integrity and authenticity."}),"\n",(0,L.jsx)(N.p,{children:"Each token is signed with a private ED25519 key and can be verified using the corresponding public key."}),"\n",(0,L.jsx)(N.p,{children:"This cryptographic mechanism guarantees that tokens cannot be tampered with or forged."}),"\n",(0,L.jsx)(N.p,{children:"A Biscuit KeyPair is a couple of a Public Key and a Private Key using ED25519 algorithm."}),"\n",(0,L.jsx)(N.h2,{id:"create-your-first-biscuit-keypair",children:"Create your first Biscuit KeyPair"}),"\n",(0,L.jsx)(N.p,{children:"To create your first Biscuit KeyPair open your Otoroshi UI interface and go to Categories > Biscuit Studio > Biscuit KeyPairs"}),"\n",(0,L.jsx)(N.p,{children:'Then, click on "Add item" top right button to display the entity form.'}),"\n",(0,L.jsx)(N.p,{children:(0,L.jsx)(N.img,{src:j(63527).A+"",width:"2830",height:"1626"})}),"\n",(0,L.jsx)(N.p,{children:'You can also generate new KeyPair by clicking on the "Generate new" button.'}),"\n",(0,L.jsx)(N.p,{children:"It will create a public and a private key."}),"\n",(0,L.jsx)(N.p,{children:(0,L.jsx)(N.img,{src:j(78146).A+"",width:"2830",height:"1626"})}),"\n",(0,L.jsx)(N.h2,{id:"example-keypair-",children:"Example Keypair :"}),"\n",(0,L.jsx)(N.h3,{id:"private-key",children:"Private Key:"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-txt",children:"0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619\n"})}),"\n",(0,L.jsx)(N.h3,{id:"public-key",children:"Public Key:"}),"\n",(0,L.jsx)(N.p,{children:"The public key is derived from the private key."}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-txt",children:"cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb\n"})}),"\n",(0,L.jsx)(N.h2,{id:"example",children:"Example"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-javascript",children:'{\n  "id": "biscuit_keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "name": "My Biscuit KeyPair",\n  "description": "A simple ED25519 Biscuit KeyPair",\n  "pubKey": "cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb",\n  "privKey": "0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619",\n  "tags": [],\n  "kind": "BiscuitKeyPair"\n}\n'})}),"\n",(0,L.jsx)(N.h2,{id:"create-a-keypair-with-otoroshis-api",children:"Create a keypair with Otoroshi's API"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/json\' \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs\' -u admin-api-apikey-id:admin-api-apikey-secret -d \'{\n  "id": "biscuit-keypair_e42033bc-f181-485f-857d-576e4728f6f9",\n  "name": "KeyPair from Otoroshi API",\n  "description": "A Biscuit KeyPair created from Otoroshi API",\n  "pubKey": "cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb",\n  "privKey": "0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619",\n  "tags": [],\n  "kind": "biscuit.extensions.cloud-apim.com/BiscuitKeyPair"\n}\'\n'})}),"\n",(0,L.jsx)(N.h2,{id:"get-a-keypair-template-with-otoroshis-api",children:"Get a KeyPair template with Otoroshi's API"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-bash",children:"curl -X GET -H 'Content-Type: application/json' 'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs/_template' -u admin-api-apikey-id:admin-api-apikey-secret\n"})}),"\n",(0,L.jsx)(N.p,{children:"Result :"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-js",children:'{\n  "id": "biscuit-keypair_b6f88449-9c1a-4e46-a3af-b42e00f14e60",\n  "name": "New Biscuit Key Pair",\n  "description": "New biscuit KeyPair",\n  "metadata": {},\n  "pubKey": "",\n  "privKey": "",\n  "tags": []\n}\n'})}),"\n",(0,L.jsx)(N.h2,{id:"create-bulk-keypairs-with-otoroshis-api",children:"Create bulk KeyPairs with Otoroshi's API"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-bash",children:'curl -X POST -H \'Content-Type: application/x-ndjson\' \'http://otoroshi-api.oto.tools:8080/apis/biscuit.extensions.cloud-apim.com/v1/biscuit-keypairs/_bulk\' -u admin-api-apikey-id:admin-api-apikey-secret -d \'{"id":"bulk_keypair1","name":"KeyPair from Otoroshi API Bulk 1","description":"A Biscuit KeyPair created from Otoroshi API","pubKey":"cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb","privKey":"0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619","tags":[],"kind":"biscuit.extensions.cloud-apim.com/BiscuitKeyPair"}\n{"id":"bulk_keypair2","name":"KeyPair from Otoroshi API Bulk 2","description":"A Biscuit KeyPair created from Otoroshi API","pubKey":"cc9f2638b2aa05ffe72a85f91875ac451ddc8995c8ddc39290fdaeb473314dcb","privKey":"0e8a4d1cf07b6ee07b12f7658b6e784b590da13b97ab5c0140764a84373c8619","tags":[],"kind":"biscuit.extensions.cloud-apim.com/BiscuitKeyPair"}\n\'\n'})}),"\n",(0,L.jsx)(N.p,{children:"Result"}),"\n",(0,L.jsx)(N.pre,{children:(0,L.jsx)(N.code,{className:"language-js",children:'{"status":201,"created":true,"id":"bulk_keypair1","id_field":"id"}\n{"status":201,"created":true,"id":"bulk_keypair2","id_field":"id"}\n'})})]})}function y(M={}){const{wrapper:N}={...(0,T.R)(),...M.components};return N?(0,L.jsx)(N,{...M,children:(0,L.jsx)(c,{...M})}):c(M)}},89229:(M,N,j)=>{j(96540),j(74848)},53487:(M,N,j)=>{j.d(N,{A:()=>i});const i="data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSI5MjQuMjU1NzEiIGhlaWdodD0iNTMwLjU5MTQxIiB2aWV3Qm94PSIwIDAgOTI0LjI1NTcxIDUzMC41OTE0MSIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHJvbGU9ImltZyIgYXJ0aXN0PSJLYXRlcmluYSBMaW1waXRzb3VuaSIgc291cmNlPSJodHRwczovL3VuZHJhdy5jby8iPjxwYXRoIGQ9Ik0xNTMuOTk4MzksMzQzLjk2NjQ2YTEyLjQxNTY3LDEyLjQxNTY3LDAsMCwxLTE1LjgyMDIzLTE0LjU4Nzc5YzcuMjIzNy0zMC4wNTI2MSwzNC42OTE2MS0xMDMuNTQ2NDIsMTM0Ljc1ODEtMTM0LjQ1Nzg0LDQzLjM0NTIyLTEzLjM4OTczLDg0LjkxMzU3LTEzLjYxODMsMTIzLjQ5OTktLjY4NDQyLDMxLjc0MzU4LDEwLjU5NjMsNTIuODE2NjYsMjcuMTc4MTcsNjIuODk2OSwzNS4xOTI3NmExMi4zMTM0MSwxMi4zMTM0MSwwLDAsMSwyLjAxNjg0LDE3LjI5NjZxLS4wMzc2OS4wNDc1Ni0uMDc1NzguMDk0NzhhMTIuNjAzMzksMTIuNjAzMzksMCwwLDEtMTcuNTYwNjgsMS45OTMzN2MtMTguMjYyOTItMTQuMzg5NjUtNzMuNjg0MTktNTguMTA1MDctMTYzLjUwNy0zMC4zNThDMTkyLjUwMDEyLDI0NS41NDkxNywxNjguNjc1NTksMzA4LjkyMiwxNjIuNDk2MTMsMzM0Ljg0OThhMTIuMjMxLDEyLjIzMSwwLDAsMS04LjQ5NzcyLDkuMTE2NjNaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTM3Ljg3MjE0IC0xODQuNzA0MykiIGZpbGw9IiM2YzYzZmYiLz48cGF0aCBkPSJNNDI2LjczODQ1LDUxNC4xODUzM2ExMi4zNDYzNywxMi4zNDYzNywwLDAsMS0zLjA2NzY2LjU3NjY2Yy03OC4xNzUwNiw0LjIxMDQ4LTEyNy45MTMyNi02MC43MzUyOS0xMjkuOTU1MzEtNjMuNDQzbC0uMzc4NTQtLjYyNWMtMS4yNDY0MS0xLjkzMzQtMzEuMzQxODQtNDcuNzIyNTEtMTguMDkyODEtODQuMDg3NzksNi4wODU1Ny0xNi42MjUwOCwxOS41NDQ0NS0yOC4yOTQzNyw0MC4xMTY0OC0zNC42NDkyNSwxOS4xMzI4Ni01LjkxMDMxLDM0Ljc4OS00LjE2MjI4LDQ4LjExNTA2LDUuMzUzNTIsMTAuOTI2ODIsNy43NTMwNiwxNy45Mjk2NCwxOS4zMTQ5MiwyNC43MTcwNiwzMC40Nzk2NSwxNC4xMTY0OCwyMy4xODIyMywyMi43Njc3MywzNC45NzY3OCw0OS41OTk2NSwyOC4xNzE5NCwxMS43ODEzLTIuOTkwMTgsMTcuMjgyMjctMTIuMjAxMjIsMTkuNzk1MzgtMTkuMzc2MzcsNi43NjYzNC0xOS41MjQ3NS43Njk1Ni00Ni4xNDI2MS0xNS4wOTU5NC02Ni4yODA2NC0yMC40OTczNy0yNi4xMjYxNy03Ny44NTk1OC02OS41MjAyNS0xNDguNjM0MTktNDcuNjU3MzQtMzAuMjIzMTMsOS4zMzYxOS01NC45NTc1NCwyNy43MzQzOC03MS41MjE2Niw1My4wNjc4Ny0xMy43MjIxLDIxLjAyNDMtMjEuNjk5MzMsNDcuMTM2NTEtMjEuODc0MjIsNzEuNDg3NjUtLjI3NTM1LDQ1LjM0MDc2LDM2Ljg4NDExLDEwNC45OTEyNywzNy4yMzY0OSwxMDUuNTMxNTlhMTIuMjkwMjEsMTIuMjkwMjEsMCwwLDEtMy45MDU3MywxNi45MzY1cS0uMDU2OTQuMDM1NjktLjExNDMyLjA3MDY0YTEyLjU4NDIxLDEyLjU4NDIxLDAsMCwxLTE3LjIzNS0zLjg1N2MtMS42NzcyMy0yLjcyNzY5LTQxLjM1NTY2LTY2LjMyOTY0LTQxLjAzNi0xMTguNzMyMTMuNDQ1LTU2Ljc5OTg5LDM0Ljk2MTM2LTEyNC41ODgzNSwxMTEuMTU0MTEtMTQ4LjEyNSwzNS4yMTgtMTAuODc5MTUsNzIuMjI4NTEtOS4wNTA2MywxMDcuMDU3NTQsNS4yMjk0LDI2Ljk5NTE2LDExLjEzNTc1LDUxLjk3OSwyOS41Njk5Miw2OC40OTY4OSw1MC42MTkyNCwyMS4wOTc1MywyNi44NjgxNSwyOC41MzA0Nyw2Mi4wMzc4NSwxOC45MjYxMiw4OS40ODczNC02LjQwMjkxLDE4LjI5OTYzLTE5LjY1ODc4LDMwLjkyNjMyLTM3LjI3NTMyLDM1LjQ0MDg1LTQ1LjkxMzc5LDExLjY3OTI2LTYzLjkwMDI0LTE3LjcyNjQ1LTc3LjAxODA5LTM5LjE3Njk0LTEzLjQ3MDI0LTIxLjk5MDgxLTIwLjkyMTE1LTMyLjMwMTM4LTQ0LjIwMjI3LTI1LjEwOTY0LTEyLjc4MzQ2LDMuOTQ4OTMtMjAuNjU4OSwxMC4yNzY2OS0yMy45NzExNCwxOS4zNjgtNC41MDcyMiwxMi40MjgtLjIzNTYsMjguMzU3Niw0LjE0MTQ4LDM5LjUyNXEuMDY4LjE3NTEzLjEzNjMyLjM0OTQxYTEwMC45NDYyNCwxMDAuOTQ2MjQsMCwwLDAsMjUuNjEzMTksMzcuMjUyNWMxOC4wMTIxMywxNi42MzM1OSw1MS40NTEwOCw0MC4zNDcsOTQuMDY5ODYsMzguMDYwMzlhMTIuMzA4NjIsMTIuMzA4NjIsMCwwLDEsMTMuMDQ4MDksMTEuNTIxNzZxLjAwMzY2LjA1OS4wMDY3Ni4xMTgwOGExMi41Nzg4NywxMi41Nzg4NywwLDAsMS04Ljg1MjMxLDEyLjQ3MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xMzcuODcyMTQgLTE4NC43MDQzKSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik0zMzEuOTQwMjcsNTM3LjUzNDE4YTEyLjYxNTU0LDEyLjYxNTU0LDAsMCwxLTkuODcwNTEtMS4wMzEzNmMtMzguNTU3MTUtMjEuODQ1Ny02NS42MjQ3LTUxLjIyODI2LTg1LjExNi05Mi40MTA0M2wtLjA3ODQ1LS4yNTRjLTEyLjI4NDE3LTI3LjQ1Nzc0LTE5LjY3OS02OS40MDktMy4zNjcyMi0xMDQuODY1NjUsMTIuMDQyMTktMjYuMTYyMzQsMzQuNjQ2NC00NC41NTE2NCw2Ny4wNzA2NC01NC41Njc3NywzOC4zNTAzNC0xMS44NDY3Niw3NC4wNTkxNC0zLjEyNDUzLDEwMy4zNjYzOSwyNS4xMDI1NEExNTMuMTcwNDUsMTUzLjE3MDQ1LDAsMCwxLDQzNS45MjkxNSwzNTQuODA2YTEyLjUxNjQ2LDEyLjUxNjQ2LDAsMCwxLTIzLjAzMDY3LDkuODAzNzUsMTI4LjI2MDY5LDEyOC4yNjA2OSwwLDAsMC0yNi45NjYxMi0zNy43NjAyOWMtMjIuNTg5ODktMjEuNDkyMS00OC44NTAxMS0yNy44NDctNzguMTQyLTE4Ljc5ODUzLTI1LjMxMjkyLDcuODE5MzgtNDIuNzQzMjIsMjEuNjQyODQtNTEuNjY1OTQsNDEuMDkxODMtMTIuODY3NDksMjcuOTkzOC02LjI0MjU3LDYyLjk0OTQxLDMuMzcwNiw4NC40NjI0NCwxNy4xODY0NSwzNi40MjI2Nyw0MS4wMjU0Miw2Mi4yNTg0OCw3NC45NjA5LDgxLjQ1MTM5YTEyLjI1NTMxLDEyLjI1NTMxLDAsMCwxLDQuNjU0NTgsMTYuNjk0OTJxLS4wNDcuMDgzMjgtLjA5NTI5LjE2NTlBMTIuNTY4NjEsMTIuNTY4NjEsMCwwLDEsMzMxLjk0MDI3LDUzNy41MzQxOFoiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xMzcuODcyMTQgLTE4NC43MDQzKSIgZmlsbD0iIzZjNjNmZiIvPjxjaXJjbGUgY3g9IjE5OC4xMjg0OCIgY3k9IjIxMy43NjgyMSIgcj0iMTUuOTQ5MDUiIGZpbGw9IiNlNmU2ZTYiLz48cGF0aCBkPSJNNjIzLjU3NTU5LDU5My4yMjQ0N2E2LjU2OTc0LDYuNTY5NzQsMCwwLDEtLjk4NDM3LS4wNzQyMSw2LjQ1OTI0LDYuNDU5MjQsMCwwLDEtNC4yNjA3NS0yLjU2OTM0TDYwOS41Njk3Myw1NzguNzAyYTYuNDk4NTMsNi40OTg1MywwLDAsMSwxLjM3My05LjA4ODg2bDc0Ljg5OTQxLTU1LjI0MDI0YTYuNDk5ODksNi40OTk4OSwwLDAsMSw5LjA4OTg1LDEuMzczbDguNzU5NzYsMTEuODc3OTNhNi41MDA4Nyw2LjUwMDg3LDAsMCwxLTEuMzcyMDcsOS4wODk4NEw2MjcuNDIwMzIsNTkxLjk1NEE2LjQ1Mzc0LDYuNDUzNzQsMCwwLDEsNjIzLjU3NTU5LDU5My4yMjQ0N1oiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xMzcuODcyMTQgLTE4NC43MDQzKSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik02NzYuNzY4NzYsNTQ4Ljc0ODM0LDY2NS4zMDUyNCw1MzMuMjA1MWE2LDYsMCwwLDEsMS4yNjc0My04LjM5MDA5bDQzLjg2MTA2LTM4LjQyOTkzYTYsNiwwLDAsMSw4LjM5MDA5LDEuMjY3NDNsMjAuOTYzNTIsMjUuMDQzMjRhNiw2LDAsMCwxLTEuMjY3NDMsOC4zOTAwOWwtNTMuMzYxMDYsMjguOTI5OTNBNiw2LDAsMCwxLDY3Ni43Njg3Niw1NDguNzQ4MzRaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTM3Ljg3MjE0IC0xODQuNzA0MykiIGZpbGw9IiMyZjJlNDEiLz48cGF0aCBkPSJNNzQ3LjI0NTUxLDcxNC45NTU5Mkg3MzIuNDg2NzJhNi41MDc1Myw2LjUwNzUzLDAsMCwxLTYuNS02LjVWNTg1LjQ0MTI3YTYuNTA3NTMsNi41MDc1MywwLDAsMSw2LjUtNi41aDE0Ljc1ODc5YTYuNTA3NTMsNi41MDc1MywwLDAsMSw2LjUsNi41VjcwOC40NTU5MkE2LjUwNzUzLDYuNTA3NTMsMCwwLDEsNzQ3LjI0NTUxLDcxNC45NTU5MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xMzcuODcyMTQgLTE4NC43MDQzKSIgZmlsbD0iIzZjNjNmZiIvPjxwYXRoIGQ9Ik03NzYuNDM3OSw3MTQuOTU1OTJINzYxLjY3ODEzYTYuNTA3NTMsNi41MDc1MywwLDAsMS02LjUtNi41VjU4NS40NDEyN2E2LjUwNzUzLDYuNTA3NTMsMCwwLDEsNi41LTYuNUg3NzYuNDM3OWE2LjUwNzUzLDYuNTA3NTMsMCwwLDEsNi41LDYuNVY3MDguNDU1OTJBNi41MDc1Myw2LjUwNzUzLDAsMCwxLDc3Ni40Mzc5LDcxNC45NTU5MloiIHRyYW5zZm9ybT0idHJhbnNsYXRlKC0xMzcuODcyMTQgLTE4NC43MDQzKSIgZmlsbD0iIzZjNjNmZiIvPjxyZWN0IHg9IjQ1Mi45MjI2OSIgeT0iNTI4LjM1MDY4IiB3aWR0aD0iMzI0LjAzMjYxIiBoZWlnaHQ9IjIuMjQwNzIiIGZpbGw9IiMzZjNkNTYiLz48Y2lyY2xlIGN4PSI2MTAuMjE2NDUiIGN5PSIxOTUuNzQyNDciIHI9IjUzLjUxOTE2IiBmaWxsPSIjNmM2M2ZmIi8+PHBhdGggZD0iTTgzMC45NDQ0OSw2NTEuODc5MjYsNzk5Ljk1MDQxLDUxNC4yMzM3NVY0ODEuNzEyMjdhOC4wMDAyLDguMDAwMiwwLDAsMC04LThINzYyLjI1OTkyVjQ1Mi43OTkxOGE3Ljk5OTg5LDcuOTk5ODksMCwwLDAtOC04aC0xNmE3Ljk5OTg4LDcuOTk5ODgsMCwwLDAtOCw4djIwLjkxMzA5SDcwMC41Njk0M2E4LjAwMDIsOC4wMDAyLDAsMCwwLTgsOFY2NTIuNzk5MThhNy45NjgxNCw3Ljk2ODE0LDAsMCwwLC41ODQyOSwyLjk5MjY4LDcuODM0MjgsNy44MzQyOCwwLDAsMCw3LjM3NzEzLDYuMjA2NTRMODI1Ljk1NTM1LDY2Ni4xOEM4MzMuNDUxMiw2NjYuMzc2ODIsODM2Ljg1MjQ1LDY1Ni42Mjc3OSw4MzAuOTQ0NDksNjUxLjg3OTI2WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTEzNy44NzIxNCAtMTg0LjcwNDMpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTc0OC42MDI5NCwzOTkuNDYwOGMtMy4zMDU2Ny0uMDkyNzctNy40MTk5My0uMjA4LTEwLjU4ODg3LTIuNTIzNDRhOC4xMzE0OCw4LjEzMTQ4LDAsMCwxLTMuMjAwMi02LjA3MDMxLDUuNDcxMzIsNS40NzEzMiwwLDAsMSwxLjg2MDM1LTQuNDk1MTJjMS42NTcyMy0xLjM5ODQzLDQuMDc2MTgtMS43MjU1OCw2LjY3Nzc0LS45NjA5M2wtMi42OTkyMi0xOS43MjY1NywxLjk4MjQyLS4yNzE0OCwzLjE3MTg4LDIzLjE5MDQzLTEuNjU0My0uNzU4NzljLTEuOTE4OTUtLjg4MDg2LTQuNTUxNzYtMS4zMjgxMi02LjE4NzUuMDU0NjlhMy41MTMzNiwzLjUxMzM2LDAsMCwwLTEuMTUyMzQsMi44OTY0OCw2LjE0MzM1LDYuMTQzMzUsMCwwLDAsMi4zODA4Niw0LjUyNjM3YzIuNDY2NzksMS44MDE3Niw1Ljc0NjA5LDIuMDM1MTYsOS40NjU4MiwyLjEzODY3WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTEzNy44NzIxNCAtMTg0LjcwNDMpIiBmaWxsPSIjMmYyZTQxIi8+PHJlY3QgeD0iNTgyLjIxMDI4IiB5PSIxODIuNjgyMjkiIHdpZHRoPSIxMC43NzE0OCIgaGVpZ2h0PSIyIiBmaWxsPSIjMmYyZTQxIi8+PHJlY3QgeD0iNjE2LjIxMDI4IiB5PSIxODIuNjgyMjkiIHdpZHRoPSIxMC43NzE0OCIgaGVpZ2h0PSIyIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggZD0iTTcwMS4yMjEzLDM0NS44NTM3NWMtOS41MDY2LTUuNzYxNDgtMTMuOTgzNzItMTYuNjkwODEtMTMuOTkyLTI3LjUwMDE5YTM5LjYzNzIyLDM5LjYzNzIyLDAsMCwxLDE0LjAxMjItMzAuMjc1MjZjOS43OTE2NS04LjQ2ODUxLDIyLjgzMTM5LTEyLjYxMjg0LDM1LjU3MDM3LTEzLjY2NTg3LDE1LjQxMzE0LTEuMjc0MDcsMzAuNzI2LDIuMDc2MTIsNDUuNDQ1NjEsNi4zNTIwNSwxNS43NjEzMiw0LjU3ODU1LDMxLjIwNTIxLDEwLjMzOTQsNDcuMzM5MTYsMTMuNTQ0NzgsMTUuNjU2NDgsMy4xMTA1MiwzMS4zNTQxNSwzLjQxNzI2LDQ2Ljc2ODI4LTEuMDgwOCwxNC4yMjY4Ni00LjE1MTU5LDI4LjY2Nzc4LTExLjQ4NzY1LDQzLjg2MzczLTEwLjIxNjMsMTMuMTI0NzMsMS4wOTgwNywyMy4wOTIsOS44MzIxOSwyOS43OTcsMjAuNjIzLDEzLjMxMTcsMjEuNDIzNiwxNC4zNjMxMSw1MC4xNzgzOSwzMy41MDY1Myw2OC4wMTg0Nyw4LjI3Nyw3LjcxMzQ2LDE5LjA1OTUyLDExLjIwNzY0LDI5LjgzNjQzLDEzLjgxOTMxLDEwLjMzNCwyLjUwNDM0LDIxLjM1NzY3LDQuMTA4OTIsMzAuODQwNjcsOS4xNzcxNSwxMC4xNjg3MSw1LjQzNDcyLDE2LjMyNDY2LDE1LjY0OTY2LDE0LjczMzg0LDI3LjM1NDMtMS44MDY4MSwxMy4yOTM3MS0xMS4yNjY0OCwyNC43MzUyLTIxLjY4MTI5LDMyLjU0NDY2QTkxLjczMDgsOTEuNzMwOCwwLDAsMSw5NTkuODY3NTUsNDY5LjY3NWMtMjcuNjMyMzgtNy4zMzA2OC01MC4wODc4LTI4Ljc0NjE3LTc5LjUyNzU0LTMwLjA4ODIyLTE2LjQyMzUxLS43NDg2OS0zMi41OTQxMywzLjcyNTYxLTQ5LjAxNTMyLDIuOTgzNzMtMTEuMzgwNTgtLjUxNDE1LTIzLjYzMDEtNC43MjE1NC0zMS41NTY3LTEzLjE4NTFhMjQuNzA4ODYsMjQuNzA4ODYsMCwwLDEtNi43NTY4OC0xNC41MzcxOWMtLjE3OTM1LTEuOTA1NjUtMy4xODExMS0xLjkyNDM3LTMsMCwxLjEyNTMxLDExLjk1NjkyLDkuOTI2NTYsMjAuNzg4NzUsMjAuNDMsMjUuNjQ0MjdhNTYuMzU1MzIsNTYuMzU1MzIsMCwwLDAsMTcuNDc5Nyw0LjgxMjFjOC4wMzYwNS44ODc1NiwxNi4yMjkwNS4wNDc5MywyNC4yMjgzMS0uODI5MDYsOC40NTUtLjkyNywxNi45MjQtMi4wNjg3NSwyNS40NDc3OS0xLjk2NjYxYTcyLjczOTg5LDcyLjczOTg5LDAsMCwxLDIyLjgzNzI5LDQuMDYxNDZjMTQuMTA1NjIsNC44MzgsMjYuODEyODMsMTIuODY0NzksNDAuMjg4MjUsMTkuMTE2MjcsMTMuMDQyMjUsNi4wNTA1MSwyNi42OTk4Niw5LjgwMTgsNDEuMTQ1NjQsOS44OTQ4NWE5Ni4yMTEsOTYuMjExLDAsMCwwLDQwLjM2NzYxLTguNjU3MTZjMTIuNDc3NzgtNS42ODQzMywyNC4yMDMyOC0xNC4xNjkzOCwzMS45NzIxNS0yNS42MDc4MSw3LjI4Mzg5LTEwLjcyNDM2LDExLjEwODE1LTI0Ljc5MzE3LDQuNTUzMDktMzYuNzk5MTYtNS4xMzc2NS05LjQwOTkyLTE0LjkzNjE3LTE0LjE3MTc0LTI0LjgxNTk1LTE3LjEwMzQtMTAuOTUyMjQtMy4yNDk5LTIyLjM5OS00LjczNDI3LTMzLjExOTgtOC43ODE0MWE0NS41OTMyOCw0NS41OTMyOCwwLDAsMS0xNS4xNzE3OS05LjEwMDIxLDU3LjAwMzA3LDU3LjAwMzA3LDAsMCwxLTExLjUzMS0xNS45MTA5MWMtNi4xMzc2OC0xMS45NzMwOS05LjI3Mzc4LTI1LjEyMzI2LTE0LjQ0Mjc2LTM3LjQ5MDQ5LTQuNzg4LTExLjQ1NTgtMTEuMzQwMTQtMjMuMDAyMi0yMS44OTM3LTMwLjA2MjE5YTM3LjI0NDQ0LDM3LjI0NDQ0LDAsMCwwLTE5LjA1NzQxLTYuMTUxNzJjLTcuNTE3NzMtLjM0LTE0Ljk4ODQ4LDEuMzg1NzgtMjIuMTMzNTMsMy41NjA1NS03LjU2MSwyLjMwMTM3LTE0LjkzOTQxLDUuMTc1ODUtMjIuNTU3NTUsNy4yOTM5NGE4MS42NzE3MSw4MS42NzE3MSwwLDAsMS0yNC42Njg4MSwyLjk0NjE1Yy0xNy4xMjItLjU1Nzg2LTMzLjY2MTgtNS42NDQxLTQ5Ljg3MTEzLTEwLjc3MjIzLTI4LjM5NTU5LTguOTgzNDgtNTkuOTc2LTE4LjYwMDY3LTg4LjUyNDU5LTQuNzUzMDYtMTEuMTIzNjksNS4zOTU1OS0yMC41NDYsMTQuNDQ4MjUtMjQuNTE4NTEsMjYuMzM1MzYtMy41MTUsMTAuNTE4MTItMy4wMjkzLDIyLjc4NywyLjM4OTY0LDMyLjU4NjI0YTMwLjQ1NTI5LDMwLjQ1NTI5LDAsMCwwLDEwLjg2MzA3LDExLjMzMDE2YzEuNjU1NCwxLjAwMzI2LDMuMTY0ODgtMS41OSwxLjUxNDE2LTIuNTkwNDFaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTM3Ljg3MjE0IC0xODQuNzA0MykiIGZpbGw9IiMyZjJlNDEiLz48cGF0aCBkPSJNNzgzLjk2MDM2LDYxMy4yOTg2OUg3NjkuMjAwNTlhNi41MDc1Myw2LjUwNzUzLDAsMCwxLTYuNS02LjVWNTIxLjU0MzgxYTYuNTA3NTMsNi41MDc1MywwLDAsMSw2LjUtNi41aDE0Ljc1OTc3YTYuNTA3NTMsNi41MDc1MywwLDAsMSw2LjUsNi41djg1LjI1NDg4QTYuNTA3NTMsNi41MDc1MywwLDAsMSw3ODMuOTYwMzYsNjEzLjI5ODY5WiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTEzNy44NzIxNCAtMTg0LjcwNDMpIiBmaWxsPSIjNmM2M2ZmIi8+PHBhdGggZD0iTTcyNi45ODg0LDMzMi42MTE3M2ExMi43MzU4NiwxMi43MzU4NiwwLDAsMCwyMS42NTg2LDcuMjAwNzJsLTIuNTA3MDgtLjY2MTlhMjEuMjA0OSwyMS4yMDQ5LDAsMCwwLDM3LjE1MTY1LDQuMzk2NjljMS4xMjk1OS0xLjU2OTQxLTEuNDcxNTMtMy4wNjg3LTIuNTkwNDEtMS41MTQxNmExOC4yMDk5MywxOC4yMDk5MywwLDAsMS0zMS42Njg0LTMuNjgsMS41MjIxMywxLjUyMjEzLDAsMCwwLTIuNTA3MDgtLjY2MTksOS43NTgxNSw5Ljc1ODE1LDAsMCwxLTE2LjY0NDQ0LTUuODc2OTJjLS4yNjc2OC0xLjkwMzg1LTMuMTU4NzUtMS4wOTM3OC0yLjg5Mjg0Ljc5NzUyWiIgdHJhbnNmb3JtPSJ0cmFuc2xhdGUoLTEzNy44NzIxNCAtMTg0LjcwNDMpIiBmaWxsPSIjMmYyZTQxIi8+PHBhdGggaWQ9ImY2YWQ4ODgxLTUzMWYtNDUwZi1hZjhjLWVlNDMwZjkxYzk0NC0zMzgiIGRhdGEtbmFtZT0iUGF0aCAzMyIgZD0iTTYzMi40MjQyOSw1NDUuNTk3bC0uMzEyODEuMjEyMDctNS44MDc2Ni04LjU2Njc2YTUuOTksNS45OSwwLDAsMC04LjMxOTMtMS41OTY4NWgwbC0xOC4xNDk5MSwxMi4zMDQzNmE1Ljk5LDUuOTksMCwwLDAtMS41OTY4NSw4LjMxOTNsMzEuODYxMzQsNDYuOTk4YTUuOTksNS45OSwwLDAsMCw4LjMxOTI5LDEuNTk2ODVoMGwxOC4xNDk4NC0xMi4zMDQzYTUuOTksNS45OSwwLDAsMCwxLjU5Njg1LTguMzE5M2gwbC0yMS45MTk2OS0zMi4zMzMyOC4zMTI4MS0uMjEyMDdaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTM3Ljg3MjE0IC0xODQuNzA0MykiIGZpbGw9IiMzZjNkNTYiLz48cGF0aCBpZD0iZmM4ODZiNTMtOGY3Zi00NTlhLThmNDYtZGM4ODk3MmYwM2MwLTMzOSIgZGF0YS1uYW1lPSJQYXRoIDM0IiBkPSJNNjE5LjM1NzQxLDUzNy44N2wtMi4yOTMxMiwxLjU1NDU3YTIuMDU3MTEsMi4wNTcxMSwwLDAsMSwuMDEzOTEsMy40MTQ4MWwtMTAuMDY0MDcsNi44MjI3M2EyLjA1NzExLDIuMDU3MTEsMCwwLDEtMy4xNjY3NS0xLjI3NzA3bC0yLjE0MjExLDEuNDUyMmE0LjMyOTg1LDQuMzI5ODUsMCwwLDAtMS4xNTQyOCw2LjAxMzU2aDBsMzAuNzkzMzEsNDUuNDIyNTdhNC4zMjk4NCw0LjMyOTg0LDAsMCwwLDYuMDEzNTUsMS4xNTQyOGgwbDE3LjY1MTYyLTExLjk2NjU2YTQuMzI5ODUsNC4zMjk4NSwwLDAsMCwxLjE1NDI4LTYuMDEzNTVoMGwtMzAuNzkzNDYtNDUuNDIyOGE0LjMyOTg1LDQuMzI5ODUsMCwwLDAtNi4wMTM1Ni0xLjE1NDI3aDBaIiB0cmFuc2Zvcm09InRyYW5zbGF0ZSgtMTM3Ljg3MjE0IC0xODQuNzA0MykiIGZpbGw9IiNmZmYiLz48L3N2Zz4="},78146:(M,N,j)=>{j.d(N,{A:()=>i});const i=j.p+"assets/images/biscuit-keypair-creation-generate-new-29decb7d7cb66c260eee153bf2b58deb.png"},63527:(M,N,j)=>{j.d(N,{A:()=>i});const i=j.p+"assets/images/biscuit-keypair-creation-159c55faa3d07806ebd6960cc3ef6b1f.png"},28453:(M,N,j)=>{j.d(N,{R:()=>D,x:()=>e});var i=j(96540);const L={},T=i.createContext(L);function D(M){const N=i.useContext(T);return i.useMemo((function(){return"function"==typeof M?M(N):{...N,...M}}),[N,M])}function e(M){let N;return N=M.disableParentContext?"function"==typeof M.components?M.components(L):M.components||L:D(M.components),i.createElement(T.Provider,{value:N},M.children)}}}]);