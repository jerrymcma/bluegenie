const axios = require('axios');

const getGroqApiKey = () =>
  process.env.GROQ_API_KEY ||
  process.env.VITE_GROQ_API_KEY;

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

const callGroq = async (systemPrompt, userMessage, conversationContext = [], apiKey) => {
  const url = 'https://api.groq.com/openai/v1/chat/completions';
  
  const messages = [
    {
      role: 'system',
      content: `${systemPrompt}\n\nCurrent date: ${new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}. Donald Trump is the current US President as of January 2025.`
    }
  ];
  
  conversationContext.slice(-10).forEach(({ role, content }) => {
    messages.push({
      role: role === 'user' ? 'user' : 'assistant',
      content
    });
  });
  
  messages.push({
    role: 'user',
    content: userMessage
  });

  return axios.post(url, {
    model: 'llama-3.3-70b-versatile',
    messages,
    temperature: 0.8,
    max_tokens: 2048
  }, {
    headers: {
      'Authorization': `Bearer ${apiKey}`,
      'Content-Type': 'application/json'
    },
    timeout: 30000
  });
};

const extractTextResponse = (response) => {
  const text = response?.data?.choices?.[0]?.message?.content;
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

  const apiKey = getGroqApiKey();
  if (!apiKey) {
    res.status(500).json({
      error: 'Groq API key not configured on server',
      hint: 'Add GROQ_API_KEY to your Vercel environment variables',
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
    } = body;

    if (!message || typeof message !== 'string') {
      res.status(400).json({ error: 'message is required' });
      return;
    }

    if (type === 'image') {
      return res.status(200).json({
        text: `I can see your image! You asked: ${message}`,
        model: 'groq-vision-placeholder',
        grounded: false,
      });
    }

    const systemPrompt = buildPersonalityPrompt(personality);

    try {
      const response = await callGroq(systemPrompt, message, conversationContext, apiKey);
      const text = extractTextResponse(response);
      if (text) {
        return res.status(200).json({
          text,
          model: 'llama-3.3-70b-versatile',
          grounded: false,
        });
      }
    } catch (error) {
      console.warn('[api/gemini] Groq failed', error?.response?.data || error.message);
      throw error;
    }

    res.status(502).json({
      error: 'AI request failed',
      details: 'No response generated',
    });
  } catch (error) {
    console.error('[api/gemini] Unexpected error', error);
    res.status(500).json({
      error: 'Internal Server Error',
      details: error?.message || error,
    });
  }
};