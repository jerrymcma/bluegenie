import { useState, useRef, KeyboardEvent } from 'react';
import { Send, Mic, MicOff, Image as ImageIcon, Camera, X, Plus } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { voiceService } from '../services/voiceService';
import { MessageType } from '../types';
import { FlashOnIcon } from './icons/FlashOnIcon';

interface ChatInputProps {
  onStartFresh: () => void;
  onShowFavorites: () => void;
}

export function ChatInput({ onStartFresh, onShowFavorites }: ChatInputProps) {
  const { sendMessage, isLoading, isListening, setIsListening } from useChatStore();
  const [messageText, setMessageText] = useState('');
  const [selectedImagePreview, setSelectedImagePreview] = useState<string | null>(null);
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [showImageOptions, setShowImageOptions] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);
  const messageInputRef = useRef<HTMLTextAreaElement>(null);

  const SPARK_IDEA_PROMPT =
    "👋 Hey Blue Genie ✨🔮, how's about creating a Genius Genie ✨🔮 idea with your crystal ball  ✨🔮 ✨✨ (Press send)";

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
            <span className="flex-1 text-sm text-blue-500 font-medium">Image selected</span>
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
            <Mic className="w-4 h-4 text-blue-500 animate-pulse" />
            <span className="text-sm text-blue-500 font-medium">Listening...</span>
          </div>
        )}

        {/* Text Input */}
        <textarea
          ref={messageInputRef}
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Say hello, ask anything..."
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-xl resize-none focus:outline-none focus:border-blue-500 transition-colors min-h-[54px] sm:min-h-[72px] text-blue-500"
          rows={2}
          disabled={isLoading}
        />

        {/* Action Buttons */}
        <div className="flex items-center mt-3 gap-3">
            {/* Left-side icon buttons */}
            <div className="flex items-center gap-3">
                {/* Image/Tools Button with Popover */}
                <div className="relative">
                    <button
                        onClick={() => setShowImageOptions(!showImageOptions)}
                        className="w-10 h-10 flex items-center justify-center bg-blue-500 text-white rounded-full hover:bg-blue-600 transition-colors shadow-md hover:shadow-lg"
                        title="Open Blue Genie tools"
                    >
                        <Plus className="w-5 h-5 text-yellow-300" strokeWidth={3} />
                    </button>
                    {showImageOptions && (
                        <div className="absolute bottom-full left-0 mb-3 bg-white rounded-2xl shadow-2xl border border-blue-500 p-4 space-y-4 min-w-[220px]">
                          <div>
                            <p className="text-[11px] font-semibold text-blue-500 uppercase tracking-wider mb-2">
                              Add to Chat
                            </p>
                            <div className="grid grid-cols-2 gap-2">
                              <button onClick={() => { handleCameraClick(); setShowImageOptions(false); }} className="flex flex-col items-center justify-center border border-gray-200 rounded-xl py-3 hover:border-blue-500 hover:shadow-lg transition-all">
                                <Camera className="w-5 h-5 text-blue-500 mb-1" />
                                <span className="text-xs text-gray-700 font-medium">Camera</span>
                              </button>
                              <button onClick={() => { handleGalleryClick(); setShowImageOptions(false); }} className="flex flex-col items-center justify-center border border-gray-200 rounded-xl py-3 hover:border-blue-500 hover:shadow-lg transition-all">
                                <ImageIcon className="w-5 h-5 text-blue-500 mb-1" />
                                <span className="text-xs text-gray-700 font-medium">Gallery</span>
                              </button>
                              <button onClick={() => { setShowImageOptions(false); onStartFresh(); }} className="flex flex-col items-center justify-center border border-gray-200 rounded-xl py-3 hover:border-blue-500 hover:shadow-lg transition-all">
                                <X className="w-5 h-5 text-blue-500 mb-1" />
                                <span className="text-xs text-gray-700 font-medium">Start Fresh</span>
                              </button>
                              <button onClick={handleFavoritesShortcut} className="flex flex-col items-center justify-center border border-gray-200 rounded-xl py-3 hover:border-blue-500 hover:shadow-lg transition-all">
                                <FlashOnIcon className="w-5 h-5" color="#FFB300" />
                                <span className="text-xs text-gray-700 font-medium">Favorite Genie Rubs</span>
                              </button>
                            </div>
                          </div>
                          <div className="border-t border-gray-100 pt-3">
                            <p className="text-[11px] font-semibold text-purple-500 uppercase tracking-wider mb-2">
                              Library
                            </p>
                            <button onClick={handleFavoritesShortcut} className="w-full flex items-center justify-between px-3 py-2 border border-purple-200 rounded-xl text-sm text-purple-600 font-semibold hover:border-purple-400 hover:shadow-lg transition-all">
                              <span>Favorite Genie Rubs</span>
                              <span className="text-lg">✨</span>
                            </button>
                          </div>
                        </div>
                    )}
                </div>

                {/* Voice Button */}
                <button onClick={handleVoiceToggle} className={`w-10 h-10 flex items-center justify-center rounded-full transition-colors shadow-md hover:shadow-lg ${isListening ? 'bg-red-500 text-white hover:bg-red-600' : 'bg-blue-500 text-white hover:bg-blue-600'}`} title={isListening ? 'Stop listening' : 'Start voice input'}>
                    {isListening ? <MicOff className="w-5 h-5" /> : <Mic className="w-5 h-5" />}
                </button>

                {/* Spark Idea Button */}
                <button
                    onClick={handleSparkIdea}
                    className="w-12 h-12 flex items-center justify-center bg-blue-500 rounded-2xl shadow-lg hover:shadow-2xl hover:bg-blue-600 transition-all"
                    title="Blue Genie Idea"
                >
                    <FlashOnIcon className="w-6 h-6" color="#FFD54F" />
                </button>
            </div>

            {/* Send Button */}
            <button
                onClick={handleSend}
                disabled={(!messageText.trim() && !selectedImagePreview) || isLoading}
                className="flex-1 flex items-center justify-center gap-2 bg-blue-500 text-white px-5 py-3 rounded-xl hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors shadow-lg"
            >
                <Send className="w-5 h-5" />
                <span className="font-semibold">Send</span>
            </button>
        </div>

        {/* Hidden File Inputs */}
        <input ref={fileInputRef} type="file" accept="image/*" onChange={handleImageSelect} className="hidden" />
        <input ref={cameraInputRef} type="file" accept="image/*" capture="environment" onChange={handleImageSelect} className="hidden" />
      </div>
    </div>
  );
}
