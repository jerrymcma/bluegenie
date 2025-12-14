interface WelcomeMessageProps {
  personalityName: string;
  greeting: string;
}

export function WelcomeMessage({ greeting }: WelcomeMessageProps) {
  return (
    <div className="flex justify-center mb-4 px-4">
      <div className="max-w-2xl w-full bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-6 shadow-lg border border-gray-100">
        <h2 className="text-2xl font-bold text-gray-800 mb-3">Welcome! 🔥</h2>
        <p className="text-gray-700 mb-3 text-lg">{greeting}</p>
        <p className="text-sm text-gray-600">
          You can type, use voice input, or share images. Click "Personalities ✨" to change my personality!
        </p>
      </div>
    </div>
  );
}
