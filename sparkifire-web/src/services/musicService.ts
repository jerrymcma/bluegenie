import axios from 'axios';

interface PredictionResponse {
  id: string;
  status: 'starting' | 'processing' | 'succeeded' | 'failed' | 'canceled';
  output?: string | string[];
  error?: string;
}

class MusicService {
  private apiUrl: string;

  constructor() {
    // Use serverless function endpoint for production, direct API for development
    const isProduction = window.location.hostname !== 'localhost' && window.location.hostname !== '127.0.0.1';
    this.apiUrl = isProduction ? '/api/music' : '/api/music';
  }

  isConfigured(): boolean {
    // Always return true since API key is handled server-side
    return true;
  }

  async generateClip(prompt: string, authToken: string): Promise<string> {
    try {
      const { lyrics, style } = this.parsePrompt(prompt);

      const predictionId = await this.createPrediction(lyrics, style, authToken);
      if (!predictionId) {
        return 'I could not start the Replicate music job. Please try again in a moment.';
      }

      const outputUrl = await this.pollPrediction(predictionId, authToken);
      if (!outputUrl) {
        return 'The music job did not finish successfully. Please try a simpler prompt or try again soon.';
      }

      return `✨ Sparki composed your song! Download it here: ${outputUrl}`;
    } catch (error: any) {
      console.error('MusicService.generateClip error', error);
      
      // Provide more specific error messages
      if (error.message?.includes('not configured')) {
        return 'Music generation is not configured yet. Please add your Replicate API key in the Vercel environment variables.';
      }
      
      if (error.response?.status === 401) {
        return 'Authentication failed with Replicate. Please check your API key configuration.';
      }
      
      return 'Sorry, I could not reach Replicate right now. Please try again later.';
    }
  }

  private parsePrompt(prompt: string): { lyrics: string; style: string } {
    const parts = prompt.split('|');
    let lyrics = parts[0]?.trim() || '';
    let style = parts[1]?.trim() || '';

    if (parts.length === 1) {
      const text = parts[0].trim();
      const looksLikeLyrics = /\[verse|chorus|bridge\]/i.test(text) || text.split('\n').length > 3;
      if (looksLikeLyrics || text.length > 120) {
        lyrics = text;
        style = '';
      } else {
        lyrics = '';
        style = text;
      }
    }

    if (!lyrics) {
      lyrics = '[Verse]\nLa la la\n[Chorus]\nOh oh oh';
    }

    if (lyrics.length > 600) {
      lyrics = lyrics.slice(0, 600);
    }

    if (!style) {
      style = 'Pop, modern production, radio ready';
    }

    return { lyrics, style };
  }

  private async createPrediction(lyrics: string, style: string, authToken: string): Promise<string | null> {
    try {
      console.log('Creating prediction with API URL:', this.apiUrl);
      const response = await axios.post<PredictionResponse>(
        this.apiUrl,
        {
          action: 'create',
          lyrics,
          prompt: style
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
          },
          timeout: 120000
        }
      );

      console.log('Prediction created:', response.data);
      return response.data?.id ?? null;
    } catch (error: any) {
      console.error('createPrediction error:', error);
      console.error('Error response:', error.response?.data);
      console.error('Error status:', error.response?.status);
      throw error;
    }
  }

  private async pollPrediction(predictionId: string, authToken: string): Promise<string | null> {
    const maxAttempts = 60;
    for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
      await this.delay(2000);
      const result = await this.fetchPrediction(predictionId, authToken);

      if (!result) {
        continue;
      }

      if (result.status === 'succeeded') {
        const output = result.output;
        if (typeof output === 'string') {
          return output;
        }
        if (Array.isArray(output) && output.length > 0) {
          return output[0];
        }
        return null;
      }

      if (result.status === 'failed' || result.status === 'canceled') {
        console.error('Replicate prediction failed', result.error);
        return null;
      }
    }

    return null;
  }

  private async fetchPrediction(predictionId: string, authToken: string): Promise<PredictionResponse | null> {
    try {
      const response = await axios.post<PredictionResponse>(
        this.apiUrl,
        {
          action: 'check',
          predictionId
        },
        {
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${authToken}`
          },
          timeout: 60000
        }
      );

      return response.data;
    } catch (error) {
      console.error('fetchPrediction error', error);
      return null;
    }
  }

  private delay(ms: number): Promise<void> {
    return new Promise((resolve) => setTimeout(resolve, ms));
  }
}

export const musicService = new MusicService();
