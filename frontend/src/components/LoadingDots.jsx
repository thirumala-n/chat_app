export default function LoadingDots({ className = '' }) {
  return (
    <div className={`loading-dots ${className}`} aria-label="Loading">
      <span />
      <span />
      <span />
    </div>
  )
}
