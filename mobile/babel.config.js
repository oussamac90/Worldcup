module.exports = function (api) {
  api.cache(true);
  return {
    presets: ["babel-preset-expo"],
    // Reanimated v4 ships its Babel plugin via react-native-worklets and it MUST be last.
    plugins: ["react-native-worklets/plugin"],
  };
};
