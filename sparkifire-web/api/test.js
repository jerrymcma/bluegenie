// Simple test endpoint to verify serverless functions work

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Content-Type', 'application/json');
  
  const hasReplicateKey = !!process.env.VITE_REPLICATE_API_KEY;
  
  res.status(200).json({
    message: 'Serverless function is working!',
    timestamp: new Date().toISOString(),
    hasReplicateKey: hasReplicateKey,
    keyPrefix: hasReplicateKey ? process.env.VITE_REPLICATE_API_KEY.substring(0, 5) : 'none'
  });
};
