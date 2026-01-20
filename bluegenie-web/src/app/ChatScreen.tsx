import { useEffect, useRef, useState } from 'react';
import { Volume2, Sparkles } from 'lucide-react';
import { useChatStore } from '../store/chatStore';
import { personalities } from '../data/personalities';
import { MessageBubble } from '../components/MessageBubble';
import { TypingIndicator } from '../components/TypingIndicator';
import { WelcomeMessage } from '../components/WelcomeMessage';
import { PersonalitySelector } from '../components/PersonalitySelector';
import { ChatInput } from '../components/ChatInput';
import { MusicGenerationDialog } from '../components/MusicGenerationDialog';
import { MusicLibraryDialog } from '../components/MusicLibraryDialog';

export function ChatScreen() {
  const {
    messages,
    isLoading,
    currentPersonality,
    isSpeaking,
    changePersonality,
    initialize,
    musicLibrary,
    loadMusicLibrary,
    deleteGeneratedMusic,
    markMusicAsRead,
  } = useChatStore();
  const [showPersonalitySelector, setShowPersonalitySelector] = useState(false);
  const [showStartFreshDialog, setShowStartFreshDialog] = useState(false);
  const [showMusicDialog, setShowMusicDialog] = useState(false);
  const [showMusicLibrary, setShowMusicLibrary] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const audioRef = useRef<HTMLAudioElement | null>(null);

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

  const handleMusicButtonClick = () => {
    if (currentPersonality.id !== 'music_composer') {
      const musicSparki = personalities.MUSIC;
      if (musicSparki) {
        changePersonality(musicSparki);
      }
    }
  };

  const handleOpenMusicLibrary = () => {
    loadMusicLibrary();
    setShowMusicLibrary(true);
  };

  const handlePlayMusic = (trackId: string, url: string) => {
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }
    const audio = new Audio(url);
    audioRef.current = audio;
    audio.play();
    markMusicAsRead(trackId);
  };

  const handleShareMusic = async (url: string, prompt: string) => {
    try {
      if (navigator.share) {
        await navigator.share({
          title: 'Magic Music',
          text: prompt,
          url,
        });
      } else {
        await navigator.clipboard.writeText(url);
      }
    } catch (error) {
      console.error('Share failed:', error);
    }
  };



  return (
    <div className="flex flex-col h-full bg-gradient-to-br from-blue-50 via-white to-purple-50">

      {/* Header */}
      <header className="flex-shrink-0 bg-white shadow-md border-b border-gray-200 z-10">
        <div className="max-w-4xl mx-auto px-4 py-3 flex items-center justify-between">
          <div className="flex items-center space-x-3 flex-1 min-w-0">
            <h1 className="text-2xl font-bold text-blue-500 flex items-center gap-2">
              <span className="truncate">{currentPersonality.name}</span>
              <span aria-hidden="true" className="text-xl leading-none">✨</span>
            </h1>
            {isSpeaking && (
              <Volume2 className="w-5 h-5 text-blue-500 animate-pulse flex-shrink-0" />
            )}
            <video
              autoPlay
              loop
              muted
              className="w-6 h-6 flex-shrink-0"
              src="/sparkles.mp4"
            />
          </div>
          <div className="flex items-center space-x-2 flex-shrink-0">
            <button
              onClick={() => setShowPersonalitySelector(true)}
              className="flex items-center space-x-2 bg-gradient-to-r from-blue-500 to-purple-500 text-white px-4 py-2 rounded-full hover:from-blue-600 hover:to-purple-600 transition-all shadow-lg hover:shadow-xl"
            >
              <span className="font-semibold text-sm">Models</span>
              <Sparkles className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Messages area */}
      <div className="flex-1 overflow-y-auto pb-4">
        <div className="max-w-4xl mx-auto px-4 pt-4 space-y-4">
          {messages.length === 0 ? (
            <WelcomeMessage
              greeting={currentPersonality.greeting}
            />
          ) : (
            messages.map((message) => (
              <MessageBubble
                key={message.id}
                message={message}
                onRequestStartFresh={handleStartFresh}
              />
            ))
          )}
          {isLoading && <TypingIndicator />}
          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* Input area */}
      <div className="flex-shrink-0 bg-white border-t border-gray-200 shadow-lg">
        <div className="max-w-4xl mx-auto">
          <ChatInput
            onShowFavorites={() => {}}
            onMusicClick={handleMusicButtonClick}
            onOpenMusicGenerator={() => setShowMusicDialog(true)}
            onOpenMusicLibrary={handleOpenMusicLibrary}
          />
        </div>
      </div>

      {/* Dialogs */}
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
        onPlayMusic={(music) => handlePlayMusic(music.id, music.url)}
        onStopMusic={() => {
          if (audioRef.current) {
            audioRef.current.pause();
            audioRef.current = null;
          }
        }}
        onShareMusic={(music) => handleShareMusic(music.url, music.prompt)}
        onDeleteMusic={(musicId) => deleteGeneratedMusic(musicId)}
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
              <button onClick={confirmStartFresh} className="flex-1 px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors font-medium">
                Confirm
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
