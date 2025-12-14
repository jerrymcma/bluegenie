import { useState } from 'react';
import { X, Music4 } from 'lucide-react';
import { useChatStore } from '../store/chatStore';

interface MusicGenerationDialogProps {
  isOpen: boolean;
  onClose: () => void;
}

export function MusicGenerationDialog({ isOpen, onClose }: MusicGenerationDialogProps) {
  const { generateMusic, isGeneratingMusic, musicStatus, setMusicStatus } = useChatStore();
  const [lyrics, setLyrics] = useState('');
  const [stylePrompt, setStylePrompt] = useState('');

  if (!isOpen) return null;

  const handleGenerate = async () => {
    const sanitizedLyrics = lyrics.trim();
    const sanitizedStyle = stylePrompt.trim();
    if (!sanitizedLyrics && !sanitizedStyle) {
      setMusicStatus('Please provide lyrics, a style prompt, or both.');
      return;
    }

    try {
      const payload = sanitizedStyle ? `${sanitizedLyrics}|${sanitizedStyle}` : sanitizedLyrics;
      await generateMusic(payload);
    } catch (error) {
      console.error('Music generation error', error);
      setMusicStatus('Sorry, I could not generate music just now. Please try again.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-xl w-full max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white">
              <Music4 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Music Idea Generator</h2>
              <p className="text-xs text-gray-500">Sparki sketches short clips based on your settings.</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full hover:bg-gray-100 text-gray-500"
            aria-label="Close music dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 space-y-4 overflow-y-auto">
          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-800">Lyrics (optional)</label>
            <textarea
              value={lyrics}
              onChange={(e) => setLyrics(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={5}
              placeholder="Paste your lyrics here (max 600 characters). Leave blank to let Sparki write them."
              maxLength={1000}
            />
            <p className="text-xs text-gray-500">Replicate uses up to 600 characters of lyrics.</p>
          </div>

          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-800">Music Prompt / Style</label>
            <textarea
              value={stylePrompt}
              onChange={(e) => setStylePrompt(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={3}
              placeholder="Example: Jazz, romantic, dreamy. Include genre, mood, instruments."
              maxLength={300}
            />
          </div>

          {musicStatus && (
            <div className="text-xs text-gray-600 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
              {musicStatus}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          <div className="flex-1">
            <p className="text-xs text-gray-500">Music is generated in the cloud via Replicate's Minimax Music 1.5 model.</p>
          </div>
          <button
            onClick={handleGenerate}
            disabled={isGeneratingMusic}
            className="px-5 py-2 text-sm font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            {isGeneratingMusic && (
              <span className="w-4 h-4 border-2 border-white/60 border-t-transparent rounded-full animate-spin" />
            )}
            <span>{isGeneratingMusic ? 'Generating...' : 'Generate'}</span>
          </button>
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
