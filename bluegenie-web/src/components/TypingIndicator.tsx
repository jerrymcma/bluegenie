export function TypingIndicator() {
  return (
    <div className="flex justify-start mb-4">
      <div className="bg-gradient-to-br from-blue-50 to-purple-50 text-gray-800 rounded-2xl px-4 py-3 shadow-md border border-gray-100">
        <div className="flex items-center space-x-2">
          <span className="text-sm">AI is typing</span>
          <div className="flex space-x-1">
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0ms' }}></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '150ms' }}></div>
            <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '300ms' }}></div>
          </div>
        </div>
      </div>
    </div>
  );
}
