/**
 * @license
 * Copyright 2019 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const t$3=window,e$6=t$3.ShadowRoot&&(void 0===t$3.ShadyCSS||t$3.ShadyCSS.nativeShadow)&&"adoptedStyleSheets"in Document.prototype&&"replace"in CSSStyleSheet.prototype,s$3=Symbol(),n$5=new WeakMap;class o$4{constructor(t,e,n){if(this._$cssResult$=!0,n!==s$3)throw Error("CSSResult is not constructable. Use `unsafeCSS` or `css` instead.");this.cssText=t,this.t=e;}get styleSheet(){let t=this.o;const s=this.t;if(e$6&&void 0===t){const e=void 0!==s&&1===s.length;e&&(t=n$5.get(s)),void 0===t&&((this.o=t=new CSSStyleSheet).replaceSync(this.cssText),e&&n$5.set(s,t));}return t}toString(){return this.cssText}}const r$2=t=>new o$4("string"==typeof t?t:t+"",void 0,s$3),i$3=(t,...e)=>{const n=1===t.length?t[0]:e.reduce(((e,s,n)=>e+(t=>{if(!0===t._$cssResult$)return t.cssText;if("number"==typeof t)return t;throw Error("Value passed to 'css' function must be a 'css' function result: "+t+". Use 'unsafeCSS' to pass non-literal values, but take care to ensure page security.")})(s)+t[n+1]),t[0]);return new o$4(n,t,s$3)},S$1=(s,n)=>{e$6?s.adoptedStyleSheets=n.map((t=>t instanceof CSSStyleSheet?t:t.styleSheet)):n.forEach((e=>{const n=document.createElement("style"),o=t$3.litNonce;void 0!==o&&n.setAttribute("nonce",o),n.textContent=e.cssText,s.appendChild(n);}));},c$1=e$6?t=>t:t=>t instanceof CSSStyleSheet?(t=>{let e="";for(const s of t.cssRules)e+=s.cssText;return r$2(e)})(t):t;

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */var s$2;const e$5=window,r$1=e$5.trustedTypes,h$1=r$1?r$1.emptyScript:"",o$3=e$5.reactiveElementPolyfillSupport,n$4={toAttribute(t,i){switch(i){case Boolean:t=t?h$1:null;break;case Object:case Array:t=null==t?t:JSON.stringify(t);}return t},fromAttribute(t,i){let s=t;switch(i){case Boolean:s=null!==t;break;case Number:s=null===t?null:Number(t);break;case Object:case Array:try{s=JSON.parse(t);}catch(t){s=null;}}return s}},a$1=(t,i)=>i!==t&&(i==i||t==t),l$3={attribute:!0,type:String,converter:n$4,reflect:!1,hasChanged:a$1},d$1="finalized";class u$1 extends HTMLElement{constructor(){super(),this._$Ei=new Map,this.isUpdatePending=!1,this.hasUpdated=!1,this._$El=null,this._$Eu();}static addInitializer(t){var i;this.finalize(),(null!==(i=this.h)&&void 0!==i?i:this.h=[]).push(t);}static get observedAttributes(){this.finalize();const t=[];return this.elementProperties.forEach(((i,s)=>{const e=this._$Ep(s,i);void 0!==e&&(this._$Ev.set(e,s),t.push(e));})),t}static createProperty(t,i=l$3){if(i.state&&(i.attribute=!1),this.finalize(),this.elementProperties.set(t,i),!i.noAccessor&&!this.prototype.hasOwnProperty(t)){const s="symbol"==typeof t?Symbol():"__"+t,e=this.getPropertyDescriptor(t,s,i);void 0!==e&&Object.defineProperty(this.prototype,t,e);}}static getPropertyDescriptor(t,i,s){return {get(){return this[i]},set(e){const r=this[t];this[i]=e,this.requestUpdate(t,r,s);},configurable:!0,enumerable:!0}}static getPropertyOptions(t){return this.elementProperties.get(t)||l$3}static finalize(){if(this.hasOwnProperty(d$1))return !1;this[d$1]=!0;const t=Object.getPrototypeOf(this);if(t.finalize(),void 0!==t.h&&(this.h=[...t.h]),this.elementProperties=new Map(t.elementProperties),this._$Ev=new Map,this.hasOwnProperty("properties")){const t=this.properties,i=[...Object.getOwnPropertyNames(t),...Object.getOwnPropertySymbols(t)];for(const s of i)this.createProperty(s,t[s]);}return this.elementStyles=this.finalizeStyles(this.styles),!0}static finalizeStyles(i){const s=[];if(Array.isArray(i)){const e=new Set(i.flat(1/0).reverse());for(const i of e)s.unshift(c$1(i));}else void 0!==i&&s.push(c$1(i));return s}static _$Ep(t,i){const s=i.attribute;return !1===s?void 0:"string"==typeof s?s:"string"==typeof t?t.toLowerCase():void 0}_$Eu(){var t;this._$E_=new Promise((t=>this.enableUpdating=t)),this._$AL=new Map,this._$Eg(),this.requestUpdate(),null===(t=this.constructor.h)||void 0===t||t.forEach((t=>t(this)));}addController(t){var i,s;(null!==(i=this._$ES)&&void 0!==i?i:this._$ES=[]).push(t),void 0!==this.renderRoot&&this.isConnected&&(null===(s=t.hostConnected)||void 0===s||s.call(t));}removeController(t){var i;null===(i=this._$ES)||void 0===i||i.splice(this._$ES.indexOf(t)>>>0,1);}_$Eg(){this.constructor.elementProperties.forEach(((t,i)=>{this.hasOwnProperty(i)&&(this._$Ei.set(i,this[i]),delete this[i]);}));}createRenderRoot(){var t;const s=null!==(t=this.shadowRoot)&&void 0!==t?t:this.attachShadow(this.constructor.shadowRootOptions);return S$1(s,this.constructor.elementStyles),s}connectedCallback(){var t;void 0===this.renderRoot&&(this.renderRoot=this.createRenderRoot()),this.enableUpdating(!0),null===(t=this._$ES)||void 0===t||t.forEach((t=>{var i;return null===(i=t.hostConnected)||void 0===i?void 0:i.call(t)}));}enableUpdating(t){}disconnectedCallback(){var t;null===(t=this._$ES)||void 0===t||t.forEach((t=>{var i;return null===(i=t.hostDisconnected)||void 0===i?void 0:i.call(t)}));}attributeChangedCallback(t,i,s){this._$AK(t,s);}_$EO(t,i,s=l$3){var e;const r=this.constructor._$Ep(t,s);if(void 0!==r&&!0===s.reflect){const h=(void 0!==(null===(e=s.converter)||void 0===e?void 0:e.toAttribute)?s.converter:n$4).toAttribute(i,s.type);this._$El=t,null==h?this.removeAttribute(r):this.setAttribute(r,h),this._$El=null;}}_$AK(t,i){var s;const e=this.constructor,r=e._$Ev.get(t);if(void 0!==r&&this._$El!==r){const t=e.getPropertyOptions(r),h="function"==typeof t.converter?{fromAttribute:t.converter}:void 0!==(null===(s=t.converter)||void 0===s?void 0:s.fromAttribute)?t.converter:n$4;this._$El=r,this[r]=h.fromAttribute(i,t.type),this._$El=null;}}requestUpdate(t,i,s){let e=!0;void 0!==t&&(((s=s||this.constructor.getPropertyOptions(t)).hasChanged||a$1)(this[t],i)?(this._$AL.has(t)||this._$AL.set(t,i),!0===s.reflect&&this._$El!==t&&(void 0===this._$EC&&(this._$EC=new Map),this._$EC.set(t,s))):e=!1),!this.isUpdatePending&&e&&(this._$E_=this._$Ej());}async _$Ej(){this.isUpdatePending=!0;try{await this._$E_;}catch(t){Promise.reject(t);}const t=this.scheduleUpdate();return null!=t&&await t,!this.isUpdatePending}scheduleUpdate(){return this.performUpdate()}performUpdate(){var t;if(!this.isUpdatePending)return;this.hasUpdated,this._$Ei&&(this._$Ei.forEach(((t,i)=>this[i]=t)),this._$Ei=void 0);let i=!1;const s=this._$AL;try{i=this.shouldUpdate(s),i?(this.willUpdate(s),null===(t=this._$ES)||void 0===t||t.forEach((t=>{var i;return null===(i=t.hostUpdate)||void 0===i?void 0:i.call(t)})),this.update(s)):this._$Ek();}catch(t){throw i=!1,this._$Ek(),t}i&&this._$AE(s);}willUpdate(t){}_$AE(t){var i;null===(i=this._$ES)||void 0===i||i.forEach((t=>{var i;return null===(i=t.hostUpdated)||void 0===i?void 0:i.call(t)})),this.hasUpdated||(this.hasUpdated=!0,this.firstUpdated(t)),this.updated(t);}_$Ek(){this._$AL=new Map,this.isUpdatePending=!1;}get updateComplete(){return this.getUpdateComplete()}getUpdateComplete(){return this._$E_}shouldUpdate(t){return !0}update(t){void 0!==this._$EC&&(this._$EC.forEach(((t,i)=>this._$EO(i,this[i],t))),this._$EC=void 0),this._$Ek();}updated(t){}firstUpdated(t){}}u$1[d$1]=!0,u$1.elementProperties=new Map,u$1.elementStyles=[],u$1.shadowRootOptions={mode:"open"},null==o$3||o$3({ReactiveElement:u$1}),(null!==(s$2=e$5.reactiveElementVersions)&&void 0!==s$2?s$2:e$5.reactiveElementVersions=[]).push("1.6.3");

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
var t$2;const i$2=window,s$1=i$2.trustedTypes,e$4=s$1?s$1.createPolicy("lit-html",{createHTML:t=>t}):void 0,o$2="$lit$",n$3=`lit$${(Math.random()+"").slice(9)}$`,l$2="?"+n$3,h=`<${l$2}>`,r=document,u=()=>r.createComment(""),d=t=>null===t||"object"!=typeof t&&"function"!=typeof t,c=Array.isArray,v=t=>c(t)||"function"==typeof(null==t?void 0:t[Symbol.iterator]),a="[ \t\n\f\r]",f=/<(?:(!--|\/[^a-zA-Z])|(\/?[a-zA-Z][^>\s]*)|(\/?$))/g,_=/-->/g,m=/>/g,p=RegExp(`>|${a}(?:([^\\s"'>=/]+)(${a}*=${a}*(?:[^ \t\n\f\r"'\`<>=]|("|')|))|$)`,"g"),g=/'/g,$=/"/g,y=/^(?:script|style|textarea|title)$/i,w=t=>(i,...s)=>({_$litType$:t,strings:i,values:s}),x=w(1),T=Symbol.for("lit-noChange"),A=Symbol.for("lit-nothing"),E=new WeakMap,C=r.createTreeWalker(r,129,null,!1);function P(t,i){if(!Array.isArray(t)||!t.hasOwnProperty("raw"))throw Error("invalid template strings array");return void 0!==e$4?e$4.createHTML(i):i}const V=(t,i)=>{const s=t.length-1,e=[];let l,r=2===i?"<svg>":"",u=f;for(let i=0;i<s;i++){const s=t[i];let d,c,v=-1,a=0;for(;a<s.length&&(u.lastIndex=a,c=u.exec(s),null!==c);)a=u.lastIndex,u===f?"!--"===c[1]?u=_:void 0!==c[1]?u=m:void 0!==c[2]?(y.test(c[2])&&(l=RegExp("</"+c[2],"g")),u=p):void 0!==c[3]&&(u=p):u===p?">"===c[0]?(u=null!=l?l:f,v=-1):void 0===c[1]?v=-2:(v=u.lastIndex-c[2].length,d=c[1],u=void 0===c[3]?p:'"'===c[3]?$:g):u===$||u===g?u=p:u===_||u===m?u=f:(u=p,l=void 0);const w=u===p&&t[i+1].startsWith("/>")?" ":"";r+=u===f?s+h:v>=0?(e.push(d),s.slice(0,v)+o$2+s.slice(v)+n$3+w):s+n$3+(-2===v?(e.push(void 0),i):w);}return [P(t,r+(t[s]||"<?>")+(2===i?"</svg>":"")),e]};class N{constructor({strings:t,_$litType$:i},e){let h;this.parts=[];let r=0,d=0;const c=t.length-1,v=this.parts,[a,f]=V(t,i);if(this.el=N.createElement(a,e),C.currentNode=this.el.content,2===i){const t=this.el.content,i=t.firstChild;i.remove(),t.append(...i.childNodes);}for(;null!==(h=C.nextNode())&&v.length<c;){if(1===h.nodeType){if(h.hasAttributes()){const t=[];for(const i of h.getAttributeNames())if(i.endsWith(o$2)||i.startsWith(n$3)){const s=f[d++];if(t.push(i),void 0!==s){const t=h.getAttribute(s.toLowerCase()+o$2).split(n$3),i=/([.?@])?(.*)/.exec(s);v.push({type:1,index:r,name:i[2],strings:t,ctor:"."===i[1]?H:"?"===i[1]?L:"@"===i[1]?z:k});}else v.push({type:6,index:r});}for(const i of t)h.removeAttribute(i);}if(y.test(h.tagName)){const t=h.textContent.split(n$3),i=t.length-1;if(i>0){h.textContent=s$1?s$1.emptyScript:"";for(let s=0;s<i;s++)h.append(t[s],u()),C.nextNode(),v.push({type:2,index:++r});h.append(t[i],u());}}}else if(8===h.nodeType)if(h.data===l$2)v.push({type:2,index:r});else {let t=-1;for(;-1!==(t=h.data.indexOf(n$3,t+1));)v.push({type:7,index:r}),t+=n$3.length-1;}r++;}}static createElement(t,i){const s=r.createElement("template");return s.innerHTML=t,s}}function S(t,i,s=t,e){var o,n,l,h;if(i===T)return i;let r=void 0!==e?null===(o=s._$Co)||void 0===o?void 0:o[e]:s._$Cl;const u=d(i)?void 0:i._$litDirective$;return (null==r?void 0:r.constructor)!==u&&(null===(n=null==r?void 0:r._$AO)||void 0===n||n.call(r,!1),void 0===u?r=void 0:(r=new u(t),r._$AT(t,s,e)),void 0!==e?(null!==(l=(h=s)._$Co)&&void 0!==l?l:h._$Co=[])[e]=r:s._$Cl=r),void 0!==r&&(i=S(t,r._$AS(t,i.values),r,e)),i}class M{constructor(t,i){this._$AV=[],this._$AN=void 0,this._$AD=t,this._$AM=i;}get parentNode(){return this._$AM.parentNode}get _$AU(){return this._$AM._$AU}u(t){var i;const{el:{content:s},parts:e}=this._$AD,o=(null!==(i=null==t?void 0:t.creationScope)&&void 0!==i?i:r).importNode(s,!0);C.currentNode=o;let n=C.nextNode(),l=0,h=0,u=e[0];for(;void 0!==u;){if(l===u.index){let i;2===u.type?i=new R(n,n.nextSibling,this,t):1===u.type?i=new u.ctor(n,u.name,u.strings,this,t):6===u.type&&(i=new Z(n,this,t)),this._$AV.push(i),u=e[++h];}l!==(null==u?void 0:u.index)&&(n=C.nextNode(),l++);}return C.currentNode=r,o}v(t){let i=0;for(const s of this._$AV)void 0!==s&&(void 0!==s.strings?(s._$AI(t,s,i),i+=s.strings.length-2):s._$AI(t[i])),i++;}}class R{constructor(t,i,s,e){var o;this.type=2,this._$AH=A,this._$AN=void 0,this._$AA=t,this._$AB=i,this._$AM=s,this.options=e,this._$Cp=null===(o=null==e?void 0:e.isConnected)||void 0===o||o;}get _$AU(){var t,i;return null!==(i=null===(t=this._$AM)||void 0===t?void 0:t._$AU)&&void 0!==i?i:this._$Cp}get parentNode(){let t=this._$AA.parentNode;const i=this._$AM;return void 0!==i&&11===(null==t?void 0:t.nodeType)&&(t=i.parentNode),t}get startNode(){return this._$AA}get endNode(){return this._$AB}_$AI(t,i=this){t=S(this,t,i),d(t)?t===A||null==t||""===t?(this._$AH!==A&&this._$AR(),this._$AH=A):t!==this._$AH&&t!==T&&this._(t):void 0!==t._$litType$?this.g(t):void 0!==t.nodeType?this.$(t):v(t)?this.T(t):this._(t);}k(t){return this._$AA.parentNode.insertBefore(t,this._$AB)}$(t){this._$AH!==t&&(this._$AR(),this._$AH=this.k(t));}_(t){this._$AH!==A&&d(this._$AH)?this._$AA.nextSibling.data=t:this.$(r.createTextNode(t)),this._$AH=t;}g(t){var i;const{values:s,_$litType$:e}=t,o="number"==typeof e?this._$AC(t):(void 0===e.el&&(e.el=N.createElement(P(e.h,e.h[0]),this.options)),e);if((null===(i=this._$AH)||void 0===i?void 0:i._$AD)===o)this._$AH.v(s);else {const t=new M(o,this),i=t.u(this.options);t.v(s),this.$(i),this._$AH=t;}}_$AC(t){let i=E.get(t.strings);return void 0===i&&E.set(t.strings,i=new N(t)),i}T(t){c(this._$AH)||(this._$AH=[],this._$AR());const i=this._$AH;let s,e=0;for(const o of t)e===i.length?i.push(s=new R(this.k(u()),this.k(u()),this,this.options)):s=i[e],s._$AI(o),e++;e<i.length&&(this._$AR(s&&s._$AB.nextSibling,e),i.length=e);}_$AR(t=this._$AA.nextSibling,i){var s;for(null===(s=this._$AP)||void 0===s||s.call(this,!1,!0,i);t&&t!==this._$AB;){const i=t.nextSibling;t.remove(),t=i;}}setConnected(t){var i;void 0===this._$AM&&(this._$Cp=t,null===(i=this._$AP)||void 0===i||i.call(this,t));}}class k{constructor(t,i,s,e,o){this.type=1,this._$AH=A,this._$AN=void 0,this.element=t,this.name=i,this._$AM=e,this.options=o,s.length>2||""!==s[0]||""!==s[1]?(this._$AH=Array(s.length-1).fill(new String),this.strings=s):this._$AH=A;}get tagName(){return this.element.tagName}get _$AU(){return this._$AM._$AU}_$AI(t,i=this,s,e){const o=this.strings;let n=!1;if(void 0===o)t=S(this,t,i,0),n=!d(t)||t!==this._$AH&&t!==T,n&&(this._$AH=t);else {const e=t;let l,h;for(t=o[0],l=0;l<o.length-1;l++)h=S(this,e[s+l],i,l),h===T&&(h=this._$AH[l]),n||(n=!d(h)||h!==this._$AH[l]),h===A?t=A:t!==A&&(t+=(null!=h?h:"")+o[l+1]),this._$AH[l]=h;}n&&!e&&this.j(t);}j(t){t===A?this.element.removeAttribute(this.name):this.element.setAttribute(this.name,null!=t?t:"");}}class H extends k{constructor(){super(...arguments),this.type=3;}j(t){this.element[this.name]=t===A?void 0:t;}}const I=s$1?s$1.emptyScript:"";class L extends k{constructor(){super(...arguments),this.type=4;}j(t){t&&t!==A?this.element.setAttribute(this.name,I):this.element.removeAttribute(this.name);}}class z extends k{constructor(t,i,s,e,o){super(t,i,s,e,o),this.type=5;}_$AI(t,i=this){var s;if((t=null!==(s=S(this,t,i,0))&&void 0!==s?s:A)===T)return;const e=this._$AH,o=t===A&&e!==A||t.capture!==e.capture||t.once!==e.once||t.passive!==e.passive,n=t!==A&&(e===A||o);o&&this.element.removeEventListener(this.name,this,e),n&&this.element.addEventListener(this.name,this,t),this._$AH=t;}handleEvent(t){var i,s;"function"==typeof this._$AH?this._$AH.call(null!==(s=null===(i=this.options)||void 0===i?void 0:i.host)&&void 0!==s?s:this.element,t):this._$AH.handleEvent(t);}}class Z{constructor(t,i,s){this.element=t,this.type=6,this._$AN=void 0,this._$AM=i,this.options=s;}get _$AU(){return this._$AM._$AU}_$AI(t){S(this,t);}}const B=i$2.litHtmlPolyfillSupport;null==B||B(N,R),(null!==(t$2=i$2.litHtmlVersions)&&void 0!==t$2?t$2:i$2.litHtmlVersions=[]).push("2.8.0");const D=(t,i,s)=>{var e,o;const n=null!==(e=null==s?void 0:s.renderBefore)&&void 0!==e?e:i;let l=n._$litPart$;if(void 0===l){const t=null!==(o=null==s?void 0:s.renderBefore)&&void 0!==o?o:null;n._$litPart$=l=new R(i.insertBefore(u(),t),t,void 0,null!=s?s:{});}return l._$AI(t),l};

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */var l$1,o$1;class s extends u$1{constructor(){super(...arguments),this.renderOptions={host:this},this._$Do=void 0;}createRenderRoot(){var t,e;const i=super.createRenderRoot();return null!==(t=(e=this.renderOptions).renderBefore)&&void 0!==t||(e.renderBefore=i.firstChild),i}update(t){const i=this.render();this.hasUpdated||(this.renderOptions.isConnected=this.isConnected),super.update(t),this._$Do=D(i,this.renderRoot,this.renderOptions);}connectedCallback(){var t;super.connectedCallback(),null===(t=this._$Do)||void 0===t||t.setConnected(!0);}disconnectedCallback(){var t;super.disconnectedCallback(),null===(t=this._$Do)||void 0===t||t.setConnected(!1);}render(){return T}}s.finalized=!0,s._$litElement$=!0,null===(l$1=globalThis.litElementHydrateSupport)||void 0===l$1||l$1.call(globalThis,{LitElement:s});const n$2=globalThis.litElementPolyfillSupport;null==n$2||n$2({LitElement:s});(null!==(o$1=globalThis.litElementVersions)&&void 0!==o$1?o$1:globalThis.litElementVersions=[]).push("3.3.3");

/*! *****************************************************************************
Copyright (c) Microsoft Corporation.

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH
REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY
AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM
LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR
OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
PERFORMANCE OF THIS SOFTWARE.
***************************************************************************** */

function __decorate(decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
}

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const e$3=e=>n=>"function"==typeof n?((e,n)=>(customElements.define(e,n),n))(e,n):((e,n)=>{const{kind:t,elements:s}=n;return {kind:t,elements:s,finisher(n){customElements.define(e,n);}}})(e,n);

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const i$1=(i,e)=>"method"===e.kind&&e.descriptor&&!("value"in e.descriptor)?{...e,finisher(n){n.createProperty(e.key,i);}}:{kind:"field",key:Symbol(),placement:"own",descriptor:{},originalKey:e.key,initializer(){"function"==typeof e.initializer&&(this[e.key]=e.initializer.call(this));},finisher(n){n.createProperty(e.key,i);}},e$2=(i,e,n)=>{e.constructor.createProperty(n,i);};function n$1(n){return (t,o)=>void 0!==o?e$2(n,t,o):i$1(n,t)}

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */function t$1(t){return n$1({...t,state:!0})}

/**
 * @license
 * Copyright 2021 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */var n;null!=(null===(n=window.HTMLSlotElement)||void 0===n?void 0:n.prototype.assignedElements)?(o,n)=>o.assignedElements(n):(o,n)=>o.assignedNodes(n).filter((o=>o.nodeType===Node.ELEMENT_NODE));

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */
const t={ATTRIBUTE:1,CHILD:2,PROPERTY:3,BOOLEAN_ATTRIBUTE:4,EVENT:5,ELEMENT:6},e$1=t=>(...e)=>({_$litDirective$:t,values:e});class i{constructor(t){}get _$AU(){return this._$AM._$AU}_$AT(t,e,i){this._$Ct=t,this._$AM=e,this._$Ci=i;}_$AS(t,e){return this.update(t,e)}update(t,e){return this.render(...e)}}

/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */class e extends i{constructor(i){if(super(i),this.et=A,i.type!==t.CHILD)throw Error(this.constructor.directiveName+"() can only be used in child bindings")}render(r){if(r===A||null==r)return this.ft=void 0,this.et=r;if(r===T)return r;if("string"!=typeof r)throw Error(this.constructor.directiveName+"() called with a non-string value");if(r===this.et)return this.ft;this.et=r;const s=[r];return s.raw=s,this.ft={_$litType$:this.constructor.resultType,strings:s,values:[]}}}e.directiveName="unsafeHTML",e.resultType=1;const o=e$1(e);

/**
 * @license
 * Copyright 2018 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */const l=l=>null!=l?l:A;

var Module = void 0 !== Module ? Module : {};
var TreeSitter = function() {
  var e, t = "object" == typeof window ? { currentScript: window.document.currentScript } : null;

  class Parser {
    constructor () {
      this.initialize();
    }

    initialize () {
      throw new Error("cannot construct a Parser before calling `init()`");
    }

    static init (r) {
      return e || (Module = Object.assign({}, Module, r), e = new Promise(e => {
        var r, n = {};
        for (r in Module) Module.hasOwnProperty(r) && (n[r] = Module[r]);
        var o, s, _ = [], a = "./this.program", i = function(e, t) {
          throw t;
        }, u = !1, l = !1;
        u = "object" == typeof window, l = "function" == typeof importScripts, o = "object" == typeof process && "object" == typeof process.versions && "string" == typeof process.versions.node, s = !u && !o && !l;
        var d, c, m, f, p, h = "";
        o ? (h = l ? require("path").dirname(h) + "/" : __dirname + "/", d = function(e, t) {
          return f || (f = require("fs")), p || (p = require("path")), e = p.normalize(e), f.readFileSync(e, t ? null : "utf8");
        }, m = function(e) {
          var t = d(e, !0);
          return t.buffer || (t = new Uint8Array(t)), k(t.buffer), t;
        }, process.argv.length > 1 && (a = process.argv[1].replace(/\\/g, "/")), _ = process.argv.slice(2), "undefined" != typeof module && (module.exports = Module), i = function(e) {
          process.exit(e);
        }, Module.inspect = function() {
          return "[Emscripten Module object]";
        }) : s ? ("undefined" != typeof read && (d = function(e) {
          return read(e);
        }), m = function(e) {
          var t;
          return "function" == typeof readbuffer ? new Uint8Array(readbuffer(e)) : (k("object" == typeof (t = read(e, "binary"))), t);
        }, "undefined" != typeof scriptArgs ? _ = scriptArgs : void 0 !== arguments && (_ = arguments), "function" == typeof quit && (i = function(e) {
          quit(e);
        }), "undefined" != typeof print && ("undefined" == typeof console && (console = {}), console.log = print, console.warn = console.error = "undefined" != typeof printErr ? printErr : print)) : (u || l) && (l ? h = self.location.href : void 0 !== t && t.currentScript && (h = t.currentScript.src), h = 0 !== h.indexOf("blob:") ? h.substr(0, h.lastIndexOf("/") + 1) : "", d = function(e) {
          var t = new XMLHttpRequest;
          return t.open("GET", e, !1), t.send(null), t.responseText;
        }, l && (m = function(e) {
          var t = new XMLHttpRequest;
          return t.open("GET", e, !1), t.responseType = "arraybuffer", t.send(null), new Uint8Array(t.response);
        }), c = function(e, t, r) {
          var n = new XMLHttpRequest;
          n.open("GET", e, !0), n.responseType = "arraybuffer", n.onload = function() {
            200 == n.status || 0 == n.status && n.response ? t(n.response) : r();
          }, n.onerror = r, n.send(null);
        });
        Module.print || console.log.bind(console);
        var g = Module.printErr || console.warn.bind(console);
        for (r in n) n.hasOwnProperty(r) && (Module[r] = n[r]);
        n = null, Module.arguments && (_ = Module.arguments), Module.thisProgram && (a = Module.thisProgram), Module.quit && (i = Module.quit);
        var w = 16;
        var M, y = [];

        function b (e, t) {
          if (!M) {
            M = new WeakMap;
            for (var r = 0; r < K.length; r++) {
              var n = K.get(r);
              n && M.set(n, r);
            }
          }
          if (M.has(e)) {
            return M.get(e);
          }
          var o = function() {
            if (y.length) {
              return y.pop();
            }
            try {
              K.grow(1);
            }
            catch (e) {
              if (!(e instanceof RangeError)) {
                throw e;
              }
              throw "Unable to grow wasm table. Set ALLOW_TABLE_GROWTH.";
            }
            return K.length - 1;
          }();
          try {
            K.set(o, e);
          }
          catch (r) {
            if (!(r instanceof TypeError)) {
              throw r;
            }
            var s = function(e, t) {
              if ("function" == typeof WebAssembly.Function) {
                for (var r = { i: "i32", j: "i64", f: "f32", d: "f64" }, n = {
                  parameters: [],
                  results: "v" == t[0] ? [] : [r[t[0]]]
                }, o = 1; o < t.length; ++o) n.parameters.push(r[t[o]]);
                return new WebAssembly.Function(n, e);
              }
              var s = [1, 0, 1, 96], _ = t.slice(0, 1), a = t.slice(1), i = { i: 127, j: 126, f: 125, d: 124 };
              for (s.push(a.length), o = 0; o < a.length; ++o) s.push(i[a[o]]);
              "v" == _ ? s.push(0) : s = s.concat([1, i[_]]), s[1] = s.length - 2;
              var u = new Uint8Array([0, 97, 115, 109, 1, 0, 0, 0].concat(s, [2, 7, 1, 1, 101, 1, 102, 0, 0, 7, 5, 1, 1, 102, 0, 0])),
                l = new WebAssembly.Module(u);
              return new WebAssembly.Instance(l, { e: { f: e } }).exports.f;
            }(e, t);
            K.set(o, s);
          }
          return M.set(e, o), o;
        }

        var v, S = Module.dynamicLibraries || [];
        Module.wasmBinary && (v = Module.wasmBinary);
        var I, A = Module.noExitRuntime || !0;

        function x (e, t, r, n) {
          switch ("*" === (r = r || "i8").charAt(r.length - 1) && (r = "i32"), r) {
            case"i1":
            case"i8":
              q[e >> 0] = t;
              break;
            case"i16":
              R[e >> 1] = t;
              break;
            case"i32":
              W[e >> 2] = t;
              break;
            case"i64":
              ie = [t >>> 0, (ae = t, +Math.abs(ae) >= 1 ? ae > 0 ? (0 | Math.min(+Math.floor(ae / 4294967296), 4294967295)) >>> 0 : ~~+Math.ceil((ae - +(~~ae >>> 0)) / 4294967296) >>> 0 : 0)], W[e >> 2] = ie[0], W[e + 4 >> 2] = ie[1];
              break;
            case"float":
              L[e >> 2] = t;
              break;
            case"double":
              O[e >> 3] = t;
              break;
            default:
              se("invalid type for setValue: " + r);
          }
        }

        function N (e, t, r) {
          switch ("*" === (t = t || "i8").charAt(t.length - 1) && (t = "i32"), t) {
            case"i1":
            case"i8":
              return q[e >> 0];
            case"i16":
              return R[e >> 1];
            case"i32":
            case"i64":
              return W[e >> 2];
            case"float":
              return L[e >> 2];
            case"double":
              return O[e >> 3];
            default:
              se("invalid type for getValue: " + t);
          }
          return null;
        }

        "object" != typeof WebAssembly && se("no native wasm support detected");
        var P = !1;

        function k (e, t) {
          e || se("Assertion failed: " + t);
        }

        var F = 1;
        var C, q, T, R, W, L, O, j = "undefined" != typeof TextDecoder ? new TextDecoder("utf8") : void 0;

        function $ (e, t, r) {
          for (var n = t + r, o = t; e[o] && !(o >= n);) ++o;
          if (o - t > 16 && e.subarray && j) {
            return j.decode(e.subarray(t, o));
          }
          for (var s = ""; t < o;) {
            var _ = e[t++];
            if (128 & _) {
              var a = 63 & e[t++];
              if (192 != (224 & _)) {
                var i = 63 & e[t++];
                if ((_ = 224 == (240 & _) ? (15 & _) << 12 | a << 6 | i : (7 & _) << 18 | a << 12 | i << 6 | 63 & e[t++]) < 65536) {
                  s += String.fromCharCode(_);
                }
                else {
                  var u = _ - 65536;
                  s += String.fromCharCode(55296 | u >> 10, 56320 | 1023 & u);
                }
              }
              else {
                s += String.fromCharCode((31 & _) << 6 | a);
              }
            }
            else {
              s += String.fromCharCode(_);
            }
          }
          return s;
        }

        function Z (e, t) {
          return e ? $(T, e, t) : "";
        }

        function D (e, t, r, n) {
          if (!(n > 0)) {
            return 0;
          }
          for (var o = r, s = r + n - 1, _ = 0; _ < e.length; ++_) {
            var a = e.charCodeAt(_);
            if (a >= 55296 && a <= 57343) {
              a = 65536 + ((1023 & a) << 10) | 1023 & e.charCodeAt(++_);
            }
            if (a <= 127) {
              if (r >= s) {
                break;
              }
              t[r++] = a;
            }
            else if (a <= 2047) {
              if (r + 1 >= s) {
                break;
              }
              t[r++] = 192 | a >> 6, t[r++] = 128 | 63 & a;
            }
            else if (a <= 65535) {
              if (r + 2 >= s) {
                break;
              }
              t[r++] = 224 | a >> 12, t[r++] = 128 | a >> 6 & 63, t[r++] = 128 | 63 & a;
            }
            else {
              if (r + 3 >= s) {
                break;
              }
              t[r++] = 240 | a >> 18, t[r++] = 128 | a >> 12 & 63, t[r++] = 128 | a >> 6 & 63, t[r++] = 128 | 63 & a;
            }
          }
          return t[r] = 0, r - o;
        }

        function z (e, t, r) {
          return D(e, T, t, r);
        }

        function U (e) {
          for (var t = 0, r = 0; r < e.length; ++r) {
            var n = e.charCodeAt(r);
            n >= 55296 && n <= 57343 && (n = 65536 + ((1023 & n) << 10) | 1023 & e.charCodeAt(++r)), n <= 127 ? ++t : t += n <= 2047 ? 2 : n <= 65535 ? 3 : 4;
          }
          return t;
        }

        function H (e) {
          var t = U(e) + 1, r = Be(t);
          return D(e, q, r, t), r;
        }

        function G (e) {
          C = e, Module.HEAP8 = q = new Int8Array(e), Module.HEAP16 = R = new Int16Array(e), Module.HEAP32 = W = new Int32Array(e), Module.HEAPU8 = T = new Uint8Array(e), Module.HEAPU16 = new Uint16Array(e), Module.HEAPU32 = new Uint32Array(e), Module.HEAPF32 = L = new Float32Array(e), Module.HEAPF64 = O = new Float64Array(e);
        }

        var B = Module.INITIAL_MEMORY || 33554432;
        (I = Module.wasmMemory ? Module.wasmMemory : new WebAssembly.Memory({
          initial: B / 65536,
          maximum: 32768
        })) && (C = I.buffer), B = C.byteLength, G(C);
        var K = new WebAssembly.Table({ initial: 20, element: "anyfunc" }), V = [], X = [], Q = [], J = [], Y = !1;
        var ee = 0, re = null;

        function ne (e) {
          ee++, Module.monitorRunDependencies && Module.monitorRunDependencies(ee);
        }

        function oe (e) {
          if (ee--, Module.monitorRunDependencies && Module.monitorRunDependencies(ee), 0 == ee && (re)) {
            var t = re;
            re = null, t();
          }
        }

        function se (e) {
          throw Module.onAbort && Module.onAbort(e), g(e += ""), P = !0, e = "abort(" + e + "). Build with -s ASSERTIONS=1 for more info.", new WebAssembly.RuntimeError(e);
        }

        Module.preloadedImages = {}, Module.preloadedAudios = {}, Module.preloadedWasm = {};
        var _e, ae, ie, ue = "data:application/octet-stream;base64,";

        function le (e) {
          return e.startsWith(ue);
        }

        function de (e) {
          return e.startsWith("file://");
        }

        function ce (e) {
          try {
            if (e == _e && v) {
              return new Uint8Array(v);
            }
            if (m) {
              return m(e);
            }
            throw "both async and sync fetching of the wasm failed";
          }
          catch (e) {
            se(e);
          }
        }

        le(_e = "tree-sitter.wasm") || (_e = function(e) {
          return Module.locateFile ? Module.locateFile(e, h) : h + e;
        }(_e));
        var me = {}, fe = {
          get: function(e, t) {
            return me[t] || (me[t] = new WebAssembly.Global({ value: "i32", mutable: !0 })), me[t];
          }
        };

        function pe (e) {
          for (; e.length > 0;) {
            var t = e.shift();
            if ("function" != typeof t) {
              var r = t.func;
              "number" == typeof r ? void 0 === t.arg ? K.get(r)() : K.get(r)(t.arg) : r(void 0 === t.arg ? null : t.arg);
            }
            else {
              t(Module);
            }
          }
        }

        function he (e) {
          var t = 0;

          function r () {
            for (var r = 0, n = 1; ;) {
              var o = e[t++];
              if (r += (127 & o) * n, n *= 128, !(128 & o)) {
                break;
              }
            }
            return r;
          }

          if (e instanceof WebAssembly.Module) {
            var n = WebAssembly.Module.customSections(e, "dylink");
            k(0 != n.length, "need dylink section"), e = new Int8Array(n[0]);
          }
          else {
            k(1836278016 == new Uint32Array(new Uint8Array(e.subarray(0, 24)).buffer)[0], "need to see wasm magic number"), k(0 === e[8], "need the dylink section to be first"), t = 9, r(), k(6 === e[t]), k(e[++t] === "d".charCodeAt(0)), k(e[++t] === "y".charCodeAt(0)), k(e[++t] === "l".charCodeAt(0)), k(e[++t] === "i".charCodeAt(0)), k(e[++t] === "n".charCodeAt(0)), k(e[++t] === "k".charCodeAt(0)), t++;
          }
          var o = {};
          o.memorySize = r(), o.memoryAlign = r(), o.tableSize = r(), o.tableAlign = r();
          var s = r();
          o.neededDynlibs = [];
          for (var _ = 0; _ < s; ++_) {
            var a = r(), i = e.subarray(t, t + a);
            t += a;
            var u = $(i, 0);
            o.neededDynlibs.push(u);
          }
          return o;
        }

        var ge = 0;

        function we () {
          return A || ge > 0;
        }

        function Me (e) {
          return 0 == e.indexOf("dynCall_") || ["stackAlloc", "stackSave", "stackRestore"].includes(e) ? e : "_" + e;
        }

        function ye (e, t) {
          for (var r in e) if (e.hasOwnProperty(r)) {
            De.hasOwnProperty(r) || (De[r] = e[r]);
            var n = Me(r);
            Module.hasOwnProperty(n) || (Module[n] = e[r]);
          }
        }

        var be = { nextHandle: 1, loadedLibs: {}, loadedLibNames: {} };

        function ve (e, t, r) {
          return e.includes("j") ? function(e, t, r) {
            var n = Module["dynCall_" + e];
            return r && r.length ? n.apply(null, [t].concat(r)) : n.call(null, t);
          }(e, t, r) : K.get(t).apply(null, r);
        }

        var Ee = 5251072;

        function Se (e) {
          return ["__cpp_exception", "__wasm_apply_data_relocs", "__dso_handle", "__set_stack_limits"].includes(e);
        }

        function Ie (e, t) {
          var r = {};
          for (var n in e) {
            var o = e[n];
            "object" == typeof o && (o = o.value), "number" == typeof o && (o += t), r[n] = o;
          }
          return function(e) {
            for (var t in e) if (!Se(t)) {
              var r = !1, n = e[t];
              t.startsWith("orig$") && (t = t.split("$")[1], r = !0), me[t] || (me[t] = new WebAssembly.Global({
                value: "i32",
                mutable: !0
              })), (r || 0 == me[t].value) && ("function" == typeof n ? me[t].value = b(n) : "number" == typeof n ? me[t].value = n : g("unhandled export type for `" + t + "`: " + typeof n));
            }
          }(r), r;
        }

        function Ae (e, t) {
          var r, n;
          return t && (r = De["orig$" + e]), r || (r = De[e]), r || (r = Module[Me(e)]), !r && e.startsWith("invoke_") && (n = e.split("_")[1], r = function() {
            var e = He();
            try {
              return ve(n, arguments[0], Array.prototype.slice.call(arguments, 1));
            }
            catch (t) {
              if (Ge(e), t !== t + 0 && "longjmp" !== t) {
                throw t;
              }
              Ke(1, 0);
            }
          }), r;
        }

        function xe (e, t) {
          var r = he(e);

          function n () {
            var n = Math.pow(2, r.memoryAlign);
            n = Math.max(n, w);
            var o, s, _, a = (o = function(e) {
              if (Y) {
                return ze(e);
              }
              var t = Ee, r = t + e + 15 & -16;
              return Ee = r, me.__heap_base.value = r, t;
            }(r.memorySize + n), (s = n) || (s = w), Math.ceil(o / s) * s), i = K.length;
            K.grow(r.tableSize);
            for (var u = a; u < a + r.memorySize; u++) q[u] = 0;
            for (u = i; u < i + r.tableSize; u++) K.set(u, null);
            var l = new Proxy({}, {
              get: function(e, t) {
                switch (t) {
                  case"__memory_base":
                    return a;
                  case"__table_base":
                    return i;
                }
                if (t in De) {
                  return De[t];
                }
                var r;
                t in e || (e[t] = function() {
                  return r || (r = function(e) {
                    var t = Ae(e, !1);
                    return t || (t = _[e]), t;
                  }(t)), r.apply(null, arguments);
                });
                return e[t];
              }
            }), d = { "GOT.mem": new Proxy({}, fe), "GOT.func": new Proxy({}, fe), env: l, wasi_snapshot_preview1: l };

            function c (e) {
              for (var n = 0; n < r.tableSize; n++) {
                var o = K.get(i + n);
                o && M.set(o, i + n);
              }
              _ = Ie(e.exports, a), t.allowUndefined || Pe();
              var s = _.__wasm_call_ctors;
              return s || (s = _.__post_instantiate), s && (Y ? s() : X.push(s)), _;
            }

            if (t.loadAsync) {
              if (e instanceof WebAssembly.Module) {
                var m = new WebAssembly.Instance(e, d);
                return Promise.resolve(c(m));
              }
              return WebAssembly.instantiate(e, d).then(function(e) {
                return c(e.instance);
              });
            }
            var f = e instanceof WebAssembly.Module ? e : new WebAssembly.Module(e);
            return c(m = new WebAssembly.Instance(f, d));
          }

          return t.loadAsync ? r.neededDynlibs.reduce(function(e, r) {
            return e.then(function() {
              return Ne(r, t);
            });
          }, Promise.resolve()).then(function() {
            return n();
          }) : (r.neededDynlibs.forEach(function(e) {
            Ne(e, t);
          }), n());
        }

        function Ne (e, t) {
          "__main__" != e || be.loadedLibNames[e] || (be.loadedLibs[-1] = {
            refcount: 1 / 0,
            name: "__main__",
            module: Module.asm,
            global: !0
          }, be.loadedLibNames.__main__ = -1), t = t || { global: !0, nodelete: !0 };
          var r, n = be.loadedLibNames[e];
          if (n) {
            return r = be.loadedLibs[n], t.global && !r.global && (r.global = !0, "loading" !== r.module && ye(r.module)), t.nodelete && r.refcount !== 1 / 0 && (r.refcount = 1 / 0), r.refcount++, t.loadAsync ? Promise.resolve(n) : n;
          }

          function o (e) {
            if (t.fs) {
              var r = t.fs.readFile(e, { encoding: "binary" });
              return r instanceof Uint8Array || (r = new Uint8Array(r)), t.loadAsync ? Promise.resolve(r) : r;
            }
            return t.loadAsync ? (n = e, fetch(n, { credentials: "same-origin" }).then(function(e) {
              if (!e.ok) {
                throw "failed to load binary file at '" + n + "'";
              }
              return e.arrayBuffer();
            }).then(function(e) {
              return new Uint8Array(e);
            })) : m(e);
            var n;
          }

          function s () {
            if (void 0 !== Module.preloadedWasm && void 0 !== Module.preloadedWasm[e]) {
              var r = Module.preloadedWasm[e];
              return t.loadAsync ? Promise.resolve(r) : r;
            }
            return t.loadAsync ? o(e).then(function(e) {
              return xe(e, t);
            }) : xe(o(e), t);
          }

          function _ (e) {
            r.global && ye(e), r.module = e;
          }

          return n = be.nextHandle++, r = {
            refcount: t.nodelete ? 1 / 0 : 1,
            name: e,
            module: "loading",
            global: t.global
          }, be.loadedLibNames[e] = n, be.loadedLibs[n] = r, t.loadAsync ? s().then(function(e) {
            return _(e), n;
          }) : (_(s()), n);
        }

        function Pe () {
          for (var e in me) if (0 == me[e].value) {
            var t = Ae(e, !0);
            "function" == typeof t ? me[e].value = b(t, t.sig) : "number" == typeof t ? me[e].value = t : k(!1, "bad export type for `" + e + "`: " + typeof t);
          }
        }

        Module.___heap_base = Ee;
        var ke, Fe = new WebAssembly.Global({ value: "i32", mutable: !0 }, 5251072);

        function Ce () {
          se();
        }

        Module._abort = Ce, Ce.sig = "v", ke = o ? function() {
          var e = process.hrtime();
          return 1e3 * e[0] + e[1] / 1e6;
        } : "undefined" != typeof dateNow ? dateNow : function() {
          return performance.now();
        };
        var qe = !0;

        function Te (e, t) {
          var r, n;
          if (0 === e) {
            r = Date.now();
          }
          else {
            if (1 !== e && 4 !== e || !qe) {
              return n = 28, W[Ue() >> 2] = n, -1;
            }
            r = ke();
          }
          return W[t >> 2] = r / 1e3 | 0, W[t + 4 >> 2] = r % 1e3 * 1e3 * 1e3 | 0, 0;
        }

        function Re (e) {
          try {
            return I.grow(e - C.byteLength + 65535 >>> 16), G(I.buffer), 1;
          }
          catch (e) {
          }
        }

        function We (e) {
          Je(e);
        }

        Te.sig = "iii", We.sig = "vi";
        var Le = {
          mappings: {}, DEFAULT_POLLMASK: 5, umask: 511, calculateAt: function(e, t, r) {
            if ("/" === t[0]) {
              return t;
            }
            var n;
            if (-100 === e) {
              n = FS.cwd();
            }
            else {
              var o = FS.getStream(e);
              if (!o) {
                throw new FS.ErrnoError(8);
              }
              n = o.path;
            }
            if (0 == t.length) {
              if (!r) {
                throw new FS.ErrnoError(44);
              }
              return n;
            }
            return PATH.join2(n, t);
          }, doStat: function(e, t, r) {
            try {
              var n = e(t);
            }
            catch (e) {
              if (e && e.node && PATH.normalize(t) !== PATH.normalize(FS.getPath(e.node))) {
                return -54;
              }
              throw e;
            }
            return W[r >> 2] = n.dev, W[r + 4 >> 2] = 0, W[r + 8 >> 2] = n.ino, W[r + 12 >> 2] = n.mode, W[r + 16 >> 2] = n.nlink, W[r + 20 >> 2] = n.uid, W[r + 24 >> 2] = n.gid, W[r + 28 >> 2] = n.rdev, W[r + 32 >> 2] = 0, ie = [n.size >>> 0, (ae = n.size, +Math.abs(ae) >= 1 ? ae > 0 ? (0 | Math.min(+Math.floor(ae / 4294967296), 4294967295)) >>> 0 : ~~+Math.ceil((ae - +(~~ae >>> 0)) / 4294967296) >>> 0 : 0)], W[r + 40 >> 2] = ie[0], W[r + 44 >> 2] = ie[1], W[r + 48 >> 2] = 4096, W[r + 52 >> 2] = n.blocks, W[r + 56 >> 2] = n.atime.getTime() / 1e3 | 0, W[r + 60 >> 2] = 0, W[r + 64 >> 2] = n.mtime.getTime() / 1e3 | 0, W[r + 68 >> 2] = 0, W[r + 72 >> 2] = n.ctime.getTime() / 1e3 | 0, W[r + 76 >> 2] = 0, ie = [n.ino >>> 0, (ae = n.ino, +Math.abs(ae) >= 1 ? ae > 0 ? (0 | Math.min(+Math.floor(ae / 4294967296), 4294967295)) >>> 0 : ~~+Math.ceil((ae - +(~~ae >>> 0)) / 4294967296) >>> 0 : 0)], W[r + 80 >> 2] = ie[0], W[r + 84 >> 2] = ie[1], 0;
          }, doMsync: function(e, t, r, n, o) {
            var s = T.slice(e, e + r);
            FS.msync(t, s, o, r, n);
          }, doMkdir: function(e, t) {
            return "/" === (e = PATH.normalize(e))[e.length - 1] && (e = e.substr(0, e.length - 1)), FS.mkdir(e, t, 0), 0;
          }, doMknod: function(e, t, r) {
            switch (61440 & t) {
              case 32768:
              case 8192:
              case 24576:
              case 4096:
              case 49152:
                break;
              default:
                return -28;
            }
            return FS.mknod(e, t, r), 0;
          }, doReadlink: function(e, t, r) {
            if (r <= 0) {
              return -28;
            }
            var n = FS.readlink(e), o = Math.min(r, U(n)), s = q[t + o];
            return z(n, t, r + 1), q[t + o] = s, o;
          }, doAccess: function(e, t) {
            if (-8 & t) {
              return -28;
            }
            var r;
            if (!(r = FS.lookupPath(e, { follow: !0 }).node)) {
              return -44;
            }
            var n = "";
            return 4 & t && (n += "r"), 2 & t && (n += "w"), 1 & t && (n += "x"), n && FS.nodePermissions(r, n) ? -2 : 0;
          }, doDup: function(e, t, r) {
            var n = FS.getStream(r);
            return n && FS.close(n), FS.open(e, t, 0, r, r).fd;
          }, doReadv: function(e, t, r, n) {
            for (var o = 0, s = 0; s < r; s++) {
              var _ = W[t + 8 * s >> 2], a = W[t + (8 * s + 4) >> 2], i = FS.read(e, q, _, a, n);
              if (i < 0) {
                return -1;
              }
              if (o += i, i < a) {
                break;
              }
            }
            return o;
          }, doWritev: function(e, t, r, n) {
            for (var o = 0, s = 0; s < r; s++) {
              var _ = W[t + 8 * s >> 2], a = W[t + (8 * s + 4) >> 2], i = FS.write(e, q, _, a, n);
              if (i < 0) {
                return -1;
              }
              o += i;
            }
            return o;
          }, varargs: void 0, get: function() {
            return Le.varargs += 4, W[Le.varargs - 4 >> 2];
          }, getStr: function(e) {
            return Z(e);
          }, getStreamFromFD: function(e) {
            var t = FS.getStream(e);
            if (!t) {
              throw new FS.ErrnoError(8);
            }
            return t;
          }, get64: function(e, t) {
            return e;
          }
        };

        function Oe (e) {
          try {
            var t = Le.getStreamFromFD(e);
            return FS.close(t), 0;
          }
          catch (e) {
            return "undefined" != typeof FS && e instanceof FS.ErrnoError || se(e), e.errno;
          }
        }

        function je (e, t, r, n) {
          try {
            var o = Le.getStreamFromFD(e), s = Le.doWritev(o, t, r);
            return W[n >> 2] = s, 0;
          }
          catch (e) {
            return "undefined" != typeof FS && e instanceof FS.ErrnoError || se(e), e.errno;
          }
        }

        function $e (e) {
        }

        Oe.sig = "ii", je.sig = "iiiii", $e.sig = "vi";
        var Ze, De = {
          __heap_base: Ee,
          __indirect_function_table: K,
          __memory_base: 1024,
          __stack_pointer: Fe,
          __table_base: 1,
          abort: Ce,
          clock_gettime: Te,
          emscripten_memcpy_big: function(e, t, r) {
            T.copyWithin(e, t, t + r);
          },
          emscripten_resize_heap: function(e) {
            var t, r, n = T.length;
            if ((e >>>= 0) > 2147483648) {
              return !1;
            }
            for (var o = 1; o <= 4; o *= 2) {
              var s = n * (1 + .2 / o);
              if (s = Math.min(s, e + 100663296), Re(Math.min(2147483648, ((t = Math.max(e, s)) % (r = 65536) > 0 && (t += r - t % r), t)))) {
                return !0;
              }
            }
            return !1;
          },
          exit: We,
          fd_close: Oe,
          fd_seek: function(e, t, r, n, o) {
            try {
              var s = Le.getStreamFromFD(e), _ = 4294967296 * r + (t >>> 0);
              return _ <= -9007199254740992 || _ >= 9007199254740992 ? -61 : (FS.llseek(s, _, n), ie = [s.position >>> 0, (ae = s.position, +Math.abs(ae) >= 1 ? ae > 0 ? (0 | Math.min(+Math.floor(ae / 4294967296), 4294967295)) >>> 0 : ~~+Math.ceil((ae - +(~~ae >>> 0)) / 4294967296) >>> 0 : 0)], W[o >> 2] = ie[0], W[o + 4 >> 2] = ie[1], s.getdents && 0 === _ && 0 === n && (s.getdents = null), 0);
            }
            catch (e) {
              return "undefined" != typeof FS && e instanceof FS.ErrnoError || se(e), e.errno;
            }
          },
          fd_write: je,
          memory: I,
          setTempRet0: $e,
          tree_sitter_log_callback: function(e, t) {
            if (pt) {
              const r = Z(t);
              pt(r, 0 !== e);
            }
          },
          tree_sitter_parse_callback: function(e, t, r, n, o) {
            var s = ft(t, { row: r, column: n });
            "string" == typeof s ? (x(o, s.length, "i32"), function(e, t, r) {
              if (void 0 === r && (r = 2147483647), r < 2) {
                return 0;
              }
              for (var n = (r -= 2) < 2 * e.length ? r / 2 : e.length, o = 0; o < n; ++o) {
                var s = e.charCodeAt(o);
                R[t >> 1] = s, t += 2;
              }
              R[t >> 1] = 0;
            }(s, e, 10240)) : x(o, 0, "i32");
          }
        }, ze = (function() {
          var e = { env: De, wasi_snapshot_preview1: De, "GOT.mem": new Proxy(De, fe), "GOT.func": new Proxy(De, fe) };

          function t (e, t) {
            var r = e.exports;
            r = Ie(r, 1024), Module.asm = r;
            var n, o = he(t);
            o.neededDynlibs && (S = o.neededDynlibs.concat(S)), ye(r), n = Module.asm.__wasm_call_ctors, X.unshift(n), oe();
          }

          function r (e) {
            t(e.instance, e.module);
          }

          function n (t) {
            return function() {
              if (!v && (u || l)) {
                if ("function" == typeof fetch && !de(_e)) {
                  return fetch(_e, { credentials: "same-origin" }).then(function(e) {
                    if (!e.ok) {
                      throw "failed to load wasm binary file at '" + _e + "'";
                    }
                    return e.arrayBuffer();
                  }).catch(function() {
                    return ce(_e);
                  });
                }
                if (c) {
                  return new Promise(function(e, t) {
                    c(_e, function(t) {
                      e(new Uint8Array(t));
                    }, t);
                  });
                }
              }
              return Promise.resolve().then(function() {
                return ce(_e);
              });
            }().then(function(t) {
              return WebAssembly.instantiate(t, e);
            }).then(t, function(e) {
              g("failed to asynchronously prepare wasm: " + e), se(e);
            });
          }

          if (ne(), Module.instantiateWasm) {
            try {
              return Module.instantiateWasm(e, t);
            }
            catch (e) {
              return g("Module.instantiateWasm callback failed with error: " + e), !1;
            }
          }
          v || "function" != typeof WebAssembly.instantiateStreaming || le(_e) || de(_e) || "function" != typeof fetch ? n(r) : fetch(_e, { credentials: "same-origin" }).then(function(t) {
            return WebAssembly.instantiateStreaming(t, e).then(r, function(e) {
              return g("wasm streaming compile failed: " + e), g("falling back to ArrayBuffer instantiation"), n(r);
            });
          });
        }(), Module.___wasm_call_ctors = function() {
          return (Module.___wasm_call_ctors = Module.asm.__wasm_call_ctors).apply(null, arguments);
        }, Module._malloc = function() {
          return (ze = Module._malloc = Module.asm.malloc).apply(null, arguments);
        }), Ue = (Module._calloc = function() {
          return (Module._calloc = Module.asm.calloc).apply(null, arguments);
        }, Module._realloc = function() {
          return (Module._realloc = Module.asm.realloc).apply(null, arguments);
        }, Module._free = function() {
          return (Module._free = Module.asm.free).apply(null, arguments);
        }, Module._ts_language_symbol_count = function() {
          return (Module._ts_language_symbol_count = Module.asm.ts_language_symbol_count).apply(null, arguments);
        }, Module._ts_language_version = function() {
          return (Module._ts_language_version = Module.asm.ts_language_version).apply(null, arguments);
        }, Module._ts_language_field_count = function() {
          return (Module._ts_language_field_count = Module.asm.ts_language_field_count).apply(null, arguments);
        }, Module._ts_language_symbol_name = function() {
          return (Module._ts_language_symbol_name = Module.asm.ts_language_symbol_name).apply(null, arguments);
        }, Module._ts_language_symbol_for_name = function() {
          return (Module._ts_language_symbol_for_name = Module.asm.ts_language_symbol_for_name).apply(null, arguments);
        }, Module._ts_language_symbol_type = function() {
          return (Module._ts_language_symbol_type = Module.asm.ts_language_symbol_type).apply(null, arguments);
        }, Module._ts_language_field_name_for_id = function() {
          return (Module._ts_language_field_name_for_id = Module.asm.ts_language_field_name_for_id).apply(null, arguments);
        }, Module._memcpy = function() {
          return (Module._memcpy = Module.asm.memcpy).apply(null, arguments);
        }, Module._ts_parser_delete = function() {
          return (Module._ts_parser_delete = Module.asm.ts_parser_delete).apply(null, arguments);
        }, Module._ts_parser_reset = function() {
          return (Module._ts_parser_reset = Module.asm.ts_parser_reset).apply(null, arguments);
        }, Module._ts_parser_set_language = function() {
          return (Module._ts_parser_set_language = Module.asm.ts_parser_set_language).apply(null, arguments);
        }, Module._ts_parser_timeout_micros = function() {
          return (Module._ts_parser_timeout_micros = Module.asm.ts_parser_timeout_micros).apply(null, arguments);
        }, Module._ts_parser_set_timeout_micros = function() {
          return (Module._ts_parser_set_timeout_micros = Module.asm.ts_parser_set_timeout_micros).apply(null, arguments);
        }, Module._memmove = function() {
          return (Module._memmove = Module.asm.memmove).apply(null, arguments);
        }, Module._memcmp = function() {
          return (Module._memcmp = Module.asm.memcmp).apply(null, arguments);
        }, Module._ts_query_new = function() {
          return (Module._ts_query_new = Module.asm.ts_query_new).apply(null, arguments);
        }, Module._ts_query_delete = function() {
          return (Module._ts_query_delete = Module.asm.ts_query_delete).apply(null, arguments);
        }, Module._iswspace = function() {
          return (Module._iswspace = Module.asm.iswspace).apply(null, arguments);
        }, Module._iswalnum = function() {
          return (Module._iswalnum = Module.asm.iswalnum).apply(null, arguments);
        }, Module._ts_query_pattern_count = function() {
          return (Module._ts_query_pattern_count = Module.asm.ts_query_pattern_count).apply(null, arguments);
        }, Module._ts_query_capture_count = function() {
          return (Module._ts_query_capture_count = Module.asm.ts_query_capture_count).apply(null, arguments);
        }, Module._ts_query_string_count = function() {
          return (Module._ts_query_string_count = Module.asm.ts_query_string_count).apply(null, arguments);
        }, Module._ts_query_capture_name_for_id = function() {
          return (Module._ts_query_capture_name_for_id = Module.asm.ts_query_capture_name_for_id).apply(null, arguments);
        }, Module._ts_query_string_value_for_id = function() {
          return (Module._ts_query_string_value_for_id = Module.asm.ts_query_string_value_for_id).apply(null, arguments);
        }, Module._ts_query_predicates_for_pattern = function() {
          return (Module._ts_query_predicates_for_pattern = Module.asm.ts_query_predicates_for_pattern).apply(null, arguments);
        }, Module._ts_tree_copy = function() {
          return (Module._ts_tree_copy = Module.asm.ts_tree_copy).apply(null, arguments);
        }, Module._ts_tree_delete = function() {
          return (Module._ts_tree_delete = Module.asm.ts_tree_delete).apply(null, arguments);
        }, Module._ts_init = function() {
          return (Module._ts_init = Module.asm.ts_init).apply(null, arguments);
        }, Module._ts_parser_new_wasm = function() {
          return (Module._ts_parser_new_wasm = Module.asm.ts_parser_new_wasm).apply(null, arguments);
        }, Module._ts_parser_enable_logger_wasm = function() {
          return (Module._ts_parser_enable_logger_wasm = Module.asm.ts_parser_enable_logger_wasm).apply(null, arguments);
        }, Module._ts_parser_parse_wasm = function() {
          return (Module._ts_parser_parse_wasm = Module.asm.ts_parser_parse_wasm).apply(null, arguments);
        }, Module._ts_language_type_is_named_wasm = function() {
          return (Module._ts_language_type_is_named_wasm = Module.asm.ts_language_type_is_named_wasm).apply(null, arguments);
        }, Module._ts_language_type_is_visible_wasm = function() {
          return (Module._ts_language_type_is_visible_wasm = Module.asm.ts_language_type_is_visible_wasm).apply(null, arguments);
        }, Module._ts_tree_root_node_wasm = function() {
          return (Module._ts_tree_root_node_wasm = Module.asm.ts_tree_root_node_wasm).apply(null, arguments);
        }, Module._ts_tree_edit_wasm = function() {
          return (Module._ts_tree_edit_wasm = Module.asm.ts_tree_edit_wasm).apply(null, arguments);
        }, Module._ts_tree_get_changed_ranges_wasm = function() {
          return (Module._ts_tree_get_changed_ranges_wasm = Module.asm.ts_tree_get_changed_ranges_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_new_wasm = function() {
          return (Module._ts_tree_cursor_new_wasm = Module.asm.ts_tree_cursor_new_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_delete_wasm = function() {
          return (Module._ts_tree_cursor_delete_wasm = Module.asm.ts_tree_cursor_delete_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_reset_wasm = function() {
          return (Module._ts_tree_cursor_reset_wasm = Module.asm.ts_tree_cursor_reset_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_goto_first_child_wasm = function() {
          return (Module._ts_tree_cursor_goto_first_child_wasm = Module.asm.ts_tree_cursor_goto_first_child_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_goto_next_sibling_wasm = function() {
          return (Module._ts_tree_cursor_goto_next_sibling_wasm = Module.asm.ts_tree_cursor_goto_next_sibling_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_goto_parent_wasm = function() {
          return (Module._ts_tree_cursor_goto_parent_wasm = Module.asm.ts_tree_cursor_goto_parent_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_node_type_id_wasm = function() {
          return (Module._ts_tree_cursor_current_node_type_id_wasm = Module.asm.ts_tree_cursor_current_node_type_id_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_node_is_named_wasm = function() {
          return (Module._ts_tree_cursor_current_node_is_named_wasm = Module.asm.ts_tree_cursor_current_node_is_named_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_node_is_missing_wasm = function() {
          return (Module._ts_tree_cursor_current_node_is_missing_wasm = Module.asm.ts_tree_cursor_current_node_is_missing_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_node_id_wasm = function() {
          return (Module._ts_tree_cursor_current_node_id_wasm = Module.asm.ts_tree_cursor_current_node_id_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_start_position_wasm = function() {
          return (Module._ts_tree_cursor_start_position_wasm = Module.asm.ts_tree_cursor_start_position_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_end_position_wasm = function() {
          return (Module._ts_tree_cursor_end_position_wasm = Module.asm.ts_tree_cursor_end_position_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_start_index_wasm = function() {
          return (Module._ts_tree_cursor_start_index_wasm = Module.asm.ts_tree_cursor_start_index_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_end_index_wasm = function() {
          return (Module._ts_tree_cursor_end_index_wasm = Module.asm.ts_tree_cursor_end_index_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_field_id_wasm = function() {
          return (Module._ts_tree_cursor_current_field_id_wasm = Module.asm.ts_tree_cursor_current_field_id_wasm).apply(null, arguments);
        }, Module._ts_tree_cursor_current_node_wasm = function() {
          return (Module._ts_tree_cursor_current_node_wasm = Module.asm.ts_tree_cursor_current_node_wasm).apply(null, arguments);
        }, Module._ts_node_symbol_wasm = function() {
          return (Module._ts_node_symbol_wasm = Module.asm.ts_node_symbol_wasm).apply(null, arguments);
        }, Module._ts_node_child_count_wasm = function() {
          return (Module._ts_node_child_count_wasm = Module.asm.ts_node_child_count_wasm).apply(null, arguments);
        }, Module._ts_node_named_child_count_wasm = function() {
          return (Module._ts_node_named_child_count_wasm = Module.asm.ts_node_named_child_count_wasm).apply(null, arguments);
        }, Module._ts_node_child_wasm = function() {
          return (Module._ts_node_child_wasm = Module.asm.ts_node_child_wasm).apply(null, arguments);
        }, Module._ts_node_named_child_wasm = function() {
          return (Module._ts_node_named_child_wasm = Module.asm.ts_node_named_child_wasm).apply(null, arguments);
        }, Module._ts_node_child_by_field_id_wasm = function() {
          return (Module._ts_node_child_by_field_id_wasm = Module.asm.ts_node_child_by_field_id_wasm).apply(null, arguments);
        }, Module._ts_node_next_sibling_wasm = function() {
          return (Module._ts_node_next_sibling_wasm = Module.asm.ts_node_next_sibling_wasm).apply(null, arguments);
        }, Module._ts_node_prev_sibling_wasm = function() {
          return (Module._ts_node_prev_sibling_wasm = Module.asm.ts_node_prev_sibling_wasm).apply(null, arguments);
        }, Module._ts_node_next_named_sibling_wasm = function() {
          return (Module._ts_node_next_named_sibling_wasm = Module.asm.ts_node_next_named_sibling_wasm).apply(null, arguments);
        }, Module._ts_node_prev_named_sibling_wasm = function() {
          return (Module._ts_node_prev_named_sibling_wasm = Module.asm.ts_node_prev_named_sibling_wasm).apply(null, arguments);
        }, Module._ts_node_parent_wasm = function() {
          return (Module._ts_node_parent_wasm = Module.asm.ts_node_parent_wasm).apply(null, arguments);
        }, Module._ts_node_descendant_for_index_wasm = function() {
          return (Module._ts_node_descendant_for_index_wasm = Module.asm.ts_node_descendant_for_index_wasm).apply(null, arguments);
        }, Module._ts_node_named_descendant_for_index_wasm = function() {
          return (Module._ts_node_named_descendant_for_index_wasm = Module.asm.ts_node_named_descendant_for_index_wasm).apply(null, arguments);
        }, Module._ts_node_descendant_for_position_wasm = function() {
          return (Module._ts_node_descendant_for_position_wasm = Module.asm.ts_node_descendant_for_position_wasm).apply(null, arguments);
        }, Module._ts_node_named_descendant_for_position_wasm = function() {
          return (Module._ts_node_named_descendant_for_position_wasm = Module.asm.ts_node_named_descendant_for_position_wasm).apply(null, arguments);
        }, Module._ts_node_start_point_wasm = function() {
          return (Module._ts_node_start_point_wasm = Module.asm.ts_node_start_point_wasm).apply(null, arguments);
        }, Module._ts_node_end_point_wasm = function() {
          return (Module._ts_node_end_point_wasm = Module.asm.ts_node_end_point_wasm).apply(null, arguments);
        }, Module._ts_node_start_index_wasm = function() {
          return (Module._ts_node_start_index_wasm = Module.asm.ts_node_start_index_wasm).apply(null, arguments);
        }, Module._ts_node_end_index_wasm = function() {
          return (Module._ts_node_end_index_wasm = Module.asm.ts_node_end_index_wasm).apply(null, arguments);
        }, Module._ts_node_to_string_wasm = function() {
          return (Module._ts_node_to_string_wasm = Module.asm.ts_node_to_string_wasm).apply(null, arguments);
        }, Module._ts_node_children_wasm = function() {
          return (Module._ts_node_children_wasm = Module.asm.ts_node_children_wasm).apply(null, arguments);
        }, Module._ts_node_named_children_wasm = function() {
          return (Module._ts_node_named_children_wasm = Module.asm.ts_node_named_children_wasm).apply(null, arguments);
        }, Module._ts_node_descendants_of_type_wasm = function() {
          return (Module._ts_node_descendants_of_type_wasm = Module.asm.ts_node_descendants_of_type_wasm).apply(null, arguments);
        }, Module._ts_node_is_named_wasm = function() {
          return (Module._ts_node_is_named_wasm = Module.asm.ts_node_is_named_wasm).apply(null, arguments);
        }, Module._ts_node_has_changes_wasm = function() {
          return (Module._ts_node_has_changes_wasm = Module.asm.ts_node_has_changes_wasm).apply(null, arguments);
        }, Module._ts_node_has_error_wasm = function() {
          return (Module._ts_node_has_error_wasm = Module.asm.ts_node_has_error_wasm).apply(null, arguments);
        }, Module._ts_node_is_missing_wasm = function() {
          return (Module._ts_node_is_missing_wasm = Module.asm.ts_node_is_missing_wasm).apply(null, arguments);
        }, Module._ts_query_matches_wasm = function() {
          return (Module._ts_query_matches_wasm = Module.asm.ts_query_matches_wasm).apply(null, arguments);
        }, Module._ts_query_captures_wasm = function() {
          return (Module._ts_query_captures_wasm = Module.asm.ts_query_captures_wasm).apply(null, arguments);
        }, Module._iswdigit = function() {
          return (Module._iswdigit = Module.asm.iswdigit).apply(null, arguments);
        }, Module._iswalpha = function() {
          return (Module._iswalpha = Module.asm.iswalpha).apply(null, arguments);
        }, Module._iswlower = function() {
          return (Module._iswlower = Module.asm.iswlower).apply(null, arguments);
        }, Module._towupper = function() {
          return (Module._towupper = Module.asm.towupper).apply(null, arguments);
        }, Module.___errno_location = function() {
          return (Ue = Module.___errno_location = Module.asm.__errno_location).apply(null, arguments);
        }), He = (Module._memchr = function() {
          return (Module._memchr = Module.asm.memchr).apply(null, arguments);
        }, Module._strlen = function() {
          return (Module._strlen = Module.asm.strlen).apply(null, arguments);
        }, Module.stackSave = function() {
          return (He = Module.stackSave = Module.asm.stackSave).apply(null, arguments);
        }), Ge = Module.stackRestore = function() {
          return (Ge = Module.stackRestore = Module.asm.stackRestore).apply(null, arguments);
        }, Be = Module.stackAlloc = function() {
          return (Be = Module.stackAlloc = Module.asm.stackAlloc).apply(null, arguments);
        }, Ke = Module._setThrew = function() {
          return (Ke = Module._setThrew = Module.asm.setThrew).apply(null, arguments);
        };
        Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEED2Ev = function() {
          return (Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEED2Ev = Module.asm._ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEED2Ev).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9__grow_byEmmmmmm = function() {
          return (Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9__grow_byEmmmmmm = Module.asm._ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9__grow_byEmmmmmm).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE6__initEPKcm = function() {
          return (Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE6__initEPKcm = Module.asm._ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE6__initEPKcm).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE7reserveEm = function() {
          return (Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE7reserveEm = Module.asm._ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE7reserveEm).apply(null, arguments);
        }, Module.__ZNKSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE4copyEPcmm = function() {
          return (Module.__ZNKSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE4copyEPcmm = Module.asm._ZNKSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE4copyEPcmm).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9push_backEc = function() {
          return (Module.__ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9push_backEc = Module.asm._ZNSt3__212basic_stringIcNS_11char_traitsIcEENS_9allocatorIcEEE9push_backEc).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEED2Ev = function() {
          return (Module.__ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEED2Ev = Module.asm._ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEED2Ev).apply(null, arguments);
        }, Module.__ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEE9push_backEw = function() {
          return (Module.__ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEE9push_backEw = Module.asm._ZNSt3__212basic_stringIwNS_11char_traitsIwEENS_9allocatorIwEEE9push_backEw).apply(null, arguments);
        }, Module.__Znwm = function() {
          return (Module.__Znwm = Module.asm._Znwm).apply(null, arguments);
        }, Module.__ZdlPv = function() {
          return (Module.__ZdlPv = Module.asm._ZdlPv).apply(null, arguments);
        }, Module.dynCall_jiji = function() {
          return (Module.dynCall_jiji = Module.asm.dynCall_jiji).apply(null, arguments);
        }, Module._orig$ts_parser_timeout_micros = function() {
          return (Module._orig$ts_parser_timeout_micros = Module.asm.orig$ts_parser_timeout_micros).apply(null, arguments);
        }, Module._orig$ts_parser_set_timeout_micros = function() {
          return (Module._orig$ts_parser_set_timeout_micros = Module.asm.orig$ts_parser_set_timeout_micros).apply(null, arguments);
        };

        function Ve (e) {
          this.name = "ExitStatus", this.message = "Program terminated with exit(" + e + ")", this.status = e;
        }

        Module.allocate = function(e, t) {
          var r;
          return r = t == F ? Be(e.length) : ze(e.length), e.subarray || e.slice ? T.set(e, r) : T.set(new Uint8Array(e), r), r;
        };
        re = function e () {
          Ze || Qe(), Ze || (re = e);
        };
        var Xe = !1;

        function Qe (e) {
          function t () {
            Ze || (Ze = !0, Module.calledRun = !0, P || (Y = !0, pe(X), pe(Q), Module.onRuntimeInitialized && Module.onRuntimeInitialized(), Ye && function(e) {
              var t = Module._main;
              if (t) {
                var r = (e = e || []).length + 1, n = Be(4 * (r + 1));
                W[n >> 2] = H(a);
                for (var o = 1; o < r; o++) W[(n >> 2) + o] = H(e[o - 1]);
                W[(n >> 2) + r] = 0;
                try {
                  Je(t(r, n), !0);
                }
                catch (e) {
                  if (e instanceof Ve) {
                    return;
                  }
                  if ("unwind" == e) {
                    return;
                  }
                  var s = e;
                  e && "object" == typeof e && e.stack && (s = [e, e.stack]), g("exception thrown: " + s), i(1, e);
                } finally {
                }
              }
            }(e), function() {
              if (Module.postRun) {
                for ("function" == typeof Module.postRun && (Module.postRun = [Module.postRun]); Module.postRun.length;) e = Module.postRun.shift(), J.unshift(e);
              }
              var e;
              pe(J);
            }()));
          }

          e = e || _, ee > 0 || !Xe && (function() {
            if (S.length) {
              if (!m) {
                return ne(), void S.reduce(function(e, t) {
                  return e.then(function() {
                    return Ne(t, { loadAsync: !0, global: !0, nodelete: !0, allowUndefined: !0 });
                  });
                }, Promise.resolve()).then(function() {
                  oe(), Pe();
                });
              }
              S.forEach(function(e) {
                Ne(e, { global: !0, nodelete: !0, allowUndefined: !0 });
              }), Pe();
            }
            else {
              Pe();
            }
          }(), Xe = !0, ee > 0) || (!function() {
            if (Module.preRun) {
              for ("function" == typeof Module.preRun && (Module.preRun = [Module.preRun]); Module.preRun.length;) e = Module.preRun.shift(), V.unshift(e);
            }
            var e;
            pe(V);
          }(), ee > 0 || (Module.setStatus ? (Module.setStatus("Running..."), setTimeout(function() {
            setTimeout(function() {
              Module.setStatus("");
            }, 1), t();
          }, 1)) : t()));
        }

        function Je (e, t) {
          t && we() && 0 === e || (we() || (Module.onExit && Module.onExit(e), P = !0), i(e, new Ve(e)));
        }

        if (Module.run = Qe, Module.preInit) {
          for ("function" == typeof Module.preInit && (Module.preInit = [Module.preInit]); Module.preInit.length > 0;) Module.preInit.pop()();
        }
        var Ye = !0;
        Module.noInitialRun && (Ye = !1), Qe();
        const et = Module, tt = {}, rt = 4, nt = 5 * rt, ot = 2 * rt, st = 2 * rt + 2 * ot, _t = { row: 0, column: 0 },
          at = /[\w-.]*/g, it = 1, ut = 2, lt = /^_?tree_sitter_\w+/;
        var dt, ct, mt, ft, pt;

        class ParserImpl {
          static init () {
            mt = et._ts_init(), dt = N(mt, "i32"), ct = N(mt + rt, "i32");
          }

          initialize () {
            et._ts_parser_new_wasm(), this[0] = N(mt, "i32"), this[1] = N(mt + rt, "i32");
          }

          delete () {
            et._ts_parser_delete(this[0]), et._free(this[1]), this[0] = 0, this[1] = 0;
          }

          setLanguage (e) {
            let t;
            if (e) {
              if (e.constructor !== Language) {
                throw new Error("Argument must be a Language");
              }
              {
                t = e[0];
                const r = et._ts_language_version(t);
                if (r < ct || dt < r) {
                  throw new Error(`Incompatible language version ${r}. ` + `Compatibility range ${ct} through ${dt}.`);
                }
              }
            }
            else {
              t = 0, e = null;
            }
            return this.language = e, et._ts_parser_set_language(this[0], t), this;
          }

          getLanguage () {
            return this.language;
          }

          parse (e, t, r) {
            if ("string" == typeof e) {
              ft = ((t, r, n) => e.slice(t, n));
            }
            else {
              if ("function" != typeof e) {
                throw new Error("Argument must be a string or a function");
              }
              ft = e;
            }
            this.logCallback ? (pt = this.logCallback, et._ts_parser_enable_logger_wasm(this[0], 1)) : (pt = null, et._ts_parser_enable_logger_wasm(this[0], 0));
            let n = 0, o = 0;
            if (r && r.includedRanges) {
              n = r.includedRanges.length;
              let e = o = et._calloc(n, st);
              for (let t = 0; t < n; t++) At(e, r.includedRanges[t]), e += st;
            }
            const s = et._ts_parser_parse_wasm(this[0], this[1], t ? t[0] : 0, o, n);
            if (!s) {
              throw ft = null, pt = null, new Error("Parsing failed");
            }
            const _ = new Tree(tt, s, this.language, ft);
            return ft = null, pt = null, _;
          }

          reset () {
            et._ts_parser_reset(this[0]);
          }

          setTimeoutMicros (e) {
            et._ts_parser_set_timeout_micros(this[0], e);
          }

          getTimeoutMicros () {
            return et._ts_parser_timeout_micros(this[0]);
          }

          setLogger (e) {
            if (e) {
              if ("function" != typeof e) {
                throw new Error("Logger callback must be a function");
              }
            }
            else {
              e = null;
            }
            return this.logCallback = e, this;
          }

          getLogger () {
            return this.logCallback;
          }
        }

        class Tree {
          constructor (e, t, r, n) {
            wt(e), this[0] = t, this.language = r, this.textCallback = n;
          }

          copy () {
            const e = et._ts_tree_copy(this[0]);
            return new Tree(tt, e, this.language, this.textCallback);
          }

          delete () {
            et._ts_tree_delete(this[0]), this[0] = 0;
          }

          edit (e) {
            !function(e) {
              let t = mt;
              St(t, e.startPosition), St(t += ot, e.oldEndPosition), St(t += ot, e.newEndPosition), x(t += ot, e.startIndex, "i32"), x(t += rt, e.oldEndIndex, "i32"), x(t += rt, e.newEndIndex, "i32"), t += rt;
            }(e), et._ts_tree_edit_wasm(this[0]);
          }

          get rootNode () {
            return et._ts_tree_root_node_wasm(this[0]), bt(this);
          }

          getLanguage () {
            return this.language;
          }

          walk () {
            return this.rootNode.walk();
          }

          getChangedRanges (e) {
            if (e.constructor !== Tree) {
              throw new TypeError("Argument must be a Tree");
            }
            et._ts_tree_get_changed_ranges_wasm(this[0], e[0]);
            const t = N(mt, "i32"), r = N(mt + rt, "i32"), n = new Array(t);
            if (t > 0) {
              let e = r;
              for (let r = 0; r < t; r++) n[r] = xt(e), e += st;
              et._free(r);
            }
            return n;
          }
        }

        class Node {
          constructor (e, t) {
            wt(e), this.tree = t;
          }

          get typeId () {
            return yt(this), et._ts_node_symbol_wasm(this.tree[0]);
          }

          get type () {
            return this.tree.language.types[this.typeId] || "ERROR";
          }

          get endPosition () {
            return yt(this), et._ts_node_end_point_wasm(this.tree[0]), It(mt);
          }

          get endIndex () {
            return yt(this), et._ts_node_end_index_wasm(this.tree[0]);
          }

          get text () {
            return ht(this.tree, this.startIndex, this.endIndex);
          }

          isNamed () {
            return yt(this), 1 === et._ts_node_is_named_wasm(this.tree[0]);
          }

          hasError () {
            return yt(this), 1 === et._ts_node_has_error_wasm(this.tree[0]);
          }

          hasChanges () {
            return yt(this), 1 === et._ts_node_has_changes_wasm(this.tree[0]);
          }

          isMissing () {
            return yt(this), 1 === et._ts_node_is_missing_wasm(this.tree[0]);
          }

          equals (e) {
            return this.id === e.id;
          }

          child (e) {
            return yt(this), et._ts_node_child_wasm(this.tree[0], e), bt(this.tree);
          }

          namedChild (e) {
            return yt(this), et._ts_node_named_child_wasm(this.tree[0], e), bt(this.tree);
          }

          childForFieldId (e) {
            return yt(this), et._ts_node_child_by_field_id_wasm(this.tree[0], e), bt(this.tree);
          }

          childForFieldName (e) {
            const t = this.tree.language.fields.indexOf(e);
            if (-1 !== t) {
              return this.childForFieldId(t);
            }
          }

          get childCount () {
            return yt(this), et._ts_node_child_count_wasm(this.tree[0]);
          }

          get namedChildCount () {
            return yt(this), et._ts_node_named_child_count_wasm(this.tree[0]);
          }

          get firstChild () {
            return this.child(0);
          }

          get firstNamedChild () {
            return this.namedChild(0);
          }

          get lastChild () {
            return this.child(this.childCount - 1);
          }

          get lastNamedChild () {
            return this.namedChild(this.namedChildCount - 1);
          }

          get children () {
            if (!this._children) {
              yt(this), et._ts_node_children_wasm(this.tree[0]);
              const e = N(mt, "i32"), t = N(mt + rt, "i32");
              if (this._children = new Array(e), e > 0) {
                let r = t;
                for (let t = 0; t < e; t++) this._children[t] = bt(this.tree, r), r += nt;
                et._free(t);
              }
            }
            return this._children;
          }

          get namedChildren () {
            if (!this._namedChildren) {
              yt(this), et._ts_node_named_children_wasm(this.tree[0]);
              const e = N(mt, "i32"), t = N(mt + rt, "i32");
              if (this._namedChildren = new Array(e), e > 0) {
                let r = t;
                for (let t = 0; t < e; t++) this._namedChildren[t] = bt(this.tree, r), r += nt;
                et._free(t);
              }
            }
            return this._namedChildren;
          }

          descendantsOfType (e, t, r) {
            Array.isArray(e) || (e = [e]), t || (t = _t), r || (r = _t);
            const n = [], o = this.tree.language.types;
            for (let t = 0, r = o.length; t < r; t++) e.includes(o[t]) && n.push(t);
            const s = et._malloc(rt * n.length);
            for (let e = 0, t = n.length; e < t; e++) x(s + e * rt, n[e], "i32");
            yt(this), et._ts_node_descendants_of_type_wasm(this.tree[0], s, n.length, t.row, t.column, r.row, r.column);
            const _ = N(mt, "i32"), a = N(mt + rt, "i32"), i = new Array(_);
            if (_ > 0) {
              let e = a;
              for (let t = 0; t < _; t++) i[t] = bt(this.tree, e), e += nt;
            }
            return et._free(a), et._free(s), i;
          }

          get nextSibling () {
            return yt(this), et._ts_node_next_sibling_wasm(this.tree[0]), bt(this.tree);
          }

          get previousSibling () {
            return yt(this), et._ts_node_prev_sibling_wasm(this.tree[0]), bt(this.tree);
          }

          get nextNamedSibling () {
            return yt(this), et._ts_node_next_named_sibling_wasm(this.tree[0]), bt(this.tree);
          }

          get previousNamedSibling () {
            return yt(this), et._ts_node_prev_named_sibling_wasm(this.tree[0]), bt(this.tree);
          }

          get parent () {
            return yt(this), et._ts_node_parent_wasm(this.tree[0]), bt(this.tree);
          }

          descendantForIndex (e, t = e) {
            if ("number" != typeof e || "number" != typeof t) {
              throw new Error("Arguments must be numbers");
            }
            yt(this);
            let r = mt + nt;
            return x(r, e, "i32"), x(r + rt, t, "i32"), et._ts_node_descendant_for_index_wasm(this.tree[0]), bt(this.tree);
          }

          namedDescendantForIndex (e, t = e) {
            if ("number" != typeof e || "number" != typeof t) {
              throw new Error("Arguments must be numbers");
            }
            yt(this);
            let r = mt + nt;
            return x(r, e, "i32"), x(r + rt, t, "i32"), et._ts_node_named_descendant_for_index_wasm(this.tree[0]), bt(this.tree);
          }

          descendantForPosition (e, t = e) {
            if (!Mt(e) || !Mt(t)) {
              throw new Error("Arguments must be {row, column} objects");
            }
            yt(this);
            let r = mt + nt;
            return St(r, e), St(r + ot, t), et._ts_node_descendant_for_position_wasm(this.tree[0]), bt(this.tree);
          }

          namedDescendantForPosition (e, t = e) {
            if (!Mt(e) || !Mt(t)) {
              throw new Error("Arguments must be {row, column} objects");
            }
            yt(this);
            let r = mt + nt;
            return St(r, e), St(r + ot, t), et._ts_node_named_descendant_for_position_wasm(this.tree[0]), bt(this.tree);
          }

          walk () {
            return yt(this), et._ts_tree_cursor_new_wasm(this.tree[0]), new TreeCursor(tt, this.tree);
          }

          toString () {
            yt(this);
            const e = et._ts_node_to_string_wasm(this.tree[0]), t = function(e) {
              for (var t = ""; ;) {
                var r = T[e++ >> 0];
                if (!r) {
                  return t;
                }
                t += String.fromCharCode(r);
              }
            }(e);
            return et._free(e), t;
          }
        }

        class TreeCursor {
          constructor (e, t) {
            wt(e), this.tree = t, Et(this);
          }

          delete () {
            vt(this), et._ts_tree_cursor_delete_wasm(this.tree[0]), this[0] = this[1] = this[2] = 0;
          }

          reset (e) {
            yt(e), vt(this, mt + nt), et._ts_tree_cursor_reset_wasm(this.tree[0]), Et(this);
          }

          get nodeType () {
            return this.tree.language.types[this.nodeTypeId] || "ERROR";
          }

          get nodeTypeId () {
            return vt(this), et._ts_tree_cursor_current_node_type_id_wasm(this.tree[0]);
          }

          get nodeId () {
            return vt(this), et._ts_tree_cursor_current_node_id_wasm(this.tree[0]);
          }

          get nodeIsNamed () {
            return vt(this), 1 === et._ts_tree_cursor_current_node_is_named_wasm(this.tree[0]);
          }

          get nodeIsMissing () {
            return vt(this), 1 === et._ts_tree_cursor_current_node_is_missing_wasm(this.tree[0]);
          }

          get nodeText () {
            vt(this);
            const e = et._ts_tree_cursor_start_index_wasm(this.tree[0]),
              t = et._ts_tree_cursor_end_index_wasm(this.tree[0]);
            return ht(this.tree, e, t);
          }

          get startPosition () {
            return vt(this), et._ts_tree_cursor_start_position_wasm(this.tree[0]), It(mt);
          }

          get endPosition () {
            return vt(this), et._ts_tree_cursor_end_position_wasm(this.tree[0]), It(mt);
          }

          get startIndex () {
            return vt(this), et._ts_tree_cursor_start_index_wasm(this.tree[0]);
          }

          get endIndex () {
            return vt(this), et._ts_tree_cursor_end_index_wasm(this.tree[0]);
          }

          currentNode () {
            return vt(this), et._ts_tree_cursor_current_node_wasm(this.tree[0]), bt(this.tree);
          }

          currentFieldId () {
            return vt(this), et._ts_tree_cursor_current_field_id_wasm(this.tree[0]);
          }

          currentFieldName () {
            return this.tree.language.fields[this.currentFieldId()];
          }

          gotoFirstChild () {
            vt(this);
            const e = et._ts_tree_cursor_goto_first_child_wasm(this.tree[0]);
            return Et(this), 1 === e;
          }

          gotoNextSibling () {
            vt(this);
            const e = et._ts_tree_cursor_goto_next_sibling_wasm(this.tree[0]);
            return Et(this), 1 === e;
          }

          gotoParent () {
            vt(this);
            const e = et._ts_tree_cursor_goto_parent_wasm(this.tree[0]);
            return Et(this), 1 === e;
          }
        }

        class Language {
          constructor (e, t) {
            wt(e), this[0] = t, this.types = new Array(et._ts_language_symbol_count(this[0]));
            for (let e = 0, t = this.types.length; e < t; e++) et._ts_language_symbol_type(this[0], e) < 2 && (this.types[e] = Z(et._ts_language_symbol_name(this[0], e)));
            this.fields = new Array(et._ts_language_field_count(this[0]) + 1);
            for (let e = 0, t = this.fields.length; e < t; e++) {
              const t = et._ts_language_field_name_for_id(this[0], e);
              this.fields[e] = 0 !== t ? Z(t) : null;
            }
          }

          get version () {
            return et._ts_language_version(this[0]);
          }

          get fieldCount () {
            return this.fields.length - 1;
          }

          fieldIdForName (e) {
            const t = this.fields.indexOf(e);
            return -1 !== t ? t : null;
          }

          fieldNameForId (e) {
            return this.fields[e] || null;
          }

          idForNodeType (e, t) {
            const r = U(e), n = et._malloc(r + 1);
            z(e, n, r + 1);
            const o = et._ts_language_symbol_for_name(this[0], n, r, t);
            return et._free(n), o || null;
          }

          get nodeTypeCount () {
            return et._ts_language_symbol_count(this[0]);
          }

          nodeTypeForId (e) {
            const t = et._ts_language_symbol_name(this[0], e);
            return t ? Z(t) : null;
          }

          nodeTypeIsNamed (e) {
            return !!et._ts_language_type_is_named_wasm(this[0], e);
          }

          nodeTypeIsVisible (e) {
            return !!et._ts_language_type_is_visible_wasm(this[0], e);
          }

          query (e) {
            const t = U(e), r = et._malloc(t + 1);
            z(e, r, t + 1);
            const n = et._ts_query_new(this[0], r, t, mt, mt + rt);
            if (!n) {
              const t = N(mt + rt, "i32"), n = Z(r, N(mt, "i32")).length, o = e.substr(n, 100).split("\n")[0];
              let s, _ = o.match(at)[0];
              switch (t) {
                case 2:
                  s = new RangeError(`Bad node name '${_}'`);
                  break;
                case 3:
                  s = new RangeError(`Bad field name '${_}'`);
                  break;
                case 4:
                  s = new RangeError(`Bad capture name @${_}`);
                  break;
                case 5:
                  s = new TypeError(`Bad pattern structure at offset ${n}: '${o}'...`), _ = "";
                  break;
                default:
                  s = new SyntaxError(`Bad syntax at offset ${n}: '${o}'...`), _ = "";
              }
              throw s.index = n, s.length = _.length, et._free(r), s;
            }
            const o = et._ts_query_string_count(n), s = et._ts_query_capture_count(n),
              _ = et._ts_query_pattern_count(n), a = new Array(s), i = new Array(o);
            for (let e = 0; e < s; e++) {
              const t = et._ts_query_capture_name_for_id(n, e, mt), r = N(mt, "i32");
              a[e] = Z(t, r);
            }
            for (let e = 0; e < o; e++) {
              const t = et._ts_query_string_value_for_id(n, e, mt), r = N(mt, "i32");
              i[e] = Z(t, r);
            }
            const u = new Array(_), l = new Array(_), d = new Array(_), c = new Array(_), m = new Array(_);
            for (let e = 0; e < _; e++) {
              const t = et._ts_query_predicates_for_pattern(n, e, mt), r = N(mt, "i32");
              c[e] = [], m[e] = [];
              const o = [];
              let s = t;
              for (let t = 0; t < r; t++) {
                const t = N(s, "i32"), r = N(s += rt, "i32");
                if (s += rt, t === it) {
                  o.push({
                    type: "capture",
                    name: a[r]
                  });
                }
                else if (t === ut) {
                  o.push({ type: "string", value: i[r] });
                }
                else if (o.length > 0) {
                  if ("string" !== o[0].type) {
                    throw new Error("Predicates must begin with a literal value");
                  }
                  const t = o[0].value;
                  let r = !0;
                  switch (t) {
                    case"not-eq?":
                      r = !1;
                    case"eq?":
                      if (3 !== o.length) {
                        throw new Error(`Wrong number of arguments to \`#eq?\` predicate. Expected 2, got ${o.length - 1}`);
                      }
                      if ("capture" !== o[1].type) {
                        throw new Error(`First argument of \`#eq?\` predicate must be a capture. Got "${o[1].value}"`);
                      }
                      if ("capture" === o[2].type) {
                        const t = o[1].name, n = o[2].name;
                        m[e].push(function(e) {
                          let o, s;
                          for (const r of e) r.name === t && (o = r.node), r.name === n && (s = r.node);
                          return void 0 === o || void 0 === s || o.text === s.text === r;
                        });
                      }
                      else {
                        const t = o[1].name, n = o[2].value;
                        m[e].push(function(e) {
                          for (const o of e) if (o.name === t) {
                            return o.node.text === n === r;
                          }
                          return !0;
                        });
                      }
                      break;
                    case"not-match?":
                      r = !1;
                    case"match?":
                      if (3 !== o.length) {
                        throw new Error(`Wrong number of arguments to \`#match?\` predicate. Expected 2, got ${o.length - 1}.`);
                      }
                      if ("capture" !== o[1].type) {
                        throw new Error(`First argument of \`#match?\` predicate must be a capture. Got "${o[1].value}".`);
                      }
                      if ("string" !== o[2].type) {
                        throw new Error(`Second argument of \`#match?\` predicate must be a string. Got @${o[2].value}.`);
                      }
                      const n = o[1].name, s = new RegExp(o[2].value);
                      m[e].push(function(e) {
                        for (const t of e) if (t.name === n) {
                          return s.test(t.node.text) === r;
                        }
                        return !0;
                      });
                      break;
                    case"set!":
                      if (o.length < 2 || o.length > 3) {
                        throw new Error(`Wrong number of arguments to \`#set!\` predicate. Expected 1 or 2. Got ${o.length - 1}.`);
                      }
                      if (o.some(e => "string" !== e.type)) {
                        throw new Error("Arguments to `#set!` predicate must be a strings.\".");
                      }
                      u[e] || (u[e] = {}), u[e][o[1].value] = o[2] ? o[2].value : null;
                      break;
                    case"is?":
                    case"is-not?":
                      if (o.length < 2 || o.length > 3) {
                        throw new Error(`Wrong number of arguments to \`#${t}\` predicate. Expected 1 or 2. Got ${o.length - 1}.`);
                      }
                      if (o.some(e => "string" !== e.type)) {
                        throw new Error(`Arguments to \`#${t}\` predicate must be a strings.".`);
                      }
                      const _ = "is?" === t ? l : d;
                      _[e] || (_[e] = {}), _[e][o[1].value] = o[2] ? o[2].value : null;
                      break;
                    default:
                      c[e].push({ operator: t, operands: o.slice(1) });
                  }
                  o.length = 0;
                }
              }
              Object.freeze(u[e]), Object.freeze(l[e]), Object.freeze(d[e]);
            }
            return et._free(r), new Query(tt, n, a, m, c, Object.freeze(u), Object.freeze(l), Object.freeze(d));
          }

          static load (e) {
            let t;
            if (e instanceof Uint8Array) {
              t = Promise.resolve(e);
            }
            else {
              const r = e;
              if ("undefined" != typeof process && process.versions && process.versions.node) {
                const e = require("fs");
                t = Promise.resolve(e.readFileSync(r));
              }
              else {
                t = fetch(r).then(e => e.arrayBuffer().then(t => {
                  if (e.ok) {
                    return new Uint8Array(t);
                  }
                  {
                    const r = new TextDecoder("utf-8").decode(t);
                    throw new Error(`Language.load failed with status ${e.status}.\n\n${r}`);
                  }
                }));
              }
            }
            const r = "function" == typeof loadSideModule ? loadSideModule : xe;
            return t.then(e => r(e, { loadAsync: !0 })).then(e => {
              const t = Object.keys(e), r = t.find(e => lt.test(e) && !e.includes("external_scanner_"));
              r || console.log(`Couldn't find language function in WASM file. Symbols:\n${JSON.stringify(t, null, 2)}`);
              const n = e[r]();
              return new Language(tt, n);
            });
          }
        }

        class Query {
          constructor (e, t, r, n, o, s, _, a) {
            wt(e), this[0] = t, this.captureNames = r, this.textPredicates = n, this.predicates = o, this.setProperties = s, this.assertedProperties = _, this.refutedProperties = a, this.exceededMatchLimit = !1;
          }

          delete () {
            et._ts_query_delete(this[0]), this[0] = 0;
          }

          matches (e, t, r, n) {
            t || (t = _t), r || (r = _t), n || (n = {});
            let o = n.matchLimit;
            if (void 0 === o) {
              o = 0;
            }
            else if ("number" != typeof o) {
              throw new Error("Arguments must be numbers");
            }
            yt(e), et._ts_query_matches_wasm(this[0], e.tree[0], t.row, t.column, r.row, r.column, o);
            const s = N(mt, "i32"), _ = N(mt + rt, "i32"), a = N(mt + 2 * rt, "i32"), i = new Array(s);
            this.exceededMatchLimit = !!a;
            let u = 0, l = _;
            for (let t = 0; t < s; t++) {
              const r = N(l, "i32"), n = N(l += rt, "i32");
              l += rt;
              const o = new Array(n);
              if (l = gt(this, e.tree, l, o), this.textPredicates[r].every(e => e(o))) {
                i[u++] = { pattern: r, captures: o };
                const e = this.setProperties[r];
                e && (i[t].setProperties = e);
                const n = this.assertedProperties[r];
                n && (i[t].assertedProperties = n);
                const s = this.refutedProperties[r];
                s && (i[t].refutedProperties = s);
              }
            }
            return i.length = u, et._free(_), i;
          }

          captures (e, t, r, n) {
            t || (t = _t), r || (r = _t), n || (n = {});
            let o = n.matchLimit;
            if (void 0 === o) {
              o = 0;
            }
            else if ("number" != typeof o) {
              throw new Error("Arguments must be numbers");
            }
            yt(e), et._ts_query_captures_wasm(this[0], e.tree[0], t.row, t.column, r.row, r.column, o);
            const s = N(mt, "i32"), _ = N(mt + rt, "i32"), a = N(mt + 2 * rt, "i32"), i = [];
            this.exceededMatchLimit = !!a;
            const u = [];
            let l = _;
            for (let t = 0; t < s; t++) {
              const t = N(l, "i32"), r = N(l += rt, "i32"), n = N(l += rt, "i32");
              if (l += rt, u.length = r, l = gt(this, e.tree, l, u), this.textPredicates[t].every(e => e(u))) {
                const e = u[n], r = this.setProperties[t];
                r && (e.setProperties = r);
                const o = this.assertedProperties[t];
                o && (e.assertedProperties = o);
                const s = this.refutedProperties[t];
                s && (e.refutedProperties = s), i.push(e);
              }
            }
            return et._free(_), i
          }

          predicatesForPattern (e) {
            return this.predicates[e]
          }

          didExceedMatchLimit () {
            return this.exceededMatchLimit
          }
        }

        function ht (e, t, r) {
          const n = r - t;
          let o = e.textCallback(t, null, r);
          for (t += o.length; t < r;) {
            const n = e.textCallback(t, null, r);
            if (!(n && n.length > 0)) {
              break;
            }
            t += n.length, o += n;
          }
          return t > r && (o = o.slice(0, n)), o
        }

        function gt (e, t, r, n) {
          for (let o = 0, s = n.length; o < s; o++) {
            const s = N(r, "i32"), _ = bt(t, r += rt);
            r += nt, n[o] = { name: e.captureNames[s], node: _ };
          }
          return r
        }

        function wt (e) {
          if (e !== tt) {
            throw new Error("Illegal constructor")
          }
        }

        function Mt (e) {
          return e && "number" == typeof e.row && "number" == typeof e.column
        }

        function yt (e) {
          let t = mt;
          x(t, e.id, "i32"), x(t += rt, e.startIndex, "i32"), x(t += rt, e.startPosition.row, "i32"), x(t += rt, e.startPosition.column, "i32"), x(t += rt, e[0], "i32");
        }

        function bt (e, t = mt) {
          const r = N(t, "i32");
          if (0 === r) {
            return null;
          }
          const n = N(t += rt, "i32"), o = N(t += rt, "i32"), s = N(t += rt, "i32"), _ = N(t += rt, "i32"),
            a = new Node(tt, e);
          return a.id = r, a.startIndex = n, a.startPosition = { row: o, column: s }, a[0] = _, a
        }

        function vt (e, t = mt) {
          x(t + 0 * rt, e[0], "i32"), x(t + 1 * rt, e[1], "i32"), x(t + 2 * rt, e[2], "i32");
        }

        function Et (e) {
          e[0] = N(mt + 0 * rt, "i32"), e[1] = N(mt + 1 * rt, "i32"), e[2] = N(mt + 2 * rt, "i32");
        }

        function St (e, t) {
          x(e, t.row, "i32"), x(e + rt, t.column, "i32");
        }

        function It (e) {
          return { row: N(e, "i32"), column: N(e + rt, "i32") }
        }

        function At (e, t) {
          St(e, t.startPosition), St(e += ot, t.endPosition), x(e += ot, t.startIndex, "i32"), x(e += rt, t.endIndex, "i32"), e += rt;
        }

        function xt (e) {
          const t = {};
          return t.startPosition = It(e), e += ot, t.endPosition = It(e), e += ot, t.startIndex = N(e, "i32"), e += rt, t.endIndex = N(e, "i32"), t
        }

        for (const e of Object.getOwnPropertyNames(ParserImpl.prototype)) Object.defineProperty(Parser.prototype, e, {
          value: ParserImpl.prototype[e],
          enumerable: !1,
          writable: !1
        });
        Parser.Language = Language, Module.onRuntimeInitialized = (() => {
          ParserImpl.init(), e();
        });
      }))
    }
  }

  return Parser
}();

await TreeSitter.init({
  locateFile(scriptName, scriptDirectory) {
    console.log(scriptName, scriptDirectory);
    return "/assets/tree-sitter.wasm";
  }  
});

const Parser = TreeSitter;
const Language = TreeSitter.Language;

function dispatchCustomEvent(node, suffix, detail, options = {}) {
    const eventName = `${node.nodeName.toLocaleLowerCase()}:${suffix}`;
    node.dispatchEvent(new CustomEvent(eventName, {
        detail,
        bubbles: true,
        composed: true,
        ...options,
    }));
}

const QUERY = `
;; Literals
(number) @constant.numeric.integer
(boolean) @constant.builtin.boolean
(string) @string
(bytes) @string
(date) @constant.numeric.integer

;; Comments
(block_comment) @comment.block
(line_comment) @comment.line

;; Variables
(variable) @variable
(param) @variable.parameter

(predicate
  (nname) @function
)

(fact
  (nname) @function
)

;; Keywords
[
  "trusting"
  "check if"
  "check all"
  "allow if"
  "deny if"
] @keyword

[
  "authority"
  "previous"
] @constant.builtin

[
  "<-"
] @keyword.operator

;; Punctuation
[ "," ] @punctuation.delimiter

[ "("
  ")"
  "["
  "]"
] @punctuation.bracket

[
  "/"
  "*"
  "+"
  "-"
  "&"
  "|"
  "^"
  ">" "<" "<=" ">=" "==" "!="
  "&&"
  "||"
] @operator`;
let BcDatalogEditor = class BcDatalogEditor extends s {
    constructor() {
        var _a;
        super();
        this.code = "";
        this.marks = [];
        this.readonly = false;
        this._tree = null;
        this._captures = [];
        this._parser = null;
        this._query = null;
        const codeChild = this.querySelector("code");
        if (codeChild !== null) {
            this.code = trimLines$1((_a = codeChild.textContent) !== null && _a !== void 0 ? _a : "");
        }
    }
    connectedCallback() {
        super.connectedCallback();
        console.log("connected callback");
        console.log("Parser initialized");
        console.log(Language);
        Language.load("/assets/tree-sitter-biscuit.wasm").then((BiscuitDatalog) => {
            console.log("Language initialized");
            const p = new Parser();
            p.setLanguage(BiscuitDatalog);
            this._query = BiscuitDatalog.query(QUERY);
            this._parser = p;
            this.handleInput(this.code);
        });
    }
    firstUpdated(values) {
        super.firstUpdated(values);
        // trigger syntax highlighting for code provided as props
        if (this.shadowRoot) {
            this.handleInput(this.shadowRoot.querySelector("#editing").value);
        }
    }
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    attributeChangedCallback(name, oldValue, newValue) {
        super.attributeChangedCallback(name, oldValue, newValue);
        if (name === "code" && oldValue != newValue) {
            this.handleInput(newValue);
        }
    }
    handleInput(value) {
        if (this._parser && this._query) {
            const code = value !== null && value !== void 0 ? value : "";
            const tree = this._parser.parse(code);
            const captures = this._query.captures(tree.rootNode);
            this._tree = tree;
            this._captures = captures;
            this.code = code;
        }
        this.syncScroll();
    }
    syncScroll() {
        if (this.shadowRoot) {
            const textarea = this.shadowRoot.querySelector("#editing");
            const highlighting = this.shadowRoot.querySelector("#highlighting");
            if (textarea && highlighting) {
                highlighting.scrollTop = textarea.scrollTop;
                highlighting.scrollLeft = textarea.scrollLeft;
            }
        }
    }
    render() {
        const rendered = this.renderText2(this.code, this._captures, this.marks);
        const rows = Math.max(this.code.split("\n").length, 1);
        return x `<div id="wrapper">
      <textarea
        id="editing"
        rows=${rows}
        readonly=${l(this.readonly ? "true" : undefined)}
        @input=${(e) => {
            var _a;
            const code = (_a = e.target) === null || _a === void 0 ? void 0 : _a.value;
            this.handleInput(code);
            dispatchCustomEvent(this, "update", { code });
        }}
        @keydown=${(e) => e.stopPropagation()}
        spellcheck="false"
        @scroll=${this.syncScroll}
      >
${this.code}</textarea
      >
      <pre
        id="highlighting"
        aria-hidden="true"
      ><code id="highlighting-content">${o(rendered)}</code></pre>
    </div>`;
    }
    renderText2(text, captures, marks) {
        // captures come from tree-sitter and are assumed to be properly nested
        // (if nested at all, they are mostly contiguous).
        // marks however are user provided and completely separate from TS. so even
        // if the user is not doing bad things, they still don't have access to the
        // TS tree and can't be asked to provide marks that don't nest correctly
        // with TS captures.
        var _a;
        // captures and marks are grouped by starting index. We know that an
        // opening tag will always be there at this position.
        const ranges = new Map();
        marks.concat(captures).forEach((c) => {
            var _a;
            if (c.node.startIndex >= c.node.endIndex)
                return;
            ranges.set(c.node.startIndex, ((_a = ranges.get(c.node.startIndex)) !== null && _a !== void 0 ? _a : []).concat(c));
        });
        let output = "";
        // active ranges, indexed by their end index
        const activeRanges = new Map();
        [...text].forEach((c, i) => {
            var _a, _b, _c, _d;
            // every range is encoded as a span element, so there is no need
            // to care about the order, we only need to close the correct amount
            // of spans
            ((_a = activeRanges.get(i)) !== null && _a !== void 0 ? _a : []).forEach(() => {
                output += "</span>";
            });
            activeRanges.delete(i);
            const openingNow = (_b = ranges.get(i)) !== null && _b !== void 0 ? _b : [];
            // position of the next range end. ranges created now can't go further
            const lastValidIndex = (_c = [...activeRanges.keys()].sort()[0]) !== null && _c !== void 0 ? _c : text.length;
            // list of the ranges created by splitting opening ranges at the next
            // enclosing range end
            const createdRanges = [];
            // for each of the new ranges, make sure they are properly included in
            // the active ranges (ie they end before the next closing tag).
            // If they are not, we will need to close them at the closest boundary,
            // and open them again. if the newly created range still intersects with
            // an already defined one, it will be handled at its starting index, so
            // we don't need to do further work here.
            const openingAdapted = openingNow.map((r) => {
                var _a;
                // since we're operating on shallow copies, mutating `r` directly
                // would mutate it everywhere else (including across) renders
                let adapted = r;
                if (r.node.endIndex > lastValidIndex) {
                    // clamp the opening range at the end of the enclosing one,
                    // and put the leftover in a new one
                    adapted = {
                        ...r,
                        node: {
                            startIndex: adapted.node.startIndex,
                            endIndex: lastValidIndex,
                        },
                    };
                    createdRanges.push({
                        name: r.name,
                        node: {
                            startIndex: lastValidIndex,
                            endIndex: r.node.endIndex,
                        },
                    });
                }
                // opening ranges are now part of the active set
                activeRanges.set(adapted.node.endIndex, ((_a = activeRanges.get(adapted.node.endIndex)) !== null && _a !== void 0 ? _a : []).concat(adapted));
                return adapted;
            });
            // insert the newly created ranges in the range map so future iterations
            // can pick them up
            ranges.set(lastValidIndex, ((_d = ranges.get(lastValidIndex)) !== null && _d !== void 0 ? _d : []).concat(createdRanges));
            // now that we have the final list of ranges starting at the current
            // position, we can sort them so that the widest ranges are opened first,
            // to let them contain smaller ranges
            openingAdapted
                .sort((a, b) => {
                return b.node.endIndex - a.node.endIndex;
            })
                .forEach((r) => {
                output += `<span class="${r.name.split(".").join(" ")}">`;
            });
            if (c === `\n`) {
                output += "<br>";
            }
            else if (c === `<`) {
                output += "&lt;";
            }
            else if (c === `>`) {
                output += "&gt;";
            }
            else if (c === ` `) {
                output += " "; // pre collapes / ignores leading spaces. an non-breaking
                // space will always take the correct amount of space
            }
            else {
                output += c;
            }
        });
        // handle ranges closing after the last char
        ((_a = activeRanges.get(text.length)) !== null && _a !== void 0 ? _a : []).forEach(() => {
            output += "</span>";
        });
        // pre hides empty final lines (or containing just a space or a tab):
        // this nbsp forces pre to display an empty line. this makes sure a new
        // line is displayed when typing enter at the end of the last line.
        // The final <br> tag might be inside one or several spans tags, so we
        // need to keep looking right until we find the last `</span>` tag, and
        // then inspect what's just before
        let offset = 0;
        while (output.slice(offset - 7, 7) == "</span>") {
            offset -= 7;
        }
        if (output.slice(offset - 4, 4) === "<br>") {
            output += " ";
        }
        return output;
    }
};
BcDatalogEditor.styles = i$3 `
    #wrapper {
      position: relative;
    }

    #editing,
    #highlighting {
      margin: 0;
      padding: 10px;
      border: 0;
      width: calc(100% - 22px);
      overflow: auto;
      white-space: nowrap;
    }

    #editing,
    #highlighting,
    #highlighting * {
      font-size: var(--editor-font, 13px);
      font-family: monospace;
      line-height: calc(var(--editor-font, 13px) * 1.3);
    }

    #highlighting {
      border: 1px solid black;
    }

    #editing {
      position: relative;
      resize: vertical;
      z-index: 1;
      min-height: 5em;

      white-space: pre;
      color: transparent;
      background: transparent;
      caret-color: var(--foreground); /* Or choose your favorite color */
    }

    #highlighting {
      position: absolute;
      top: 0;
      bottom: 3px;
      left: 0;
      z-index: 0;
      color: var(--foreground);
      background-color: var(--background);
    }

    :host {
      --background: #fafafa;
      --foreground: #5c6166;
      --black: #e7eaed;
      --white: #fcfcfc;
      --blue: #399ee6;
      --light_blue: #55b4d4;
      --cyan: #478acc;
      --dark_gray: #d8d8d7;
      --gray: #828c9a;
      --green: #86b300;
      --green-bg: rgba(134, 170, 0, 0.3);
      --magenta: #a37acc;
      --orange: #fa8d3e;
      --red: #f07171;
      --red-bg: rgba(240, 113, 113, 0.3);
      --yellow: #ffaa33;
    }

    .constant {
      color: var(--magenta);
    }

    .string {
      color: var(--green);
    }

    .comment {
      color: var(--gray);
      font-style: italic;
    }

    .variable {
      color: var(--foreground);
    }

    .punctuation {
      color: var(--foreground);
    }

    .keyword {
      color: var(--orange);
    }

    .operator {
      color: var(--orange);
    }

    .function {
      color: var(--yellow);
    }

    .warning {
      color: var(--yellow);
    }

    .mark.failure {
      background-color: var(--red-bg);
    }

    .mark.error {
      text-decoration: underline wavy var(--red);
    }

    .mark.success {
      background-color: var(--green-bg);
    }

    .hint {
      color: var(--blue);
      font-weight: bold;
    }

    @media (prefers-color-scheme: dark) {
      :host {
        --background: #0f1419;
        --foreground: #bfbdb6;
        --black: #131721;
        --blue: #59c2ff;
        --dark_gray: #2d3640;
        --cyan: #73b8ff;
        --gray: #5c6773;
        --green: #aad94c;
        --green-bg: rgba(170, 217, 76, 0.5);
        --magenta: #d2a6ff;
        --orange: #ff8f40;
        --red: #f07178;
        --red-bg: rgba(240, 113, 120, 0.5);
        --yellow: #e6b450;
      }
      .comment {
        color: var(--gray);
        text-transform: italic;
      }
      .error {
        color: var(--red);
        font-weight: bold;
      }
      .hint {
        color: var(--blue);
        font-weight: bold;
      }
      .constant {
        color: var(--magenta);
      }
      .function {
        color: var(--yellow);
      }
      .keyword {
        color: var(--orange);
      }
      .operator {
        color: var(--orange);
      }
      .punctuation {
        color: var(--foreground);
      }
      .string {
        color: var(--green);
      }
      .variable {
        color: var(--foreground);
      }
      .warning {
        color: var(--yellow);
      }
    }
  `;
__decorate([
    n$1()
], BcDatalogEditor.prototype, "code", void 0);
__decorate([
    n$1()
], BcDatalogEditor.prototype, "marks", void 0);
__decorate([
    n$1()
], BcDatalogEditor.prototype, "readonly", void 0);
__decorate([
    t$1()
], BcDatalogEditor.prototype, "_tree", void 0);
__decorate([
    t$1()
], BcDatalogEditor.prototype, "_captures", void 0);
__decorate([
    t$1()
], BcDatalogEditor.prototype, "_parser", void 0);
__decorate([
    t$1()
], BcDatalogEditor.prototype, "_query", void 0);
BcDatalogEditor = __decorate([
    e$3("bc-datalog-editor")
], BcDatalogEditor);
function trimLines$1(str) {
    return str
        .trim()
        .split("\n")
        .map((line) => line.trim())
        .join("\n");
}

/**
 * TODO DOCS
 */
class BcTokenEditor extends s {
    static get properties() {
        return {
            biscuit: { type: String },
            _blocks: { type: Array },
            parseErrors: { type: Array },
            markers: { type: Array },
        };
    }
    constructor() {
        super();
        this._blocks = [];
        for (const child of Array.from(this.children)) {
            this._blocks.push({ code: child.innerHTML });
        }
        this.parseErrors = [];
        this.markers = [];
    }
    _onAddBlock() {
        this._blocks = [...this._blocks, { code: "" }];
    }
    _onRemoveBlock(block) {
        this._blocks = this._blocks.filter((b) => b !== block);
    }
    _onUpdatedCode(block, code) {
        block.code = code;
        dispatchCustomEvent(this, "update", { blocks: this._blocks });
    }
    update(changedProperties) {
        super.update(changedProperties);
    }
    render() {
        return x `
      <div>
        <button @click=${this._onAddBlock}>add block</button>
      </div>
      ${this._blocks.map((block, index) => x `
          <button @click=${() => this._onRemoveBlock(block)}>
            remove this block
          </button>
          <bc-datalog-editor
            code=${block.code}
            .marks=${this.markers[index].concat(this.parseErrors[index])}
            @bc-datalog-editor:update="${(e) => {
            this._onUpdatedCode(block, e.detail.code);
        }}"
            }
          >
          </bc-datalog-editor>
        `)}
    `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-token-editor", BcTokenEditor);

/**
 * TODO DOCS
 */
class BcTokenContent extends s {
    static get properties() {
        return {
            content: { type: Object },
        };
    }
    constructor() {
        super();
    }
    render() {
        const content = this.content == null ? "no content yet" : this.content;
        return x ` <div><pre>${content}</pre></div> `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-token-content", BcTokenContent);

/**
 * TODO DOCS
 */
class BcAuthorizerEditor extends s {
    static get properties() {
        return {
            code: { type: String },
            parseErrors: { type: Array },
            markers: { type: Array },
        };
    }
    constructor() {
        super();
        if (this.children[0] != undefined) {
            this.code = this.children[0].innerHTML;
        }
        else {
            this.code = "";
        }
        this.parseErrors = [];
        this.markers = [];
    }
    _onUpdatedCode(code) {
        this.code = code;
        dispatchCustomEvent(this, "update", { code: code });
    }
    update(changedProperties) {
        super.update(changedProperties);
    }
    render() {
        return x `
      <bc-datalog-editor
        code=${this.code}
        .marks=${this.markers.concat(this.parseErrors)}
        @bc-datalog-editor:update="${(e) => {
            this._onUpdatedCode(e.detail.code);
        }}"
        }
      >
      </bc-datalog-editor>
    `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-authorizer-editor", BcAuthorizerEditor);

/**
 * TODO DOCS
 */
class BcAuthorizerContent extends s {
    static get properties() {
        return {
            content: { type: Array },
        };
    }
    constructor() {
        super();
        this.content = [];
    }
    render() {
        const deduped = [...new Set(this.content)];
        const sortedFacts = [...deduped].sort((f1, f2) => {
            if (f1.name == f2.name) {
                return f1.terms > f2.terms ? 1 : -1;
            }
            else {
                return f1.name > f2.name ? 1 : -1;
            }
        });
        var facts_map = {};
        var facts = "";
        var current_name;
        for (let fact of sortedFacts) {
            if (facts_map[fact.name] == undefined) {
                facts_map[fact.name] = [];
            }
            let alreadyThere = false;
            for (let terms of facts_map[fact.name]) {
                console.log(terms.join(), fact.terms.join());
                if (terms.join() === fact.terms.join()) {
                    alreadyThere = true;
                    break;
                }
            }
            if (alreadyThere)
                continue;
            facts_map[fact.name].push(fact.terms);
            if (current_name == undefined) {
                current_name = fact.name;
            }
            if (fact.name != current_name) {
                facts += "\n";
                current_name = fact.name;
            }
            facts += fact.name + "(" + fact.terms + ");\n";
        }
        return x ` <div>
      <bc-datalog-editor code=${facts} readonly="true"></bc-datalog-editor>
    </div>`;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-authorizer-content", BcAuthorizerContent);

function performance_now() {
  return performance.now();
}

var __wbg_star0 = /*#__PURE__*/Object.freeze({
    __proto__: null,
    performance_now: performance_now
});

let wasm;

const heap = new Array(128).fill(undefined);

heap.push(undefined, null, true, false);

function getObject(idx) { return heap[idx]; }

let heap_next = heap.length;

function dropObject(idx) {
    if (idx < 132) return;
    heap[idx] = heap_next;
    heap_next = idx;
}

function takeObject(idx) {
    const ret = getObject(idx);
    dropObject(idx);
    return ret;
}

function addHeapObject(obj) {
    if (heap_next === heap.length) heap.push(heap.length + 1);
    const idx = heap_next;
    heap_next = heap[idx];

    heap[idx] = obj;
    return idx;
}

let WASM_VECTOR_LEN = 0;

let cachedUint8Memory0 = null;

function getUint8Memory0() {
    if (cachedUint8Memory0 === null || cachedUint8Memory0.byteLength === 0) {
        cachedUint8Memory0 = new Uint8Array(wasm.memory.buffer);
    }
    return cachedUint8Memory0;
}

const cachedTextEncoder = (typeof TextEncoder !== 'undefined' ? new TextEncoder('utf-8') : { encode: () => { throw Error('TextEncoder not available') } } );

const encodeString = (typeof cachedTextEncoder.encodeInto === 'function'
    ? function (arg, view) {
    return cachedTextEncoder.encodeInto(arg, view);
}
    : function (arg, view) {
    const buf = cachedTextEncoder.encode(arg);
    view.set(buf);
    return {
        read: arg.length,
        written: buf.length
    };
});

function passStringToWasm0(arg, malloc, realloc) {

    if (realloc === undefined) {
        const buf = cachedTextEncoder.encode(arg);
        const ptr = malloc(buf.length, 1) >>> 0;
        getUint8Memory0().subarray(ptr, ptr + buf.length).set(buf);
        WASM_VECTOR_LEN = buf.length;
        return ptr;
    }

    let len = arg.length;
    let ptr = malloc(len, 1) >>> 0;

    const mem = getUint8Memory0();

    let offset = 0;

    for (; offset < len; offset++) {
        const code = arg.charCodeAt(offset);
        if (code > 0x7F) break;
        mem[ptr + offset] = code;
    }

    if (offset !== len) {
        if (offset !== 0) {
            arg = arg.slice(offset);
        }
        ptr = realloc(ptr, len, len = offset + arg.length * 3, 1) >>> 0;
        const view = getUint8Memory0().subarray(ptr + offset, ptr + len);
        const ret = encodeString(arg, view);

        offset += ret.written;
        ptr = realloc(ptr, len, offset, 1) >>> 0;
    }

    WASM_VECTOR_LEN = offset;
    return ptr;
}

function isLikeNone(x) {
    return x === undefined || x === null;
}

let cachedInt32Memory0 = null;

function getInt32Memory0() {
    if (cachedInt32Memory0 === null || cachedInt32Memory0.byteLength === 0) {
        cachedInt32Memory0 = new Int32Array(wasm.memory.buffer);
    }
    return cachedInt32Memory0;
}

const cachedTextDecoder = (typeof TextDecoder !== 'undefined' ? new TextDecoder('utf-8', { ignoreBOM: true, fatal: true }) : { decode: () => { throw Error('TextDecoder not available') } } );

if (typeof TextDecoder !== 'undefined') { cachedTextDecoder.decode(); }
function getStringFromWasm0(ptr, len) {
    ptr = ptr >>> 0;
    return cachedTextDecoder.decode(getUint8Memory0().subarray(ptr, ptr + len));
}

let cachedFloat64Memory0 = null;

function getFloat64Memory0() {
    if (cachedFloat64Memory0 === null || cachedFloat64Memory0.byteLength === 0) {
        cachedFloat64Memory0 = new Float64Array(wasm.memory.buffer);
    }
    return cachedFloat64Memory0;
}

function debugString(val) {
    // primitive types
    const type = typeof val;
    if (type == 'number' || type == 'boolean' || val == null) {
        return  `${val}`;
    }
    if (type == 'string') {
        return `"${val}"`;
    }
    if (type == 'symbol') {
        const description = val.description;
        if (description == null) {
            return 'Symbol';
        } else {
            return `Symbol(${description})`;
        }
    }
    if (type == 'function') {
        const name = val.name;
        if (typeof name == 'string' && name.length > 0) {
            return `Function(${name})`;
        } else {
            return 'Function';
        }
    }
    // objects
    if (Array.isArray(val)) {
        const length = val.length;
        let debug = '[';
        if (length > 0) {
            debug += debugString(val[0]);
        }
        for(let i = 1; i < length; i++) {
            debug += ', ' + debugString(val[i]);
        }
        debug += ']';
        return debug;
    }
    // Test for built-in
    const builtInMatches = /\[object ([^\]]+)\]/.exec(toString.call(val));
    let className;
    if (builtInMatches.length > 1) {
        className = builtInMatches[1];
    } else {
        // Failed to match the standard '[object ClassName]'
        return toString.call(val);
    }
    if (className == 'Object') {
        // we're a user defined class or Object
        // JSON.stringify avoids problems with cycles, and is generally much
        // easier than looping through ownProperties of `val`.
        try {
            return 'Object(' + JSON.stringify(val) + ')';
        } catch (_) {
            return 'Object';
        }
    }
    // errors
    if (val instanceof Error) {
        return `${val.name}: ${val.message}\n${val.stack}`;
    }
    // TODO we could test for more things here, like `Set`s and `Map`s.
    return className;
}

let stack_pointer = 128;

function addBorrowedObject(obj) {
    if (stack_pointer == 1) throw new Error('out of js stack');
    heap[--stack_pointer] = obj;
    return stack_pointer;
}
/**
* @param {any} query
* @returns {any}
*/
function attenuate_token(query) {
    try {
        const ret = wasm.attenuate_token(addBorrowedObject(query));
        return takeObject(ret);
    } finally {
        heap[stack_pointer++] = undefined;
    }
}

/**
* @param {any} query
* @returns {any}
*/
function execute(query) {
    try {
        const ret = wasm.execute(addBorrowedObject(query));
        return takeObject(ret);
    } finally {
        heap[stack_pointer++] = undefined;
    }
}

/**
* @param {any} query
* @returns {any}
*/
function execute_serialized(query) {
    try {
        const ret = wasm.execute_serialized(addBorrowedObject(query));
        return takeObject(ret);
    } finally {
        heap[stack_pointer++] = undefined;
    }
}

/**
* @returns {any}
*/
function generate_keypair() {
    const ret = wasm.generate_keypair();
    return takeObject(ret);
}

/**
* @param {string} private_key
* @returns {string}
*/
function get_public_key(private_key) {
    let deferred3_0;
    let deferred3_1;
    try {
        const retptr = wasm.__wbindgen_add_to_stack_pointer(-16);
        const ptr0 = passStringToWasm0(private_key, wasm.__wbindgen_malloc, wasm.__wbindgen_realloc);
        const len0 = WASM_VECTOR_LEN;
        wasm.get_public_key(retptr, ptr0, len0);
        var r0 = getInt32Memory0()[retptr / 4 + 0];
        var r1 = getInt32Memory0()[retptr / 4 + 1];
        var r2 = getInt32Memory0()[retptr / 4 + 2];
        var r3 = getInt32Memory0()[retptr / 4 + 3];
        var ptr2 = r0;
        var len2 = r1;
        if (r3) {
            ptr2 = 0; len2 = 0;
            throw takeObject(r2);
        }
        deferred3_0 = ptr2;
        deferred3_1 = len2;
        return getStringFromWasm0(ptr2, len2);
    } finally {
        wasm.__wbindgen_add_to_stack_pointer(16);
        wasm.__wbindgen_free(deferred3_0, deferred3_1, 1);
    }
}

/**
* @param {any} query
* @returns {string}
*/
function generate_token(query) {
    let deferred2_0;
    let deferred2_1;
    try {
        const retptr = wasm.__wbindgen_add_to_stack_pointer(-16);
        wasm.generate_token(retptr, addBorrowedObject(query));
        var r0 = getInt32Memory0()[retptr / 4 + 0];
        var r1 = getInt32Memory0()[retptr / 4 + 1];
        var r2 = getInt32Memory0()[retptr / 4 + 2];
        var r3 = getInt32Memory0()[retptr / 4 + 3];
        var ptr1 = r0;
        var len1 = r1;
        if (r3) {
            ptr1 = 0; len1 = 0;
            throw takeObject(r2);
        }
        deferred2_0 = ptr1;
        deferred2_1 = len1;
        return getStringFromWasm0(ptr1, len1);
    } finally {
        wasm.__wbindgen_add_to_stack_pointer(16);
        heap[stack_pointer++] = undefined;
        wasm.__wbindgen_free(deferred2_0, deferred2_1, 1);
    }
}

/**
* @param {any} query
* @returns {any}
*/
function inspect_snapshot(query) {
    try {
        const ret = wasm.inspect_snapshot(addBorrowedObject(query));
        return takeObject(ret);
    } finally {
        heap[stack_pointer++] = undefined;
    }
}

/**
* @param {any} query
* @returns {any}
*/
function parse_token(query) {
    try {
        const ret = wasm.parse_token(addBorrowedObject(query));
        return takeObject(ret);
    } finally {
        heap[stack_pointer++] = undefined;
    }
}

function handleError(f, args) {
    try {
        return f.apply(this, args);
    } catch (e) {
        wasm.__wbindgen_exn_store(addHeapObject(e));
    }
}

async function __wbg_load(module, imports) {
    if (typeof Response === 'function' && module instanceof Response) {
        if (typeof WebAssembly.instantiateStreaming === 'function') {
            try {
                return await WebAssembly.instantiateStreaming(module, imports);

            } catch (e) {
                if (module.headers.get('Content-Type') != 'application/wasm') {
                    console.warn("`WebAssembly.instantiateStreaming` failed because your server does not serve wasm with `application/wasm` MIME type. Falling back to `WebAssembly.instantiate` which is slower. Original error:\n", e);

                } else {
                    throw e;
                }
            }
        }

        const bytes = await module.arrayBuffer();
        return await WebAssembly.instantiate(bytes, imports);

    } else {
        const instance = await WebAssembly.instantiate(module, imports);

        if (instance instanceof WebAssembly.Instance) {
            return { instance, module };

        } else {
            return instance;
        }
    }
}

function __wbg_get_imports() {
    const imports = {};
    imports.wbg = {};
    imports.wbg.__wbindgen_is_undefined = function(arg0) {
        const ret = getObject(arg0) === undefined;
        return ret;
    };
    imports.wbg.__wbindgen_in = function(arg0, arg1) {
        const ret = getObject(arg0) in getObject(arg1);
        return ret;
    };
    imports.wbg.__wbindgen_is_object = function(arg0) {
        const val = getObject(arg0);
        const ret = typeof(val) === 'object' && val !== null;
        return ret;
    };
    imports.wbg.__wbindgen_object_drop_ref = function(arg0) {
        takeObject(arg0);
    };
    imports.wbg.__wbindgen_object_clone_ref = function(arg0) {
        const ret = getObject(arg0);
        return addHeapObject(ret);
    };
    imports.wbg.__wbindgen_string_get = function(arg0, arg1) {
        const obj = getObject(arg1);
        const ret = typeof(obj) === 'string' ? obj : undefined;
        var ptr1 = isLikeNone(ret) ? 0 : passStringToWasm0(ret, wasm.__wbindgen_malloc, wasm.__wbindgen_realloc);
        var len1 = WASM_VECTOR_LEN;
        getInt32Memory0()[arg0 / 4 + 1] = len1;
        getInt32Memory0()[arg0 / 4 + 0] = ptr1;
    };
    imports.wbg.__wbg_log_19ef6052afc58b5a = function(arg0, arg1) {
        console.log(getStringFromWasm0(arg0, arg1));
    };
    imports.wbg.__wbg_new_abda76e883ba8a5f = function() {
        const ret = new Error();
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_stack_658279fe44541cf6 = function(arg0, arg1) {
        const ret = getObject(arg1).stack;
        const ptr1 = passStringToWasm0(ret, wasm.__wbindgen_malloc, wasm.__wbindgen_realloc);
        const len1 = WASM_VECTOR_LEN;
        getInt32Memory0()[arg0 / 4 + 1] = len1;
        getInt32Memory0()[arg0 / 4 + 0] = ptr1;
    };
    imports.wbg.__wbg_error_f851667af71bcfc6 = function(arg0, arg1) {
        let deferred0_0;
        let deferred0_1;
        try {
            deferred0_0 = arg0;
            deferred0_1 = arg1;
            console.error(getStringFromWasm0(arg0, arg1));
        } finally {
            wasm.__wbindgen_free(deferred0_0, deferred0_1, 1);
        }
    };
    imports.wbg.__wbindgen_string_new = function(arg0, arg1) {
        const ret = getStringFromWasm0(arg0, arg1);
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_debug_7d879afce6cf56cb = function(arg0, arg1, arg2, arg3) {
        console.debug(getObject(arg0), getObject(arg1), getObject(arg2), getObject(arg3));
    };
    imports.wbg.__wbg_error_8e3928cfb8a43e2b = function(arg0) {
        console.error(getObject(arg0));
    };
    imports.wbg.__wbg_error_696630710900ec44 = function(arg0, arg1, arg2, arg3) {
        console.error(getObject(arg0), getObject(arg1), getObject(arg2), getObject(arg3));
    };
    imports.wbg.__wbg_info_80803d9a3f0aad16 = function(arg0, arg1, arg2, arg3) {
        console.info(getObject(arg0), getObject(arg1), getObject(arg2), getObject(arg3));
    };
    imports.wbg.__wbg_log_151eb4333ef0fe39 = function(arg0, arg1, arg2, arg3) {
        console.log(getObject(arg0), getObject(arg1), getObject(arg2), getObject(arg3));
    };
    imports.wbg.__wbg_warn_5d3f783b0bae8943 = function(arg0, arg1, arg2, arg3) {
        console.warn(getObject(arg0), getObject(arg1), getObject(arg2), getObject(arg3));
    };
    imports.wbg.__wbindgen_number_get = function(arg0, arg1) {
        const obj = getObject(arg1);
        const ret = typeof(obj) === 'number' ? obj : undefined;
        getFloat64Memory0()[arg0 / 8 + 1] = isLikeNone(ret) ? 0 : ret;
        getInt32Memory0()[arg0 / 4 + 0] = !isLikeNone(ret);
    };
    imports.wbg.__wbindgen_jsval_loose_eq = function(arg0, arg1) {
        const ret = getObject(arg0) == getObject(arg1);
        return ret;
    };
    imports.wbg.__wbindgen_boolean_get = function(arg0) {
        const v = getObject(arg0);
        const ret = typeof(v) === 'boolean' ? (v ? 1 : 0) : 2;
        return ret;
    };
    imports.wbg.__wbindgen_error_new = function(arg0, arg1) {
        const ret = new Error(getStringFromWasm0(arg0, arg1));
        return addHeapObject(ret);
    };
    imports.wbg.__wbindgen_number_new = function(arg0) {
        const ret = arg0;
        return addHeapObject(ret);
    };
    imports.wbg.__wbindgen_bigint_from_u64 = function(arg0) {
        const ret = BigInt.asUintN(64, arg0);
        return addHeapObject(ret);
    };
    imports.wbg.__wbindgen_bigint_from_u128 = function(arg0, arg1) {
        const ret = BigInt.asUintN(64, arg0) << BigInt(64) | BigInt.asUintN(64, arg1);
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_getwithrefkey_15c62c2b8546208d = function(arg0, arg1) {
        const ret = getObject(arg0)[getObject(arg1)];
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_set_20cbc34131e76824 = function(arg0, arg1, arg2) {
        getObject(arg0)[takeObject(arg1)] = takeObject(arg2);
    };
    imports.wbg.__wbg_crypto_1d1f22824a6a080c = function(arg0) {
        const ret = getObject(arg0).crypto;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_process_4a72847cc503995b = function(arg0) {
        const ret = getObject(arg0).process;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_versions_f686565e586dd935 = function(arg0) {
        const ret = getObject(arg0).versions;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_node_104a2ff8d6ea03a2 = function(arg0) {
        const ret = getObject(arg0).node;
        return addHeapObject(ret);
    };
    imports.wbg.__wbindgen_is_string = function(arg0) {
        const ret = typeof(getObject(arg0)) === 'string';
        return ret;
    };
    imports.wbg.__wbg_require_cca90b1a94a0255b = function() { return handleError(function () {
        const ret = module.require;
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_msCrypto_eb05e62b530a1508 = function(arg0) {
        const ret = getObject(arg0).msCrypto;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_randomFillSync_5c9c955aa56b6049 = function() { return handleError(function (arg0, arg1) {
        getObject(arg0).randomFillSync(takeObject(arg1));
    }, arguments) };
    imports.wbg.__wbg_getRandomValues_3aa56aa6edec874c = function() { return handleError(function (arg0, arg1) {
        getObject(arg0).getRandomValues(getObject(arg1));
    }, arguments) };
    imports.wbg.__wbindgen_is_function = function(arg0) {
        const ret = typeof(getObject(arg0)) === 'function';
        return ret;
    };
    imports.wbg.__wbg_self_ce0dbfc45cf2f5be = function() { return handleError(function () {
        const ret = self.self;
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_window_c6fb939a7f436783 = function() { return handleError(function () {
        const ret = window.window;
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_globalThis_d1e6af4856ba331b = function() { return handleError(function () {
        const ret = globalThis.globalThis;
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_global_207b558942527489 = function() { return handleError(function () {
        const ret = global.global;
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_newnoargs_e258087cd0daa0ea = function(arg0, arg1) {
        const ret = new Function(getStringFromWasm0(arg0, arg1));
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_length_cd7af8117672b8b8 = function(arg0) {
        const ret = getObject(arg0).length;
        return ret;
    };
    imports.wbg.__wbg_new_16b304a2cfa7ff4a = function() {
        const ret = new Array();
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_next_40fc327bfc8770e6 = function(arg0) {
        const ret = getObject(arg0).next;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_value_d93c65011f51a456 = function(arg0) {
        const ret = getObject(arg0).value;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_iterator_2cee6dadfd956dfa = function() {
        const ret = Symbol.iterator;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_new_72fb9a18b5ae2624 = function() {
        const ret = new Object();
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_get_bd8e338fbd5f5cc8 = function(arg0, arg1) {
        const ret = getObject(arg0)[arg1 >>> 0];
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_set_d4638f722068f043 = function(arg0, arg1, arg2) {
        getObject(arg0)[arg1 >>> 0] = takeObject(arg2);
    };
    imports.wbg.__wbg_isArray_2ab64d95e09ea0ae = function(arg0) {
        const ret = Array.isArray(getObject(arg0));
        return ret;
    };
    imports.wbg.__wbg_instanceof_ArrayBuffer_836825be07d4c9d2 = function(arg0) {
        let result;
        try {
            result = getObject(arg0) instanceof ArrayBuffer;
        } catch (_) {
            result = false;
        }
        const ret = result;
        return ret;
    };
    imports.wbg.__wbg_call_27c0f87801dedf93 = function() { return handleError(function (arg0, arg1) {
        const ret = getObject(arg0).call(getObject(arg1));
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_call_b3ca7c6051f9bec1 = function() { return handleError(function (arg0, arg1, arg2) {
        const ret = getObject(arg0).call(getObject(arg1), getObject(arg2));
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_next_196c84450b364254 = function() { return handleError(function (arg0) {
        const ret = getObject(arg0).next();
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_done_298b57d23c0fc80c = function(arg0) {
        const ret = getObject(arg0).done;
        return ret;
    };
    imports.wbg.__wbg_get_e3c254076557e348 = function() { return handleError(function (arg0, arg1) {
        const ret = Reflect.get(getObject(arg0), getObject(arg1));
        return addHeapObject(ret);
    }, arguments) };
    imports.wbg.__wbg_buffer_12d079cc21e14bdb = function(arg0) {
        const ret = getObject(arg0).buffer;
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_newwithbyteoffsetandlength_aa4a17c33a06e5cb = function(arg0, arg1, arg2) {
        const ret = new Uint8Array(getObject(arg0), arg1 >>> 0, arg2 >>> 0);
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_new_63b92bc8671ed464 = function(arg0) {
        const ret = new Uint8Array(getObject(arg0));
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_instanceof_Uint8Array_2b3bbecd033d19f6 = function(arg0) {
        let result;
        try {
            result = getObject(arg0) instanceof Uint8Array;
        } catch (_) {
            result = false;
        }
        const ret = result;
        return ret;
    };
    imports.wbg.__wbg_newwithlength_e9b4878cebadb3d3 = function(arg0) {
        const ret = new Uint8Array(arg0 >>> 0);
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_subarray_a1f73cd4b5b42fe1 = function(arg0, arg1, arg2) {
        const ret = getObject(arg0).subarray(arg1 >>> 0, arg2 >>> 0);
        return addHeapObject(ret);
    };
    imports.wbg.__wbg_length_c20a40f15020d68a = function(arg0) {
        const ret = getObject(arg0).length;
        return ret;
    };
    imports.wbg.__wbg_set_a47bac70306a19a7 = function(arg0, arg1, arg2) {
        getObject(arg0).set(getObject(arg1), arg2 >>> 0);
    };
    imports.wbg.__wbindgen_debug_string = function(arg0, arg1) {
        const ret = debugString(getObject(arg1));
        const ptr1 = passStringToWasm0(ret, wasm.__wbindgen_malloc, wasm.__wbindgen_realloc);
        const len1 = WASM_VECTOR_LEN;
        getInt32Memory0()[arg0 / 4 + 1] = len1;
        getInt32Memory0()[arg0 / 4 + 0] = ptr1;
    };
    imports.wbg.__wbindgen_throw = function(arg0, arg1) {
        throw new Error(getStringFromWasm0(arg0, arg1));
    };
    imports.wbg.__wbindgen_memory = function() {
        const ret = wasm.memory;
        return addHeapObject(ret);
    };
    imports['./snippets/biscuit-auth-4a94c16b4e5134af/inline0.js'] = __wbg_star0;

    return imports;
}

function __wbg_finalize_init(instance, module) {
    wasm = instance.exports;
    __wbg_init.__wbindgen_wasm_module = module;
    cachedFloat64Memory0 = null;
    cachedInt32Memory0 = null;
    cachedUint8Memory0 = null;

    wasm.__wbindgen_start();
    return wasm;
}

async function __wbg_init(input) {
    if (wasm !== undefined) return wasm;

    if (typeof input === 'undefined') {
        input = new URL(new URL('assets/biscuit_bg-0db375d4.wasm', import.meta.url).href, import.meta.url);
    }
    const imports = __wbg_get_imports();

    if (typeof input === 'string' || (typeof Request === 'function' && input instanceof Request) || (typeof URL === 'function' && input instanceof URL)) {
        input = fetch(input);
    }

    const { instance, module } = await __wbg_load(await input, imports);

    return __wbg_finalize_init(instance, module);
}

/**
 * TODO DOCS
 */
let BcAuthorizerResult = class BcAuthorizerResult extends s {
    renderLogicError(e) {
        var _a, _b, _c, _d;
        const failedChecksCount = (_d = (_b = (_a = e.Unauthorized) === null || _a === void 0 ? void 0 : _a.checks.length) !== null && _b !== void 0 ? _b : (_c = e.NoMatchingPolicy) === null || _c === void 0 ? void 0 : _c.checks.length) !== null && _d !== void 0 ? _d : 0;
        const failedChecks = failedChecksCount > 0
            ? x `<pre>${failedChecksCount} failed checks</pre>`
            : null;
        let policyError;
        if (e.NoMatchingPolicy) {
            policyError = x `<pre>No policy matched</pre>`;
        }
        else if (e.Unauthorized && e.Unauthorized.policy.Deny !== undefined) {
            policyError = x `<pre>A deny policy matched</pre>`;
        }
        return x `<div>${failedChecks} ${policyError}</div>`;
    }
    renderResult() {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k, _l, _m;
        const logicError = (_d = (_c = (_b = (_a = this.content) === null || _a === void 0 ? void 0 : _a.Ok) === null || _b === void 0 ? void 0 : _b.authorizer_result) === null || _c === void 0 ? void 0 : _c.Err) === null || _d === void 0 ? void 0 : _d.FailedLogic;
        const parsingError = (_f = (_e = this.content) === null || _e === void 0 ? void 0 : _e.Err) === null || _f === void 0 ? void 0 : _f.token;
        const publicKeyError = (_h = (_g = this.content) === null || _g === void 0 ? void 0 : _g.Err) === null || _h === void 0 ? void 0 : _h.root_key;
        const success = ((_l = (_k = (_j = this.content) === null || _j === void 0 ? void 0 : _j.Ok) === null || _k === void 0 ? void 0 : _k.authorizer_result) === null || _l === void 0 ? void 0 : _l.Ok) !== undefined;
        if (success) {
            return x `<div><pre>Success</pre></div>`;
        }
        else if (logicError) {
            return this.renderLogicError(logicError);
        }
        else if (publicKeyError) {
            return x `<div><pre>Public key parsing error</pre></div>`;
        }
        else if (parsingError) {
            return x `<div><pre>Token parsing error</pre></div>`;
        }
        else if ((_m = this.content) === null || _m === void 0 ? void 0 : _m.Err) {
            return x `<div><pre>Datalog execution error</pre></div>`;
        }
        else {
            console.error(this.content);
            return x `<div><pre>Unknown error</pre></div>`;
        }
    }
    render() {
        if (this.content === null) {
            return x ` <div><pre>no result yet</pre></div> `;
        }
        else {
            return this.renderResult();
        }
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
};
__decorate([
    n$1({ type: Object })
], BcAuthorizerResult.prototype, "content", void 0);
BcAuthorizerResult = __decorate([
    e$3("bc-authorizer-result")
], BcAuthorizerResult);

let loadPromise = null;
async function initialize() {
    if (loadPromise == null) {
        console.log("will create wasm promise");
        loadPromise = __wbg_init();
    }
    console.log("returning wasm promise");
    return loadPromise;
}

/**
 * TODO DOCS
 */
class BcAuthorizerExample extends s {
    static get properties() {
        return {
            code: { type: String },
            defaultAllow: { type: Boolean },
            started: { type: Boolean },
        };
    }
    constructor() {
        super();
        if (this.children[0] != undefined) {
            this.code = this.children[0].innerHTML;
        }
        else {
            this.code = "";
        }
        this.defaultAllow = false;
        this.started = false;
    }
    _onUpdatedCode(code) {
        this.code = code;
        dispatchCustomEvent(this, "update", { code: code });
    }
    firstUpdated(changedProperties) {
        initialize().then(() => (this.started = true));
    }
    update(changedProperties) {
        super.update(changedProperties);
    }
    render() {
        var _a, _b, _c, _d;
        var parseErrors = [];
        var markers = [];
        var authorizer_world = [];
        var code = this.code;
        if (this.defaultAllow) {
            code += "\n\nallow if true;";
        }
        if (this.started) {
            var state = {
                token_blocks: [""],
                authorizer_code: code,
                external_private_keys: [],
                query: "",
            };
            var result = execute(state);
            if (result.Ok === undefined) {
                for (let b of result.Err.blocks) {
                    var errors = [];
                    var marks = [];
                    for (let error of b) {
                        errors.push({
                            message: error.message,
                            severity: "error",
                            start: error.position.start,
                            end: error.position.end,
                        });
                    }
                    blockParseErrors.push(errors);
                }
                for (let error of result.Err.authorizer) {
                    parseErrors.push({
                        message: error.message,
                        severity: "error",
                        start: error.position.start,
                        end: error.position.end,
                    });
                }
            }
            else {
                for (let b of result.Ok.token_blocks) {
                    var marks = [];
                    for (let marker of b.markers) {
                        marks.push({
                            start: marker.position.start,
                            end: marker.position.end,
                            ok: marker.ok,
                        });
                    }
                    blockMarkers.push(marks);
                }
                for (let marker of result.Ok.authorizer_editor.markers) {
                    console.log(marker);
                    markers.push({
                        start: marker.position.start,
                        end: marker.position.end,
                        ok: marker.ok,
                    });
                }
            }
            authorizer_world = (_b = (_a = result.Ok) === null || _a === void 0 ? void 0 : _a.authorizer_world) !== null && _b !== void 0 ? _b : [];
            (_d = (_c = result.Ok) === null || _c === void 0 ? void 0 : _c.authorizer_result) !== null && _d !== void 0 ? _d : null;
            if (result.authorizer_editor != null) {
                for (let error of result.authorizer_editor.errors) {
                    parseErrors.push({
                        message: error.message,
                        severity: "error",
                        from: error.position.start,
                        to: error.position.end,
                    });
                }
                for (let marker of result.authorizer_editor.markers) {
                    console.log(marker);
                    markers.push({
                        start: marker.position.start,
                        end: marker.position.end,
                        ok: marker.ok,
                    });
                }
            }
        }
        return x `
      <bc-authorizer-editor
        code="${this.code}"
        parseErrors="${JSON.stringify(parseErrors)}"
        markers="${JSON.stringify(markers)}"
        @bc-authorizer-editor:update="${(e) => {
            this._onUpdatedCode(e.detail.code);
        }}"
        }
      >
      </bc-authorizer-editor>
      <em>Execution result</em>
      <bc-authorizer-result .content=${result}></bc-authorizer-result>
      <details>
        <summary>Facts</summary>
        <bc-authorizer-content
          content="${JSON.stringify(authorizer_world)}"
        ></bc-authorizer-content>
      </details>
    `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-authorizer-example", BcAuthorizerExample);

/**
 * TODO DOCS
 */
class BcDatalogExample extends s {
    static get properties() {
        return {
            code: { type: String },
            started: { type: Boolean },
        };
    }
    constructor() {
        super();
        if (this.children[0] != undefined) {
            this.code = this.children[0].innerHTML;
        }
        else {
            this.code = "";
        }
        console.log("constructor: got code:");
        console.log(this.code);
        this.started = false;
    }
    _onUpdatedCode(code) {
        this.code = code;
        dispatchCustomEvent(this, "update", { code: code });
    }
    firstUpdated(changedProperties) {
        initialize().then(() => (this.started = true));
    }
    update(changedProperties) {
        super.update(changedProperties);
    }
    render() {
        var parseErrors = [];
        var markers = [];
        var authorizer_world = [];
        var code = this.code;
        code += "\n\nallow if true;";
        if (this.started) {
            var state = {
                token_blocks: [],
                authorizer_code: code,
                query: "",
            };
            var result = execute(state);
            result.authorizer_result;
            authorizer_world = result.authorizer_world;
            if (result.authorizer_editor != null) {
                for (let error of result.authorizer_editor.errors) {
                    parseErrors.push({
                        message: error.message,
                        severity: "error",
                        start: error.position.start,
                        end: error.position.end,
                    });
                }
                for (let marker of result.authorizer_editor.markers) {
                    // do not display the marker for the additional "allow if true"
                    if (marker.position.start != this.code.length + 2) {
                        markers.push({
                            start: marker.position.start,
                            end: marker.position.end,
                            ok: marker.ok,
                        });
                    }
                }
            }
        }
        return x `
      <bc-authorizer-editor
        code="${this.code}"
        parseErrors="${JSON.stringify(parseErrors)}"
        markers="${JSON.stringify(markers)}"
        @bc-authorizer-editor:update="${(e) => {
            this._onUpdatedCode(e.detail.code);
        }}"
        }
      >
      </bc-authorizer-editor>
      Facts:
      <bc-authorizer-content
        content="${JSON.stringify(authorizer_world)}"
      ></bc-authorizer-content>
    `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: block;
        }
      `,
        ];
    }
}
window.customElements.define("bc-datalog-example", BcDatalogExample);

/**
 * TODO DOCS
 */
class BcFullExample extends s {
    static get properties() {
        return {
            blocks: { type: Array },
            _authorizer: { type: String },
            _started: { type: Boolean },
        };
    }
    constructor() {
        super();
        this.blocks = [];
        for (let block of this.querySelectorAll(".block")) {
            console.log("block: " + block.innerHTML);
            this.blocks.push({ code: block.innerHTML });
        }
        let auth = this.querySelector(".authorizer");
        if (auth !== null) {
            this._authorizer = auth.innerHTML;
        }
        console.log("authorizer: " + this._authorizer);
        this._started = false;
    }
    _onUpdatedBlock(index, code) {
        console.log("full::_onUpdatedCode");
        console.log(code);
        this.blocks[index].code = code;
        console.log(this.blocks);
        dispatchCustomEvent(this, "update", { blocks: this.blocks });
        this.requestUpdate();
    }
    _onUpdatedAuthorizer(code) {
        this._authorizer = code;
        dispatchCustomEvent(this, "update", { _authorizer: code });
        this.requestUpdate();
    }
    firstUpdated(changedProperties) {
        initialize().then(() => {
            console.log("start");
            this._started = true;
        });
    }
    update(changedProperties) {
        super.update(changedProperties);
    }
    render() {
        var _a, _b, _c, _d;
        console.log("render0");
        if (!this._started) {
            return x ``;
        }
        console.log(this.blocks);
        let blocks = [];
        for (let b of this.blocks) {
            blocks.push(b.code);
        }
        var state = {
            token_blocks: blocks,
            authorizer_code: this._authorizer,
            query: "",
            external_private_keys: [],
        };
        console.log("WILL EXECUTE");
        var result = execute(state);
        console.log(result);
        var parseErrors = [];
        var markers = [];
        var authorizer_world = [];
        var blockParseErrors = [];
        var blockMarkers = [];
        // handle parse errors
        if (result.Ok === undefined) {
            for (let b of result.Err.blocks) {
                var errors = [];
                var marks = [];
                for (let error of b) {
                    errors.push({
                        message: error.message,
                        severity: "error",
                        start: error.position.start,
                        end: error.position.end,
                    });
                }
                blockParseErrors.push(errors);
            }
            for (let error of result.Err.authorizer) {
                parseErrors.push({
                    message: error.message,
                    severity: "error",
                    start: error.position.start,
                    end: error.position.end,
                });
            }
        }
        else {
            for (let b of result.Ok.token_blocks) {
                var marks = [];
                for (let marker of b.markers) {
                    marks.push({
                        start: marker.position.start,
                        end: marker.position.end,
                        ok: marker.ok,
                    });
                }
                blockMarkers.push(marks);
            }
            for (let marker of result.Ok.authorizer_editor.markers) {
                console.log(marker);
                markers.push({
                    start: marker.position.start,
                    end: marker.position.end,
                    ok: marker.ok,
                });
            }
        }
        authorizer_world = (_b = (_a = result.Ok) === null || _a === void 0 ? void 0 : _a.authorizer_world) !== null && _b !== void 0 ? _b : [];
        (_d = (_c = result.Ok) === null || _c === void 0 ? void 0 : _c.authorizer_result) !== null && _d !== void 0 ? _d : null;
        return x `
      <div class="blocks">
        <p>Token</p>
        ${this.blocks.map((block, index) => x `
            <p>Block ${index}</p>
            <bc-datalog-editor
              code=${block.code}
              .marks=${blockMarkers[index].concat(blockParseErrors[index])}
              @bc-datalog-editor:update="${(e) => {
            this._onUpdatedBlock(index, e.detail.code);
        }}"
              }
            >
            </bc-datalog-editor>
          `)}
      </div>

      <div class="authorizer">
        <p>Authorizer policies</p>
        <bc-authorizer-editor
          code="${this._authorizer}"
          parseErrors="${JSON.stringify(parseErrors)}"
          markers="${JSON.stringify(markers)}"
          @bc-authorizer-editor:update="${(e) => {
            this._onUpdatedAuthorizer(e.detail.code);
        }}"
          }
        >
        </bc-authorizer-editor>

        <em>Execution result</em>
        <bc-authorizer-result .content=${result}></bc-authorizer-result>
        <details>
          <summary>Facts</summary>
          <bc-authorizer-content
            content="${JSON.stringify(authorizer_world)}"
          ></bc-authorizer-content>
        </details>
      </div>
    `;
    }
    static get styles() {
        return [
            // language=CSS
            i$3 `
        :host {
          display: flex;
          flex-direction: column;
          text-align: left;
        }

        @media (prefers-color-scheme: dark) {
          :host {
            color: #dee2e6;
            background: #131314;
          }
          textarea {
            color: #dee2e6;
            background: #131314;
          }
        }

        @media (prefers-color-scheme: light) {
          :host {
            color: #1d2d35;
            background: #fff;
          }
        }

        @media (min-width: 576px) {
          :host {
            display: flex;
            flex-flow: row wrap;
            flex-direction: row;
          }

          .blocks {
            order: 1;
            width: 49%;
          }

          .authorizer {
            order: 2;
            width: 49%;
          }
        }

        .blocks {
          border: 1px rgba(128, 128, 128, 0.4) solid;
        }
        .authorizer {
          border-top: 1px rgba(128, 128, 128, 0.4) solid;
          border-right: 1px rgba(128, 128, 128, 0.4) solid;
          border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
        }

        p {
          margin-block-start: 0px;
          margin-block-end: 0px;
          padding: 0.2em;
          font-size: 0.8em;
          border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
        }

        bc-datalog-editor {
          border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
        }

        bc-authorizer-editor {
          border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
        }
      `,
        ];
    }
}
window.customElements.define("bc-full-example", BcFullExample);

const convertMarker = (marker) => {
    console.log({ marker });
    return {
        name: marker.ok ? "mark.success" : "mark.failure",
        node: {
            startIndex: marker.position.start,
            endIndex: marker.position.end,
        },
    };
};
const convertError = (error) => {
    console.log({ error });
    return {
        name: "mark.error",
        node: {
            startIndex: error.position.start,
            endIndex: error.position.end,
        },
    };
};
function trimLines(str) {
    return str
        .trim()
        .split("\n")
        .map((line) => line.trim())
        .join("\n");
}

/**
 * TODO DOCS
 */
let BCDatalogPlayground = class BCDatalogPlayground extends s {
    constructor() {
        var _a;
        super();
        this.code = "";
        this.showBlocks = false;
        this.blocks = [];
        this.started = false;
        const codeChild = this.querySelector(".authorizer");
        if (codeChild !== null) {
            this.code = trimLines((_a = codeChild.textContent) !== null && _a !== void 0 ? _a : "");
        }
        const blockChildren = this.querySelectorAll(".block");
        this.blocks = Array.from(blockChildren)
            .map((b, i) => {
            var _a;
            const code = trimLines((_a = b.textContent) !== null && _a !== void 0 ? _a : "");
            let externalKey = null;
            if (i > 0) {
                externalKey = b.getAttribute("privateKey");
            }
            return { code, externalKey };
        })
            .filter(({ code }, i) => i === 0 || code !== "");
    }
    firstUpdated() {
        initialize().then(() => (this.started = true));
    }
    addBlock() {
        const newBlocks = [...this.blocks];
        newBlocks.push({ code: "", externalKey: null });
        this.blocks = newBlocks;
    }
    onUpdatedBlock(blockId, e) {
        const newBlocks = [...this.blocks];
        newBlocks[blockId] = {
            code: e.detail.code,
            externalKey: newBlocks[blockId].externalKey,
        };
        this.blocks = newBlocks;
    }
    onUpdatedExternalKey(blockId, e) {
        const newBlocks = [...this.blocks];
        const newValue = e.target.value.trim();
        newBlocks[blockId] = {
            code: newBlocks[blockId].code,
            externalKey: newValue !== "" ? newValue : null,
        };
        this.blocks = newBlocks;
    }
    onUpdatedCode(e) {
        this.code = e.detail.code;
    }
    render() {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j, _k;
        let authorizer_world = [];
        let authorizer_result = null;
        const parseErrors = {
            blocks: [],
            authorizer: [],
        };
        const markers = {
            blocks: [],
            authorizer: [],
        };
        if (this.started) {
            const authorizerQuery = {
                token_blocks: this.blocks.length > 0 ? this.blocks.map(({ code }) => code) : [""],
                authorizer_code: this.code,
                query: "",
                external_private_keys: this.blocks.map(({ externalKey }) => externalKey),
            };
            const authorizerResult = execute(authorizerQuery);
            console.warn({ authorizerQuery, authorizerResult });
            authorizer_world = (_b = (_a = authorizerResult.Ok) === null || _a === void 0 ? void 0 : _a.authorizer_world) !== null && _b !== void 0 ? _b : [];
            authorizer_result = authorizerResult;
            markers.authorizer =
                (_d = (_c = authorizerResult.Ok) === null || _c === void 0 ? void 0 : _c.authorizer_editor.markers.map(convertMarker)) !== null && _d !== void 0 ? _d : [];
            parseErrors.authorizer =
                (_f = (_e = authorizerResult.Err) === null || _e === void 0 ? void 0 : _e.authorizer.map(convertError)) !== null && _f !== void 0 ? _f : [];
            markers.blocks =
                (_h = (_g = authorizerResult.Ok) === null || _g === void 0 ? void 0 : _g.token_blocks.map((b) => b.markers.map(convertMarker))) !== null && _h !== void 0 ? _h : [];
            parseErrors.blocks =
                (_k = (_j = authorizerResult.Err) === null || _j === void 0 ? void 0 : _j.blocks.map((b) => b.map(convertError))) !== null && _k !== void 0 ? _k : [];
        }
        return x `
      ${this.renderBlocks(markers.blocks, parseErrors.blocks)}
      ${this.renderAuthorizer(markers.authorizer, parseErrors.authorizer)}
      <p>Result</p>
      <bc-authorizer-result .content=${authorizer_result}>
      </bc-authorizer-result>
      <p>Facts</p>
      <bc-authorizer-content
        .content=${authorizer_world}
      ></bc-authorizer-content>
    `;
    }
    renderExternalKeyInput(blockId) {
        if (blockId <= 0)
            return;
        return x `
      <input
        @input=${(e) => this.onUpdatedExternalKey(blockId, e)}
        value=${this.blocks[blockId].externalKey}
      />
    `;
    }
    renderBlock(blockId, code, markers = [], errors = []) {
        return x ` <p>
        ${blockId == 0 ? "Authority block" : "Block " + blockId}:
        ${this.renderExternalKeyInput(blockId)}
      </p>
      <bc-datalog-editor
        code=${code}
        .marks=${markers.concat(errors)}
        @bc-datalog-editor:update=${(e) => this.onUpdatedBlock(blockId, e)}
        }
      >
      </bc-datalog-editor>`;
    }
    renderBlocks(markers, errors) {
        if (!this.showBlocks)
            return;
        return x `
      ${this.blocks.map(({ code }, id) => {
            return this.renderBlock(id, code, markers[id], errors[id]);
        })}
      <button @click=${this.addBlock}>Add block</button>
    `;
    }
    renderAuthorizer(markers, parseErrors) {
        return x ` <p>Authorizer</p>
      <bc-datalog-editor
        code=${this.code}
        @bc-datalog-editor:update=${this.onUpdatedCode}
        .marks=${markers.concat(parseErrors)}
      >
      </bc-datalog-editor>`;
    }
};
__decorate([
    n$1()
], BCDatalogPlayground.prototype, "code", void 0);
__decorate([
    n$1()
], BCDatalogPlayground.prototype, "showBlocks", void 0);
__decorate([
    t$1()
], BCDatalogPlayground.prototype, "blocks", void 0);
__decorate([
    t$1()
], BCDatalogPlayground.prototype, "started", void 0);
BCDatalogPlayground = __decorate([
    e$3("bc-datalog-playground")
], BCDatalogPlayground);

/**
 * TODO DOCS
 */
let BcTokenPrinter = class BcTokenPrinter extends s {
    constructor() {
        var _a;
        super();
        this.biscuit = "";
        this.readonly = false;
        this.showAuthorizer = false;
        this.authorizer = "";
        this.rootPublicKey = "";
        this.showAttenuation = false;
        this.extraBlocks = [];
        this._started = false;
        const authorizerChild = this.querySelector(".authorizer");
        if (authorizerChild !== null) {
            this.authorizer = trimLines((_a = authorizerChild.textContent) !== null && _a !== void 0 ? _a : "");
        }
    }
    firstUpdated() {
        initialize().then(() => (this._started = true));
    }
    _onUpdatedToken(e) {
        if (this.readonly)
            return;
        this.biscuit = e.target.value.trim();
    }
    _onUpdatedPublicKey(e) {
        this.rootPublicKey = e.target.value.trim();
    }
    _onUpdatedAuthorizer(e) {
        if (!this.showAuthorizer)
            return;
        this.authorizer = e.detail.code;
    }
    _onUpdatedExtraBlock(blockId, e) {
        const newBlocks = [...this.extraBlocks];
        newBlocks[blockId] = e.detail.code;
        this.extraBlocks = newBlocks;
    }
    _addExtraBlock() {
        const newBlocks = [...this.extraBlocks].filter((b) => b !== "");
        newBlocks.push("");
        this.extraBlocks = newBlocks;
    }
    renderTokenInput() {
        if (this.readonly) {
            return x `
        <div class="code">
          <p>Encoded token</p>
          <code>${this.biscuit}</code>
        </div>
      `;
        }
        return x `
      <div class="code">
        <p>Encoded token</p>
        <textarea @input=${this._onUpdatedToken}>${this.biscuit}</textarea>
      </div>
    `;
    }
    renderNotStarted() {
        return x `
      <div class="token">
        ${this.renderTokenInput()}
        <div class="content">loading biscuit token</div>
      </div>
    `;
    }
    renderEmptyToken() {
        return x `
      ${this.renderTokenInput()}
      <div class="content">Please enter a base64-encoded token</div>
    `;
    }
    renderResult(error, blocks, blockMarkers) {
        if (this.biscuit === "") {
            return x `
        ${this.renderTokenInput()}
        <div class="content">Please enter a base64-encoded token</div>
      `;
        }
        if (error) {
            return x `
        ${this.renderTokenInput()}
        <div class="content">${error}</div>
      `;
        }
        return x `
      ${this.renderTokenInput()}
      <div class="content">
        <p>Decoded token</p>
        ${blocks.map((block, index) => {
            var _a, _b;
            return x `
        <div>
        <p>${index === 0 ? "Authority block" : `Block ${index}`}:</p>
        <p class="revocation-id">Revocation id: <span class="id">${block.revocation_id}</span></p>
        <p class="external-key">Signed by: <span class="id">${(_a = block.external_key) !== null && _a !== void 0 ? _a : "n/a"}</span></p>
        <bc-datalog-editor
          code=${block.code}
          .marks=${(_b = blockMarkers[index]) !== null && _b !== void 0 ? _b : []}
          readonly="true"
        </bc-datalog-editor>
        </div>
      `;
        })}
      </div>
    `;
    }
    renderAuthorizer(result) {
        var _a, _b, _c, _d;
        if (!this.showAuthorizer)
            return;
        const markers = (_b = (_a = result.Ok) === null || _a === void 0 ? void 0 : _a.authorizer_editor.markers) !== null && _b !== void 0 ? _b : [];
        const errors = (_d = (_c = result.Err) === null || _c === void 0 ? void 0 : _c.authorizer) !== null && _d !== void 0 ? _d : [];
        return x `
      <div class="code">
        <p class="public-key-input">
          <label for="public-key">Public key</label>
          <input
            @input=${this._onUpdatedPublicKey}
            id="public-key"
            value="${this.rootPublicKey}"
          />
        </p>
        <p>Authorizer</p>
        <bc-authorizer-editor
          code="${this.authorizer}"
          .markers=${markers.map(convertMarker)}
          .parseErrors=${errors.map(convertError)}
          @bc-authorizer-editor:update=${this._onUpdatedAuthorizer}
        >
        </bc-authorizer-editor>
      </div>
      <div class="content">
        <p>Result</p>
        ${this.renderAuthorizerResult(result)}
      </div>
    `;
    }
    renderAuthorizerResult(result) {
        if (this.biscuit === "") {
            return x `<div><pre>Please enter a base64-encoded token</pre></div>`;
        }
        if (this.rootPublicKey === "") {
            return x `<div><pre>Please enter a public key</pre></div>`;
        }
        return x `<bc-authorizer-result
      .content=${result}
    ></bc-authorizer-result>`;
    }
    renderExtraBlock(blockId, errors = [], blocksOffset) {
        var _a;
        return x `
      <p>${"Block " + (blocksOffset + blockId)}:</p>
      <bc-datalog-editor
        code="${(_a = this.extraBlocks[blockId]) !== null && _a !== void 0 ? _a : ""}"
        .marks=${errors.map(convertError)}
        @bc-datalog-editor:update=${(e) => this._onUpdatedExtraBlock(blockId, e)}
      >
      </bc-datalog-editor>
    `;
    }
    renderAttenuation(blocksOffset) {
        var _a, _b, _c, _d;
        if (!this.showAttenuation)
            return;
        const result = attenuate_token({
            token: this.biscuit,
            blocks: this.extraBlocks.filter((b) => b !== ""),
        });
        const blocksWithErrors = [];
        ((_c = (_b = (_a = result.Err) === null || _a === void 0 ? void 0 : _a.BlockParseErrors) === null || _b === void 0 ? void 0 : _b.blocks) !== null && _c !== void 0 ? _c : []).forEach((errors, bId) => {
            if (errors.length > 0) {
                blocksWithErrors.push(bId);
            }
        });
        let errorMessage = "An error has happened";
        if (blocksWithErrors.length > 0) {
            const blockList = blocksWithErrors
                .map((bId) => (bId + blocksOffset).toString())
                .join(", ");
            errorMessage =
                "Please correct the datalog input on the following blocks: " +
                    blockList;
        }
        let attenuated;
        if (this.biscuit == "") {
            attenuated = x `Please enter a base64-encoded token`;
        }
        else {
            attenuated = (_d = result.Ok) !== null && _d !== void 0 ? _d : errorMessage;
        }
        return x `
      <div class="code">
        <p>Extra blocks</p>
        ${this.extraBlocks.map((_, id) => {
            var _a, _b, _c;
            const blockErrors = (_c = (_b = (_a = result.Err) === null || _a === void 0 ? void 0 : _a.BlockParseErrors) === null || _b === void 0 ? void 0 : _b.blocks[id]) !== null && _c !== void 0 ? _c : [];
            return this.renderExtraBlock(id, blockErrors, blocksOffset);
        })}
        <button @click=${this._addExtraBlock}>Add block</button>
      </div>
      <div class="content">
        <p>Attenuated token</p>
        <code>${attenuated}</code>
      </div>
    `;
    }
    render() {
        var _a, _b;
        if (!this._started)
            return this.renderNotStarted();
        const parseResult = parse_token({ data: this.biscuit });
        const blocks = parseResult.token_blocks.map((code, i) => ({
            code,
            revocation_id: parseResult.revocation_ids[i],
            external_key: parseResult.external_keys[i],
        }));
        const authorizerQuery = {
            token: this.biscuit,
            root_public_key: this.rootPublicKey,
            authorizer_code: this.authorizer,
            query: "",
        };
        const authorizerResult = execute_serialized(authorizerQuery);
        const blockMarkers = (_b = (_a = authorizerResult.Ok) === null || _a === void 0 ? void 0 : _a.token_blocks.map((b) => b.markers.map(convertMarker))) !== null && _b !== void 0 ? _b : [];
        // Extra blocks numbers are offset by the amount of blocks already in the token
        const blocksOffset = blocks.length;
        return x `
      <div class="row">
        ${this.renderResult(parseResult.error, blocks, this.showAuthorizer ? blockMarkers : [])}
      </div>
      <div class="row">${this.renderAuthorizer(authorizerResult)}</div>
      <div class="row">${this.renderAttenuation(blocksOffset)}</div>
    `;
    }
};
BcTokenPrinter.styles = i$3 `
    .row {
      display: flex;
      flex-direction: column;
      text-align: left;
    }

    .public-key-input {
      display: flex;
    }

    .public-key-input label {
      height: 1.5em;
    }

    #public-key {
      margin-left: 1em;
      flex-grow: 1;
    }

    @media (prefers-color-scheme: dark) {
      .row {
        color: #dee2e6;
        background: #131314;
      }
      textarea {
        color: #dee2e6;
        background: #131314;
      }
    }

    @media (prefers-color-scheme: light) {
      .row {
        color: #1d2d35;
        background: #fff;
      }
    }

    @media (min-width: 576px) {
      .row {
        display: flex;
        flex-flow: row wrap;
        flex-direction: row;
      }

      .code {
        order: 1;
        width: 49%;
      }

      .content {
        order: 2;
        width: 49%;
      }
    }

    code {
      overflow-wrap: anywhere;
      padding: 0.2em;
      padding-top: 1em;
    }

    .code {
      border: 1px rgba(128, 128, 128, 0.4) solid;
      display: flex;
      flex-direction: column;
    }

    textarea {
      flex-grow: 1;
      border: 0;
    }

    .content {
      border-top: 1px rgba(128, 128, 128, 0.4) solid;
      border-right: 1px rgba(128, 128, 128, 0.4) solid;
    }

    p {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
      margin-block-start: 0px;
      margin-block-end: 0px;
      padding: 0.2em;
      font-size: 0.8em;
    }

    .revocation-id,
    .external-key {
      overflow: hidden;
      diplay: inline-block;
      text-overflow: ellipsis;
      max-width: 100;
    }

    .revocation-id,
    .external-key > .id {
      user-select: all;
    }

    .content code {
      user-select: all;
    }

    bc-datalog-editor {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
    }
  `;
__decorate([
    n$1()
], BcTokenPrinter.prototype, "biscuit", void 0);
__decorate([
    n$1()
], BcTokenPrinter.prototype, "readonly", void 0);
__decorate([
    n$1()
], BcTokenPrinter.prototype, "showAuthorizer", void 0);
__decorate([
    n$1()
], BcTokenPrinter.prototype, "authorizer", void 0);
__decorate([
    n$1()
], BcTokenPrinter.prototype, "rootPublicKey", void 0);
__decorate([
    n$1()
], BcTokenPrinter.prototype, "showAttenuation", void 0);
__decorate([
    t$1()
], BcTokenPrinter.prototype, "extraBlocks", void 0);
__decorate([
    t$1()
], BcTokenPrinter.prototype, "_started", void 0);
BcTokenPrinter = __decorate([
    e$3("bc-token-printer")
], BcTokenPrinter);

/**
 * TODO DOCS
 */
let BcTokenGenerator = class BcTokenGenerator extends s {
    constructor() {
        super();
        this.privateKey = "";
        this._blocks = [];
        this._started = false;
        console.log("constructor");
        const blockChildren = this.querySelectorAll(".block");
        console.log({ blockChildren });
        this._blocks = Array.from(blockChildren)
            .map((b, i) => {
            var _a;
            const code = trimLines((_a = b.textContent) !== null && _a !== void 0 ? _a : "");
            let externalKey = null;
            if (i > 0) {
                externalKey = b.getAttribute("privateKey");
            }
            return { code, externalKey };
        })
            .filter(({ code }, i) => i === 0 || code !== "");
        if (this._blocks.length === 0) {
            this._blocks = [{ code: "", externalKey: null }];
        }
    }
    firstUpdated() {
        initialize().then(() => (this._started = true));
    }
    _onUpdatedBlock(blockId, e) {
        const newBlocks = [...this._blocks];
        newBlocks[blockId] = {
            code: e.detail.code,
            externalKey: this._blocks[blockId].externalKey,
        };
        this._blocks = newBlocks;
    }
    _onUpdatedBlockKey(blockId, e) {
        const newBlocks = [...this._blocks];
        newBlocks[blockId] = {
            code: this._blocks[blockId].code,
            externalKey: e.target.value.trim(),
        };
        this._blocks = newBlocks;
    }
    generateKey() {
        const { private_key } = generate_keypair();
        this.privateKey = private_key;
    }
    addBlock() {
        const newBlocks = [...this._blocks];
        newBlocks.push({ code: "", externalKey: null });
        this._blocks = newBlocks;
    }
    renderNotStarted() {
        return x `
      <div class="row">
        <div class="content">loading biscuit generator</div>
      </div>
    `;
    }
    renderKeyInput() {
        const publicKey = this.privateKey && get_public_key(this.privateKey);
        return x `
      <div class="row">
        <p>
          <label for="private-key">Private key</label>
          <input id="private-key" value="${this.privateKey}" /><br />
          <label for="public-key">Public key</label>
          <input id="public-key" value="${publicKey}" readonly disabled /><br />
          <button @click=${this.generateKey}>Generate key</button>
        </p>
      </div>
    `;
    }
    renderBlockKeyInput(blockId) {
        if (blockId <= 0)
            return;
        return x `<input
      @input=${(e) => this._onUpdatedBlockKey(blockId, e)}
      placeholder="Third party private key"
    />`;
    }
    renderBlock(blockId, errors = []) {
        var _a, _b;
        return x `
      <p>${blockId == 0 ? "Authority block" : "Block " + blockId}:</p>
      ${this.renderBlockKeyInput(blockId)}
      <bc-datalog-editor
        code="${(_b = (_a = this._blocks[blockId]) === null || _a === void 0 ? void 0 : _a.code) !== null && _b !== void 0 ? _b : ""}"
        .marks=${errors.map(convertError)}
        @bc-datalog-editor:update=${(e) => this._onUpdatedBlock(blockId, e)}
      >
      </bc-datalog-editor>
    `;
    }
    render() {
        var _a, _b, _c, _d, _e, _f, _g, _h, _j;
        if (!this._started)
            return this.renderNotStarted();
        const nonEmptyBlocks = this._blocks.filter(({ code }) => code !== "");
        const query = {
            token_blocks: nonEmptyBlocks.map(({ code }) => code),
            private_key: this.privateKey,
            external_private_keys: nonEmptyBlocks.map(({ externalKey }) => externalKey),
        };
        let result;
        try {
            result = {
                Ok: generate_token(query),
            };
        }
        catch (error) {
            result = { Err: error };
        }
        const blocksWithErrors = [];
        ((_c = (_b = (_a = result.Err) === null || _a === void 0 ? void 0 : _a.Parse) === null || _b === void 0 ? void 0 : _b.blocks) !== null && _c !== void 0 ? _c : []).forEach((errors, bId) => {
            if (errors.length > 0) {
                blocksWithErrors.push(bId);
            }
        });
        let errorMessage = "Please correct the datalog input";
        if (((_d = result.Err) === null || _d === void 0 ? void 0 : _d.Biscuit) === "InternalError") {
            errorMessage = "Please provide an authority block";
        }
        else if (typeof ((_e = result.Err) === null || _e === void 0 ? void 0 : _e.Biscuit) === "object" &&
            ((_h = (_g = (_f = result.Err) === null || _f === void 0 ? void 0 : _f.Biscuit) === null || _g === void 0 ? void 0 : _g.Format) === null || _h === void 0 ? void 0 : _h.InvalidKeySize) !== undefined) {
            errorMessage = "Please enter (or generate) a valid private key";
        }
        else if (blocksWithErrors.length > 0) {
            const blockList = blocksWithErrors
                .map((bId) => (bId === 0 ? "authority" : bId.toString()))
                .join(", ");
            errorMessage =
                "Please correct the datalog input on the following blocks: " +
                    blockList;
        }
        const token = (_j = result.Ok) !== null && _j !== void 0 ? _j : errorMessage;
        return x `
      <div class="row">
        <div class="code">
          ${this.renderKeyInput()}
          ${this._blocks.map((code, id) => {
            var _a, _b, _c;
            const blockErrors = (_c = (_b = (_a = result.Err) === null || _a === void 0 ? void 0 : _a.Parse) === null || _b === void 0 ? void 0 : _b.blocks[id]) !== null && _c !== void 0 ? _c : [];
            return this.renderBlock(id, blockErrors);
        })}
          <button @click=${this.addBlock}>Add block</button>
        </div>
        <div class="content">
          <p>Generated token</p>
          <code>${token}</code>
        </div>
      </div>
    `;
    }
};
BcTokenGenerator.styles = i$3 `
    .row {
      display: flex;
      flex-direction: column;
      text-align: left;
    }

    @media (prefers-color-scheme: dark) {
      .row {
        color: #dee2e6;
        background: #131314;
      }
      textarea {
        color: #dee2e6;
        background: #131314;
      }
    }

    @media (prefers-color-scheme: light) {
      .row {
        color: #1d2d35;
        background: #fff;
      }
    }

    @media (min-width: 576px) {
      .row {
        display: flex;
        flex-flow: row wrap;
        flex-direction: row;
      }

      .code {
        order: 1;
        width: 49%;
      }

      .content {
        order: 2;
        width: 49%;
      }
    }

    code {
      overflow-wrap: anywhere;
      padding: 0.2em;
      padding-top: 1em;
    }

    .code {
      border: 1px rgba(128, 128, 128, 0.4) solid;
      display: flex;
      flex-direction: column;
    }

    .content code {
      user-select: all;
    }

    textarea {
      flex-grow: 1;
      border: 0;
    }

    .content {
      border-top: 1px rgba(128, 128, 128, 0.4) solid;
      border-right: 1px rgba(128, 128, 128, 0.4) solid;
    }

    p {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
      margin-block-start: 0px;
      margin-block-end: 0px;
      padding: 0.2em;
      font-size: 0.8em;
    }

    bc-datalog-editor {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
    }
  `;
__decorate([
    n$1()
], BcTokenGenerator.prototype, "privateKey", void 0);
__decorate([
    t$1()
], BcTokenGenerator.prototype, "_blocks", void 0);
__decorate([
    t$1()
], BcTokenGenerator.prototype, "_started", void 0);
BcTokenGenerator = __decorate([
    e$3("bc-token-generator")
], BcTokenGenerator);

/**
 * TODO DOCS
 */
let BcSnapshotPrinter = class BcSnapshotPrinter extends s {
    constructor() {
        var _a;
        super();
        this.snapshot = "";
        this.readonly = false;
        this.showAuthorizer = false;
        this.showQuery = false;
        this.authorizer = "";
        this.query = "";
        this._started = false;
        const authorizerChild = this.querySelector(".authorizer");
        if (authorizerChild !== null) {
            this.authorizer = trimLines((_a = authorizerChild.textContent) !== null && _a !== void 0 ? _a : "");
        }
    }
    firstUpdated() {
        initialize().then(() => (this._started = true));
    }
    _onUpdatedSnapshot(e) {
        if (this.readonly)
            return;
        this.snapshot = e.target.value.trim();
    }
    _onUpdatedQuery(e) {
        if (!this.showQuery)
            return;
        this.query = e.detail.code;
    }
    _onUpdatedAuthorizer(e) {
        if (!this.showAuthorizer)
            return;
        this.authorizer = e.detail.code;
    }
    renderSnapshotInput() {
        if (this.readonly) {
            return x `
        <div class="code">
          <p>Encoded snapshot</p>
          <code>${this.snapshot}</code>
        </div>
      `;
        }
        return x `
      <div class="code">
        <p>Encoded snapshot</p>
        <textarea @input=${this._onUpdatedSnapshot}>${this.snapshot}</textarea>
      </div>
    `;
    }
    renderNotStarted() {
        return x `
      <div class="snapshot">
        ${this.renderSnapshotInput()}
        <div class="content">loading biscuit snapshot</div>
      </div>
    `;
    }
    renderEmptyToken() {
        return x `
      ${this.renderSnapshotInput()}
      <div class="content">Please enter a base64-encoded snapshot</div>
    `;
    }
    renderResult(result) {
        if (this.snapshot === "") {
            return x `
        ${this.renderSnapshotInput()}
        <div class="content">Please enter a base64-encoded snapshot</div>
      `;
        }
        if (result.snapshot.Err) {
            return x `
        ${this.renderSnapshotInput()}
        <div class="content">${result.snapshot.Err}</div>
      `;
        }
        const { code, elapsed_micros, iterations, authorization_result, query_result, } = result.snapshot.Ok;
        const auth_res = {
            Ok: {
                authorizer_editor: { markers: [] },
                authorizer_result: authorization_result,
                authorizer_world: [],
                token_blocks: [],
            },
        };
        return x `
      ${this.renderSnapshotInput()}
      <div class="content">
        <p>Elapsed time: ${elapsed_micros.toString()}μs</p>
        <p>Iterations: ${iterations}</p>
        <p>Snapshot contents</p>
        <bc-datalog-editor code=${code} .marks=${[]} readonly="true">
        </bc-datalog-editor>
        ${this.renderAuthorizerInput()}
        <bc-authorizer-result .content=${auth_res}></bc-authorizer-result>
        ${this.renderQueryInput()}
        ${query_result && this.renderQueryResult(query_result)}
      </div>
    `;
    }
    renderAuthorizerInput() {
        if (!this.showAuthorizer)
            return;
        return x `
      <p>Extra authorizer code</p>
      <bc-datalog-editor
        @bc-datalog-editor:update=${(e) => this._onUpdatedAuthorizer(e)}
      >
      </bc-datalog-editor>
    `;
    }
    renderQueryInput() {
        if (!this.showQuery)
            return;
        return x `
      <p>Query snapshot</p>
      <bc-datalog-editor
        @bc-datalog-editor:update=${(e) => this._onUpdatedQuery(e)}
      >
      </bc-datalog-editor>
    `;
    }
    renderQueryResult(result) {
        if (result.Err) {
            console.error(result.Err);
            return x `<p>error</p>`;
        }
        else {
            return x `<bc-authorizer-content .content=${result.Ok}>
      </bc-authorizer-content>`;
        }
    }
    render() {
        if (!this._started)
            return this.renderNotStarted();
        const inspectQuery = {
            data: this.snapshot,
            extra_authorizer: this.authorizer != "" ? this.authorizer : undefined,
            query: this.query != "" ? this.query : undefined,
        };
        const parseResult = inspect_snapshot(inspectQuery);
        return x ` <div class="row">${this.renderResult(parseResult)}</div> `;
    }
};
BcSnapshotPrinter.styles = i$3 `
    .row {
      display: flex;
      flex-direction: column;
      text-align: left;
    }

    .public-key-input {
      display: flex;
    }

    .public-key-input label {
      height: 1.5em;
    }

    #public-key {
      margin-left: 1em;
      flex-grow: 1;
    }

    @media (prefers-color-scheme: dark) {
      .row {
        color: #dee2e6;
        background: #131314;
      }
      textarea {
        color: #dee2e6;
        background: #131314;
      }
    }

    @media (prefers-color-scheme: light) {
      .row {
        color: #1d2d35;
        background: #fff;
      }
    }

    @media (min-width: 576px) {
      .row {
        display: flex;
        flex-flow: row wrap;
        flex-direction: row;
      }

      .code {
        order: 1;
        width: 49%;
      }

      .content {
        order: 2;
        width: 49%;
      }
    }

    code {
      overflow-wrap: anywhere;
      padding: 0.2em;
      padding-top: 1em;
    }

    .code {
      border: 1px rgba(128, 128, 128, 0.4) solid;
      display: flex;
      flex-direction: column;
    }

    textarea {
      flex-grow: 1;
      border: 0;
    }

    .content {
      border-top: 1px rgba(128, 128, 128, 0.4) solid;
      border-right: 1px rgba(128, 128, 128, 0.4) solid;
    }

    p {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
      margin-block-start: 0px;
      margin-block-end: 0px;
      padding: 0.2em;
      font-size: 0.8em;
    }

    .revocation-id,
    .external-key {
      overflow: hidden;
      diplay: inline-block;
      text-overflow: ellipsis;
      max-width: 100;
    }

    .revocation-id,
    .external-key > .id {
      user-select: all;
    }

    .content code {
      user-select: all;
    }

    bc-datalog-editor {
      border-bottom: 1px rgba(128, 128, 128, 0.4) solid;
    }
  `;
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "snapshot", void 0);
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "readonly", void 0);
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "showAuthorizer", void 0);
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "showQuery", void 0);
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "authorizer", void 0);
__decorate([
    n$1()
], BcSnapshotPrinter.prototype, "query", void 0);
__decorate([
    t$1()
], BcSnapshotPrinter.prototype, "_started", void 0);
BcSnapshotPrinter = __decorate([
    e$3("bc-snapshot-printer")
], BcSnapshotPrinter);

async function setup() {
  console.log("SETUP");
  await initialize();
  console.log("DONE");
}

setup();
