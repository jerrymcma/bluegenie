import axios from 'axios';

class MusicService {
  private apiKey: string;
  private baseUrl = 'https://generativelanguage.googleapis.com/v1beta/models';
  private musicModel = 'music-latest';

  constructor() {
    this.apiKey = import.meta.env.VITE_GEMINI_API_KEY || '';
  }

  isConfigured(): boolean {
    return this.apiKey.length > 0;
  }

  async generateClip(prompt: string): Promise<string> {
    if (!this.isConfigured()) {
      return 'Music generation is not configured yet. Please add your Gemini API key on the web project.';
    }

    try {
      const url = `${this.baseUrl}/${this.musicModel}:generateContent?key=${this.apiKey}`;

      const requestBody = {
        contents: [
          {
            parts: [{ text: prompt }]
          }
        ]
      };

      const response = await axios.post(url, requestBody, {
        headers: { 'Content-Type': 'application/json' },
        timeout: 60000
      });

      // For now we just confirm that the request succeeded and show a friendly status.
      if (response.data) {
        return 'Your music idea was sent to Sparkis composer. Web playback is coming soon; for now, try the full experience in the Android app.';
      }

      return 'I sent your idea to the composer, but did not get a usable response. Please try again.';
    } catch (error) {
      console.error('MusicService.generateClip error', error);
      return 'Sorry, I could not reach the music service right now. Please try again later.';
    }
  }
}

export const musicService = new MusicService();
