import { Volume2 } from 'lucide-react';
import { Message } from '../types';
import { voiceService } from '../services/voiceService';
import { useChatStore } from '../store/chatStore';

interface MessageBubbleProps {
  message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const { setIsSpeaking } = useChatStore();

  const handleSpeak = () => {
    voiceService.speak(message.content, () => {
      setIsSpeaking(false);
    });
    setIsSpeaking(true);
  };

  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  };

  if (message.isFromUser) {
    return (
      <div className="flex justify-end mb-4">
        <div className="max-w-[70%]">
          {message.imageUri && (
            <div className="mb-2">
              <img
                src={message.imageUri}
                alt="User uploaded"
                className="rounded-lg max-w-full h-auto"
              />
            </div>
          )}
          <div className="bg-blue-500 text-white rounded-2xl px-4 py-3 shadow-md">
            <p className="whitespace-pre-wrap break-words">{message.content}</p>
            <p className="text-xs text-blue-100 mt-1">{formatTime(message.timestamp)}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex justify-start mb-4">
      <div className="max-w-[70%]">
        <div className="bg-gradient-to-br from-blue-50 to-purple-50 text-gray-800 rounded-2xl px-4 py-3 shadow-md border border-gray-100">
          <p className="whitespace-pre-wrap break-words">{message.content}</p>
          <div className="flex items-center justify-between mt-2">
            <p className="text-xs text-gray-500">{formatTime(message.timestamp)}</p>
            <button
              onClick={handleSpeak}
              className="ml-2 p-1 hover:bg-white/50 rounded-full transition-colors"
              title="Read aloud"
            >
              <Volume2 className="w-4 h-4 text-blue-600" />
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
