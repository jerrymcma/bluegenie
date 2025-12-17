import { useEffect, useRef, useState } from 'react';
import { Volume2, Sparkles, Music4 } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { MessageBubble } from '../components/MessageBubble';
import { TypingIndicator } from '../components/TypingIndicator';
import { WelcomeMessage } from '../components/WelcomeMessage';
import { PersonalitySelector } from '../components/PersonalitySelector';
import { ChatInput } from '../components/ChatInput';
import { MusicGenerationDialog } from '../components/MusicGenerationDialog';

export function ChatScreen() {
  const { messages, isLoading, currentPersonality, isSpeaking, initialize } = useChatStore();
  const [showPersonalitySelector, setShowPersonalitySelector] = useState(false);
  const [showStartFreshDialog, setShowStartFreshDialog] = useState(false);
  const [showMusicDialog, setShowMusicDialog] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    initialize();
  }, [initialize]);

  useEffect(() => {
    // Auto-scroll to bottom when messages change
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleStartFresh = () => {
    setShowStartFreshDialog(true);
  };

  const confirmStartFresh = () => {
    useChatStore.getState().startFresh();
    setShowStartFreshDialog(false);
  };

  // Check if current personality is Sparki (default)
  const isSparki = currentPersonality.id === 'default';

  return (
    <div className="flex flex-col h-screen bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Header */}
      <header className="bg-white shadow-md border-b border-gray-200">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3 flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-gray-800">{currentPersonality.name}</h1>
            {isSpeaking && (
              <Volume2 className="w-5 h-5 text-blue-600 animate-pulse flex-shrink-0" />
            )}
          </div>
          <div className="flex items-center space-x-2 flex-shrink-0">
            {/* Show full button for Sparki, compact button for other personalities */}
            {isSparki ? (
              <button
                onClick={() => setShowPersonalitySelector(true)}
                className="flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-2 rounded-full hover:from-blue-600 hover:to-purple-600 transition-all shadow-lg hover:shadow-xl"
              >
                <span className="font-semibold text-sm">Personalities</span>
                <Sparkles className="w-4 h-4" />
              </button>
            ) : (
              <button
                onClick={() => setShowPersonalitySelector(true)}
                className="w-10 h-10 flex items-center justify-center bg-gradient-to-r from-blue-500 to-purple-500 text-white rounded-full hover:from-blue-600 hover:to-purple-600 transition-all shadow-lg hover:shadow-xl"
                title="Switch Personality"
              >
                <Sparkles className="w-5 h-5" />
              </button>
            )}

            <button
              onClick={() => setShowMusicDialog(true)}
              className="w-10 h-10 flex items-center justify-center bg-white border-2 border-blue-200 text-blue-600 rounded-full hover:bg-blue-50 hover:border-blue-300 transition-all shadow-md hover:shadow-lg"
              title="Generate Music"
            >
              <Music4 className="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto px-4 py-6">
        <div className="max-w-4xl mx-auto">
          {messages.length === 0 ? (
            <WelcomeMessage
              personalityName={currentPersonality.name}
              greeting={currentPersonality.greeting}
            />
          ) : (
            <>
              {messages.map((message) => (
                <MessageBubble key={message.id} message={message} />
              ))}
              {isLoading && <TypingIndicator />}
            </>
          )}
          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Input Area */}
      <ChatInput onStartFresh={handleStartFresh} />

      {/* Personality Selector Modal */}
      <PersonalitySelector
        isOpen={showPersonalitySelector}
        onClose={() => setShowPersonalitySelector(false)}
      />

      {/* Music Generation Dialog */}
      <MusicGenerationDialog
        isOpen={showMusicDialog}
        onClose={() => setShowMusicDialog(false)}
      />

      {/* Start Fresh Confirmation Dialog */}
      {showStartFreshDialog && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-3">Start Fresh</h3>
            <p className="text-gray-600 mb-6">
              Start over? AI will forget this chat and begin a new conversation.
            </p>
            <div className="flex space-x-3">
              <button
                onClick={() => setShowStartFreshDialog(false)}
                className="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium"
              >
                Cancel
              </button>
              <button
                onClick={confirmStartFresh}
                className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium"
              >
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
