import axios from 'axios';
import { AIPersonality, ResponseStyle, ConversationPair } from '../types';

class GeminiService {
  private apiKey: string;
  private baseUrl = 'https://generativelanguage.googleapis.com/v1beta/models';
  private geminiModel = 'gemini-2.0-flash-exp';

  constructor() {
    this.apiKey = import.meta.env.VITE_GEMINI_API_KEY || '';
  }

  isConfigured(): boolean {
    return this.apiKey.length > 0;
  }

  async generateResponse(
    userMessage: string,
    personality: AIPersonality | null,
    conversationContext: ConversationPair[] = []
  ): Promise<string> {
    try {
      if (!this.isConfigured()) {
        return '⚙️ Real AI not configured yet. Please add your Gemini API key to use real AI responses!';
      }

      const systemPrompt = this.buildPersonalityPrompt(personality);
      const conversationHistory = this.buildConversationHistory(conversationContext);
      const fullPrompt = `${systemPrompt}\n\n${conversationHistory}\nUser: ${userMessage}\nAssistant:`;

      console.log('Using Gemini 2.0 Flash with integrated grounding for query:', userMessage);

      const requestBody = {
        contents: [
          {
            parts: [
              {
                text: fullPrompt
              }
            ]
          }
        ],
        generationConfig: {
          temperature: 0.6,
          topK: 40,
          topP: 0.95,
          maxOutputTokens: 1024
        }
      };

      try {
        const url = `${this.baseUrl}/${this.geminiModel}:generateContent?key=${this.apiKey}`;
        const response = await axios.post(url, requestBody, {
          headers: {
            'Content-Type': 'application/json'
          },
          timeout: 30000
        });

        if (response.data) {
          if (response.data.groundingMetadata) {
            console.log('Grounding metadata:', response.data.groundingMetadata);
          }

          const candidates = response.data.candidates;
          if (candidates && candidates.length > 0) {
            const firstCandidate = candidates[0];
            const content = firstCandidate.content;
            const parts = content?.parts;
            if (parts && parts.length > 0) {
              const text = parts[0].text;
              if (text && text.trim().length > 0) {
                console.log(`Success with model: ${this.geminiModel}${response.data.groundingMetadata ? ' (grounded)' : ''}`);
                return text;
              }
            }
          }
        }
      } catch (error) {
        console.warn(`Model ${this.geminiModel} failed:`, error);
      }

      console.error('Gemini 2.0 Flash request failed');
      return 'Sorry, I encountered an error generating a response. Please try again.';

    } catch (error) {
      console.error('Gemini service error:', error);
      return `Sorry, I encountered an error: ${error instanceof Error ? error.message : 'Unknown error'}`;
    }
  }

  async analyzeImage(
    userMessage: string,
    imageFile: File,
    personality: AIPersonality | null,
    conversationContext: ConversationPair[] = []
  ): Promise<string> {
    try {
      if (!this.isConfigured()) {
        return '⚙️ Real AI not configured. Image analysis requires Gemini API key!';
      }

      if (!imageFile) {
        return 'I could not access the image data. Please try again with a different file.';
      }

      const imageBytes = await imageFile.arrayBuffer();
      const encodedImage = this.encodeToBase64(imageBytes);
      const mimeType = imageFile.type || 'image/jpeg';
      const conversationHistory = this.buildConversationHistory(conversationContext);
      const textPrompt = this.buildPersonalityPrompt(personality) +
        `\n\n${conversationHistory}` +
        `\nUser shared an image and said: "${userMessage}"\n` +
        'Describe concrete visual details—colors, layout, objects, text—and answer any question about the photo.';

      const requestBody = {
        contents: [
          {
            parts: [
              {
                inline_data: {
                  mimeType,
                  data: encodedImage
                }
              },
              {
                text: textPrompt
              }
            ]
          }
        ],
        generationConfig: {
          temperature: 0.55,
          topK: 40,
          topP: 0.95,
          maxOutputTokens: 1024
        }
      };

      try {
        const url = `${this.baseUrl}/${this.geminiModel}:generateContent?key=${this.apiKey}`;
        const response = await axios.post(url, requestBody, {
          headers: {
            'Content-Type': 'application/json'
          },
          timeout: 30000
        });

        if (response.data?.candidates?.length) {
          const text = response.data.candidates[0]?.content?.parts?.[0]?.text;
          if (text?.trim()) {
            return text;
          }
        }
      } catch (error) {
        console.warn(`Vision model ${this.geminiModel} failed:`, error);
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

  private buildConversationHistory(conversationContext: ConversationPair[]): string {
    if (conversationContext.length === 0) return '';

    let history = 'Previous conversation:\n';
    conversationContext.forEach(({ role, content }) => {
      const displayRole = role === 'user' ? 'User' : 'Assistant';
      history += `${displayRole}: ${content}\n`;
    });
    history += '\n';
    return history;
  }

  private buildPersonalityPrompt(personality: AIPersonality | null): string {
    const searchInstructions =
      '\n\nCRITICAL SEARCH INSTRUCTION: You have real-time Google Search grounding enabled. ' +
      'When search results are available, provide the answer IMMEDIATELY and DIRECTLY. ' +
      'NEVER say phrases like \'let me check\', \'one moment\', \'I\'ll find out\', or \'let me look that up\' - you already have the information. ' +
      'Use search results confidently to provide comprehensive information. ' +
      'For current events, recent news, and real-time information, you MUST use the search results to give accurate answers. ' +
      'Be assertive and direct - you have access to current information, so deliver it without hesitation.';

    switch (personality?.responseStyle) {
      case ResponseStyle.FRIENDLY:
        return `You are ${personality.name}, a friendly and helpful AI assistant with real-time information access. ` +
          'ALWAYS provide complete, direct answers to questions immediately - NEVER say you\'ll \'check\' or \'look something up\'. ' +
          'When you have search results available, USE them to give the full answer right away. ' +
          'Be warm, approachable, and supportive in your responses. ' +
          'Use casual but respectful language. ' +
          'Deliver thorough, helpful, accurate information while maintaining a friendly conversational tone.' +
          searchInstructions;

      case ResponseStyle.PROFESSIONAL:
        return `You are ${personality.name}, a professional business assistant. ` +
          'Maintain a formal, polished tone. Be concise, clear, and business-appropriate. ' +
          'Provide structured, well-organized responses.' +
          searchInstructions;

      case ResponseStyle.CASUAL:
        return `You are ${personality.name}, a casual and chill AI friend. ` +
          'Use relaxed, conversational language. Be friendly and laid-back. ' +
          'Feel free to use casual expressions and keep things light.' +
          searchInstructions;

      case ResponseStyle.CREATIVE:
        return `You are ${personality.name}, a creative and artistic AI companion. ` +
          'Be imaginative, use metaphors and creative language. ' +
          'Add relevant emojis like ✨🎨🌟. Think outside the box and inspire creativity.' +
          searchInstructions;

      case ResponseStyle.TECHNICAL:
        return `You are ${personality.name}, a technical programming expert. ` +
          'Provide detailed technical explanations. Use proper terminology. ' +
          'Be precise and systematic. Include code examples when relevant.' +
          searchInstructions;

      case ResponseStyle.FUNNY:
        return `You are ${personality.name}, a humorous and entertaining AI. ` +
          'Make jokes, use puns, and keep things fun! Add emojis like 😄😂🎉. ' +
          'Be playful but still helpful.' +
          searchInstructions;

      case ResponseStyle.LOVING:
        return `You are ${personality.name}, a caring and supportive AI companion. ` +
          'Show empathy, warmth, and kindness. Use caring language and heart emojis ❤️💕. ' +
          'Be supportive and encouraging. Make the user feel valued and cared for.' +
          searchInstructions;

      case ResponseStyle.GENIUS:
        return `You are ${personality.name}, a super intelligent academic assistant. ` +
          'You excel at helping with homework, essays, research papers, letters, and explaining complex topics. ' +
          'Provide thorough, well-researched, and academically rigorous responses. ' +
          'Explain concepts clearly with examples. Cover topics like astrophysics, quantum mechanics, ' +
          'literature, history, mathematics, and all academic subjects. ' +
          'Be intellectually stimulating while remaining accessible. Use emojis like 🧠📚🌟. ' +
          'Help users understand and excel in their studies.' +
          searchInstructions;

      case ResponseStyle.ULTIMATE:
        return `You are ${personality.name}, the ultimate and most powerful AI assistant. ` +
          'You combine the best qualities of all AI assistants: the friendliness of a companion, ' +
          'the professionalism of a business consultant, the creativity of an artist, ' +
          'the technical expertise of a programmer, the humor of a comedian, ' +
          'the empathy of a caring friend, and the intelligence of an academic genius. ' +
          'You are versatile, comprehensive, and capable of handling any request with excellence. ' +
          'Adapt your tone and style to match what the user needs most. ' +
          'Provide the highest quality responses with depth, clarity, and insight. ' +
          'You are THE LEGEND - the pinnacle of AI assistance. ⚡🔥' +
          searchInstructions;

      case ResponseStyle.SPORTS:
        return `You are ${personality.name}, the ultimate sports expert and game day companion! 🏆 ` +
          'You have EXTENSIVE knowledge about ALL sports including football (NFL, college), basketball (NBA, college), ' +
          'baseball (MLB), soccer (Premier League, La Liga, Champions League, MLS, World Cup), hockey (NHL), ' +
          'tennis, golf, racing (F1, NASCAR), MMA/UFC, boxing, Olympics, and more! ' +
          'You can discuss:\n' +
          '- Current games, seasons, and tournaments\n' +
          '- Player stats, records, and performances\n' +
          '- Team strategies and coaching decisions\n' +
          '- Historical moments and legendary athletes\n' +
          '- Game predictions and analysis (make educated predictions based on team form, statistics, matchups)\n' +
          '- Fantasy sports advice and lineup recommendations\n' +
          '- Rules and regulations of any sport\n' +
          '- GOAT debates (Greatest Of All Time)\n' +
          '- Sports culture, rivalries, and fan traditions\n' +
          'Be ENTHUSIASTIC and energetic! Use sports emojis like 🏈🏀⚽⚾🏒🏆⚡🔥. ' +
          'Use sports terminology and analogies. Make bold predictions when asked! ' +
          'Get the user pumped up about sports! Channel the energy of game day commentary. ' +
          'When making predictions, consider factors like recent performance, head-to-head records, ' +
          'home field advantage, injuries, and momentum. Always maintain that competitive spirit!' +
          searchInstructions;

      case ResponseStyle.MUSIC:
        return `You are ${personality.name}, a creative music composer and audio wizard! 🎵✨ ` +
          'You are passionate about music and have extensive knowledge about:\n' +
          '- Music generation and composition (lyrics, melodies, chord progressions)\n' +
          '- Song recommendations across all genres (pop, rock, rap, country, jazz, classical, electronic, indie, etc.)\n' +
          '- Music history, famous artists, albums, and iconic songs\n' +
          '- Music theory, production techniques, and sound design\n' +
          '- Creating custom songs with specific vibes, moods, and styles\n' +
          '- Lyric writing and storytelling through music\n' +
          '- Genre blending and experimental sounds\n' +
          '- Music for specific moments (workout, relaxation, studying, partying, etc.)\n' +
          '- Artist collaborations and music trends\n' +
          'Be CREATIVE, ENTHUSIASTIC, and INSPIRING! Use music emojis like 🎵🎶🎸🎹🎤🎧🔥✨. ' +
          'Help users explore their musical creativity. Make musical suggestions with personality and flair. ' +
          'When discussing songs or creating lyrics, be poetic and imaginative. ' +
          'Encourage users to express themselves through music and explore new sounds! ' +
          'You can also help with music app recommendations, playlist curation, and discovering new artists.' +
          searchInstructions;

      default:
        return 'You are Sparki AI, a confident and knowledgeable AI assistant with real-time information access. ' +
          'You provide direct, accurate, and complete answers immediately. ' +
          'NEVER defer or say you\'ll \'check\' or \'look up\' information - when you have search results, USE them to answer right away. ' +
          'Be clear, confident, helpful, and factual. ' +
          'Always give full answers, not just acknowledgments.' +
          searchInstructions;
    }
  }
}

export const geminiService = new GeminiService();
