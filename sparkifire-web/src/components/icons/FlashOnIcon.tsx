export function FlashOnIcon({ className = 'w-6 h-6', color = '#FFC107' }: { className?: string; color?: string }) {
  return (
    <svg
      viewBox="0 0 24 24"
      className={className}
      role="img"
      aria-label="Spark idea"
      fill={color}
    >
      <path d="M7 2v11h3v9l7-13h-4l4-7H7z" />
    </svg>
  );
}