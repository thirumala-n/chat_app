import { useState, useCallback } from 'react'
import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import { Check, Copy } from 'lucide-react'

/**
 * ChatGPT / Claude style separate code box with top header bar and Copy Code button.
 */
function CodeBlock({ language, code }) {
  const [copied, setCopied] = useState(false)

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(code).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }, [code])

  return (
    <div className="code-block-wrapper">
      <div className="code-block-header">
        <span className="code-block-lang">{language || 'code'}</span>
        <button
          onClick={handleCopy}
          className={`code-copy-btn ${copied ? 'copied' : ''}`}
          aria-label="Copy code to clipboard"
          title="Copy code"
        >
          {copied ? <Check size={14} className="copy-icon check" /> : <Copy size={14} className="copy-icon" />}
          <span>{copied ? 'Copied!' : 'Copy code'}</span>
        </button>
      </div>
      <div className="code-block-content">
        <pre className="code-block-pre">
          <code>{code}</code>
        </pre>
      </div>
    </div>
  )
}

/**
 * Renders Markdown content with proper styling and code block support.
 */
export default function MarkdownRenderer({ content }) {
  if (!content) return null

  return (
    <div className="markdown">
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          pre({ children }) {
            return <div className="code-block-container">{children}</div>
          },
          code({ node, className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || '')
            const text = String(children || '')
            const isBlock = Boolean(match) || text.includes('\n')

            if (!isBlock) {
              return (
                <code className="inline-code" {...props}>
                  {children}
                </code>
              )
            }

            const language = match ? match[1] : ''
            const cleanCode = text.replace(/\n$/, '')

            return <CodeBlock language={language} code={cleanCode} />
          },
          a({ href, children }) {
            return (
              <a href={href} target="_blank" rel="noopener noreferrer">
                {children}
              </a>
            )
          },
          table({ children }) {
            return (
              <div style={{ overflowX: 'auto', margin: '14px 0' }}>
                <table>{children}</table>
              </div>
            )
          },
        }}
      >
        {content}
      </ReactMarkdown>
    </div>
  )
}
