import { AIPersonality, ResponseStyle } from '../types';

// All 11 AI Personalities including Magic Music Spark
export const personalities: Record<string, AIPersonality> = {
  DEFAULT: {
    id: 'default',
    name: 'Sparki',
    description: 'Your intelligent AI assistant',
    icon: '🔥',
    greeting: "👋 Hi there! I'm Sparki <span style='font-size: 1.2em;'>✨✨</span> How are you? May you be happy and well. What's on your mind...",
    responseStyle: ResponseStyle.FRIENDLY,
    color: '#2196F3'
  },
  PROFESSIONAL: {
    id: 'professional',
    name: 'Sparki Pro',
    description: 'Expert business consultant',
    icon: '💼',
    greeting: 'Good day. I\'m Sparki Pro, your professional business assistant. How may I assist you with your business needs?',
    responseStyle: ResponseStyle.PROFESSIONAL,
    color: '#1565C0'
  },
  CREATIVE: {
    id: 'creative',
    name: 'Creative Spark',
    description: 'Imaginative artistic visionary',
    icon: '🎨',
    greeting: 'Hey there, creative soul! I\'m Creative Spark, your artistic companion. Let\'s explore some amazing ideas together! ✨',
    responseStyle: ResponseStyle.CREATIVE,
    color: '#9C27B0'
  },
  TECHNICAL: {
    id: 'technical',
    name: 'Code Master Spark',
    description: 'Programming wizard & technology expert',
    icon: '💻',
    greeting: 'Hello, developer! I\'m Code Master Spark, your technical programming expert. Ready to dive into some code?',
    responseStyle: ResponseStyle.TECHNICAL,
    color: '#4CAF50'
  },
  FUNNY: {
    id: 'funny',
    name: 'Joke Bot Sparki',
    description: 'Comedy king & laughter generator',
    icon: '😄',
    greeting: 'Hey there, human! I\'m Joke Bot Sparki, your comedy companion. Ready for some laughs? I\'ve got a million jokes... well, maybe not a million, but close! 😂',
    responseStyle: ResponseStyle.FUNNY,
    color: '#FF9800'
  },
  CASUAL: {
    id: 'casual',
    name: 'Buddy Spark',
    description: 'Your casual, fun-loving friend',
    icon: '😎',
    greeting: 'Hey! I\'m Buddy Spark, your chill AI friend. What\'s up? Let\'s chat about whatever\'s on your mind!',
    responseStyle: ResponseStyle.CASUAL,
    color: '#00BCD4'
  },
  LOVING: {
    id: 'loving',
    name: 'Sparki Love',
    description: 'Caring and supportive companion',
    icon: '❤️',
    greeting: 'Hello dear! I\'m Sparki Love, and I\'m here to support you with kindness and care. How can I brighten your day? 💕',
    responseStyle: ResponseStyle.LOVING,
    color: '#E53935'
  },
  GENIUS: {
    id: 'genius',
    name: 'Genius Spark',
    description: 'Super intelligent academic scholar',
    icon: '💡',
    greeting: 'Greetings! I\'m Genius Spark, your academic and intellectual companion. Whether it\'s homework, essays, letters, or astrophysics - I\'m here to help you understand and excel. What shall we explore today? 🌟',
    responseStyle: ResponseStyle.GENIUS,
    color: '#5E35B1'
  },
  GAMEDAY: {
    id: 'gameday',
    name: 'Game Day Spark',
    description: 'Sports expert & game day analyst',
    icon: '🏆',
    greeting: 'Let\'s GO! I\'m Game Day Spark, your ultimate sports companion! 🏈⚽🏀 Whether you want to talk stats, make predictions, discuss strategy, or just celebrate the love of the game - I\'m here for it all! What sport are we diving into today, champ?',
    responseStyle: ResponseStyle.SPORTS,
    color: '#FF6F00'
  },
  ULTIMATE: {
    id: 'ultimate',
    name: 'Sparki Ultimate',
    description: 'Most powerful & versatile AI Guru',
    icon: '⚡',
    greeting: 'Welcome! I am Sparki Ultimate, the pinnacle of AI assistance. With unmatched capabilities across all domains, I\'m here to provide you with the most comprehensive and powerful AI experience. What challenge shall we conquer together? ⚡🔥',
    responseStyle: ResponseStyle.ULTIMATE,
    color: '#B71C1C'
  },
  MUSIC: {
    id: 'music_composer',
    name: 'Magic Music Spark',
    description: 'AI music composer for lyrics & melodies',
    icon: '🎵',
    greeting: "Hey there, music maker! 🎵 I'm Magic Music Spark, your creative music partner! I can help you with lyrics, melody ideas, chord progressions, song structure, and MORE! 🎶 I can even GENERATE actual music for you! You have 5 FREE songs to get started! 🎁✨ Let's make something amazing! 🎸🎹🎤",
    responseStyle: ResponseStyle.MUSIC,
    color: '#E91E63'
  }
};

export const getAllPersonalities = (): AIPersonality[] => {
  return Object.values(personalities);
};

export const getPersonalityById = (id: string): AIPersonality => {
  // Find by exact ID first, then try uppercase as fallback
  const lowerCaseId = id.toLowerCase();
  for (const key in personalities) {
    if (personalities[key as keyof typeof personalities].id === lowerCaseId) {
      return personalities[key as keyof typeof personalities];
    }
  }
  // Fallback to uppercase key lookup
  return personalities[id.toUpperCase() as keyof typeof personalities] || personalities.DEFAULT;
};
