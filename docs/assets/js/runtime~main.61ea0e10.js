(()=>{"use strict";var e,t,r,a,o,n={},c={};function i(e){var t=c[e];if(void 0!==t)return t.exports;var r=c[e]={exports:{}};return n[e].call(r.exports,r,r.exports,i),r.exports}i.m=n,e=[],i.O=(t,r,a,o)=>{if(!r){var n=1/0;for(u=0;u<e.length;u++){r=e[u][0],a=e[u][1],o=e[u][2];for(var c=!0,d=0;d<r.length;d++)(!1&o||n>=o)&&Object.keys(i.O).every((e=>i.O[e](r[d])))?r.splice(d--,1):(c=!1,o<n&&(n=o));if(c){e.splice(u--,1);var f=a();void 0!==f&&(t=f)}}return t}o=o||0;for(var u=e.length;u>0&&e[u-1][2]>o;u--)e[u]=e[u-1];e[u]=[r,a,o]},i.n=e=>{var t=e&&e.__esModule?()=>e.default:()=>e;return i.d(t,{a:t}),t},r=Object.getPrototypeOf?e=>Object.getPrototypeOf(e):e=>e.__proto__,i.t=function(e,a){if(1&a&&(e=this(e)),8&a)return e;if("object"==typeof e&&e){if(4&a&&e.__esModule)return e;if(16&a&&"function"==typeof e.then)return e}var o=Object.create(null);i.r(o);var n={};t=t||[null,r({}),r([]),r(r)];for(var c=2&a&&e;"object"==typeof c&&!~t.indexOf(c);c=r(c))Object.getOwnPropertyNames(c).forEach((t=>n[t]=()=>e[t]));return n.default=()=>e,i.d(o,n),o},i.d=(e,t)=>{for(var r in t)i.o(t,r)&&!i.o(e,r)&&Object.defineProperty(e,r,{enumerable:!0,get:t[r]})},i.f={},i.e=e=>Promise.all(Object.keys(i.f).reduce(((t,r)=>(i.f[r](e,t),t)),[])),i.u=e=>"assets/js/"+({48:"a94703ab",61:"1f391b9e",98:"a7bd4aaa",134:"393be207",141:"3564e3a7",179:"cabb12cd",218:"92063559",235:"18891827",287:"723d447c",401:"17896441",402:"d467179c",424:"aeecb766",568:"046e2194",616:"a7456010",634:"c4f5d8e4",647:"5e95c892",742:"aba21aa0",748:"fd744dac",916:"b2cafd49",969:"14eb3368"}[e]||e)+"."+{48:"096241fe",61:"e063795c",98:"89d560b3",134:"d246ddc4",141:"0e8f3cd3",179:"b6283d0b",218:"30c3d519",235:"dca59b89",237:"5d257071",287:"46f32f89",401:"b22fff72",402:"4b95f64e",408:"e60a5ba2",424:"85fc5017",568:"9e2f685e",616:"33838e69",634:"8692db09",647:"6e8df14c",742:"f0a1b06a",748:"6c94ca1a",916:"1ed2fc96",969:"b423c818"}[e]+".js",i.miniCssF=e=>{},i.g=function(){if("object"==typeof globalThis)return globalThis;try{return this||new Function("return this")()}catch(e){if("object"==typeof window)return window}}(),i.o=(e,t)=>Object.prototype.hasOwnProperty.call(e,t),a={},o="documentation:",i.l=(e,t,r,n)=>{if(a[e])a[e].push(t);else{var c,d;if(void 0!==r)for(var f=document.getElementsByTagName("script"),u=0;u<f.length;u++){var b=f[u];if(b.getAttribute("src")==e||b.getAttribute("data-webpack")==o+r){c=b;break}}c||(d=!0,(c=document.createElement("script")).charset="utf-8",c.timeout=120,i.nc&&c.setAttribute("nonce",i.nc),c.setAttribute("data-webpack",o+r),c.src=e),a[e]=[t];var l=(t,r)=>{c.onerror=c.onload=null,clearTimeout(s);var o=a[e];if(delete a[e],c.parentNode&&c.parentNode.removeChild(c),o&&o.forEach((e=>e(r))),t)return t(r)},s=setTimeout(l.bind(null,void 0,{type:"timeout",target:c}),12e4);c.onerror=l.bind(null,c.onerror),c.onload=l.bind(null,c.onload),d&&document.head.appendChild(c)}},i.r=e=>{"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},i.p="/otoroshi-biscuit-studio/",i.gca=function(e){return e={17896441:"401",18891827:"235",92063559:"218",a94703ab:"48","1f391b9e":"61",a7bd4aaa:"98","393be207":"134","3564e3a7":"141",cabb12cd:"179","723d447c":"287",d467179c:"402",aeecb766:"424","046e2194":"568",a7456010:"616",c4f5d8e4:"634","5e95c892":"647",aba21aa0:"742",fd744dac:"748",b2cafd49:"916","14eb3368":"969"}[e]||e,i.p+i.u(e)},(()=>{var e={354:0,869:0};i.f.j=(t,r)=>{var a=i.o(e,t)?e[t]:void 0;if(0!==a)if(a)r.push(a[2]);else if(/^(354|869)$/.test(t))e[t]=0;else{var o=new Promise(((r,o)=>a=e[t]=[r,o]));r.push(a[2]=o);var n=i.p+i.u(t),c=new Error;i.l(n,(r=>{if(i.o(e,t)&&(0!==(a=e[t])&&(e[t]=void 0),a)){var o=r&&("load"===r.type?"missing":r.type),n=r&&r.target&&r.target.src;c.message="Loading chunk "+t+" failed.\n("+o+": "+n+")",c.name="ChunkLoadError",c.type=o,c.request=n,a[1](c)}}),"chunk-"+t,t)}},i.O.j=t=>0===e[t];var t=(t,r)=>{var a,o,n=r[0],c=r[1],d=r[2],f=0;if(n.some((t=>0!==e[t]))){for(a in c)i.o(c,a)&&(i.m[a]=c[a]);if(d)var u=d(i)}for(t&&t(r);f<n.length;f++)o=n[f],i.o(e,o)&&e[o]&&e[o][0](),e[o]=0;return i.O(u)},r=self.webpackChunkdocumentation=self.webpackChunkdocumentation||[];r.forEach(t.bind(null,0)),r.push=t.bind(null,r.push.bind(r))})()})();