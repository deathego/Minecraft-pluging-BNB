require('dotenv').config();
const express = require('express');
const cors = require('cors');
const axios = require('axios');
const app = express();
app.use(cors());
app.use(express.json());
const PORT = process.env.PORT || 3000;
// Test route
app.get('/', (req, res) => {
  res.send('Minecraft Quest Backend API is running!');
});
// POST /generate-quest
app.post('/generate-quest', async (req, res) => {
  try {
    const prompt = `
Generate a [Only 1] Minecraft quest in **valid JSON** format only. No explanation, no markdown. Output ONLY the JSON object.

Format:
{
  "title": "string",
  "description": "string",
  "objective": [
    { "type": "collect" | "craft" | "defeat", "item": "string", "amount": number }
  ]
}

Example:
{
  "title": "Diamond Collector",
  "description": "Collect 10 diamonds to complete this quest.",
  "objective": [
    { "type": "collect", "item": "diamond", "amount": 10 }
  ]
}

Now generate a creative Real In-Game Minecraft quest [collect 10 string and 10 white wool and 3 arrow].
`;

    const response = await axios.post(
      'https://openrouter.ai/api/v1/chat/completions',
      {
        model: 'anthropic/claude-3-haiku',
        messages: [
          { role: 'system', content: 'You are a Minecraft quest generator.' },
          { role: 'user', content: prompt }
        ]
      },
      {
        headers: {
          'Authorization': `Bearer ${process.env.OPENROUTER_API_KEY}`,
          'HTTP-Referer': 'https://localhost',
          'Content-Type': 'application/json'
        }
      }
    );
    const questText = response.data.choices[0].message.content;

// Extract JSON block using regex
const jsonMatch = questText.match(/{[\s\S]*}/);

let questJSON;
try {
  if (!jsonMatch) throw new Error('No JSON block found in response');
  questJSON = JSON.parse(jsonMatch[0]);
} catch (parseError) {
  console.error('Failed to parse quest JSON:', parseError);
  return res.status(500).json({ error: 'Failed to parse quest JSON' });
}

    res.json(questJSON);
  } catch (error) {
    console.error('OpenRouter error:', error.response?.data || error.message);
    res.status(500).json({ error: 'Failed to generate quest' });
  }

});

// POST /submit-quest
app.post('/submit-quest', (req, res) => {
  const { playerName, questTitle, completedAt } = req.body;

  console.log(`[✔] ${playerName} completed '${questTitle}' at ${completedAt}`);

  // You can save this to a database later
  res.status(200).json({ message: "Quest completion recorded." });
});

app.listen(PORT, () => {
  console.log(`✅ Server running on port ${PORT}`);
});
