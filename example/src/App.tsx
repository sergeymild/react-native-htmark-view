import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { HtMarkView } from 'react-native-htmark-view';
import { useState } from 'react';

//<span style="font-size:20px;margin:0!important;padding:0!important;">This is a <strong>test</strong> <span style="color: yellow;">string</span> with <i><b><span style="font-size: 14px; color: green;">HTML</span></b></i></span>
export default function App() {
  const [maxLines, setMaxLines] = useState<number | undefined>(undefined);
  const html = `<span style="font-size:20px; color:red;">first first first<b>first</b>first <a href="https://google.com">link</a> firstfirstfirstfirstfirstfirstfirstfirstfirst12345678firstfirstfirstfirstfirstfirstfirstfirstbefore<span style="color: green;">first</span>after12345678</span>`;
  // const html = `<p style="font-size: 20px">My favorite search engine is <a href="https://duckduckgo.com">link</a>.</p>`;
  const mark = `
  My <b>favorite</b> search *engine* **is** [Duck Duck Go](https://duckduckgo.com).
  `;

  return (
    <View style={styles.container}>
      <TouchableOpacity
        style={{ paddingHorizontal: 16 }}
        onPress={(e) => {
          console.log('🍓[App.parent]');
        }}
      >
        <HtMarkView
          params={{ html: html, maxLines, ellipsize: 'tail' }}
          // onPress={() => console.log('🍓[App.press]')}
          // onLinkPress={(params) => {
          //   console.log('🍓[App.]', params);
          // }}
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
