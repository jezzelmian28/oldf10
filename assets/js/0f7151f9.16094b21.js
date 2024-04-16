"use strict";(self.webpackChunk_detekt_website=self.webpackChunk_detekt_website||[]).push([[6302],{2634:(e,t,n)=>{n.r(t),n.d(t,{assets:()=>a,contentTitle:()=>o,default:()=>c,frontMatter:()=>s,metadata:()=>r,toc:()=>d});var i=n(5893),l=n(1151);const s={title:"Run detekt using the Compiler Plugin",keywords:["detekt","static","analysis","code","kotlin"],sidebar:null,permalink:"compilerplugin.html",folder:"gettingstarted",summary:null,sidebar_position:7},o=void 0,r={id:"gettingstarted/compilerplugin",title:"Run detekt using the Compiler Plugin",description:"You can integrate detekt in your project using the Detekt Compiler Plugin instead of the classic Detekt Gradle Plugin. Detekt offers a compiler plugin for K1 which allows you to run detekt as part of the Kotlin compilation process using type resolution. This allows you to run detekt on your code without having separate tasks to invoke and results in much faster execution of detekt, especially if you use type resolution with the Gradle plugin.",source:"@site/docs/gettingstarted/compilerplugin.mdx",sourceDirName:"gettingstarted",slug:"/gettingstarted/compilerplugin",permalink:"/docs/next/gettingstarted/compilerplugin",draft:!1,unlisted:!1,editUrl:"https://github.com/detekt/detekt/edit/main/website/docs/gettingstarted/compilerplugin.mdx",tags:[],version:"current",sidebarPosition:7,frontMatter:{title:"Run detekt using the Compiler Plugin",keywords:["detekt","static","analysis","code","kotlin"],sidebar:null,permalink:"compilerplugin.html",folder:"gettingstarted",summary:null,sidebar_position:7},sidebar:"defaultSidebar",previous:{title:"Run detekt using a Git pre-commit hook",permalink:"/docs/next/gettingstarted/git-pre-commit-hook"},next:{title:"Comments Rule Set",permalink:"/docs/next/rules/comments"}},a={},d=[{value:"Using Gradle",id:"using-gradle",level:2},{value:"Adding the Compiler Plugin",id:"adding-the-compiler-plugin",level:3},{value:"Configuring the Compiler Plugin",id:"configuring-the-compiler-plugin",level:3},{value:"Adding third party plugins",id:"adding-third-party-plugins",level:3},{value:"Running the Compiler Plugin",id:"running-the-compiler-plugin",level:3},{value:"Using CLI compiler",id:"using-cli-compiler",level:2},{value:"Known Issues",id:"known-issues",level:2}];function u(e){const t={a:"a",admonition:"admonition",code:"code",h2:"h2",h3:"h3",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",...(0,l.a)(),...e.components};return(0,i.jsxs)(i.Fragment,{children:[(0,i.jsxs)(t.p,{children:["You can integrate detekt in your project using the Detekt Compiler Plugin instead of the classic ",(0,i.jsx)(t.a,{href:"/docs/gettingstarted/gradle",children:"Detekt Gradle Plugin"}),". Detekt offers a compiler plugin for K1 which allows you to run detekt as part of the Kotlin compilation process using ",(0,i.jsx)(t.a,{href:"/docs/gettingstarted/type-resolution",children:"type resolution"}),". This allows you to run detekt on your code without having separate tasks to invoke and results in much faster execution of detekt, especially if you use type resolution with the Gradle plugin."]}),"\n",(0,i.jsx)(t.admonition,{type:"caution",children:(0,i.jsxs)(t.p,{children:["Please note that Detekt Compiler Plugin is an ",(0,i.jsx)(t.strong,{children:"experimental extension"})," of detekt. We expect it to be stable with the upcoming release of detekt (2.x)"]})}),"\n",(0,i.jsx)(t.h2,{id:"using-gradle",children:"Using Gradle"}),"\n",(0,i.jsx)(t.h3,{id:"adding-the-compiler-plugin",children:"Adding the Compiler Plugin"}),"\n",(0,i.jsx)(t.p,{children:"To use the detekt Compiler Plugin, you will have to add the following Gradle Plugin:"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'plugins {\n  id("io.github.detekt.gradle.compiler-plugin") version "1.23.3"\n}\n'})}),"\n",(0,i.jsx)(t.h3,{id:"configuring-the-compiler-plugin",children:"Configuring the Compiler Plugin"}),"\n",(0,i.jsxs)(t.p,{children:["The compiler plugin can be configured using the ",(0,i.jsx)(t.code,{children:"detekt {}"})," block in your gradle file."]}),"\n",(0,i.jsx)(t.p,{children:"The following options are allowed:"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'detekt {\n    // Define the detekt configuration(s) you want to use.\n    // Defaults to the default detekt configuration.\n    config.setFrom("path/to/config.yml")\n\n    // Applies the config files on top of detekt\'s default config file. `false` by default.\n    buildUponDefaultConfig = false\n\n    // Turns on all the rules. `false` by default.\n    allRules = false\n\n    // Specifying a baseline file. All findings stored in this file in subsequent runs of detekt.\n    baseline = file("path/to/baseline.xml")\n\n    // Disables all default detekt rulesets and will only run detekt with custom rules\n    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.\n    disableDefaultRuleSets = false\n\n    // Adds debug output during task execution. `false` by default.\n    debug = false\n\n    // Kill switch to turn off the Compiler Plugin execution entirely.\n    enableCompilerPlugin.set(true)\n}\n'})}),"\n",(0,i.jsx)(t.p,{children:"Moreover, detekt reports can be configured at the Kotlin Compilation tasks level as follows"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile).configureEach {\n    detekt {\n        reports {\n            xml.enabled.set(true)\n            txt.enabled.set(false)\n            create("custom") {\n                enabled.set(false)\n            }\n        }\n    }\n}\n'})}),"\n",(0,i.jsx)(t.h3,{id:"adding-third-party-plugins",children:"Adding third party plugins"}),"\n",(0,i.jsxs)(t.p,{children:["As for the Detekt Gradle Plugin, you can add third party plugins to the Compiler Plugin using the ",(0,i.jsx)(t.code,{children:"detektPlugins"})," configuration."]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-kotlin",children:'dependencies {\n    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.3")\n}\n'})}),"\n",(0,i.jsx)(t.h3,{id:"running-the-compiler-plugin",children:"Running the Compiler Plugin"}),"\n",(0,i.jsxs)(t.p,{children:["The compiler plugin will run during your ",(0,i.jsx)(t.code,{children:"compileKotlin"})," tasks execution:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-shell",children:"$ ./gradlew compileKotlin\n\n> Task :example:compileKotlin\nw: Analysis failed with 1 issues.\nw: file:///.../example/src/main/java/Sample.kt:4:17 MagicNumber: This expression contains a magic number. Consider defining it to a well named constant.\n\nBUILD SUCCESSFUL in 1s\n5 actionable tasks: 1 executed, 4 up-to-date\n"})}),"\n",(0,i.jsx)(t.h2,{id:"using-cli-compiler",children:"Using CLI compiler"}),"\n",(0,i.jsx)(t.p,{children:"You can also use the Compiler Plugin with the Kotlin command-line compiler."}),"\n",(0,i.jsxs)(t.p,{children:["You'll need to grab ",(0,i.jsx)(t.code,{children:"detekt-compiler-plugin-<version>-all.jar"})," from our ",(0,i.jsx)(t.a,{href:"https://github.com/detekt/detekt/releases",children:"GitHub releases page"})," or Maven Central ",(0,i.jsx)(t.code,{children:"io.github.detekt:detekt-compiler-plugin"})," artifact (note that you'll need the uber-jar file, the one with the ",(0,i.jsx)(t.code,{children:"all"})," classifier)."]}),"\n",(0,i.jsxs)(t.p,{children:["You can attach the plugin by providing the path to its JAR file using the ",(0,i.jsx)(t.code,{children:"-Xplugin"})," kotlinc option:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{className:"language-shell",children:"kotlinc Main.kt -Xplugin=./detekt-compiler-plugin-1.23.7-all.jar\n"})}),"\n",(0,i.jsxs)(t.p,{children:["It's possible to pass options to the plugin using the ",(0,i.jsx)(t.code,{children:"-P"})," kotlinc option:"]}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{children:'# The plugin option format is: "-P plugin:detekt-compiler-plugin:<key>=<value>".\n# Options can be repeated.\n\n-P plugin:detekt-compiler-plugin:config=path/to/config.yml\n-P plugin:detekt-compiler-plugin:debug=true\n'})}),"\n",(0,i.jsx)(t.p,{children:"The available options are:"}),"\n",(0,i.jsx)(t.pre,{children:(0,i.jsx)(t.code,{children:"config=<path|paths> # Comma separated paths to detekt config files.\nbaseline=<path> # Path to a detekt baseline file.\ndebug=<true|false> # Print debug messages.\nisEnabled=<true|false> # Should detekt run?\nuseDefaultConfig=<true|false> # Use the default detekt config as baseline.\nallRules=<true|false> # Turns on all the rules.\ndisableDefaultRuleSets=<true|false> # Disables all default detekt rulesets.\nparallel=<true|false> # Enables parallel compilation and analysis of source files.\nrootPath=<path> # Root path used to relativize paths when using exclude patterns.\nexcludes=<base64-encoded globs> # A base64-encoded list of the globs used to exclude paths from scanning.\nreport=<report-id:path> # Generates a report for given 'report-id' and stores it on given 'path'. Available 'report-id' values: 'txt', 'xml', 'html'.\n"})}),"\n",(0,i.jsx)(t.h2,{id:"known-issues",children:"Known Issues"}),"\n",(0,i.jsxs)(t.ol,{children:["\n",(0,i.jsxs)(t.li,{children:["The rule ",(0,i.jsx)(t.code,{children:"InvalidPackageDeclaration"})," is known to not be working well with the Compiler Plugin ",(0,i.jsx)(t.a,{href:"https://github.com/detekt/detekt/issues/5747",children:"#5747"}),"."]}),"\n"]})]})}function c(e={}){const{wrapper:t}={...(0,l.a)(),...e.components};return t?(0,i.jsx)(t,{...e,children:(0,i.jsx)(u,{...e})}):u(e)}},1151:(e,t,n)=>{n.d(t,{Z:()=>r,a:()=>o});var i=n(7294);const l={},s=i.createContext(l);function o(e){const t=i.useContext(s);return i.useMemo((function(){return"function"==typeof e?e(t):{...t,...e}}),[t,e])}function r(e){let t;return t=e.disableParentContext?"function"==typeof e.components?e.components(l):e.components||l:o(e.components),i.createElement(s.Provider,{value:t},e.children)}}}]);