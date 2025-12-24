const axios = require('axios');

const DEFAULT_MODEL_LIST =
  process.env.GEMINI_MODEL_LIST ||
  process.env.VITE_GEMINI_MODEL_LIST ||
  'gemini-3.0-flash,gemini-3.0-pro,gemini-2.5-flash,gemini-1.5-pro-latest,gemini-1.5-flash';

const getModelQueue = () =>
  DEFAULT_MODEL_LIST.split(',')
    .map((model) => model.trim())
    .filter(Boolean);

const getGeminiApiKey = () =>
  process.env.GEMINI_API_KEY ||
  process.env.VITE_GEMINI_API_KEY ||
  process.env.NEXT_PUBLIC_GEMINI_API_KEY ||
  null;

const setCors = (res) => {
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'Authorization, X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );
};

const RESPONSE_STYLE = {
  FRIENDLY: 'FRIENDLY',
  PROFESSIONAL: 'PROFESSIONAL',
  CASUAL: 'CASUAL',
  CREATIVE: 'CREATIVE',
  TECHNICAL: 'TECHNICAL',
  FUNNY: 'FUNNY',
  LOVING: 'LOVING',
  GENIUS: 'GENIUS',
  ULTIMATE: 'ULTIMATE',
  SPORTS: 'SPORTS',
  MUSIC: 'MUSIC',
};

const SEARCH_INSTRUCTIONS =
  '\n\nCRITICAL SEARCH INSTRUCTION: You have real-time Google Search grounding enabled. ' +
  "When search results are available, provide the answer IMMEDIATELY and DIRECTLY. " +
  "NEVER say phrases like 'let me check', 'one moment', 'I'll find out', or 'let me look that up' - you already have the information. " +
  'Use search results confidently to provide comprehensive information. ' +
  'For current events, recent news, and real-time information, you MUST use the search results to give accurate answers. ' +
  'Be assertive and direct - you have access to current information, so deliver it without hesitation.';

const buildPersonalityPrompt = (personality = null) => {
  if (!personality) {
    return (
      'You are Sparki AI, a confident and knowledgeable AI assistant with real-time information access. ' +
      "You provide direct, accurate, and complete answers immediately. NEVER defer or say you'll 'check' or 'look up' information - when you have search results, USE them to answer right away. " +
      'Be clear, confident, helpful, and factual. Always give full answers, not just acknowledgments.' +
      SEARCH_INSTRUCTIONS
    );
  }

  const { responseStyle, name } = personality;
  switch (responseStyle) {
    case RESPONSE_STYLE.FRIENDLY:
      return (
        `You are ${name}, a friendly and helpful AI assistant with real-time information access. ` +
        "ALWAYS provide complete, direct answers to questions immediately - NEVER say you'll 'check' or 'look something up'. " +
        'Be warm, approachable, and supportive while delivering accurate information.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.PROFESSIONAL:
      return (
        `You are ${name}, a professional business assistant. ` +
        'Maintain a formal, polished tone. Be concise, clear, and business-appropriate.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.CASUAL:
      return (
        `You are ${name}, a casual and chill AI friend. ` +
        'Use relaxed, conversational language. Be friendly and laid-back.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.CREATIVE:
      return (
        `You are ${name}, a creative and artistic AI companion. ` +
        'Be imaginative, use metaphors and creative language. Add relevant emojis like ✨🎨🌟.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.TECHNICAL:
      return (
        `You are ${name}, a technical programming expert. ` +
        'Provide detailed technical explanations with proper terminology and examples.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.FUNNY:
      return (
        `You are ${name}, a humorous and entertaining AI. ` +
        'Make jokes, use puns, and keep things fun while still helping the user.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.LOVING:
      return (
        `You are ${name}, a caring and supportive AI companion. ` +
        'Show empathy, warmth, and kindness. Use caring language and heart emojis ❤️💕.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.GENIUS:
      return (
        `You are ${name}, a super intelligent academic assistant. ` +
        'Provide thorough, well-researched, and academically rigorous responses across subjects.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.ULTIMATE:
      return (
        `You are ${name}, the ultimate and most powerful AI assistant. ` +
        'Combine friendliness, professionalism, creativity, technical expertise, humor, empathy, and intelligence.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.SPORTS:
      return (
        `You are ${name}, the ultimate sports expert and game day companion! 🏆 ` +
        'Discuss all sports with energy, using stats, predictions, and history.' +
        SEARCH_INSTRUCTIONS
      );
    case RESPONSE_STYLE.MUSIC:
      return (
        `You are ${name}, a creative music composer and audio wizard! 🎵✨ ` +
        'Help users explore music, lyrics, recommendations, and production tips.' +
        SEARCH_INSTRUCTIONS
      );
    default:
      return (
        `You are ${name || 'Sparki AI'}, a confident and knowledgeable AI assistant with real-time information access. ` +
        'Provide direct, accurate, and complete answers immediately.' +
        SEARCH_INSTRUCTIONS
      );
  }
};

const buildConversationHistory = (conversationContext = []) => {
  if (!conversationContext.length) {
    return '';
  }

  let history = 'Previous conversation:\n';
  conversationContext.forEach(({ role, content }) => {
    const displayRole = role === 'user' ? 'User' : 'Assistant';
    history += `${displayRole}: ${content}\n`;
  });
  history += '\n';
  return history;
};

const buildRequestBody = ({ type, fullPrompt, imageData, imageMimeType }) => {
  if (type === 'image') {
    return {
      contents: [
        {
          parts: [
            {
              inline_data: {
                mimeType: imageMimeType || 'image/jpeg',
                data: imageData,
              },
            },
            {
              text: fullPrompt,
            },
          ],
        },
      ],
      generationConfig: {
        temperature: 0.55,
        topK: 40,
        topP: 0.95,
        maxOutputTokens: 2048,
      },
      tools: [
        {
          googleSearch: {},
        },
      ],
    };
  }

  return {
    contents: [
      {
        parts: [
          {
            text: fullPrompt,
          },
        ],
      },
    ],
    generationConfig: {
      temperature: 0.6,
      topK: 40,
      topP: 0.95,
      maxOutputTokens: 2048,
    },
    tools: [
      {
        googleSearch: {},
      },
    ],
  };
};

const callGemini = async (modelName, requestBody, apiKey) => {
  const baseUrl = 'https://generativelanguage.googleapis.com/v1beta/models';
  const url = `${baseUrl}/${modelName}:generateContent?key=${apiKey}`;
  return axios.post(url, requestBody, {
    headers: {
      'Content-Type': 'application/json',
    },
    timeout: 30000,
  });
};

const extractTextResponse = (response) => {
  const candidates = response?.data?.candidates;
  if (!Array.isArray(candidates) || !candidates.length) {
    return null;
  }

  const parts = candidates[0]?.content?.parts;
  if (!Array.isArray(parts) || !parts.length) {
    return null;
  }

  const text = parts[0]?.text;
  return typeof text === 'string' && text.trim().length > 0 ? text.trim() : null;
};

module.exports = async (req, res) => {
  setCors(res);

  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  if (req.method !== 'POST') {
    res.status(405).json({ error: 'Method not allowed' });
    return;
  }

  const apiKey = getGeminiApiKey();
  if (!apiKey) {
    res.status(500).json({
      error: 'Gemini API key not configured on server',
      hint: 'Add GEMINI_API_KEY to your Vercel environment variables',
    });
    return;
  }

  try {
    const body = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : req.body || {};
    const {
      message,
      conversationContext = [],
      personality = null,
      type = 'text',
      imageData = null,
      imageMimeType = null,
    } = body;

    if (type === 'text' && (!message || typeof message !== 'string')) {
      res.status(400).json({ error: 'message is required for text requests' });
      return;
    }

    if (type === 'image') {
      if (!imageData || typeof imageData !== 'string') {
        res.status(400).json({ error: 'imageData (base64 string) is required for image analysis' });
        return;
      }
    }

    const systemPrompt = buildPersonalityPrompt(personality);
    const conversationHistory = buildConversationHistory(conversationContext);
    const textPrompt =
      type === 'image'
        ? `${systemPrompt}\n\n${conversationHistory}\nUser shared an image and said: "${message || ''}"\nDescribe concrete visual details—colors, layout, objects, text—and answer any question about the photo.`
        : `${systemPrompt}\n\n${conversationHistory}\nUser: ${message}\nAssistant:`;

    const requestBody = buildRequestBody({
      type,
      fullPrompt: textPrompt,
      imageData,
      imageMimeType,
    });

    const models = getModelQueue();
    if (!models.length) {
      throw new Error('No Gemini models configured to try');
    }

    let lastError = null;
    for (const modelName of models) {
      try {
        const response = await callGemini(modelName, requestBody, apiKey);
        const text = extractTextResponse(response);
        if (text) {
          return res.status(200).json({
            text,
            model: modelName,
            grounded: Boolean(response?.data?.groundingMetadata),
          });
        }
      } catch (error) {
        lastError = error;
        console.warn(`[api/gemini] Model ${modelName} failed`, error?.response?.data || error.message);
      }
    }

    const errorMessage =
      lastError?.response?.data?.error?.message ||
      lastError?.message ||
      'Gemini did not return a usable response';

    res.status(502).json({
      error: 'Gemini request failed',
      details: errorMessage,
    });
  } catch (error) {
    console.error('[api/gemini] Unexpected error', error);
    res.status(500).json({
      error: 'Internal Server Error',
      details: error?.message || error,
    });
  }
};