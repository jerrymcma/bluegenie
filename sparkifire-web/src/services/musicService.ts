import axios from 'axios';

interface PredictionResponse {
  id: string;
  status: 'starting' | 'processing' | 'succeeded' | 'failed' | 'canceled';
  output?: string | string[];
  error?: string;
}

class MusicService {
  private apiKey: string;
  private baseUrl = 'https://api.replicate.com/v1';
  private modelPath = 'minimax/music-1.5';

  constructor() {
    this.apiKey = import.meta.env.VITE_REPLICATE_API_KEY || '';
  }

  isConfigured(): boolean {
    return this.apiKey.length > 0 && this.apiKey !== 'your-replicate-api-key-here';
  }

  async generateClip(prompt: string): Promise<string> {
    if (!this.isConfigured()) {
      return 'Music generation is not configured yet. Please add your Replicate API key in the Vercel project.';
    }

    try {
      const { lyrics, style } = this.parsePrompt(prompt);

      const predictionId = await this.createPrediction(lyrics, style);
      if (!predictionId) {
        return 'I could not start the Replicate music job. Please try again in a moment.';
      }

      const outputUrl = await this.pollPrediction(predictionId);
      if (!outputUrl) {
        return 'The music job did not finish successfully. Please try a simpler prompt or try again soon.';
      }

      return `✨ Sparki composed your song! Download it here: ${outputUrl}`;
    } catch (error) {
      console.error('MusicService.generateClip error', error);
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

  private async createPrediction(lyrics: string, style: string): Promise<string | null> {
    const payload = {
      input: {
        lyrics,
        prompt: style
      }
    };

    const response = await axios.post<PredictionResponse>(
      `${this.baseUrl}/models/${this.modelPath}/predictions`,
      payload,
      {
        headers: {
          Authorization: `Bearer ${this.apiKey}`,
          'Content-Type': 'application/json',
          Prefer: 'wait'
        },
        timeout: 120000
      }
    );

    return response.data?.id ?? null;
  }

  private async pollPrediction(predictionId: string): Promise<string | null> {
    const maxAttempts = 60;
    for (let attempt = 0; attempt < maxAttempts; attempt += 1) {
      await this.delay(2000);
      const result = await this.fetchPrediction(predictionId);

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

  private async fetchPrediction(predictionId: string): Promise<PredictionResponse | null> {
    try {
      const response = await axios.get<PredictionResponse>(`${this.baseUrl}/predictions/${predictionId}`, {
        headers: {
          Authorization: `Bearer ${this.apiKey}`
        },
        timeout: 60000
      });

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
