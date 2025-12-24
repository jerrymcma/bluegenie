import axios from 'axios';
import { AIPersonality, ConversationPair } from '../types';

interface GeminiApiResponse {
  text?: string;
  model?: string;
  grounded?: boolean;
  error?: string;
  details?: string;
}

class GeminiService {
  private apiEndpoint: string;

  constructor() {
    const hostname = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
    const isLocalhost = hostname === 'localhost' || hostname === '127.0.0.1';
    this.apiEndpoint = isLocalhost ? '/api/gemini' : '/api/gemini';
  }

  isConfigured(): boolean {
    // API key now lives on the serverless function
    return true;
  }

  async generateResponse(
    userMessage: string,
    personality: AIPersonality | null,
    conversationContext: ConversationPair[] = []
  ): Promise<string> {
    try {
      const response = await axios.post<GeminiApiResponse>(
        this.apiEndpoint,
        {
          type: 'text',
          message: userMessage,
          personality,
          conversationContext
        },
        {
          headers: {
            'Content-Type': 'application/json'
          },
          timeout: 35000
        }
      );

      const text = response.data?.text;
      if (text && text.trim().length > 0) {
        const modelName = response.data?.model ? ` ${response.data.model}` : '';
        const grounded = response.data?.grounded ? ' (grounded)' : '';
        console.log(`[GeminiService] Success via${modelName}${grounded}`);
        return text.trim();
      }

      throw new Error(response.data?.error || 'Gemini did not return a usable response');

    } catch (error) {
      console.error('Gemini service error (server proxy):', error);
      if (error instanceof Error) {
        return `Sorry, I encountered an error: ${error.message}`;
      }
      return 'Sorry, I encountered an unexpected error generating a response.';
    }
  }

  async analyzeImage(
    userMessage: string,
    imageFile: File,
    personality: AIPersonality | null,
    conversationContext: ConversationPair[] = []
  ): Promise<string> {
    try {
      if (!imageFile) {
        return 'I could not access the image data. Please try again with a different file.';
      }

      const imageBytes = await imageFile.arrayBuffer();
      const encodedImage = this.encodeToBase64(imageBytes);
      const mimeType = imageFile.type || 'image/jpeg';

      const response = await axios.post<GeminiApiResponse>(
        this.apiEndpoint,
        {
          type: 'image',
          message: userMessage,
          personality,
          conversationContext,
          imageData: encodedImage,
          imageMimeType: mimeType
        },
        {
          headers: {
            'Content-Type': 'application/json'
          },
          timeout: 45000
        }
      );

      const text = response.data?.text;
      if (text?.trim()) {
        const modelName = response.data?.model ? ` ${response.data.model}` : '';
        const grounded = response.data?.grounded ? ' (grounded)' : '';
        console.log(`[GeminiService] Vision success via${modelName}${grounded}`);
        return text.trim();
      }

      return 'Sorry, I could not analyze that image just now. Please try another image or resend it.';

    } catch (error) {
      console.error('Image analysis error:', error);
      return `Sorry, I had trouble analyzing the image: ${error instanceof Error ? error.message : 'Unknown error'}`;
    }
  }

  private encodeToBase64(buffer: ArrayBuffer): string {
    let binary = '';
    const bytes = new Uint8Array(buffer);
    const chunkSize = 0x8000;
    for (let i = 0; i < bytes.length; i += chunkSize) {
      binary += String.fromCharCode.apply(null, Array.from(bytes.slice(i, i + chunkSize)));
    }
    return btoa(binary);
  }
}

export const geminiService = new GeminiService();
