import { X, Crown, Music4, Sparkles, Check } from 'lucide-react';
import { PAYMENT_LINK_URL } from '../config/stripe';

interface PremiumUpgradeModalProps {
  isOpen: boolean;
  onClose: () => void;
  onUpgrade: () => void;
  isRenewal?: boolean;
}

const handleUpgradeClick = () => {
  // Redirect to Stripe payment link
  window.location.href = PAYMENT_LINK_URL;
};

export function PremiumUpgradeModal({ 
  isOpen, 
  onClose, 
  onUpgrade,
  isRenewal = false 
}: PremiumUpgradeModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4">
      <div className="bg-gradient-to-br from-white to-blue-50 rounded-3xl shadow-2xl max-w-md w-full overflow-hidden">
        {/* Header with gradient */}
        <div className="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-8 text-center relative">
          <button
            onClick={onClose}
            className="absolute top-4 right-4 p-1 rounded-full hover:bg-white/20 text-white"
            aria-label="Close"
          >
            <X className="w-5 h-5" />
          </button>
          
          <div className="flex justify-center mb-3">
            <div className="w-16 h-16 bg-white/20 rounded-full flex items-center justify-center backdrop-blur-sm">
              <Crown className="w-9 h-9 text-yellow-300" />
            </div>
          </div>
          
          <h2 className="text-2xl font-bold text-white mb-2">
            {isRenewal ? '🎉 Renew Premium' : '✨ Upgrade to Premium'}
          </h2>
        </div>

        {/* Content */}
        <div className="px-6 py-6">
          <div className="text-center mb-6">
            <div className="text-4xl font-bold text-blue-600 mb-1">$5</div>
            <div className="text-sm text-gray-600">per month</div>
          </div>

          {/* Features */}
          <div className="space-y-4 mb-6">
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-blue-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Sparkles className="w-4 h-4 text-blue-600" />
              </div>
              <div className="flex-1">
                <p className="text-blue-600 font-semibold leading-tight">Unlock All 11 Personalities</p>
                <p className="text-sm text-blue-600">Including Sparki Ultimate!</p>
              </div>
              <Check className="w-5 h-5 text-green-500 flex-shrink-0" />
            </div>

            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-purple-100 rounded-full flex items-center justify-center flex-shrink-0">
                <Music4 className="w-4 h-4 text-purple-600" />
              </div>
              <div className="flex-1">
                <p className="text-blue-600 font-semibold">50 Songs per Month</p>
                <p className="text-sm text-blue-600">Sparki-generated music!</p>
              </div>
              <Check className="w-5 h-5 text-green-500 flex-shrink-0" />
            </div>

            <div className="flex items-center gap-3">
              <div className="w-8 h-8 bg-green-100 rounded-full flex items-center justify-center flex-shrink-0">
                <span className="text-lg">💬</span>
              </div>
              <div className="flex-1">
                <p className="text-blue-600 font-semibold">Unlimited Messages</p>
                <p className="text-sm text-blue-600">Chat as much as you want!</p>
              </div>
              <Check className="w-5 h-5 text-green-500 flex-shrink-0" />
            </div>
          </div>

          {/* CTA Button */}
          <button
            onClick={handleUpgradeClick}
            className="w-full bg-gradient-to-r from-blue-600 to-purple-600 text-white px-6 py-4 rounded-full hover:from-blue-700 hover:to-purple-700 transition-all shadow-lg hover:shadow-xl font-bold text-lg flex items-center justify-center gap-2"
          >
            <Crown className="w-5 h-5" />
            <span>{isRenewal ? 'Renew for $5' : 'Upgrade for $5'}</span>
          </button>

          <p className="text-xs text-gray-500 text-center mt-4">
            Cancel anytime • No commitments
          </p>
        </div>
      </div>
    </div>
  );
}
