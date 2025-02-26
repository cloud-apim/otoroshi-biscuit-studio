"use strict";(self.webpackChunkotoroshi_biscuit_studio_documentation=self.webpackChunkotoroshi_biscuit_studio_documentation||[]).push([[568],{96708:(A,e,o)=>{o.r(e),o.d(e,{assets:()=>a,contentTitle:()=>d,default:()=>r,frontMatter:()=>s,metadata:()=>i,toc:()=>h});const i=JSON.parse('{"id":"install","title":"Installation","description":"App Installation","source":"@site/docs/install.mdx","sourceDirName":".","slug":"/install","permalink":"/otoroshi-biscuit-studio/docs/install","draft":false,"unlisted":false,"tags":[],"version":"current","sidebarPosition":3,"frontMatter":{"sidebar_position":3},"sidebar":"tutorialSidebar","previous":{"title":"Introduction","permalink":"/otoroshi-biscuit-studio/docs/introduction"},"next":{"title":"Entities","permalink":"/otoroshi-biscuit-studio/docs/category/entities"}}');var n=o(74848),t=o(28453);o(16438);const s={sidebar_position:3},d="Installation",a={},h=[{value:"Download Otoroshi",id:"download-otoroshi",level:2},{value:"Download the Biscuit Studio Extension",id:"download-the-biscuit-studio-extension",level:2},{value:"Run Otoroshi with the Biscuit Studio Extension",id:"run-otoroshi-with-the-biscuit-studio-extension",level:2},{value:"Use it on Cloud APIM with Otoroshi Managed Instances",id:"use-it-on-cloud-apim-with-otoroshi-managed-instances",level:2},{value:"Accessing the Otoroshi with Interface",id:"accessing-the-otoroshi-with-interface",level:3},{value:"Setting Up on Clever Cloud",id:"setting-up-on-clever-cloud",level:2},{value:"Create a new Otoroshi add-on",id:"create-a-new-otoroshi-add-on",level:3}];function c(A){const e={a:"a",br:"br",code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",img:"img",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",...(0,t.R)(),...A.components};return(0,n.jsxs)(n.Fragment,{children:[(0,n.jsx)(e.header,{children:(0,n.jsx)(e.h1,{id:"installation",children:"Installation"})}),"\n",(0,n.jsx)(e.p,{children:(0,n.jsx)(e.img,{alt:"App Installation",src:o(71036).A+"",width:"738",height:"729"})}),"\n",(0,n.jsx)(e.h2,{id:"download-otoroshi",children:"Download Otoroshi"}),"\n",(0,n.jsx)(e.p,{children:(0,n.jsxs)(e.a,{href:"https://github.com/MAIF/otoroshi/releases/download/v16.23.2/otoroshi.jar",children:[" ",(0,n.jsx)(e.img,{src:"https://img.shields.io/github/release/MAIF/otoroshi.svg",alt:"Download"})," "]})}),"\n",(0,n.jsx)(e.p,{children:"First, download the Otoroshi jar file:"}),"\n",(0,n.jsx)(e.pre,{children:(0,n.jsx)(e.code,{className:"language-sh",children:"curl -L -o otoroshi.jar 'https://github.com/MAIF/otoroshi/releases/download/v16.23.2/otoroshi.jar'\n"})}),"\n",(0,n.jsx)(e.h2,{id:"download-the-biscuit-studio-extension",children:"Download the Biscuit Studio Extension"}),"\n",(0,n.jsx)(e.p,{children:(0,n.jsxs)(e.a,{href:"https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/download/0.0.6/otoroshi-biscuit-studio-0.0.6.jar",children:[(0,n.jsx)(e.img,{src:"https://img.shields.io/github/release/cloud-apim/otoroshi-biscuit-studio.svg",alt:"Download Otoroshi Biscuit Studio extension"})," "]})}),"\n",(0,n.jsx)(e.p,{children:"Next, download the Biscuit Studio extension for Otoroshi:"}),"\n",(0,n.jsxs)(e.p,{children:["You can download the latest release of ",(0,n.jsx)(e.code,{children:"otoroshi-biscuit-studio"})," from ",(0,n.jsx)(e.a,{href:"https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/latest",children:"here"}),"."]}),"\n",(0,n.jsx)(e.pre,{children:(0,n.jsx)(e.code,{className:"language-sh",children:"curl -L -o biscuit-studio-extension.jar 'https://github.com/cloud-apim/otoroshi-biscuit-studio/releases/download/0.0.6/otoroshi-biscuit-studio-0.0.6.jar'\n"})}),"\n",(0,n.jsx)(e.h2,{id:"run-otoroshi-with-the-biscuit-studio-extension",children:"Run Otoroshi with the Biscuit Studio Extension"}),"\n",(0,n.jsx)(e.p,{children:"Run Otoroshi with the Biscuit Studio extension by executing the following command:"}),"\n",(0,n.jsx)(e.pre,{children:(0,n.jsx)(e.code,{className:"language-sh",children:'java -cp "./biscuit-studio-extension.jar:./otoroshi.jar" -Dotoroshi.adminLogin=admin -Dotoroshi.adminPassword=password -Dotoroshi.storage=file play.core.server.ProdServerStart\n'})}),"\n",(0,n.jsxs)(e.p,{children:["This will start the Otoroshi API Gateway with Biscuit Studio integrated. You can access the Otoroshi UI by opening ",(0,n.jsx)(e.a,{href:"http://otoroshi.oto.tools:8080/",children:"http://otoroshi.oto.tools:8080/"})," in your browser."]}),"\n",(0,n.jsxs)(e.p,{children:["Default Otoroshi UI credentials:",(0,n.jsx)(e.br,{}),"\n",(0,n.jsx)(e.strong,{children:"Username"}),": ",(0,n.jsx)(e.code,{children:"admin"}),(0,n.jsx)(e.br,{}),"\n",(0,n.jsx)(e.strong,{children:"Password"}),": ",(0,n.jsx)(e.code,{children:"password"})]}),"\n",(0,n.jsx)(e.h2,{id:"use-it-on-cloud-apim-with-otoroshi-managed-instances",children:"Use it on Cloud APIM with Otoroshi Managed Instances"}),"\n",(0,n.jsx)(e.p,{children:(0,n.jsx)(e.img,{src:o(39937).A+"",width:"256",height:"256"})}),"\n",(0,n.jsxs)(e.p,{children:["Register on ",(0,n.jsx)(e.a,{href:"https://cloud-apim.com",children:"Cloud APIM"})]}),"\n",(0,n.jsxs)(e.p,{children:["Go to your ",(0,n.jsx)(e.a,{href:"https://console.cloud-apim.com/",children:"Cloud APIM console"})," and now click on ",(0,n.jsx)(e.code,{children:"Managed Instance"})," then ",(0,n.jsx)(e.code,{children:"New instance"})," and select Otoroshi's logo."]}),"\n",(0,n.jsx)(e.h3,{id:"accessing-the-otoroshi-with-interface",children:"Accessing the Otoroshi with Interface"}),"\n",(0,n.jsx)(e.p,{children:"Once your Otoroshi instance is created you can click on 'console' button to open the interface and login with your credentials."}),"\n",(0,n.jsx)(e.p,{children:"Now you can use the Biscuit Studio extension, it is included in your Otoroshi managed instance."}),"\n",(0,n.jsx)(e.h2,{id:"setting-up-on-clever-cloud",children:"Setting Up on Clever Cloud"}),"\n",(0,n.jsx)(e.p,{children:(0,n.jsx)(e.img,{src:o(92742).A+"",width:"1536",height:"592"})}),"\n",(0,n.jsx)(e.h3,{id:"create-a-new-otoroshi-add-on",children:"Create a new Otoroshi add-on"}),"\n",(0,n.jsxs)(e.ol,{children:["\n",(0,n.jsxs)(e.li,{children:[(0,n.jsx)(e.a,{href:"https://console.clever-cloud.com/users/me/addons/new",children:"Create a new add-on"})," by selecting the ",(0,n.jsx)(e.strong,{children:"Create..."})," dropdown in the sidebar, then choose ",(0,n.jsx)(e.strong,{children:"Add-On"}),"."]}),"\n",(0,n.jsxs)(e.li,{children:["Pick the ",(0,n.jsx)(e.strong,{children:"Otoroshi with LLM"})," add-on."]}),"\n",(0,n.jsx)(e.li,{children:"Give your Otoroshi with LLM add-on a name and choose a deployment zone."}),"\n"]})]})}function r(A={}){const{wrapper:e}={...(0,t.R)(),...A.components};return e?(0,n.jsx)(e,{...A,children:(0,n.jsx)(c,{...A})}):c(A)}},16438:(A,e,o)=>{o(96540),o(74848)},92742:(A,e,o)=>{o.d(e,{A:()=>i});const i="data:image/webp;base64,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"},39937:(A,e,o)=>{o.d(e,{A:()=>i});const i="data:image/png;base64,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"},71036:(A,e,o)=>{o.d(e,{A:()=>i});const i=o.p+"assets/images/undraw_app-installation_3czh-8cb66dd4db1fe37ba2ce827b12816585.svg"},28453:(A,e,o)=>{o.d(e,{R:()=>s,x:()=>d});var i=o(96540);const n={},t=i.createContext(n);function s(A){const e=i.useContext(t);return i.useMemo((function(){return"function"==typeof A?A(e):{...e,...A}}),[e,A])}function d(A){let e;return e=A.disableParentContext?"function"==typeof A.components?A.components(n):A.components||n:s(A.components),i.createElement(t.Provider,{value:e},A.children)}}}]);