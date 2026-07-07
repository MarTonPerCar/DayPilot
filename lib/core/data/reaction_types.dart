const reactionEmojiByType = {
  'fire': '🔥',
  'clap': '👏',
  'strong': '💪',
  'star': '⭐',
};

String? reactionTypeForEmoji(String emoji) {
  for (final entry in reactionEmojiByType.entries) {
    if (entry.value == emoji) return entry.key;
  }
  return null;
}
