import { X, Copy, Share2, Trash2, Bookmark } from 'lucide-react';
import { FavoriteSpark } from '../types';

interface FavoritesDialogProps {
  isOpen: boolean;
  onClose: () => void;
  favorites: FavoriteSpark[];
  onRemoveFavorite: (id: string) => void;
}

export function FavoritesDialog({
  isOpen,
  onClose,
  favorites,
  onRemoveFavorite,
}: FavoritesDialogProps) {
  if (!isOpen) return null;

  const handleCopy = (content: string) => {
    navigator.clipboard.writeText(content);
  };

  const handleShare = async (content: string) => {
    try {
      if (navigator.share) {
        await navigator.share({
          title: 'Blue Genie Favorite',
          text: content,
        });
      } else {
        await navigator.clipboard.writeText(content);
      }
    } catch (error) {
      console.error('Share failed:', error);
    }
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-2xl w-full max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
          <div className="flex items-center space-x-3">
            <div className="bg-amber-100 p-2 rounded-xl">
              <Bookmark className="w-6 h-6 text-amber-500 fill-current" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-gray-800">Favorites</h2>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 rounded-full hover:bg-gray-100 text-gray-400 hover:text-gray-600 transition-colors"
            aria-label="Close favorites"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 overflow-y-auto flex-1 bg-gray-50">
          {favorites.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-16 text-center">
              <div className="bg-gray-100 p-4 rounded-full mb-4">
                <Bookmark className="w-8 h-8 text-gray-400" />
              </div>
              <h3 className="text-xl font-bold text-gray-700 mb-2">No favorites yet!</h3>
              <p className="text-gray-500 text-sm max-w-xs">
                Tap the bookmark icon on any message to save it to your favorites collection.
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              <p className="text-sm text-gray-500 font-medium px-1">
                {favorites.length} saved item{favorites.length !== 1 ? 's' : ''}
              </p>
              {favorites.map((favorite) => (
                <div key={favorite.id} className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
                  <div className="flex justify-between items-start gap-4 mb-3">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-semibold text-blue-500 bg-blue-50 px-2 py-1 rounded-md">
                        {favorite.personalityName || 'Blue Genie'}
                      </span>
                      <span className="text-xs text-gray-400">
                        {formatTime(favorite.timestamp)}
                      </span>
                    </div>
                  </div>
                  
                  <p className="text-gray-700 whitespace-pre-wrap text-sm mb-4 line-clamp-6">
                    {favorite.content}
                  </p>

                  <div className="flex items-center justify-end gap-2 pt-3 border-t border-gray-50">
                    <button
                      onClick={() => handleCopy(favorite.content)}
                      className="p-2 hover:bg-gray-50 rounded-lg text-gray-500 hover:text-blue-500 transition-colors"
                      title="Copy text"
                    >
                      <Copy className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleShare(favorite.content)}
                      className="p-2 hover:bg-gray-50 rounded-lg text-gray-500 hover:text-green-500 transition-colors"
                      title="Share"
                    >
                      <Share2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => onRemoveFavorite(favorite.id)}
                      className="p-2 hover:bg-red-50 rounded-lg text-gray-400 hover:text-red-500 transition-colors"
                      title="Remove from favorites"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-100 bg-white rounded-b-2xl">
          <button
            onClick={onClose}
            className="w-full px-4 py-2.5 text-sm font-medium text-gray-600 bg-gray-100 rounded-xl hover:bg-gray-200 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
