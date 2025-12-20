import { X } from 'lucide-react';

interface SignInModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSignIn: () => void;
}

export function SignInModal({ isOpen, onClose, onSignIn }: SignInModalProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 px-4">
      <div className="bg-white rounded-3xl shadow-2xl max-w-md w-full p-8">
        <div className="flex justify-between items-start mb-6">
          <h2 className="text-2xl font-bold text-blue-600">You've Hit Your Free Limit! 🎉</h2>
          <button
            onClick={onClose}
            className="p-1 rounded-full hover:bg-gray-100 text-gray-500"
            aria-label="Close"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="text-center mb-6">
          <p className="text-blue-600 text-base mb-4">
            You've used your <strong>50 free messages</strong> or <strong>5 free songs</strong>!
          </p>
          
          <p className="text-blue-600 text-sm">
            Sign in to upgrade to <strong>Premium</strong> for just <strong>$5/month</strong>:
          </p>
        </div>

        <div className="space-y-3 mb-6 text-sm text-blue-600">
          <div className="flex items-center gap-2">
            <span className="text-green-500">✓</span>
            <span>Unlimited messages with all 11 personalities</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-green-500">✓</span>
            <span>50 AI-generated songs per month</span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-green-500">✓</span>
            <span>Access to Sparki Ultimate & all features</span>
          </div>
        </div>

        <button
          onClick={onSignIn}
          className="w-full bg-white border-2 border-gray-300 text-gray-700 px-6 py-3 rounded-full hover:bg-gray-50 transition-all shadow-md hover:shadow-lg font-semibold flex items-center justify-center gap-3"
        >
          <svg className="w-5 h-5" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
          </svg>
          <span>Sign in with Google</span>
        </button>

        <p className="text-xs text-gray-500 text-center mt-4">
          We'll save your progress and unlock Premium features
        </p>
      </div>
    </div>
  );
}
