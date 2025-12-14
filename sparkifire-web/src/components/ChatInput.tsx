import { useState, useRef, KeyboardEvent } from 'react';
import { Send, Mic, MicOff, Image as ImageIcon, Camera, X, Plus } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { voiceService } from '../services/voiceService';
import { MessageType } from '../types';

interface ChatInputProps {
  onStartFresh: () => void;
}

export function ChatInput({ onStartFresh }: ChatInputProps) {
  const { sendMessage, isLoading, isListening, setIsListening } = useChatStore();
  const [messageText, setMessageText] = useState('');
  const [selectedImagePreview, setSelectedImagePreview] = useState<string | null>(null);
  const [selectedImageFile, setSelectedImageFile] = useState<File | null>(null);
  const [showImageOptions, setShowImageOptions] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const cameraInputRef = useRef<HTMLInputElement>(null);

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

  return (
    <div className="bg-white border-t border-gray-200 p-4 shadow-lg">
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
          value={messageText}
          onChange={(e) => setMessageText(e.target.value)}
          onKeyPress={handleKeyPress}
          placeholder="Say hello, ask anything..."
          className="w-full px-4 py-3 border-2 border-gray-300 rounded-xl resize-none focus:outline-none focus:border-blue-500 transition-colors"
          rows={3}
          disabled={isLoading}
        />

        {/* Action Buttons */}
        <div className="flex items-center justify-between mt-3">
          <div className="flex items-center space-x-2">
            {/* Image Button */}
            <div className="relative">
              <button
                onClick={() => setShowImageOptions(!showImageOptions)}
                className="p-3 text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
                title="Add image"
              >
                <ImageIcon className="w-6 h-6" />
              </button>

              {/* Image Options Dropdown */}
              {showImageOptions && (
                <div className="absolute bottom-full left-0 mb-2 bg-white rounded-lg shadow-xl border border-gray-200 p-2 space-y-1 min-w-[150px]">
                  <button
                    onClick={handleCameraClick}
                    className="w-full flex items-center space-x-2 px-3 py-2 hover:bg-gray-100 rounded-lg transition-colors text-left"
                  >
                    <Camera className="w-5 h-5 text-gray-600" />
                    <span className="text-sm text-gray-700">Camera</span>
                  </button>
                  <button
                    onClick={handleGalleryClick}
                    className="w-full flex items-center space-x-2 px-3 py-2 hover:bg-gray-100 rounded-lg transition-colors text-left"
                  >
                    <ImageIcon className="w-5 h-5 text-gray-600" />
                    <span className="text-sm text-gray-700">Gallery</span>
                  </button>
                </div>
              )}
            </div>

            {/* Voice Button */}
            <button
              onClick={handleVoiceToggle}
              className={`p-3 rounded-full transition-colors ${
                isListening
                  ? 'bg-red-100 text-red-600 hover:bg-red-200'
                  : 'text-blue-600 hover:bg-blue-50'
              }`}
              title={isListening ? 'Stop listening' : 'Start voice input'}
            >
              {isListening ? <MicOff className="w-6 h-6" /> : <Mic className="w-6 h-6" />}
            </button>

            {/* Start Fresh Button */}
            <button
              onClick={onStartFresh}
              className="p-3 text-blue-600 hover:bg-blue-50 rounded-full transition-colors"
              title="Start fresh conversation"
            >
              <Plus className="w-6 h-6" />
            </button>
          </div>

          {/* Send Button */}
          <button
            onClick={handleSend}
            disabled={(!messageText.trim() && !selectedImagePreview) || isLoading}
            className="flex items-center space-x-2 bg-blue-600 text-white px-6 py-3 rounded-full hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors shadow-lg"
          >
            <Send className="w-5 h-5" />
            <span className="font-medium">Send</span>
          </button>
        </div>

        {/* Hidden File Inputs */}
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          onChange={handleImageSelect}
          className="hidden"
        />
        <input
          ref={cameraInputRef}
          type="file"
          accept="image/*"
          capture="environment"
          onChange={handleImageSelect}
          className="hidden"
        />
      </div>
    </div>
  );
}
