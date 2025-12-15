// Simple test endpoint to verify serverless functions work

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Content-Type', 'application/json');
  
  const replicateKey = process.env.REPLICATE_API_KEY || process.env.VITE_REPLICATE_API_KEY;
  const hasReplicateKey = !!replicateKey;
  
  res.status(200).json({
    message: 'Serverless function is working!',
    timestamp: new Date().toISOString(),
    hasReplicateKey: hasReplicateKey,
    keyPrefix: hasReplicateKey ? replicateKey.substring(0, 5) : 'none',
    envVars: Object.keys(process.env).filter(k => k.includes('REPLICATE') || k.includes('VITE'))
  });
};
