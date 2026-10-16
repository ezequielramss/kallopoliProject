import type { ReactNode } from "react"

function Button({
  children,
  variant = "primary",
  onClick,
  className = "",
  type = "button",
}: {
  children: ReactNode
  variant?: "primary" | "secondary" | "ghost"
  onClick?: () => void
  className?: string
  type?: "button" | "submit"
}) {
  return (
    <button
      type={type}
      className={`button button-${variant} ${className}`}
      onClick={onClick}
    >
      {children}
    </button>
  )
}

export { Button }
