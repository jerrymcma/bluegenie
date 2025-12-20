import { X } from 'lucide-react';
import { AIPersonality } from '../types';
import { getAllPersonalities } from '../data/personalities';
import { useChatStore } from '../store/chatStore';

interface PersonalitySelectorProps {
  isOpen: boolean;
  onClose: () => void;
}

export function PersonalitySelector({ isOpen, onClose }: PersonalitySelectorProps) {
  const { currentPersonality, changePersonality } = useChatStore();
  const personalities = getAllPersonalities();

  if (!isOpen) return null;

  const handleSelectPersonality = (personality: AIPersonality) => {
    changePersonality(personality);
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-blue-600">Choose Your AI Personality ✨</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-full transition-colors"
          >
            <X className="w-6 h-6 text-blue-600" />
          </button>
        </div>

        {/* Personalities Grid */}
        <div className="overflow-y-auto p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {personalities.map((personality) => (
              <button
                key={personality.id}
                onClick={() => handleSelectPersonality(personality)}
                className={`text-left p-4 rounded-xl border-2 transition-all hover:shadow-lg ${
                  currentPersonality.id === personality.id
                    ? 'border-blue-500 bg-blue-50'
                    : 'border-gray-200 hover:border-blue-300'
                }`}
                style={{
                  borderLeftWidth: '6px',
                  borderLeftColor: currentPersonality.id === personality.id ? personality.color : 'transparent'
                }}
              >
                <div className="flex items-start space-x-3">
                  <div className="text-4xl">{personality.icon}</div>
                  <div className="flex-1">
                    <h3 className="font-bold text-lg text-blue-600 mb-1">
                      {personality.name}
                      {currentPersonality.id === personality.id && (
                        <span className="ml-2 text-xs bg-blue-500 text-white px-2 py-1 rounded-full">
                          Active
                        </span>
                      )}
                    </h3>
                    <p className="text-sm text-blue-600 mb-2">{personality.description}</p>
                    <p 
                      className="text-xs text-blue-600 italic line-clamp-2"
                      dangerouslySetInnerHTML={{ __html: `"${personality.greeting}"` }}
                    />
                  </div>
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-gray-200 bg-gray-50">
          <p className="text-sm text-blue-600 text-center">
            Each personality has its own conversation history and unique style!
          </p>
        </div>
      </div>
    </div>
  );
}
