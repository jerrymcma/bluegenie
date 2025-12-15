// Vercel Serverless Function to proxy Replicate API calls
// This solves CORS issues and keeps API keys secure

const axios = require('axios');

module.exports = async (req, res) => {
  // Enable CORS
  res.setHeader('Access-Control-Allow-Credentials', true);
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET,OPTIONS,PATCH,DELETE,POST,PUT');
  res.setHeader(
    'Access-Control-Allow-Headers',
    'X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version'
  );

  // Handle OPTIONS request for CORS preflight
  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  // Only allow POST requests
  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method not allowed' });
  }

  const REPLICATE_API_KEY = process.env.VITE_REPLICATE_API_KEY;

  if (!REPLICATE_API_KEY) {
    return res.status(500).json({ 
      error: 'Replicate API key not configured on server',
      message: 'Please add VITE_REPLICATE_API_KEY to Vercel environment variables'
    });
  }

  const { action, predictionId, lyrics, prompt } = req.body;

  try {
    if (action === 'create') {
      // Create a new prediction
      const response = await axios.post(
        'https://api.replicate.com/v1/models/minimax/music-1.5/predictions',
        {
          input: {
            lyrics: lyrics || '[Verse]\nLa la la\n[Chorus]\nOh oh oh',
            prompt: prompt || 'Pop, modern production, radio ready'
          }
        },
        {
          headers: {
            'Authorization': `Bearer ${REPLICATE_API_KEY}`,
            'Content-Type': 'application/json',
            'Prefer': 'wait'
          },
          timeout: 120000
        }
      );

      return res.status(200).json(response.data);

    } else if (action === 'check') {
      // Check prediction status
      if (!predictionId) {
        return res.status(400).json({ error: 'predictionId is required for check action' });
      }

      const response = await axios.get(
        `https://api.replicate.com/v1/predictions/${predictionId}`,
        {
          headers: {
            'Authorization': `Bearer ${REPLICATE_API_KEY}`
          },
          timeout: 60000
        }
      );

      return res.status(200).json(response.data);

    } else {
      return res.status(400).json({ error: 'Invalid action. Use "create" or "check"' });
    }

  } catch (error) {
    console.error('Replicate API error:', error.response?.data || error.message);
    
    return res.status(error.response?.status || 500).json({
      error: 'Failed to communicate with Replicate',
      message: error.response?.data?.detail || error.message,
      details: error.response?.data
    });
  }
};
