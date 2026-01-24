import axios from 'axios';
import { AIPersonality, ConversationPair, MessageType } from '../types';

interface GroqApiResponse {
  text?: string;
  model?: string;
  error?: string;
}

class GroqService {
  private apiEndpoint: string;

  constructor() {
    const hostname = typeof window !== 'undefined' ? window.location.hostname : 'localhost';
    const isLocalhost = hostname === 'localhost' || hostname === '127.0.0.1';
    this.apiEndpoint = isLocalhost ? '/api/groq' : '/api/groq';
  }

  isConfigured(): boolean {
    return true;
  }

  async generateResponse(
    userMessage: string,
    personality: AIPersonality | null,
    conversationContext: ConversationPair[] = [],
    imageBase64?: string,
    messageType: MessageType = MessageType.TEXT
  ): Promise<string> {
    try {
      const response = await axios.post<GroqApiResponse>(
        this.apiEndpoint,
        {
          type: messageType || MessageType.TEXT,
          message: userMessage,
          personality,
          conversationContext,
          imageBase64
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
        console.log(`[GroqService] Success via${modelName}`);
        return text.trim();
      }

      throw new Error(response.data?.error || 'Groq did not return a usable response');

    } catch (error) {
      console.error('Groq service error (server proxy):', error);
      
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 413) {
          return "The image file is too large to send. Please try a smaller image.";
        }
        if (error.response?.status === 504) {
          return "The request timed out. Please try again.";
        }
      }

      if (error instanceof Error) {
        return `Sorry, I encountered an error: ${error.message}`;
      }
      return 'Sorry, I encountered an unexpected error generating a response.';
    }
  }
}

export const groqService = new GroqService();
