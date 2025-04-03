// import React from 'react';
// import Markdown from 'react-markdown';
// import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
// import { dracula } from 'react-syntax-highlighter/dist/esm/styles/prism';
// import remarkGfm from 'remark-gfm';

// // 自定义代码块组件
// // const CodeBlock = ({ children, className, ...props }) => {
// //   const language = className?.replace('language-', '') || 'text';
// //   return (
// //     <SyntaxHighlighter
// //       style={dracula}
// //       language={language}
// //       {...props}
// //     >
// //       {children}
// //     </SyntaxHighlighter>
// //   );
// // };

// // const DmsMarkdown = ({ content }) => {
// //   return (
// //     <Markdown

// //     >
// //       {content}
// //     </Markdown>
// //   );
// // };

// const DmsMarkdown: React.FC<{ content: string }> = (props) => {
//   const codeBlock = (children: string, className: string) => {
//     const language = className?.replace('language-', '') || 'text';
//     return <SyntaxHighlighter style={dracula}>{children}</SyntaxHighlighter>;
//   };

//   return (
//     <>
//       <Markdown remarkPlugins={[remarkGfm]} components={{ code: codeBlock }}>
//         {props.content}
//       </Markdown>
//     </>
//   );
// };

// export default DmsMarkdown;
