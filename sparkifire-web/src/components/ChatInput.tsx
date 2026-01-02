import { useState, useRef, KeyboardEvent } from 'react';
import { Send, Mic, MicOff, Image as ImageIcon, Camera, X } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { voiceService } from '../services/voiceService';
import { MessageType } from '../types';
import { FlashOnIcon } from './icons/FlashOnIcon';

interface ChatInputProps {
  onStartFresh: () => void;
  onShowFavorites: () => void;
}

export function ChatInput({ onStartFresh, onShowFavorites }: ChatInputProps) {
  const { sendMessage, isLoading, isListening, setIsListening } = useChatStore();
  const [messageText, setMessageText] = useState('');
  const [selectedImagePreview, setSelectedImagePreview] = useState<string | null>(null);
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [showImageOptions, setShowImageOptions] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);
  const messageInputRef = useRef<HTMLTextAreaElement>(null);

  const SPARK_IDEA_PROMPT =
    '✨ Hey Sparki, how about igniting an original Spark Idea! (Press send). ✨';

  const handleSend = () => {
    if ((messageText.trim() || selectedImagePreview) && !isLoading) {
      let type = MessageType.TEXT;
      if (selectedImagePreview && messageText.trim()) {
        type = MessageType.TEXT_WITH_IMAGE;
      } else if (selectedImagePreview) {
        type = MessageType.IMAGE;
      }

      sendMessage(
        messageText.trim() || '📷 Image shared',
        selectedImagePreview || undefined,
        selectedImageFile || undefined,
        type
      );
      setMessageText('');
      setSelectedImagePreview(null);
      setSelectedImageFile(null);
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const handleVoiceToggle = () => {
    if (!voiceService.isSupported()) {
      alert('Speech recognition is not supported in your browser. Please try Chrome or Edge.');
      return;
    }

    if (isListening) {
      voiceService.stopListening();
      setIsListening(false);
    } else {
      voiceService.startListening(
        (text) => {
          setMessageText(text);
        },
        setIsListening
      );
    }
  };

  const handleImageSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0] || null;
    if (file) {
      setSelectedImageFile(file);
      const reader = new FileReader();
      reader.onload = (event) => {
        setSelectedImagePreview(event.target?.result as string);
      };
      reader.readAsDataURL(file);
    } else {
      setSelectedImageFile(null);
      setSelectedImagePreview(null);
    }
    setShowImageOptions(false);
  };

  const handleGalleryClick = () => {
    fileInputRef.current?.click();
  };

  const handleCameraClick = () => {
    cameraInputRef.current?.click();
  };
  
  const handleSparkIdea = () => {
    setMessageText(SPARK_IDEA_PROMPT);

    // Focus the textarea so the user can immediately send or edit the prompt
    if (messageInputRef.current) {
      const input = messageInputRef.current;
      requestAnimationFrame(() => {
        input.focus();
        input.setSelectionRange(SPARK_IDEA_PROMPT.length, SPARK_IDEA_PROMPT.length);
      });
    }
  };
  
  const handleFavoritesShortcut = () => {
    setShowImageOptions(false);
    onShowFavorites();
  };

  return (
    <div
      className="bg-white/95 border-t border-gray-200 pt-3 px-3 sm:px-4 pb-12 shadow-lg"
    >
      <div className="max-w-4xl mx-auto">
        {/* Selected Image Preview */}
        {selectedImagePreview && (
          <div className="mb-3 flex items-center space-x-3 bg-blue-50 p-3 rounded-lg">
            <img
              src={selectedImagePreview}
              alt="Selected"
              className="w-16 h-16 object-cover rounded-lg"
            />
            <span className="flex-1 text-sm text-blue-600 font-medium">Image selected</span>
            <button
              onClick={() => {
                setSelectedImagePreview(null);
                setSelectedImageFile(null);
              }}
              className="p-1 hover:bg-red-100 rounded-full transition-colors"
            >
              <X className="w-5 h-5 text-red-500" />
            </button>
          </div>
        )}

        {/* Voice Listening Indicator */}
        {isListening && (
          <div className="mb-3 flex items-center justify-center space-x-2 bg-blue-50 p-2 rounded-lg">
            <Mic className="w-4 h-4 text-blue-600 animate-pulse" />
            <span className="text-sm text-blue-600 font-medium">Listening...</span>
          </div>
        )}

        {/* Text Input */}
        <textarea
          ref={messageInputRef}
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Say hello, ask anything..."
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-xl resize-none focus:outline-none focus:border-blue-500 transition-colors min-h-[54px] sm:min-h-[72px] text-blue-600"
          rows={2}
          disabled={isLoading}
        />

        {/* Action Buttons */}
        <div className="mt-3 flex justify-between items-center space-x-3 relative w-full">
                  <div className="relative flex-1">
                    <button
                      onClick={() => setShowImageOptions(!showImageOptions)}
                      className="w-full h-12 flex items-center justify-center bg-white border-2 border-blue-100 text-blue-600 rounded-2xl hover:border-blue-300 transition-colors shadow-md hover:shadow-xl"
                      title="Open Sparki tools"
                    >
                      <ImageIcon className="w-6 h-6" />
                    </button>
        // ... existing code ...
                  </div>

                  <button
                    onClick={handleVoiceToggle}
                    className={`flex-1 h-12 flex items-center justify-center rounded-2xl transition-colors shadow-md hover:shadow-lg ${
                      isListening ? 'bg-red-100 text-red-500' : 'text-blue-600 hover:bg-blue-50'
                    }`}
                    title={isListening ? 'Stop listening' : 'Start voice input'}
                  >
                    {isListening ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
                  </button>

                  <button
                    onClick={handleSparkIdea}
                    className="flex-1 h-12 flex items-center justify-center bg-blue-600 rounded-2xl shadow-lg hover:shadow-2xl transition-all"
                    title="Sparki Idea"
                  >
                    <FlashOnIcon color="#FFD54F" />
          </button>

          <button
            onClick={handleSend}
            disabled={(!messageText.trim() && !selectedImagePreview) || isLoading}
            className="flex items-center justify-center gap-2 bg-blue-600 text-white w-full h-12 rounded-full hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors shadow-lg"
          >
            <Send className="w-5 h-5" />
            <span className="font-medium text-base">Send</span>
          </button>
        </div>

        {/* Hidden File Inputs */}
        <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageSelect} className="hidden" />
        <input ref={cameraInputRef} type="file" accept="image/*" capture="environment" onChange={handleImageSelect} className="hidden" />
      </div>
    </div>
  );
}


