"use strict";(self.webpackChunk_detekt_website=self.webpackChunk_detekt_website||[]).push([[1319],{43174:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>a,contentTitle:()=>t,default:()=>u,frontMatter:()=>s,metadata:()=>o,toc:()=>d});var r=i(85893),l=i(11151);const s={title:"Libraries Rule Set",sidebar:"home_sidebar",keywords:["rules","libraries"],permalink:"libraries.html",toc:!0,folder:"documentation"},t=void 0,o={id:"rules/libraries",title:"Libraries Rule Set",description:"Rules in this rule set report issues related to libraries API exposure.",source:"@site/versioned_docs/version-1.23.7/rules/libraries.md",sourceDirName:"rules",slug:"/rules/libraries",permalink:"/docs/rules/libraries",draft:!1,unlisted:!1,editUrl:"https://github.com/detekt/detekt/edit/main/website/versioned_docs/version-1.23.7/rules/libraries.md",tags:[],version:"1.23.7",frontMatter:{title:"Libraries Rule Set",sidebar:"home_sidebar",keywords:["rules","libraries"],permalink:"libraries.html",toc:!0,folder:"documentation"},sidebar:"defaultSidebar",previous:{title:"Formatting Rule Set",permalink:"/docs/rules/formatting"},next:{title:"Naming Rule Set",permalink:"/docs/rules/naming"}},a={},d=[{value:"ForbiddenPublicDataClass",id:"forbiddenpublicdataclass",level:3},{value:"Configuration options:",id:"configuration-options",level:4},{value:"Noncompliant Code:",id:"noncompliant-code",level:4},{value:"Compliant Code:",id:"compliant-code",level:4},{value:"LibraryCodeMustSpecifyReturnType",id:"librarycodemustspecifyreturntype",level:3},{value:"Configuration options:",id:"configuration-options-1",level:4},{value:"Noncompliant Code:",id:"noncompliant-code-1",level:4},{value:"Compliant Code:",id:"compliant-code-1",level:4},{value:"LibraryEntitiesShouldNotBePublic",id:"libraryentitiesshouldnotbepublic",level:3},{value:"Noncompliant Code:",id:"noncompliant-code-2",level:4},{value:"Compliant Code:",id:"compliant-code-2",level:4}];function c(e){const n={a:"a",code:"code",h3:"h3",h4:"h4",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,l.a)(),...e.components};return(0,r.jsxs)(r.Fragment,{children:[(0,r.jsx)(n.p,{children:"Rules in this rule set report issues related to libraries API exposure."}),"\n",(0,r.jsx)(n.p,{children:(0,r.jsxs)(n.strong,{children:["Note: The ",(0,r.jsx)(n.code,{children:"libraries"})," rule set is not included in the detekt-cli or Gradle plugin."]})}),"\n",(0,r.jsxs)(n.p,{children:["To enable this rule set, add ",(0,r.jsx)(n.code,{children:'detektPlugins "io.gitlab.arturbosch.detekt:detekt-rules-libraries:$version"'}),"\nto your Gradle ",(0,r.jsx)(n.code,{children:"dependencies"})," or reference the ",(0,r.jsx)(n.code,{children:"detekt-rules-libraries"}),"-jar with the ",(0,r.jsx)(n.code,{children:"--plugins"})," option\nin the command line interface."]}),"\n",(0,r.jsx)(n.h3,{id:"forbiddenpublicdataclass",children:"ForbiddenPublicDataClass"}),"\n",(0,r.jsx)(n.p,{children:"Data classes are bad for binary compatibility in public APIs. Avoid using them."}),"\n",(0,r.jsx)(n.p,{children:"This rule is aimed at library maintainers. If you are developing a final application you can ignore this issue."}),"\n",(0,r.jsxs)(n.p,{children:["More info: ",(0,r.jsx)(n.a,{href:"https://jakewharton.com/public-api-challenges-in-kotlin/",children:"Public API challenges in Kotlin"})]}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Active by default"}),": Yes - Since v1.16.0"]}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Debt"}),": 20min"]}),"\n",(0,r.jsx)(n.h4,{id:"configuration-options",children:"Configuration options:"}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.code,{children:"ignorePackages"})," (default: ",(0,r.jsx)(n.code,{children:"['*.internal', '*.internal.*']"}),")"]}),"\n",(0,r.jsx)(n.p,{children:"ignores classes in the specified packages."}),"\n"]}),"\n"]}),"\n",(0,r.jsx)(n.h4,{id:"noncompliant-code",children:"Noncompliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"data class C(val a: String) // violation: public data class\n"})}),"\n",(0,r.jsx)(n.h4,{id:"compliant-code",children:"Compliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"internal data class C(val a: String)\n"})}),"\n",(0,r.jsx)(n.h3,{id:"librarycodemustspecifyreturntype",children:"LibraryCodeMustSpecifyReturnType"}),"\n",(0,r.jsx)(n.p,{children:"Functions/properties exposed as public APIs of a library should have an explicit return type.\nInferred return type can easily be changed by mistake which may lead to breaking changes."}),"\n",(0,r.jsxs)(n.p,{children:["See also: ",(0,r.jsx)(n.a,{href:"https://kotlinlang.org/docs/whatsnew14.html#explicit-api-mode-for-library-authors",children:"Kotlin 1.4 Explicit API"})]}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Active by default"}),": Yes - Since v1.2.0"]}),"\n",(0,r.jsx)(n.p,{children:(0,r.jsx)(n.strong,{children:"Requires Type Resolution"})}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Debt"}),": 5min"]}),"\n",(0,r.jsx)(n.h4,{id:"configuration-options-1",children:"Configuration options:"}),"\n",(0,r.jsxs)(n.ul,{children:["\n",(0,r.jsxs)(n.li,{children:["\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.code,{children:"allowOmitUnit"})," (default: ",(0,r.jsx)(n.code,{children:"false"}),")"]}),"\n",(0,r.jsxs)(n.p,{children:["if functions with ",(0,r.jsx)(n.code,{children:"Unit"})," return type should be allowed without return type declaration"]}),"\n"]}),"\n"]}),"\n",(0,r.jsx)(n.h4,{id:"noncompliant-code-1",children:"Noncompliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'// code from a library\nval strs = listOf("foo, bar")\nfun bar() = 5\nclass Parser {\n    fun parse() = ...\n}\n'})}),"\n",(0,r.jsx)(n.h4,{id:"compliant-code-1",children:"Compliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:'// code from a library\nval strs: List<String> = listOf("foo, bar")\nfun bar(): Int = 5\n\nclass Parser {\n    fun parse(): ParsingResult = ...\n}\n'})}),"\n",(0,r.jsx)(n.h3,{id:"libraryentitiesshouldnotbepublic",children:"LibraryEntitiesShouldNotBePublic"}),"\n",(0,r.jsx)(n.p,{children:"Library typealias and classes should be internal or private."}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Active by default"}),": Yes - Since v1.16.0"]}),"\n",(0,r.jsxs)(n.p,{children:[(0,r.jsx)(n.strong,{children:"Debt"}),": 5min"]}),"\n",(0,r.jsx)(n.h4,{id:"noncompliant-code-2",children:"Noncompliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"// code from a library\nclass A\n"})}),"\n",(0,r.jsx)(n.h4,{id:"compliant-code-2",children:"Compliant Code:"}),"\n",(0,r.jsx)(n.pre,{children:(0,r.jsx)(n.code,{className:"language-kotlin",children:"// code from a library\ninternal class A\n"})})]})}function u(e={}){const{wrapper:n}={...(0,l.a)(),...e.components};return n?(0,r.jsx)(n,{...e,children:(0,r.jsx)(c,{...e})}):c(e)}},11151:(e,n,i)=>{i.d(n,{Z:()=>o,a:()=>t});var r=i(67294);const l={},s=r.createContext(l);function t(e){const n=r.useContext(s);return r.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function o(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(l):e.components||l:t(e.components),r.createElement(s.Provider,{value:n},e.children)}}}]);