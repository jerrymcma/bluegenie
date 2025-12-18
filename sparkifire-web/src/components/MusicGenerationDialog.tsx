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
      setMusicStatus('Please provide lyrics, a music description, or both.');
      return;
    }

    if (sanitizedStyle && (sanitizedStyle.length < 10 || sanitizedStyle.length > 300)) {
      setMusicStatus('Music Description must be between 10 and 300 characters.');
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
  
  const renderMusicStatus = () => {
    if (!musicStatus) return null;

    const downloadPrefix = 'Download it here: ';
    const downloadIndex = musicStatus.indexOf(downloadPrefix);

    if (downloadIndex !== -1) {
      const textPart = musicStatus.substring(0, downloadIndex);
      const urlPart = musicStatus.substring(downloadIndex + downloadPrefix.length);
      return (
        <span>
          {textPart}
          <a
            href={urlPart}
            target="_blank"
            rel="noopener noreferrer"
            className="text-blue-600 hover:underline font-semibold"
          >
            Download it here
          </a>
          .
        </span>
      );
    }
    
    return musicStatus;
  }

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
              <h2 className="text-lg font-semibold text-gray-900">Music Generator</h2>
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
            <label className="block text-sm font-semibold text-gray-800">Lyrics</label>
            <textarea
              value={lyrics}
              onChange={(e) => setLyrics(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={5}
              placeholder="Type or paste your lyrics here. Leave blank for instrumental only"
              maxLength={1000}
            />
            <p className="text-xs text-gray-500">10 to 600 characters is used.</p>
          </div>

          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-800">Music Description</label>
            <textarea
              value={stylePrompt}
              onChange={(e) => setStylePrompt(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={3}
              placeholder="Genre, style, tempo, mood, instrumentals..."
              maxLength={300}
            />
            <div className="flex items-center justify-between text-xs text-gray-500">
              <span>10-300 characters when used.</span>
              <span>{stylePrompt.length}/300</span>
            </div>
          </div>

          {musicStatus && (
            <div className="text-xs text-gray-600 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
              {renderMusicStatus()}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
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
