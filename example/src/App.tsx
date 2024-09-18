import { ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { HtMarkView } from 'react-native-htmark-view';
import { useState } from 'react';

//<span style="font-size:20px;margin:0!important;padding:0!important;">This is a <strong>test</strong> <span style="color: yellow;">string</span> with <i><b><span style="font-size: 14px; color: green;">HTML</span></b></i></span>
export default function App() {
  const [maxLines, setMaxLines] = useState<number | undefined>(undefined);
  const html = `<p style="margin: 0px; font-style: normal; font-variant-caps: normal; font-stretch: normal; font-size: 24px; line-height: normal; font-family: Arial; font-size-adjust: none; font-kerning: auto; font-variant-alternates: normal; font-variant-ligatures: normal; font-variant-numeric: normal; font-variant-east-asian: normal; font-variant-position: normal; font-variant-emoji: normal; font-feature-settings: normal; font-optical-sizing: auto; font-variation-settings: normal; -webkit-text-stroke-width: 0px; -webkit-text-stroke-color: rgb(0, 0, 0);"><span style="font-family: Arial-BoldMT; font-weight: bold; font-size: 24px; font-kerning: none;">Lorem Ipsum</span><span style="font-size: 24px; font-kerning: none;">&nbsp;is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum</span></p><p style="margin: 0px; font-style: normal; font-variant-caps: normal; font-stretch: normal; font-size: 24px; line-height: normal; font-family: Arial; font-size-adjust: none; font-kerning: auto; font-variant-alternates: normal; font-variant-ligatures: normal; font-variant-numeric: normal; font-variant-east-asian: normal; font-variant-position: normal; font-variant-emoji: normal; font-feature-settings: normal; font-optical-sizing: auto; font-variation-settings: normal; -webkit-text-stroke-width: 0px; -webkit-text-stroke-color: rgb(0, 0, 0);"><span style="font-size: 24px; font-kerning: none;"></span></p><p style="margin: 0px; font-style: normal; font-variant-caps: normal; font-stretch: normal; font-size:`;
  // const html = `<p style="font-size: 20px">My favorite search engine is <a href="https://duckduckgo.com">link</a>.</p>`;
  const mark = `
  My <b>favorite</b> search *engine* **is** [Duck Duck Go](https://duckduckgo.com).
  `;

  return (
    <View style={styles.container}>
      <ScrollView>
        <TouchableOpacity
          style={{ paddingHorizontal: 16 }}
          onPress={(e) => {
            console.log('🍓[App.parent]');
          }}
        >
          <HtMarkView
            onSizeCalculated={(e) => {
              console.log('🍓[App.]', e.nativeEvent.params)
            }}
            params={{ html: html, maxLines, ellipsize: 'tail' }}
            onPress={() => console.log('🍓[App.press]')}
            onLinkPress={(params) => {
              console.log('🍓[App.]', params);
            }}
          />
          {/*<HtMarkView*/}
          {/*  params={{ markdown: mark, maxLines, ellipsize: 'tail' }}*/}
          {/*  // onPress={() => console.log('🍓[App.press]')}*/}
          {/*  onLinkPress={(params) => {*/}
          {/*    console.log('🍓[App.]', params);*/}
          {/*  }}*/}
          {/*/>*/}

          {/*<MarkdownView*/}
          {/*  params={{ appText: mark, maxLines, ellipsize: 'tail' }}*/}
          {/*  // onPress={() => console.log('🍓[App.press]')}*/}
          {/*  onLinkPress={(params) => {*/}
          {/*    console.log('🍓[App.]', params);*/}
          {/*  }}*/}
          {/*/>*/}
        </TouchableOpacity>
      </ScrollView>
      <Text style={{ fontSize: 14 }}>Styled text</Text>
      <TouchableOpacity
        onPress={() => setMaxLines(maxLines === undefined ? 1 : undefined)}
      >
        <Text>change max</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingTop: 100,
    backgroundColor: 'white',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
