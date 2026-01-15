interface WelcomeMessageProps {
  personalityName: string;
  greeting: string;
}

export function WelcomeMessage({ greeting }: WelcomeMessageProps) {
  return (
    <div className="flex justify-center mb-4 px-4">
      <div className="max-w-2xl w-full bg-gradient-to-br from-blue-50 to-purple-50 rounded-2xl p-6 shadow-lg border border-gray-100">
        <h2 className="text-xl font-bold text-blue-600 mb-3">Welcome! ✨💜</h2>
        <p className="text-blue-600 mb-3 text-base">{greeting}</p>
        <p className="text-blue-600 mb-3 text-base">You can enjoy 10 Blue Genie AI models, generate songs with Magic Music ✨🎵, and smash that lightening bolt button to generate a Genius Genie idea ✨🔮!</p>
      </div>
    </div>
  );
}
