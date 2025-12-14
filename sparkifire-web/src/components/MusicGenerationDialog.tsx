import { useState } from 'react';
import { X, Music4 } from 'lucide-react';
import { musicService } from '../services/musicService';

interface MusicGenerationDialogProps {
  isOpen: boolean;
  onClose: () => void;
}

const outputOptions = ['Vocals', 'Instrumental', 'Composition', 'Music'];
const genreOptions = ['Pop', 'Rap', 'Country', 'Rock', 'R&B', 'Orchestral', 'Acoustic'];
const tempoOptions = ['Fast', 'Slow', 'Medium', 'Upbeat', 'Downbeat'];
const toneOptions = ['Happy', 'Sad', 'Uplifting', 'Dark', 'Dreamy', 'Intense'];
const styleOptions = ['Love Song', 'Funny', 'Dramatic', 'Inspirational', 'Relaxing'];

export function MusicGenerationDialog({ isOpen, onClose }: MusicGenerationDialogProps) {
  const [description, setDescription] = useState('');
  const [outputIndex, setOutputIndex] = useState(0);
  const [genreIndex, setGenreIndex] = useState(0);
  const [tempoIndex, setTempoIndex] = useState(2);
  const [toneIndex, setToneIndex] = useState(0);
  const [styleIndex, setStyleIndex] = useState(0);
  const [isGenerating, setIsGenerating] = useState(false);
  const [statusMessage, setStatusMessage] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleGenerate = async () => {
    const prompt = [
      `Make a ${genreOptions[genreIndex].toLowerCase()} song`,
      `with ${outputOptions[outputIndex].toLowerCase()}`,
      `at a ${tempoOptions[tempoIndex].toLowerCase()} tempo`,
      `and a ${toneOptions[toneIndex].toLowerCase()} tone`,
      `in a ${styleOptions[styleIndex].toLowerCase()} style`,
    ].join(' ');

    const fullPrompt = description.trim()
      ? `${prompt}. ${description.trim()}`
      : prompt;

    setIsGenerating(true);
    setStatusMessage('Sending your music idea to Sparkis composer0 please wait...');

    try {
      const result = await musicService.generateClip(fullPrompt);
      setStatusMessage(result);
    } catch (error) {
      console.error('Music generation error', error);
      setStatusMessage('Sorry, I could not generate music just now. Please try again.');
    } finally {
      setIsGenerating(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-xl w-full max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-9 h-9 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center text-white">
              <Music4 className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-semibold text-gray-900">Music Idea Generator</h2>
              <p className="text-xs text-gray-500">Sparki sketches short clips based on your settings.</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-full hover:bg-gray-100 text-gray-500"
            aria-label="Close music dialog"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="px-6 py-4 space-y-4 overflow-y-auto">
          {/* Description */}
          <div className="space-y-1">
            <label className="block text-sm font-semibold text-gray-800">Description</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              rows={3}
              placeholder="Lyrics, description, style, structure, composition. Examples: make a love song; upbeat pop; dramatic ballad."
            />
          </div>

          {/* Settings */}
          <div className="space-y-2">
            <div className="text-sm font-semibold text-gray-800">Settings</div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <SelectField
                label="Output"
                value={outputIndex}
                onChange={setOutputIndex}
                options={outputOptions}
              />
              <SelectField
                label="Genre"
                value={genreIndex}
                onChange={setGenreIndex}
                options={genreOptions}
              />
              <SelectField
                label="Tempo"
                value={tempoIndex}
                onChange={setTempoIndex}
                options={tempoOptions}
              />
              <SelectField
                label="Tone"
                value={toneIndex}
                onChange={setToneIndex}
                options={toneOptions}
              />
              <SelectField
                label="Style"
                value={styleIndex}
                onChange={setStyleIndex}
                options={styleOptions}
              />
            </div>
          </div>

          {statusMessage && (
            <div className="text-xs text-gray-600 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2">
              {statusMessage}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-gray-600 rounded-lg hover:bg-gray-100"
          >
            Cancel
          </button>
          <button
            onClick={handleGenerate}
            disabled={isGenerating}
            className="px-5 py-2 text-sm font-semibold rounded-lg bg-blue-600 text-white hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center space-x-2"
          >
            {isGenerating && (
              <span className="w-4 h-4 border-2 border-white/60 border-t-transparent rounded-full animate-spin" />
            )}
            <span>{isGenerating ? 'Generating...' : 'Generate'}</span>
          </button>
        </div>
      </div>
    </div>
  );
}

interface SelectFieldProps {
  label: string;
  value: number;
  onChange: (index: number) => void;
  options: string[];
}

function SelectField({ label, value, onChange, options }: SelectFieldProps) {
  return (
    <div className="space-y-1">
      <label className="block text-xs font-medium text-gray-600">{label}</label>
      <select
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
      >
        {options.map((option, index) => (
          <option key={option} value={index}>
            {option}
          </option>
        ))}
      </select>
    </div>
  );
}
