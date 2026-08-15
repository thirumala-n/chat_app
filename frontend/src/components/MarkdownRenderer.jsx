import { useState, useCallback } from 'react'
import ReactMarkdown from 'react-markdown'
import { Check, Copy } from 'lucide-react'

/**
 * Code block component with language label and copy button.
 */
function CodeBlock({ language, children }) {
  const [copied, setCopied] = useState(false)

  const handleCopy = useCallback(() => {
    const text = String(children).replace(/\n$/, '')
    navigator.clipboard.writeText(text).then(() => {
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    })
  }, [children])

  return (
    <div className="code-block-wrapper">
      <div className="code-block-header">
        <span className="code-block-lang">{language || 'code'}</span>
        <button onClick={handleCopy} className="code-copy-btn" aria-label="Copy code">
          {copied ? <Check size={12} /> : <Copy size={12} />}
          {copied ? 'Copied' : 'Copy'}
        </button>
      </div>
      <pre style={{ margin: 0, borderRadius: '0 0 10px 10px' }}>
        <code>{children}</code>
      </pre>
    </div>
  )
}

/**
 * Renders Markdown content with proper styling and code block support.
 */
export default function MarkdownRenderer({ content }) {
  return (
    <div className="markdown">
      <ReactMarkdown
        components={{
          code({ node, inline, className, children, ...props }) {
            const match = /language-(\w+)/.exec(className || '')
            const language = match ? match[1] : ''

            if (inline) {
              return <code className={className} {...props}>{children}</code>
            }

            return (
              <CodeBlock language={language}>
                {String(children).replace(/\n$/, '')}
              </CodeBlock>
            )
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
              <div style={{ overflowX: 'auto' }}>
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
