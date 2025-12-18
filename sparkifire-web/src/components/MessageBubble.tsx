import { useState, useRef, useEffect } from 'react';
import { Volume2, MoreVertical, Copy, Share2, Bookmark } from 'lucide-react';
import { Message } from '../types';
import { voiceService } from '../services/voiceService';
import { useChatStore } from '../store/chatStore';

interface MessageBubbleProps {
  message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const { setIsSpeaking } = useChatStore();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(event.target as Node)) {
        setIsMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const handleSpeak = () => {
    voiceService.speak(message.content, () => {
      setIsSpeaking(false);
    });
    setIsSpeaking(true);
  };

  const handleCopy = () => {
    navigator.clipboard.writeText(message.content);
    setIsMenuOpen(false);
  };

  const handleShare = () => {
    // TODO: Implement share functionality
    alert('Share button clicked!');
    setIsMenuOpen(false);
  };

  const handleFavorite = () => {
    // TODO: Implement favorite functionality
    alert('Favorite button clicked!');
    setIsMenuOpen(false);
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
          <p className="whitespace-pre-wrap break-words text-blue-600" dangerouslySetInnerHTML={{ __html: message.content }} />
          <div className="flex items-center justify-between mt-2">
            <p className="text-xs text-gray-500">{formatTime(message.timestamp)}</p>
            <div className="flex items-center space-x-1">
              <button
                onClick={handleSpeak}
                className="p-1 hover:bg-white/50 rounded-full transition-colors"
                title="Read aloud"
              >
                <Volume2 className="w-4 h-4 text-blue-600" />
              </button>
              <div className="relative" ref={menuRef}>
                <button
                  onClick={() => setIsMenuOpen(!isMenuOpen)}
                  className="p-1 hover:bg-white/50 rounded-full transition-colors"
                  title="More options"
                >
                  <MoreVertical className="w-4 h-4 text-blue-600" />
                </button>
                {isMenuOpen && (
                  <div className="absolute bottom-full right-0 mb-1 bg-white rounded-lg shadow-xl border border-gray-200 p-1 min-w-[150px] z-10">
                    <button
                      onClick={handleCopy}
                      className="w-full flex items-center space-x-2 px-3 py-2 hover:bg-gray-100 rounded-md transition-colors text-left"
                    >
                      <Copy className="w-4 h-4 text-gray-600" />
                      <span className="text-sm text-gray-700">Copy</span>
                    </button>
                    <button
                      onClick={handleShare}
                      className="w-full flex items-center space-x-2 px-3 py-2 hover:bg-gray-100 rounded-md transition-colors text-left"
                    >
                      <Share2 className="w-4 h-4 text-gray-600" />
                      <span className="text-sm text-gray-700">Share</span>
                    </button>
                    <button
                      onClick={handleFavorite}
                      className="w-full flex items-center space-x-2 px-3 py-2 hover:bg-gray-100 rounded-md transition-colors text-left"
                    >
                      <Bookmark className="w-4 h-4 text-gray-600" />
                      <span className="text-sm text-gray-700">Favorite Spark</span>
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
