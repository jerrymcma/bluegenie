import { useEffect, useRef, useState } from 'react';
import { Volume2, Sparkles, Music4, Library } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { MessageBubble } from '../components/MessageBubble';
import { TypingIndicator } from '../components/TypingIndicator';
import { WelcomeMessage } from '../components/WelcomeMessage';
import { PersonalitySelector } from '../components/PersonalitySelector';
import { ChatInput } from '../components/ChatInput';
import { MusicGenerationDialog } from '../components/MusicGenerationDialog';
import { MusicLibraryDialog } from '../components/MusicLibraryDialog';
import { GeneratedMusic } from '../types';

export function ChatScreen() {
  const {
    messages,
    isLoading,
    currentPersonality,
    isSpeaking,
    musicCredits,
    musicLibrary,
    deleteMusicFromLibrary,
    markMusicAsRead,
    changePersonality,
    initialize,
  } = useChatStore();
  const [showPersonalitySelector, setShowPersonalitySelector] = useState(false);
  const [showStartFreshDialog, setShowStartFreshDialog] = useState(false);
  const [showMusicDialog, setShowMusicDialog] = useState(false);
  const [showMusicLibrary, setShowMusicLibrary] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    initialize();
  }, [initialize]);

  useEffect(() => {
    if (messages.length > 0) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages]);

  const handleStartFresh = () => {
    setShowStartFreshDialog(true);
  };

  const confirmStartFresh = () => {
    useChatStore.getState().startFresh();
    setShowStartFreshDialog(false);
  };

  const isMusicPersonality = currentPersonality.id === 'music_composer';
  const isSparki = currentPersonality.id === 'default';

  const unreadCount = musicLibrary.filter((m) => !m.isRead).length;
  const hasUnreadMusic = unreadCount > 0;

  const handleMusicButtonClick = () => {
    if (isSparki) {
      // Switch to Magic Music Sparki personality
      import('../data/personalities').then(({ personalities }) => {
        const musicSparki = personalities.MUSIC;
        if (musicSparki) {
          changePersonality(musicSparki);
        }
      });
    } else {
      // Already in Music personality, open the generator dialog
      setShowMusicDialog(true);
    }
  };

  const handlePlayMusic = (music: GeneratedMusic) => {
    markMusicAsRead(music.id);
    window.open(music.url, '_blank');
  };

  const handleShareMusic = async (music: GeneratedMusic) => {
    markMusicAsRead(music.id);
    try {
      if (navigator.share) {
        await navigator.share({
          title: 'My SparkiFire Music',
          text: `Check out this music I generated with SparkiFire: ${music.prompt.substring(0, 100)}`,
          url: music.url,
        });
      } else {
        await navigator.clipboard.writeText(music.url);
        alert('Music link copied to clipboard!');
      }
    } catch (error) {
      console.error('Error sharing:', error);
      try {
        await navigator.clipboard.writeText(music.url);
        alert('Music link copied to clipboard!');
      } catch (clipboardError) {
        alert('Unable to share. Please try again.');
      }
    }
  };

  const handleDeleteMusic = (id: string) => {
    if (confirm('Are you sure you want to delete this track from your library?')) {
      deleteMusicFromLibrary(id);
    }
  };

  return (
    <div className="flex flex-col h-full bg-gradient-to-br from-blue-50 via-white to-purple-50">
      {/* Header */}
      <header className="flex-shrink-0 bg-white shadow-md border-b border-gray-200 z-10">
        <div className="max-w-4xl mx-auto px-4 py-4 flex items-center justify-between">
          <div className="flex items-center space-x-3 flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-blue-600">{currentPersonality.name}</h1>
            {isSpeaking && (
              <Volume2 className="w-5 h-5 text-blue-600 animate-pulse flex-shrink-0" />
            )}
          </div>
          <div className="flex items-center space-x-2 flex-shrink-0">
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
              onClick={handleMusicButtonClick}
              className="w-10 h-10 flex items-center justify-center bg-white border-2 border-blue-200 text-blue-600 rounded-full hover:bg-blue-50 hover:border-blue-300 transition-all shadow-md hover:shadow-lg"
              title={isSparki ? "Go to Music Sparki" : "Generate Music"}
            >
              <Music4 className="w-5 h-5" />
            </button>
            {isMusicPersonality && (
              <button
                onClick={() => setShowMusicLibrary(true)}
                className={`relative w-10 h-10 flex items-center justify-center rounded-full transition-all shadow-md hover:shadow-lg ${
                  hasUnreadMusic
                    ? 'bg-purple-600 border-2 border-purple-400 text-white shadow-purple-400 shadow-lg animate-pulse'
                    : 'bg-white border-2 border-purple-200 text-purple-600 hover:bg-purple-50 hover:border-purple-300'
                }`}
                title="Music Library"
              >
                <Library className="w-5 h-5" />
                {unreadCount > 0 && (
                  <span className="absolute -top-1 -right-1 bg-white text-purple-600 text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center border-2 border-purple-600">
                    {unreadCount}
                  </span>
                )}
              </button>
            )}
          </div>
        </div>
      </header>

      {/* Messages Area */}
      <main className="flex-1 overflow-y-auto">
        <div className="max-w-4xl mx-auto px-4 py-6">
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
              <div ref={messagesEndRef} />
            </>
          )}
        </div>
      </main>

      {/* Music Generation UI (visible only for music personality) */}
      {isMusicPersonality && (
        <div className="flex-shrink-0 px-4 pb-2 text-center">
          <p className="text-sm text-gray-600 mb-2">
            {musicCredits} of 5 free songs remaining
          </p>
          <button
            onClick={() => setShowMusicDialog(true)}
            className="w-full max-w-sm mx-auto flex items-center justify-center space-x-2 bg-gradient-to-r from-pink-500 to-purple-500 text-white px-4 py-3 rounded-full hover:from-pink-600 hover:to-purple-600 transition-all shadow-lg hover:shadow-xl"
          >
            <Music4 className="w-5 h-5" />
            <span className="font-semibold text-base">Generate Music</span>
          </button>
        </div>
      )}

      {/* Input Area */}
      <footer className="flex-shrink-0">
        <ChatInput onStartFresh={handleStartFresh} />
      </footer>

      {/* Modals */}
      <PersonalitySelector
        isOpen={showPersonalitySelector}
        onClose={() => setShowPersonalitySelector(false)}
      />
      <MusicGenerationDialog
        isOpen={showMusicDialog}
        onClose={() => setShowMusicDialog(false)}
      />
      <MusicLibraryDialog
        isOpen={showMusicLibrary}
        onClose={() => setShowMusicLibrary(false)}
        library={musicLibrary}
        onPlayMusic={handlePlayMusic}
        onShareMusic={handleShareMusic}
        onDeleteMusic={handleDeleteMusic}
      />
      {showStartFreshDialog && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl shadow-2xl max-w-md w-full p-6">
            <h3 className="text-xl font-bold text-gray-800 mb-3">Start Fresh</h3>
            <p className="text-gray-600 mb-6">Start over? AI will forget this chat and begin a new conversation.</p>
            <div className="flex space-x-3">
              <button onClick={() => setShowStartFreshDialog(false)} className="flex-1 px-4 py-2 border-2 border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-medium">
                Cancel
              </button>
              <button onClick={confirmStartFresh} className="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium">
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
