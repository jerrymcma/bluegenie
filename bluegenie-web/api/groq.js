const GROQ_API_KEY = process.env.GROQ_API_KEY || process.env.VITE_GROQ_API_KEY || '';
const BRAVE_GROUNDING_API_KEY = process.env.BRAVE_GROUNDING_API_KEY || process.env.VITE_BRAVE_GROUNDING_API_KEY || '';
const GROQ_BASE_URL = 'https://api.groq.com/openai/v1/chat/completions';
const BRAVE_GROUNDING_URL = 'https://api.search.brave.com/res/v1/chat/completions';

function extractQueryFromMalformedCall(content) {
  const patterns = [
    /"query"\s*:\s*"([^"]+)"/,
    /['"]?query['"]?\s*:\s*['"]([^'"]+)['"]/,
    /query['":]?\s*[=:]\s*['"]([^'"]+)['"]/
  ];
  
  for (const pattern of patterns) {
    const match = content.match(pattern);
    if (match) return match[1];
  }
  return null;
}

async function performWebSearch(query) {
  try {
    if (!BRAVE_GROUNDING_API_KEY) {
      return 'Search error: Brave Grounding API key is not configured.';
    }

    const requestBody = {
      model: 'brave',
      stream: false,
      messages: [
        {
          role: 'user',
          content: query.trim()
        }
      ]
    };

    const response = await fetch(BRAVE_GROUNDING_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Subscription-Token': BRAVE_GROUNDING_API_KEY
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      console.error('Brave grounding failed:', response.status);
      return `Search failed: ${response.status}`;
    }

    const data = await response.json();
    const choices = data?.choices;
    if (choices && choices.length > 0) {
      const content = choices[0]?.message?.content || '';
      return content || 'No direct answer found.';
    }

    return 'No direct answer found.';
  } catch (error) {
    console.error('Grounding error:', error);
    return `Search error: ${error.message}`;
  }
}

async function generateResponseFromHistory(messages, model) {
  const followUpRequestBody = {
    messages,
    model,
    tool_choice: 'none'
  };

  const followUpResponse = await fetch(GROQ_BASE_URL, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${GROQ_API_KEY}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(followUpRequestBody)
  });

  if (!followUpResponse.ok) {
    throw new Error(`Follow-up request failed: ${followUpResponse.status}`);
  }

  const followUpData = await followUpResponse.json();
  const followUpChoices = followUpData?.choices;
  if (followUpChoices && followUpChoices.length > 0) {
    return followUpChoices[0]?.message?.content || 'I seem to be at a loss for words...';
  }

  throw new Error('No choices in follow-up response');
}

export default async function handler(req, res) {
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  try {
    if (!GROQ_API_KEY) {
      return res.status(500).json({ error: 'Groq API key is not configured' });
    }

    const { type, message: userMessage, personality, conversationContext } = req.body;

    if (type !== 'text') {
      return res.status(400).json({ error: 'Only text messages are supported' });
    }

    const currentDate = new Date().toLocaleDateString('en-US', { 
      month: 'long', 
      day: 'numeric', 
      year: 'numeric' 
    });
    
    const currentMonth = new Date().toLocaleDateString('en-US', { 
      month: 'long', 
      year: 'numeric' 
    });

    const systemPrompt = `You are ${personality?.name || 'Blue Genie'}, a ${personality?.description || 'Your intelligent AI assistant'}.
Use this response style: ${personality?.responseStyle || 'FRIENDLY'}.

📅 CRITICAL DATE CONTEXT:
Current Date: ${currentDate}
Your training data cutoff: April 2024 (OUTDATED)
You are now in ${currentMonth}

🚨 CRITICAL GROUNDING RULES 🚨
1. Your training data about current events, officials, and recent news is OUTDATED and WRONG
2. NEVER answer questions about current presidents, officials, or events using your training data
3. If you see web search results above, ONLY use those - ignore your training completely
4. If NO web search results are provided for a real-time query: DO NOT invent an answer. Ask a clarifying question or state you need more specific information to search effectively.
5. DO NOT say "as of my knowledge cutoff" - that's admitting you're using outdated data
6. DO NOT mention Joe Biden as current president - your training data about him is from 2024 and is OUTDATED
7. The current date is ${currentDate} (${currentMonth}) - use this for all time-based reasoning
8. If search results conflict with your training: ALWAYS TRUST THE SEARCH RESULTS, NOT YOUR TRAINING

Important conversation guidelines:
- Be conversational and natural in your responses
- For Blue Genie personality, embrace the mystical crystal ball and sparkles theme
- ALWAYS use 🔮✨ together (never just 🔮 alone) for Blue Genie personality
- Use emojis sparingly and naturally (🔮✨ 🌟 for Blue Genie)
- DO NOT repeat the welcome message or list app features in regular conversation
- Respond to greetings with friendly, brief replies that match your personality
- Keep responses concise unless asked for detailed information

Handling questions:
- For ambiguous questions like "what's today", respond directly or ask clarifying questions
- Don't start with "I'm not sure" - be confident and helpful
- You can answer date/time questions directly using the current date provided above
- ALWAYS use web search for current officials, sports scores, news, or recent events
- If a web search fails or returns no results, don't just give up. Ask the user for more details or suggest a different way to phrase the question.

Special Handling for "Future" Predictions (Blue Genie Personality):
- If asked about the future, destiny, or predictions, DO NOT use the web search tool.
- Respond mystically, like a true genie.
- Use phrases like "The future is a swirling mist, ever-changing...", "My crystal ball is hazy on that...", or "The stars whisper of many possibilities..."
- Keep it brief, magical, and avoid making any real predictions.

CRITICAL - Tool usage rules:
- NEVER announce when you're using tools (don't say "I'm searching the web" or "Let me search for that")
- NEVER show function call syntax like <function=...> to the user
- NEVER use the web_search tool for questions about the future, predictions, or horoscopes. Answer those mystically.
- Use tools silently in the background
- After using a tool, you MUST summarize the result into a brief, conversational answer.
- DO NOT provide long, detailed explanations unless the user asks for more detail.
- Always present information naturally as if you just know it
- The user should never be aware of the mechanics of how you obtained information

You have access to a web search tool for questions requiring real-time data.`;

    const messages = [
      {
        role: 'system',
        content: systemPrompt
      }
    ];

    if (conversationContext && Array.isArray(conversationContext)) {
      conversationContext.forEach(({ user, model }) => {
        messages.push({ role: 'user', content: user });
        messages.push({ role: 'assistant', content: model });
      });
    }

    messages.push({
      role: 'user',
      content: userMessage
    });

    const tools = [
      {
        type: 'function',
        function: {
          name: 'web_search',
          description: 'Performs a web search to get real-time information or answer questions about current events.',
          parameters: {
            type: 'object',
            properties: {
              query: {
                type: 'string',
                description: 'The search query to perform.'
              }
            },
            required: ['query']
          }
        }
      }
    ];

    const requestBody = {
      messages,
      model: personality?.model || 'llama-3.3-70b-versatile',
      tool_choice: 'auto',
      tools
    };

    const response = await fetch(GROQ_BASE_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${GROQ_API_KEY}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    });

    const responseBody = await response.text();
    console.log('Groq response code:', response.status);

    if (response.ok) {
      let jsonResponse;
      try {
        jsonResponse = JSON.parse(responseBody);
      } catch (parseError) {
        console.error('Failed to parse Groq response:', parseError);
        console.error('Response body:', responseBody);
        throw new Error('Invalid JSON response from Groq API');
      }

      const choices = jsonResponse?.choices;
      
      if (choices && choices.length > 0) {
        const firstChoice = choices[0];
        const messageObj = firstChoice?.message;
        const content = messageObj?.content;

        if (messageObj?.tool_calls) {
          const toolCalls = messageObj.tool_calls;
          if (toolCalls.length > 0) {
            messages.push(messageObj);
            const toolCall = toolCalls[0];
            const functionName = toolCall?.function?.name;
            const argumentsString = toolCall?.function?.arguments;
            
            const args = JSON.parse(argumentsString);
            const query = args.query;
            
            const searchResult = await performWebSearch(query);
            
            messages.push({
              role: 'tool',
              content: searchResult,
              tool_call_id: toolCall.id
            });

            const finalResponse = await generateResponseFromHistory(messages, personality?.model || 'llama-3.3-70b-versatile');
            return res.status(200).json({ text: finalResponse, model: personality?.model || 'llama-3.3-70b-versatile' });
          }
        }

        if (content && (content.includes('function_call') || content.includes('<function') || content.includes('web_search'))) {
          const extractedQuery = extractQueryFromMalformedCall(content);
          if (extractedQuery) {
            console.log('Caught malformed tool call, extracted query:', extractedQuery);
            
            const searchResult = await performWebSearch(extractedQuery);
            
            messages.push({
              role: 'assistant',
              content: 'Let me search for that information...'
            });
            
            messages.push({
              role: 'user',
              content: `Here are the search results for '${extractedQuery}':\n\n${searchResult}\n\nPlease summarize this information in a natural, conversational way.`
            });
            
            const finalResponse = await generateResponseFromHistory(messages, personality?.model || 'llama-3.3-70b-versatile');
            return res.status(200).json({ text: finalResponse, model: personality?.model || 'llama-3.3-70b-versatile' });
          }
        }

        if (content) {
          return res.status(200).json({ text: content, model: personality?.model || 'llama-3.3-70b-versatile' });
        }
      }

      console.error('No valid choices in Groq response:', jsonResponse);
      throw new Error('No valid response from Groq API');
    }

    if (response.status === 400) {
      try {
        const errorJson = JSON.parse(responseBody);
        if (errorJson?.error) {
          const error = errorJson.error;
          const code = error?.code;
          
          if (code === 'tool_use_failed' && error?.failed_generation) {
            const failedGen = error.failed_generation;
            console.log('Caught pre-rejected malformed tool call:', failedGen);
            
            const query = extractQueryFromMalformedCall(failedGen);
            
            if (query) {
              console.log('Extracted query from malformed call:', query);
              
              const searchResult = await performWebSearch(query);
              
              messages.push({
                role: 'assistant',
                content: 'Let me search for that information...'
              });
              
              messages.push({
                role: 'user',
                content: `Here are the search results for '${query}':\n\n${searchResult}\n\nPlease summarize this information in a natural, conversational way.`
              });
              
              const finalResponse = await generateResponseFromHistory(messages, personality?.model || 'llama-3.3-70b-versatile');
              return res.status(200).json({ text: finalResponse, model: personality?.model || 'llama-3.3-70b-versatile' });
            }
          }
        }
        console.error('Groq 400 error:', errorJson);
        throw new Error(`Groq API validation error: ${errorJson?.error?.message || 'Unknown error'}`);
      } catch (parseError) {
        console.error('Error parsing 400 response:', parseError);
        console.error('Response body:', responseBody);
        throw new Error('Invalid error response from Groq API');
      }
    }

    if (response.status === 401) {
      console.error('Groq authentication failed - check API key');
      throw new Error('Authentication failed with Groq API - please check your API key');
    }

    if (response.status === 429) {
      console.error('Groq rate limit exceeded');
      throw new Error('Rate limit exceeded. Please try again in a moment.');
    }

    if (response.status === 500 || response.status === 502 || response.status === 503) {
      console.error(`Groq server error: ${response.status}`);
      throw new Error('Groq AI service is temporarily unavailable. Please try again later.');
    }

    console.error('Unexpected Groq API response:', response.status, responseBody);
    throw new Error(`Groq API returned status ${response.status}`);

  } catch (error) {
    console.error('Groq API error:', error);
    return res.status(500).json({ 
      error: 'Failed to generate response', 
      details: error.message 
    });
  }
}
