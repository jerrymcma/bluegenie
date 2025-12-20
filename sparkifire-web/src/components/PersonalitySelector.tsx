import { X, Lock } from 'lucide-react';
import { AIPersonality } from '../types';
import { getAllPersonalities } from '../data/personalities';
import { useChatStore } from '../store/chatStore';

interface PersonalitySelectorProps {
  isOpen: boolean;
  onClose: () => void;
}

const FREE_PERSONALITIES = ['default', 'music_composer'];

export function PersonalitySelector({ isOpen, onClose }: PersonalitySelectorProps) {
  const { currentPersonality, changePersonality, subscription, setShowUpgradeModal } = useChatStore();
  const personalities = getAllPersonalities();

  if (!isOpen) return null;

  const handleSelectPersonality = (personality: AIPersonality) => {
    const isLocked = !subscription.isPremium && !FREE_PERSONALITIES.includes(personality.id);
    
    if (isLocked) {
      setShowUpgradeModal(true);
      return;
    }
    
    changePersonality(personality);
    onClose();
  };

  const isPersonalityLocked = (personalityId: string) => {
    return !subscription.isPremium && !FREE_PERSONALITIES.includes(personalityId);
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
            {personalities.map((personality) => {
              const isLocked = isPersonalityLocked(personality.id);
              
              return (
                <button
                  key={personality.id}
                  onClick={() => handleSelectPersonality(personality)}
                  className={`text-left p-4 rounded-xl border-2 transition-all relative ${
                    isLocked
                      ? 'border-gray-300 bg-gray-50 opacity-75'
                      : currentPersonality.id === personality.id
                      ? 'border-blue-500 bg-blue-50 hover:shadow-lg'
                      : 'border-gray-200 hover:border-blue-300 hover:shadow-lg'
                  }`}
                  style={{
                    borderLeftWidth: '6px',
                    borderLeftColor: currentPersonality.id === personality.id ? personality.color : 'transparent'
                  }}
                >
                  {isLocked && (
                    <div className="absolute top-3 right-3 bg-gradient-to-r from-blue-600 to-purple-600 text-white w-6 h-6 rounded-full flex items-center justify-center">
                      <Lock className="w-3 h-3" />
                    </div>
                  )}
                  
                  <div className="flex items-start space-x-3">
                    <div className={`text-4xl ${isLocked ? 'opacity-50' : ''}`}>{personality.icon}</div>
                    <div className="flex-1">
                      <h3 className={`font-bold text-lg mb-1 ${isLocked ? 'text-gray-600' : 'text-blue-600'}`}>
                        {personality.name}
                        {currentPersonality.id === personality.id && (
                          <span className="ml-2 text-xs bg-blue-500 text-white px-2 py-1 rounded-full">
                            Active
                          </span>
                        )}
                      </h3>
                      <p className={`text-sm mb-2 ${isLocked ? 'text-gray-500' : 'text-blue-600'}`}>
                        {personality.description}
                      </p>
                      <p 
                        className={`text-xs italic line-clamp-2 ${isLocked ? 'text-gray-400' : 'text-blue-600'}`}
                        dangerouslySetInnerHTML={{ __html: `"${personality.greeting}"` }}
                      />
                    </div>
                  </div>
                </button>
              );
            })}
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
