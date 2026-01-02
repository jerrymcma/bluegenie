export function MagicNotesIcon({ className = 'w-6 h-6' }: { className?: string }) {
  return (
    <img
      src="/icons/magic-notes.svg"
      alt="Magic Music Spark"
      className={className}
      loading="lazy"
      draggable={false}
    />
  );
}