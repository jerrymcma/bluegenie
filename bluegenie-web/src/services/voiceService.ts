// Web Speech API service for voice input and output
export class VoiceService {
  private recognition: SpeechRecognition | null = null;
  private synthesis: SpeechSynthesis;
  private isListeningState = false;
  private onTextRecognized?: (text: string) => void;
  private onListeningChange?: (isListening: boolean) => void;

  constructor() {
    this.synthesis = window.speechSynthesis;
    
    // Check if browser supports speech recognition
    type SpeechRecognitionConstructor = typeof window.SpeechRecognition;
    const SpeechRecognitionClass =
      (window.SpeechRecognition || window.webkitSpeechRecognition) as SpeechRecognitionConstructor;
    
    if (SpeechRecognitionClass) {
      this.recognition = new SpeechRecognitionClass();
      this.recognition.continuous = false;
      this.recognition.interimResults = false;
      this.recognition.lang = 'en-US';

      const handleResult = (event: SpeechRecognitionEvent) => {
        const transcript = event.results[0]?.[0]?.transcript;
        if (transcript && this.onTextRecognized) {
          this.onTextRecognized(transcript);
        }
      };

      this.recognition.onresult = handleResult;

      this.recognition.onend = () => {
        this.isListeningState = false;
        this.onListeningChange?.(false);
      };

      this.recognition.onerror = (event: SpeechRecognitionErrorEvent) => {
        console.error('Speech recognition error:', event.error);
        this.isListeningState = false;
        this.onListeningChange?.(false);
      };
    }
  }

  isSupported(): boolean {
    return this.recognition !== null;
  }

  startListening(onTextRecognized: (text: string) => void, onListeningChange: (isListening: boolean) => void): void {
    if (!this.recognition) {
      console.warn('Speech recognition not supported in this browser');
      return;
    }

    this.onTextRecognized = onTextRecognized;
    this.onListeningChange = onListeningChange;

    try {
      this.recognition.start();
      this.isListeningState = true;
      this.onListeningChange?.(true);
    } catch (error) {
      console.error('Error starting speech recognition:', error);
    }
  }

  stopListening(): void {
    if (this.recognition && this.isListeningState) {
      this.recognition.stop();
      this.isListeningState = false;
      this.onListeningChange?.(false);
    }
  }

  speak(text: string, onEnd?: () => void): void {
    // Cancel any ongoing speech
    this.synthesis.cancel();

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.lang = 'en-US';
    utterance.rate = 1.0;
    utterance.pitch = 1.0;
    utterance.volume = 1.0;

    if (onEnd) {
      utterance.onend = onEnd;
    }

    this.synthesis.speak(utterance);
  }

  stopSpeaking(): void {
    this.synthesis.cancel();
  }

  isSpeaking(): boolean {
    return this.synthesis.speaking;
  }

  isListening(): boolean {
    return this.isListeningState;
  }

  destroy(): void {
    this.stopListening();
    this.stopSpeaking();
  }
}

export const voiceService = new VoiceService();
