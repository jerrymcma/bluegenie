import { X, PlayCircle, Share2, Trash2, MoreVertical } from 'lucide-react';
import { GeneratedMusic } from '../types';
import { useState } from 'react';

interface MusicLibraryDialogProps {
  isOpen: boolean;
  onClose: () => void;
  library: GeneratedMusic[];
  onPlayMusic: (music: GeneratedMusic) => void;
  onShareMusic: (music: GeneratedMusic) => void;
  onDeleteMusic: (id: string) => void;
}

export function MusicLibraryDialog({
  isOpen,
  onClose,
  library,
  onPlayMusic,
  onShareMusic,
  onDeleteMusic,
}: MusicLibraryDialogProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="bg-gray-800 rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-700">
          <div className="flex items-center space-x-3">
            <div className="text-3xl">🎵</div>
            <div>
              <h2 className="text-xl font-bold text-white">Music Library</h2>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full hover:bg-gray-700 text-gray-400"
            aria-label="Close music library"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 overflow-y-auto flex-1">
          {library.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <div className="text-6xl mb-4">🎼</div>
              <h3 className="text-xl font-bold text-white mb-2">No music yet!</h3>
              <p className="text-gray-400 text-sm">
                Generate your first track to start building your music library.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              <p className="text-sm text-gray-400 mb-4">
                {library.length} track{library.length !== 1 ? 's' : ''} in library
              </p>
              {library.map((music) => (
                <MusicTrackCard
                  key={music.id}
                  music={music}
                  onPlay={() => onPlayMusic(music)}
                  onShare={() => onShareMusic(music)}
                  onDelete={() => onDeleteMusic(music.id)}
                />
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-700">
          <button
            onClick={onClose}
            className="w-full px-4 py-2 text-sm font-medium text-white rounded-lg hover:bg-gray-700"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}

interface MusicTrackCardProps {
  music: GeneratedMusic;
  onPlay: () => void;
  onShare: () => void;
  onDelete: () => void;
}

function MusicTrackCard({ music, onPlay, onShare, onDelete }: MusicTrackCardProps) {
  const [showOptions, setShowOptions] = useState(false);

  const getShortPrompt = (prompt: string, maxLength: number = 40) => {
    return prompt.length > maxLength ? `${prompt.substring(0, maxLength)}...` : prompt;
  };

  const getFormattedDuration = (seconds: number) => {
    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  const getFormattedCost = () => {
    if (music.costCents === 0) {
      return 'FREE';
    }
    const dollars = music.costCents / 100;
    return `$${dollars.toFixed(2)}`;
  };

  return (
    <div className="bg-gray-700 rounded-xl p-3">
      <div className="flex items-start justify-between">
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-bold text-white mb-1 truncate">
            {getShortPrompt(music.prompt, 40)}
          </h4>
          <div className="flex items-center gap-2 text-xs">
            <span className="text-gray-400">⏱️ {getFormattedDuration(music.durationSeconds)}</span>
            <span className="text-gray-500">•</span>
            <span
              className={`font-bold ${
                music.isFreeTier ? 'text-green-400' : 'text-orange-400'
              }`}
            >
              {getFormattedCost()}
            </span>
          </div>
        </div>
        <button
          onClick={() => setShowOptions(!showOptions)}
          className="p-2 hover:bg-gray-600 rounded-lg text-white"
        >
          <MoreVertical className="w-5 h-5" />
        </button>
      </div>

      {showOptions ? (
        <div className="mt-3 pt-3 border-t border-gray-600">
          <div className="flex justify-around">
            <button
              onClick={onPlay}
              className="flex flex-col items-center gap-1 px-4 py-2 hover:bg-gray-600 rounded-lg"
            >
              <PlayCircle className="w-5 h-5 text-green-400" />
              <span className="text-xs text-white">Play</span>
            </button>
            <button
              onClick={onShare}
              className="flex flex-col items-center gap-1 px-4 py-2 hover:bg-gray-600 rounded-lg"
            >
              <Share2 className="w-5 h-5 text-blue-400" />
              <span className="text-xs text-white">Share</span>
            </button>
            <button
              onClick={onDelete}
              className="flex flex-col items-center gap-1 px-4 py-2 hover:bg-gray-600 rounded-lg"
            >
              <Trash2 className="w-4 h-4 text-gray-400" />
              <span className="text-xs text-gray-400">Delete</span>
            </button>
          </div>
        </div>
      ) : (
        <button
          onClick={onPlay}
          className="w-full mt-3 px-4 py-2 bg-pink-600 hover:bg-pink-700 text-white rounded-lg font-medium text-sm flex items-center justify-center gap-2"
        >
          <PlayCircle className="w-4 h-4" />
          Play Track
        </button>
      )}
    </div>
  );
}
